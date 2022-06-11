package com.nr.fit.instrumentation.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.newrelic.api.agent.DatastoreParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import static com.nr.fit.instrumentation.jdbc.DBUtils.DEFAULT_COLLECTION_AND_OPERATION;

@Weave(originalName="java.sql.Statement",type=MatchType.Interface)
public abstract class Statement_instrumentation {

	@NewField 
	private String databaseVendor = null;

	@Trace(leaf=true)
	public int[] executeBatch() throws SQLException  {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Database","Statement","ExecuteBatch");
		if(databaseVendor == null) {
			databaseVendor = DBUtils.getVendor(getConnection());
		}
		
		int hash = hashCode();
		List<String> batchQueries = DBUtils.getBatchQueries(hash);
		int[] result =  Weaver.callOriginal();
		int queriesExecuted = result.length; //preparedCount != null && preparedCount > 0 ? preparedCount : batchQueries.size();
		CollectionAndOperation collectionAndOperation = DEFAULT_COLLECTION_AND_OPERATION;
		if(!batchQueries.isEmpty()) {
			collectionAndOperation = DBUtils.parse(batchQueries.get(0));
		}
		DatastoreParameters params = DatastoreParameters
				.product(databaseVendor)
				.collection(collectionAndOperation.getCollection())
				.operation(collectionAndOperation.getOperation())
				.noInstance()
				.noDatabaseName()
				.slowQuery("", new BatchQueryConverter(queriesExecuted))
				.build();
		if(!batchQueries.isEmpty()) {
			TracedMethod traced = NewRelic.getAgent().getTracedMethod();
			int count = 1;
			for(String query : batchQueries) {
				String obFuscatedQuery = DBUtils.obfuscate(query);
				if(obFuscatedQuery != null) {
					traced.addCustomAttribute("Batch-Query-"+count,obFuscatedQuery );
				}
				count++;
			}
			DBUtils.clearBatchQueries(hash);
		}
		NewRelic.getAgent().getTracedMethod().reportAsExternal(params);
		if(DBUtils.isPrepared(this)) {
			String query = DBUtils.getPreparedQuery(hash);
			if(query != null && !query.isEmpty()) {
				DBUtils.addBatchQuery(hash, query);
			}
		}
		return result;
	}
	
	public void addBatch( String sql ) {
		DBUtils.addBatchQuery(hashCode(), sql);
		Weaver.callOriginal();
	}
	
	public void clearBatch() {
		DBUtils.clearBatchQueries(hashCode());
		Weaver.callOriginal();
	}
	
	public void close() {
		DBUtils.clearBatchQueries(hashCode());
		DBUtils.removePreparedQuery(hashCode());
		
		Weaver.callOriginal();
	}
	
	public abstract Connection getConnection() throws SQLException;
	

}
