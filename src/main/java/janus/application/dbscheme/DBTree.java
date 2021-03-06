package janus.application.dbscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedClassAction;
import janus.application.actions.GoToMappedDataPropertyAction;
import janus.application.actions.GoToMappedObjectPropertyAction;
import janus.database.DBColumn;
import janus.database.DBEntityTypes;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class DBTree extends JScrollPane {
	private JTree tree;
	private JPopupMenu popupMenu;
	private AbstractAction goToMappedClass;
	private AbstractAction goToMappedObjectPropery;
	private AbstractAction goToMappedDataPropery;
	
	public DBTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getDBHierarchy());
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new DBTreeCellRenderer(new ImageIcon(ImageURIs.DB_CATALOG),
													  new ImageIcon(ImageURIs.DB_TABLE),
													  new ImageIcon(ImageURIs.DB_PRIMARY),
													  new ImageIcon(ImageURIs.DB_NON_PRIMARY)));
		
		tree.add(buildPopupMenu());
		
		setViewportView(tree);
	}
	
	private JPopupMenu buildPopupMenu() {
		popupMenu = new JPopupMenu();
		
		goToMappedClass = new GoToMappedClassAction();
		popupMenu.add(goToMappedClass);
		
		goToMappedDataPropery = new GoToMappedDataPropertyAction();
		popupMenu.add(goToMappedDataPropery);
		
		goToMappedObjectPropery = new GoToMappedObjectPropertyAction();
		popupMenu.add(goToMappedObjectPropery);
		
		return popupMenu;
	}
	
	private TreeNode getDBHierarchy() {
		// root node
		String catalog = Janus.localDBSchema.getCatalog();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DBTreeNode(catalog, DBEntityTypes.CATALOG));
		// table nodes
		Set<String> tables = Janus.localDBSchema.getTableNames();
		for(String table: tables) {	
			DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new DBTreeNode(table, DBEntityTypes.TABLE));
			root.add(tableNode);
			
			List<String> columns = Janus.localDBSchema.getColumns(table);
			
			// primary key column nodes
			List<String> pks = Janus.localDBSchema.getPrimaryKey(table);
			for(String pk: pks)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(pk, DBEntityTypes.PRIMARY)));
			
			columns.removeAll(pks);

			// non-key column nodes
			Set<String> nonKeys = Janus.localDBSchema.getNonKeyColumns(table);
			for(String nonKey : nonKeys)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(nonKey, DBEntityTypes.NON_KEY)));
			
			columns.removeAll(nonKeys);
			
			// key column (except primary) nodes
			for(String key: columns)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(key, DBEntityTypes.KEY)));
		}

		return root;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	void showPopupMenu(int x, int y) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DBTreeNode dbNode = (DBTreeNode)node.getUserObject();
		DBEntityTypes type = dbNode.getType();
		// setting mapped class menu enabled/disabled
		if (type.equals(DBEntityTypes.CATALOG) 
				|| type.equals(DBEntityTypes.NON_KEY))
			goToMappedClass.setEnabled(false);
		else
			goToMappedClass.setEnabled(true);
		// setting mapped object property menu enabled/disabled
		if (type.equals(DBEntityTypes.CATALOG) 
				|| type.equals(DBEntityTypes.TABLE)
				|| type.equals(DBEntityTypes.NON_KEY))
			goToMappedObjectPropery.setEnabled(false);
		else
			goToMappedObjectPropery.setEnabled(true);
		// setting mapped data property menu enabled/disabled
		if (type.equals(DBEntityTypes.CATALOG) 
				|| type.equals(DBEntityTypes.TABLE))
			goToMappedDataPropery.setEnabled(false);
		else
			goToMappedDataPropery.setEnabled(true);
		
		popupMenu.show(this, x, y);
	}
	
	public DBEntityTypes getTypeOfSelectedNode() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DBTreeNode dbNode = (DBTreeNode)node.getUserObject();

		return dbNode.getType();
	}
	
	TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}
	
	public void setSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}
	
	boolean isPathSelected(TreePath path) {
		return tree.isPathSelected(path);
	}
	
	boolean isSelectionEmpty() {
		return tree.isSelectionEmpty();
	}
	
	public String getSelectedTable() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DBTreeNode dbNode = (DBTreeNode)node.getUserObject();

		return dbNode.toString();
	}
	
	public DBColumn getSelectedColumn() {
		DefaultMutableTreeNode columnNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DefaultMutableTreeNode tableNode = (DefaultMutableTreeNode)columnNode.getParent();
		
		DBTreeNode dbColumnNode = (DBTreeNode)columnNode.getUserObject();
		DBTreeNode dbTableNode = (DBTreeNode)tableNode.getUserObject();

		return new DBColumn(dbTableNode.toString(), dbColumnNode.toString());
	}
	
	public TreePath getTreePathOfTable(String table) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		
		Enumeration<TreeNode> e = root.breadthFirstEnumeration();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			DBTreeNode dbNode = (DBTreeNode)node.getUserObject();
	        if (dbNode.getType().equals(DBEntityTypes.TABLE)
	        		&& dbNode.toString().equals(table))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
	
	public TreePath getTreePathOfColumn(DBColumn column) {
		TreePath path = getTreePathOfTable(column.getTableName());
		
		DefaultMutableTreeNode table = (DefaultMutableTreeNode)path.getLastPathComponent();
		
		Enumeration<TreeNode> e = table.breadthFirstEnumeration();
		
		String columnName = column.getColumnName();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
			DBTreeNode dbNode = (DBTreeNode)node.getUserObject();
	        if (dbNode.toString().equals(columnName))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
}

@SuppressWarnings("serial")
class DBTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon catalogIcon;
    private Icon tableIcon;
    private Icon primaryIcon;
    private Icon nonPrimaryIcon;

    DBTreeCellRenderer(Icon catalogIcon,
    		           Icon tableIcon,
    		           Icon primaryIcon,
    		           Icon nonPrimaryIcon) {
        this.catalogIcon = catalogIcon;
        this.tableIcon = tableIcon;
        this.primaryIcon = primaryIcon;
        this.nonPrimaryIcon = nonPrimaryIcon;
    }

    public Component getTreeCellRendererComponent(JTree tree,
							                      Object value,
							                      boolean sel,
							                      boolean expanded,
							                      boolean leaf,
							                      int row,
							                      boolean hasFocus) {
    	super.getTreeCellRendererComponent(tree, value, sel,
                        				   expanded, leaf, row,
                                           hasFocus);
        
    	if(isCatalogNode(value))
        	setIcon(catalogIcon);
        else if(isTableNode(value))
        	setIcon(tableIcon);
        else if(isPrimaryNode(value))
        	setIcon(primaryIcon);
        else if(isNonPrimaryNode(value))
        	setIcon(nonPrimaryIcon);

        return this;
    }

    private boolean isCatalogNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == DBEntityTypes.CATALOG)
        	return true;
        return false;
    }
    
    private boolean isTableNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == DBEntityTypes.TABLE)
        	return true;
        return false;
    }
    
    private boolean isPrimaryNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == DBEntityTypes.PRIMARY)
        	return true;
        return false;
    }
    
    private boolean isNonPrimaryNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == DBEntityTypes.NON_KEY
        		|| nodeObject.getType() == DBEntityTypes.KEY)
        	return true;
        return false;
    }
}