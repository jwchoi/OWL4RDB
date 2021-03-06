package janus.application;

import janus.application.dbscheme.DBTree;
import janus.application.description.DescrTree;
import janus.application.ontscheme.OntTree;
import janus.application.query.Showable;
import janus.application.query.Submittable;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class UIRegistry {
	private static JTabbedPane schemeTab;
	private static JScrollPane classTree;
	private static JTabbedPane propertiesTab;
	private static JScrollPane dataPropertyTree;
	private static JScrollPane dbSchemeTree;
	private static JScrollPane descriptionTree;
	private static JTabbedPane displayTab;
	private static JTabbedPane documentTab;
	private static JFrame frame;
	private static JTabbedPane AssertionsTab;
	private static JScrollPane objectPropertyTree;
	private static JTabbedPane queryTab;
	private static JScrollPane queryResultTable;
	
	
	public static JTabbedPane getAssertionsTab() {
		return AssertionsTab;
	}
	
	public static JTabbedPane getPropertiesTab() {
		return propertiesTab;
	}
	
	public static JTabbedPane getschemeTab() {
		return schemeTab;
	}
	
	public static JTabbedPane getDisplayTab() {
		return displayTab;
	}
	
	public static JTabbedPane getDocumentTab() {
		return documentTab;
	}
	
	public static OntTree getClsTree() {
		return (OntTree)classTree;
	}
	
	public static OntTree getDPTree() {
		return (OntTree)dataPropertyTree;
	}
	
	public static DBTree getDBTree() {
		return (DBTree)dbSchemeTree;
	}
	
	static DescrTree getDescriptionTree() {
		return (DescrTree)descriptionTree;
	}
	
	public static Window getDialogOwner() { return frame; }
	
	public static OntTree getOPTree() {
		return (OntTree)objectPropertyTree;
	}
	
	public static Showable getQueryResultTable() {
		return (Showable)queryResultTable;
	}
	
	public static Submittable getSelectedQueryArea() {
		return (Submittable)queryTab.getSelectedComponent();
	}
	
	static JFrame getWindow() { return frame; }
	
	static void registerDisplayTab(JTabbedPane displayTab) {
		UIRegistry.displayTab = displayTab;
	}
	
	static void registerDocumentTab(JTabbedPane documentTab) {
		UIRegistry.documentTab = documentTab;
	}
	
	static void registerClassTree(JScrollPane classTree) {
		UIRegistry.classTree = classTree;
	}
	
	static void registerDataPropertyTree(JScrollPane dataPropertyTree) {
		UIRegistry.dataPropertyTree = dataPropertyTree;
	}
	
	static void registerDBSchemeTree(JScrollPane dbSchemeTree) {
		UIRegistry.dbSchemeTree = dbSchemeTree;
	}
	
	static void registerDescriptionTree(JScrollPane descriptionTree) {
		UIRegistry.descriptionTree = descriptionTree;
	}
	
	static void registerAssertionsTab(JTabbedPane assertionsTab) {
		UIRegistry.AssertionsTab = assertionsTab;
	}
	
	static void registerSchemeTab(JTabbedPane schemeTab) {
		UIRegistry.schemeTab = schemeTab;
	}
	
	static void registerObjectPropertyTree(JScrollPane objectPropertyTree) {
		UIRegistry.objectPropertyTree = objectPropertyTree;
	}
	
	static void registerQueryTab(JTabbedPane queryTab) {
		UIRegistry.queryTab = queryTab;
	}
	
	static void registerPropertiesTab(JTabbedPane propertiesTab) {
		UIRegistry.propertiesTab = propertiesTab;
	}
	
	static void registerQueryResultTable(JScrollPane queryResultTable) {
		UIRegistry.queryResultTable = queryResultTable;
	}
	
	static void registerWindow(JFrame frame) {
		UIRegistry.frame = frame;
	}
}