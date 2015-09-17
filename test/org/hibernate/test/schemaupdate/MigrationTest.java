package org.hibernate.test.schemaupdate;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.cfg.Configuration;
import org.hibernate.test.TestCase;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

/**
 * @author Max Rydahl Andersen
 */
public class MigrationTest extends TestCase {
	
	public MigrationTest(String str) {
		super(str);
	}

	public void testSimpleColumnAddition() {
		Configuration v1cfg = new Configuration();
		v1cfg.addResource(getBaseForMappings() + "/schemaupdate/1_Version.hbm.xml");
		
		new SchemaExport(v1cfg).execute(false, true, true, false);
		
		SchemaUpdate v1schemaUpdate = new SchemaUpdate(v1cfg);		
		v1schemaUpdate.execute(true, true);
		
		assertEquals(0, v1schemaUpdate.getExceptions().size());
		
		Configuration v2cfg = new Configuration();
		v2cfg.addResource(getBaseForMappings() + "/schemaupdate/2_Version.hbm.xml");
		
		SchemaUpdate v2schemaUpdate = new SchemaUpdate(v2cfg);
		v2schemaUpdate.execute(true, true);
		assertEquals(0, v2schemaUpdate.getExceptions().size());
		
	}
	
	protected String[] getMappings() {
		return new String[] {  };
	}

	public static Test suite() {
		return new TestSuite(MigrationTest.class);
	}

}

