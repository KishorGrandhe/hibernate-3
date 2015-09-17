package org.hibernate.test.ops;

import org.hibernate.test.TestCase;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

/**
 * {@inheritDoc}
 *
 * @author Steve Ebersole
 */
public abstract class AbstractOperationTestCase extends TestCase {
	public AbstractOperationTestCase(String name) {
		super( name );
	}

	protected void configure(Configuration cfg) {
		cfg.setProperty( Environment.GENERATE_STATISTICS, "true");
		cfg.setProperty( Environment.STATEMENT_BATCH_SIZE, "0" );
	}

	protected String[] getMappings() {
		return new String[] { "ops/Node.hbm.xml", "ops/Employer.hbm.xml", "ops/OptLockEntity.hbm.xml", "ops/OneToOne.hbm.xml" };
	}

	public String getCacheConcurrencyStrategy() {
		return null;
	}

	protected void clearCounts() {
		getSessions().getStatistics().clear();
	}

	protected void assertInsertCount(int expected) {
		int inserts = ( int ) getSessions().getStatistics().getEntityInsertCount();
		assertEquals( "unexpected insert count", expected, inserts );
	}

	protected void assertUpdateCount(int expected) {
		int updates = ( int ) getSessions().getStatistics().getEntityUpdateCount();
		assertEquals( "unexpected update counts", expected, updates );
	}

	protected void assertDeleteCount(int expected) {
		int deletes = ( int ) getSessions().getStatistics().getEntityDeleteCount();
		assertEquals( "unexpected delete counts", expected, deletes );
	}
}
