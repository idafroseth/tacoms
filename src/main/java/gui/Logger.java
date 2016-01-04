package gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class Logger extends JFrame{
	static JTextPane Log = new JTextPane();
	 Style error = Log.addStyle("ErrorStyle", null);
	 StyleConstants.setForeground(style, Color.RED);
	
	public static void init(){
		Log.setEditable(false);
		Log.setText("LOG FOR TACOMS");
		JScrollPane editorScrollPane = new JScrollPane(Log);
		editorScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

	}
	
	public static void error(){
		try
		{
		//    doc.insertString(0, "Start of text\n", null );
			//Strig keyword;
			String errorMessage = "\n"+ "ERROR - " ;
			StyledDocument doc = Log.getStyledDocument();
			doc.insertString(0,errorMessage, Color.BLUE );
		}
		catch(Exception e) { System.out.println(e); }
	}
	
	public static void status(){
		
	}

	
}
