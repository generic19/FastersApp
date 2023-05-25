package com.basilalasadi.fasters.math;

import org.threeten.bp.ZonedDateTime;
import static com.basilalasadi.fasters.math.AstronomyMath.*;

import com.basilalasadi.fasters.util.TimeProvider;


/**
 * <p>Abstract class for calculating prayer times.</p>
 * @see AstronomyMath
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public abstract class PrayerTimings {
	/**
	 * <p>Get fajr time.</p>
	 * @param angleDegrees sun angle at fajr.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return fajr time in minutes from 00:00.
	 */
	public static double getFajr(double angleDegrees, double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double offset = noonOffsetFromSunAngle(angleDegrees, daysSinceEpoch, latitude);
		
		return noon - offset;
	}
	
	/**
	 * <p>Get fajr time.</p>
	 * @param method method enum.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return fajr time in minutes from 00:00.
	 */
	public static double getFajr(TimingsMethod method, double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		return getFajr(method.getFajrAngleDegrees(), daysSinceEpoch, timeZone, longitude, latitude);
	}
	
	/**
	 * <p>Get duhr time.</p>
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @return duhr time in minutes from 00:00.
	 */
	public static double getDuhr(double daysSinceEpoch, int timeZone, double longitude) {
		return localSolarNoon(daysSinceEpoch, timeZone, longitude);
	}
	
	/**
	 * <p>Get asr time.</p>
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @param useShafaiMethod use shafai method instead (shadow ratio of 2 instead of 1).
	 * @return asr time in minutes from 00:00.
	 */
	public static double getAsr(double daysSinceEpoch, int timeZone, double longitude, double latitude,
			boolean useShafaiMethod) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		double shadowLength = useShafaiMethod ? 2 : 1;
		double offset = noonOffsetFromShadowLength(shadowLength, daysSinceEpoch, latitude);
		
		return noon + offset;
	}
	
	/**
	 * <p>Get magrib time.</p>
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return magrib time in minutes from 00:00.
	 */
	public static double getMagrib(double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double offset = noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
		return noon + offset;
	}
	
	/**
	 * <p>Get isha time from sun angle.</p>
	 * @param angleDegrees sun angle at isha.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return isha time in minutes from 00:00.
	 */
	public static double getIsha(double angleDegrees, double daysSinceEpoch, int timeZone,
			double longitude, double latitude) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double offset = noonOffsetFromSunAngle(angleDegrees, daysSinceEpoch, latitude);
		return noon + offset / 60;
	}
	
	/**
	 * <p>Get isha time from fixed time offset.</p>
	 * @param offsetFromSunsetMinutes fixed time offset from magrib.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return isha time in minutes from 00:00.
	 */
	public static double getIsha(int offsetFromSunsetMinutes, double daysSinceEpoch, int timeZone,
			double longitude, double latitude) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double magribOffset = noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
		return noon + magribOffset + offsetFromSunsetMinutes / 60d;
	}
	
	/**
	 * <p>Get isha time using method (enum).</p>
	 * @param method method enum.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @param isRamadan whether it is ramadan at specified <em>daysSinceEpoch</em>.
	 * @return isha time in minutes from 00:00.
	 */
	public static double getIsha(TimingsMethod method, double daysSinceEpoch, int timeZone,
			double longitude, double latitude, boolean isRamadan) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		if (method.usesFixedOffsetForIsha()) {
			return getIsha(method.getIshaFixedOffset(isRamadan), daysSinceEpoch, timeZone, longitude, latitude);
		}
		else {
			return getIsha(method.getIshaAngleDegrees(), daysSinceEpoch, timeZone, longitude, latitude);
		}
	}
	
	/**
	 * <p>Get all timings for day specified by <em>daysSinceEpoch</em>.</p>
	 * @param fajrAngleDegrees sun angle at fajr.
	 * @param useShafaiMethod use shafai method instead (shadow ratio of 2 instead of 1).
	 * @param ishaAngleDegrees sun angle at isha.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return timings array in minutes since 00:00.
	 */
	public static double[] getTimings(double fajrAngleDegrees, boolean useShafaiMethod, double ishaAngleDegrees,
			double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double fajr = noon - noonOffsetFromSunAngle(fajrAngleDegrees, daysSinceEpoch, latitude);
		double asr = noon + noonOffsetFromShadowLength(useShafaiMethod ? 2 : 1, daysSinceEpoch, latitude);
		double magrib = noon + noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
		double isha = noon + noonOffsetFromSunAngle(ishaAngleDegrees, daysSinceEpoch, latitude);
		
		return new double[]{ fajr, noon, asr, magrib, isha };
	}
	
	/**
	 * <p>Get all timings for day specified by <em>daysSinceEpoch</em>.</p>
	 * @param fajrAngleDegrees sun angle at fajr.
	 * @param useShafaiMethod use shafai method instead (shadow ratio of 2 instead of 1).
	 * @param ishaTimeOffsetMinutes fixed time offset from magrib in minutes.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @return timings array in minutes since 00:00.
	 */
	public static double[] getTimings(double fajrAngleDegrees, boolean useShafaiMethod, int ishaTimeOffsetMinutes,
			double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		double fajr = noon - noonOffsetFromSunAngle(fajrAngleDegrees, daysSinceEpoch, latitude);
		double asr = noon + noonOffsetFromShadowLength(useShafaiMethod ? 2 : 1, daysSinceEpoch, latitude);
		double magrib = noon + noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
		double isha = magrib + ishaTimeOffsetMinutes / 60d;
		
		return new double[]{ fajr, noon, asr, magrib, isha };
	}
	
	/**
	 * <p>Get all timings for day specified by <em>daysSinceEpoch</em> and method (enum).</p>
	 * @param method method enum.
	 * @param daysSinceEpoch days (and fraction of day) since 2000-01-01 00:00 UTC.
	 * @param timeZone time zone offset from UTC.
	 * @param longitude longitude in degrees.
	 * @param latitude latitude in degrees.
	 * @param isRamadan whether it is ramadan at specified <em>daysSinceEpoch</em>.
	 * @param useShafaiMethod use shafai method instead (shadow ratio of 2 instead of 1).
	 * @return timings array in minutes since 00:00.
	 */
	public static double[] getTimings(TimingsMethod method, double daysSinceEpoch, int timeZone,
			double longitude, double latitude, boolean isRamadan, boolean useShafaiMethod) {
		
		double fajrAngle = method.getFajrAngleDegrees();
		
		if (method.usesFixedOffsetForIsha()) {
			int ishaOffset = (int) method.getIshaFixedOffset(isRamadan);
			return getTimings(fajrAngle, useShafaiMethod, ishaOffset, daysSinceEpoch, timeZone, longitude, latitude);
		}
		else {
			double ishaAngle = method.getIshaAngleDegrees();
			return getTimings(fajrAngle, useShafaiMethod, ishaAngle, daysSinceEpoch, timeZone, longitude, latitude);
		}
	}
	
	/**
	 * <p>Convert zoned datetime to days since epoch.</p>
	 * @param date zoned datetime.
	 * @return days (and fraction of day) since 2000-01-01 00:00 UTC.
	 */
	public static double toDaysSinceEpoch2000(ZonedDateTime date) {
		return daysSinceEpoch(date.getYear(),
				date.getMonthValue(),
				date.getDayOfMonth(),
				date.getHour(),
				date.getMinute(),
				date.getOffset().getTotalSeconds() / 3600);
	}
	
	/**
	 * <p>Converts current zoned datetime to days since epoch.</p>
	 * @return days (and fraction of day) since 2000-01-01 00:00 UTC.
	 */
	public static double toDaysSinceEpoch2000() {
		return toDaysSinceEpoch2000(TimeProvider.now());
	}
}
