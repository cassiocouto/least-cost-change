package mase.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import mase.main.Main;

public class GRIDManager extends Agent {
	private static final long serialVersionUID = 1L;
	private Main main;

	public GRIDManager() {
		main = Main.getInstance();
	}

	public void setup() {
		main.setGRIDManagerAddress(getAID());
		HashMap<String, Agent> agents = new HashMap<String, Agent>();

		int index = main.getInitialSpaces().size() / main.getAgentsQuantity();
		for (int i = 0; i < main.getAgentsQuantity(); i++) {
			ArrayList<Point> subSet = new ArrayList<Point>();
			if (i != main.getAgentsQuantity() - 1) {
				subSet.addAll(0, main.getInitialSpaces().subList(i * index, (i + 1) * index));
			} else {
				subSet.addAll(0, main.getInitialSpaces().subList(i * index, main.getInitialSpaces().size()));
			}
			TrackerAgent a = new TrackerAgent(i, subSet, Main.getInstance().getFinalSpaces());
			String name = "tracker" + i;
			agents.put(name, a);
			agents.put(name, a);
		}
		SequentialBehaviour list = new SequentialBehaviour();
		list.addSubBehaviour(new AddAgents(agents));

		if (main.getChoosenStrategy() == Main.DIJKSTRA_STRATEGY) {
			list.addSubBehaviour(new WaitForProposals());
		} else {
			list.addSubBehaviour(new WaitForProposals());
		}
		addBehaviour(list);

	}

	class AddAgents extends OneShotBehaviour {
		private static final long serialVersionUID = 1L;
		private HashMap<String, Agent> agents;

		public AddAgents(HashMap<String, Agent> agents) {
			this.agents = agents;
		}

		public void action() {
			Set<String> keys = agents.keySet();
			for (String nickname : keys) {
				Agent a = agents.get(nickname);
				try {
					this.myAgent.getContainerController().acceptNewAgent(nickname, a);
					this.myAgent.getContainerController().getAgent(nickname).start();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				} catch (ControllerException e) {
					e.printStackTrace();
				}
			}

		}

	}

	class AddDelayedAgents extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;
		private HashMap<String, Agent> agents;
		private int agentNr;
		private long interval;

		public AddDelayedAgents(HashMap<String, Agent> agents, int agentNr, long interval) {
			this.agents = agents;
			this.agentNr = agentNr;
			this.interval = interval;
		}

		public void action() {
			Set<String> keys = agents.keySet();
			int count = 0;
			for (String nickname : keys) {
				Agent a = agents.get(nickname);
				count++;
				if (count % agentNr == 0) {
					count = 0;
					myAgent.doWait(interval);
				}
				try {
					this.myAgent.getContainerController().acceptNewAgent(nickname, a);
					this.myAgent.getContainerController().getAgent(nickname).start();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				} catch (ControllerException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class WaitForProposals extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
			int count = 0;
			long bestMinimumCost = Long.MAX_VALUE;
			AID bestProponentAddress = null;
			while (count < main.getAgentsQuantity()) {
				if (main.isDebug()) {
					System.out.println(myAgent + ": Waiting for proposals");
				}
				myAgent.doWait();
				ACLMessage m = myAgent.receive();

				long proposal = Long.parseLong(m.getContent());
				if (main.isDebug()) {
					System.out.println(myAgent.getLocalName() + ": Received proposal from "
							+ m.getSender().getLocalName() + ": " + proposal);
				}
				if (proposal < bestMinimumCost) {
					bestMinimumCost = proposal;
					bestProponentAddress = m.getSender();
				}
				count++;
			}

			for (int i = 0; i < main.getAgentsQuantity(); i++) {
				if (bestProponentAddress.equals(main.getAgentsAddresses()[i])) {
					ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					if (Main.getInstance().isDebug()) {
						System.out.println(myAgent.getLocalName() + ": Accepted proposal from "
								+ bestProponentAddress.getLocalName());
					}
					m.addReceiver(bestProponentAddress);
					myAgent.send(m);
				} else {
					if (Main.getInstance().isDebug()) {
						System.out.println(myAgent.getLocalName() + ": Refused proposal from "
								+ Main.getInstance().getAgentsAddresses()[i].getLocalName());
					}
					ACLMessage m = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
					m.addReceiver(Main.getInstance().getAgentsAddresses()[i]);
					myAgent.send(m);
				}
			}

			myAgent.doWait();
			ACLMessage m = myAgent.receive();
			long finishingTime = System.currentTimeMillis();
			ArrayList<ArrayList<Point>> foundPaths = new ArrayList<>();
			try {
				foundPaths.add((ArrayList<Point>) m.getContentObject());
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Agent quantity = " + Main.getInstance().getAgentsQuantity());
			System.out.println("Minimum cost = " + bestMinimumCost);
			System.out.println("Total time = " + (finishingTime - Main.getInstance().getStartingTime()));
			Main.getInstance().writeImage("teste.bmp", foundPaths);
			System.exit(0);

		}

	}

	private class WaitForPaths extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public void action() {
			int count = 0;
			int bestMinimumCost = Integer.MAX_VALUE;
			ArrayList<ArrayList<Point>> foundPaths = new ArrayList<ArrayList<Point>>();
			while (count < main.getAgentsQuantity()) {
				ACLMessage m = myAgent.blockingReceive();
				// int receivedbestCost = Integer.parseInt(m.getContent());
				ArrayList<ArrayList<Point>> paths;
				try {
					paths = (ArrayList<ArrayList<Point>>) m.getContentObject();
					foundPaths.addAll(paths);
					if (main.isDebug()) {
						System.out.println(
								myAgent.getLocalName() + ": Received paths from " + m.getSender().getLocalName());
					}
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				count++;
			}
			long finishingTime = System.currentTimeMillis();

			Main.getInstance().writeImage("teste.bmp", foundPaths);

			System.out.println("Agent quantity = " + Main.getInstance().getAgentsQuantity());
			System.out.println("Minimum cost = " + bestMinimumCost);
			System.out.println("Total time = " + (finishingTime - Main.getInstance().getStartingTime()));
			System.exit(0);

		}

	}
}
