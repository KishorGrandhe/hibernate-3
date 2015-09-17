package org.hibernate.test;

/**
 * @author Steve Ebersole
 */
public abstract class DatabaseSpecificTestCase extends TestCase {
	public DatabaseSpecificTestCase(String name) {
		super( name );
	}

	protected void runTest() throws Throwable {
		// Note: this protection comes into play when running
		// tests individually.  The suite as a whole is already
		// "protected" by the fact that these tests are actually
		// filtered out of the suite
		if ( appliesTo( getDialect() ) ) {
			super.runTest();
		}
		else {
			SKIP_LOG.warn( "skipping database-specific test [" + fullTestName() + "] for dialect [" + getDialect().getClass().getName() + "]" );
		}
	}
}
