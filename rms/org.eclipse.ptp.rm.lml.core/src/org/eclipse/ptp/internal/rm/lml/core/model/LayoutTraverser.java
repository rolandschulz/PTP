/**
 * Copyright (c) 2012 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.NodedisplaylayoutType;

/**
 * Traverses a nodedisplay layout. Allows to execute command on each level
 * of the nodedisplay layout tree.
 * 
 * @author karbach
 * 
 */
public class LayoutTraverser {

	/**
	 * The layout, which is traversed.
	 */
	private final NodedisplaylayoutType layout;

	/**
	 * Creates a layout traverser by setting the layout object, which is traversed
	 * 
	 * @param layout
	 *            The layout, which is traversed
	 */
	public LayoutTraverser(NodedisplaylayoutType layout) {
		this.layout = layout;
	}

	/**
	 * Execute the given command on each treenode
	 * 
	 * @param operation
	 *            the executed command
	 */
	public void traverse(TreeNodeOperation operation) {
		traverse(layout, operation);
	}

	/**
	 * Recursive traversal of any sub tree of the layout
	 * 
	 * @param layoutPart
	 *            the root of the traversed part of the layout
	 * 
	 * @param operation
	 *            the operation, which has to be executed on each node
	 */
	protected void traverse(Object layoutPart, TreeNodeOperation operation) {
		operation.execute(layoutPart);
		final List<? extends Nodedisplayelement> children = LMLCheck.getLowerNodedisplayElements(layoutPart);

		for (final Object child : children) {
			traverse(child, operation);
		}
	}

}
