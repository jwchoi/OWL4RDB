package janus.query.rewriter;

import janus.Janus;
import janus.database.DBColumn;
import janus.database.DBField;
import janus.mapping.DatatypeMap;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;
import janus.mapping.OntMapper;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.TableModel;

class MariaDBSQLGenerator extends SQLGenerator {
	
	static SQLGenerator getInstance() {
		return new MariaDBSQLGenerator();
	}
	
	protected String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table, String columnName) {
		return "SELECT " + getConcatCallStatementToBuildFieldIndividual(table, aPK) + " AS " + columnName
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfTableClass(String table, String columnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		query.append(getConcatCallStatementToBuildRecordIndividual(table));
		
		query.append(" AS " + columnName);
		
		query.append(" FROM " + table);
		
		return query.toString();
	}
	
	protected String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table, String columnName) {
		return "SELECT DISTINCT " + getConcatCallStatementToBuildFieldIndividual(table, keyColumn) + " AS " + columnName
				+ " FROM " + table;
	}
	
	protected String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table, String columnName) {
		return "SELECT DISTINCT " + getConcatCallStatementToBuildFieldIndividual(table, keyColumn) + " AS " + columnName
				+ " FROM " + table
				+ " WHERE " + keyColumn + " IS NOT NULL";
	}
	
	public String getQueryToGetEmptyResultSet(int columnCount) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		for (int i = 0; i < columnCount-1; i++)
			query.append("'', ");
		
		query.append("''");
		
		query.append(" FROM DUAL WHERE FALSE");
		
		return query.toString();
	}
	
	public String getQueryToGetEmptyResultSet(List<String> columnNames) {
		int columnCount = columnNames.size();
		
		StringBuffer query = new StringBuffer("SELECT ");
		
		for (int i = 0; i < columnCount-1; i++)
			query.append("''" + " AS " + columnNames.get(i) + ", ");
		
		query.append("''" + " AS " + columnNames.get(columnCount-1));
		
		query.append(" FROM DUAL WHERE FALSE");
		
		return query.toString();
	}
	
	public String getQueryToGetEmptyResultSet(String columnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		query.append("''" + " AS " + columnName);
		
		query.append(" FROM DUAL WHERE FALSE");
		
		return query.toString();
	}
	
	public String getQueryToGetOneBooleanValueResultSet(boolean value) {
		return "SELECT " + value + "''";
	}
	
	private String getConcatCallStatementToBuildFieldIndividual(String table, String keyColumn) {
		DBColumn rootColumn = Janus.localDBSchema.getRootColumn(table, keyColumn);
		
		return "concat('" + OntMapper.COLON + OntMapper.TABLE_NAME + "=" + rootColumn.getTableName() + "&" + OntMapper.COLUMN_NAME + "="+ rootColumn.getColumnName() + "&" + OntMapper.VALUE + "=', " + table + "." + keyColumn + ")";
	}
	
	// generates an individual with ordered column names.
	private String getConcatCallStatementToBuildRecordIndividual(String table) {
		String rootTable = Janus.localDBSchema.getRootTable(table);
		
		StringBuffer concat = new StringBuffer("concat('" + OntMapper.COLON + OntMapper.TABLE_NAME + "=" + rootTable + "'");
		
		List<String> pkOfRootTable = Janus.localDBSchema.getPrimaryKey(rootTable);
		
		for (String column: pkOfRootTable) {
			String matchedColumn = Janus.localDBSchema.getMatchedPKColumnAmongFamilyTables(rootTable, column, table);
			concat.append(", '&" + OntMapper.PK_COLUMN_NAME + "=" + column + "&" + OntMapper.VALUE + "=', " + table + "." + matchedColumn);
		}
		
		concat.append(")");
		
		return concat.toString();
	}
	
	protected String getQueryToGetOPAssertionOfRecord(URI op, String opColumn, String table, List<DBField> PKFields, String pColumnName, String tColumnName) {
		String concatCallStatement = getConcatCallStatementToBuildFieldIndividual(table, opColumn);
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" + " AS " + pColumnName + ", " + concatCallStatement + " AS " + tColumnName
												+ " FROM " + table
												+ " WHERE ");
		
		for (int i = 0; i < PKFields.size() - 1; i++) {
			DBField field = PKFields.get(i);
			query.append(field.getTableName() + "." + field.getColumnName() + " = " + "'" + field.getValue() + "'");
			query.append(" AND ");
		}
		
		DBField lastField = PKFields.get(PKFields.size()-1);
		query.append(lastField.getTableName() + "." + lastField.getColumnName() + " = " + "'" + lastField.getValue() + "'");
		
		if (!Janus.localDBSchema.isNotNull(table, opColumn))
			query.append(" AND " + table + "." + opColumn + " IS NOT NULL");
		
		return query.toString();
	}
	
	private String getConcatCallStatementToBuildLiteral(String table, String dpColumn) {
		int sqlDataType = Janus.localDBSchema.getJDBCDataType(table, dpColumn);
		String xmlSchemaDataType = DatatypeMap.getMappedXSD(sqlDataType);
		
		return "concat('\"', " + table + "." + dpColumn + ", '\"', '^^', '" + xmlSchemaDataType + "')";
	}
	
	protected String getQueryToGetDPAssertionOfRecord(URI dp, String dpColumn, String table, List<DBField> PKFields, String pColumnName, String tColumnName) {
		StringBuffer query = new StringBuffer("SELECT '"  + OntEntity.getCURIE(dp) +"'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildLiteral(table, dpColumn) + " AS " + tColumnName
												+ " FROM " + table
												+ " WHERE ");
		
		for (int i = 0; i < PKFields.size() - 1; i++) {
			DBField field = PKFields.get(i);
			query.append(field.getTableName() + "." + field.getColumnName() + " = " + "'" + field.getValue() + "'");
			query.append(" AND ");
		}
		
		DBField lastField = PKFields.get(PKFields.size()-1);
		query.append(lastField.getTableName() + "." + lastField.getColumnName() + " = " + "'" + lastField.getValue() + "'");
		
		if (!Janus.localDBSchema.isNotNull(table, dpColumn))
			query.append(" AND " + table + "." + dpColumn + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionOfField(URI dp, String dpColumn, String table, String value, String pColumnName, String tColumnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.localDBSchema.isPrimaryKey(table, dpColumn)
				&& Janus.localDBSchema.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildLiteral(table, dpColumn) + " AS " + tColumnName 
				+ " FROM " + table
				+ " WHERE " + table + "." + dpColumn + " = '" + value + "'");
		
		return query.toString();
	}
	
	protected String getQueryToGetOPAssertionOfField(DBField field, String pColumnName, String sColumnName) {
		String table = field.getTableName();
		String column = field.getColumnName();
		String value = field.getValue();
		
		URI op = Janus.owlMappingMetadata.getMappedObjectProperty(table, column);

		return "SELECT " + "'" + OntEntity.getCURIE(op) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName
						+ " FROM " + table 
						+ " WHERE " + table + "." + column + " = " + "'" + value + "'";
	}
	
	protected String getQueryToGetDPAssertionsOfDPWithTableClassDomain(URI dp, String sColumnName, String tColumnName) {
		DBColumn mappedColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(dp);
		String table = mappedColumn.getTableName();
		String dpColumn = mappedColumn.getColumnName();
		
		StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildLiteral(table, dpColumn) + " AS " + tColumnName 
										   + " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, dpColumn))
			query.append(" WHERE " + table + "." + dpColumn + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionsOfDPWithColumnClassDomain(URI dp, String sColumnName, String tColumnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		DBColumn mappedColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(dp);
		String table = mappedColumn.getTableName();
		String column = mappedColumn.getColumnName();
		
		if (!(Janus.localDBSchema.isPrimaryKey(table, column)
				&& Janus.localDBSchema.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildLiteral(table, column) + " AS " + tColumnName
					 + " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	//object property assertions 
	public String getQueryToGetOPAsserionsOfOP(URI op, String sColumnName, String tColumnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		DBColumn mappedColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(op);
		String table = mappedColumn.getTableName();
		String column = mappedColumn.getColumnName();
		
		query.append(getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + tColumnName
				     + " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	//data property assertions
	protected String getQueryToGetDPAssertionsOfKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral, String pColumnName, String sColumnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		String table = dbColumn.getTableName();
		String column = dbColumn.getColumnName();
		
		if (!(Janus.localDBSchema.isPrimaryKey(table, column)
				&& Janus.localDBSchema.isPrimaryKeySingleColumn(table)))
			query.append("DISTINCT ");
		
		URI dp = Janus.owlMappingMetadata.getMappedDataProperty(table, column);
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + sColumnName
				 + " FROM " + table
				 + " WHERE " + dbColumn.toString() + " = " + "'" + lexicalValueOfLiteral + "'");
				
		return query.toString();
	}
	
	protected String getQueryToGetDPAssertionsOfNonKeyColumnLiteral(DBColumn dbColumn, String lexicalValueOfLiteral, String pColumnName, String sColumnName) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		String table = dbColumn.getTableName();
		String column = dbColumn.getColumnName();
		
		URI dp = Janus.owlMappingMetadata.getMappedDataProperty(table, column);
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName 
				 + " FROM " + table
				 + " WHERE " + dbColumn.toString() + " = " + "'" + lexicalValueOfLiteral + "'");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllClsAssertionsOfTableClass(String table, URI cls, String columnName1, String columnName2) {
		return "SELECT " + "'" + OntEntity.getCURIE(cls) + "'" + " AS " + columnName1 + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + columnName2
				        + " FROM " + table;
	}
	
	protected String getQueryToGetAllClsAssertionsOfColumnClass(String table, String column, URI cls, String columnName1, String columnName2) {
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.localDBSchema.isPrimaryKeySingleColumn(table)
				&& Janus.localDBSchema.isPrimaryKey(table, column)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(cls) + "'" + " AS " + columnName1 + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + columnName2
		        + " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllOPAssertions(String table, String column, String pColumnName, String sColumnName, String tColumnName) {
		URI op = Janus.owlMappingMetadata.getMappedObjectProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(op) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + tColumnName
												+ " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllDPAssertionsOfRecords(String table, String column, String pColumnName, String sColumnName, String tColumnName) {
		URI dp = Janus.owlMappingMetadata.getMappedDataProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT " + "'" + OntEntity.getCURIE(dp) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildLiteral(table, column) + " AS " + tColumnName
												+ " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	protected String getQueryToGetAllDPAssertionsOfFields(String table, String column, String pColumnName, String sColumnName, String tColumnName) {
		URI dp = Janus.owlMappingMetadata.getMappedDataProperty(table, column);
		
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (!(Janus.localDBSchema.isPrimaryKeySingleColumn(table)
				&& Janus.localDBSchema.isPrimaryKey(table, column)))
			query.append("DISTINCT ");
		
		query.append("'" + OntEntity.getCURIE(dp) + "'" + " AS " + pColumnName + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + sColumnName + ", " + getConcatCallStatementToBuildLiteral(table, column) + " AS " + tColumnName
		        + " FROM " + table);
		
		if (!Janus.localDBSchema.isNotNull(table, column))
			query.append(" WHERE " + table + "." + column + " IS NOT NULL");
		
		return query.toString();
	}
	
	public String getQueryToGetSourceIndividualsOfOPAssertion(URI op, URI aTargetIndividual, String columnName) {
		DBColumn opColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(op);
		
		String table = opColumn.getTableName();
		
		return "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + columnName
				+ " FROM " + table
				+ " WHERE " + opColumn.toString() + " = " + "'" + Janus.owlMappingMetadata.getMappedDBFieldToFieldIndividual(aTargetIndividual).getValue() + "'";
	}
	
	public String getQueryToGetTargetIndividualsOfOPAssertion(URI op, URI aSourceIndividual, String columnName) {
		DBColumn opColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(op);
		
		String table = opColumn.getTableName();
		String column = opColumn.getColumnName();
		
		StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + columnName
										   + " FROM " + table
										   + " WHERE ");
		
		List<DBField> srcFields = Janus.owlMappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
		
		for (int i = 0; i < srcFields.size()-1; i++) {
			String matchedColumn = Janus.localDBSchema.getMatchedPKColumnAmongFamilyTables(srcFields.get(i).getTableName(), srcFields.get(i).getColumnName(), table);
			
			query.append(table + "." + matchedColumn + " = " + "'" + srcFields.get(i).getValue() + "'" + " AND ");
		}
		
		DBField lastSrcField = srcFields.get(srcFields.size()-1);
		String matchedColumn = Janus.localDBSchema.getMatchedPKColumnAmongFamilyTables(lastSrcField.getTableName(), lastSrcField.getColumnName(), table);
		query.append(table + "." + matchedColumn + " = " + "'" + lastSrcField.getValue() + "'");
		
		return query.toString();
	}
	
	public String getQueryToGetTargetLiteralsOfDPAssertion(URI dp, URI aSourceIndividual, String columnName) {
		DBColumn dpColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(dp);
		String table = dpColumn.getTableName();
		String column = dpColumn.getColumnName();
		
		OntEntityTypes srcIndividualType = Janus.owlMappingMetadata.getIndividualType(aSourceIndividual);
		
		if (srcIndividualType.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			
			String srcTable = Janus.owlMappingMetadata.getMappedTableNameToRecordIndividual(aSourceIndividual);
			
			Set<String> familyTablesOfSrc = Janus.localDBSchema.getFamilyTables(srcTable);
			
			if (!familyTablesOfSrc.contains(table))
				return getQueryToGetEmptyResultSet(1);
			
			StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildLiteral(table, column) + " AS " + columnName + 
												 " FROM " + table +
												 " WHERE ");
			
			List<DBField> srcFields = Janus.owlMappingMetadata.getMappedDBFieldsToRecordIndividual(aSourceIndividual);
			
			for (DBField field: srcFields) {
				String matchedColumn = Janus.localDBSchema.getMatchedPKColumnAmongFamilyTables(srcTable, field.getColumnName(), table);
				
				query.append(table + "." + matchedColumn + " = " + "'" + field.getValue() + "'" + " AND ");
			}
			
			query.delete(query.lastIndexOf(" AND "), query.length());
			
			return query.toString();
			
		} else if (srcIndividualType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			
			DBField srcField = Janus.owlMappingMetadata.getMappedDBFieldToFieldIndividual(aSourceIndividual);
			String srcTable = srcField.getTableName();
			String srcColumn = srcField.getColumnName();
			
			Set<DBColumn> familyColumnsOfSrc = Janus.localDBSchema.getFamilyColumns(srcTable, srcColumn);
			
			if (!familyColumnsOfSrc.contains(dpColumn))
				return getQueryToGetEmptyResultSet(1);
			
			StringBuffer query = new StringBuffer("SELECT ");
			
			if (!(Janus.localDBSchema.isSingleColumnUniqueKey(table, column)
					|| ((Janus.localDBSchema.isPrimaryKey(table, column)
							&& Janus.localDBSchema.isPrimaryKeySingleColumn(table)))))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildLiteral(table, column) + " AS " + columnName +
					" FROM " + table +
					" WHERE " + table + "." + column + " = " + "'" + srcField.getValue() + "'");
			
			return query.toString();
			
		} else
			return getQueryToGetEmptyResultSet(columnName);
	}
	
	public String getQueryToGetSourceIndividualsOfDPAssertion(URI dp, String aTargetLiteral, String columnName) {
		DBColumn dpColumn = Janus.owlMappingMetadata.getMappedColumnToProperty(dp);
		String table = dpColumn.getTableName();
		String column = dpColumn.getColumnName();
		
		String datatypeOfTargetLiteral = Janus.owlMappingMetadata.getDatatypeOfTypedLiteral(aTargetLiteral);
		
		Set<Integer> mappedSQLTypes = DatatypeMap.getMappedJDBCTypes(datatypeOfTargetLiteral);
		
		String valueOfTargetLiteral = Janus.owlMappingMetadata.getLexicalValueOfTypedLiteral(aTargetLiteral);
		
		if (!mappedSQLTypes.contains(Janus.localDBSchema.getJDBCDataType(table, column)))
			return getQueryToGetEmptyResultSet(1);
		
		StringBuffer query = new StringBuffer("SELECT ");
		
		if (Janus.localDBSchema.isKey(table, column)) {
			
			if (!(Janus.localDBSchema.isSingleColumnUniqueKey(table, column)
					|| ((Janus.localDBSchema.isPrimaryKey(table, column)
							&& Janus.localDBSchema.isPrimaryKeySingleColumn(table)))))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildFieldIndividual(table, column));
			
		} else {
			query.append(getConcatCallStatementToBuildRecordIndividual(table));
		}
		
		query.append(" AS " + columnName);
		
		query.append(" FROM " + table + 
					 " WHERE " + dpColumn.toString() + " = " + "'" + valueOfTargetLiteral + "'");
		
		return query.toString();
	}
	
	public String getQueryToGetAllPairsOfTheSameIndividuals(String variable1, String variable2) {
		List<String> queries = new Vector<String>();
		
		Set<String> rootTables = Janus.localDBSchema.getRootTables();
		
		for (String table: rootTables) {
			String query = "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + variable1 + ", " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + variable2 +
						  " FROM " + table;
			queries.add(query);
		}
		
		Set<DBColumn> rootColumns = Janus.localDBSchema.getRootColumns();
		
		for (DBColumn aDBColumn: rootColumns) {
			String table = aDBColumn.getTableName();
			String column = aDBColumn.getColumnName();
			
			StringBuffer query = new StringBuffer("SELECT ");
			
			if (!((Janus.localDBSchema.isPrimaryKey(table, column) && Janus.localDBSchema.isPrimaryKeySingleColumn(table))
					|| Janus.localDBSchema.isSingleColumnUniqueKey(table, column)))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + variable1 + ", " + getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + variable2 + 
					" FROM " + table);
			
			if (!Janus.localDBSchema.isPrimaryKey(table, column))
				query.append(" WHERE " + aDBColumn.toString() + " IS NOT NULL");
			
			queries.add(query.toString());
		}
		
		return getUnionQuery(queries, 2);
	}
	
	public String getQueryToGetAllPairsOfDiffIndividuals(String variable1, String variable2) {
		String table1 = getQueryToGetAllIndividuals(variable1);
		String table2 = getQueryToGetAllIndividuals(variable2);
		String alias1 = "t1"; // for table1
		String alias2 = "t2"; // for table2
		
		return "SELECT " + variable1 + ", " + variable2 +
			  " FROM " + "(" + table1 + ")" + " AS " + alias1 + ", " + "(" + table2 + ")" + " AS " + alias2 +
			  " WHERE " + variable1 + " <> " + variable2;
	}
	
	public String getQueryToGetDiffIndividualsFrom(URI individual, String variable) {
		List<String> queries = new Vector<String>();
		
		Set<String> rootTables = Janus.localDBSchema.getRootTables();
		
		Set<DBColumn> rootColumns = Janus.localDBSchema.getRootColumns();
		
		OntEntityTypes type = Janus.owlMappingMetadata.getIndividualType(individual);
		
		if (type.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
						
			for (DBColumn aDBColumn: rootColumns) {
				String table = aDBColumn.getTableName();
				String column = aDBColumn.getColumnName();
				
				StringBuffer query = new StringBuffer("SELECT ");
				
				if (!((Janus.localDBSchema.isPrimaryKey(table, column) && Janus.localDBSchema.isPrimaryKeySingleColumn(table))
						|| Janus.localDBSchema.isSingleColumnUniqueKey(table, column)))
					query.append("DISTINCT ");
				
				query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + variable + 
						" FROM " + table);
				
				if (!Janus.localDBSchema.isPrimaryKey(table, column))
					query.append(" WHERE " + aDBColumn.toString() + " IS NOT NULL");
				
				queries.add(query.toString());
			}
			
			List<DBField> mappedRecord = Janus.owlMappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
			String mappedTable = Janus.owlMappingMetadata.getMappedTableNameToRecordIndividual(individual);
			
			for (String table: rootTables) {
				
				StringBuffer query = new StringBuffer("SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + variable + 
													 " FROM " + table);
				
				if (table.equals(mappedTable)) {
					query.append(" WHERE ");
					for (DBField field: mappedRecord)
						query.append(field.getNotEqualExpression() + " AND ");
					
					query.delete(query.lastIndexOf(" AND "), query.length());
				}
				
				queries.add(query.toString());
			}
			
			
			
		} else if (type.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			
			for (String table: rootTables) {
				String query = "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + variable +
							  " FROM " + table;
				queries.add(query);
			}
			
			DBField mappedField = Janus.owlMappingMetadata.getMappedDBFieldToFieldIndividual(individual);
			
			for (DBColumn aDBColumn: rootColumns) {
				String table = aDBColumn.getTableName();
				String column = aDBColumn.getColumnName();
				
				StringBuffer query = new StringBuffer("SELECT ");
				
				boolean isPK = Janus.localDBSchema.isPrimaryKey(table, column);
				
				if (!((isPK && Janus.localDBSchema.isPrimaryKeySingleColumn(table))
						|| Janus.localDBSchema.isSingleColumnUniqueKey(table, column)))
					query.append("DISTINCT ");
				
				query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + variable + 
						" FROM " + table);
				
				if (!isPK)
					query.append(" WHERE " + aDBColumn.toString() + " IS NOT NULL");
				
				if (table.equals(mappedField.getTableName()) && column.equals(mappedField.getColumnName())) {
					
					if (!isPK)
						query.append(" AND ");
					else
						query.append(" WHERE ");
					
					query.append(mappedField.getNotEqualExpression());
				}
				
				queries.add(query.toString());
			}
			
		}
		
		return getUnionQuery(queries, 1);
	}
	
	protected String getQueryToGetAllIndividuals(String variable) {
		List<String> queries = new Vector<String>();
		
		Set<String> rootTables = Janus.localDBSchema.getRootTables();
		
		for (String table: rootTables) {
			String query = "SELECT " + getConcatCallStatementToBuildRecordIndividual(table) + " AS " + variable +
						  " FROM " + table;
			queries.add(query);
		}
		
		Set<DBColumn> rootColumns = Janus.localDBSchema.getRootColumns();
		
		for (DBColumn aDBColumn: rootColumns) {
			String table = aDBColumn.getTableName();
			String column = aDBColumn.getColumnName();
			
			StringBuffer query = new StringBuffer("SELECT ");
			
			if (!((Janus.localDBSchema.isPrimaryKey(table, column) && Janus.localDBSchema.isPrimaryKeySingleColumn(table))
					|| Janus.localDBSchema.isSingleColumnUniqueKey(table, column)))
				query.append("DISTINCT ");
			
			query.append(getConcatCallStatementToBuildFieldIndividual(table, column) + " AS " + variable + 
					" FROM " + table);
			
			if (!Janus.localDBSchema.isPrimaryKey(table, column))
				query.append(" WHERE " + aDBColumn.toString() + " IS NOT NULL");
			
			queries.add(query.toString());
		}
		
		return getUnionQuery(queries, 1);
	}
	
	public String getQueryCorrespondingToURIResultSet(TableModel aURIResultSet) {
		List<String> queries = new Vector<String>();
		
		int rowCount = aURIResultSet.getRowCount();
		int columnCount = aURIResultSet.getColumnCount();
		
		for (int i = 0; i < rowCount; i++) {
			StringBuffer query = new StringBuffer("SELECT ");
			for (int j = 0; j < columnCount; j++) {
				URI value = (URI)aURIResultSet.getValueAt(i, j);
				String curie = OntEntity.getCURIE(value);
				
				query.append("'" + curie + "'");
				
				if (i == 0)
					query.append(" AS " + aURIResultSet.getColumnName(j));
				
				if (j != columnCount - 1)
					query.append(", ");
			}
			queries.add(query.toString());
		}
		 
		return getUnionQuery(queries, columnCount);
	}
}