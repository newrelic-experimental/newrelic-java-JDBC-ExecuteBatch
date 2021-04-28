package com.nr.fit.instrumentation.jdbc;

import com.newrelic.api.agent.QueryConverter;

public class BatchQueryConverter implements QueryConverter<String> {
	
	private int numberQueries = 0;
	
	public BatchQueryConverter(int n) {
		numberQueries = n;
	}

	@Override
	public String toRawQueryString(String rawQuery) {
		return "Batch Execute "+numberQueries+" Queries";
	}

	@Override
	public String toObfuscatedQueryString(String rawQuery) {
		return "Batch Execute "+numberQueries+" Queries";
	}

}
