package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import manager.TacomsMng;

public class ConnectRow extends JPanel{
	TacomsMng mng;
	JTextField ipAddress = new JTextField("IP address");
	JTextField userName = new JTextField("Username");
	JPasswordField password = new JPasswordField("Password");
	JButton connect = new JButton("Connect");
	Dimension filedSize =  new Dimension( 100, 24  );
	
	public ConnectRow(TacomsMng mnger){
		this.mng = mnger;
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 10));//;);
		ipAddress.setPreferredSize( filedSize);
		userName.setPreferredSize( filedSize);
		password.setPreferredSize( filedSize);
		connect.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mng.connect(ipAddress.getText(),userName.getText(),password.getText());	
			}
		});
		
		this.add(new JLabel("IP address:"));
		this.add(ipAddress);
		this.add(new JLabel("UserName:"));
		this.add(userName);
		this.add(new JLabel("Password:"));
		this.add(password);
		this.add(connect);
	}
	

}
