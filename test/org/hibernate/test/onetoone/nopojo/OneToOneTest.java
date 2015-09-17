//$Id: OneToOneTest.java 9012 2006-01-10 18:54:11Z steveebersole $
package org.hibernate.test.onetoone.nopojo;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.EntityMode;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.test.TestCase;

/**
 * @author Gavin King
 */
public class OneToOneTest extends TestCase {

	public OneToOneTest(String str) {
		super(str);
	}

	public void testOneToOneOnSubclass() {
		Map person = new HashMap();
		person.put( "name", "Steve" );
		Map address = new HashMap();
		address.put( "zip", "12345" );
		address.put( "state", "TX" );
		address.put( "street", "123 Main St" );

		person.put( "address", address );
		address.put( "owner", person );

		Session s = openSession();
		s.beginTransaction();
		s.persist( "Person", person );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();

		EntityStatistics addressStats = getSessions().getStatistics().getEntityStatistics( "Address" );

		person = ( Map ) s.createQuery( "from Person p join fetch p.address" ).uniqueResult();
		assertNotNull( "could not locate person", person );
		assertNotNull( "could not locate persons address", person.get( "address" ) );
		s.clear();

		Object[] tuple = ( Object[] ) s.createQuery( "select p.name, p from Person p join fetch p.address" ).uniqueResult();
		assertEquals( tuple.length, 2 );
		person = ( Map ) tuple[1];
		assertNotNull( "could not locate person", person );
		assertNotNull( "could not locate persons address", person.get( "address" ) );

		s.delete( "Person", person );

		s.getTransaction().commit();
		s.close();

		assertEquals( addressStats.getFetchCount(), 0 );
	}

	protected String[] getMappings() {
		return new String[] { "onetoone/nopojo/Person.hbm.xml" };
	}

	protected void configure(Configuration cfg) {
		cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
		cfg.setProperty( Environment.DEFAULT_ENTITY_MODE, EntityMode.MAP.toString() );
	}

	public static Test suite() {
		return new TestSuite(OneToOneTest.class);
	}

}

