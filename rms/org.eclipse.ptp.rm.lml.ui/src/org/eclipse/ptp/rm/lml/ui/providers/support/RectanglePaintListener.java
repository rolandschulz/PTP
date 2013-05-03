/**
 * Copyright (c) 2011-2012 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers.support;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.internal.rm.lml.core.model.RowColumnSorter;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;
import org.eclipse.ptp.rm.lml.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.core.model.ObjectStatus;
import org.eclipse.ptp.rm.lml.ui.providers.NodedisplayComp;
import org.eclipse.ptp.rm.lml.ui.providers.NodedisplayCompMinSize;
import org.eclipse.ptp.rm.lml.ui.providers.support.UsagebarPainter.JobInterval;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * A listener, which paints Node<LMLNodeData>-instances within a panel. This is used for the lowest level
 * rectangles in a nodedisplay. It is faster to paint rectangles than using a gridlayout and inserting nodedisplay-
 * composites for every rectangle. As a result this implementation causes less general layouts
 * (for example: it is more difficult to react for cursor-focus on these rectangles,
 * texts as titles have to be painted
 * and can not be inserted by using a layout-manager)
 */
public class RectanglePaintListener implements PaintListener, JobDetector {

	/**
	 * nodes which are painted in the composite
	 */
	private final List<Node<LMLNodeData>> nodes;
	/**
	 * map for fast access to node-colors
	 */
	private final HashMap<Node<LMLNodeData>, Color> nodeToColor;
	/**
	 * map containing positions of nodes, they might change in
	 * every paint
	 */
	private final HashMap<Node<LMLNodeData>, Rectangle> nodeToRectangle;

	/**
	 * A map, which stores the found layout for each node.
	 * The layout configures the complete painting for a node.
	 */
	private HashMap<Node<LMLNodeData>, Nodedisplayelement> nodeToLayout;

	/**
	 * count of columns in the grid
	 */
	private int columnCount;
	/**
	 * The composite which is painted by this listener
	 */
	private final NodedisplayComp nodedisplayComp;

	/**
	 * space around the grid in x-direction
	 */
	public int marginWidth = 1;
	/**
	 * space around the grid in y-direction
	 */
	public int marginHeight = 1;

	/**
	 * Saves the maximum width accepted for double buffering. Otherwise
	 * it is switched of in order to workaround the SWT bug.
	 */
	private static final int maxWidthForDoubleBuffer = 30000;

	/**
	 * Minimal width of drawn rectangles
	 */
	public int minRectangleWidth = NodedisplayCompMinSize.defaultMinSize;
	/**
	 * Minimal height of drawn rectangles
	 */
	public int minRectangleHeight = NodedisplayCompMinSize.defaultMinSize;

	/**
	 * Parameters, which are changed when painting, they can be used by getNodeAtPos
	 */
	private int rowCount, rectangleWidth, rectangleHeight;

	/**
	 * area within the using composite, which is used for painting these nodes
	 */
	private Rectangle paintArea;

	/**
	 * All layout information needed to configure the painting
	 * in the same way like the NodedisplayComp is configured.
	 */
	private final Nodedisplayelement layout;

	/**
	 * Makes this class recursive like the nodedisplaycomp.
	 * For each node, which has children a new paintlistener
	 * is created, which handles the painting for this node.
	 */
	private HashMap<Node<LMLNodeData>, RectanglePaintListener> innerListener;

	/**
	 * Saves usagebar-painter for each node, to which a usage-tag is attached and which
	 * is on the lowest painting level (maxlevel).
	 */
	private HashMap<Node<LMLNodeData>, UsagebarPainter> usagebarsMap;

	/**
	 * Easy acces to LML handler objectstatus
	 */
	private final ObjectStatus objectStatus;

	/**
	 * LML handler for converting oid to corresponding ObjectType-instances
	 */
	private final OIDToObject oidToObject;

	/**
	 * The composite, on which this listener paints on
	 */
	private final Composite usingListener;

