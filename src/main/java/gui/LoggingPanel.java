package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JPanel;

import manager.TacomsMng;
import model.ServiceID;

public class LoggingPanel extends  JPanel {
	TacomsMng mng;
	HashMap<String, Canvas> canvases = new HashMap<String, Canvas>();
	JPanel contentPane = new JPanel();

public LoggingPanel(TacomsMng mng, String title){
	this.mng = mng;
	setLayout(new BorderLayout());
	contentPane.setLayout(new FlowLayout(FlowLayout.LEADING, 30, 30));
	add(new ConnectRow(mng), BorderLayout.PAGE_START);
	add(contentPane,BorderLayout.CENTER);
}

public void addContainer(String id){
	Canvas can = new Canvas(mng, id, MainWindow.CONTENT_WINDOW_WIDTH, MainWindow.CONTENT_WINDOW_HEIGHT);
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
