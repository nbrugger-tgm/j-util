package com.niton.util.db;

public interface DatabaseTester<C>{
	public void testConnection(C connection);

	boolean isReadyForUse(C db);

	void testSchemaExistence(C db);
}
