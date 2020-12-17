package com.niton.util.db;

import java.io.Reader;
import java.sql.Connection;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FullDatabase<C> extends
                                 Connector<C>,
                                 DatabaseCloser<C>,
                                 DatabaseInitializer<C>,
                                 DatabaseLinker<C>,
                                 DatabaseTester<C>,
                                 TimeZoneAdaptation<C>,
                                 Supplier<Reader>, Function<C, Connection> { }
