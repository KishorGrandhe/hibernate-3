// $Id: TimestampGeneratedValuesWithCachingTest.java 10435 2006-09-01 23:53:40Z steve.ebersole@jboss.com $
package org.hibernate.test.generated;

import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.Dialect;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Implementation of TimestampGeneratedValuesWithCachingTest.
 *
 * @author Steve Ebersole
 */
public class TimestampGeneratedValuesWithCachingTest extends AbstractGeneratedPropertyTest {

	public TimestampGeneratedValuesWithCachingTest(String x) {
		super( x );
	}

	protected final String[] getMappings() {
		return new String[] { "generated/MSSQLGeneratedPropertyEntity.hbm.xml" };
	}

	public boolean appliesTo(Dialect dialect) {
		// this test is specific to Sybase/SQLServer as it is testing support
		// for their TIMESTAMP datatype...
		return ( dialect instanceof SybaseDialect );
	}

	public static Test suite() {
		return new TestSuite( TimestampGeneratedValuesWithCachingTest.class );
	}
}
