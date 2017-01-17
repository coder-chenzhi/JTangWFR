package org.jtang.distance.ged.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.StringUtils;

/**
 * String edit distance calculation.
 * 
 * @author Roman Tekhov
 */
public class StringEditDistance {
	
	/**
	 * Calculates the similarity coefficient of two given strings
	 * based on their edit distance. The coefficient is equal to
	 * edit distance divided by the length of the longest string
	 * (largest possible edit distance).
	 * 
	 * @param str1 first string
	 * @param str2 second string
	 * 
	 * @return string similarity coefficient
	 */
	public static BigDecimal calculateCoefficient(String str1, String str2) {
		
		/*String a, b;
		
		// Determine the shorter string
		if(str1.length() < str2.length()) {
			a = str1;
			b = str2;
		} else {
			a = str2;
			b = str1;
		}
		
		double editDistance = StringUtils.getLevenshteinDistance(a, b);
		
		return BigDecimal.valueOf(editDistance).divide(
				BigDecimal.valueOf(b.length()), 2, RoundingMode.HALF_EVEN);
				*/
		if(str1.equals(str2)){
			return BigDecimal.valueOf(0).setScale(2);
		} else {
			return BigDecimal.valueOf(100).setScale(2);
		}
	}
}
