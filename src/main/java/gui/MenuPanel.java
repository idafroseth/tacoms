package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuPanel extends JPanel {
	public static final Color MENU_COLOR = Color.LIGHT_GRAY;
	public static final Color ACTIVE_BUTTON_COLOR = new Color(238,238,238);
	public static final Color BUTTON_COLOR = Color.LIGHT_GRAY;
	
	public static final Dimension MENU_BUTTON_SIZE = new Dimension(150,30);
	
	private MouseClickListener mouseListener = new MouseClickListener();
	
	private JLabel activeButton = new JLabel();
	private MainWindow mainWindow;
	
	public MenuPanel(MainWindow parent){
		this.setBackground(MENU_COLOR);
		this.mainWindow = parent;
		setPreferredSize(new Dimension(MainWindow.MAIN_WINDOW_WIDTH, 30));
		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
	}
	
	public JLabel addMenuButton(String title){
		JLabel button = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
		button.setPreferredSize(MENU_BUTTON_SIZE);
		button.addMouseListener(mouseListener);
		button.setOpaque(true);
		button.setBackground(BUTTON_COLOR);
		button.setName(title);
		button.setFont(new Font("Verdana", Font.PLAIN, 12));
		add(button);
		setButtonAsActive(button);
		return button;
		
	}
	public void setButtonAsActive(JLabel button){
		activeButton.setBackground(BUTTON_COLOR);
		activeButton.setFont(button.getFont().deriveFont(Font.PLAIN));
		activeButton = button;
		button.setBackground(ACTIVE_BUTTON_COLOR);
		button.setFont(button.getFont().deriveFont(Font.BOLD));
	}
	
	private class MouseClickListener implements MouseListener{
		JLabel hovered = new JLabel();

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			JLabel button = (JLabel)e.getSource();
		//BorderFactory.createLoweredBevelBorder());
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			JLabel button = (JLabel)e.getSource();
			setButtonAsActive(button);
			mainWindow.changeCard(button.getName());	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			JLabel button = (JLabel)e.getSource();	
			button.setBackground(new Color(238,238,238));
			hovered = button; 
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			JLabel button = (JLabel)e.getSource();
			if(hovered != null && button != activeButton){
				button.setBackground(MENU_COLOR);
				hovered = null;
			}
			
		}
	}

}
