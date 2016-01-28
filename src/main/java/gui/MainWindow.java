package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import manager.TacomsMng;

public class MainWindow extends JFrame{
	TacomsMng tacoms;
	public static final Integer MAIN_WINDOW_WIDTH = 680;
	public static final Integer MAIN_WINDOW_HEIGHT = 1000;
	public static Integer CONTENT_WINDOW_WIDTH = MAIN_WINDOW_WIDTH - 70;
	public static Integer CONTENT_WINDOW_HEIGHT = MAIN_WINDOW_HEIGHT-300 ;
	
	
	//Different windows
	private JPanel contentPane;
	private MenuPanel menuPanel;
	
	
	public MainWindow(TacomsMng tacoms, String firstMenu){
		this.setTitle("Tacoms Admin");
		menuPanel = new MenuPanel(this);
		this.tacoms = tacoms;
		this.setSize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
		System.out.println(MAIN_WINDOW_WIDTH);
		this.setVisible(true);
		this.setLayout(new BorderLayout());
		contentPane = new JPanel(new CardLayout());
		contentPane.setPreferredSize(new Dimension(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));

		this.getContentPane().add(contentPane, BorderLayout.CENTER);
		this.getContentPane().add(menuPanel, BorderLayout.PAGE_START);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();	
	}
	
	/**
	 * 
	 * @param window
	 * @param title
	 */
	public void addContentWindow(JPanel window, String title){
		menuPanel.addMenuButton(title);
		contentPane.add(window, title.toUpperCase());	
	}
	
	
	/**
	 * Draw the correct tab. This is invoked when the result menu button is hit
	 */
	public void changeCard(String id){
		setTitle("YTDownloader ~ Result");
		((CardLayout) contentPane.getLayout()).show(contentPane, id.toUpperCase());
	}
	
}
