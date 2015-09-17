//$Id$
package org.hibernate.test.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.DataDirectOracle9Dialect;
import org.hibernate.HibernateException;

import java.sql.SQLException;

/**
 * @author Gavin King
 */
public class OracleSQLTest extends HandSQLTest {
	public void testHandSQL() {
		super.testHandSQL();
	}

	public void testScalarStoredProcedure() throws HibernateException, SQLException {
		super.testScalarStoredProcedure();
	}

	public void testParameterHandling() throws HibernateException, SQLException {
		super.testParameterHandling();
	}

	public void testEntityStoredProcedure() throws HibernateException, SQLException {
		super.testEntityStoredProcedure();
	}

	public OracleSQLTest(String str) {
		super(str);
	}

	protected String[] getMappings() {
		return new String[] { "sql/OracleEmployment.hbm.xml", "sql/OracleDriverStoredProcedures.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite(OracleSQLTest.class);
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof Oracle9Dialect ) && !( dialect instanceof DataDirectOracle9Dialect );
	}

}