	/**
	 * Is true, if this listener was directly created by a nodedisplay.
	 * If the protected constructor was called to create this listener
	 * it is false.
	 */
	private final boolean rootListener;

	/**
	 * Image for double buffering this painter.
	 * It is only created for root listeners
	 */
	private Image doubleBuffer = null;

	/**
	 * Create the listener, initialize attributes and generate nodetocolor-map.
	 * The listener paints on the entire using composite.
	 * 
	 * @param nodes
	 *            nodes, which should be painted
	 * @param nodeComp
	 *            NodedisplayComp, in which this listener is used
	 * @param usingListener
	 *            Composite, which uses this listener. It might differ from the nodecomp
	 *            as the nodecomp nests other composites internally to paint the nodedisplay.
	 */
	public RectanglePaintListener(List<Node<LMLNodeData>> nodes, NodedisplayComp nodeComp, Composite usingListener) {
		this(nodes, nodeComp, nodeComp.getNodedisplayLayout(), usingListener, new Rectangle(0, 0, usingListener.getSize().x,
				usingListener.getSize().y), true);
	}

	/**
	 * Create the listener, initialize attributes and generate nodetocolor-map.
	 * The listener is only allowed to paint within the given paintArea.
	 * This constructor is used for inner listeners, which are childs of a
	 * surrounding paint listener.
	 * 
	 * @param nodes
	 *            nodes, which should be painted
	 * @param nodeComp
	 *            NodedisplayComp, in which this listener is used
	 * @param layout
	 *            Nodedisplay layout, which would be used for a nodedisplaycomp,
	 *            which displays the same tree part like this listener
	 * @param usingListener
	 *            Composite, which uses this listener
	 * @param paintArea
	 *            area within the pusingListener, which is used for painting these nodes
	 * @param rootListener
	 *            is true, when this is only a forwarded constructor call from the public constructor
	 */
	protected RectanglePaintListener(List<Node<LMLNodeData>> nodes, NodedisplayComp nodeComp, Nodedisplayelement layout,
			Composite usingListener, Rectangle paintArea, boolean rootListener) {
		this.paintArea = paintArea;
		this.nodes = nodes;
		this.layout = layout;
		columnCount = this.layout.getCols().intValue();
		this.nodedisplayComp = nodeComp;

		// Make columncount at least 1
		if (columnCount <= 0) {
			columnCount = 1;
		}

		oidToObject = this.nodedisplayComp.getLguiItem().getOIDToObject();

		nodeToColor = this.nodedisplayComp.generateNodeToColorMap(this.nodes);

		nodeToRectangle = new HashMap<Node<LMLNodeData>, Rectangle>();

		objectStatus = this.nodedisplayComp.getLguiItem().getObjectStatus();

		this.usingListener = usingListener;

		initInnerListener();

		this.rootListener = rootListener;

		updateRectangleSize();
	}

	@Override
	public void detectJobPositions(Set<Point> points, String jobId) {

		updateRectangleSize();

		for (int x = 0; x < columnCount; x++) {

			for (int y = 0; y < rowCount; y++) {
				detectPaintPositionsForChild(points, jobId, x, y);
			}

		}
	}

	/**
	 * Disposes all resources used by this painter, but not released so far
	 */
	public void dispose() {
		for (final RectanglePaintListener childPainter : this.innerListener.values()) {
			childPainter.dispose();
		}
		if (doubleBuffer != null) {
			doubleBuffer.dispose();
		}
	}

