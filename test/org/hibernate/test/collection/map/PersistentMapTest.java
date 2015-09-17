package org.hibernate.test.collection.map;

import org.hibernate.test.TestCase;
import org.hibernate.Session;
import org.hibernate.collection.PersistentMap;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * todo: describe PersistentMapTest
 *
 * @author Steve Ebersole
 */
public class PersistentMapTest extends TestCase {
	public PersistentMapTest(String name) {
		super( name );
	}

	protected String[] getMappings() {
		return new String[] { "collection/map/Mappings.hbm.xml" };
	}

	public static Test suite() {
		return new TestSuite( PersistentMapTest.class );
	}

	public void testWriteMethodDirtying() {
		Parent parent = new Parent( "p1" );
		Child child = new Child( "c1" );
		parent.getChildren().put( child.getName(), child );
		child.setParent( parent );
		Child otherChild = new Child( "c2" );

		Session session = openSession();
		session.beginTransaction();
		session.save( parent );
		session.flush();
		// at this point, the set on parent has now been replaced with a PersistentSet...
		PersistentMap children = ( PersistentMap ) parent.getChildren();

		Object old = children.put( child.getName(), child );
		assertTrue( old == child );
		assertFalse( children.isDirty() );

		old = children.remove( otherChild.getName() );
		assertNull( old );
		assertFalse( children.isDirty() );

		HashMap otherMap = new HashMap();
		otherMap.put( child.getName(), child );
		children.putAll( otherMap );
		assertFalse( children.isDirty() );

		otherMap = new HashMap();
		otherMap.put( otherChild.getName(), otherChild );
		children.putAll( otherMap );
		assertTrue( children.isDirty() );

		children.clearDirty();
		session.delete( child );
		children.clear();
		assertTrue( children.isDirty() );
		session.flush();

		children.clear();
		assertFalse( children.isDirty() );

		session.delete( parent );
		session.getTransaction().commit();
		session.close();
	}
}
