package main.auxiliar;

public class MazeCreator {
	public static int height = 150;
	public static int width = 150;

	public static void main(String[] args) {
		Maze m = createMaze1();
		m.createFiles();
	}

	public static Maze createMaze1() {
		Maze m1 = new Maze(height, width);
		double chance = 1;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				if ((i == 0 || j == 0) || (i == m1.getInitialPoint().getX() && j == m1.getInitialPoint().getY())
						|| (i == m1.getFinalPoint().getX() && j == m1.getFinalPoint().getY())
						|| (i == height - 1 || j == width - 1)) {
					m1.setSpace(i, j, Maze.WALKING_VALUE);
					continue;

				}

				double r = Math.random();
				if (r <= chance) {
					m1.setSpace(i, j, Maze.BARRIER_VALUE);
				} else {
					m1.setSpace(i, j, Maze.WALKING_VALUE);
				}
			}
		}
		return m1;
	}

}
