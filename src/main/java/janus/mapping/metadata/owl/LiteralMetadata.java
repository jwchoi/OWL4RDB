package janus.mapping.metadata.owl;

import janus.Janus;
import janus.mapping.DatatypeMap;

class LiteralMetadata {
	static String getMappedTypedLiteral(String table, String column, String value) {
		int sqlDataType = Janus.localDBSchema.getJDBCDataType(table, column);
		String xmlSchemaDataType = DatatypeMap.getMappedXSD(sqlDataType);
		
		if (value.contains("\""))
			value = value.replaceAll("\"", "&quot;");
		
		String literal = "\"" + value + "\"^^" + xmlSchemaDataType;
		
		return literal;
	}
	
	static String getDatatype(String typedLiteral) {
		return typedLiteral.substring(typedLiteral.lastIndexOf("^^")+2);
	}
	
	static String getLexicalValue(String typedLiteral) {
		return typedLiteral.substring(typedLiteral.indexOf("\"")+1, typedLiteral.lastIndexOf("\"^^" + getDatatype(typedLiteral)));
	}
}
