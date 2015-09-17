//$Id: ComponentTest.java 10060 2006-06-28 02:53:39Z steve.ebersole@jboss.com $
package org.hibernate.test.component;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.criterion.Property;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.test.TestCase;

/**
 * @author Gavin King
 */
public class ComponentTest extends TestCase {
	
	public ComponentTest(String str) {
		super(str);
	}
	
	public void testUpdateFalse() {
		
		if ( getDialect() instanceof PostgreSQLDialect ) return; //postgres got no year() function
		if ( getDialect() instanceof Oracle9Dialect ) return; //oracle got no year() function
		
		getSessions().getStatistics().clear();
		
		Session s = openSession();
		Transaction t = s.beginTransaction();
		User u = new User( "gavin", "secret", new Person("Gavin King", new Date(), "Karbarook Ave") );
		s.persist(u);
		s.flush();
		u.getPerson().setName("XXXXYYYYY");
		t.commit();
		s.close();
		
		assertEquals( 1, getSessions().getStatistics().getEntityInsertCount() );
		assertEquals( 0, getSessions().getStatistics().getEntityUpdateCount() );

		s = openSession();
		t = s.beginTransaction();
		u = (User) s.get(User.class, "gavin");
		assertEquals( u.getPerson().getName(), "Gavin King" );
		s.delete(u);
		t.commit();
		s.close();
		
		assertEquals( 1, getSessions().getStatistics().getEntityDeleteCount() );
	}
	
	public void testComponent() {
		
		if ( getDialect() instanceof PostgreSQLDialect ) return; //postgres got no year() function
		if ( getDialect() instanceof Oracle9Dialect ) return; //oracle got no year() function
		
		Session s = openSession();
		Transaction t = s.beginTransaction();
		User u = new User( "gavin", "secret", new Person("Gavin King", new Date(), "Karbarook Ave") );
		s.persist(u);
		s.flush();
		u.getPerson().changeAddress("Phipps Place");
		t.commit();
		s.close();
		
		s = openSession();
		t = s.beginTransaction();
		u = (User) s.get(User.class, "gavin");
		assertEquals( u.getPerson().getAddress(), "Phipps Place" );
		assertEquals( u.getPerson().getPreviousAddress(), "Karbarook Ave" );
		assertEquals( u.getPerson().getYob(), u.getPerson().getDob().getYear()+1900 );
		u.setPassword("$ecret");
		t.commit();
		s.close();

		s = openSession();
		t = s.beginTransaction();
		u = (User) s.get(User.class, "gavin");
		assertEquals( u.getPerson().getAddress(), "Phipps Place" );
		assertEquals( u.getPerson().getPreviousAddress(), "Karbarook Ave" );
		assertEquals( u.getPassword(), "$ecret" );
		s.delete(u);
		t.commit();
		s.close();
	}

	public void testComponentQueries() {
//		if ( !dialectSupportsRowValueConstructorSyntax() ) {
//			return;
//		}
		Session s = openSession();
		Transaction t = s.beginTransaction();
		Employee emp = new Employee();
		emp.setHireDate( new Date() );
		emp.setPerson( new Person() );
		emp.getPerson().setName( "steve" );
		emp.getPerson().setDob( new Date() );
		s.save( emp );

		s.createQuery( "from Employee e where e.person = :p and 1 = 1 and 2=2" ).setParameter( "p", emp.getPerson() ).list();
		s.createQuery( "from Employee e where :p = e.person" ).setParameter( "p", emp.getPerson() ).list();
		s.createQuery( "from Employee e where e.person = ('steve', current_timestamp)" ).list();

		s.delete( emp );
		t.commit();
		s.close();
	}
	
	public void testComponentFormulaQuery() {
		
		if ( getDialect() instanceof PostgreSQLDialect ) return; //postgres got no year() function
		if ( getDialect() instanceof Oracle9Dialect ) return; //oracle got no year() function
		
		Session s = openSession();
		Transaction t = s.beginTransaction();
		s.createQuery("from User u where u.person.yob = 1999").list();
		s.createCriteria(User.class)
			.add( Property.forName("person.yob").between( new Integer(1999), new Integer(2002) ) )
			.list();
		if ( ! (getDialect() instanceof HSQLDialect) ) {
			s.createQuery("from User u where u.person = ('gavin', :dob, 'Peachtree Rd', 'Karbarook Ave', 1974, 'Peachtree Rd')")
				.setDate("dob", new Date("March 25, 1974")).list();
			s.createQuery("from User where person = ('gavin', :dob, 'Peachtree Rd', 'Karbarook Ave', 1974, 'Peachtree Rd')")
				.setDate("dob", new Date("March 25, 1974")).list();
		}
		t.commit();
		s.close();
	}

	public void testNamedQuery() {
		if ( getDialect() instanceof PostgreSQLDialect ) return; //postgres got no year() function
		if ( getDialect() instanceof Oracle9Dialect ) return; //oracle got no year() function
		
		Session s = openSession();
		Transaction t = s.beginTransaction();
		s.getNamedQuery("userNameIn")
			.setParameterList( "nameList", new Object[] {"1ovthafew", "turin", "xam"} )
			.list();
		t.commit();
		s.close();
	}
	
	protected String[] getMappings() {
		return new String[] { "component/User.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite(ComponentTest.class);
	}

	protected void configure(Configuration cfg) {
		cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
	}

}

