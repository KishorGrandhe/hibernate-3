package org.hibernate.test.instrument.runtime;

import org.hibernate.bytecode.BytecodeProvider;
import org.hibernate.bytecode.cglib.BytecodeProviderImpl;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Steve Ebersole
 */
public class CGLIBInstrumentationTest extends AbstractTransformingClassLoaderInstrumentTestCase {
	protected BytecodeProvider buildBytecodeProvider() {
		return new BytecodeProviderImpl();
	}

	public static Test suite() {
		return new TestSuite( CGLIBInstrumentationTest.class );
	}
}
