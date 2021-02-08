package com.gepeo.device;
/**
 * 
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author gille
 *
 */
public class CAppSettings {
	
	public static enum OS {
		WIN,
		LINUX,
		MACOS,
		UNDEF
	};
	
	public static enum enOutputMessageType {
		SILENT("1"),
		ERROR("2"),
		INFO("3"),
		DEBUG("4");
		
		public final String mstrCode;
		
		private enOutputMessageType(String pstrCode) {
			this.mstrCode = pstrCode;
		}
		
		public static enOutputMessageType decode(final String pstrCode) {
		    return Stream.of(enOutputMessageType.values()).filter(targetEnum -> targetEnum.mstrCode.equals(pstrCode)).findFirst().orElse(null);
		}
		
		public static enOutputMessageType getEnumValue(final String pstrCode) {
		    try {
		    	return enOutputMessageType.valueOf(pstrCode);
		    } catch (Exception e) {
		    	return SILENT;
		    }
		}
		
		public String getCode() {
			return mstrCode;
		}
		
		public int getCodeInt() {
			return Integer.parseInt(mstrCode);
		}
	};
	
	public static final Integer[] 		SERIAL_SPEEDS_LIST														= new Integer[]{9600, 38400, 115200};
	
	
	public static final int				TABBEDPANE_SERIALPORT_SETTINGS__INDEX									= 0;
	public static final int 			TABBEDPANE_DEVICE_INFOS_SETTINGS__INDEX									= 1;
	public static final int 			TABBEDPANE_AT_TERMINAL__INDEX											= 2;
	
	public static final int 			TABBEDPANE__DEVICE_INFOS_SETTINGS__INFOS_INDEX							= 0;
	public static final int 			TABBEDPANE__DEVICE_INFOS_SETTINGS__SETTINGS_INDEX						= 1;
	
	public static final String 			APP_TITLE																= "Devices Setting Tool";
	public static final	String			APP_VERSION																= "V1.0";
	
	public static final String			APP_SETTINGS__FILE_NAME													= "settings.json";
	public static final String 			JSON_SCHEMA_SETTINGS_KEY 												= "settings";
	public static final String 			JSON_SCHEMA_INFOS_KEY 													= "infos";
	
	
	/*defaults values*/
	public static String				PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION 					= "Refresh";
	public static String 				PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_CONNECT 			= "Connect";
	public static String 				PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_DISCONNECT 		= "Disconnect";
	public static String 				PANEL_SERIAL_PORT__CHECKBOX_KEEP_PREVIOUS_INFOS_SETTINGS__CAPTION 		= "Keep previous Infos&Settings";
	public static String 				PANEL_SERIAL_PORT__LABEL_AVAILABLE_SERIAL_PORTS__CAPTION 				= "Available Serials Port";
	public static String 				PANEL_SERIAL_PORT__LABEL_SPEED_SERIAL_PORTS__CAPTION 					= "Serial speed";

	public static String 				PANEL_AT_TERMINAL__BUTTON_CLEAR__CAPTION_DISCONNECT 					= "Clear";
	public static String 				PANEL_AT_TERMINAL__BUTTON_SEND__CAPTION_DISCONNECT 						= "Send";
	
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__BUTTON_READ_DEVICE_SETTINGS__CAPTION 		= "Read Settings";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__BUTTON_WRITE_DEVICE_SETTINGS__CAPTION 		= "Write Settings";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__BUTTON_SAVE_DEVICE_SETTINGS__CAPTION 		= "Save Settings";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__BUTTON_LOAD_DEVICE_SETTINGS__CAPTION 		= "Load Settings";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__TAB_SETTINGS_CAPTION						= "Settings";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__TAB_INFOS_CAPTION							= "Infos";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__LOAD_FILE_DEVICE_ERROR__MESSAGE			= "File's device type does not fit the connected one. Continue ?";
	public static String 				PANEL_DEVICE_INFOS_SETTINGS__SAVE_FILE_ALREADY_EXIST__MESSAGE			= "File already exists. Continue ?";
	
	private static enOutputMessageType 	VERBOSE_MODE															= enOutputMessageType.DEBUG;
	private static String 				INFOS_SETTINGS_SCHEMA_JSON__FILE_NAME									= "schema.json";
	
	
	private static String				mstrAppPath 															= "";
	private static OS					menCurrentOS 															= OS.UNDEF;
	
	private static Integer				miSerialSpeed															= 115200;

	public static void loadAppSettingsFile() {
	}
	
	public static void init() throws Exception {
		
		mstrAppPath = (new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toString())).getParent();
		String lstrCurrentOS = System.getProperty("os.name").toLowerCase();
		
		if (lstrCurrentOS.indexOf("win") >= 0) {
			menCurrentOS = CAppSettings.OS.WIN;
			mstrAppPath = mstrAppPath.replace("file:\\", "") + "\\";
		} else {
			if (lstrCurrentOS.indexOf("mac") >= 0) {
				menCurrentOS = CAppSettings.OS.MACOS;
				mstrAppPath = mstrAppPath.replace("file:/", "/") + "/";
			} else {
				if ((lstrCurrentOS.indexOf("nix") >= 0) || ( lstrCurrentOS.indexOf("nux") >= 0) || ( lstrCurrentOS.indexOf("aix") >= 0)) {
					menCurrentOS = CAppSettings.OS.LINUX;
					mstrAppPath = mstrAppPath.replace("file:/", "/") + "/";
				}
			}
		}
		
