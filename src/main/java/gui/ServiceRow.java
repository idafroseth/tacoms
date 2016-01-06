package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import manager.TacomsMng;
import model.ServiceID;

public class ServiceRow extends JPanel{
	TacomsMng mng;
	JLabel serviceLable;
	JButton enableButton = new JButton("Enable");
	JButton disableButton =  new JButton("Disable");
	static Color primaryColor = Color.WHITE;
	static Color secondaryColor = new Color(233,233,233);
	public static Color NEXT_COLOR;

	
	
	public ServiceRow(TacomsMng mng, String serviceName, ServiceID serviceID ,Boolean isServiceEnabled){
		this.mng = mng;
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 10));//;);
		this.setBackground(NEXT_COLOR);
		configureServiceLabel(serviceName);
		configureButtons(mng, serviceName, serviceID, isServiceEnabled);
		this.setPreferredSize(new Dimension(Canvas.CANVAS_WIDTH-40, Canvas.CANVAS_HEIGHT));
		//this.setAlignmentX(Component.LEFT_ALIGNMENT);
		toggleColor();
		this.add(this.serviceLable);
		this.add(enableButton);
		this.add(disableButton);
		
		
	}
	public static void init(){
		NEXT_COLOR = primaryColor;
	}
	
	private void toggleColor(){
		if(NEXT_COLOR.equals(primaryColor)){
			NEXT_COLOR = secondaryColor;
		}else{
			NEXT_COLOR = primaryColor;
		}
	}
	private void configureServiceLabel(String serviceName){
		this.serviceLable = new JLabel(serviceName);
		this.serviceLable.setPreferredSize(new Dimension(100, 20));
	}
	private void configureButtons(final TacomsMng mng, String serviceName, ServiceID serviceID, Boolean isServiceEnabled){
		enableButton.setActionCommand(serviceID.toString());
		disableButton.setActionCommand(serviceID.toString());
		
		enableButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				serviceEnabled(mng.enableService(e.getActionCommand()));
			}
		});
		
		disableButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				serviceEnabled(mng.disableService(e.getActionCommand()));
			}
		});
		serviceEnabled(isServiceEnabled);
	}
	
	public void serviceEnabled(Boolean isEnabled){
		enableButton.setSelected(isEnabled);
		disableButton.setSelected(!isEnabled);
	}



}
