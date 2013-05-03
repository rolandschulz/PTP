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

import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;

/**
 * Creates expanded trees from LML-data. The results are
 * Node-instances, which are abstract trees independent
 * from LML-data.
 */
public class TreeExpansion {

	/**
	 * If this attribute is set to true, all leaves are explicitly created by this class's generateUsagebarsForAllLeaves.
	 * Otherwise existing usagebars are not overridden.
	 */
	private final static boolean generateAllLeaves = true;

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

	/**
	 * Generate for each leaf within the LML-data-tree a usage-tag collecting
	 * all jobs running within the specific node. This function is needed for cutting
	 * the LML tree on an arbitrary level. All information from lower levels will be
	 * collected from each node's children and compressed in a usage-tag.
	 * This usage tag is then visualized by a usagebar within the nodedisplay.
	 * 
	 * @param node
	 *            the root node where to start the usage-tag generation.
	 */
	public static void generateUsagebarsForAllLeaves(Node<LMLNodeData> node) {
		final LMLNodeData lmlNodeData = node.getData();

		if (lmlNodeData.isRootNode()) {// Do not generate a usage-tag for the root node
			for (final Node<LMLNodeData> child : node.getChildren()) {
				TreeExpansion.generateUsagebarsForAllLeaves(child);
			}
			return;
		}

		if (lmlNodeData.isDataElementOnNodeLevel()) {
			// Do not generate usagetags, if the node is referencing a higher level data-tag
			if (node.getChildren().size() == 0) {
				// Is this node a leaf in the partly expanded tree?

				if (generateAllLeaves || lmlNodeData.getDataElement().getUsage() == null) {// Do I have to generate the usage-tag?
					final UsageType usage = lmlNodeData.generateUsage();
					// Do not set a usagebar with only one job collected and this job is also
					// The job referenced directly by the data-tag
					if (usage.getJob().size() == 1) {
						if (usage.getJob().get(0).getOid().equals(lmlNodeData.getDataElement().getOid())) {
							return;
						}
					}

					lmlNodeData.getDataElement().setUsage(usage);
				}
			}
			else {// Drill down the tree
				for (final Node<LMLNodeData> child : node.getChildren()) {
					TreeExpansion.generateUsagebarsForAllLeaves(child);
				}
			}
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
		final LMLNodeData rootData = new LMLNodeData(nodedisplay.getData(), nodedisplay.getScheme(), new ArrayList<Integer>(), ""); //$NON-NLS-1$
		final Node<LMLNodeData> root = new Node<LMLNodeData>(rootData);

		expandLMLNode(root, maxLevel);

		return root;
	}

	public Nodedisplay getNodedisplay() {
		return nodedisplay;
	}

}
