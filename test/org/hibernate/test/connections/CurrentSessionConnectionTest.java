// $Id: CurrentSessionConnectionTest.java 9595 2006-03-10 18:14:21Z steve.ebersole@jboss.com $
package org.hibernate.test.connections;

import org.hibernate.Session;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Implementation of CurrentSessionConnectionTest.
 *
 * @author Steve Ebersole
 */
public class CurrentSessionConnectionTest extends AggressiveReleaseTest {

	public CurrentSessionConnectionTest(String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( CurrentSessionConnectionTest.class );
	}

	protected Session getSessionUnderTest() throws Throwable {
		return getSessions().getCurrentSession();
	}

	protected void release(Session session) {
		// do nothing, txn synch should release session as part of current-session definition
	}
}
