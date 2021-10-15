package com.basilalasadi.fasters.math;

import org.threeten.bp.ZonedDateTime;
import static com.basilalasadi.fasters.math.AstronomyMath.*;


/**
 * <p>Abstract class for calculating prayer times.</p>
 * @see AstronomyMath
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public abstract class PrayerTimings {
	
	public enum Method {
		IslamicSocietyOfNorthAmerica,
		MuslimWorldLeague,
		UmmAlQuraUniversityMakkah,
		EgyptianGeneralAuthorityOfSurvey,
		InstituteOfGeophysicsTehran,
		GulfRegion,
		Kuwait,
		Qatar,
		MajlisUgamaIslamSingapuraSingapore,
		UnionOrganizationIslamicDeFrance,
		DiyanetIsleriBaskanligiTurkey,
		SpiritualAdministrationOfMuslimsOfRussia,
	}
	
	private static final String COUNTRY_UNITED_STATES         = "United States";
	private static final String COUNTRY_CANADA                = "Canada";
	private static final String COUNTRY_MEXICO                = "Mexico";
	private static final String COUNTRY_OMAN                  = "Oman";
	private static final String COUNTRY_BAHRAIN               = "Bahrain";
	private static final String COUNTRY_UNITED_ARAB_EMIRATES  = "United Arab Emirates";
	private static final String COUNTRY_SAUDI_ARABIA          = "Saudi Arabia";
	private static final String COUNTRY_EGYPT                 = "Egypt";
	private static final String COUNTRY_IRAN                  = "Iran";
	private static final String COUNTRY_KUWAIT                = "Kuwait";
	private static final String COUNTRY_QATAR                 = "Qatar";
	private static final String COUNTRY_SINGAPORE             = "Singapore";
	private static final String COUNTRY_FRANCE                = "France";
	private static final String COUNTRY_TURKEY                = "Turkey";
	private static final String COUNTRY_RUSSIA                = "Russia";
	
	
	/**
	 * <p>Get default method (enum) for country.</p>
	 * @param country country name
	 * @return the default method (enum) for country
	 */
	public static Method getMethodForCountry(String country) {
		switch (country) {
			case COUNTRY_UNITED_STATES:
			case COUNTRY_CANADA:
			case COUNTRY_MEXICO:
				return Method.IslamicSocietyOfNorthAmerica;
			
			case COUNTRY_OMAN:
			case COUNTRY_BAHRAIN:
			case COUNTRY_UNITED_ARAB_EMIRATES:
				return Method.GulfRegion;
			
			case COUNTRY_SAUDI_ARABIA:  return Method.UmmAlQuraUniversityMakkah;
			case COUNTRY_EGYPT:         return Method.EgyptianGeneralAuthorityOfSurvey;
			case COUNTRY_IRAN:          return Method.InstituteOfGeophysicsTehran;
			case COUNTRY_KUWAIT:        return Method.Kuwait;
			case COUNTRY_QATAR:         return Method.Qatar;
			case COUNTRY_SINGAPORE:     return Method.MajlisUgamaIslamSingapuraSingapore;
			case COUNTRY_FRANCE:        return Method.UnionOrganizationIslamicDeFrance;
			case COUNTRY_TURKEY:        return Method.DiyanetIsleriBaskanligiTurkey;
			case COUNTRY_RUSSIA:        return Method.SpiritualAdministrationOfMuslimsOfRussia;
			
			default:                    return Method.MuslimWorldLeague;
		}
	}
	
	/**
	 * <p>Get the sun angle at fajr.</p>
	 * @param method method enum.
	 * @return sun angle in degrees.
	 */
	public static double getFajrAngleDegrees(Method method) {
		switch (method) {
			case IslamicSocietyOfNorthAmerica:
			case SpiritualAdministrationOfMuslimsOfRussia:
				return 15;
			
			case MuslimWorldLeague:
			case Kuwait:
			case MajlisUgamaIslamSingapuraSingapore:
			case DiyanetIsleriBaskanligiTurkey:
				return 18;
			
			case UmmAlQuraUniversityMakkah:
				return 18.5;
			
			case EgyptianGeneralAuthorityOfSurvey:
			case GulfRegion:
			case Qatar:
				return 19.5;
			
			case InstituteOfGeophysicsTehran:
				return 17.7;
			
			case UnionOrganizationIslamicDeFrance:
				return 12;
			
			default:
				return -1;
			
		}
	}
	
	/**
	 * <p>Get the sun angle at isha.</p>
	 * @param method method enum.
	 * @return sun angle in degrees.
	 */
	public static double getIshaAngleDegrees(Method method) {
		switch (method) {
			case IslamicSocietyOfNorthAmerica:
			case SpiritualAdministrationOfMuslimsOfRussia:
			case InstituteOfGeophysicsTehran:
				return 15;
			
			case MuslimWorldLeague:
			case MajlisUgamaIslamSingapuraSingapore:
			case DiyanetIsleriBaskanligiTurkey:
				return 17;
			
			case EgyptianGeneralAuthorityOfSurvey:
				return 17.5;
			
			case Kuwait:
				return 18;
			
			case UnionOrganizationIslamicDeFrance:
				return 12;
			
			default:
				return -1;
			
		}
	}
	
	/**
	 * <p>Get the fixed time offset from sunset to isha.</p>
	 * @param method method enum.
	 * @param isRamadan <em>true</em> if and only if current month is ramadan.
	 * @return time offset in minutes, or <em>-1</em> if method doesn't have a fixed offset.
	 */
	public static double getIshaFixedOffset(Method method, boolean isRamadan) {
		switch (method) {
			case GulfRegion:
			case Qatar:
				return 90;
			
			case UmmAlQuraUniversityMakkah:
				if (isRamadan) {
					return 120;
				}
				else {
					return 90;
				}
			
			default:
				return -1;
		}
	}
	
	/**
	 * <p>Get whether method (enum) has a fixed time offset for isha.</p>
	 * @param method method enum.
	 * @return <em>true</em> if and only if the method has a fixed time offset for isha.
	 */
	public static boolean getHasFixedIshaOffset(Method method) {
		switch (method) {
			case GulfRegion:
			case Qatar:
			case UmmAlQuraUniversityMakkah:
				return true;
			
			default:
				return false;
		}
	}
	
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
	public static double getFajr(Method method, double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		return getFajr(getFajrAngleDegrees(method), daysSinceEpoch, timeZone, longitude, latitude);
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
	public static double getIsha(Method method, double daysSinceEpoch, int timeZone,
			double longitude, double latitude, boolean isRamadan) {
		
		double noon = localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		if (getHasFixedIshaOffset(method)) {
			return getIsha(getIshaFixedOffset(method, isRamadan), daysSinceEpoch, timeZone, longitude, latitude);
		}
		else {
			return getIsha(getIshaAngleDegrees(method), daysSinceEpoch, timeZone, longitude, latitude);
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
	public static double[] getTimings(Method method, double daysSinceEpoch, int timeZone,
			double longitude, double latitude, boolean isRamadan, boolean useShafaiMethod) {
		
		double fajrAngle = getFajrAngleDegrees(method);
		
		if (getHasFixedIshaOffset(method)) {
			int ishaOffset = (int) getIshaFixedOffset(method, isRamadan);
			return getTimings(fajrAngle, useShafaiMethod, ishaOffset, daysSinceEpoch, timeZone, longitude, latitude);
		}
		else {
			double ishaAngle = getIshaAngleDegrees(method);
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
		return toDaysSinceEpoch2000(ZonedDateTime.now());
	}
}
