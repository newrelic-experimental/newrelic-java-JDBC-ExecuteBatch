package com.nr.fit.instrumentation.jdbc;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName="java.sql.PreparedStatement",type=MatchType.Interface)
public abstract class PreparedStatement_instrumentation {

	/**
	 * Handle case where PreparedStatment is reused.
	 */
	public void clearParameters() {
		Integer hash = hashCode();
		String query = DBUtils.getPreparedQuery(hash);
		if(query != null && !query.isEmpty()) {
			DBUtils.addBatchQuery(hash, query);
		}
		Weaver.callOriginal();
	}
}
