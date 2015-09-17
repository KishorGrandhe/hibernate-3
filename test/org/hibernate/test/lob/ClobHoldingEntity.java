package org.hibernate.test.lob;

import java.sql.Clob;

/**
 * Used to test materialized and lazy-materialized CLOB data.
 * <p/>
 * The {@link #serialData} field is used to hold CLOB data that is
 * materialized into a String immediately (mapped via the
 * Hibernate text type).
 * <p/>
 * The {@link #clobData} field is used to hold CLOB data that is
 * materialized lazily via a JDBC CLOB locator (mapped via
 * the Hibernate clob type).
 *
 * @author Steve Ebersole
 */
public class ClobHoldingEntity {
	private Long id;
	private String serialData;
	private Clob clobData;

	public ClobHoldingEntity() {
	}

	public ClobHoldingEntity(String serialData) {
		this.serialData = serialData;
	}

	public ClobHoldingEntity(Clob clobData) {
		this.clobData = clobData;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSerialData() {
		return serialData;
	}

	public void setSerialData(String serialData) {
		this.serialData = serialData;
	}

	public Clob getClobData() {
		return clobData;
	}

	public void setClobData(Clob clobData) {
		this.clobData = clobData;
	}
}
