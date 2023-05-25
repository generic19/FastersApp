package com.basilalasadi.fasters.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public enum TimingsMethod {
	IslamicSocietyOfNorthAmerica(15, 15),
	MuslimWorldLeague(18, 17),
	UmmAlQuraUniversityMakkah(18.5, 90, 120),
	EgyptianGeneralAuthorityOfSurvey(19.5, 17.5),
	InstituteOfGeophysicsTehran(17.7, 15),
	GulfRegion(19.5, 90, 90),
	Kuwait(18, 18),
	Qatar(19.5, 90, 90),
	MajlisUgamaIslamSingapuraSingapore(18, 17),
	UnionOrganizationIslamicDeFrance(12, 12),
	DiyanetIsleriBaskanligiTurkey(18, 17),
	SpiritualAdministrationOfMuslimsOfRussia(15, 15);
	
	private static final Map<Country, TimingsMethod> defaultMethods;
	
	private final double fajrAngle;
	private final double ishaAngle;
	private final double ishaFixedOffset;
	private final double ishaFixedOffsetForRamadan;
	
	TimingsMethod(double fajrAngle, double ishaAngle) {
		this.fajrAngle = fajrAngle;
		this.ishaAngle = ishaAngle;
		this.ishaFixedOffset = Double.NaN;
		this.ishaFixedOffsetForRamadan = Double.NaN;
	}
	
	TimingsMethod(double fajrAngle, double ishaFixedOffset, double ishaFixedOffsetForRamadan) {
		this.fajrAngle = fajrAngle;
		this.ishaAngle = Double.NaN;
		this.ishaFixedOffset = ishaFixedOffset;
		this.ishaFixedOffsetForRamadan = ishaFixedOffsetForRamadan;
	}
	
	/**
	 * <p>Get the sun angle at fajr.</p>
	 * @return sun angle in degrees.
	 */
	public double getFajrAngleDegrees() {
		return fajrAngle;
	}
	
	/**
	 * <p>Get the sun angle at isha.</p>
	 * @return sun angle in degrees.
	 */
	public double getIshaAngleDegrees() {
		return ishaAngle;
	}
	
	/**
	 * <p>Gets whether method uses fixed time offset from sunset for isha.</p>
	 * @return true if method uses fixed offset.
	 */
	public boolean usesFixedOffsetForIsha() {
		return Double.isNaN(ishaAngle);
	}
	
	/**
	 * <p>Gets fixed time offset from sunset for isha.</p>
	 * @param isRamadan is hijri month Ramadan.
	 * @return time offset in minutes.
	 */
	public double getIshaFixedOffset(boolean isRamadan) {
		return isRamadan ? ishaFixedOffsetForRamadan : ishaFixedOffset;
	}
	
	
	static {
		HashMap<Country, TimingsMethod> defaultMethodsMutable = new HashMap<>();
		
		for (Country country : new Country[]{
				Country.UnitedStates,
				Country.Canada,
				Country.Mexico}) {
			
			defaultMethodsMutable.put(country, TimingsMethod.IslamicSocietyOfNorthAmerica);
		}
		
		for (Country country : new Country[]{
				Country.Oman,
				Country.Bahrain,
				Country.UnitedArabEmirates}) {
			
			defaultMethodsMutable.put(country, TimingsMethod.GulfRegion);
		}
		
		defaultMethodsMutable.put(Country.SaudiArabia, TimingsMethod.UmmAlQuraUniversityMakkah);
		defaultMethodsMutable.put(Country.Egypt, TimingsMethod.EgyptianGeneralAuthorityOfSurvey);
		defaultMethodsMutable.put(Country.Iran, TimingsMethod.InstituteOfGeophysicsTehran);
		defaultMethodsMutable.put(Country.Kuwait, TimingsMethod.Kuwait);
		defaultMethodsMutable.put(Country.Qatar, TimingsMethod.Qatar);
		defaultMethodsMutable.put(Country.Singapore, TimingsMethod.MajlisUgamaIslamSingapuraSingapore);
		defaultMethodsMutable.put(Country.France, TimingsMethod.UnionOrganizationIslamicDeFrance);
		defaultMethodsMutable.put(Country.Turkey, TimingsMethod.DiyanetIsleriBaskanligiTurkey);
		defaultMethodsMutable.put(Country.Russia, TimingsMethod.SpiritualAdministrationOfMuslimsOfRussia);
		
		defaultMethods = Collections.unmodifiableMap(defaultMethodsMutable);
	}
	
	/**
	 * <p>Get default method (enum) for country.</p>
	 * @param countryName country name
	 * @return the default method (enum) for country
	 */
	public static TimingsMethod getDefaultForCountry(String countryName) {
		Country country = Country.find(countryName);
		
		return (country != null && defaultMethods.containsKey(country)) ?
				defaultMethods.get(country) :
				TimingsMethod.MuslimWorldLeague;
	}
}
