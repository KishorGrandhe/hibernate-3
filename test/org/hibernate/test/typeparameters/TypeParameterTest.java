//$Id: TypeParameterTest.java 10421 2006-09-01 20:52:35Z steve.ebersole@jboss.com $
package org.hibernate.test.typeparameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.hibernate.classic.Session;
import org.hibernate.Transaction;
import org.hibernate.test.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test for parameterizable types.
 * 
 * @author Michael Gloegl
 */
public class TypeParameterTest extends TestCase {

	public TypeParameterTest(String name) {
		super(name);
	}

	/**
	 * @see org.hibernate.test.TestCase#getMappings()
	 */
	protected String[] getMappings() {
		return new String[] {
				"typeparameters/Typedef.hbm.xml", 
				"typeparameters/Widget.hbm.xml"
		};
	}

	public void testSave() throws Exception {
		deleteData();

		Session s = openSession();

		Transaction t = s.beginTransaction();

		Widget obj = new Widget();
		obj.setValueThree(5);

		Integer id = (Integer) s.save(obj);

		t.commit();
		s.close();

		s = openSession();
		t = s.beginTransaction();

		Connection connection = s.connection();
		PreparedStatement statement = connection.prepareStatement("SELECT * FROM STRANGE_TYPED_OBJECT WHERE ID=?");
		statement.setInt(1, id.intValue());
		ResultSet resultSet = statement.executeQuery();

		assertTrue("A row should have been returned", resultSet.next());
		assertTrue("Default value should have been mapped to null", resultSet.getObject("VALUE_ONE") == null);
		assertTrue("Default value should have been mapped to null", resultSet.getObject("VALUE_TWO") == null);
		assertEquals("Non-Default value should not be changed", resultSet.getInt("VALUE_THREE"), 5);
		assertTrue("Default value should have been mapped to null", resultSet.getObject("VALUE_FOUR") == null);

		deleteData();
		t.commit();
		s.close();
	}

	public void testLoading() throws Exception {
		initData();

		Session s = openSession();
		Transaction t = s.beginTransaction();

		Widget obj = (Widget) s.createQuery("from Widget o where o.string = :string").setString("string", "all-normal").uniqueResult();
		assertEquals("Non-Default value incorrectly loaded", obj.getValueOne(), 7);
		assertEquals("Non-Default value incorrectly loaded", obj.getValueTwo(), 8);
		assertEquals("Non-Default value incorrectly loaded", obj.getValueThree(), 9);
		assertEquals("Non-Default value incorrectly loaded", obj.getValueFour(), 10);

		obj = (Widget) s.createQuery("from Widget o where o.string = :string").setString("string", "all-default").uniqueResult();
		assertEquals("Default value incorrectly loaded", obj.getValueOne(), 1);
		assertEquals("Default value incorrectly loaded", obj.getValueTwo(), 2);
		assertEquals("Default value incorrectly loaded", obj.getValueThree(), -1);
		assertEquals("Default value incorrectly loaded", obj.getValueFour(), -5);

		deleteData();
		t.commit();
		s.close();
	}

	private void initData() throws Exception {
		Session s = openSession();
		Transaction t = s.beginTransaction();

		Widget obj = new Widget();
		obj.setValueOne(7);
		obj.setValueTwo(8);
		obj.setValueThree(9);
		obj.setValueFour(10);
		obj.setString("all-normal");
		s.save(obj);

		obj = new Widget();
		obj.setValueOne(1);
		obj.setValueTwo(2);
		obj.setValueThree(-1);
		obj.setValueFour(-5);
		obj.setString("all-default");
		s.save(obj);

		t.commit();
		s.close();
	}

	private void deleteData() throws Exception {
		Session s = openSession();
		Transaction t = s.beginTransaction();
		s.delete("from Widget");
		t.commit();
		s.close();
	}

	public static Test suite() {
		return new TestSuite(TypeParameterTest.class);
	}

	public static void main(String[] args) throws Exception {
		TestRunner.run(suite());
	}
}