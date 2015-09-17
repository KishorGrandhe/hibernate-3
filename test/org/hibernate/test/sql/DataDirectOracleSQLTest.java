//$Id$
package org.hibernate.test.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.dialect.DataDirectOracle9Dialect;
import org.hibernate.dialect.Dialect;

/**
 * @author Max Rydahl Andersen
 */
public class DataDirectOracleSQLTest extends HandSQLTest {
	
	public DataDirectOracleSQLTest(String str) {
		super(str);
	}

	protected String[] getMappings() {
		return new String[] { "sql/OracleEmployment.hbm.xml", "sql/OracleDataDirectDriverStoredProcedures.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite(DataDirectOracleSQLTest.class);
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof DataDirectOracle9Dialect );
	}

}

