package com.nr.fit.instrumentation.jdbc;

import java.util.regex.Pattern;

public class ObfuscationService {
	
	private SqlObfuscator sqlObfuscator = null;
	
	private static ObfuscationService instance = new ObfuscationService();
	
	protected static ObfuscationService getInstance() {
		return instance;
	}
	
 	private ObfuscationService()  {
 		sqlObfuscator = new SqlObfuscator();

	}

	public boolean isInitialized() {
		return sqlObfuscator != null;
	}

	public void initialize() {
		
	}

	protected String obfuscate(String sql) {
		if(sqlObfuscator != null) {
			return sqlObfuscator.obfuscateSql(sql);
		}
		return null;
	}
	
	
	private static class SqlObfuscator {

        private static final Pattern ALL_DIALECTS_PATTERN;
        private static final Pattern ALL_UNMATCHED_PATTERN;

        static {
            String allDialectsPattern = "'(?:[^']|'')*?(?:\\\\'.*|'(?!'))|\"(?:[^\"]|\"\")*?(?:\\\\\".*|\"(?!\"))|(\\$(?!\\d)[^$]*?\\$).*?(?:\\1|$)|q'\\[.*?(?:\\]'|$)|q'\\{.*?(?:\\}'|$)|q'<.*?(?:>'|$)|q'\\(.*?(?:\\)'|$)|(?:#|--).*?(?=\\r|\\n|$)|/\\*(?:[^/]|/[^*])*?(?:\\*/|/\\*.*)|\\{?(?:[0-9a-f]\\-*){32}\\}?|0x[0-9a-f]+|\\b(?:true|false|null)\\b|-?\\b(?:[0-9_]+\\.)?[0-9_]+([eE][+-]?[0-9_]+)?";
            

            ALL_DIALECTS_PATTERN = Pattern.compile(allDialectsPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            ALL_UNMATCHED_PATTERN = Pattern.compile("'|\"|/\\*|\\*/|\\$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        }
        
        public String obfuscateSql(String sql) {
            if (sql == null || sql.length() == 0) {
                return sql;
            }
            String obfuscatedSql = ALL_DIALECTS_PATTERN.matcher(sql).replaceAll("?");
            return checkForUnmatchedPairs(ALL_UNMATCHED_PATTERN, obfuscatedSql);
        }


        private String checkForUnmatchedPairs(Pattern pattern, String obfuscatedSql) {
            return pattern.matcher(obfuscatedSql).find() ? "?" : obfuscatedSql;
        }

	}
}
