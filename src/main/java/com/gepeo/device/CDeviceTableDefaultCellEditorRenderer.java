package com.gepeo.device;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CDeviceTableDefaultCellEditorRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object mObjValue; 

	public CDeviceTableDefaultCellEditorRenderer() {
		
	}

	/**
	 * return cell editor value
	 */
	@Override
	public Object getCellEditorValue() {
		if (this.mObjValue != null) {
			return this.mObjValue;
		} else {
			return "";
		}
	}

	/**
	 * get renderer: performing some custom rendering, e.g, setting BOLD or ITALIC on first column according to 
	 * content type
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		
		//retrieve default cell renderer
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		Component componentRenderer = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		//apply only on first column
		if (column == 0) {
			//retrieve cell's item corresponding to the row
			CDeviceAbstractDataItem item = (CDeviceAbstractDataItem)((CDeviceAbstractTableModel)table.getModel()).getItem(row);
			
			//create a  component for the cell
			JLabel lLabel = new JLabel();
			lLabel.setText(value.toString());
			Font componentDefaultFont = componentRenderer.getFont();
			
			//change font settings according to the item type. Here is a workaround in order to identify
			//if item inb the table corresponds to a Json key,value or if it's just a title
			if (item.getTypeEnum() == CDeviceAbstractDataItem.enmTypes.DUMMY) {
				lLabel.setFont(componentDefaultFont.deriveFont(Font.BOLD));
			} else {
				
				lLabel.setFont(componentDefaultFont.deriveFont(Font.ITALIC));
			}
			
			return lLabel;
		}
		
		return componentRenderer;
	}

	/**
	 * return a cell editor, either a JTexrField or a JComboBox according to the item type 
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
			int column) {
		
		//cells belongings to column 0 are not allowed to be updated
		if (column == 0) {
			return null;
		}
			
		//retrieve cell's item corresponding to the row
		CDeviceAbstractDataItem item = (CDeviceAbstractDataItem)((CDeviceAbstractTableModel)table.getModel()).getItem(row);

		switch (item.getTypeEnum()) {
		//case type is a list
		case LIST:
			//create a JCombobox component which will be used as the cell's editor 
			JComboBox<CTypeListItem> lCombo = new JComboBox<CTypeListItem>();
			
			//create a listener in order to record new cell's value when a new item into the JCombobox has been selected
			lCombo.addActionListener(new ActionListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void actionPerformed(ActionEvent e) {
					//set new value
					mObjValue = ((CTypeListItem)(((JComboBox<CTypeListItem>)e.getSource()).getSelectedItem())).getValue().toString();
				}
			});
			
			//listen JCombobox PopUpMenu events in order to fire a global event and update the table. 
			//Action listener above is not enough because it does not fire the setValueAt event to the table data model 
			lCombo.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					//fire setValueAt to Table data model
					fireEditingStopped();
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}
				
			});
			
			for (CTypeListItem listItem : item.getListItems()) {
				lCombo.addItem(listItem);
			}
			
			return lCombo;
			
		
		//case type is a STRING or a LIST
		case STRING: case INT: case FLOAT:
			//create a JCombobox component which will be used as the cell's editor 
			JTextField lField = new JTextField();
			lField.setText(value.toString());
			lField.selectAll();
			lField.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					lField.selectAll();
					//set new value
					mObjValue = lField.getText();
				}
			});
			
			lField.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {	
				}

				@Override
				public void keyPressed(KeyEvent e) {
					//typing 'enter' shall produce Stop cell editing 
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						stopCellEditing();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}
				
			});
			
			
			lField.getDocument().addDocumentListener(new DocumentListener() {
				private void updateData() {
					//set new value
			        mObjValue = (Object)lField.getText();
			    }
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateData();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateData();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					updateData();
				}
				
			});
			return lField;
			
			default:
				break;
		}
		
		return null;
	}
}
