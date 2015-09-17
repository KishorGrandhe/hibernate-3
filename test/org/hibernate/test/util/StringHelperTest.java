package org.hibernate.test.util;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.hibernate.util.StringHelper;

/**
 * todo: describe StringHelperTest
 *
 * @author Steve Ebersole
 */
public class StringHelperTest extends TestCase {
	public StringHelperTest(String string) {
		super( string );
	}

	public void testAliasGeneration() {
		assertSimpleAlias( "xyz", "xyz_" );
		assertSimpleAlias( "_xyz", "xyz_" );
		assertSimpleAlias( "!xyz", "xyz_" );
		assertSimpleAlias( "abcdefghijklmnopqrstuvwxyz", "abcdefghij_" );
	}

	private void assertSimpleAlias(String source, String expected) {
		assertEquals( expected, StringHelper.generateAlias( source ) );
	}

	public static Test suite() {
		return new TestSuite( StringHelperTest.class );
	}
}
