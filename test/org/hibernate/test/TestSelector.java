package org.hibernate.test;

import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.Set;
import java.util.HashSet;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * A custom Ant FileSelector used to limit the tests run from the Ant
 * build script to only those defined in the {@link AllTests} suite.
 * <p/>
 * {@link AllTests} is used/maintained by the developers to easily
 * run the test suite in all IDEs.  It represents all the tests
 * which should actually be run and included in test results.
 * 
 * @author Steve Ebersole
 */
public class TestSelector extends junit.framework.TestCase implements FileSelector {

	private final Set allTestClassNames = new HashSet();

	public TestSelector() {
		TestSuiteVisitor.Handler handler = new TestSuiteVisitor.Handler() {
			public void handleTestCase(Test test) {
				allTestClassNames.add( test.getClass().getName() );
			}
			public void startingTestSuite(TestSuite suite) {}
			public void completedTestSuite(TestSuite suite) {}
		};
		TestSuiteVisitor visitor = new TestSuiteVisitor( handler );
		visitor.visit( ( TestSuite ) AllTests.suite() );
	}


	// FileSelector impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public boolean isSelected(File dir, String fileFromDir, File fullFile) throws BuildException {
		String correspondingClassName = determineClassName( fileFromDir );
		return allTestClassNames.contains( correspondingClassName );
	}

	private String determineClassName(String file) {
		if ( file.endsWith( ".class" ) ) {
			file = file.substring( 0, file.length() - 6 );
		}
		else if ( file.endsWith( ".java" ) ) {
			file = file.substring( 0, file.length() - 5 );
		}
		else {
			return null;
		}
		file = file.replace( '\\', '.' );
		file = file.replace( '/', '.' );
		return file;
	}


	// unit tests ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void testFileSelection() {
		File file = new File( "" );
		assertTrue( "valid test not selected", isSelected( file, "org/hibernate/test/hql/HQLTest.class", file ) );
		assertFalse( "invalid test selected", isSelected( file, "hithere.class", file ) );
	}
}
