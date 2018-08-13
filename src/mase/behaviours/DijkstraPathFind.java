package mase.behaviours;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import mase.main.Main;
import mase.util.PriorityQueue;

public class DijkstraPathFind extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;
	private boolean[][] visited;
	private Long[][] sum;
	private Point[][] parent;
	private Point initialSpace;
	private PriorityQueue actualSpaces;
	private PriorityQueue adjacentSpaces;
	private ArrayList<Point> initialSpaces;
	private ArrayList<Point> finalSpaces;
	private int height;
	private int width;

	private ArrayList<Point> pathFound;
	private long minimumCost = Long.MAX_VALUE;

	public DijkstraPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		this.height = height;
		this.width = width;
		this.initialSpaces = initialSpaces;
		this.finalSpaces = finalSpaces;
		parent = new Point[height][width];
	}

	public void action() {
		if (initialSpaces.size() == 0) {
			ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
			m.addReceiver(Main.getInstance().getGRIDManagerAddress());
			m.setContent(minimumCost + "");
			myAgent.send(m);
			myAgent.doWait();

			m = myAgent.receive();
			if (m.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				if (Main.getInstance().isDebug()) {
					System.out.println(myAgent.getLocalName() + ": My proposal won!");
				}
				m = new ACLMessage(ACLMessage.INFORM);
				m.addReceiver(Main.getInstance().getGRIDManagerAddress());
				try {
					m.setContentObject(pathFound);
					myAgent.send(m);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			myAgent.doDelete();

		} else {
			initialSpace = initialSpaces.remove(0);
			System.out.println("Trying space: " + initialSpace);
			sum = new Long[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					sum[i][j] = Long.MAX_VALUE;
				}
			}
			if (Main.getInstance().getGui() != null) {
				Main.getInstance().getGui().repaint2();
			}
			sum[initialSpace.x][initialSpace.y] = (long) Main.getInstance()
					.getWeightedGraph()[initialSpace.x][initialSpace.y].getWeight();

			visited = new boolean[height][width];

			actualSpaces = new PriorityQueue();
			adjacentSpaces = new PriorityQueue();

			actualSpaces.add(0, initialSpace);

			for (int aux = 0; !isGoalFound(visited); aux++) {
				Point actualSpace = (Point) actualSpaces.get(aux);
				if (visited[actualSpace.x][actualSpace.y]) {
					continue;
				}

				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {

						int nextX = (int) (actualSpace.x + i);
						int nextY = (int) (actualSpace.y + j); // if (nextY < 0 || nextY >= width)continue;

						try {
							if (visited[nextX][nextY]) {
								continue;
							}
							long tentative = Main.getInstance().getWeightedGraph()[nextX][nextY].getWeight()
									+ sum[actualSpace.x][actualSpace.y];
							if (tentative < sum[nextX][nextY]) {
								sum[nextX][nextY] = tentative;
								parent[nextX][nextY] = actualSpace;
								adjacentSpaces.add(tentative, new Point(nextX, nextY));
							}
						} catch (Exception e) {
						}

					}
				}

				visited[actualSpace.x][actualSpace.y] = true;
				actualSpaces.addAll(adjacentSpaces);
				adjacentSpaces = new PriorityQueue();

			}

			retrievePath();
		}
	}

	public boolean isGoalFound(boolean[][] visited) {
		for (Point finalSpace : finalSpaces) {
			if (!visited[finalSpace.x][finalSpace.y]) {
				return false;
			}
		}
		System.out.println("All exits found");
		return true;
	}

	public void retrievePath() {

		long currentMinimumSum = Long.MAX_VALUE;
		Point choosenFinalSpace = null;

		for (Point finalSpace : finalSpaces) {
			if (currentMinimumSum > sum[finalSpace.x][finalSpace.y]) {
				currentMinimumSum = sum[finalSpace.x][finalSpace.y];
				choosenFinalSpace = finalSpace;
			}
		}

		System.out.println("Minimum cost this round: " + currentMinimumSum);
		System.out.println("Global Minimum cost: " + minimumCost);
		if (minimumCost < currentMinimumSum) {
			return;
		} else {
			minimumCost = currentMinimumSum;
			pathFound = new ArrayList<Point>();
			Point actualPoint = choosenFinalSpace;
			while (actualPoint != null && !actualPoint.equals(initialSpace)) {
				pathFound.add(actualPoint);
				actualPoint = parent[actualPoint.x][actualPoint.y];
			}
			pathFound.add(actualPoint);
		}
	}

}
