package com.gepeo.device;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CDeviceAbstractTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class CCategoriesItem {
		private String mstrKey;
		private String mstrName;
		private int miDisplayOrder;
		
		public CCategoriesItem(String pstrKey, String pstrName, int piDisplayOrder) {
			this.mstrKey = pstrKey;
			this.mstrName = pstrName;
			this.miDisplayOrder = piDisplayOrder;
		}
		
		@SuppressWarnings("unused")
		public String getKey() {
			return this.mstrKey;
		}
		
		public String getName() {
			return this.mstrName;
		}
		
		public int getDisplayOrder() {
			return this.miDisplayOrder;
		}
	};
	
	private TreeMap<String, CCategoriesItem>				mTreeMapListCategories;
	private JSONObject										mJSONObjectSchema = null;
	private String											mStrSchemaPrimaryKey;
	private ListMultimap<String, CDeviceAbstractDataItem> 	mDataTableMultiMap;
	private ArrayList<CDeviceAbstractDataItem>				mDataRawTableArrayList;
	private String[]										mStrColumnsName = {"item", "value"};
	private MainFrame 										mMainFrame;
	
	public CDeviceAbstractTableModel(MainFrame pMainFrame, String pStrSchemaPrimaryKey) {
		this.mMainFrame = pMainFrame;
		
		try {
			this.mJSONObjectSchema = (JSONObject) new JSONParser().parse(new FileReader(new File(CAppSettings.getSchemaDefinitionPathName())));
		} catch (IOException | ParseException e) {
			this.mMainFrame.insertMessageToOutput("CDeviceAbstractTableModel:CDeviceAbstractTableModel:" + e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}
		
		this.mTreeMapListCategories = new TreeMap<String, CCategoriesItem>();
		this.mDataTableMultiMap = ArrayListMultimap.create();
		this.mDataRawTableArrayList = new ArrayList<CDeviceAbstractDataItem>();
		this.mStrSchemaPrimaryKey = pStrSchemaPrimaryKey;
	}
	
	public void clearAll() {
		this.mDataTableMultiMap.clear();
		this.mDataRawTableArrayList = new ArrayList<CDeviceAbstractDataItem>();
		this.mTreeMapListCategories.clear();
		fireTableDataChanged();
	}
	
	@SuppressWarnings("unchecked")
	public boolean parse(String pstrJsonStringDeviceInfosSettings) {
		
		//remove all previous data
		clearAll();
		
		try {
			//parse JSON String provided by device
			JSONObject lJSONObjectDeviceInfosSettings = (JSONObject)new JSONParser().parse(pstrJsonStringDeviceInfosSettings);
			
			//iterate JSON provided by device
			lJSONObjectDeviceInfosSettings.forEach((key, value) -> {
				JSONObject lJSONObjectConsolideDeviceAndSchema;
				
				//for each key found into the JSON provided by the device, search the corresponding entry into the Schema definition and return a consolidate JSON object including
				//the key's schema definition and the value provided by the device
				if (lJSONObjectDeviceInfosSettings.containsKey("device_type")) {
					if ((lJSONObjectConsolideDeviceAndSchema = findJSONObjectFromSchema(this.mStrSchemaPrimaryKey, key.toString(), lJSONObjectDeviceInfosSettings.get("device_type").toString())) != null) {
						
						if (lJSONObjectConsolideDeviceAndSchema.containsKey("type")) {
							
							CDeviceAbstractDataItem.enmTypes lenmItemType;
							if ((lenmItemType = CDeviceAbstractDataItem.getTypeMatchingEnum(((JSONObject)lJSONObjectConsolideDeviceAndSchema).get("type").toString())) != null) {
								
								ArrayList<CTypeListItem> lListItems = null;;
								
								//if item type is a list, retrieve the corresponding list items
								if (lenmItemType == CDeviceAbstractDataItem.enmTypes.LIST) {
									if (lJSONObjectConsolideDeviceAndSchema.containsKey("list_items")) {
										lListItems = getListItemsFromSchema(lJSONObjectConsolideDeviceAndSchema.get("list_items").toString());
									
									} else {
										this.mMainFrame.insertMessageToOutput("CDeviceAbstractTableModel:parse: key 'list_item' does not exist", CAppSettings.enOutputMessageType.ERROR);
										//throw...list_item key missing
									}
								}
								
								this.mDataTableMultiMap.put(lJSONObjectConsolideDeviceAndSchema.get("category_name").toString(),
															new CDeviceAbstractDataItem(
																	key.toString(),
																	lJSONObjectConsolideDeviceAndSchema.get("name").toString(),
																	value.toString(),
																	lJSONObjectConsolideDeviceAndSchema.get("update_allowed").toString().equals("true") ? true : false,
																	CDeviceAbstractDataItem.getTypeMatchingEnum(lJSONObjectConsolideDeviceAndSchema.get("type").toString()),
																	Integer.parseInt(lJSONObjectConsolideDeviceAndSchema.get("display_order").toString()),
																	lListItems, 
																	lJSONObjectConsolideDeviceAndSchema.containsKey("pattern") ? lJSONObjectConsolideDeviceAndSchema.get("pattern").toString() : null,
																	lJSONObjectConsolideDeviceAndSchema.containsKey("comment") ? lJSONObjectConsolideDeviceAndSchema.get("comment").toString() : "")
															);
								
							} else {
								this.mMainFrame.insertMessageToOutput("CDeviceAbstractTableModel:parse: key 'type' into schema does not exist", CAppSettings.enOutputMessageType.ERROR);
							}
							
						} else {
							this.mMainFrame.insertMessageToOutput("CDeviceAbstractTableModel:parse: 'type' key value does not exist", CAppSettings.enOutputMessageType.ERROR);
						}
					}
				} else {
					this.mMainFrame.insertMessageToOutput("CDeviceAbstractTableModel:parse: key 'type' into device response does not exist", CAppSettings.enOutputMessageType.ERROR);
				}
			});
			
			//now, we have to insert categories title in order to populate the table (dummy item) and sort all items according to the display_order keys
			
			//first, sorted categories: mTreeMapListCategories has been populated by findJSONObjectFromSchema(...)
			ArrayList<CCategoriesItem> lSortedCategoriesListItems = new ArrayList<CCategoriesItem>(mTreeMapListCategories.values());
			Collections.sort(lSortedCategoriesListItems, new Comparator<CCategoriesItem>() {

				@Override
				public int compare(CDeviceAbstractTableModel.CCategoriesItem o1,
						CDeviceAbstractTableModel.CCategoriesItem o2) {
					return (o1.getDisplayOrder() > o2.getDisplayOrder()) ? 0 : -1;
				}
			});
			
			//iterate sorted categories
			for (CCategoriesItem lcategoryItem : lSortedCategoriesListItems) {
				//create a temporary Array List to store sorted items belonging to a category
				ArrayList<CDeviceAbstractDataItem> lTempsArrayList = new ArrayList<CDeviceAbstractDataItem>();
				
				//first populate the temporary array with corresponding items
				for (CDeviceAbstractDataItem lDataItem : this.mDataTableMultiMap.get(lcategoryItem.getName())) {
					lTempsArrayList.add(lDataItem);
				}
				
				//next, sort the temporary array
				Collections.sort(lTempsArrayList, new Comparator<CDeviceAbstractDataItem>() {

					@Override
					public int compare(CDeviceAbstractDataItem o1, CDeviceAbstractDataItem o2) {
						return (o1.getDisplayOrder() > o2.getDisplayOrder()) ? 0 : -1;
					}
				});
				
				//finally, add the category itself as a dummy CDeviceAbstractDataItem
				lTempsArrayList.add(0, new CDeviceAbstractDataItem("", lcategoryItem.getName(), "", false, CDeviceAbstractDataItem.enmTypes.DUMMY, -1, null, null, ""));
				
				//and insert the temporary list into the global data raw model
				this.mDataRawTableArrayList.addAll(lTempsArrayList);
			}
			
		} catch (ParseException e) {
			this.mMainFrame.insertMessageToOutput("parse:" + e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}	
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject findJSONObjectFromSchema(String pstrSchemaPrimaryKey, String pstrKeyToFind, String pstrDeviceType) {
		
		JSONObject ljsonObjCategory;
		JSONArray ljsonArrayCategoryValues;
		JSONObject ljsonObjCategoryValuesItem;
		Iterator<JSONObject> literatorCategoryValues;
		
		String lstrJsonKeyName;

		if (this.mJSONObjectSchema != null) {
			//check if primary key exist, e.g: 'settings'
			if (this.mJSONObjectSchema.containsKey(pstrSchemaPrimaryKey)) {
				
				if (((JSONObject)this.mJSONObjectSchema.get(pstrSchemaPrimaryKey)).containsKey("values")) {
					
					//iterate all direct-child-keys which are the categories, e.g.: 'api', 'wifi', 'radio', etc.
					for (Object categoryKey : ((JSONObject)((JSONObject)this.mJSONObjectSchema.get(pstrSchemaPrimaryKey)).get("values")).keySet())  {
						
						//retrieve full JSONObject for a category
						ljsonObjCategory = ((JSONObject)((JSONObject)((JSONObject)this.mJSONObjectSchema.get(pstrSchemaPrimaryKey)).get("values")).get(categoryKey.toString()));
									
						//retrieve values from the category. Values is an JSON array
						if (ljsonObjCategory.containsKey("values")) {
		
							ljsonArrayCategoryValues = (JSONArray)ljsonObjCategory.get("values");
								
							//create iterator - avoiding using a lambda in order to returnand exist the function
							literatorCategoryValues = ljsonArrayCategoryValues.iterator();
							
							//iterate all JSON objects from the array
							while (literatorCategoryValues.hasNext()) {
								ljsonObjCategoryValuesItem = literatorCategoryValues.next();
								
								//search for the requested key: pstrDeviceType
								if (((lstrJsonKeyName = ljsonObjCategoryValuesItem.get("key").toString()) != null) && (lstrJsonKeyName.equals(pstrKeyToFind))) {
									
									if (ljsonObjCategoryValuesItem.containsKey("scope")) {
										//ensure that key scope fit with the device Type
										if (((ArrayList<String>)ljsonObjCategoryValuesItem.get("scope")).indexOf(pstrDeviceType) != -1) {
											
											//add category infos into the categories treetable if does not already exist
											if (this.mTreeMapListCategories.get(categoryKey.toString()) == null) {
												this.mTreeMapListCategories.put(categoryKey.toString(), new CCategoriesItem(categoryKey.toString(), ljsonObjCategory.get("name").toString(), Integer.parseInt(ljsonObjCategory.get("display_order").toString())));
											}
											
											//Build the returning JSON Object including the schema definition for the key, adding the category name and removing the scope
											JSONObject ljsonObjItem = new JSONObject(ljsonObjCategoryValuesItem);
											ljsonObjItem.put("category_name", ljsonObjCategory.get("name").toString());
											ljsonObjItem.remove("scope");
											
											return ljsonObjItem;
										} else {
											return null;
										}
									} else {
										this.mMainFrame.insertMessageToOutput("findJSONObjectFromSchema: 'value' key into category does not exist", CAppSettings.enOutputMessageType.ERROR);
									}
								} //schema item does not fit. Check the next one
							}
						} else {
							this.mMainFrame.insertMessageToOutput("findJSONObjectFromSchema: 'values' key into category does not exist", CAppSettings.enOutputMessageType.ERROR);
						}
					}
				} else {
					this.mMainFrame.insertMessageToOutput("findJSONObjectFromSchema: 'value' key into primary " + pstrSchemaPrimaryKey + " key does not exist", CAppSettings.enOutputMessageType.ERROR);
					//throw...values key does not exist
				}
			} else {
				this.mMainFrame.insertMessageToOutput("findJSONObjectFromSchema: schema primary key " + pstrSchemaPrimaryKey + " does not exist", CAppSettings.enOutputMessageType.ERROR);
			}
		}
		
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	private ArrayList<CTypeListItem> getListItemsFromSchema(String pstrListKey) {
		if (this.mJSONObjectSchema != null) {
			//search 'params' key into the schema definition
			if (this.mJSONObjectSchema.containsKey("params")) {
				//search 'list' key into 'params' entry
				if (((JSONObject)this.mJSONObjectSchema.get("params")).containsKey("lists")) {
					
					//search for the corresponding list key
					JSONObject lMatchingListObj = (JSONObject) ((JSONArray)(((JSONObject)this.mJSONObjectSchema.get("params")).get("lists")))
							.stream()
							.filter(item -> ((JSONObject)item).get("name").equals(pstrListKey))
							.findFirst().orElse(null);
					
					//if a corresponding key list has been found
					if (lMatchingListObj != null) {
						
						if (lMatchingListObj.containsKey("values")) {
							Iterator<JSONObject> lMatchingListItemsIterator = ((JSONArray)lMatchingListObj.get("values")).iterator();
							
							//iterate and return a list with the corresponding items
							ArrayList<CTypeListItem> lArrayList = new ArrayList<CTypeListItem>();
							while (lMatchingListItemsIterator.hasNext()) {
								JSONObject lObjListItem = lMatchingListItemsIterator.next();
								lArrayList.add(new CTypeListItem(lObjListItem.get("name").toString(), lObjListItem.get("value").toString()));
							}
							
							return lArrayList;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public int getRowCount() {
		return this.mDataRawTableArrayList.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return this.mDataRawTableArrayList.get(rowIndex).getName();
		} else {
			if (this.mDataRawTableArrayList.get(rowIndex).getTypeEnum() == CDeviceAbstractDataItem.enmTypes.LIST) {
				CTypeListItem lTypeListItems = (CTypeListItem)this.mDataRawTableArrayList.get(rowIndex).getListItems()
												.stream()
												.filter(item -> item.getValue().equals(this.mDataRawTableArrayList.get(rowIndex).getValue().toString()))
												.findFirst()
												.orElse(null);
				
				if (lTypeListItems != null) {
					return lTypeListItems.getName();
				} else {
					return "";
				}
			}
			
			return this.mDataRawTableArrayList.get(rowIndex).getValue();
		}
	}
	
	@Override
	public String getColumnName(int col) {
		return mStrColumnsName[col];
	}
	
	
	@Override
	public boolean isCellEditable(int row, int column){  
		if (column == 1) {
			return this.mDataRawTableArrayList.get(row).isUpdateAllowed();
		} else {
        	return false;
        }
    }
	
	public CDeviceAbstractDataItem getItem(int row) {
		return this.mDataRawTableArrayList.get(row);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		
		CDeviceAbstractDataItem lItem = this.mDataRawTableArrayList.get(row);
	
	operation: {
			if (lItem.getRegexPattern() != null) {
				Pattern lPattern = Pattern.compile(lItem.getRegexPattern(), Pattern.CASE_INSENSITIVE);
		        Matcher lMatcher = lPattern.matcher(value.toString());
		        
		        if (lMatcher.find()) {
		        	break operation;
		        } else {
		        	JOptionPane.showMessageDialog(null, lItem.getComment(), "Error", JOptionPane.ERROR_MESSAGE);		
		        	return;
		        }
			} 
		}
			
		lItem.setValue(value.toString());
    	this.mDataRawTableArrayList.set(row, lItem);
    	fireTableCellUpdated(row, col);
	}
}
