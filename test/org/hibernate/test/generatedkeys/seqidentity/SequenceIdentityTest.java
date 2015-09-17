package org.hibernate.test.generatedkeys.seqidentity;

import org.hibernate.test.DatabaseSpecificTestCase;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DataDirectOracle9Dialect;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Steve Ebersole
 */
public class SequenceIdentityTest extends DatabaseSpecificTestCase {
	public SequenceIdentityTest(String x) {
		super( x );
	}

	protected void configure(Configuration cfg) {
		super.configure( cfg );
	}

	public boolean appliesTo(Dialect dialect) {
		// the DataDirect driver for Oracle known to not support
		// JDBC3 getGeneratedKeys...
		return ( dialect instanceof Oracle9Dialect ) && ( ! ( dialect instanceof DataDirectOracle9Dialect ) ) ;
	}

	protected String[] getMappings() {
		return new String[] {
				"generatedkeys/seqidentity/MyEntity.hbm.xml"
		};
	}

	public static Test suite() {
		return new TestSuite( SequenceIdentityTest.class );
	}

	public void testSequenceIdentityGenerator() {
		Session session = openSession();
		session.beginTransaction();

		MyEntity e = new MyEntity( "entity-1" );
		session.save( e );

		// this insert should happen immediately!
		assertEquals( "id not generated through forced insertion", new Long(1), e.getId() );

		session.delete( e );
		session.getTransaction().commit();
		session.close();
	}
}
