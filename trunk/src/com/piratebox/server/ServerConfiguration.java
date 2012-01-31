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

/**
 * This class contains informations about the configuration of the server
 * @author Aylatan
 */
public class ServerConfiguration {

	/**
	 * The port that will be used by the server
	 */
	public static final int PORT = 1337;
	/**
	 * The name of the wifi access point
	 */
	public static final String WIFI_AP_NAME = "P1R4T3B0X";
	/**
	 * The default root directory
	 */
	public static final String DEFAULT_ROOT_DIR = "/sdcard/piratebox";
	
	private static String rootDir = DEFAULT_ROOT_DIR;
	/**
	 * Returns the value of the current set root directory.
	 * @return the path to the directory to be used as root
	 */
	public static String getRootDir() {
		return rootDir;
	}
	
	/**
	 * Sets the value of the current root directory.
	 * @param rootDir the path to the directory to use
	 */
	public static void setRootDir(String rootDir) {
	    ServerConfiguration.rootDir = rootDir;
	}
}
