//$Id: MySQLTest.java 9014 2006-01-10 23:04:27Z steveebersole $
package org.hibernate.test.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Dialect;

/**
 * @author Gavin King
 */
public class MySQLTest extends HandSQLTest {

	public MySQLTest(String str) {
		super(str);
	}

	protected String[] getMappings() {
		return new String[] { "sql/MySQLEmployment.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite(MySQLTest.class);
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof MySQLDialect );
	}
}

