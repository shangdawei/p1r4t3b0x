package com.piratebox.utils;

public class Utils {

	public static int indexOf(Object value, Object[] array) {
		for (int i = 0; i < array.length; i++) {
			if (value.equals(array[i])) {
				return i;
			}
		}
		
		return -1;
	}
}
