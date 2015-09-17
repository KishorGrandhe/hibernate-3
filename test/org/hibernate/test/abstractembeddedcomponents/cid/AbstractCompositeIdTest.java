package org.hibernate.test.abstractembeddedcomponents.cid;

import org.hibernate.test.TestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Steve Ebersole
 */
public class AbstractCompositeIdTest extends TestCase {
	public AbstractCompositeIdTest(String x) {
		super( x );
	}

	public static Test suite() {
		return new TestSuite( AbstractCompositeIdTest.class );
	}

	protected String[] getMappings() {
		return new String[] { "abstractembeddedcomponents/cid/Mappings.hbm.xml" };
	}

	public void testEmbeddedCompositeIdentifierOnAbstractClass() {
		MyInterfaceImpl myInterface = new MyInterfaceImpl();
		myInterface.setKey1( "key1" );
		myInterface.setKey2( "key2" );
		myInterface.setName( "test" );

		Session s = openSession();
		Transaction t = s.beginTransaction();
		s.save( myInterface );
		s.flush();

		s.createQuery( "from MyInterface" ).list();

		s.delete( myInterface );
		t.commit();
		s.close();

	}
}
