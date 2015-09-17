package org.hibernate.test.lob;

import java.sql.Clob;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.test.TestCase;
import org.hibernate.Session;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.dialect.H2Dialect;

/**
 * Test various access scenarios for eager and lazy materialization
 * of CLOB data, as well as bounded and unbounded materialization
 * and mutation.
 *
 * @author Steve Ebersole
 */
public class ClobTest extends TestCase {
	private static final int CLOB_SIZE = 10000;

	public ClobTest(String name) {
		super( name );
	}

	protected String[] getMappings() {
		return new String[] { "lob/ClobHoldingEntity.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite( ClobTest.class );
	}

	public void testBoundedMaterializedClobAccess() {
		if ( !supportsExpectedLobUsagePattern() ) {
			return;
		}

		String original = buildRecursively( CLOB_SIZE, 'x' );
		String changed = buildRecursively( CLOB_SIZE, 'y' );

		Session s = openSession();
		s.beginTransaction();
		ClobHoldingEntity entity = new ClobHoldingEntity();
		entity.setSerialData( original );
		s.save( entity );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId() );
		assertEquals( CLOB_SIZE, entity.getSerialData().length() );
		assertEquals( original, entity.getSerialData() );
		entity.setSerialData( changed );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId() );
		assertEquals( CLOB_SIZE, entity.getSerialData().length() );
		assertEquals( changed, entity.getSerialData() );
		s.delete( entity );
		s.getTransaction().commit();
		s.close();
	}

	public void testBoundedClobLocatorAccess() throws Throwable {
		if ( !supportsExpectedLobUsagePattern() ) {
			return;
		}

		String original = buildRecursively( CLOB_SIZE, 'x' );
		String changed = buildRecursively( CLOB_SIZE, 'y' );

		Session s = openSession();
		s.beginTransaction();
		ClobHoldingEntity entity = new ClobHoldingEntity();
		entity.setClobData( Hibernate.createClob( original ) );
		s.save( entity );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId() );
		assertEquals( CLOB_SIZE, entity.getClobData().length() );
		assertEquals( original, extractData( entity.getClobData() ) );
		s.getTransaction().commit();
		s.close();

		// test mutation via setting the new clob data...
		if ( supportsLobValueChangePropogation() ) {
			s = openSession();
			s.beginTransaction();
			entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId(), LockMode.UPGRADE );
			entity.getClobData().truncate( 1 );
			entity.getClobData().setString( 1, changed );
			s.getTransaction().commit();
			s.close();

			s = openSession();
			s.beginTransaction();
			entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId(), LockMode.UPGRADE );
			assertNotNull( entity.getClobData() );
			assertEquals( CLOB_SIZE, entity.getClobData().length() );
			assertEquals( changed, extractData( entity.getClobData() ) );
			entity.getClobData().truncate( 1 );
			entity.getClobData().setString( 1, original );
			s.getTransaction().commit();
			s.close();
		}

		// test mutation via supplying a new clob locator instance...
		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId(), LockMode.UPGRADE );
		assertNotNull( entity.getClobData() );
		assertEquals( CLOB_SIZE, entity.getClobData().length() );
		assertEquals( original, extractData( entity.getClobData() ) );
		entity.setClobData( Hibernate.createClob( changed ) );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId() );
		assertEquals( CLOB_SIZE, entity.getClobData().length() );
		assertEquals( changed, extractData( entity.getClobData() ) );
		s.delete( entity );
		s.getTransaction().commit();
		s.close();

	}

	public void testUnboundedClobLocatorAccess() throws Throwable {
		if ( !supportsExpectedLobUsagePattern() || ! supportsUnboundedLobLocatorMaterialization() ) {
			return;
		}

		// Note: unbounded mutation of the underlying lob data is completely
		// unsupported; most databases would not allow such a construct anyway.
		// Thus here we are only testing materialization...

		String original = buildRecursively( CLOB_SIZE, 'x' );

		Session s = openSession();
		s.beginTransaction();
		ClobHoldingEntity entity = new ClobHoldingEntity();
		entity.setClobData( Hibernate.createClob( original ) );
		s.save( entity );
		s.getTransaction().commit();
		s.close();

		// load the entity with the clob locator, and close the session/transaction;
		// at that point it is unbounded...
		s = openSession();
		s.beginTransaction();
		entity = ( ClobHoldingEntity ) s.get( ClobHoldingEntity.class, entity.getId() );
		s.getTransaction().commit();
		s.close();

		assertEquals( CLOB_SIZE, entity.getClobData().length() );
		assertEquals( original, extractData( entity.getClobData() ) );

		s = openSession();
		s.beginTransaction();
		s.delete( entity );
		s.getTransaction().commit();
		s.close();
	}

	private String extractData(Clob clob) throws Throwable {
		if ( getDialect() instanceof H2Dialect ) {
			return clob.getSubString( 1, ( int ) clob.length() );
		}
		else {
			char[] data = new char[ (int) clob.length() ];
			clob.getCharacterStream().read( data );
			return new String( data );
		}
	}


	private String buildRecursively(int size, char baseChar) {
		StringBuffer buff = new StringBuffer();
		for( int i = 0; i < size; i++ ) {
			buff.append( baseChar );
		}
		return buff.toString();
	}
}
