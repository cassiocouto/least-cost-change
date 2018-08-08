package main.auxiliar;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.imageio.ImageIO;

public class Maze {
	public static final int WALKING_VALUE = 1;
	public static final Color WALKING_VALUE_COLOR = new Color(155, 155, 155);
	public static final int BARRIER_VALUE = 10000;
	public static final Color BARRIER_VALUE_COLOR = new Color(0, 0, 0);
	public static final Color NEUTRAL_COLOR = new Color(255, 255, 255);

	private int height;
	private int width;
	private int[][] theMaze;

	private Point initialPoint;
	private Point finalPoint;

	public Maze(int height, int width) {
		if (height >= 0 && width >= 0) {
			this.height = height;
			this.width = width;
			initialPoint = new Point(0, 0);
			finalPoint = new Point(height - 1, width - 1);
			theMaze = new int[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					theMaze[i][j] = WALKING_VALUE;
				}
			}
		} else {
		}
	}

	public void setSpace(int i, int j, int value) {
		if (i >= 0 && i < height && j >= 0 && j < width) {
			this.theMaze[i][j] = value;
		}
	}

	public void createFiles() {
		try {
			BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			BufferedImage sourceImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			BufferedImage destinationImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (theMaze[j][i] == WALKING_VALUE) {
						finalImg.setRGB(i, j, WALKING_VALUE_COLOR.getRGB());
					} else if (theMaze[j][i] == BARRIER_VALUE) {
						finalImg.setRGB(i, j, BARRIER_VALUE_COLOR.getRGB());
					} else {
						finalImg.setRGB(i, j, NEUTRAL_COLOR.getRGB());
					}

					if (i == initialPoint.getX() && j == initialPoint.getY()) {
						sourceImg.setRGB(i, j, BARRIER_VALUE_COLOR.getRGB());
					} else {
						sourceImg.setRGB(i, j, NEUTRAL_COLOR.getRGB());
					}

					if (i == finalPoint.getX() && j == finalPoint.getY()) {
						destinationImg.setRGB(i, j, BARRIER_VALUE_COLOR.getRGB());
					} else {
						destinationImg.setRGB(i, j, NEUTRAL_COLOR.getRGB());
					}
				}
			}

			File csv = new File("res/maze.csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv));
			bw.write("r,g,b,w\n");
			bw.write(WALKING_VALUE_COLOR.getRed() + "," + WALKING_VALUE_COLOR.getGreen() + ","
					+ WALKING_VALUE_COLOR.getBlue() + "," + WALKING_VALUE + "\n");
			bw.write(BARRIER_VALUE_COLOR.getRed() + "," + BARRIER_VALUE_COLOR.getGreen() + ","
					+ BARRIER_VALUE_COLOR.getBlue() + "," + BARRIER_VALUE + "\n");
			bw.close();
			ImageIO.write(finalImg, "bmp", new File("res/maze_path.bmp"));
			ImageIO.write(sourceImg, "bmp", new File("res/maze_source.bmp"));
			ImageIO.write(destinationImg, "bmp", new File("res/maze_destination.bmp"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Point getInitialPoint() {
		return initialPoint;
	}

	public Point getFinalPoint() {
		return finalPoint;
	}
}
