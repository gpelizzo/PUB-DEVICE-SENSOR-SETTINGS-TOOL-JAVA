package com.gepeo.device;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CPanelDeviceInfosSettingsMngt implements ActionListener {
	
	private MainFrame 					mMainFrame;
	private CDeviceAbstractTableModel	mDeviceInfosAbstractTableModel;
	private CDeviceAbstractTableModel	mDeviceSettingsAbstractTableModel;
	
	private CDeviceTableDefaultCellEditorRenderer	mTableDefaultEditorRenderer;
	
	public CPanelDeviceInfosSettingsMngt(MainFrame pMainFrame) {
		this.mMainFrame = pMainFrame;
		
		this.mDeviceInfosAbstractTableModel = new CDeviceAbstractTableModel(pMainFrame, CAppSettings.JSON_SCHEMA_INFOS_KEY);
		this.mDeviceSettingsAbstractTableModel = new CDeviceAbstractTableModel(pMainFrame, CAppSettings.JSON_SCHEMA_SETTINGS_KEY);
		
		this.mTableDefaultEditorRenderer = new CDeviceTableDefaultCellEditorRenderer();
		
		this.mMainFrame.getJTabbedPaneDeviceInfosSettings().setTitleAt(CAppSettings.TABBEDPANE__DEVICE_INFOS_SETTINGS__INFOS_INDEX, CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__TAB_INFOS_CAPTION);
		this.mMainFrame.getJTabbedPaneDeviceInfosSettings().setTitleAt(CAppSettings.TABBEDPANE__DEVICE_INFOS_SETTINGS__SETTINGS_INDEX, CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__TAB_SETTINGS_CAPTION);
			
		this.mMainFrame.getJTableDeviceInfo().setModel(this.mDeviceInfosAbstractTableModel);
		this.mMainFrame.getJTableDeviceInfo().setDefaultEditor(Object.class, this.mTableDefaultEditorRenderer);
		this.mMainFrame.getJTableDeviceInfo().setDefaultRenderer(Object.class, this.mTableDefaultEditorRenderer);
		
		this.mMainFrame.getJTableDeviceSettings().setModel(this.mDeviceSettingsAbstractTableModel);
		this.mMainFrame.getJTableDeviceSettings().setDefaultEditor(Object.class, this.mTableDefaultEditorRenderer);
		this.mMainFrame.getJTableDeviceSettings().setDefaultRenderer(Object.class, this.mTableDefaultEditorRenderer);
		
		this.mMainFrame.getJButtonReadDeviceSettings().setText(CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__BUTTON_READ_DEVICE_SETTINGS__CAPTION);
		this.mMainFrame.getJButtonReadDeviceSettings().addActionListener(this);
		
		this.mMainFrame.getJButtonWriteDeviceSettings().setText(CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__BUTTON_WRITE_DEVICE_SETTINGS__CAPTION);
		this.mMainFrame.getJButtonWriteDeviceSettings().addActionListener(this);
		this.mMainFrame.getJButtonWriteDeviceSettings().setEnabled(false);
		
		this.mMainFrame.getJButtonSaveDeviceSettings().setText(CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__BUTTON_SAVE_DEVICE_SETTINGS__CAPTION);
		this.mMainFrame.getJButtonSaveDeviceSettings().addActionListener(this);
		this.mMainFrame.getJButtonSaveDeviceSettings().setEnabled(false);
		
		this.mMainFrame.getJButtonLoadDeviceSettings().setText(CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__BUTTON_LOAD_DEVICE_SETTINGS__CAPTION);
		this.mMainFrame.getJButtonLoadDeviceSettings().addActionListener(this);
		this.mMainFrame.getJButtonLoadDeviceSettings().setEnabled(false);
	}

	public void parseJson(String pstrJsonContent) {
		pstrJsonContent = pstrJsonContent.substring(pstrJsonContent.indexOf("{"), pstrJsonContent.lastIndexOf("}") + 1);
		
		this.mDeviceInfosAbstractTableModel.parse(pstrJsonContent);
		this.mDeviceInfosAbstractTableModel.fireTableDataChanged();
		this.mDeviceSettingsAbstractTableModel.parse(pstrJsonContent);
		this.mDeviceSettingsAbstractTableModel.fireTableDataChanged();
		
		this.mMainFrame.getJButtonWriteDeviceSettings().setEnabled(true);
		this.mMainFrame.getJButtonSaveDeviceSettings().setEnabled(true);
		this.mMainFrame.getJButtonLoadDeviceSettings().setEnabled(true);
	}
	
	private String buildJsonDeviceConfig() {
		String lstrATCommand = "{";
		boolean lbFirstItem = true;
		
		for (int liTemsCounter = 0;  liTemsCounter < this.mDeviceSettingsAbstractTableModel.getRowCount(); liTemsCounter++) {
			CDeviceAbstractDataItem lItemData = this.mDeviceSettingsAbstractTableModel.getItem(liTemsCounter);
			
			if (!lItemData.getKey().isEmpty()) {
				if (lbFirstItem) {
					lbFirstItem = false;
				} else {
					lstrATCommand += ",";
				}
				
				lstrATCommand += ("\"" + lItemData.getKey() + "\":\"" + lItemData.getValue() + "\"");
			}
		}
		
		lstrATCommand += (",\"device_type\":\"" + this.mMainFrame.GetPanelSerialPortMngt().getDeviceType() + "\"");
		
		
		lstrATCommand += "}";
		
		return lstrATCommand;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser lFileChooser;
		
		if (e.getSource() == this.mMainFrame.getJButtonReadDeviceSettings()) {
			try {
				String lstrATResponse = this.mMainFrame.GetPanelSerialPortMngt().getSerialPort().getJsonSettings();
				
				this.mMainFrame.insertMessageToOutput("Read Settings OK", CAppSettings.enOutputMessageType.INFO);
				
				parseJson(lstrATResponse);
			} catch (Exception e1) {
				this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
			}
		}
		
		if (e.getSource() == this.mMainFrame.getJButtonWriteDeviceSettings()) {
			String lstrATCommand = buildJsonDeviceConfig();
			
			try {
				if (this.mMainFrame.GetPanelSerialPortMngt().getSerialPort().sendJsonSettings(lstrATCommand)) {
					this.mMainFrame.GetPanelSerialPortMngt().getSerialPort().sendSaveSettings();
					
					this.mMainFrame.insertMessageToOutput("Write Settings OK", CAppSettings.enOutputMessageType.INFO);
				}
			} catch (Exception e1) {
				this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
			}
		}
		
		if (e.getSource() == this.mMainFrame.getJButtonSaveDeviceSettings()) {
			lFileChooser = new JFileChooser();
			
			lFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Json file", "json"));
			lFileChooser.setAcceptAllFileFilterUsed(false);
			
			int liRetValue = lFileChooser.showSaveDialog(this.mMainFrame);
			if (liRetValue == JFileChooser.APPROVE_OPTION) {
				File lFile = lFileChooser.getSelectedFile();
				String lstrFilePathName = lFile.getAbsolutePath();
				
				
				if ((lFileChooser.getSelectedFile()).exists()) {
					int lReply = JOptionPane.showConfirmDialog(null, CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__SAVE_FILE_ALREADY_EXIST__MESSAGE, "Warning", JOptionPane.YES_NO_OPTION);		
					
					if (lReply == JOptionPane.NO_OPTION) {
						return;
					} 
				}
				
				
				if (!lstrFilePathName.endsWith(".json")) {
					lstrFilePathName += ".json";
				}
				
				try {
					FileWriter lFileWriter = new FileWriter(lstrFilePathName);
					
					lFileWriter.write(buildJsonDeviceConfig());
					lFileWriter.close();
					
					this.mMainFrame.insertMessageToOutput("Save file OK", CAppSettings.enOutputMessageType.INFO);
					
				} catch (IOException e1) {
					this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
				}
			}
		}
		
		
		if (e.getSource() == this.mMainFrame.getJButtonLoadDeviceSettings()) {
			lFileChooser = new JFileChooser();
			
			lFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Json file", "json"));
			lFileChooser.setAcceptAllFileFilterUsed(false);
			
			int liRetValue = lFileChooser.showOpenDialog(this.mMainFrame);
			
			if (liRetValue == JFileChooser.APPROVE_OPTION) {
				File lFile = lFileChooser.getSelectedFile();
				
				try {
					JSONObject ljsonFile = (JSONObject) new JSONParser().parse(new FileReader(lFile));
					
					if (ljsonFile.containsKey("device_type")) {
						if (!this.mMainFrame.GetPanelSerialPortMngt().getDeviceType().equals(ljsonFile.get("device_type").toString())) {
							/*int lReply = JOptionPane.showConfirmDialog(null, CAppSettings.PANEL_DEVICE_INFOS_SETTINGS__LOAD_FILE_DEVICE_ERROR__MESSAGE, "Warning", JOptionPane.YES_NO_OPTION);		
							
							if (lReply == JOptionPane.NO_OPTION) {
								return;
							} */
							
							this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: device-IDs don't match", CAppSettings.enOutputMessageType.ERROR);
						
							return;
						}
						
						String lstrFileContent = new String(Files.readAllBytes(Paths.get(lFile.getAbsolutePath())));
						this.mMainFrame.insertMessageToOutput("Load file: " + lstrFileContent, CAppSettings.enOutputMessageType.DEBUG);
						this.mMainFrame.insertMessageToOutput("Load file OK", CAppSettings.enOutputMessageType.INFO);
						parseJson(lstrFileContent);
						
					} else {
						this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: loaded file does not contain a device-id", CAppSettings.enOutputMessageType.ERROR);
					}
										
				} catch (FileNotFoundException e1) {
					this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
				} catch (IOException e1) {
					this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
				} catch (ParseException e1) {
					this.mMainFrame.insertMessageToOutput("CPanelDeviceInfosSettingsMngt:actionPerformed: " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
				}
			}
		}
	}
	
	public void clear() {
		this.mDeviceInfosAbstractTableModel.clearAll();
		this.mDeviceSettingsAbstractTableModel.clearAll();
		
		this.mMainFrame.getJButtonWriteDeviceSettings().setEnabled(false);
		this.mMainFrame.getJButtonSaveDeviceSettings().setEnabled(false);
		this.mMainFrame.getJButtonLoadDeviceSettings().setEnabled(false); 
	}
}
