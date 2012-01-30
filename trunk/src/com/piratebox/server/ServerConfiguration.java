/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox.server;

public class ServerConfiguration {

	public static final int PORT = 1337;
	public static final String WIFI_AP_NAME = "P1R4T3B0X";
	public static final String DEFAULT_ROOT_DIR = "/sdcard/piratebox";
	
	public static String getRootDir() {
		return DEFAULT_ROOT_DIR;
	}
}
