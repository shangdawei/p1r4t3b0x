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

package com.piratebox.utils;

/**
 * This class describes a callback.
 * This object is used to perform an asynchronous call to a function.
 * @author Aylatan
 */
public abstract class Callback {
	/**
	 * This method is the one called asynchronously.
	 * @param arg the argument of this method depends on the context where it is used. Check the documentation of the class using it for more information.
	 */
	public abstract void call(Object arg);
}
