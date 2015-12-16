package com.hopebaytech.hcfsmgmt.utils;

import java.util.Locale;

public class UnitConverter {
	
	public static String convertByteToProperUnit(long amount) {
		float result = amount;
		String[] unit = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
		int unitIndex = 0;
		while (true) {
			float tmp = result / 1024f;
			if ((long) tmp > 0) {
				result = tmp;
				unitIndex++;
			} else {
				break;
			}
		}

		if (result == (long) result) {
			return String.format(Locale.getDefault(), "%d" + unit[unitIndex], (long) result);
		} else {
			return String.format(Locale.getDefault(), "%.2f" + unit[unitIndex], result);
		}
	}
	
}
