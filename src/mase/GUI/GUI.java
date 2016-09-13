package mase.GUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GUI extends JFrame {
	
	protected final JFrame myInstance;
	protected final JPanel mainContents;
	protected final JMenuBar mainMenu;
	private JMenu file;
	private JMenuItem config;
	private JMenuItem exit;

	public static void main(String[] args) {
		// FIXME for test purposes. Delete this function in the future
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI thisClass = new GUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	public GUI() {
		// starting main frame
		myInstance = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		//
		mainContents = new JPanel(new BorderLayout());
		
		// starting and attaching main menu
		mainMenu = new JMenuBar();
		file = new JMenu("File");
		config = new JMenuItem("Config");
		config.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO open config window
			}
		});
		file.add(config);

		exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO stop and kill the platform
				myInstance.dispose();
			}
		});
		file.add(exit);
		this.setJMenuBar(mainMenu);
		this.setContentPane(mainContents);
		this.revalidate();
	}

}
