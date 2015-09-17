package org.hibernate.test.util.dtd;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.hibernate.cfg.Configuration;


/**
 * todo: describe EntityResolverTest
 *
 * @author Steve Ebersole
 */
public class EntityResolverTest extends TestCase {
	public EntityResolverTest(String name) {
		super( name );
	}

	public void testEntityIncludeResolution() {
		Configuration cfg = new Configuration();
		cfg.addResource( "org/hibernate/test/util/dtd/Parent.hbm.xml" );
		cfg.buildMappings();
	}

	public static Test suite() {
		return new TestSuite( EntityResolverTest.class );
	}
}
