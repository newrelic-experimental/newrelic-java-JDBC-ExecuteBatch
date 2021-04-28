package com.nr.fit.instrumentation.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;

public class DBUtils {

	private static HashMap<Integer, List<String>> batchQueries = new HashMap<Integer, List<String>>();
	private static HashMap<Integer, String> preparedQueries = new HashMap<Integer, String>();
	
	static {
		// Run checks to make sure caches are not growing
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Check(), 1, 1, TimeUnit.MINUTES);
	}
	
	public static void addPreparedQuery(Integer hash, String query) {
		if(!preparedQueries.containsKey(hash)) {
			preparedQueries.put(hash, query);
		}
	}
	
	public static String getPreparedQuery(Integer hash) {
		return preparedQueries.get(hash);
	}
	
	public static void removePreparedQuery(Integer hash) {
		preparedQueries.remove(hash);
	}
	
	public static void addBatchQuery(Integer hash, String query) {
		List<String> queries = null;
		if(batchQueries.containsKey(hash)) {
			queries = batchQueries.get(hash);
		} else {
			queries = new ArrayList<String>();
		}
		if(!queries.contains(query)) {
			queries.add(query);
		}
		batchQueries.put(hash, queries);
	}
	
	public static List<String>  getBatchQueries(Integer hash) {
		return batchQueries.get(hash);
	}
	
	public static void clearBatchQueries(Integer hash) {
		batchQueries.remove(hash);
	}
	
	public static String obfuscate(String sql) {
		ObfuscationService obfuscationService = ObfuscationService.getInstance();
		if(!obfuscationService.isInitialized()) {
			obfuscationService.initialize();
		}
		String obfuscated = obfuscationService.obfuscate(sql);
		return obfuscated;
	}

	public static String getVendor(Connection connection) {

		String dbVendor = null;
		DatabaseMetaData metadata = null;

		try {
			if(connection != null) {
				metadata = connection.getMetaData();
			}

			dbVendor = metadata != null ? metadata.getDatabaseProductName() : "Unknown";
		} catch (SQLException e) {
		}

		return dbVendor != null ? dbVendor : "Unknown";

	}
	
	private static class Check implements Runnable {

		@Override
		public void run() {
			NewRelic.recordMetric("Custom/JDBC-Execute/BatchQueries/Size", batchQueries.size());
			NewRelic.recordMetric("Custom/JDBC-Execute/PreparedQueries/Size", preparedQueries.size());
		}
		
	}

}
