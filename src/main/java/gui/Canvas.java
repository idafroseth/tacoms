package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

import manager.TacomsMng;
import model.ServiceID;

public class Canvas extends JXPanel{

	JLabel title;
	TacomsMng mng;
	JPanel contentPane = new JPanel();
	
    public Canvas(TacomsMng mng, String title, int width, int height){
    	this.mng = mng;
    	this.setPreferredSize(new Dimension(width, height));
    	
    	this.setLayout(new BorderLayout());
    	
    	
    //	this.setAlignmentX(Component.LEFT_ALIGNMENT);
    	setContentPane();
		setTitle(title);
		
		
    	ServiceRow.init();
    	addShadow();
    }
    
    public void addRow(String serviceName, ServiceID serviceID, Boolean isServiceEnabled){
    	contentPane.add(new ServiceRow(mng, serviceName, serviceID ,isServiceEnabled));
    }
    
    public void changeLayout(){
    	
    }
    public void addLogger(){
     	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
    	
    	contentPane.add(new Logger());
    }
    
    public void setContentPane(){
    	contentPane.setLayout(new GridLayout(6, 1));
    	this.add(contentPane, BorderLayout.CENTER);
    //	contentPane.setPreferredSize(new Dimension(width,width));
    }
    
    public String getTitle(){
    	return title.getText();
    }
    
    public void setTitle(String title){
    	this.title = new JLabel(title, SwingConstants.CENTER);
    	this.title.setFont(new Font("Verdana", Font.BOLD, 15));
    	this.title.setPreferredSize(new Dimension(15,43));
    	this.add(this.title, BorderLayout.PAGE_START);
    }
    
    private void addShadow(){
        DropShadowBorder shadow = new DropShadowBorder();
        shadow.setShadowColor(Color.BLACK);
        shadow.setShowLeftShadow(true);
        shadow.setShowRightShadow(true);
        shadow.setShowBottomShadow(true);
        shadow.setShowTopShadow(true);
        this.setBorder(shadow);
    }
    private void removeShadow(){
    	this.setBorder(null);
    }
}
