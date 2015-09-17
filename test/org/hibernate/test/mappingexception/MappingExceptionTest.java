// $Id: SQLExceptionConversionTest.java 6847 2005-05-21 15:46:41Z oneovthafew $
package org.hibernate.test.mappingexception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.DuplicateMappingException;
import org.hibernate.Hibernate;
import org.hibernate.InvalidMappingException;
import org.hibernate.MappingException;
import org.hibernate.MappingNotFoundException;
import org.hibernate.test.TestCase;
import org.hibernate.util.ConfigHelper;

/**
 * Test for various mapping exceptions thrown when mappings are not found or invalid.
 *
 * @author Max Rydahl Andersen
 */
public class MappingExceptionTest extends TestCase {

	public MappingExceptionTest(String name) {
		super( name );
	}

	protected String[] getMappings() {
		return new String[] { "mappingexception/User.hbm.xml" };
	}

	public void testNotFound() throws MappingException, MalformedURLException {

		try {
			getCfg().addCacheableFile( "completelybogus.hbm.xml" );
			fail();
		}
		catch ( MappingNotFoundException e ) {
			assertEquals( e.getType(), "file" );
			assertEquals( e.getPath(), "completelybogus.hbm.xml" );
		}

		try {
			getCfg().addCacheableFile( new File( "completelybogus.hbm.xml" ) );
			fail();
		}
		catch ( MappingNotFoundException e ) {
			assertEquals( e.getType(), "file" );
			assertEquals( e.getPath(), "completelybogus.hbm.xml" );
		}

		try {
			getCfg().addClass( Hibernate.class ); // TODO: String.class result in npe, because no classloader exists for it
			fail();
		}
		catch ( MappingNotFoundException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), "org/hibernate/Hibernate.hbm.xml" );
		}

		try {
			getCfg().addFile( "completelybogus.hbm.xml" );
			fail();
		}
		catch ( MappingNotFoundException e ) {
			assertEquals( e.getType(), "file" );
			assertEquals( e.getPath(), "completelybogus.hbm.xml" );
		}

		try {
			getCfg().addFile( new File( "completelybogus.hbm.xml" ) );
			fail();
		}
		catch ( MappingNotFoundException inv ) {
			assertEquals( inv.getType(), "file" );
			assertEquals( inv.getPath(), "completelybogus.hbm.xml" );
		}

		try {
			getCfg().addInputStream( new ByteArrayInputStream( new byte[0] ) );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "input stream" );
			assertEquals( inv.getPath(), null );
		}

		try {
			getCfg().addResource( "nothere" );
			fail();
		}
		catch ( MappingNotFoundException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), "nothere" );
		}

		try {
			getCfg().addResource( "nothere", getClass().getClassLoader() );
			fail();
		}
		catch ( MappingNotFoundException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), "nothere" );
		}

		try {
			getCfg().addURL( new URL( "file://nothere" ) );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "URL" );
			assertEquals( inv.getPath(), "file://nothere" );
		}
	}

	public void testDuplicateMapping() {
		try {
			getCfg().addResource( getBaseForMappings() + "mappingexception/User.hbm.xml" );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), getBaseForMappings() + "mappingexception/User.hbm.xml" );
			assertClassAssignability( inv.getCause().getClass(), DuplicateMappingException.class );
		}
	}

	void copy(InputStream in, File dst) throws IOException {
		OutputStream out = new FileOutputStream( dst );

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ( ( len = in.read( buf ) ) > 0 ) {
			out.write( buf, 0, len );
		}
		in.close();
		out.close();
	}

	public void testInvalidMapping() throws MappingException, IOException {
		String resourceName = getBaseForMappings() + "mappingexception/InvalidMapping.hbm.xml";

		File file = File.createTempFile( "TempInvalidMapping", ".hbm.xml" );
		file.deleteOnExit();
		copy( ConfigHelper.getConfigStream( resourceName ), file );

		try {
			getCfg().addCacheableFile( file.getAbsolutePath() );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "file" );
			assertNotNull( inv.getPath() );
			assertTrue( inv.getPath().endsWith( ".hbm.xml" ) );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addCacheableFile( file );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "file" );
			assertNotNull( inv.getPath() );
			assertTrue( inv.getPath().endsWith( ".hbm.xml" ) );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addClass( InvalidMapping.class );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), "org/hibernate/test/mappingexception/InvalidMapping.hbm.xml" );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addFile( file.getAbsolutePath() );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "file" );
			assertEquals( inv.getPath(), file.getPath() );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addFile( file );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "file" );
			assertEquals( inv.getPath(), file.getPath() );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}


		try {
			getCfg().addInputStream( ConfigHelper.getResourceAsStream( resourceName ) );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "input stream" );
			assertEquals( inv.getPath(), null );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addResource( resourceName );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), resourceName );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addResource( resourceName, getClass().getClassLoader() );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "resource" );
			assertEquals( inv.getPath(), resourceName );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}

		try {
			getCfg().addURL( ConfigHelper.findAsResource( resourceName ) );
			fail();
		}
		catch ( InvalidMappingException inv ) {
			assertEquals( inv.getType(), "URL" );
			assertTrue( inv.getPath().endsWith( "InvalidMapping.hbm.xml" ) );
			assertTrue( !( inv.getCause() instanceof MappingNotFoundException ) );
		}
	}

	public static Test suite() {
		return new TestSuite( MappingExceptionTest.class );
	}
}
