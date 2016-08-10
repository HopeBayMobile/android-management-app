package com.hopebaytech.hcfsmgmt.utils;

import java.util.Locale;

public class UnitConverter {

	public static String formatPercentage(float number) {
		String formatValue;
		number = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", number));
		if ((long) number == number) {
			formatValue = String.format(Locale.getDefault(), "%d", (long) number);
		} else {
			formatValue = String.format(Locale.getDefault(), "%.1f", number);
		}
		return formatValue;
	}

	public static String convertByteToProperUnit(long amount) {
		float result = amount;
		String[] unit = new String[] { "Byte", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
		int unitIndex = 0;
		while (true) {
			float tmp = result / 1024f;
			if (result >= 1000) {
				if (tmp > 0) {
					result = tmp;
					unitIndex++;
				} else {
					break;
				}
			} else if (result < 0) {
				result = 0;
				break;
			} else {
				break;
			}
		}

		float number = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", result));
		String mUnit = unit[unitIndex];
		if (mUnit.equals(unit[0])) {
			if ((long) number > 1) {
				mUnit = "Bytes";
			}
		}
        if ((long) number == number) {
            return String.format(Locale.getDefault(), "%d " + mUnit, (long) number);
        } else {
            return String.format(Locale.getDefault(), "%.1f " + mUnit, number);
        }
	}
	// public static String convertByteToProperUnit(long amount) {
	// float result = amount;
	// String[] unit = new String[] { "Byte", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
	// int unitIndex = 0;
	// while (true) {
	// float tmp = result / 1024f;
	// if ((long) tmp > 0) {
	// result = tmp;
	// unitIndex++;
	// } else {
	// break;
	// }
	// }
	//
	// if (result == (long) result) {
	// return String.format(Locale.getDefault(), "%d " + unit[unitIndex], (long) result);
	// } else {
	// return String.format(Locale.getDefault(), "%.2f " + unit[unitIndex], result);
	// }
	// }

//	public static String convertByteToProperUnit(long amount, long maxValue) {
//		float result = amount;
//		String[] unit = new String[] { "Byte", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
//		int unitIndex = 0;
//		while (true) {
//			float tmp = result / 1024f;
//			if ((long) tmp > 0) {
//				result = tmp;
//				unitIndex++;
//			} else {
//				break;
//			}
//		}
//
//		float maxValueResult = maxValue;
//		int maxValueUnitIndex = 0;
//		while (true) {
//			float tmp = maxValueResult / 1024f;
//			if ((long) tmp > 0) {
//				maxValueResult = tmp;
//				maxValueUnitIndex++;
//			} else {
//				break;
//			}
//		}
//
//		if (maxValueUnitIndex == unitIndex) {
//			if (result == (long) result) {
//				if (result == (long) maxValueResult) {
//					result = result - 1;
//				}
//				return String.format(Locale.getDefault(), "%d " + unit[unitIndex], (long) result);
//			} else {
//				result = result - 0.01f;
//				return String.format(Locale.getDefault(), "%.2f " + unit[unitIndex], result);
//			}
//		} else {
//			if (result == (long) result) {
//				return String.format(Locale.getDefault(), "%d " + unit[unitIndex], (long) result);
//			} else {
//				return String.format(Locale.getDefault(), "%.2f " + unit[unitIndex], result);
//			}
//		}
//
//	}

}
