package janus.application.actions;

import janus.Janus;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class BuildTBoxAction extends AbstractAction {
	private static final String NAME = "Build TBOX";
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Janus.ontMapper.generateTBoxFile();
		
		JOptionPane.showMessageDialog((Component)e.getSource(), "Translating the schema into OWL has finished.");
	}
	
	public String getToolTipText() {
		return NAME;
	}
}