	/**
	 * This function is similar to <code>getNodeAtPos</code>. It is used in mouse-interactions.
	 * It returns the connected ObjectType-instance for the node, which includes the point (px,py)
	 * within this PaintListener's paintings. Mainly this function calls <code>getNodeAtPos</code> to retrieve the connected Node
	 * and its referenced ObjectType. This function also searches
	 * for the correct job in painted usagebars. If a usagebar is connected with the found node,
	 * the focussed job within this bar is returned. Usagebars display and save only ObjectType-
	 * instances, because they cannot refer to a specific tree-part.
	 * 
	 * @param x
	 *            x-position of cursor within the composite
	 * @param y
	 *            y-position of cursor within the composite
	 * @return the job, on which the cursor is positioned
	 */
	public ObjectType getJobAtPos(int x, int y) {
		return getJobAtPos(getNodeAtPos(x, y), x);
	}

	/**
	 * Part of <code>getJobAtPos(px,py)</code> call. Leaves the search of the
	 * node at the coordinates to external functions. This is needed for
	 * optimization: the call <code>getJobAtPos(px,py)</code> needs to search
	 * the node at first. If a function already knows the desired node, it is
	 * faster to call this function, because the search does not have to be repeated.
	 * 
	 * @param node
	 *            the node, which might be connected to a usagebar
	 * @param x
	 *            the x-coordinate within this rectPaintListener
	 * @return the job painted within this node at position px or null, if there is no job
	 */
	public ObjectType getJobAtPos(Node<LMLNodeData> node, int x) {
		if (node == null) {
			return null;
		}

		final UsagebarPainter usage = getUsagebarConnectedToNode(node);
		// Is there a usagebar for this node?
		if (usage != null) {
			return usage.getJobAtPosition(x);
		}
		else {
			return nodedisplayComp.getLguiItem().getOIDToObject().getObjectByLMLNode(node.getData());
		}
	}

	/**
	 * @return minimal composite-size to show rectangles with minRectWidth and minRectHeight
	 */
	public Point getMinimalSize() {
		final int horizontalSpacing = layout.getHgap().intValue();
		final int verticalSpacing = layout.getVgap().intValue();

		int rectWidth = minRectangleWidth;
		int rectHeight = minRectangleHeight;
		// Calculate inner sizes, search for biggest inner rectangle
		// set rectWidth and rectHeight to maximum, thus each rectangle
		// has enough space
		if (innerListener.size() > 0) {

			for (final RectanglePaintListener listener : innerListener.values()) {
				final Point minSize = listener.getMinimalSize();
				if (minSize.x > rectWidth) {
					rectWidth = minSize.x;
				}
				if (minSize.y > rectHeight) {
					rectHeight = minSize.y;
				}
			}

		}

		return new Point((rectWidth + horizontalSpacing) * columnCount + 2 * marginWidth, (rectHeight + verticalSpacing) * rowCount
				+ 2 * marginHeight);
	}

	/**
	 * Pass a relative mouse-position on the composite, which uses this listener.
	 * Then the node on focus will be returned. If no node is focussed
	 * null is returned.
	 * 
	 * @param xCursor
	 *            x-position of cursor within the composite
	 * @param yCursor
	 *            y-position of cursor within the composite
	 * @return focussed node or null, if nothing is focussed
	 */
	public Node<LMLNodeData> getNodeAtPos(int xCursor, int yCursor) {

		final int x = xCursor - marginWidth - paintArea.x;
		final int y = yCursor - marginHeight - paintArea.y;

		if (x < 0 || y < 0)
		{
			return null;// Outside grid, left or top
		}

		if (rectangleWidth == 0 || rectangleHeight == 0) {
			return null;
		}

		final int column = x / rectangleWidth;
		final int row = y / rectangleHeight;

		if (column >= columnCount || row >= rowCount) {
			return null;
		}

		final int index = row * columnCount + column;

		if (index >= nodes.size()) {
			return null;
		}
		// Do we have to search in a deeper level?
		final Node<LMLNodeData> connNode = nodes.get(index);
		if (innerListener.containsKey(connNode)) {
			return innerListener.get(connNode).getNodeAtPos(xCursor, yCursor);
		}
		else {
			return connNode;
		}
	}

