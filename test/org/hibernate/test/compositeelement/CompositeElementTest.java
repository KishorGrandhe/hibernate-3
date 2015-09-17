//$Id: CompositeElementTest.java 10405 2006-09-01 18:40:39Z steve.ebersole@jboss.com $
package org.hibernate.test.compositeelement;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.util.XMLHelper;
import org.hibernate.util.StringHelper;
import org.hibernate.util.CollectionHelper;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.HbmBinder;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.test.TestCase;
import org.xml.sax.InputSource;
import org.dom4j.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Gavin King
 */
public class CompositeElementTest extends TestCase {

	public CompositeElementTest(String str) {
		super(str);
	}

	private boolean hasLengthFunction() {
		// neither support length() as a function, but each has len() which
		// is the same semantic
		return ! ( getDialect() instanceof SybaseDialect || getDialect() instanceof SQLServerDialect );
	}

	protected String[] getMappings() {
		return hasLengthFunction() ? new String[] { "compositeelement/Parent.hbm.xml" } : new String[0];
	}

	protected void configure(Configuration cfg) {
		super.configure( cfg );
		if ( !hasLengthFunction() ) {
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				InputStream xmlInputStream = cl.getResourceAsStream( getBaseForMappings() + "compositeelement/Parent.hbm.xml" );
				XMLHelper xmlHelper = new XMLHelper();
				ArrayList errors = new ArrayList();
				org.dom4j.Document doc = xmlHelper.createSAXReader( "XML InputStream", errors, cfg.getEntityResolver() )
						.read( new InputSource( xmlInputStream ) );

				Element hmNode = doc.getRootElement();
				Iterator classNodes = hmNode.elementIterator( "class" );
				while ( classNodes.hasNext() ) {
					Element classNode = ( Element ) classNodes.next();
					if ( "Parent".equals( classNode.attributeValue( "name" ) ) ) {
						Iterator sets = classNode.elementIterator( "set" );
						while ( sets.hasNext() ) {
							Element set = ( Element ) sets.next();
							if ( "children".equals( set.attributeValue( "name" ) ) ) {
								Element component = set.element( "composite-element" );
								Iterator componentProperties = component.elementIterator( "property" );
								while ( componentProperties.hasNext() ) {
									Element property = ( Element ) componentProperties.next();
									if ( "bioLength".equals( property.attributeValue( "name" ) ) ) {
										String formula = property.attributeValue( "formula" );
										property.attribute( "formula" ).setValue( StringHelper.replace( formula, "length", "len" ) );
									}
								}
							}
						}
					}
				}

				// Whew! ;)
				HbmBinder.bindRoot( doc, cfg.createMappings(), CollectionHelper.EMPTY_MAP );
			}
			catch( Throwable t ) {
				throw new HibernateException( "Grrr" );
			}
		}
	}

	public void testHandSQL() {
		Session s = openSession();
		Transaction t = s.beginTransaction();
		Child c = new Child("Child One");
		Parent p = new Parent("Parent");
		p.getChildren().add(c);
		c.setParent(p);
		s.save(p);
		s.flush();

		p.getChildren().remove(c);
		c.setParent(null);
		s.flush();

		p.getChildren().add(c);
		c.setParent(p);
		t.commit();
		s.close();

		s = openSession();
		t = s.beginTransaction();
		s.createQuery("select distinct p from Parent p join p.children c where c.name like 'Child%'").uniqueResult();
		s.clear();
		s.createQuery("select new Child(c.name) from Parent p left outer join p.children c where c.name like 'Child%'").uniqueResult();
		s.clear();
		//s.createQuery("select c from Parent p left outer join p.children c where c.name like 'Child%'").uniqueResult(); //we really need to be able to do this!
		s.clear();
		p = (Parent) s.createQuery("from Parent p left join fetch p.children").uniqueResult();
		t.commit();
		s.close();

		s = openSession();
		t = s.beginTransaction();
		s.delete(p);
		t.commit();
		s.close();
	}



	public static Test suite() {
		return new TestSuite(CompositeElementTest.class);
	}

}

