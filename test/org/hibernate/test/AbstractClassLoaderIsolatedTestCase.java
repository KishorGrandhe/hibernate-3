package org.hibernate.test;

import junit.framework.TestCase;

/**
 * A specialized TestCase for running tests in an isolated class-loader
 *
 * @author Steve Ebersole
 */
public abstract class AbstractClassLoaderIsolatedTestCase extends TestCase {
	private ClassLoader parentLoader;
	private ClassLoader isolatedLoader;

	protected void setUp() throws Exception {
		parentLoader = Thread.currentThread().getContextClassLoader();
		isolatedLoader = buildIsolatedClassLoader( parentLoader );
		Thread.currentThread().setContextClassLoader( isolatedLoader );
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Thread.currentThread().setContextClassLoader( parentLoader );
		releaseIsolatedClassLoader( isolatedLoader );
		parentLoader = null;
		isolatedLoader = null;
	}

	protected abstract ClassLoader buildIsolatedClassLoader(ClassLoader parent);

	protected abstract void releaseIsolatedClassLoader(ClassLoader isolatedLoader);
}
