package mase.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	protected final JFrame myInstance;
	protected final JPanel mainContents;
	protected final JScrollPane simulationSite;
	protected final JMenuBar mainMenu;
	private JMenu file;
	private JMenuItem config;
	private JMenuItem exit;
	private static GUI instance;

	private Color[][] colors;
	private Color[][] auxiliaryColors;

	public static void main(String[] args) {
		// FIXME for test purposes. Delete this function in the future
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUI thisClass = new GUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(false);
			}
		});
	}

	private GUI() {
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

		class SimulationCanvas extends JScrollPane {
			private static final long serialVersionUID = 1L;

			public SimulationCanvas() {
				if (colors != null && colors[0] != null)
					setSize(colors.length, colors[0].length);
			}

			public void paint(Graphics g) {
				super.paint(g);
				setDoubleBuffered(true);
				if (colors != null && colors[0] != null) {
					for (int i = 0; i < colors.length; i++) {
						for (int j = 0; j < colors.length; j++) {
							if (!auxiliaryColors[i][j].equals(new Color(255, 255, 255))) {
								g.setColor(auxiliaryColors[i][j]);
								g.fillRect(1 * i, 1 * j, 1, 1);
							} else {
								g.setColor(colors[i][j]);
								g.fillRect(1 * i, 1 * j, 1, 1);
							}
						}
					}
				}
			}
		}

		simulationSite = new SimulationCanvas();
		simulationSite.setAutoscrolls(true);

		mainContents.add(simulationSite, BorderLayout.CENTER);
		this.setJMenuBar(mainMenu);
		this.setContentPane(mainContents);
		this.revalidate();

	}

	public static GUI getInstance() {
		if (instance == null) {
			instance = new GUI();
		}
		return instance;
	}

	public void repaint2() {
		simulationSite.repaint();
		this.repaint();
	}

	public Color[][] getColors() {
		return colors;
	}

	public void setColors(Color[][] colors) {
		GUI.getInstance().colors = colors;
	}

	public Color[][] getAuxiliaryColors() {
		return auxiliaryColors;
	}

	public void setAuxiliaryColors(Color[][] auxiliaryColors) {
		GUI.getInstance().auxiliaryColors = auxiliaryColors;
	}

}
