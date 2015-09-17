package org.hibernate.test.generatedkeys.select;

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
public class SelectGeneratorTest extends DatabaseSpecificTestCase {
	public SelectGeneratorTest(String x) {
		super( x );
	}

	// TODO : need to determine appropriate physical generation strategies for select-generator testing on other databases...

	protected void configure(Configuration cfg) {
		super.configure( cfg );
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof Oracle9Dialect );
	}

	protected String[] getMappings() {
		return new String[] {
				"generatedkeys/select/MyEntity.hbm.xml"
		};
	}

	public static Test suite() {
		return new TestSuite( SelectGeneratorTest.class );
	}

	public void testJDBC3GetGeneratedKeysSupportOnOracle() {
		if ( getDialect() instanceof DataDirectOracle9Dialect ) {
			reportSkip( "DataDirect drivers known to not support JDBC3 getGeneratedKeys for Oracle", "oracle getGeneratedKeys support" );
			return;
		}
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
