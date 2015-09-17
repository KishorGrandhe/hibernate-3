//$Id$
package org.hibernate.test.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SybaseDialect;

/**
 * @author Gavin King
 */
public class MSSQLTest extends HandSQLTest {

	public MSSQLTest(String str) {
		super( str );
	}

	public static Test suite() {
		return new TestSuite( MSSQLTest.class );
	}

	protected String[] getMappings() {
		return new String[] {"sql/MSSQLEmployment.hbm.xml"};
	}

	public boolean appliesTo(Dialect dialect) {
		return ( dialect instanceof SybaseDialect );
	}
}

