package janus.database;

import janus.Janus;

import java.util.Optional;
import java.util.Set;

class ColumnMetadata implements Comparable<ColumnMetadata> {
	private String columnName;

	private int JDBCDataType; //java.sql.Types.something

    private String columnType; // varchar(50)

	private Optional<Integer> characterMaximumLength; // if the column is mapped by xsd:string, else null

	private Optional<Integer> maximumOctetLength; // # of bytes // if the column is mapped by xsd:hexBinary, else null

	private Optional<String> maximumIntegerValue; // if the column is mapped by xsd:integer, else null
	private Optional<String> minimumIntegerValue; // if the column is mapped by xsd:integer, else null

	private Optional<Integer> numericPrecision; // if the column is mapped by xsd:decimal, else null
	private Optional<Integer> numericScale; // if the column is mapped by xsd:decimal, else null

	private Optional<Boolean> isUnsigned;

	private Optional<Set<String>> valueSet; // if the column is equivalent to enum, else null

    private String defaultValue;

	private String superColumn;
	
	private boolean isNotNull;
	private boolean isPrimaryKey;
	private boolean isForeignKey;
	private boolean isUniqueKey;
	private boolean isSingleColumnUniqueKey;

    private boolean isAutoIncrement;
	
	ColumnMetadata(String columnName) {
		this.columnName = columnName;
	}

	Optional<Set<String>> getValueSet(String catalog, String table) {
		if (valueSet == null)
			valueSet = Janus.dbBridge.getValueSet(catalog, table, columnName);

		return valueSet;
	}

	boolean isUnsigned(String catalog, String table) {
		if (isUnsigned == null)
			isUnsigned = Optional.of(Janus.dbBridge.isUnsigned(catalog, table, columnName));

		return isUnsigned.get();
	}

	Optional<Integer> getNumericScale(String catalog, String table) {
		if (numericScale == null)
			numericScale = Janus.dbBridge.getNumericScale(catalog, table, columnName);

		return numericScale;
	}

	Optional<Integer> getNumericPrecision(String catalog, String table) {
		if (numericPrecision == null)
			numericPrecision = Janus.dbBridge.getNumericPrecision(catalog, table, columnName);

		return numericPrecision;
	}

	Optional<Integer> getCharacterMaximumLength(String catalog, String table) {
		if (characterMaximumLength == null)
			characterMaximumLength = Janus.dbBridge.getCharacterMaximumLength(catalog, table, columnName);

		return characterMaximumLength;
	}

	Optional<Integer> getMaximumOctetLength(String catalog, String table) {
		if (maximumOctetLength == null)
			maximumOctetLength = Janus.dbBridge.getCharacterOctetLength(catalog, table, columnName);

		return maximumOctetLength;
	}

	Optional<String> getMaximumIntegerValue(String catalog, String table) {
		if (maximumIntegerValue == null)
			maximumIntegerValue = Janus.dbBridge.getMaximumIntegerValue(catalog, table, columnName);

		return maximumIntegerValue;
	}

	Optional<String> getMinimumIntegerValue(String catalog, String table) {
		if (minimumIntegerValue == null)
			minimumIntegerValue = Janus.dbBridge.getMinimumIntegerValue(catalog, table, columnName);

		return minimumIntegerValue;
	}

    boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }
	
	String getSuperColumn() {
		return superColumn;
	}
	
	int getJDBCDataType() {
		return JDBCDataType;
	}
	
	String getColumnName() {
		return columnName;
	}
	
	boolean isSingleColumnUniqueKey() {
		return isSingleColumnUniqueKey;
	}

	void setSingleColumnUniqueKey(boolean isSingleColumnUniqueKey) {
		this.isSingleColumnUniqueKey = isSingleColumnUniqueKey;
	}

	boolean isUniqueKey() {
		return isUniqueKey;
	}

	void setUniqueKey(boolean isUniqueKey) {
		this.isUniqueKey = isUniqueKey;
	}

	boolean isForeignKey() {
		return isForeignKey;
	}

	void setForeignKey(boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}

	boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	boolean isNotNull() {
		return isNotNull;
	}

	void setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}

	void setSuperColumn(String superColumn) {
		this.superColumn = superColumn;
	}
	
	void setJDBCDataType(int JDBCDataType) {
		this.JDBCDataType = JDBCDataType;
	}
	
	boolean isKey() {
		if (isPrimaryKey || isForeignKey || isUniqueKey)
			return true;
		
		return false;
	}

    String getColumnType() {
        return columnType;
    }

    void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    String getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

	@Override
	public int compareTo(ColumnMetadata o) {
		return columnName.compareTo(o.getColumnName());
	}
}
