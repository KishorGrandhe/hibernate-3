package org.hibernate.test.jpa;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.hibernate.test.jpa.lock.EJB3LockTest;
import org.hibernate.test.jpa.lock.RepeatableReadTest;
import org.hibernate.test.jpa.removed.RemovedEntityTest;
import org.hibernate.test.jpa.proxy.JPAProxyTest;
import org.hibernate.test.jpa.fetch.FetchingTest;
import org.hibernate.test.jpa.ql.JPAQLComplianceTest;

/**
 * @author Steve Ebersole
 */
public class JPAComplianceSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite( "JPA-compliance tests");
		suite.addTest( EJB3LockTest.suite() );
		suite.addTest( RepeatableReadTest.suite() );
		suite.addTest( JPAProxyTest.suite() );
		suite.addTest( FetchingTest.suite() );
		suite.addTest( JPAQLComplianceTest.suite()  );
		suite.addTest( RemovedEntityTest.suite() );
		return suite;
	}
}
