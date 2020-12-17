package com.niton.util.db;

import java.sql.SQLException;

/**
 * Interface responsible to build a connection
 * @param <C> the type of the connection
 */
public interface Connector<C> {
	public C connect(String host,String user,String password) throws SQLException;
}
