package mase.util;

import java.util.ArrayList;

public class PriorityQueue {
	private ArrayList<Number> priority;
	private ArrayList<Object> value;

	public PriorityQueue() {
		priority = new ArrayList<Number>();
		value = new ArrayList<>();
	}

	public Object remove(int index) throws ArrayIndexOutOfBoundsException {
		if (index >= value.size())
			throw new ArrayIndexOutOfBoundsException();
		priority.remove(index);
		return value.remove(index);
	}

	public void add(Number elementPriority, Object element) {
		for (int i = 0; i < priority.size(); i++) {
			if (elementPriority.doubleValue() >= priority.get(i).doubleValue()) {
				continue;
			} else {
				priority.add(i, elementPriority);
				value.add(i, element);
			}
		}
	}
	public int size(){return priority.size();}
}
