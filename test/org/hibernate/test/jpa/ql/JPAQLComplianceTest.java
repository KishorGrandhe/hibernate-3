package org.hibernate.test.jpa.ql;

import org.hibernate.test.jpa.AbstractJPATest;
import org.hibernate.Session;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for various JPAQL compliance issues
 *
 * @author Steve Ebersole
 */
public class JPAQLComplianceTest extends AbstractJPATest {
	public JPAQLComplianceTest(String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( JPAQLComplianceTest.class );
	}

	public void testAliasNameSameAsUnqualifiedEntityName() {
		Session s = openSession();
		s.beginTransaction();
		s.createQuery( "select item from Item item" ).list();
		s.createQuery( "select item from Item item where item.name = 'a'" ).list();
		s.getTransaction().commit();
		s.close();
	}

	public void testIdentifierCaseSensitive() throws Exception {
		Session s = openSession( );
		// a control test (a user reported that the JPA 'case insensitivity' support
		// caused problems with the "discriminator resolution" code; unable to reproduce)...
		s.createQuery( "from MyEntity e where e.class = MySubclassEntity" );
		s.createQuery( "from MyEntity e where e.other.class = MySubclassEntity" );

		s.createQuery( "select object(I) from Item i").list();
		s.close();
	}

	public void testGeneratedSubquery() {
		Session s = openSession();
		s.createQuery( "select c FROM Item c WHERE c.parts IS EMPTY" ).list();
		s.close();
	}
}
