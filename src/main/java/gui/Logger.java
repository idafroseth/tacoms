package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Logger extends JPanel{
	static JTextPane Log = new JTextPane();
	JTextPane ln = new JTextPane();
	static SimpleDateFormat sdf = new SimpleDateFormat("dd_HH:mm:ss");

	public Logger(){
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(Log);
	}
	
	public static void init(){
		//setLookAndFeel();
		Log.setEditable(false);
		Log.setText("LOG FOR TACOMS");
		JScrollPane editorScrollPane = new JScrollPane(Log);
		editorScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Log.setPreferredSize(new Dimension(300,200));
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));


	}
	private static void setLookAndFeel(){
        //namestanje teme
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public static void error(String errorMsg){
		try
		{
			  SimpleAttributeSet error = new SimpleAttributeSet();
			  StyleConstants.setForeground(error, Color.red);
		//    doc.insertString(0, "Start of text\n", null );
			//Strig keyword;
			String errorMessage = "\n"+sdf.format(new Date(System.currentTimeMillis())) + " - " + errorMsg;
			StyledDocument doc = Log.getStyledDocument();
			System.out.println(errorMsg);
			doc.insertString(15,errorMessage, error );
		}
		catch(Exception e) { System.out.println(e); }
	}
	
	public static void info(String info){
		try
		{
			  SimpleAttributeSet status = new SimpleAttributeSet();
			  StyleConstants.setForeground(status, Color.blue);
		//    doc.insertString(0, "Start of text\n", null );
			//Strig keyword;
			String errorMessage = "\n"+sdf.format(new Date(System.currentTimeMillis())) + " - " + info ;
			StyledDocument doc = Log.getStyledDocument();
			doc.insertString(15,errorMessage, status );
			System.out.println(info);
		}
		catch(Exception e) { System.out.println(e); }
	}

	
}
