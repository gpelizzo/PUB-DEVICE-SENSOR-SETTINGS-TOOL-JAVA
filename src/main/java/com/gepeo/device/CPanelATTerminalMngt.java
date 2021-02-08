package com.gepeo.device;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.JTextPane;

/**
 * 
 */

/**
 * @author gille
 *
 */
public class CPanelATTerminalMngt implements ActionListener {
	
	private MainFrame 			mMainFrame;
	private Document			mATTerminalDocument;
	
	public CPanelATTerminalMngt(MainFrame pMainFrame) {
		this.mMainFrame = pMainFrame;
		
		this.mATTerminalDocument = this.mMainFrame.getJTextPaneATTerminal().getDocument();
		this.mMainFrame.getJTextPaneATTerminal().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_C) {
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(((JTextPane)e.getSource()).getSelectedText()), null);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		});
		
		this.mMainFrame.getJButtonClearATTerminal().setText(CAppSettings.PANEL_AT_TERMINAL__BUTTON_CLEAR__CAPTION_DISCONNECT);
		this.mMainFrame.getJButtonClearATTerminal().addActionListener(this);		
		
		this.mMainFrame.getJButtonSendATCommand().setText(CAppSettings.PANEL_AT_TERMINAL__BUTTON_SEND__CAPTION_DISCONNECT);
		this.mMainFrame.getJButtonSendATCommand().addActionListener(this);
		this.mMainFrame.getJTextFieldATCommand().addActionListener(this);
	}

	public void insertString(String pStrData) {
		try {
			this.mATTerminalDocument.insertString(this.mATTerminalDocument.getLength(), pStrData, null);
		} catch (BadLocationException e) {
			this.mMainFrame.insertMessageToOutput("CPanelATTerminalMngt:insertString " + e.getMessage(), CAppSettings.enOutputMessageType.ERROR);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.mMainFrame.getJButtonClearATTerminal()) {
			try {
				this.mATTerminalDocument.remove(0, this.mATTerminalDocument.getLength());
			} catch (BadLocationException e1) {
				this.mMainFrame.insertMessageToOutput("CPanelATTerminalMngt:actionPerformed " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
			}
		}
		
		if (e.getSource() == this.mMainFrame.getJButtonSendATCommand()) {
			sendATCommand();
		}
		
		if (e.getSource() == this.mMainFrame.getJTextFieldATCommand()) {
			sendATCommand();
		}
	}
	
	private void sendATCommand() {
		if (this.mMainFrame.getJTextFieldATCommand().getText().length() != 0) {
			try {
				this.mMainFrame.GetPanelSerialPortMngt().getSerialPort().sendATCommand(this.mMainFrame.getJTextFieldATCommand().getText());
				this.mMainFrame.getJTextFieldATCommand().setText("");
			} catch (Exception e1) {
				this.mMainFrame.insertMessageToOutput("CPanelATTerminalMngt:sendATCommand " + e1.getMessage(), CAppSettings.enOutputMessageType.ERROR);
			}
		}
	}
}
