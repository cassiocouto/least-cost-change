package mase.main;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import mase.agents.GRIDManager;
import mase.entity.Cell;

public abstract class Main {
	public static final int DIJKSTRA_STRATEGY = 0;
	public static final int ASTAR_STRATEGY = 1;
	private static String worcwest = "res/worcwest15.bmp";
	private static String factory = "res/factory15.bmp";
	private static String powerline = "res/powerline15.bmp";
	private static Cell[][] weightedGraph;
	private static ArrayList<Point> initialSpaces;
	private static ArrayList<Point> finalSpaces;
	public static long startingTime;

	public static int agentsQuantity = 20;
	public static AID GRIDManagerAddress;
	public static AID[] agentsAddresses = new AID[agentsQuantity];

	public static int choosenStrategy = DIJKSTRA_STRATEGY;
	
	public static boolean debug = false;

	public static void main(String args[]) {
		startingTime = System.currentTimeMillis();
		initWeightedGraph();
		setInitialAndFinalSpaces();
		startPlatform();
	}

	public static void initWeightedGraph() {
		BufferedImage imgWorcwest;
		try {
			imgWorcwest = ImageIO.read(new File(worcwest));
			int height = imgWorcwest.getHeight();
			int width = imgWorcwest.getWidth();
			weightedGraph = new Cell[height][width];

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color c = new Color(imgWorcwest.getRGB(i, j));
					int red = c.getRed();
					int green = c.getGreen();
					int blue = c.getBlue();
					int weight = 0;
					if (red == 0 && green == 255 && blue == 0) {
						// deciduous florest
						weight = 4;
					} else if (red == 255 && green == 255 && blue == 0) {
						// coniferous florest
						weight = 5;
					} else if (red == 255 && green == 0 && blue == 0) {
						// farming
						weight = 1;
					} else if (red == 0 && green == 0 && blue == 255) {
						// urbanization
						weight = 1000;
					} else { // removed
						weight = Integer.MAX_VALUE;
					}
					weightedGraph[i][j] = new Cell(weight, new Point(i, j));
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	public static void setInitialAndFinalSpaces() {
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
					}

					Color cPowerline = new Color(imgPowerline.getRGB(i, j));

					red = cPowerline.getRed();
					green = cPowerline.getGreen();
					blue = cPowerline.getBlue();

					if (red != 255 && green != 255 && blue != 255
							&& weightedGraph[i][j].getWeight() != Integer.MAX_VALUE) {
						finalSpaces.add(new Point(i, j));
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
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

	public static Cell[][] getWeightedGraph() {
		return weightedGraph;
	}

	public static ArrayList<Point> getInitialSpaces() {
		return initialSpaces;
	}

	public static ArrayList<Point> getFinalSpaces() {
		return finalSpaces;
	}

	public static void setWeightedGraph(Cell[][] weightedGraph) {
		Main.weightedGraph = weightedGraph;
	}

	public static void setInitialSpaces(ArrayList<Point> initialSpaces) {
		Main.initialSpaces = initialSpaces;
	}

	public static void setFinalSpaces(ArrayList<Point> finalSpaces) {
		Main.finalSpaces = finalSpaces;
	}

}
