package com.niton.util.db;

public interface DatabaseCloser<C> {
	public void close(C db);
}
