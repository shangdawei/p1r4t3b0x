package com.piratebox.server;

public class ServerConfiguration {

	public static final int PORT = 1337;
	public static final String WIFI_AP_NAME = "P1R4T3B0X";
	public static final String DEFAULT_ROOT_DIR = "/sdcard/piratebox";
	
	public static String getRootDir() {
		return "/sdcard/piratebox/";
	}
}
