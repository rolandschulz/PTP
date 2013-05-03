/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a tree, which allows every Node to have arbitrarily many
 * children. A Node without children is a leave. Every Node references a
 * data-instance. This data contains information connected to the node, which
 * might collect the name, a color and so on.
 * 
 * @param <T>
 *            Type of data placed in every node
 */
public class Node<T> {

	/**
	 * List with inner nodes
	 */
	private List<Node<T>> children;

	/**
	 * Data connected to this node
	 */
	private T data;

	/**
	 * Create an empty node without data-reference.
	 */
	public Node() {
		children = new ArrayList<Node<T>>();
	}

	/**
	 * Create a Node with data.
	 * 
	 * @param data
	 *            contains information connected to the node
	 */
	public Node(T data) {
		this();
		this.data = data;
	}

	/**
	 * Add a child to this node.
	 * 
	 * @param child
	 */
	public void add(Node<T> child) {
		children.add(child);
	}

	/**
	 * @return original list with all children, no copy
	 */
	public List<Node<T>> getChildren() {
		return children;
	}

	/**
	 * The call getChildrenCountTillLevel(infinity) would return
	 * the same result like getFullChildrenCount().
	 * 
	 * @param level
	 *            depth until the amount of children is searched
	 * @return amount of children in a depth of given level
	 */
	public int getChildrenCountTillLevel(int level) {
		if (level == 0) {
			return 0;
		}
		if (level == 1) {
			return children.size();
		}

		int count = children.size();

		for (final Node<T> child : children) {
			count += child.getChildrenCountTillLevel(level - 1);
		}

		return count;
	}

	/**
	 * @return connected data
	 */
	public T getData() {
		return data;
	}

	/**
	 * Count all children and the children's children.
	 * 
	 * @return full amount of child-nodes for this node
	 */
	public int getFullChildrenCount() {
		int count = children.size();

		for (final Node<T> child : children) {
			count += child.getFullChildrenCount();
		}

		return count;
	}

	/**
	 * Answer the question, of how deep this sub-tree is.
	 * 
	 * @return the highest amount of tree-levels, until all leaves are reached
	 */
	public int getLowerLevelCount() {
		if (children.size() == 0) {
			return 0;
		}
		else {
			int max = 0;
			for (final Node<T> child : children) {
				if (child.getLowerLevelCount() > max) {
					max = child.getLowerLevelCount();
				}
			}
			return max + 1;
		}
	}

	/**
	 * Remove a child
	 * 
	 * @param child
	 * @return true, if removing worked and element was available
	 */
	public boolean remove(Node<T> child) {
		return children.remove(child);
	}

	/**
	 * Set referenced data.
	 * 
	 * @param data
	 */
	public void setData(T data) {
		this.data = data;
	}

}
