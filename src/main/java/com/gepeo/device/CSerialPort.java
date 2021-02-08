package com.gepeo.device;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author gille
 *
 */
public class CSerialPort {	
	private static int MAX_OUTGOING_FUFFER_SIZE = 2048;
	private static int INCOMING_WAITING_TIMEOUT = 10000;		//millisec
	
	private SerialPort mSerialPort = null;
	private boolean mbIsConnected = false;

	private MainFrame mMainFrame = null;
	
	public CSerialPort(MainFrame pMainFrame) {
		this.mMainFrame = pMainFrame;
	}

	/**
	 * Send an AT command to device and wait for response
	 * 
	 * Return:
	 * 		String: response message
	 *
	 */
	public String sendATCommand(String pStrATCmd) throws Exception {
		byte[] lbyReadBuffer = new byte[MAX_OUTGOING_FUFFER_SIZE];
		int liBufferIndex = 0;
		
		int liBytesAvailableCount;
		long llTimeMills;
		
		//add termination characters
		pStrATCmd += "\r\n";
		
		if (mSerialPort != null) {
			if (mSerialPort.writeBytes(pStrATCmd.getBytes(), pStrATCmd.length()) != -1) {
				if (this.mMainFrame.getJTextPaneATTerminal() != null) {
					this.mMainFrame.getPaneATTerminalMngt().insertString(pStrATCmd);
				}
				
				//set timeout
				llTimeMills = System.currentTimeMillis();
				
				while ((System.currentTimeMillis() - llTimeMills) < INCOMING_WAITING_TIMEOUT) {
					//Retrieve count of available readable bytes 
					liBytesAvailableCount = mSerialPort.bytesAvailable();
					
					//reader byte and return after detecting end of transmission: \r\\r\n
					if (liBytesAvailableCount > 0) {
						mSerialPort.readBytes(lbyReadBuffer, 1, liBufferIndex++);
						
						if (this.mMainFrame.getPaneATTerminalMngt() != null) {
							this.mMainFrame.getPaneATTerminalMngt().insertString(Character.toString((char)lbyReadBuffer[liBufferIndex-1]));
						}
						
						
						if (liBufferIndex > 4) {
							if ((lbyReadBuffer[liBufferIndex-4] == '\r') 
								&& (lbyReadBuffer[liBufferIndex-3] == '\n') 
								&& (lbyReadBuffer[liBufferIndex-2] == '\r') 
								&& (lbyReadBuffer[liBufferIndex-1] == '\n')) {
								return new String(lbyReadBuffer, StandardCharsets.UTF_8);
							}
						}
					}
				}
			}
		} else {
			throw new Exception("CSerialPort:sendATCommand: Serial port is null");
		}
		

		throw new Exception("CSerialPort:sendATCommand: Timeout has been reached");
	}
	
	/**
	 * Retrieve the list of available Serial Com Ports and perfrom some
	 * filtering operation before returning
	 * 
	 * Return:
	 * 		List: list of available serial Com ports
	 *
	 */
	public ArrayList<SerialPort> getAvailableSerialComPorts() throws Exception {
		try {
			SerialPort[] lSerialPorts = SerialPort.getCommPorts();
			ArrayList<SerialPort> lArraySerialPorts = new ArrayList<SerialPort>(Arrays.asList(lSerialPorts));
			
			
			switch (CAppSettings.getCurrentOS()) {
				//if System is MAC-OS, return only Serial Ports starting with 'tty."
				case MACOS:
					lArraySerialPorts
						.stream()
						.filter(item -> !item.getSystemPortName().startsWith("tty."))
						.collect(Collectors.toList());
					break;
					
				case WIN:
					break;
					
				case LINUX:
					break;
				
				default: 
					break;
			}
						
			return lArraySerialPorts;
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	public SerialPort getSerialPort() {
		return this.mSerialPort;
	}
	
	public void setSerialPort(SerialPort pSerialPort) {
		this.mSerialPort = pSerialPort;
	}
	
	public boolean isConnected() {
		return this.mbIsConnected;
	}
	
	public boolean connectToSerialPort() {
		if ((this.mSerialPort != null) && (!this.mbIsConnected)) {
			this.mSerialPort.openPort();
			this.mSerialPort.setBaudRate(CAppSettings.getSerialSpeed());
			this.mSerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 3000, 0);
			this.mbIsConnected = true;
			return true;
		}
		
		return false;
	}
	
	public boolean disconnectFromSerialPort() {
		if (this.mbIsConnected) {
			this.mSerialPort.closePort();
			this.mbIsConnected = false; 
			return true;
		}
		
		return false;
	}
	
	public String getJsonSettings() {
		try {
			String lstrResponse =  sendATCommand("AT+JSONSTATUS");
			
			this.mMainFrame.insertMessageToOutput("AT+JSONSTATUS=" + lstrResponse, CAppSettings.enOutputMessageType.DEBUG);
			
			return lstrResponse;
		} catch (Exception e) {
			this.mMainFrame.insertMessageToOutput(e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}
		
		return null;
	}
	
	public boolean sendJsonSettings(String pstrATCommand) throws Exception {
		boolean lbResponse = sendATCommand("AT+JSONSETTINGS=" + pstrATCommand).contains("OK\r\n\r\n");
		
		this.mMainFrame.insertMessageToOutput("AT+JSONSETTINGS=" + (lbResponse ? "passed" : "failed"), CAppSettings.enOutputMessageType.DEBUG);
		
		return lbResponse;
	}
	
	public boolean sendSaveSettings() throws Exception {
		boolean lbResponse =  sendATCommand("AT+SAVESETTINGS").contains("OK\r\n\r\n");
		
		this.mMainFrame.insertMessageToOutput("AT+JSONSETTINGS=" + (lbResponse ? "passed" : "failed"), CAppSettings.enOutputMessageType.DEBUG);
		
		return lbResponse;
	}
}
