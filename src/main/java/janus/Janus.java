package janus;

import java.io.File;
import janus.application.JanusWindow;
import janus.application.dialog.SessionManager;
import janus.database.DBBridge;
import janus.database.DBBridgeFactory;
import janus.database.LocalDBSchemaFactory;
import janus.database.LocalDBSchema;
import janus.mapping.OntMapper;
import janus.mapping.metadata.owl.OWLMappingMetadata;
import janus.mapping.metadata.owl.OWLMappingMetadataFactory;
import janus.ontology.OntBridge;
import janus.ontology.OntBridgeFactory;
import janus.ontology.ReasonerType;
import janus.query.rewriter.SQLGenerator;
import janus.query.rewriter.SQLGeneratorFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Janus {
	
	public static final String DEFAULT_DIR_FOR_TBOX_FILE = "./ontologies/tbox/";
	public static final String DEFAULT_DIR_FOR_DUMP_FILE = "./ontologies/tbox&abox/";
	
	public static String baseURI;
	public static String prefix;
	
	public static DBBridge dbBridge;
	public static OntBridge ontBridge;
	public static OntMapper ontMapper;
	public static LocalDBSchema localDBSchema;
	public static OWLMappingMetadata owlMappingMetadata;
	public static SQLGenerator sqlGenerator;

	public static JanusWindow mainWindow;
	
	public static void main (String[] args) {
		Splash splash = new Splash(ImageURIs.SPLASH, ImageURIs.LOGO);
		splash.setVisible(true);
		
		setLookAndFeel();
		
		SessionManager sessionManager = new SessionManager(splash);
		
		do {
			sessionManager.setVisible(true);
			
			if(!sessionManager.isNormalExit()) 
				System.exit(0);

			dbBridge = DBBridgeFactory.getDBBridge(sessionManager.getDBMSType(),
															sessionManager.getHost(),
															sessionManager.getPort(),
															sessionManager.getID(),
															sessionManager.getPassword(),
															sessionManager.getSchema());
			
			if(dbBridge == null)
				JOptionPane.showMessageDialog(splash, "Could not connect to the DBMS.", 
											  "Janus Error", JOptionPane.ERROR_MESSAGE);
			else {
				baseURI = sessionManager.getBaseIRI();
				prefix = sessionManager.getPrefix();
				break;
			}
			
		} while(true);
		
		sqlGenerator = SQLGeneratorFactory.getSQLGenerator(sessionManager.getDBMSType());
		
		localDBSchema = LocalDBSchemaFactory.generateLocalDatabaseMetaData(dbBridge);
		
		ontMapper = new OntMapper();
			
		File ontFile = new File(Janus.DEFAULT_DIR_FOR_TBOX_FILE + localDBSchema.getCatalog() + ".owl");
		if (!ontFile.exists())
			ontFile = ontMapper.generateTBoxFile();

		long start = System.currentTimeMillis();

		ontBridge = OntBridgeFactory.getOntBridge(ontFile, ReasonerType.PELLET_REASONER);

		long end = System.currentTimeMillis();
		System.out.println( "loading and reasoning time : " + ( end - start));

		Janus.owlMappingMetadata = OWLMappingMetadataFactory.generateMappingMetaData();
		
		mainWindow = new JanusWindow(sessionManager.getSchema() + " <" + baseURI + ">");
		sessionManager.dispose();
		splash.dispose();
		mainWindow.setVisible(true);
	}
	
	private static void setLookAndFeel() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		});
	}
}