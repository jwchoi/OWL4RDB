package janus.database;

import janus.mapping.DatatypeMap;

import javax.xml.bind.DatatypeConverter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class SQLResultSet {
	ResultSet rs;
	ResultSetMetaData rsmd;
	
	SQLResultSet(ResultSet rs, ResultSetMetaData rsmd) {
		this.rs = rs;
		this.rsmd = rsmd;
	}
	
	public int getResultSetColumnCount() {
		int cnt = 0;
		
		try {
			cnt = rsmd.getColumnCount();
		} catch(SQLException e) { e.printStackTrace(); }
		
		return cnt;
	}
	
	public String getResultSetColumnLabel(int column) {
		String label = null;
		
		try {
			label = rsmd.getColumnLabel(column);
		} catch(SQLException e) {e.printStackTrace(); }
		
		return label;
	}

	public List<String> getResultSetRowAt(int row) {
		int cnt = getResultSetColumnCount();

		List<String> v = new Vector<>(cnt);

		try {
			rs.absolute(row);

			for (int i = 1 ; i <= cnt; i++) {
				if (DatatypeMap.getMappedXSD(rsmd.getColumnType(i)).equals(DatatypeMap.XSD_HEX_BINARY))
					v.add(DatatypeConverter.printHexBinary(rs.getBytes(i)));
				else v.add(rs.getString(i));
			}
		} catch(SQLException e) { e.printStackTrace(); }

		return v;
	}
	
	public int getResultSetRowCount() {
		int cnt = 0;
		try {
			if(rs.last()) cnt = rs.getRow();
		} catch(SQLException e) { e.printStackTrace(); }
		
		return cnt;
	}
	
	public String getColumnName(int column) {
		String name = null;
		
		try {
			name = rsmd.getColumnName(column);
		} catch(SQLException e) { e.printStackTrace(); }
		
		return name;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getColumnTypeName(String columnLabel) {
		String columnTypeName = null;
		int columnCount = getResultSetColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			if (getResultSetColumnLabel(i).equals(columnLabel)) {
				try {
					columnTypeName = rsmd.getColumnTypeName(i);
					break;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return columnTypeName;
	}

	public Optional<Integer> getColumnType(String columnLabel) {
		Integer columnType = null;
		int columnCount = getResultSetColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			if (getResultSetColumnLabel(i).equals(columnLabel)) {
				try {
					columnType = rsmd.getColumnType(i);
					break;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return Optional.ofNullable(columnType);
	}

	public Optional<Integer> isNullable(String columnLabel) {
		Optional<Integer> nullable = Optional.empty();
		int columnCount = getResultSetColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			if (getResultSetColumnLabel(i).equals(columnLabel)) {
				try {
					nullable = Optional.of(rsmd.isNullable(i));
					break;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return nullable;
	}

	public String getTableName(String columnLabel) {
		String tableName = "";
		int columnCount = getResultSetColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			if (getResultSetColumnLabel(i).equals(columnLabel)) {
				try {
					return rsmd.getTableName(i);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return tableName;
	}
}
