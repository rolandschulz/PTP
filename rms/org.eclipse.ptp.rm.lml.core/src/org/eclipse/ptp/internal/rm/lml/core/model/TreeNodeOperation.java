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

/**
 * Implements an action executed on each tree node, which is traversed by
 * any tree traverser (such as LayoutTraverser).
 * 
 * @author karbach
 * 
 */
public interface TreeNodeOperation {

	/**
	 * Implements the command executed for each tree node.
	 * 
	 * @param treenode
	 *            the current tree node
	 */
	public void execute(Object treenode);

}
