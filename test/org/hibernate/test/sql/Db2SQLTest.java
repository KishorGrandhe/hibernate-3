//$Id$
package org.hibernate.test.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle9Dialect;

/**
 * @author Max Rydahl Andersen
 */
public class Db2SQLTest extends HandSQLTest {
	
	public Db2SQLTest(String str) {
		super(str);
	}

	protected String[] getMappings() {
		return new String[] { "sql/Db2Employment.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite(Db2SQLTest.class);
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof DB2Dialect);
	}

}

