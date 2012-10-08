package org.eclipse.ptp.rm.lml.internal.core.model;

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
