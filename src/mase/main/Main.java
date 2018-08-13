package mase.main;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import mase.GUI.GUI;
import mase.agents.GRIDManager;
import mase.entity.Cell;

public class Main {

	private boolean isGUIActive = true;

	public static final int DIJKSTRA_STRATEGY = 0;
	public static final int ASTAR_STRATEGY = 1;

	 public static String[] files = { "res/worcwest15.bmp", "res/factory15.bmp", "res/powerline15.bmp",
	 		"res/worcwest.csv" };
	// public static String[] files = { "res/maze_path.bmp", "res/maze_source.bmp",
	// "res/maze_destination.bmp",
	// "res/maze.csv" };

	// public static String[] files = { "res/mariana/friction.bmp",
	// "res/mariana/destination.bmp",
	// "res/mariana/source.bmp", "res/mariana.csv" };

	private String worcwest;
	private String factory;
	private String powerline;
	private String classes;
	HashMap<Color, Integer> classMap;
	private Cell[][] weightedGraph;
	private ArrayList<Point> initialSpaces;
	private ArrayList<Point> finalSpaces;
	private Point bestFinalSpace;
	private int[][] paths;

	private long startingTime;
	private int agentsQuantity = 1;
	private AID GRIDManagerAddress;
	private AID[] agentsAddresses = new AID[agentsQuantity];
	private int choosenStrategy = DIJKSTRA_STRATEGY;
	private boolean debug = true;
	private boolean optimize = false;

	private GUI gui;
	public static Main instance;

	private int width;
	private int height;

	private int breadth = 0;

	public Main() {
	}

	public static Main getInstance() {
		if (instance == null) {
			instance = new Main();
		}
		return instance;
	}

	public static void main(String args[]) {
		Main main = Main.getInstance();
		if (args != null && args.length > 0) {
			for (String argument : args) {
				if (argument.startsWith("-gui:")) {
					String value = argument.replace("-gui:", "");
					main.setGUIActive(Boolean.parseBoolean(value));
				} else if (argument.startsWith("-optimize:")) {
					String value = argument.replace("-optimize:", "");
					main.setOptimize(Boolean.parseBoolean(value));
				}
			}
		}
		main.setStartingTime(System.currentTimeMillis());
		main.init();
		main.setInitialAndFinalSpaces();
		System.gc();
		if (main.isGUIActive()) {
			main.startGUI();
		}
		startPlatform();

	}

	public void init() {
		worcwest = files[0];
		factory = files[1];
		powerline = files[2];
		classes = files[3];
		classMap = new HashMap<Color, Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(classes));
			String aux = "";
			int count = 0;
			while ((aux = br.readLine()) != null) {
				if (count != 0) {
					StringTokenizer str = new StringTokenizer(aux, ",;");
					int r = Integer.parseInt(str.nextToken());
					int g = Integer.parseInt(str.nextToken());
					int b = Integer.parseInt(str.nextToken());
					int w = Integer.parseInt(str.nextToken());
					classMap.put(new Color(r, g, b), w);
				}
				count++;
			}
			br.close();
			BufferedImage imgWorcwest;
			imgWorcwest = ImageIO.read(new File(worcwest));
			height = imgWorcwest.getHeight();
			width = imgWorcwest.getWidth();
			weightedGraph = new Cell[height][width];

