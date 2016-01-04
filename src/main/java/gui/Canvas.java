package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

import manager.TacomsMng;

public class Canvas extends JXPanel{

	JLabel title;
	TacomsMng mng;
    public Canvas(TacomsMng mng, String title){
    	this.mng = mng;
    	this.setPreferredSize(new Dimension(300,300));
    	
    	this.title = new JLabel(title, SwingConstants.CENTER);
    	this.setLayout(new GridLayout(6, 1));
    	this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.title.setFont(new Font("Verdana", Font.BOLD, 15));
    	this.add(this.title);
    	ServiceRow.init();
    	addShadow();
    }
    
    public void addRow(String serviceName, ServiceID serviceID, Boolean isServiceEnabled){
    	this.add(new ServiceRow(mng, serviceName, serviceID ,isServiceEnabled));
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
