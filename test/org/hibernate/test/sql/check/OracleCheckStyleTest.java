package org.hibernate.test.sql.check;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle9Dialect;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * todo: describe OracleCheckStyleTest
 *
 * @author Steve Ebersole
 */
public class OracleCheckStyleTest extends ResultCheckStyleTest {
	public OracleCheckStyleTest(String name) {
		super( name );
	}

	protected String[] getMappings() {
		return new String[] { "sql/check/oracle-mappings.hbm.xml" };
	}

	public boolean appliesTo(Dialect dialect) {
		return dialect instanceof Oracle9Dialect;
	}

	public static Test suite() {
		return new TestSuite( OracleCheckStyleTest.class );
	}
}
