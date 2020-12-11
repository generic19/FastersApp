package com.basilalasadi.fasters.math;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.TemporalField;

public abstract class PrayerTimings {
	
	public static final int METHOD_ISLAMIC_SOCIETY_OF_NORTH_AMERICA 				= 0;
	public static final int METHOD_MUSLIM_WORLD_LEAGUE 								= 1;
	public static final int METHOD_UMM_AL_QURA_UNIVERSITY_MAKKAH 					= 2;
	public static final int METHOD_EGYPTIAN_GENERAL_AUTHORITY_OF_SURVEY 			= 3;
	public static final int METHOD_INSTITUTE_OF_GEOPHYSICS_TEHRAN 					= 4;
	public static final int METHOD_GULF_REGION 										= 5;
	public static final int METHOD_KUWAIT 											= 6;
	public static final int METHOD_QATAR 											= 7;
	public static final int METHOD_MAJLIS_UGAMA_ISLAM_SINGAPURA_SINGAPORE 			= 8;
	public static final int METHOD_UNION_ORGANIZATION_ISLAMIC_DE_FRANCE 			= 9;
	public static final int METHOD_DIYANET_ISLERI_BASKANLIGI_TURKEY 				= 10;
	public static final int METHOD_SPIRITUAL_ADMINISTRATION_OF_MUSLIMS_OF_RUSSIA 	= 11;
	
	
	public static int getMethodForCountry(String country) {
		
		if (
				country.contains("United States") ||
				country.contains("Canada") ||
				country.contains("Mexico"))
			return METHOD_ISLAMIC_SOCIETY_OF_NORTH_AMERICA;
		
		else if (
				country.contains("Oman") ||
			    country.contains("Bahrain") ||
			    country.contains("United Arab Emirates"))
			return METHOD_GULF_REGION;
		
		else if (country.contains("Saudi Arabia"))   return METHOD_UMM_AL_QURA_UNIVERSITY_MAKKAH;
		else if (country.contains("Egypt"))          return METHOD_EGYPTIAN_GENERAL_AUTHORITY_OF_SURVEY;
		else if (country.contains("Iran"))           return METHOD_INSTITUTE_OF_GEOPHYSICS_TEHRAN;
		else if (country.contains("Kuwait"))         return METHOD_KUWAIT;
		else if (country.contains("Qatar"))          return METHOD_QATAR;
		else if (country.contains("Singapore"))      return METHOD_MAJLIS_UGAMA_ISLAM_SINGAPURA_SINGAPORE;
		else if (country.contains("France"))         return METHOD_UNION_ORGANIZATION_ISLAMIC_DE_FRANCE;
		else if (country.contains("Turkey"))         return METHOD_DIYANET_ISLERI_BASKANLIGI_TURKEY;
		else if (country.contains("Russia"))         return METHOD_SPIRITUAL_ADMINISTRATION_OF_MUSLIMS_OF_RUSSIA;
		
		else return METHOD_MUSLIM_WORLD_LEAGUE;
	}
	
	public static double getFajrAngleDegrees(int method) {
		
		switch (method) {
			
			case METHOD_ISLAMIC_SOCIETY_OF_NORTH_AMERICA:
			case METHOD_SPIRITUAL_ADMINISTRATION_OF_MUSLIMS_OF_RUSSIA:
				return 15;
				
			case METHOD_MUSLIM_WORLD_LEAGUE:
			case METHOD_KUWAIT:
			case METHOD_MAJLIS_UGAMA_ISLAM_SINGAPURA_SINGAPORE:
			case METHOD_DIYANET_ISLERI_BASKANLIGI_TURKEY:
				return 18;
				
			case METHOD_UMM_AL_QURA_UNIVERSITY_MAKKAH:
				return 18.5;
				
			case METHOD_EGYPTIAN_GENERAL_AUTHORITY_OF_SURVEY:
			case METHOD_GULF_REGION:
			case METHOD_QATAR:
				return 19.5;
				
			case METHOD_INSTITUTE_OF_GEOPHYSICS_TEHRAN:
				return 17.7;
				
			case METHOD_UNION_ORGANIZATION_ISLAMIC_DE_FRANCE:
				return 12;
				
			default:
				return -1;
				
		}
	}
	
	public static double getIshaAngleDegrees(int method) {
		
		switch (method) {
			
			case METHOD_ISLAMIC_SOCIETY_OF_NORTH_AMERICA:
			case METHOD_SPIRITUAL_ADMINISTRATION_OF_MUSLIMS_OF_RUSSIA:
			case METHOD_INSTITUTE_OF_GEOPHYSICS_TEHRAN:
				return 15;
				
			case METHOD_MUSLIM_WORLD_LEAGUE:
			case METHOD_MAJLIS_UGAMA_ISLAM_SINGAPURA_SINGAPORE:
			case METHOD_DIYANET_ISLERI_BASKANLIGI_TURKEY:
				return 17;
				
			case METHOD_EGYPTIAN_GENERAL_AUTHORITY_OF_SURVEY:
				return 17.5;
				
			case METHOD_KUWAIT:
				return 18;
				
			case METHOD_UNION_ORGANIZATION_ISLAMIC_DE_FRANCE:
				return 12;
				
			default:
				return -1;
				
		}
	}
	
