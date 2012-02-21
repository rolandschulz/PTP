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
package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;

/**
 * Creates expanded trees from LML-data. The results are
 * Node-instances, which are abstract trees independent
 * from LML-data.
 */
public class TreeExpansion {

	/**
	 * Expand a node to a given maximum level (maxlevel).
	 * This means to find all implicitly defined physical elements,
	 * which are children of the node, and to create child-nodes
	 * for them.
	 * 
	 * @param node
	 *            the node, which should be expanded
	 * @param maxLevel
	 *            the maximum expansion level
	 */
	public static void expandLMLNode(Node<LMLNodeData> node, int maxLevel) {
		final LMLNodeData data = node.getData();
		final int currentLevel = data.getLevelIds().size();
		final List<Node<LMLNodeData>> childrenNodes = node.getChildren();

		// is maximum level already reached?
		if (currentLevel >= maxLevel)
			return;

		childrenNodes.clear();
		final List<LMLNodeData> childrenData = data.getLowerNodes();

		for (final LMLNodeData nodeData : childrenData) {
			final Node<LMLNodeData> newNode = new Node<LMLNodeData>(nodeData);
			expandLMLNode(newNode, maxLevel);
			childrenNodes.add(newNode);
		}

	}

	private final Nodedisplay nodedisplay;

	/**
	 * Initialize private attributes.
	 * 
	 * @param nodedisplay
	 *            a lml-model
	 */
	public TreeExpansion(Nodedisplay nodedisplay) {
		this.nodedisplay = nodedisplay;
	}

	/**
	 * Start from root-node of the nodedisplay and expand the tree
	 * till maxlevel is reached.
	 * 
	 * @param maxLevel
	 * @return
	 */
	public Node<LMLNodeData> getFullTree(int maxLevel) {
		// There is no root data, which must be connected
		final LMLNodeData rootData = new LMLNodeData(nodedisplay.getData(), nodedisplay.getScheme(), new ArrayList<Integer>(), "");
		final Node<LMLNodeData> root = new Node<LMLNodeData>(rootData);

		expandLMLNode(root, maxLevel);

		return root;
	}

	public Nodedisplay getNodedisplay() {
		return nodedisplay;
	}

}