	/**
	 * Checks if there is a usagebar for the given node within this paintlistener.
	 * Returns the corresponding usagebar painter. This painter can be managed directly
	 * within this RectanglePaintListener or its children.
	 * 
	 * @param node
	 *            the investigated node within this listener or nested
	 *            listeners
	 * @return the usagebarpainter connected to the node or null, if there is no usagebar connected
	 */
	public UsagebarPainter getUsagebarConnectedToNode(Node<LMLNodeData> node) {
		// Is this node directly painted by this listener
		if (nodes.contains(node)) {
			return usagebarsMap.get(node);
		}
		else { // Search for the node in inner listeners
			for (final RectanglePaintListener currentListener : innerListener.values()) {
				final UsagebarPainter usagebar = currentListener.getUsagebarConnectedToNode(node);
				if (usagebar != null) {
					return usagebar;
				}
			}
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	public void paintControl(PaintEvent event) {
		// Avoid parallel disposal of image resources
		synchronized (this) {
			updateRectangleSize();

			// Do not paint, if rectangle is invisible
			if (!isPaintAreaVisible()) {
				return;
			}
			GC eventGC = null;
			GC doubleBufGC = null;

			if (doDoubleBuffering()) {
				// Init double buffering
				eventGC = event.gc;
				doubleBufGC = new GC(doubleBuffer);
				doubleBufGC.setForeground(eventGC.getForeground());
				doubleBufGC.setFont(eventGC.getFont());
				event.gc = doubleBufGC;
			}

			// Paint background color
			final Color backgroundColor = ColorConversion.getColor(LMLColor.stringToColor(layout.getBackground()));

			event.gc.setBackground(backgroundColor);
			event.gc.fillRectangle(paintArea);

			for (int x = 0; x < columnCount; x++) {

				for (int y = 0; y < rowCount; y++) {
					paintChild(event, x, y);
				}

			}

			if (doDoubleBuffering()) {
				// Finish double buffering
				if (eventGC != null) {
					eventGC.drawImage(doubleBuffer, 0, 0);
				}
				if (doubleBufGC != null) {
					doubleBufGC.dispose();
				}
			}

		}
	}

	/**
	 * Forces the rectpaintlisteners to paint their rectangles at least height pixels
	 * high.
	 * 
	 * @param height
	 *            minimum height of lowest level rectangles in pixels
	 */
	public void setMinimumRectangleHeight(int height) {
		minRectangleHeight = height;

		for (final RectanglePaintListener inner : innerListener.values()) {
			inner.setMinimumRectangleHeight(height);
		}
	}

	/**
	 * Forces the rectpaintlisteners to paint their rectangles at least width pixels
	 * wide.
	 * 
	 * @param width
	 *            minimum width of lowest level rectangles in pixels
	 */
	public void setMinimumRectangleWidth(int width) {
		minRectangleWidth = width;

		for (final RectanglePaintListener inner : innerListener.values()) {
			inner.setMinimumRectangleWidth(width);
		}
	}

	/**
	 * Set minimum rectangle size. Set min-width and
	 * min-height to size.
	 * 
	 * @param size
	 *            size in pixels
	 */
	public void setMinRectangleSize(int size) {
		setMinimumRectangleHeight(size);
		setMinimumRectangleWidth(size);
	}

	/**
	 * Define a new area for this listener to paint on.
	 * This function should be called, when the nodedisplay
	 * resizes.
	 * 
	 * @param paintArea
	 *            new area to paint on
	 */
	public void updatePaintArea(Rectangle paintArea) {
		this.paintArea = paintArea;
	}

	/**
	 * Create recursively inner listeners, if
	 * the painted nodes also have children.
	 * Search and save layouts for all child nodes.
	 */
	private void initInnerListener() {
		innerListener = new HashMap<Node<LMLNodeData>, RectanglePaintListener>();
		nodeToLayout = new HashMap<Node<LMLNodeData>, Nodedisplayelement>();
		usagebarsMap = new HashMap<Node<LMLNodeData>, UsagebarPainter>();

		for (int index = 0; index < nodes.size(); index++) {
			final Node<LMLNodeData> node = nodes.get(index);
			final Nodedisplayelement layout = nodedisplayComp.findLayout(node.getData());

			nodeToLayout.put(node, layout);

			if (node.getLowerLevelCount() > 0) {

				final List<Node<LMLNodeData>> reorderedChildren = (new RowColumnSorter<Node<LMLNodeData>>(node.getChildren()))
						.reorder(layout.isHighestrowfirst(), layout.isHighestcolfirst(), layout.getCols().intValue());

				final RectanglePaintListener newListener = new RectanglePaintListener(reorderedChildren, nodedisplayComp, layout,
						usingListener, getRectangleForNode(index), false);
				innerListener.put(node, newListener);
			}
			else if (node.getData().getDataElement().getUsage() != null) {
				if (node.getData().isDataElementOnNodeLevel()) {
					final UsagebarPainter usagebarPainter = new UsagebarPainter(nodedisplayComp.getLguiItem(), node.getData()
							.getDataElement().getUsage(), getRectangleForNode(index));
					usagebarPainter.setPaintScale(false);
					usagebarPainter.setBarFactor(1);
					usagebarPainter.setStandardFrame(layout.getBorder().intValue());
					usagebarPainter.setMouseOverFrame(layout.getMouseborder().intValue());
					usagebarsMap.put(node, usagebarPainter);
				}
			}
		}

	}

	/**
	 * Check if the paint area of this rectpaintlistener is
	 * visible to the user on his GUI at all.
	 * This function can be used for clipping painting of not visible
	 * rectangles.
	 * 
	 * @return <code>true</code> if any part of the paintArea is visible
	 */
	private boolean isPaintAreaVisible() {
		final Rectangle bounds = NodedisplayComp.getNextNodedisplayBounds(usingListener);

		final Rectangle areaInNodedisplayComp = new Rectangle(bounds.x + paintArea.x, bounds.y + paintArea.y, paintArea.width,
				paintArea.height);

		return nodedisplayComp.isRectangleVisible(areaInNodedisplayComp);
	}

	/**
	 * Paints one inner rectangle, which represents
	 * one node, which is painted by this listener.
	 * 
	 * @param event
	 *            paint-event for accessing graphics context
	 * @param x
	 *            x-coordinate within the nodes' grid
	 * @param y
	 *            y-coordinate within the nodes' grid
	 * @return <code>true</code> if child cannot be painted, because it does not exist in the nodes' list
	 */
	private boolean paintChild(PaintEvent event, int x, int y) {
		final Node<LMLNodeData> node = getNodeByIndexPosition(x, y);
		if (node == null) {
			return true;
		}

		final Nodedisplayelement currentLayout = nodeToLayout.get(node);

		// Rectangle frame
		final Rectangle rectangle = getPaintAreaForNodePosition(x, y);

		final Color borderColor = ColorConversion.getColor(LMLColor.stringToColor(currentLayout.getBordercolor()));
		// Paint outer rectangle
		event.gc.setBackground(borderColor);
		event.gc.fillRectangle(rectangle);

		if (!innerListener.containsKey(node)) {
			// Is this node on the lowest level?
			paintLowestLevelChild(event, node, currentLayout, rectangle);
		}
		else {// Node has children and must be painted recursively
			final Rectangle decreasedRectangle = getInnerPaintAreaForRecursiveListener(rectangle, currentLayout);
			paintHigherLevelChild(event, node, decreasedRectangle);
		}
		nodeToRectangle.put(node, rectangle);// save the current rectangle

		return false;
	}

	/**
	 * Paints nodes, which have inner nodes, which
	 * will be painted by inner listeners.
	 * 
	 * @param event
	 *            provides paint environment
	 * @param node
	 *            the node, which has to be painted
	 * @param rectangle
	 *            values for the rectangle, in which this node is painted
	 */
	private void paintHigherLevelChild(PaintEvent event, Node<LMLNodeData> node, Rectangle rectangle) {
		final RectanglePaintListener rectanglePaintListener = innerListener.get(node);
		rectanglePaintListener.updatePaintArea(rectangle);

		rectanglePaintListener.paintControl(event);
	}

	/**
	 * Paint a rectangle completely. There is no
	 * inner listener, which will paint on this
	 * area later. This method takes care of the lowest
	 * level nodes.
	 * 
	 * @param event
	 *            environment for painting
	 * @param node
	 *            painted node
	 * @param currentLayout
	 *            corresponding layout for paint configuration
	 * @param rectangle
	 *            values for the rectangle
	 */
	private void paintLowestLevelChild(PaintEvent event, Node<LMLNodeData> node, Nodedisplayelement currentLayout,
			Rectangle rectangle) {
		// Paint this node by a usagebar?
		if (usagebarsMap.containsKey(node)) {
			final UsagebarPainter usagebarPainter = usagebarsMap.get(node);
			usagebarPainter.updatePaintArea(rectangle);
			usagebarPainter.paintControl(event);
			return;
		}

		// Paint it
		if (objectStatus.isAnyMouseDown() && !objectStatus.isMouseDown(oidToObject.getObjectByLMLNode(node.getData()))) {
			// Change color
			event.gc.setBackground(ColorConversion.getColor(nodedisplayComp.getLguiItem().getOIDToObject().getColorById(null)));
		} else {
			event.gc.setBackground(nodeToColor.get(node));
		}

		final Rectangle innerRec = getLowestLevelFillRectangle(node, currentLayout, rectangle);

		event.gc.fillRectangle(innerRec);
	}

	/**
	 * Detect job positions for a node painted by a recursive painter.
	 * 
	 * @param points
	 *            contains detected job positions
	 * @param jobId
	 *            searched job ID
	 * @param node
	 *            the node painted by the corresponding paint function
	 * @param rectangle
	 *            the sub area, in which this node is painted
	 */
	protected void detectHigherLevelChild(Set<Point> points, String jobId, Node<LMLNodeData> node, Rectangle rectangle) {
		final RectanglePaintListener rectanglePaintListener = innerListener.get(node);
		rectanglePaintListener.updatePaintArea(rectangle);

		rectanglePaintListener.detectJobPositions(points, jobId);
	}

	/**
	 * Detect jobs, which can be painted by the lowestLevelChild paint function.
	 * 
	 * @param points
	 *            contains detected job positions
	 * @param jobId
	 *            searched job ID
	 * @param node
	 *            the node painted by the corresponding paint function
	 * @param currentLayout
	 *            layout for this node painting
	 * @param rectangle
	 *            the sub area, in which this node is painted
	 */
	protected void detectLowestLevelChild(Set<Point> points, String jobId, Node<LMLNodeData> node,
			Nodedisplayelement currentLayout, Rectangle rectangle) {
		// Paint this node by a usagebar?
		if (usagebarsMap.containsKey(node)) {
			final UsagebarPainter usagebarPainter = usagebarsMap.get(node);
			usagebarPainter.updatePaintArea(rectangle);
			usagebarPainter.detectJobPositions();

			final List<JobInterval> jobintervals = usagebarPainter.getJobIntervals();
			for (final JobInterval interval : jobintervals) {
				if (interval.job.getId().equals(jobId)) {
					// Detect this position in the ScrolledComposite
					final Rectangle subPaintArea = NodedisplayComp.getNextNodedisplayBounds(usingListener);
					final Point jobPoint = nodedisplayComp.getPositionInScrollComp(
							new Point(rectangle.x + subPaintArea.x + interval.start, rectangle.y + subPaintArea.y));
					points.add(jobPoint);
				}
			}

			return;
		}

		final Rectangle innerRec = getLowestLevelFillRectangle(node, currentLayout, rectangle);

		if (node.getData() != null && node.getData().getDataElement() != null && node.getData().getDataElement().getOid() != null) {
			if (node.getData().getDataElement().getOid().equals(jobId)) {
				final Rectangle subPaintArea = NodedisplayComp.getNextNodedisplayBounds(usingListener);
				final Point jobPoint = nodedisplayComp.getPositionInScrollComp(new Point(subPaintArea.x + innerRec.x,
						subPaintArea.y + innerRec.y));
				points.add(jobPoint);
			}
		}
	}

	/**
	 * Detect all painted rectangles for the given job ID.
	 * 
	 * @param points
	 *            result set of job positions
	 * @param jobId
	 *            searched job
	 * @param x
	 *            x-index of painted child
	 * @param y
	 *            y-index of painted child
	 */
	protected void detectPaintPositionsForChild(Set<Point> points, String jobId, int x, int y) {
		final Node<LMLNodeData> node = getNodeByIndexPosition(x, y);
		if (node == null) {
			return;
		}
		final Nodedisplayelement currentLayout = nodeToLayout.get(node);

		// Rectangle frame
		final Rectangle rectangle = getPaintAreaForNodePosition(x, y);

		if (!innerListener.containsKey(node)) {
			// Is this node on the lowest level?
			detectLowestLevelChild(points, jobId, node, currentLayout, rectangle);
		}
		else {// Node has children and must be painted recursively
			final Rectangle decreasedRectangle = getInnerPaintAreaForRecursiveListener(rectangle, currentLayout);

			detectHigherLevelChild(points, jobId, node, decreasedRectangle);
		}
	}

	/**
	 * @return true, if double buffering should be enabled, false otherwise
	 */
	protected boolean doDoubleBuffering() {
		return rootListener && doubleBuffer != null &&
				doubleBuffer.getBounds().width < maxWidthForDoubleBuffer
				&& doubleBuffer.getBounds().height < maxWidthForDoubleBuffer;
	}

	/**
	 * @param outer
	 *            the rectangle calculated inclusive bordersizes
	 * @param layout
	 *            the layout definition for the corresponding node
	 * @return the rectangle area available to recursive rectanglepaintlisteners painting a sub area of this listener's area
	 */
	protected Rectangle getInnerPaintAreaForRecursiveListener(Rectangle outer, Nodedisplayelement layout) {
		// Decrease inner rectangle size with regard to current border size
		final int borderSize = layout.getBorder().intValue();
		final Rectangle decreasedRectangle = new Rectangle(outer.x + borderSize, outer.y + borderSize, outer.width
				- 2 * borderSize, outer.height - 2 * borderSize);
		return decreasedRectangle;
	}

	/**
	 * @param node
	 *            the node, which is painted by the filled rectangle
	 * @param currentLayout
	 *            the layout for this node
	 * @param rectangle
	 *            the outer rectangle for this node inclusive border
	 * @return the inner rectangle colored by the identifying job color
	 */
	protected Rectangle getLowestLevelFillRectangle(Node<LMLNodeData> node, Nodedisplayelement currentLayout, Rectangle rectangle) {
		int border = currentLayout.getBorder().intValue();

		if (objectStatus.isMouseOver(oidToObject.getObjectByLMLNode(node.getData()))) {
			border = currentLayout.getMouseborder().intValue();
		}
		// Paint at least one pixel rectangles in the right color
		int innerWidth = rectangle.width - 2 * border;
		int innerHeight = rectangle.height - 2 * border;
		int x = rectangle.x + border;
		int y = rectangle.y + border;

		if (innerWidth <= 0) {
			innerWidth = 1;
			x = rectangle.x;
		}
		if (innerHeight <= 0) {
			innerHeight = 1;
			y = rectangle.y;
		}

		return new Rectangle(x, y, innerWidth, innerHeight);
	}

	/**
	 * @param x
	 *            index in 2d node array in x direction
	 * @param y
	 *            index in 2d node array in y direction
	 * @return null, if there is no node with the given indices or the corresponding node data
	 */
	protected Node<LMLNodeData> getNodeByIndexPosition(int x, int y) {
		final int index = y * columnCount + x;
		if (index >= nodes.size()) {
			return null;
		}

		return nodes.get(index);
	}

	/**
	 * Determine the sub area for a node painted by this listener.
	 * 
	 * @param x
	 *            index in 2d node array in x direction
	 * @param y
	 *            index in 2d node array in y direction
	 * @return rectangle where to paint this node
	 */
	protected Rectangle getPaintAreaForNodePosition(int x, int y) {
		final int horizontalSpacing = layout.getHgap().intValue();
		final int verticalSpacing = layout.getVgap().intValue();

		// Rectangle frame
		final Rectangle rectangle = new Rectangle(marginWidth + rectangleWidth * x + paintArea.x, marginHeight + rectangleHeight
				* y + paintArea.y, rectangleWidth - horizontalSpacing, rectangleHeight - verticalSpacing);
		return rectangle;
	}

	/**
	 * Find a rectangle, in which a direct child node is painted.
	 * This function only handles nodes, which can be found
	 * in the nodes-list.
	 * 
	 * @param childNodeIndex
	 *            index in nodes-list of direct child node, which is painted by this listener
	 * @return Rectangle, in which the passed childNode identified by its index will be painted
	 */
	protected Rectangle getRectangleForNode(int childNodeIndex) {

		if (childNodeIndex >= nodes.size()) {
			return null;
		}

		// Calculate row and column for this node
		final int x = childNodeIndex % columnCount;
		final int y = childNodeIndex / columnCount;

		final int horizontalSpacing = layout.getHgap().intValue();
		final int verticalSpacing = layout.getVgap().intValue();

		final Rectangle result = new Rectangle(marginWidth + rectangleWidth * x, marginHeight + rectangleHeight * y, rectangleWidth
				- horizontalSpacing, rectangleHeight - verticalSpacing);

		return result;
	}

	/**
	 * Generate size of drawn rectangles.
	 * 
	 * @return size of painted rectangles
	 */
	protected void updateRectangleSize() {

		// Get size of surrounding Nodedisplay if this listener
		// was directly created by a nodedisplay => uses entire space of usingListener-
		// component
		if (rootListener) {
			final Point nodedisplaySize = usingListener.getSize();
			updatePaintArea(new Rectangle(0, 0, nodedisplaySize.x, nodedisplaySize.y));
			final Rectangle subPaintArea = NodedisplayComp.getNextNodedisplayBounds(usingListener);
			int imgWidth = subPaintArea.width;
			int imgHeight = subPaintArea.height;
			if (imgWidth == 0) {
				imgWidth = 1;
			}
			if (imgHeight == 0) {
				imgHeight = 1;
			}
			if (doubleBuffer == null ||
					doubleBuffer.getBounds().width != imgWidth ||
					doubleBuffer.getBounds().height != imgHeight || doubleBuffer.isDisposed()) {
				// Delete the old image if necessary
				if (doubleBuffer != null) {
					doubleBuffer.dispose();
				}
				doubleBuffer = new Image(nodedisplayComp.getDisplay(), imgWidth, imgHeight);
			}
		}

		int width, height;

		final Point size = new Point(paintArea.width, paintArea.height);
		// Generate available size
		width = size.x - marginWidth * 2;
		height = size.y - marginHeight * 2;

		if (width < 0) {
			width = 0;
		}
		if (height < 0) {
			height = 0;
		}

		if (columnCount <= 0) {
			columnCount = 1;
		}

		// Calculate how many rows have to be painted
		rowCount = nodes.size() / columnCount;
		if (nodes.size() % columnCount != 0) {
			rowCount++;
		}

		if (rowCount == 0) {
			return;
		}

		rectangleWidth = width / columnCount;

		rectangleHeight = height / rowCount;
	}

}
