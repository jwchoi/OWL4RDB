package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.OntTree;
import janus.database.DBColumn;
import janus.database.DBEntityTypes;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedClassAction extends AbstractAction {
	private static final String NAME = "Go to Mapped Class";
	
	public GoToMappedClassAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DBTree dbTree = UIRegistry.getDBTree();
		OntTree clsTree = UIRegistry.getClsTree();
		
		DBEntityTypes selectedNodeType = dbTree.getTypeOfSelectedNode();
		
		URI mappedClass = null;
		
		if (selectedNodeType.equals(DBEntityTypes.TABLE))
			mappedClass = Janus.owlMappingMetadata.getMappedClass(dbTree.getSelectedTable());
		if (selectedNodeType.equals(DBEntityTypes.PRIMARY) 
				|| selectedNodeType.equals(DBEntityTypes.KEY)) {
			DBColumn column = dbTree.getSelectedColumn();
			mappedClass = Janus.owlMappingMetadata.getMappedClass(column.getTableName(), column.getColumnName());
		}
		
		TreePath path = clsTree.getTreePathOfEntity(mappedClass);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.ONTOLOGY));
			
			clsTree.setSelectionPath(path);
		}
	}
}
