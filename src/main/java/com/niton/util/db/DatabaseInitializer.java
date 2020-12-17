package com.niton.util.db;

public interface DatabaseInitializer<C> {
	public void addInitialData(C db);
}
