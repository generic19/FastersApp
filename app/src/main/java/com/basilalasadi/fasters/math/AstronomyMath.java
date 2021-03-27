package com.basilalasadi.fasters.math;

import static java.lang.Math.*;


/**
 * Abstract class for calculating the equation of time using the Fourier method. Calculations are
 * valid between year 2000 and 2050, and are accurate up to +/- 3 seconds.
 *
 * Calculations are copied from https://equation-of-time.info/calculating-the-equation-of-time.
 */
public abstract class AstronomyMath {
	/**
	 * Calculates days since epoch (12 on January 1, 2000 UTC).
	 *
	 * @return Days since epoch in UTC.
	 */
	public static double daysSinceEpoch(int year, int month, int day, int hour, int minute, int timeZone) {
		double daysSinceEpochComponent1 = 367 * year - 730531.5;
		int daysSinceEpochComponent2 = -(7 * (year + (month + 9) / 12) / 4);
		int daysSinceEpochComponent3 = (275 * month / 9) + day;
		
		double daysToday = (hour + minute / 60d - timeZone) / 24;
		
		return daysSinceEpochComponent1 + daysSinceEpochComponent2 + daysSinceEpochComponent3 + daysToday - 2;
	}
	
	/**
	 * Calculates the equation of time using the Fourier method. Calculation is
	 * valid between year 2000 and 2050, and accurate up to +/- 3 seconds.
	 *
	 * Calculations are copied from https://equation-of-time.info/calculating-the-equation-of-time.
	 */
	public static double equationOfTime(double daysSinceEpoch) {
		int cycle = (int) (daysSinceEpoch / 365.25);
		
		double theta = 0.0172024 * (daysSinceEpoch - 365.25 * cycle);
		
		double amp1 = 7.36303 - cycle * 0.00009;
		double amp2 = 9.92465 - cycle * 0.00014;
		
		double phi1 = 3.07892 - cycle * 0.00019;
		double phi2 = -1.38995 + cycle * 0.00013;
		
		double eot1 = amp1 * sin(theta + phi1);
		double eot2 = amp2 * sin(2 * (theta + phi2));
		double eot3 = 0.31730 * sin(3 * (theta - 0.94686));
		double eot4 = 0.21922 * sin(4 * (theta - 0.60716));
		
		return 0.00526 + eot1 + eot2 + eot3 + eot4;
	}
	
	/**
	 * Calculates solar declination angle in radians.
	 *
	 * Citation for the equation used:
	 * P. I. Cooper, “The absorption of radiation in solar stills”, Solar Energy, vol. 12,
	 * pp. 333 - 346, 1969.
	 *
	 * @return Solar declination angle in radians.
	 */
	public static double solarDeclination(double daysSinceEpoch) {
		return -0.4092797 * Math.cos(2 * Math.PI / 365 * (daysSinceEpoch + 10));
	}
	
	/**
	 * Calculates solar local noon time in hours.
	 *
	 * @return local noon time in hours.
	 */
	public static double localSolarNoon(double daysSinceEpoch, int timeZone, double longitude) {
		return 12 + timeZone - longitude / 15 - equationOfTime(daysSinceEpoch) / 60;
	}
	
	/**
	 * Calculates offset from solar noon in hours for a given sun angle.
	 *
	 * Equation copied from http://praytimes.org/calculation.
	 *
	 * @param angleDegrees Angle of the sun in degrees (hour angle at noon is 0 degrees).
	 * @return Offset from solar noon in hours.
	 */
	public static double noonOffsetFromSunAngle(double angleDegrees, double daysSinceEpoch, double latitude) {
		final double a = angleDegrees * PI / 180;
		final double L = latitude * PI / 180;
		final double D = solarDeclination(daysSinceEpoch);
		
		final double nom = -sin(a) - sin(L) * sin(D);
		final double denom = cos(L) * cos(D);
		
		final double f = nom / denom;
		
		return acos(f) / 15 * 180 / PI;
	}
	
	/**
	 * Calculates time offset from local solar noon at which the shadow of an object is equal
	 * to `shadowLength` multiplied by its length.
	 *
	 * Equation copied from http://praytimes.org/calculation.
	 *
	 * @param shadowLength Ratio between an object's shadow and its length.
	 * @return Time offset from local solar noon.
	 */
	public static double noonOffsetFromShadowLength(double shadowLength, double daysSinceEpoch, double latitude) {
		final double t = shadowLength;
		final double L = latitude * PI / 180;
		final double D = solarDeclination(daysSinceEpoch);
		
		final double nom = sin(acot(t + tan(L - D))) - sin(L) * sin(D);
		final double denom = cos(L) * cos(D);
		
		final double f = nom / denom;
		
		return acos(f) / 15 * 180 / PI;
	}
	
	static double acot(double x) {
		return atan2(1, x);
	}
}
