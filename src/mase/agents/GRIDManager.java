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
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import mase.main.Main;

public class GRIDManager extends Agent {
	private static final long serialVersionUID = 1L;

	public GRIDManager() {

	}

	public void setup() {
		Main.GRIDManagerAddress = getAID();
		HashMap<String, Agent> agents = new HashMap<String, Agent>();

		int index = Main.getInitialSpaces().size() / Main.agentsQuantity;
		for (int i = 0; i < Main.agentsQuantity; i++) {
			ArrayList<Point> subSet = new ArrayList<Point>();
			if (i != Main.agentsQuantity - 1) {
				subSet.addAll(0, Main.getInitialSpaces().subList(i * index, (i + 1) * index));
			} else {
				subSet.addAll(0, Main.getInitialSpaces().subList(i * index, Main.getInitialSpaces().size()));
			}
			TrackerAgent a = new TrackerAgent(i, subSet);
			String name = "tracker" + i;
			agents.put(name, a);
			agents.put(name, a);
		}
		SequentialBehaviour list = new SequentialBehaviour();
		list.addSubBehaviour(new AddAgents(agents));

		if (Main.choosenStrategy == Main.DIJKSTRA_STRATEGY) {
			list.addSubBehaviour(new WaitForProposals());
		} else {

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

	private class WaitForProposals extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
			int count = 0;
			long bestMinimumCost = Long.MAX_VALUE;
			AID bestProponentAddress = null;
			while (count < Main.agentsQuantity) {
				if (Main.debug) {
					System.out.println(myAgent + ": Waiting for proposals");
				}
				myAgent.doWait();
				ACLMessage m = myAgent.receive();

				long proposal = Long.parseLong(m.getContent());
				if (Main.debug) {
					System.out.println(myAgent.getLocalName() + ": Received proposal from "
							+ m.getSender().getLocalName() + ": " + proposal);
				}
				if (proposal < bestMinimumCost) {
					bestMinimumCost = proposal;
					bestProponentAddress = m.getSender();
				}
				count++;
			}

			for (int i = 0; i < Main.agentsQuantity; i++) {
				if (bestProponentAddress.equals(Main.agentsAddresses[i])) {
					ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					if (Main.debug) {
						System.out.println(myAgent.getLocalName() + ": Accepted proposal from "
								+ bestProponentAddress.getLocalName());
					}
					m.addReceiver(bestProponentAddress);
					myAgent.send(m);
				} else {
					if (Main.debug) {
						System.out.println(myAgent.getLocalName() + ": Refused proposal from "
								+ Main.agentsAddresses[i].getLocalName());
					}
					ACLMessage m = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
					m.addReceiver(Main.agentsAddresses[i]);
					myAgent.send(m);
				}
			}

			myAgent.doWait();
			ACLMessage m = myAgent.receive();
			long finishingTime = System.currentTimeMillis();
			System.out.println("Agent quantity = " + Main.agentsQuantity);
			System.out.println("Minimum cost = " + bestMinimumCost);
			System.out.println("Total time = " + (finishingTime - Main.startingTime));
			System.exit(0);
			
		}

	}
}
