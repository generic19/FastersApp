package com.basilalasadi.fasters.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


enum Country {
	UnitedStates("United States"),
	Canada("Canada"),
	Mexico("Mexico"),
	Oman("Oman"),
	Bahrain("Bahrain"),
	UnitedArabEmirates("United Arab Emirates"),
	SaudiArabia("Saudi Arabia"),
	Egypt("Egypt"),
	Iran("Iran"),
	Kuwait("Kuwait"),
	Qatar("Qatar"),
	Singapore("Singapore"),
	France("France"),
	Turkey("Turkey"),
	Russia("Russia");
	
	private final String name;
	
	Country(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	
	private static final Map<String, Country> byName;
	
	static {
		HashMap<String, Country> byNameMutable = new HashMap<>();
		
		byNameMutable.put("United States", UnitedStates);
		byNameMutable.put("Canada", Canada);
		byNameMutable.put("Mexico", Mexico);
		byNameMutable.put("Oman", Oman);
		byNameMutable.put("Bahrain", Bahrain);
		byNameMutable.put("United Arab Emirates", UnitedArabEmirates);
		byNameMutable.put("Saudi Arabia", SaudiArabia);
		byNameMutable.put("Egypt", Egypt);
		byNameMutable.put("Iran", Iran);
		byNameMutable.put("Kuwait", Kuwait);
		byNameMutable.put("Qatar", Qatar);
		byNameMutable.put("Singapore", Singapore);
		byNameMutable.put("France", France);
		byNameMutable.put("Turkey", Turkey);
		byNameMutable.put("Russia", Russia);
		
		byName = Collections.unmodifiableMap(byNameMutable);
	}
	
	public static Country find(String name) {
		return byName.containsKey(name) ? byName.get(name) : null;
	}
}
