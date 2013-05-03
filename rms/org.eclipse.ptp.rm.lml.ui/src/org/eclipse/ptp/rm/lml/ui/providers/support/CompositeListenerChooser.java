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

package org.eclipse.ptp.rm.lml.ui.providers.support;

import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;

/**
 * This is a helper class for the nodedisplay.
 * It chooses the amount of levels painted directly
 * by paintListeners instead of nesting more composites.
 * This class is used by the nodedisplay to estimate
 * when to stop nesting real composites.
 * 
 * Nesting composites should be chosen as long as possible,
 * because it allows to add pictures and titles to the paintings.
 * PaintListeners are much faster than nesting composites, but
 * more difficult to implement and thus less configurable.
 */
public class CompositeListenerChooser {

	/**
	 * Defines maximum allowed amount of nested composites in a nodedisplay.
	 * With this parameter levelsPaintedByPaintListener has to
	 * be adjusted to decide, which level is painted by listeners
	 * instead of further composite nesting.
	 * 
	 * This attribute affects the performance of the nodedisplay.
	 * A high amount of nested composites causes the nodedisplay to
	 * slow down. A low amount will cause a fast nodedisplay, which
	 * is less configurable through nodedisplay layouts, because it
	 * misses the options provided by SWT on the lower levels.
	 */
	private final int maxCompositeAmount = 100;

	/**
	 * Holds the amount of tree levels, which
	 * are painted in a nodedisplay in fast way, but less configurable.
	 * This fast painting is done by the rectpaintlistener.
	 */
	private int levelsPaintedByPaintListener = 0;

	/**
	 * Calculates the corresponding value for levelsPaintedByPaintListener.
	 * 
	 * @param rootNode
	 *            the node, for which calculation is done.
	 */
	public CompositeListenerChooser(Node<LMLNodeData> rootNode) {

		final int maxLevel = rootNode.getLowerLevelCount();
		// Go that deep that more than maxCompositeAmount composites
		// are needed to paint the tree till this level
		int compLevel = 1;

		while (compLevel <= maxLevel && rootNode.getChildrenCountTillLevel(compLevel) <= maxCompositeAmount) {
			compLevel++;
		}
		// compLevel is one level to deep, because on this level
		// there are already too many composites
		compLevel--;
		// The levels, which must be painted ny the listeners
		// are calculated here
		levelsPaintedByPaintListener = maxLevel - compLevel;
	}

	/**
	 * @return the amount of tree levels, which
	 *         are painted in a nodedisplay in fast way, but less configurable.
	 *         This fast painting is done by the rectpaintlistener.
	 */
	public int getLevelsPaintedByPaintListener() {
		return levelsPaintedByPaintListener;
	}
}
