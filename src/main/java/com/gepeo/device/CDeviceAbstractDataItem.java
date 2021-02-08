package com.gepeo.device;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CDeviceAbstractDataItem {
	
	public static enum enmTypes {
		STRING("string"),
		INT("int"),
		FLOAT("float"),
		LIST("list"),
		DUMMY("dummy");
		
		public final String label;
		
		private enmTypes(String label) {
			this.label = label;
		}
		
		public static enmTypes valueOfLabel(String label) {
		    for (enmTypes e : values()) {
		        if (e.label.equals(label)) {
		            return e;
		        }
		    }
		    return null;
		}
	};
	
	private String mstrSchemaKey;
	private String mstrSchemaName;
	private String mstrCategoryKey;
	private String mstrCategoryName;
	private String mstrKey;
	private String mstrName;
	private String mstrValue;
	private boolean mbIsUpdateAllowed;
	private enmTypes menmType;
	private int miDisplayOrder;
	private ArrayList<CTypeListItem> mlistItems; 
	private String mstrRegexPattern;
	private String mstrComment;

	public CDeviceAbstractDataItem(String pstrKey, String pstrName, String pstrValue, boolean pbIsUpdateAllowed, 
					enmTypes penmType, int piDisplayOrder, ArrayList<CTypeListItem> plistItems, String pstrRegexPattern, String pstrComment) {
		
		this.mstrKey = pstrKey;
		this.mstrName = pstrName;
		this.mstrValue = pstrValue;
		this.menmType = penmType;		
		this.mbIsUpdateAllowed = pbIsUpdateAllowed;
		this.miDisplayOrder = piDisplayOrder;
		this.mlistItems = plistItems;
		this.mstrRegexPattern = pstrRegexPattern;
		this.mstrComment = pstrComment;
	}
	
	public ArrayList<CTypeListItem> getListItems() {
		return this.mlistItems;
	}
	
	public void setValue(String pstrValue) {
		this.mstrValue = pstrValue;
	}
	
	public String getKey() {
		return this.mstrKey;
	}
	
	public String getName() {
		return this.mstrName;
	}
	
	
	public String getValue() {
		return this.mstrValue;
	}
	
	public boolean isUpdateAllowed() {
		return this.mbIsUpdateAllowed;
	}
	
	public String getTypeLabel() {
		return this.menmType.label;
	};
	
	public enmTypes getTypeEnum() {
		return this.menmType;
	}
	
	public String getCategoryKey() {
		return this.mstrCategoryKey;
	}
	
	public String getCategoryName() {
		return this.mstrCategoryName; 
	}
	
	public String getSchemaKey() {
		return this.mstrSchemaKey;
	}
	
	public String getSchemaName() {
		return this.mstrSchemaName;
	}
	
	public int getDisplayOrder() {
		return this.miDisplayOrder;
	}
	
	public String getRegexPattern() {
		return this.mstrRegexPattern;
	}
	
	public String getComment() {
		return this.mstrComment;
	}
	
	static public enmTypes getTypeMatchingEnum(String pstrType) {
		return enmTypes.valueOfLabel(pstrType);
	}
}
