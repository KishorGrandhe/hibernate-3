package org.hibernate.test.any;

import org.hibernate.test.TestCase;
import org.hibernate.Session;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * todo: describe AnyTypeTest
 *
 * @author Steve Ebersole
 */
public class AnyTypeTest extends TestCase {
	public AnyTypeTest(String name) {
		super( name );
	}

	protected String[] getMappings() {
		return new String[] { "any/Person.hbm.xml" };
	}

	protected String getCacheConcurrencyStrategy() {
		// having second level cache causes a condition whereby the original test case would not fail...
		return null;
	}

	public static Test suite() {
		return new TestSuite( AnyTypeTest.class );
	}

	/**
	 * Specific test for HHH-1663...
	 */
	public void testFlushProcessing() {
		Session session = openSession();
		session.beginTransaction();
		Person person = new Person();
		Address address = new Address();
		person.setData( address );
		session.saveOrUpdate(person);
		session.saveOrUpdate(address);
		session.getTransaction().commit();
		session.close();

		session = openSession();
		session.beginTransaction();
        person = (Person) session.load( Person.class, person.getId() );
        person.setName("makingpersondirty");
		session.getTransaction().commit();
		session.close();

		session = openSession();
		session.beginTransaction();
		session.delete( person );
		session.getTransaction().commit();
		session.close();
	}
}
