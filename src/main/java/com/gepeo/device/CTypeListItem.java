package com.gepeo.device;
/**
 * 
 */

/**
 * @author gille
 *
 */
public class CTypeListItem {
	private String name;
	private String value;
	
	public CTypeListItem(String pstrName, String pstrValue) {
		this.name = pstrName;
		this.value = pstrValue;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return name;
	}
}
