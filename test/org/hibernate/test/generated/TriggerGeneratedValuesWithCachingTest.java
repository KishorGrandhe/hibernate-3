// $Id: TriggerGeneratedValuesWithCachingTest.java 10435 2006-09-01 23:53:40Z steve.ebersole@jboss.com $
package org.hibernate.test.generated;

import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.Dialect;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Implementation of TriggerGeneratedValuesWithoutCachingTest.
 *
 * @author Steve Ebersole
 */
public class TriggerGeneratedValuesWithCachingTest extends AbstractGeneratedPropertyTest {

	public TriggerGeneratedValuesWithCachingTest(String x) {
		super( x );
	}

	protected final String[] getMappings() {
		return new String[] { "generated/GeneratedPropertyEntity.hbm.xml" };
	}

	public boolean appliesTo(Dialect dialect) {
		// currently have only defined triggers for oracle...
		// TODO : add more triggers for dialects which allow mods in triggers...
		return ( dialect instanceof Oracle9Dialect );
	}

	public static Test suite() {
		return new TestSuite( TriggerGeneratedValuesWithCachingTest.class );
	}
}
