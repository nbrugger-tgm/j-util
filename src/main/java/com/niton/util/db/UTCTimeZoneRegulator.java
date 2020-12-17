package com.niton.util.db;

import com.niton.util.Logging;

import java.util.TimeZone;

public class UTCTimeZoneRegulator<C> implements TimeZoneAdaptation<C> {
	@Override
	public void adaptTimeZone(C db) {
		TimeZone timeZoneDefault = TimeZone.getDefault();
		Logging.log("Java time zone : " + timeZoneDefault.getID());
		if (!timeZoneDefault.toString().contains("UTC")) {
			Logging.log(Logging.Level.WARNING, "Time zone is not UTC");
			System.setProperty("user.timezone", "UTC");
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			timeZoneDefault = TimeZone.getDefault();
			Logging.log(Logging.Level.WARNING, "Changed time zone to UTC");
			Logging.log(Logging.Level.WARNING, "New Java time zone : " + timeZoneDefault.getID());
		}
	}
}
