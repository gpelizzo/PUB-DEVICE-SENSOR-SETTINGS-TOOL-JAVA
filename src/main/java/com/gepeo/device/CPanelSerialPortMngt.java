package com.gepeo.device;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fazecast.jSerialComm.SerialPort;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CPanelSerialPortMngt implements ActionListener, ListSelectionListener {
	
	private MainFrame 			mMainFrame;
	private CSerialPort 		mSerialPort;
	private String				mstrDeviceType = null;
	
	public CPanelSerialPortMngt(MainFrame pMainFrame) {
		this.mMainFrame = pMainFrame;
		
		this.mMainFrame.getJButtonRefreshListSerialComPorts().addActionListener(this);
		this.mMainFrame.getJButtonRefreshListSerialComPorts().setText(CAppSettings.PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION);
		
		this.mMainFrame.getJButtonComPortConnect().addActionListener(this);
		this.mMainFrame.getJButtonComPortConnect().setText(CAppSettings.PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_CONNECT);
		
		this.mMainFrame.getJListSerialComPorts().addListSelectionListener(this);
		
		this.mMainFrame.getJCheckBoxKeepPreviousInfosSettingsSerialPort().setText(CAppSettings.PANEL_SERIAL_PORT__CHECKBOX_KEEP_PREVIOUS_INFOS_SETTINGS__CAPTION);
		this.mMainFrame.getJCheckBoxKeepPreviousInfosSettingsSerialPort().setSelected(false);
		
		this.mMainFrame.getJLabelAvailableSerialPorts().setText(CAppSettings.PANEL_SERIAL_PORT__LABEL_AVAILABLE_SERIAL_PORTS__CAPTION);
		
		this.mMainFrame.getJLabelSerialPortSpeed().setText(CAppSettings.PANEL_SERIAL_PORT__LABEL_SPEED_SERIAL_PORTS__CAPTION);
		
		Arrays.asList(CAppSettings.SERIAL_SPEEDS_LIST).forEach(item -> {
			this.mMainFrame.getJComboBoxSerialSpeeds().addItem(item);
		});

		this.mMainFrame.getJComboBoxSerialSpeeds().setSelectedItem(CAppSettings.getSerialSpeed());
		this.mMainFrame.getJComboBoxSerialSpeeds().addActionListener(this);
		
		//create CSerialComPort object to manage Serial communication
		this.mSerialPort = new CSerialPort(this.mMainFrame /*CAppSettings.getCurrentOS(), this.mMainFrame.getPaneATTerminalMngt()*/);
		
		try {
			//populate Serial Com Ports list with available ports
			this.mMainFrame.getJListSerialComPorts().setListData(mSerialPort.getAvailableSerialComPorts().toArray(new SerialPort[mSerialPort.getAvailableSerialComPorts().size()]));
		} catch (Exception e) {
			this.mMainFrame.insertMessageToOutput("CPanelSerialPortMngt:CPanelSerialPortMngt " + e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}
		
		updateComponentsStatus();
	}

	/**
	 * Manage ActionListener fired event for JBUttons
	 * 
	 * Return:
	 * 		NONE
	 *
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == this.mMainFrame.getJButtonComPortConnect()) {
			if (this.mSerialPort.isConnected()) {
				if (this.mSerialPort.disconnectFromSerialPort()) {
					this.mMainFrame.insertMessageToOutput("Serial port closed", CAppSettings.enOutputMessageType.INFO);
					this.mstrDeviceType = null;
				} else {
					this.mMainFrame.insertMessageToOutput("can't close serial port", CAppSettings.enOutputMessageType.ERROR);
				}
			} else {
				if (this.mSerialPort.connectToSerialPort()) {
					this.mMainFrame.insertMessageToOutput("Serial port opened", CAppSettings.enOutputMessageType.INFO);
					
					if (!this.mMainFrame.getJCheckBoxKeepPreviousInfosSettingsSerialPort().isSelected()) {
						this.mMainFrame.getPanelDeviceInfosSettingsMngt().clear();
					}
					
					try {
						String lstrATResponse;
						
						if ((lstrATResponse = this.mSerialPort.sendATCommand("AT+DEVICETYPE")).contains("OK\r\n\r\n")) {
							this.mstrDeviceType = lstrATResponse.substring(0, lstrATResponse.indexOf("\r\nOK"));
							this.mMainFrame.insertMessageToOutput("device responded '" + this.mstrDeviceType + "'", CAppSettings.enOutputMessageType.INFO);
						} else {
							this.mMainFrame.insertMessageToOutput("device did not responded expected value", CAppSettings.enOutputMessageType.INFO);
						}
					} catch (Exception e2) {
						this.mMainFrame.insertMessageToOutput(e2.getMessage(), CAppSettings.enOutputMessageType.ERROR);
					}
				} else {
					this.mMainFrame.insertMessageToOutput("can't open serial port", CAppSettings.enOutputMessageType.ERROR);
				}
			}
			
			updateComponentsStatus();
			return;
		}
		
		if (e.getSource() == this.mMainFrame.getJButtonRefreshListSerialComPorts()) {
			try {
				this.mMainFrame.getJListSerialComPorts().setListData(mSerialPort.getAvailableSerialComPorts().toArray(new SerialPort[mSerialPort.getAvailableSerialComPorts().size()]));
			} catch (Exception e1) {
				this.mMainFrame.insertMessageToOutput(e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
			}
			return;
		}
		
		if (e.getSource() == this.mMainFrame.getJComboBoxSerialSpeeds()) {
			CAppSettings.setSerialSpeed((int)this.mMainFrame.getJComboBoxSerialSpeeds().getSelectedItem());
		}
	}

	/**
	 * Manage ActionListener fired event for JList
	 * 
	 * Return:
	 * 		NONE
	 *
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == this.mMainFrame.getJListSerialComPorts()) {
			if (!e.getValueIsAdjusting()) {
				//set new selected serial com port 
				this.mSerialPort.setSerialPort(this.mMainFrame.getJListSerialComPorts().getSelectedValue());
				updateComponentsStatus();
			}
		}
	}
	
	/**
	 * Update status of components according global status
	 * 
	 * Return:
	 * 		NONE
	 *
	 */
	private void updateComponentsStatus() {
		//no serial com port selected: disable unecessary components
		if (this.mSerialPort.getSerialPort() == null) {
			this.mMainFrame.getJButtonRefreshListSerialComPorts().setEnabled(true);
			this.mMainFrame.getJButtonComPortConnect().setEnabled(false);
			this.mMainFrame.getJListSerialComPorts().setEnabled(true);
			this.mMainFrame.getJComboBoxSerialSpeeds().setEnabled(true);
		} else {
			// one serial com port selected: enable dedicated components according to 
			//Serial com port has been opened
			if (this.mSerialPort.isConnected()) {
				this.mMainFrame.getJButtonRefreshListSerialComPorts().setEnabled(false);
				this.mMainFrame.getJListSerialComPorts().setEnabled(false);
				this.mMainFrame.getJComboBoxSerialSpeeds().setEnabled(false);
				this.mMainFrame.getJButtonComPortConnect().setText(CAppSettings.PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_DISCONNECT);
				
				this.mMainFrame.getJTabddedPaneMainPane().setEnabledAt(CAppSettings.TABBEDPANE_DEVICE_INFOS_SETTINGS__INDEX, true);
				this.mMainFrame.getJTabddedPaneMainPane().setEnabledAt(CAppSettings.TABBEDPANE_AT_TERMINAL__INDEX, true);

			} else {
				//serial com port has been closed
				this.mMainFrame.getJButtonRefreshListSerialComPorts().setEnabled(true);
				this.mMainFrame.getJButtonComPortConnect().setEnabled(true);
				this.mMainFrame.getJListSerialComPorts().setEnabled(true);
				this.mMainFrame.getJComboBoxSerialSpeeds().setEnabled(true);
				this.mMainFrame.getJButtonComPortConnect().setText(CAppSettings.PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_CONNECT);
				
				this.mMainFrame.getJTabddedPaneMainPane().setEnabledAt(CAppSettings.TABBEDPANE_DEVICE_INFOS_SETTINGS__INDEX, false);
				this.mMainFrame.getJTabddedPaneMainPane().setEnabledAt(CAppSettings.TABBEDPANE_AT_TERMINAL__INDEX, false);
			}
		}
	}
	
	public CSerialPort getSerialPort() {
		return this.mSerialPort;
	}

	
	public boolean isConnected() {
		return this.mSerialPort.isConnected();
	}
	
	public String getDeviceType() {
		return this.mstrDeviceType;
	}
}