		JSONObject ljsonObject;
		
		try {
			JSONObject lJSONObjectAppSettings = (JSONObject) new JSONParser().parse(new FileReader(new File(mstrAppPath + APP_SETTINGS__FILE_NAME)));
			
			if (lJSONObjectAppSettings.containsKey("components_caption")) {
				ljsonObject = (JSONObject)lJSONObjectAppSettings.get("components_caption");
				
				if (ljsonObject.containsKey("button_refresh_available_serial_ports_caption")) {
					PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION = ljsonObject.get("button_refresh_available_serial_ports_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_connect_serial_port_caption")) {
					PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_CONNECT = ljsonObject.get("button_connect_serial_port_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_disconnect_serial_port_caption")) {
					PANEL_SERIAL_PORT__BUTTON_REFRESH_COM_PORTS__CAPTION_DISCONNECT = ljsonObject.get("button_disconnect_serial_port_caption").toString();
				}
				
				if (ljsonObject.containsKey("label_serial_speed_caption")) {
					PANEL_SERIAL_PORT__LABEL_SPEED_SERIAL_PORTS__CAPTION = ljsonObject.get("label_serial_speed_caption").toString();
				}
				
				if (ljsonObject.containsKey("checkbox_keep_previous_infos_settings_caption")) {
					PANEL_SERIAL_PORT__CHECKBOX_KEEP_PREVIOUS_INFOS_SETTINGS__CAPTION = ljsonObject.get("checkbox_keep_previous_infos_settings_caption").toString();
				}
				
				if (ljsonObject.containsKey("label_available_serial_ports_caption")) {
					PANEL_SERIAL_PORT__LABEL_AVAILABLE_SERIAL_PORTS__CAPTION = ljsonObject.get("label_available_serial_ports_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_clear_at_terminal_caption")) {
					PANEL_AT_TERMINAL__BUTTON_CLEAR__CAPTION_DISCONNECT = ljsonObject.get("button_clear_at_terminal_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_send_at_terminal_caption")) {
					PANEL_AT_TERMINAL__BUTTON_SEND__CAPTION_DISCONNECT = ljsonObject.get("button_send_at_terminal_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_read_infos_settings_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__BUTTON_READ_DEVICE_SETTINGS__CAPTION = ljsonObject.get("button_read_infos_settings_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_write_infos_settings_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__BUTTON_WRITE_DEVICE_SETTINGS__CAPTION = ljsonObject.get("button_write_infos_settings_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_load_infos_settings_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__BUTTON_LOAD_DEVICE_SETTINGS__CAPTION = ljsonObject.get("button_load_infos_settings_caption").toString();
				}
				
				if (ljsonObject.containsKey("button_save_infos_settings_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__BUTTON_SAVE_DEVICE_SETTINGS__CAPTION = ljsonObject.get("button_save_infos_settings_caption").toString();
				}
				
				if (ljsonObject.containsKey("tab_infos_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__TAB_INFOS_CAPTION = ljsonObject.get("tab_infos_caption").toString();
				}
				
				if (ljsonObject.containsKey("tab_settings_caption")) {
					PANEL_DEVICE_INFOS_SETTINGS__TAB_SETTINGS_CAPTION = ljsonObject.get("tab_settings_caption").toString();;
				}
			} else {
				throw new Exception("CAppSettings:init: Settings file does not contain 'component_caption' key");
			}
			
			if (lJSONObjectAppSettings.containsKey("settings")) {
				ljsonObject = (JSONObject)lJSONObjectAppSettings.get("settings");
				
				if (ljsonObject.containsKey("schema_definition_file_name")) {
					INFOS_SETTINGS_SCHEMA_JSON__FILE_NAME = ljsonObject.get("schema_definition_file_name").toString();
				}
				
				if (ljsonObject.containsKey("verbose_mode")) {
					VERBOSE_MODE = CAppSettings.enOutputMessageType.getEnumValue(ljsonObject.get("verbose_mode").toString());
				}
				
				if (ljsonObject.containsKey("serial_speed")) {
					miSerialSpeed = (int)(long)ljsonObject.get("serial_speed");
				}
				
			} else {
				throw new Exception("CAppSettings:init: Settings file does not contain 'settings' key");
			}
		} catch (IOException | ParseException e) {
			throw new Exception("CAppSettings:init: " + e.getMessage());
		}
	}
	
	public static String getAppFullPath() {
		return mstrAppPath;
	}
	
	public static OS getCurrentOS() {
		return menCurrentOS;
	}
	
	public static String getSchemaDefinitionPathName() {
		return mstrAppPath + INFOS_SETTINGS_SCHEMA_JSON__FILE_NAME;
	}
	
	public static String getAppTitle() {
		return APP_TITLE + " " + APP_VERSION + " by gepeo";
	}
	
	public static enOutputMessageType getVerboseMode() {
		return VERBOSE_MODE;
	}
	
	public static int getSerialSpeed() {
		return miSerialSpeed;
	}
	
	public static void setSerialSpeed(int piSerialSpeed) {
		miSerialSpeed = piSerialSpeed;
	}
}
