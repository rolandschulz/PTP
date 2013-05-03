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
package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This class extends a NodedisplayComp and adds functions for
 * setting minimal sizes of painted rectangles. The rectangles'
 * sizes will not take arbitrary size, but stop minimizing on a
 * defined size. The additionally needed space has to be provided
 * by the surrounding composite. E.g. this can be achieved by
 * nesting the nodedisplay into a scrollpane.
 * 
 * 
 */
public class NodedisplayCompMinSize extends NodedisplayComp implements IMinSizeNodedisplay {

	/**
	 * Takes possible results of MinSizeCalculation
	 * 
	 */
	public static enum ResizeResult {
		SMALLER, BIGGER, EQUAL;
	}

	/**
	 * Default size of rectangles is pixel.
	 * Defines width and height of rectangles.
	 */
	public static final int defaultMinSize = 7;

	/**
	 * contains minimum size of a lowest level rectangle
	 */
	protected int minRectangleWidth = defaultMinSize, minRectangleHeight = defaultMinSize;

	/**
	 * Call this constructor for start, maxlevel is chosen from nodedisplaylayout.
	 * The pnode has to be expanded as much as desired before this constructor is
	 * called. The nodedisplay does not expand non-root nodes.
	 * 
	 * This constructor is designed for non-root nodes, which should appear
	 * as the root-node in the display. One could call this constructor for
	 * showing one rack of a supercomputer.
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param node
	 *            current node, which is root-data-element of this
	 *            NodedisplayComp
	 * @param layout
	 *            layout definition for this nodedisplay part
	 * @param parent
	 *            parent composite, in which this nodedisplay is located
	 * @param style
	 *            SWT Style
	 */
	public NodedisplayCompMinSize(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout, Composite parent, int style) {

		super(lguiItem, node, layout, parent, style);
	}

	/**
	 * easy constructor for a nodedisplay as root-node, which needs
	 * to have fixed minimal rectangle sizes.
	 * 
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param nodedisplay
	 *            lml-model for the nodedisplay, which should be shown in this
	 *            panel
	 * @param parent
	 *            parameter for calling super constructor
	 * @param style
	 *            parameter for calling super constructor
	 */
	public NodedisplayCompMinSize(ILguiItem lguiItem, Nodedisplay nodedisplay, Composite parent, int style) {

		this(lguiItem, new Node<LMLNodeData>(new LMLNodeData("", nodedisplay)), //$NON-NLS-1$
				// get the layout for this nodedisplay
				(lguiItem.getLayoutAccess().getLayoutForNodedisplay(nodedisplay.getId())).getEl0(),
				parent, style);
	}

	/**
	 * Call this constructor for inner or lower elements. It is not allowed
	 * to call this constructor from outside.
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param node
	 *            current node, which is root-data-element of this
	 *            NodedisplayComp
	 * @param layout
	 *            nodedisplay-layout part for this nodedisplay
	 * @param levelsPaintedByPaintListener
	 *            Holds the amount of tree levels, which
	 *            are painted in fast way, but less configurable.
	 *            This fast painting is done by the rectpaintlistener.
	 *            This parameter is currently only forwarded from the
	 *            root node to all of its children.
	 * @param parentNodedisplay
	 *            father of this nodedisplay
	 * @param x
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param y
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param parent
	 *            parent composite for SWT constructor, differs from pparentNodedisplay, because
	 *            parent is the innerPanel of pparentNodedisplay
	 * @param style
	 *            SWT-style of this nodedisplay
	 */
	protected NodedisplayCompMinSize(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout,
			int levelsPaintedByPaintListener, NodedisplayCompMinSize parentNodedisplay, int x, int y, Composite parent, int style) {

		super(lguiItem, node, layout, levelsPaintedByPaintListener, parentNodedisplay, x, y, parent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.IMinSizeNodedisplay#getMinimalSize()
	 */
	public Point getMinimalSize() {

		// Calculate space needed for decorations around the innerPanel
		// with the real rectangles inside
		final int panelDiffX = this.getSize().x - innerComp.getSize().x;
		final int panelDiffY = this.getSize().y - innerComp.getSize().y;

		Point point = null;

		if (innerCompsList.size() > 0) {// Are there inner composites

			final GridLayout gridLayout = (GridLayout) innerComp.getLayout();

			final int numColumns = gridLayout.numColumns;
			int rows = innerCompsList.size() / numColumns;
			if (innerCompsList.size() % numColumns != 0) {
				rows++;
			}
			// Search the biggest minimal height and width
			int maxHeight = 0;
			int maxWidth = 0;
			for (final NodedisplayComp innerComp : innerCompsList) {
				final NodedisplayCompMinSize minInnerComp = (NodedisplayCompMinSize) innerComp;
				final Point minPoint = minInnerComp.getMinimalSize();

				if (minPoint.y > maxHeight) {
					maxHeight = minPoint.y;
				}
				if (minPoint.x > maxWidth) {
					maxWidth = minPoint.x;
				}
			}

			point = new Point((maxWidth + gridLayout.horizontalSpacing) * numColumns, (maxHeight + gridLayout.verticalSpacing)
					* rows);
		}
		else if (rectanglePaintListener != null) {
			// Is there a rectpaintlistener
			point = rectanglePaintListener.getMinimalSize();
		}
		else {
			// None of them, just a rectangle as composite?
			point = new Point(minRectangleWidth, minRectangleHeight);
		}

		point.x += panelDiffX;
		point.y += panelDiffY;

		return point;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.IMinSizeNodedisplay#setMinimumRectangleHeight(int)
	 */
	public void setMinimumRectangleHeight(int height) {
		if (height > 0) {
			minRectangleHeight = height;

			for (final NodedisplayComp innerComp : innerCompsList) {
				final NodedisplayCompMinSize minComp = (NodedisplayCompMinSize) innerComp;

				minComp.setMinimumRectangleHeight(height);
			}

			if (rectanglePaintListener != null) {
				rectanglePaintListener.setMinimumRectangleHeight(height);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.IMinSizeNodedisplay#setMinimumRectangleWidth(int)
	 */
	public void setMinimumRectangleWidth(int width) {
		if (width > 0) {
			minRectangleWidth = width;

			for (final NodedisplayComp innerComp : innerCompsList) {
				final NodedisplayCompMinSize minComp = (NodedisplayCompMinSize) innerComp;

				minComp.setMinimumRectangleWidth(width);
			}

			if (rectanglePaintListener != null) {
				rectanglePaintListener.setMinimumRectangleWidth(width);
			}

		}
	}

	/**
	 * Generate a new NodedisplayCompMinSize with all parameters.
	 * This function is used in insertInnerPanel of super-class.
	 * 
	 * @return generated MinSizeNodedisplayComp, which can be exchanged by instances of sub-class
	 */
	@Override
	protected NodedisplayCompMinSize createNodedisplayComp(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout,
			int levelsPaintedByPaintListener, NodedisplayComp parentNodedisplay, int x, int y, Composite parent, int style) {

		return new NodedisplayCompMinSize(lguiItem, node, layout, levelsPaintedByPaintListener,
				(NodedisplayCompMinSize) parentNodedisplay, x, y, parent, style);
	}
}
