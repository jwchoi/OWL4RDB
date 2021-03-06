package janus.mapping.metadata.owl;

import janus.Janus;
import janus.database.DBField;
import janus.mapping.OntEntityTypes;
import janus.mapping.OntMapper;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class IndividualMetadata {
	
	// generates an individual with ordered column names.
	static String getMappedRecordIndividualFragment(String table, List<DBField> pkFields) {
		String rootTable = Janus.localDBSchema.getRootTable(table);
		
		List<String> pkOfRootTable = Janus.localDBSchema.getPrimaryKey(rootTable);
		
		List<DBField> pkRootFields = new Vector<DBField>();
		
		for (String pkColumn: pkOfRootTable) {
			String matchedColumn = Janus.localDBSchema.getMatchedPKColumnAmongFamilyTables(rootTable, pkColumn, table);
			
			for (DBField field: pkFields)
				if (field.getColumnName().equals(matchedColumn)) {
					pkRootFields.add(new DBField(rootTable, pkColumn, field.getValue()));
					break;
				}
		} 
		
		String individualFragment = OntMapper.TABLE_NAME + OntMapper.IS + rootTable;
		
		for (DBField pkField: pkRootFields) {
			String column = pkField.getColumnName();
			String value = pkField.getValue();
			
			individualFragment = individualFragment 
								 + OntMapper.AND + OntMapper.PK_COLUMN_NAME + OntMapper.IS + column 
								 + OntMapper.AND + OntMapper.VALUE + OntMapper.IS + value;
		} 
		
		return individualFragment;
	}
	
	static URI getMappedRecordIndividual(String table, List<DBField> pkFields) {
		String fragment = getMappedRecordIndividualFragment(table, pkFields);
		
		try {
			return URI.create(getIndividualStringOfFragment(fragment));
		} catch (IllegalArgumentException e) {
			return getEncodedIndividual(fragment);
		}
	}
	
	static URI getMappedFieldIndividual(String table, String column, String value) {
		String fragment = getMappedFieldIndividualFragment(table, column, value);
		
		try {
			return URI.create(getIndividualStringOfFragment(fragment));
		} catch (IllegalArgumentException e) {
			return getEncodedIndividual(fragment);
		}
	}
	
	static String getMappedFieldIndividualFragment(String table, String column, String value) {
		String rootColumn = Janus.localDBSchema.getRootTableDotColumn(table, column);

		String[] tableDotColumn = rootColumn.split("\\.");
		String rootTableName = tableDotColumn[0];
		String rootColumnName = tableDotColumn[1]; 
		
		String individualFragment = OntMapper.TABLE_NAME + OntMapper.IS + rootTableName 
									+ OntMapper.AND + OntMapper.COLUMN_NAME + OntMapper.IS + rootColumnName 
									+ OntMapper.AND + OntMapper.VALUE + OntMapper.IS + value;
		
		return individualFragment;
	}
	
	static String getMappedTableNameToRecordIndividual(URI individual) {
		String fragment = individual.getFragment();
		
		String[] tokens = fragment.split("&");
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME)) {
				return pair[1];
			}
		}
		
		return null;
	}
	
	static List<DBField> getMappedDBFieldsToRecordIndividual(URI individual) {
		List<DBField> fields = new Vector<DBField>();
		
		String tableName = null;
		List<String> columnNames = new Vector<String>();
		List<String> values = new Vector<String>();
		
		String fragment = getDecodedIndividualFragment(individual.getRawFragment());
		
		String[] tokens = fragment.split("&");
		
		try {
			if (tokens.length < 3 || ((tokens.length % 2) != 1))
				throw new IndividualParsingException();
		} catch (IndividualParsingException e) {
				e.printStackTrace();
		}
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME))
				tableName =  pair[1];
			if (pair[0].equals(OntMapper.PK_COLUMN_NAME))
				columnNames.add(pair[1]);
			if (pair[0].equals(OntMapper.VALUE))
				values.add(pair[1]);
		}
		
		for (int i = 0; i < (tokens.length)/2; i++)
			fields.add(new DBField(tableName, columnNames.get(i), values.get(i)));
		
		return fields;
	}
	
	static DBField getMappedDBFieldToFieldIndividual(URI individual) {
		String tableName = null;
		String columnName = null;
		String value = null;
		
		String fragment = getDecodedIndividualFragment(individual.getRawFragment());
		
		String[] tokens = fragment.split("&");
		
		try {
			if (tokens.length != 3)
				throw new IndividualParsingException();
		} catch (IndividualParsingException e) {
				e.printStackTrace();
		}
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME))
				tableName =  pair[1];
			if (pair[0].equals(OntMapper.COLUMN_NAME))
				columnName =  pair[1];
			if (pair[0].equals(OntMapper.VALUE))
				value = pair[1];
		}
		
		return new DBField(tableName, columnName, value);
	}
	
	static OntEntityTypes getIndividualType(URI individual) {
		if (Pattern.matches(OntEntityTypes.FIELD_INDIVIDUAL.pattern(), individual.toString()))
			return OntEntityTypes.FIELD_INDIVIDUAL;
		else if (Pattern.matches(OntEntityTypes.RECORD_INDIVIDUAL.pattern(), individual.toString()))
			return OntEntityTypes.RECORD_INDIVIDUAL;
		
		return null;
	}
	
	static URI getIndividual(String individualFragment) {
		try {
			return  new URI(getIndividualStringOfFragment(individualFragment));
		} catch (URISyntaxException e) {
			return getEncodedIndividual(individualFragment);
		}
	}
	
	private static URI getEncodedIndividual(String individualFragment) {
		return URI.create(getIndividualStringOfFragment(getEncodedIndividualFragment(individualFragment)));
		
	}
	
	private static String getIndividualStringOfFragment(String fragment) {
		return Janus.ontBridge.getOntologyID() + "#" + fragment;
	}
	
	private static String getEncodedIndividualFragment(String fragment) {
		try {
			return URLEncoder.encode(fragment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static String getDecodedIndividualFragment(String encodedFragment) {
		try {
			return URLDecoder.decode(encodedFragment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	static boolean isBeableIndividual(URI individual) {
		
		try {
			URI ontologyURI = new URI(individual.getScheme(), individual.getSchemeSpecificPart(), null);
			
			if (!ontologyURI.toString().equals(Janus.baseURI))
				return false;
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
		
		OntEntityTypes type = Janus.owlMappingMetadata.getIndividualType(individual);
		
		if (type.equals(OntEntityTypes.RECORD_INDIVIDUAL)) {
			
			String table = Janus.owlMappingMetadata.getMappedTableNameToRecordIndividual(individual);
			List<DBField> fields = Janus.owlMappingMetadata.getMappedDBFieldsToRecordIndividual(individual);
			
			if (Janus.localDBSchema.isRootTable(table)) {
				List<String> pks = Janus.localDBSchema.getPrimaryKey(table);

				for (DBField field: fields)
					pks.remove(field.getColumnName());
				
				if (!pks.isEmpty())
					return false;
				
			} else 
				return false;
			
		} else if (type.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			DBField field = Janus.owlMappingMetadata.getMappedDBFieldToFieldIndividual(individual);
			
			if (!Janus.localDBSchema.isRootColumn(field.getTableName(), field.getColumnName()))
				return false;
		} else
			return false;
		
		return true;
	}
}