package com.nr.fit.instrumentation.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtils {

	public static final CollectionAndOperation DEFAULT_COLLECTION_AND_OPERATION =
			new CollectionAndOperation("Batch", "execute");
	public static final Pattern PATTERN = Pattern.compile(
			"(?:(?:(SELECT|INSERT|DELETE)\\s+.*(?:FROM|INTO)))\\s+([A-Z][A-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

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

	public static boolean isPrepared(Object obj) {
		return obj instanceof PreparedStatement;
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

		if ("PostgreSQL".equals(dbVendor)) {
			return "Postgres";
		}
		return dbVendor != null ? dbVendor : "Unknown";

	}

	public static CollectionAndOperation parse(String statement) {
		if (statement.startsWith("/*")) {
			int index = statement.indexOf("*/");
			if (index != -1) {
				return new CollectionAndOperation(statement.substring(2, index).trim(), "batch");
			}
		}
		Matcher matcher = PATTERN.matcher(emptyParentheticals(statement)).useAnchoringBounds(false);
		if (matcher.find(0)) {
			return new CollectionAndOperation(matcher.group(2), matcher.group(1).toLowerCase());
		}

		return DEFAULT_COLLECTION_AND_OPERATION;
	}

	/**
	 * Remove the contents of all parentheticals - counting parens and ignoring nested parens
	 */
	public static String emptyParentheticals(String statement) {
		StringBuilder sb = new StringBuilder();
		int inParens = 0;
		for (char ch : statement.toCharArray()) {
			if (inParens == 0) {
				sb.append(ch);
			}
			if (ch == '(') {
				inParens++;
			} else if (ch == ')') {
				inParens--;
				if (inParens == 0) {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	private static class Check implements Runnable {

		@Override
		public void run() {
			NewRelic.recordMetric("Custom/JDBC-Execute/BatchQueries/Size", batchQueries.size());
			NewRelic.recordMetric("Custom/JDBC-Execute/PreparedQueries/Size", preparedQueries.size());
		}
		
	}

}
