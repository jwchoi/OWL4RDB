package janus.application.ontscheme;

import janus.application.description.AttrDescribable;
import janus.mapping.OntEntity;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class DPTreeSelectionListener implements TreeSelectionListener {
	private AttrDescribable announcer;
	
	public DPTreeSelectionListener(AttrDescribable announcer) {
		this.announcer = announcer;
	}

	public void valueChanged(TreeSelectionEvent e) {
		JTree tree = (JTree)e.getSource();
		
		if (tree.isSelectionEmpty())
			return;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		OntEntity dpNode = (OntEntity)node.getUserObject();
		
		announcer.describeOWLDataProp(dpNode.getURI());
	}
}