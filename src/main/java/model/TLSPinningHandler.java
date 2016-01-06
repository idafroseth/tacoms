package model;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.text.BadLocationException;

import com.cisco.onep.core.util.TLSUnverifiedElementHandler;

/**
 * 
 * This class  processes unverified TLS connections and decides whether
 * to accept or reject the certificate.
 *
 */
// START SNIPPET: pinningHandler
public class TLSPinningHandler implements TLSUnverifiedElementHandler {

	String pinningFile;

	public TLSPinningHandler(String tlsPinningFile) {
		pinningFile = tlsPinningFile;
	}

	private Decision decision = null;

	public Decision handleVerify(String host, String hashType,
			String fingerprint, boolean changed) {
		Decision decision = showPinningDialog(host, hashType, fingerprint,
				changed);
		return decision;
	}
// END SNIPPET: pinningHandler
	
	/**
	 * Handler for TLS verification failures. Prompts the user to determine
	 * whether to add a host to pinning DB Upon receipt of a certificate which
	 * could not be verified, and for which there is no match in the pinning
	 * database, this handler asks the application whether to accept the
	 * connection and/or whether to add the host to the pinning database. By
	 * default, the connection will be terminated and the pinning db will remain
	 * unchanged.
	 * 
	 * @param host
	 *            String containing either the FQDN or a text version of the IP
	 *            address
	 * @param hashType
	 *            If there was a host name with a non-matching certificate, this
	 *            will be the hash-type from that entry. If there was no entry,
	 *            this will be created as "SHA-1".
	 * @param fingerprint
	 *            Fingerprint text created from the certificate. This will be a
	 *            series of hex bytes separated by colons of the form
	 *            "A1:B2:C3:..."
	 * @param changed
	 *            changed is TRUE if there was an existing entry in the database
	 *            but the certificate does not match. FALSE indicates that there
	 *            was no entry in the database for this host.
	 * @return ACCEPT_AND_PIN if onep should both accept the connection and add
	 *         the entry to the pinning database. ACCEPT_ONCE if onep should
	 *         only accept the connection but not add the entry to the pinning
	 *         database. REJECT if onep should neither accept the connection nor
	 *         add the entry to the pinning database.
	 */
	public Decision showPinningDialog(String host, String hashType,
			String fingerprint, boolean changed) {
		decision = TLSUnverifiedElementHandler.Decision.REJECT;
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500, 300));
		JTextArea textArea = new JTextArea(10, 40);
		String msg = null;
		if (changed) {
            msg = " WARNING: THE CERTIFICATE PRESENTED BY REMOTE HOST:" +host + "\n IS DIFFERENT FROM THE ONE PREVIOUSLY ACCEPTED ";
		} else {
			msg = "WARNING: Certificate presented by remote host(" + host
			+ ") is not verified.";
		}
		msg += "\n\nThe " + hashType+ " fingerprint sent by the remote host(" + host+ ") is:\n"+ fingerprint;
		msg += "\n\nYou MUST verify the certificate on remote host before proceeding! \n";
		msg += "\nChoose from following options:";
		try {
			textArea.getDocument().insertString(0, msg, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setOpaque(false);
		textArea.setEditable(false);
		panel.add(textArea);

		JRadioButton radR = new JRadioButton("Reject", true);
		JRadioButton radA = new JRadioButton("Accept Once");
		JRadioButton radP = new JRadioButton("Accept and Pin");
		if (null == pinningFile)
			radP.setEnabled(false);

		radR.setMnemonic(KeyEvent.VK_R);
		radA.setMnemonic(KeyEvent.VK_A);
		radP.setMnemonic(KeyEvent.VK_P);

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(radR);
		group.add(radA);
		group.add(radP);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));

		buttonPanel.add(radR);
		buttonPanel.add(radA);
		buttonPanel.add(radP);

		panel.add(buttonPanel);

		SpringLayout layout = new SpringLayout();

		layout.putConstraint(SpringLayout.NORTH, textArea, 0,
				SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.NORTH, buttonPanel, 3,
				SpringLayout.SOUTH, textArea);
		layout.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST,
				buttonPanel);

		panel.setLayout(layout);
		panel.setVisible(true);
		// Add listeners to radio buttons:
		radP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				decision = TLSUnverifiedElementHandler.Decision.ACCEPT_AND_PIN;
			}
		});

		radA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				decision = TLSUnverifiedElementHandler.Decision.ACCEPT_ONCE;
			}
		});

		radR.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				decision = TLSUnverifiedElementHandler.Decision.REJECT;
			}
		});
		int option = JOptionPane.showOptionDialog(null, panel,
				"Authenticate host", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, null, null);
		if (option != 0) {
			System.exit(0);
		}
		return decision;

	}

}
