package janus.application.description;

import janus.Janus;
import janus.application.description.DBDescrTreeNode.Type;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

class DBDescrTreeBuilder {
	static TreeNode buildHierarchy(String catalog, String table, String column) {
		// root node: to be visible
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DBDescrTreeNode("'" + catalog + "'.'" + table + "'.'" + column + "'", Type.COLUMN));
		
		// Data Type
		root.add(getDataTypeSubtree(table, column));
		
		// Not Null
		root.add(getNotNullSubtree());
		
		// Auto Increment
		root.add(getAutoIncrementSubtree());
		
		// Default Value
		root.add(getDefaultValueSubtree(table, column));
		
		return root;
	}
	
	private static MutableTreeNode getDefaultValueSubtree(String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Default Value", Type.LABEL));
		
		node.add(new DefaultMutableTreeNode(new DBDescrTreeNode(Janus.localDBSchema.getDefaultValue(table, column), Type.DEFAULT_VALUE)));
		
		return node;
	}
	
	private static MutableTreeNode getDataTypeSubtree(String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Data Type", Type.LABEL));
		
		String type = Janus.localDBSchema.getColumnType(table, column);
		
		node.add(new DefaultMutableTreeNode(new DBDescrTreeNode(type, Type.DATA_TYPE)));
		
		return node;
	}
	
	private static MutableTreeNode getNotNullSubtree() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Not Null", Type.NOT_NULL));
		
		return node;
	}
	
	private static MutableTreeNode getAutoIncrementSubtree() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Auto Increment", Type.AUTO_INC));
		
		return node;
	}
}