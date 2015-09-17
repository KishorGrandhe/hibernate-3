package org.hibernate.test;

import junit.framework.TestSuite;
import junit.framework.Test;

import java.util.Enumeration;

/**
 * Handles walking a TestSuite hierarchy for recognition of individual tests.
 * 
 * @author Steve Ebersole
 */
public class TestSuiteVisitor {

	private final Handler handler;

	public TestSuiteVisitor(Handler handler) {
		this.handler = handler;
	}

	public void visit(TestSuite testSuite) {
		handler.startingTestSuite( testSuite );
		Enumeration tests = testSuite.tests();
		while ( tests.hasMoreElements() ) {
			Test test = ( Test ) tests.nextElement();
			if ( test instanceof TestSuite ) {
				visit( ( TestSuite ) test );
			}
			else {
				handler.handleTestCase( test );
			}
		}
		handler.completedTestSuite( testSuite );
	}

	public static interface Handler {
		public void handleTestCase(Test test);
		public void startingTestSuite(TestSuite suite);
		public void completedTestSuite(TestSuite suite);
	}

}