			if (isGUIActive) {
				GUI.getInstance().setColors(new Color[height][width]);
				GUI.getInstance().setAuxiliaryColors(new Color[height][width]);
			}

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color c = new Color(imgWorcwest.getRGB(i, j));
					if (isGUIActive) {
						GUI.getInstance().getColors()[i][j] = c;
						GUI.getInstance().getAuxiliaryColors()[i][j] = new Color(255, 255, 255);
					}
					Integer weight = classMap.get(c);
					if (weight != null) {
						weightedGraph[i][j] = new Cell(weight, new Point(i, j));
					} else {
						weightedGraph[i][j] = new Cell(Integer.MAX_VALUE, new Point(i, j));
					}
				}
			}

			imgWorcwest = null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	public void setInitialAndFinalSpaces() {
		initialSpaces = new ArrayList<>();
		finalSpaces = new ArrayList<>();
		try {
			BufferedImage imgFactory = ImageIO.read(new File(powerline));
			BufferedImage imgPowerline = ImageIO.read(new File(factory));

			int height = imgFactory.getHeight();
			int width = imgFactory.getWidth();

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color cFactory = new Color(imgFactory.getRGB(i, j));

					int red = cFactory.getRed();
					int green = cFactory.getGreen();
					int blue = cFactory.getBlue();

					if (red != 255 && green != 255 && blue != 255
							&& weightedGraph[i][j].getWeight() != Integer.MAX_VALUE) {
						initialSpaces.add(new Point(i, j));
						if (isGUIActive) {
							GUI.getInstance().getColors()[i][j] = new Color(255, 51, 163);
						}
					}

					Color cPowerline = new Color(imgPowerline.getRGB(i, j));

					red = cPowerline.getRed();
					green = cPowerline.getGreen();
					blue = cPowerline.getBlue();

					if (red != 255 && green != 255 && blue != 255
							&& weightedGraph[i][j].getWeight() != Integer.MAX_VALUE) {
						finalSpaces.add(new Point(i, j));
						if (isGUIActive) {
							GUI.getInstance().getColors()[i][j] = new Color(255, 51, 163);
						}
					}
				}
			}
			if (optimize && initialSpaces.size() > finalSpaces.size()) {
				ArrayList<Point> switcharoo = new ArrayList<>();
				switcharoo.addAll(initialSpaces);
				initialSpaces = new ArrayList<Point>();
				initialSpaces.addAll(finalSpaces);
				finalSpaces = new ArrayList<Point>();
				finalSpaces.addAll(switcharoo);
			}
			imgFactory = null;
			imgPowerline = null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void initPaths() {
		paths = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				paths[i][j] = -1;
			}
		}
	}

	public void writeImage(String name, ArrayList<ArrayList<Point>> paths) {
		try {
			BufferedImage worcwestImg = ImageIO.read(new File(worcwest));

			BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					finalImg.setRGB(i, j, worcwestImg.getRGB(i, j));
				}
			}

			for (ArrayList<Point> path : paths) {
				Random rand = new Random();
				Color color = new Color(rand.nextInt(0xFFFFFF));
				for (Point p : path) {
					for (int i = (-1 * breadth); i <= breadth; i++) {
						for (int j = (-1 * breadth); j <= breadth; j++) {
							if (p.x + i < 0 || p.y + j < 0 || p.x + i >= width || p.y + j >= height) {
								continue;
							}
							finalImg.setRGB(p.x + i, p.y + j, color.getRGB());
						}
					}
				}
			}

			for (int i = 0; i < initialSpaces.size(); i++) {
				finalImg.setRGB(initialSpaces.get(i).x, initialSpaces.get(i).y, Color.CYAN.getRGB());
			}

			for (int i = 0; i < finalSpaces.size(); i++) {
				finalImg.setRGB(finalSpaces.get(i).x, finalSpaces.get(i).y, Color.YELLOW.getRGB());
			}

			try {
				ImageIO.write(finalImg, "bmp", new File(name));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void startPlatform() {
		Runtime runtime = Runtime.instance();

		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.GUI, "false");
		profile.setParameter(Profile.CONTAINER_NAME, "Cont_LCC_0");
		profile.setParameter(Profile.PLATFORM_ID, "MASE_0");

		AgentContainer container = runtime.createMainContainer(profile);
		try {
			GRIDManager g = new GRIDManager();
			container.acceptNewAgent("GRIDManager", g);
			container.getAgent("GRIDManager").start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startGUI() {
		// gui = GUI.getInstance();
		// gui.setVisible(false);
	}

	public Cell[][] getWeightedGraph() {
		return weightedGraph;
	}

	public ArrayList<Point> getInitialSpaces() {
		return initialSpaces;
	}

	public ArrayList<Point> getFinalSpaces() {
		return finalSpaces;
	}

	public void setWeightedGraph(Cell[][] weightedGraph) {
		this.weightedGraph = weightedGraph;
	}

	public void setInitialSpaces(ArrayList<Point> initialSpaces) {
		this.initialSpaces = initialSpaces;
	}

	public void setFinalSpaces(ArrayList<Point> finalSpaces) {
		this.finalSpaces = finalSpaces;
	}

	public boolean isGUIActive() {
		return isGUIActive;
	}

	public void setGUIActive(boolean isGUIActive) {
		this.isGUIActive = isGUIActive;
	}

	public String getWorcwest() {
		return worcwest;
	}

	public void setWorcwest(String worcwest) {
		this.worcwest = worcwest;
	}

	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	public String getPowerline() {
		return powerline;
	}

	public void setPowerline(String powerline) {
		this.powerline = powerline;
	}

	public long getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(long startingTime) {
		this.startingTime = startingTime;
	}

	public int getAgentsQuantity() {
		return agentsQuantity;
	}

	public void setAgentsQuantity(int agentsQuantity) {
		this.agentsQuantity = agentsQuantity;
	}

	public AID getGRIDManagerAddress() {
		return GRIDManagerAddress;
	}

	public void setGRIDManagerAddress(AID gRIDManagerAddress) {
		GRIDManagerAddress = gRIDManagerAddress;
	}

	public AID[] getAgentsAddresses() {
		return agentsAddresses;
	}

	public void setAgentsAddresses(AID[] agentsAddresses) {
		this.agentsAddresses = agentsAddresses;
	}

	public int getChoosenStrategy() {
		return choosenStrategy;
	}

	public void setChoosenStrategy(int choosenStrategy) {
		this.choosenStrategy = choosenStrategy;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isOptimize() {
		return optimize;
	}

	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public Point getBestFinalSpaces() {
		if (bestFinalSpace == null) {
			bestFinalSpace = finalSpaces.get(0);
		}
		return bestFinalSpace;
	}

	public synchronized void setPath(int id, int i, int j) {
		paths[i][j] = id;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
