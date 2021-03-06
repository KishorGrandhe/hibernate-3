// $Id: ConnectionsSuite.java 6974 2005-05-31 21:32:56Z steveebersole $
package org.hibernate.test.connections;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Implementation of ConnectionsSuite.
 *
 * @author Steve Ebersole
 */
public class ConnectionsSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite( "Connection-management tests");
		suite.addTest( BasicConnectionProviderTest.suite() );
		suite.addTest( SuppliedConnectionTest.suite() );
		suite.addTest( AggressiveReleaseTest.suite() );
		suite.addTest( CurrentSessionConnectionTest.suite() );
		return suite;
	}
}
