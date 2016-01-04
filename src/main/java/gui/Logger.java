package gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Logger extends JPanel{
	static JTextPane Log = new JTextPane();
	JTextPane ln = new JTextPane();

	public Logger(){
		add(Log);
	}
	
	public static void init(){
		Log.setEditable(false);
		Log.setText("LOG FOR TACOMS");
		JScrollPane editorScrollPane = new JScrollPane(Log);
		editorScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

	}
	
	public static void error(String errorMsg){
		try
		{
			  SimpleAttributeSet error = new SimpleAttributeSet();
			  StyleConstants.setForeground(error, Color.red);
		//    doc.insertString(0, "Start of text\n", null );
			//Strig keyword;
			String errorMessage = "\n"+ "ERROR - " +errorMsg;
			StyledDocument doc = Log.getStyledDocument();
			doc.insertString(15,errorMessage, error );
		}
		catch(Exception e) { System.out.println(e); }
	}
	
	public static void status(){
		try
		{
			  SimpleAttributeSet status = new SimpleAttributeSet();
			  StyleConstants.setForeground(status, Color.blue);
		//    doc.insertString(0, "Start of text\n", null );
			//Strig keyword;
			String errorMessage = "\n"+ "STATUS - " ;
			StyledDocument doc = Log.getStyledDocument();
			doc.insertString(15,errorMessage, status );
		}
		catch(Exception e) { System.out.println(e); }
	}

	
}
