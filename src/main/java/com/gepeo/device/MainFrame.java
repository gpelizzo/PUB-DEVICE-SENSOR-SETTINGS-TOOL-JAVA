package com.gepeo.device;

import java.awt.Color;
import java.awt.EventQueue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel 							mContentPane;
	private JTable 							mTableDeviceInfos;
	private JTable 							mTableDeviceSettings;
	private JTabbedPane 					mTabbedMainPane;
	private JPanel 							mPanelSerial;
	private JButton 						mButtonnRefreshListSerialPorts;
	private JScrollPane 					mScrollPaneListSerialPort;
	private JList<SerialPort> 				mListSerialPorts;
	private JLabel 							mLabelSerialPortSpeed;
	private JButton 						mButtonSerialPortConnect;
	private JComboBox<Integer>				mComboBoxSerialSpeeds;
	private JLabel 							mLabelAvailableSerialPorts;
	private JCheckBox						mCheckboxKeepPreviousInfosSettingsSerialPort;
	private JPanel 							mPanelDeviceInfosSettings;
	private JTabbedPane 					mTabbedPaneDeviceInfosSettings;
	private JScrollPane 					mScrollPaneInfos;
	private JScrollPane 					mScrollPaneSettings;
	private JButton 						mButtonReadDeviceSettings;
	private JButton 						mButtonWriteDeviceSettings;
	private JButton 						mButtonSaveDeviceSettings;
	private JButton 						mButtonLoadDeviceSettings;
	private JPanel 							mPanelTerminal;
	private JScrollPane 					mScrollPaneATTerminal;
	private JButton 						mButtonSendATCommand;
	private JTextPane 						mTextPaneATTerminal;
	private JButton 						mButtonClearATTerminal;
	private JTextField 						mTextFieldATCommand;
	private JScrollPane						mScrollPaneOutput;
	private JTextPane 						mTextPaneOutput;
	
	private CPanelSerialPortMngt 			mPanelSerialPortMngt;
	private CPanelATTerminalMngt			mPanelATTerminalMngt;
	private CPanelDeviceInfosSettingsMngt	mPanelDeviceInfosSettingsMngt;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	/**
	 * 
	 */
	public MainFrame() {
		
		setResizable(false);
		
		try {
			CAppSettings.init();
		} catch (Exception e) {
			insertMessageToOutput("MainFrame:MainFrame " + e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}
		
		this.setTitle(CAppSettings.getAppTitle());
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 585, 669);
		this.mContentPane = new JPanel();
		this.mContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.mContentPane);
		this.mContentPane.setLayout(null);
		
		this.mTabbedMainPane = new JTabbedPane(JTabbedPane.TOP);
		this.mTabbedMainPane.setBounds(10, 11, 548, 494);
		this.mContentPane.add(this.mTabbedMainPane);
		
		//Serial Port Swing Objects
		this.mPanelSerial = new JPanel();
		this.mTabbedMainPane.addTab("Serial", null, this.mPanelSerial, null);
		this.mPanelSerial.setLayout(null);
		
		this.mButtonnRefreshListSerialPorts = new JButton("Refresh");
		this.mButtonnRefreshListSerialPorts.setBounds(348, 51, 169, 63);
		this.mPanelSerial.add(this.mButtonnRefreshListSerialPorts);
		
		this.mScrollPaneListSerialPort = new JScrollPane();
		this.mScrollPaneListSerialPort.setBounds(20, 51, 318, 207);
		this.mPanelSerial.add(this.mScrollPaneListSerialPort);
		
		this.mListSerialPorts = new JList<SerialPort>();
		this.mListSerialPorts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.mScrollPaneListSerialPort.setViewportView(this.mListSerialPorts);
		
		this.mButtonSerialPortConnect = new JButton("Connect");
		this.mButtonSerialPortConnect.setBounds(26, 384, 491, 63);
		this.mPanelSerial.add(this.mButtonSerialPortConnect);
		
		this.mLabelAvailableSerialPorts = new JLabel("Available COM ports:");
		this.mLabelAvailableSerialPorts.setBounds(22, 30, 316, 14);
		this.mPanelSerial.add(this.mLabelAvailableSerialPorts);
		
		this.mCheckboxKeepPreviousInfosSettingsSerialPort = new JCheckBox("New check box");
		this.mCheckboxKeepPreviousInfosSettingsSerialPort.setBounds(20, 272, 318, 23);
		this.mPanelSerial.add(this.mCheckboxKeepPreviousInfosSettingsSerialPort);
		
		this.mComboBoxSerialSpeeds = new JComboBox<Integer>();
		this.mComboBoxSerialSpeeds.setBounds(348, 166, 169, 22);
		mPanelSerial.add(this.mComboBoxSerialSpeeds);
		
		this.mLabelSerialPortSpeed = new JLabel("Serial Speed:");
		this.mLabelSerialPortSpeed.setBounds(348, 147, 185, 14);
		mPanelSerial.add(this.mLabelSerialPortSpeed);
		
		//Device Infos&Setting Swing Objects
		this.mPanelDeviceInfosSettings = new JPanel();
		this.mTabbedMainPane.addTab("Device Settings", null, this.mPanelDeviceInfosSettings, null);
		this.mTabbedMainPane.setEnabledAt(1, false);
		this.mPanelDeviceInfosSettings.setLayout(null);
		
		this.mTabbedPaneDeviceInfosSettings = new JTabbedPane(JTabbedPane.TOP);
		this.mTabbedPaneDeviceInfosSettings.setBounds(10, 11, 534, 391);
		this.mPanelDeviceInfosSettings.add(this.mTabbedPaneDeviceInfosSettings);
		
		this.mScrollPaneInfos = new JScrollPane();
		this.mTabbedPaneDeviceInfosSettings.addTab("Infos", null, this.mScrollPaneInfos, null);
		
		this.mTableDeviceInfos = new JTable();
		this.mScrollPaneInfos.setViewportView(this.mTableDeviceInfos);
		
		this.mScrollPaneSettings = new JScrollPane();
		this.mTabbedPaneDeviceInfosSettings.addTab("Settings", null, this.mScrollPaneSettings, null);
		
		this.mTableDeviceSettings = new JTable();
		this.mScrollPaneSettings.setViewportView(mTableDeviceSettings);
		
		this.mButtonReadDeviceSettings = new JButton("Read settings");
		this.mButtonReadDeviceSettings.setBounds(9, 414, 126, 41);
		this.mPanelDeviceInfosSettings.add(this.mButtonReadDeviceSettings);
		
		this.mButtonWriteDeviceSettings = new JButton("Write settings");
		this.mButtonWriteDeviceSettings.setBounds(144, 414, 126, 41);
		this.mPanelDeviceInfosSettings.add(this.mButtonWriteDeviceSettings);
		
		this.mButtonSaveDeviceSettings = new JButton("Save settings");
		this.mButtonSaveDeviceSettings.setBounds(279, 414, 126, 41);
		this.mPanelDeviceInfosSettings.add(this.mButtonSaveDeviceSettings);
		
		this.mButtonLoadDeviceSettings = new JButton("Load settings");
		this.mButtonLoadDeviceSettings.setBounds(414, 414, 126, 41);
		this.mPanelDeviceInfosSettings.add(this.mButtonLoadDeviceSettings);
		
		//AT Terminal Swing Objects
		this.mPanelTerminal = new JPanel();
		this.mTabbedMainPane.addTab("Terminal", null, this.mPanelTerminal, null);
		this.mTabbedMainPane.setEnabledAt(2, false);
		
		this.mPanelTerminal.setLayout(null);
		
		this.mScrollPaneATTerminal = new JScrollPane();
		this.mScrollPaneATTerminal.setBounds(10, 61, 523, 400);
		this.mPanelTerminal.add(this.mScrollPaneATTerminal);
		
		this.mTextPaneATTerminal = new JTextPane();
		mTextPaneATTerminal.setEditable(false);
		this.mScrollPaneATTerminal.setViewportView(mTextPaneATTerminal);
		
		this.mButtonClearATTerminal = new JButton("Clear");
		mScrollPaneATTerminal.setColumnHeaderView(mButtonClearATTerminal);
		
		this.mButtonSendATCommand = new JButton("Send");
		this.mButtonSendATCommand.setBounds(434, 20, 99, 27);
		this.mPanelTerminal.add(this.mButtonSendATCommand);
		
		this.mTextFieldATCommand = new JTextField();
		this.mTextFieldATCommand.setBounds(10, 23, 407, 20);
		this.mPanelTerminal.add(this.mTextFieldATCommand);
		this.mTextFieldATCommand.setColumns(10);
		
		this.mScrollPaneOutput = new JScrollPane();
		this.mScrollPaneOutput.setBounds(10, 516, 548, 101);
		this.mContentPane.add(this.mScrollPaneOutput);
		
		this.mTextPaneOutput = new JTextPane();
		this.mScrollPaneOutput.setViewportView(this.mTextPaneOutput);
		
		//-----
		this.mPanelATTerminalMngt = new CPanelATTerminalMngt(this);
		this.mPanelSerialPortMngt = new CPanelSerialPortMngt(this);
		
		this.mPanelDeviceInfosSettingsMngt = new CPanelDeviceInfosSettingsMngt(this);
	}
	
	public JPanel getPanelSerial() {
		return this.mPanelSerial;
	}
	
	public JTabbedPane getJTabddedPaneMainPane() {
		return this.mTabbedMainPane;
	}
	
	public JList<SerialPort> getJListSerialComPorts() {
		return this.mListSerialPorts;
	}
	
	public JButton getJButtonRefreshListSerialComPorts() {
		return this.mButtonnRefreshListSerialPorts;
	}
	
	public JButton getJButtonComPortConnect() {
		return this.mButtonSerialPortConnect;
	}
	
	public JComboBox<Integer> getJComboBoxSerialSpeeds() {
		return this.mComboBoxSerialSpeeds;
	}
	
	public CPanelSerialPortMngt GetPanelSerialPortMngt() {
		return this.mPanelSerialPortMngt;
	}
	
	public JLabel getJLabelSerialPortSpeed() {
		return this.mLabelSerialPortSpeed;
	}
	
	public JCheckBox getJCheckBoxKeepPreviousInfosSettingsSerialPort() {
		return this.mCheckboxKeepPreviousInfosSettingsSerialPort;
	}
	
	public JLabel getJLabelAvailableSerialPorts() {
		return this.mLabelAvailableSerialPorts;
	}
	
	public JTextPane getJTextPaneATTerminal() {
		return this.mTextPaneATTerminal;
	}
	
	public JButton getJButtonClearATTerminal () {
		return this.mButtonClearATTerminal;
	}
	
	public JButton getJButtonSendATCommand() {
		return this.mButtonSendATCommand;
	}
	
	public CPanelATTerminalMngt getPaneATTerminalMngt() {
		return this.mPanelATTerminalMngt;
	}
	
	public JTextField getJTextFieldATCommand() {
		return this.mTextFieldATCommand;
	}
	
	public JTabbedPane getJTabbedPaneDeviceInfosSettings() {
		return this.mTabbedPaneDeviceInfosSettings;
	}
	
	public CPanelDeviceInfosSettingsMngt getPanelDeviceInfosSettingsMngt() {
		return this.mPanelDeviceInfosSettingsMngt;
	}
	
	public JTable getJTableDeviceInfo() {
		return this.mTableDeviceInfos;
	}
	
	public JTable getJTableDeviceSettings() {
		return this.mTableDeviceSettings;
	}
	
	public JButton getJButtonReadDeviceSettings () {
		return this.mButtonReadDeviceSettings;
	}
	
	public JButton getJButtonWriteDeviceSettings () {
		return this.mButtonWriteDeviceSettings;
	}
	
	public JButton getJButtonSaveDeviceSettings () {
		return this.mButtonSaveDeviceSettings;
	}

	public JButton getJButtonLoadDeviceSettings () {
		return this.mButtonLoadDeviceSettings;
	}
	
	public JTextPane getJTextPaneOutput() {
		return this.mTextPaneOutput;
	}
	
	public void insertMessageToOutput(String pstrData, CAppSettings.enOutputMessageType pMessageType) {
		
		if (pMessageType.getCodeInt() > CAppSettings.getVerboseMode().getCodeInt()) {
			return;
		}
		
		Document lDocument = this.mTextPaneOutput.getDocument();
		Style lStyle = this.mTextPaneOutput.addStyle("", null);
		String lstrMessageTypePrefix = "";
		
		switch (pMessageType) {
			case INFO:
				StyleConstants.setForeground(lStyle, Color.black);
				lstrMessageTypePrefix = "[INFO]";
				break;
				
			case DEBUG:
				StyleConstants.setForeground(lStyle, Color.black);
				lstrMessageTypePrefix = "[DEBUG]";
				break;
				
			case ERROR:
				StyleConstants.setForeground(lStyle, Color.red);
				lstrMessageTypePrefix = "[ERROR]";
				break;
				
			default:
				break;
		}
		
		
		DateTimeFormatter lDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime lCurrentDateTime = LocalDateTime.now();  
		
		try {
			lDocument.insertString(lDocument.getLength(), lDateTimeFormatter.format(lCurrentDateTime) + " " + lstrMessageTypePrefix + " : " + pstrData + "\r\n", lStyle);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}
}
