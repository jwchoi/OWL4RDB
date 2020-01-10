package janus.application.dialog;

import janus.database.DBMSTypes;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

@SuppressWarnings("serial")
public class SessionManager extends JDialog {
	
	private JComboBox<DBMSTypes> DBMSType;
	private JTextField host;
	private JTextField port;
	private JTextField id;
	private JPasswordField password;
	private JTextField schema;
	private JTextField baseIRI;
	private JTextField prefix;
	private ButtonGroup rdfOrOwl;
	
	private boolean NORMAL_EXIT = false;

	public SessionManager(Frame owner) {
		super(owner, "Session Manager", true);

		buildUI();
	}

	private void buildUI() {
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// DBMS Type
		JLabel DBMSTypeLabel = new JLabel("DBMS Type: ", SwingConstants.RIGHT);
		DBMSTypeLabel.setBounds(10, 10, 100, 30);
		contentPane.add(DBMSTypeLabel);
		DBMSType = new JComboBox<>();
		DBMSType.addItem(DBMSTypes.MARIADB);
		DBMSType.setBounds(120, 10, 120, 30);
		contentPane.add(DBMSType);

		// Server Host
		JLabel hostLabel = new JLabel("Server Host: ", SwingConstants.RIGHT);
		hostLabel.setBounds(10, 45, 100, 30);
		contentPane.add(hostLabel);
		host = new JTextField("localhost");
		host.setBounds(120, 45, 120, 30);
		contentPane.add(host);

		// Port
		JLabel portLabel = new JLabel("Port: ", SwingConstants.RIGHT);
		portLabel.setBounds(250, 45, 30, 30);
		contentPane.add(portLabel);
		port = new JTextField("3306");
		port.setBounds(290, 45, 40, 30);
		contentPane.add(port);

		// ID
		JLabel idLabel = new JLabel("Username: ", SwingConstants.RIGHT);
		idLabel.setBounds(10, 80, 100, 30);
		contentPane.add(idLabel);
		id = new JTextField("root");
		id.setBounds(120, 80, 120, 30);
		contentPane.add(id);

		// Password
		JLabel passwordLabel = new JLabel("Password: ", SwingConstants.RIGHT);
		passwordLabel.setBounds(10, 115, 100, 30);
		contentPane.add(passwordLabel);
		password = new JPasswordField("root");
		password.setBounds(120, 115, 120, 30);
		contentPane.add(password);

		// Default Schema
		JLabel defaultSchemaLabel = new JLabel("Schema: ", SwingConstants.RIGHT);
		defaultSchemaLabel.setBounds(10, 150, 100, 30);
		contentPane.add(defaultSchemaLabel);
		schema = new JTextField("iswc");
		schema.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                baseIRI.setText("<http://foo.example/" + schema.getText() + "/>");
            }
        });
		schema.setBounds(120, 150, 120, 30);
		contentPane.add(schema);
		
		// Base IRI
		JLabel ontologyIRILabel = new JLabel("Base IRI: ", SwingConstants.RIGHT);
		ontologyIRILabel.setBounds(10, 185, 100, 30);
		contentPane.add(ontologyIRILabel);
		baseIRI = new JTextField("<http://foo.example/iswc/>");
		baseIRI.setBounds(120, 185, 210, 30);
		contentPane.add(baseIRI);

		// Prefix
		JLabel prefixLabel = new JLabel("Prefix: ", SwingConstants.RIGHT);
		prefixLabel.setBounds(340, 185, 40, 30);
		contentPane.add(prefixLabel);
		prefix = new JTextField("foo");
		prefix.setBounds(390, 185, 40, 30);
		contentPane.add(prefix);

		// OK
		JButton OK = new JButton("OK");
		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NORMAL_EXIT = true;
				setVisible(false);
			}
		});
		getRootPane().setDefaultButton(OK);
		OK.setBounds(135,220,80,30);
		contentPane.add(OK);

		// Cancel
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		cancel.setBounds(225,220,80,30);
		contentPane.add(cancel);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 440) >> 1, (screenSize.height- 290) >> 1, 440, 290);
		
		setResizable(false);
	}
	
	public DBMSTypes getDBMSType() { return DBMSTypes.MARIADB; }
	public String getHost() { return host.getText().trim(); }
	public String getPort() { return port.getText().trim(); }
	public String getID() { return id.getText().trim(); }
	public String getPassword() { return new String(password.getPassword()).trim(); }
	public String getSchema() { return schema.getText().trim(); }
	public String getPrefix() { return prefix.getText().trim(); }
	
	public String getBaseIRI() {
		String iri = baseIRI.getText().trim();
		iri = iri.substring(iri.indexOf("<")+1, iri.indexOf(">"));
		
		return iri; 
	}
	
	public boolean isNormalExit() { return NORMAL_EXIT; }
}