package com.niton.util;

/**
 * A utility to enable float conversion to long (use case: storing precise coma values)
 */
public class FloatInt {
	public static String formatAsFloat(long i, int percition) {
		int base = (int) Math.pow(10, percition);
		boolean negative = i < 0;
		if (negative)
			i *= -1;
		long cent = i % base;
		long eur = (i - cent) / base;
		return (negative ? "-" : "") + eur + "." + cent;
	}
	public static float toFloat(long i, int percition) {
		int base = (int) Math.pow(10, percition);
		return i/(float)base;
	}
	public static long toLong(long i,int percition){
		int base = (int) Math.pow(10, percition);
		return i*base;
	}

	public static long parseFloatToLong(String s, int percition) {
		int base = (int) Math.pow(10, percition);
		try {
			String[] vals = s.split("[.,]");
			if(vals.length==0)
				throw new IllegalArgumentException("The number \"" + s + "\" is not a parasable number");
			if (vals.length == 1)
				return Long.parseLong(vals[0]) * base;
			int cents = Integer.parseInt(vals[1]);
			if (vals[1].length() < 2)
				cents *= 10;
			int eurs = Integer.parseInt(vals[0]) * base;
			boolean neg = eurs < 0;
			if (neg)
				cents *= -1;
			return cents + eurs;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The number \"" + s + "\" is not a parasable number");
		}

	}
}