	public static double getIshaFixedOffset(int method, boolean isRamadan) {
		
		switch (method) {
			case METHOD_GULF_REGION:
			case METHOD_QATAR:
				return 90;
				
			case METHOD_UMM_AL_QURA_UNIVERSITY_MAKKAH:
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
	
	public static boolean getHasFixedIshaOffset(int method) {
		
		switch (method) {
			
			case METHOD_GULF_REGION:
			case METHOD_QATAR:
			case METHOD_UMM_AL_QURA_UNIVERSITY_MAKKAH:
				return true;
			
			default:
				return false;
		}
	}
	
	public static double getFajr(int method, double daysSinceEpoch, int timeZone, double longitude,
			double latitude) {
		
		double noon = AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		double angleDegrees = getFajrAngleDegrees(method);
		double angle = Math.toRadians(angleDegrees);
		
		double offset = AstronomyMath.noonOffsetFromSunAngle(angle, daysSinceEpoch, latitude);
		
		return noon - offset / 60;
	}
	
	public static double getDuhr(double daysSinceEpoch, int timeZone, double longitude) {
		return AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
	}
	
	public static double getAsr(double daysSinceEpoch, int timeZone, double longitude,
			double latitude, boolean useShafaiMethod) {
		
		double noon = AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		double shadowLength = useShafaiMethod? 2 : 1;
		double offset = AstronomyMath.noonOffsetFromShadowLength(shadowLength, daysSinceEpoch, latitude);
		
		return noon + offset / 60;
	}
	
	public static double getMagrib(double daysSinceEpoch, int timeZone, double longitude, double latitude) {
		double noon = AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		double offset = AstronomyMath.noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
		
		return noon + offset / 60;
	}
	
	public static double getIsha(int method, double daysSinceEpoch, int timeZone, double longitude,
			double latitude, boolean isRamadan) {
		
		double noon = AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		if (getHasFixedIshaOffset(method)) {
			double fixedTimeOffset = getIshaFixedOffset(method, isRamadan);
			double magribOffset = AstronomyMath.noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
			
			return noon + (magribOffset + fixedTimeOffset) / 60;
		}
		else {
			double angleDegrees = getIshaAngleDegrees(method);
			double angle = Math.toRadians(angleDegrees);
			
			double offset = AstronomyMath.noonOffsetFromSunAngle(angle, daysSinceEpoch, latitude);
			
			return noon + offset / 60;
		}
	}
	
	public static double[] getTimings(int method, double daysSinceEpoch, int timeZone,
			double longitude, double latitude, boolean isRamadan, boolean useShafaiMethod) {
		
		double[] timings = new double[5];
		
		double noon = AstronomyMath.localSolarNoon(daysSinceEpoch, timeZone, longitude);
		
		// Duhr timing
		timings[1] = noon;
		
		// Fajr timing
		{
			double angleDegrees = getFajrAngleDegrees(method);
			double angle = Math.toRadians(angleDegrees);
			
			double offset = AstronomyMath.noonOffsetFromSunAngle(angle, daysSinceEpoch, latitude);
			
			timings[0] = noon - offset / 60;
		}
		
		// Asr timing
		{
			double shadowLength = useShafaiMethod? 2 : 1;
			double offset = AstronomyMath.noonOffsetFromShadowLength(shadowLength, daysSinceEpoch, latitude);
			
			timings[2] = noon + offset / 60;
		}
		
		// Magrib timing
		{
			double offset = AstronomyMath.noonOffsetFromSunAngle(0.833, daysSinceEpoch, latitude);
			
			timings[3] = noon + offset / 60;
		}
		
		// Isha timing
		{
			if (getHasFixedIshaOffset(method)) {
				double fixedTimeOffset = getIshaFixedOffset(method, isRamadan);
				
				timings[4] = timings[3] + fixedTimeOffset / 60;
			}
			else {
				double angleDegrees = getIshaAngleDegrees(method);
				double angle = Math.toRadians(angleDegrees);
				
				double offset = AstronomyMath.noonOffsetFromSunAngle(angle, daysSinceEpoch, latitude);
				
				timings[4] = noon + offset / 60;
			}
		}
		
		return timings;
	}
	
	public static double toDaysSinceEpoch2000(ZonedDateTime date) {
		return AstronomyMath.daysSinceEpoch(
				date.getYear(),
				date.getMonthValue(),
				date.getDayOfMonth(),
				date.getHour(),
				date.getMinute(),
				date.getOffset().getTotalSeconds() / 60 / 60
		);
	}
	
	public static double toDaysSinceEpoch2000() {
		return toDaysSinceEpoch2000(ZonedDateTime.now());
	}
}
