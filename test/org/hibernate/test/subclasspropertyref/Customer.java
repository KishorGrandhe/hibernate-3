//$Id: Customer.java 6029 2005-03-06 16:34:16Z oneovthafew $
package org.hibernate.test.subclasspropertyref;

/**
 * @author Gavin King
 */
public class Customer extends Person {
	private String customerId;

	public String getCustomerId() {
		return customerId;
	}
	
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
}
