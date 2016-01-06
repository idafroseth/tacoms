package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import manager.TacomsMng;

public class ServicesPanel extends JPanel {
	
	TacomsMng mng;
	HashMap<String, Canvas> canvases = new HashMap<String, Canvas>();
	JPanel contentPane = new JPanel();
	
	public ServicesPanel(TacomsMng mng, String title){
		this.mng = mng;
		setLayout(new BorderLayout());
		
		
		contentPane.setLayout(new FlowLayout(FlowLayout.LEADING, 30, 30));
		add(new ConnectRow(mng), BorderLayout.PAGE_START);
		add(contentPane,BorderLayout.CENTER);
	}
	
	public void addService(String containerID,String servName, ServiceID ServiceId, Boolean isServiceEnabled ){
		canvases.get(containerID).addRow( servName, ServiceId, isServiceEnabled);
	}
	
	public void addContainer(String id){
		Canvas can = new Canvas(mng, id);
		can.setForeground(Color.WHITE);
		canvases.put(id, can);
		contentPane.add(can);
		
	}
	public HashMap<String, Canvas> getCanvases(){
		return this.canvases;
	}
	public Canvas getCanvas(String id){
		return canvases.get(id);
	}
	
	
}
