/**
 * Copyright (c) 2011-2012 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.providers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.ptp.internal.rm.lml.core.events.NodedisplayZoomEvent;
import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.internal.rm.lml.core.model.RowColumnSorter;
import org.eclipse.ptp.internal.rm.lml.core.model.TreeExpansion;
import org.eclipse.ptp.rm.lml.core.elements.AlignType;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.elements.PictureType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;
import org.eclipse.ptp.rm.lml.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.internal.ui.LMLUIPlugin;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout.BorderData;
import org.eclipse.ptp.rm.lml.ui.providers.support.ColorConversion;
import org.eclipse.ptp.rm.lml.ui.providers.support.CompositeListenerChooser;
import org.eclipse.ptp.rm.lml.ui.providers.support.JobDetector;
import org.eclipse.ptp.rm.lml.ui.providers.support.MouseInteraction;
import org.eclipse.ptp.rm.lml.ui.providers.support.RectanglePaintListener;
import org.eclipse.ptp.rm.lml.ui.providers.support.UsagebarPainter.JobInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * 
 * This is a composite for creating output of a nodedisplay.
 * 
 * Inner composites are NodedisplayComp again (recursive).
 * 
 * A NodedisplayComp represents one physical element within the nodedisplay-tag. This might be a row, midplane, node or cpu. This
 * composite visualizes a {@code Node<LMLNodeData>}. It takes this node and paints it in exactly the way the tree --given by the
 * Node-- is expanded. Every visible component in the tree passed to this instance is shown by a NodedisplayComp.
 * 
 * The look of the nodedisplay is defined by the lml-Nodedisplay-Layout. For a non-root nodedisplay lower elements within the
 * nodedisplay-layout are searched.
 * 
 */
public class NodedisplayComp extends LguiWidget implements Updatable, JobDetector {

	/**
	 * This class is a workaround for a SWT-bug. The mouselistener callbacks receive negative coordinates as cursor positions, if
	 * the window is too large. This seems to be the result of an integer overflow. Thus this listener will use the last coordinates
	 * passed to the mousemove function as coordinates in the mousedown function.
	 * 
	 * 
	 */
	private class MouseMoveAndDownListener implements MouseListener, MouseMoveListener {

		/**
		 * Saves the last coordinates passed to the mouseMove-function.
		 */
		private int lastX = 0, lastY = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent e) {
			// Use the last coordinates from the mouseMove action
			final Node<LMLNodeData> focussed = rectanglePaintListener.getNodeAtPos(lastX, lastY);

			if (rectanglePaintListener.getUsagebarConnectedToNode(focussed) != null) {
				mouseInteraction.mouseDownAction(rectanglePaintListener.getJobAtPos(focussed, lastX));
			} else {
				mouseInteraction.mouseDownAction(focussed);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseMove(MouseEvent e) {
			lastX = e.x;
			lastY = e.y;
			final Node<LMLNodeData> focussed = rectanglePaintListener.getNodeAtPos(lastX, lastY);

			if (rectanglePaintListener.getUsagebarConnectedToNode(focussed) != null) {
				mouseInteraction.mouseMoveAction(rectanglePaintListener.getJobAtPos(focussed, lastX));
			} else {
				mouseInteraction.mouseMoveAction(focussed);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e) {
			// Use the last coordinates from the mouseMove action
			final Node<LMLNodeData> focussed = rectanglePaintListener.getNodeAtPos(lastX, lastY);

			if (rectanglePaintListener.getUsagebarConnectedToNode(focussed) != null) {
				mouseInteraction.mouseUpAction(rectanglePaintListener.getJobAtPos(focussed, lastX));
			} else {
				mouseInteraction.mouseUpAction(focussed);
			}
		}

	}

	/**
	 * Creates a new GridData-instance every time this function is called.
	 * 
	 * @return default GridData instance, which simulates Swing-behaviour of gridlayout
	 */
	public static GridData getDefaultGridData() {
		return new GridData(GridData.FILL, GridData.FILL, true, true);
	}

	/**
	 * Generates the bounds of the inner composite relative to the surrounding nodedisplay.
	 * 
	 * @return bounds of the inner composite relative to surrounding nodedisplaycomp
	 */
	public static Rectangle getNextNodedisplayBounds(Composite innerComp) {

		final Rectangle bounds = innerComp.getBounds();

		Composite currentComp = innerComp;

		while (currentComp.getParent() != null && !(currentComp.getParent() instanceof NodedisplayComp)) {
			currentComp = currentComp.getParent();

			final Rectangle newBounds = currentComp.getBounds();
			bounds.x += newBounds.x;
			bounds.y += newBounds.y;
		}

		return bounds;
	}

	// TODO insert levelsPaintedByPaintListener into LML nodedisplaylayout
	// This will allow individual performance configuration
	/**
	 * Holds the amount of tree levels, which are painted in fast way, but less configurable. This fast painting is done by the
	 * rectpaintlistener.
	 */
	private int levelsPaintedByPaintListener = 2;

	/**
	 * implicit name of this node
	 */
	private String title;

	/**
	 * Color of the job, to which this panel is connected
	 */
	private Color jobColor;

	/**
	 * Stores the current font used in title label and later used in other UI-elements, which contain characters
	 */
	private Font fontObject;

	/**
	 * Node-model which has to be displayed, contains partly expanded LML-tree
	 */
	private Node<LMLNodeData> node;

	/**
	 * this panel contains pictures as direct children and the mainPanel in center
	 */
	private Composite pictureComp;

	/**
	 * Important panel where everything but pictures is in
	 */
	private Composite mainComp;

	/**
	 * Has gridlayout and all lower nodedisplays as children
	 */
	protected Composite innerComp = null;

	/**
	 * Holds a usagebar-composite, if this nodedisplay is the lowest level composite and a usage-tag is provided by the nodedisplay.
	 */
	protected Usagebar usagebar;

	/**
	 * Instance handling all types of mouse actions. Creates tooltip.
	 */
	protected MouseInteraction mouseInteraction;

	/**
	 * For title-line
	 */
	private Label titleLabel;
	/**
	 * Settings for lower level provided by the corresponding layout.
	 */
	private Nodedisplayelement nodedisplayLayout;

	/**
	 * Current backgroundcolor
	 */
	private Color backgroundColor;

	/**
	 * Background color of title label
	 */
	private Color titleBackgroundColor;

	/**
	 * The only frame which has a border, borderwidth changes when mouse touches the panel
	 */
	private BorderComposite borderComp;

	/**
	 * List with pictures, which are directly aligned to this nodedisplay
	 */
	private List<ImageComp> picturesList;

	/**
	 * Picture in center is special and has to be handled separately.
	 */
	private Image centerPicture;

	/**
	 * Save created inner NodedisplayComp-instances for disposing them afterwards
	 */
	protected List<NodedisplayComp> innerCompsList;

	/**
	 * Reference to parent nodedisplay
	 */
	protected NodedisplayComp parentNodedisplayComp = null;

	/**
	 * Position of this nodedisplay within surrounding grid
	 */
	protected int x = 0, y = 0;

	/**
	 * Saves rectpaintlistener, which is needed for fast painting of inner rectangles.
	 */
	protected RectanglePaintListener rectanglePaintListener = null;

	/**
	 * Save references to those instances interested in the zooming-events
	 */
	private final List<INodedisplayZoomListener> zoomListeners = new ArrayList<INodedisplayZoomListener>();

	/**
	 * Call this constructor for start, maxlevel is chosen from nodedisplaylayout. The pnode has to be expanded as much as desired.
	 * The nodedisplay does not expand non-root nodes.
	 * 
	 * This constructor is designed for non-root nodes, which should appear as the root-node in the display. One could call this
	 * constructor for showing one rack of a supercomputer.
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param node
	 *            current node, which is root-data-element of this NodedisplayComp
	 * @param layout
	 *            layout definition for this nodedisplay part
	 * @param parent
	 *            parent composite, in which this nodedisplay is located
	 * @param style
	 *            SWT Style
	 */
	public NodedisplayComp(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout, Composite parent, int style) {

		super(lguiItem, parent, style);

		// Expand the node, if it is a root-node and not expanded at all
		if (node.getData().isRootNode() && node.getChildren().size() == 0) {
			int maxLevel = 10;
			if (layout.getMaxlevel() != null) {
				maxLevel = layout.getMaxlevel().intValue();
			}
			TreeExpansion.expandLMLNode(node, maxLevel);
			TreeExpansion.generateUsagebarsForAllLeaves(node);
		}

		init(node, layout);
	}

	/**
	 * This is an easy to use constructor for a nodedisplay as root-node.
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param nodedisplay
	 *            lml-model for the nodedisplay, which should be shown in this panel
	 * @param parent
	 *            parameter for calling super constructor
	 * @param style
	 *            parameter for calling super constructor
	 */
	public NodedisplayComp(ILguiItem lguiItem, Nodedisplay nodedisplay, Composite parent, int style) {

		this(lguiItem, new Node<LMLNodeData>(new LMLNodeData("", nodedisplay)), //$NON-NLS-1$
				// get the layout for this nodedisplay
				lguiItem.getLayoutAccess().getLayoutForNodedisplay(nodedisplay.getId()).getEl0(), parent, style);

	}

	/**
	 * Call this constructor for inner or lower elements. It is not allowed to call this constructor from outside.
	 * 
	 * @param lguiItem
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param node
	 *            current node, which is root-data-element of this NodedisplayComp
	 * @param layout
	 *            nodedisplay-layout part for this nodedisplay
	 * @param levelsPaintedByPaintListener
	 *            Holds the amount of tree levels, which are painted in fast way, but less configurable. This fast painting is done
	 *            by the rectpaintlistener. This parameter is currently only forwarded from the root node to all of its children.
	 * @param parentNodedisplay
	 *            father of this nodedisplay
	 * @param x
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param y
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param parent
	 *            parent composite for SWT constructor, differs from pparentNodedisplay, because parent is the innerPanel of
	 *            pparentNodedisplay
	 * @param style
	 *            SWT-style of this nodedisplay
	 */
	protected NodedisplayComp(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout,
			int levelsPaintedByPaintListener, NodedisplayComp parentNodedisplay, int x, int y, Composite parent, int style) {

		super(lguiItem, parent, style);

		// Save father reference
		this.parentNodedisplayComp = parentNodedisplay;

		// Save grid-positions
		this.x = x;
		this.y = y;

		// Get listener painted levels from parent node
		this.levelsPaintedByPaintListener = levelsPaintedByPaintListener;

		init(node, layout);
	}

	/**
	 * Add a reference, which is informed about every zoom event within this nodedisplay.
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void addZoomListener(INodedisplayZoomListener listener) {
		zoomListeners.add(listener);
	}

	@Override
	public void detectJobPositions(Set<Point> points, String jobId) {

		for (final NodedisplayComp childComp : innerCompsList) {
			childComp.detectJobPositions(points, jobId);
		}
		// Try to get job position from rectangle painter
		if (rectanglePaintListener != null) {
			rectanglePaintListener.detectJobPositions(points, jobId);
		}
		// Try to get job position from usagebar
		if (usagebar != null) {
			final List<JobInterval> jobintervals = usagebar.getJobIntervals();
			for (final JobInterval interval : jobintervals) {
				if (interval.job.getId().equals(jobId)) {
					// Detect this position in the ScrolledComposite
					final Rectangle subPaintArea = getNextNodedisplayBounds(usagebar);
					final Point jobPoint = getPositionInScrollComp(
							new Point(subPaintArea.x + interval.start, subPaintArea.y));
					points.add(jobPoint);
				}
			}
		}
		if (node.getLowerLevelCount() == 0) {
			final ObjectType object = getConnectedObject();
			if (object != null) {
				if (object.getId().equals(jobId)) {
					final Rectangle subPaintArea = getNextNodedisplayBounds(innerComp);
					final Point jobPoint = getPositionInScrollComp(
							new Point(subPaintArea.x, subPaintArea.y));
					points.add(jobPoint);
				}
			}
		}
	}

	/**
	 * Search a layout-section for a lower node <code>nodeData</code>. This lower node must be a child of this instance.
	 * 
	 * @param nodeData
	 *            a child of this nodedisplay's node, for which a layout is searched.
	 * @return lml-Nodedisplay-layout-section for this node, or default-layout if no layout is defined
	 */
	public Nodedisplayelement findLayout(LMLNodeData nodeData) {

		Nodedisplayelement result = null;

		if (lguiItem.getNodedisplayAccess() != null) {

			// Copy level-numbers
			final ArrayList<Integer> levelNumbers = new ArrayList<Integer>();
			for (int i = node.getData().getLevelIds().size(); i < nodeData.getLevelIds().size(); i++) {
				levelNumbers.add(nodeData.getLevelIds().get(i));
			}

			// deeper-level => traverse layout-tree
			result = LMLCheck.getNodedisplayElementByLevels(levelNumbers, nodedisplayLayout);
		}

		if (result == null) {
			return NodedisplayAccess.getDefaultLayout();
		} else {
			return result;
		}

	}

	/**
	 * Generates a hashmap, which connects nodes to their SWT-colors.
	 * 
	 * @param nodesList
	 *            The nodes, which are keys from the resulting hashmap
	 * @return hashmap, which connects nodes to their SWT-colors
	 */
	public HashMap<Node<LMLNodeData>, Color> generateNodeToColorMap(List<Node<LMLNodeData>> nodesList) {
		final OIDToObject oidToObject = lguiItem.getOIDToObject();

		final HashMap<Node<LMLNodeData>, Color> nodeToColorMap = new HashMap<Node<LMLNodeData>, Color>();
		if (oidToObject != null) {
			for (final Node<LMLNodeData> node : nodesList) {
				if (node.getData() != null) {
					nodeToColorMap.put(node,
							ColorConversion.getColor(oidToObject.getColorById(node.getData().getDataElement().getOid())));
				}
			}
		}

		return nodeToColorMap;
	}

	/**
	 * @return lml-data-access to data shown by this nodedisplay
	 */
	@Override
	public ILguiItem getLguiItem() {
		return lguiItem;
	}

	/**
	 * @return the layout used for this nodedisplay for configuration
	 */
	public Nodedisplayelement getNodedisplayLayout() {
		return nodedisplayLayout;
	}

	/**
	 * Retrieves the location of relative coordinates in this nodedisplay
	 * relative to the scrolled composite, which wraps around the top nodedisplay.
	 * 
	 * @param relPoint
	 *            the point relative for this nodedisplay
	 * @return the coordinates of this point relative to the scrolled composite
	 */
	public Point getPositionInScrollComp(Point relPoint) {
		Rectangle bounds = null;
		if (parentNodedisplayComp == null) {
			bounds = getBounds();
		}
		else {
			bounds = getNextNodedisplayBounds(this);
		}

		final Point pointInParent = new Point(bounds.x + relPoint.x, bounds.y + relPoint.y);
		if (parentNodedisplayComp == null) {
			return pointInParent;
		}
		else {
			return parentNodedisplayComp.getPositionInScrollComp(pointInParent);
		}

	}

	/**
	 * @return implicit name of node within nodedisplay, which is shown by this NodedisplayPanel
	 */
	public String getShownImpName() {
		if (node == null) {
			return null;
		}

		return node.getData().getFullImpName();
	}

	/**
	 * @return x-directed position of this nodedisplay in the parent's gridlayout
	 */
	public int getXPosition() {
		return x;
	}

	/**
	 * @return y-directed position of this nodedisplay in the parent's gridlayout
	 */
	public int getYPosition() {
		return y;
	}

	public boolean hasParentNodedisplay() {
		return parentNodedisplayComp != null;
	}

	/**
	 * Hide title or name for this panel
	 */
	public void hideTitle() {
		titleLabel.setVisible(false);
		nodedisplayLayout.setShowtitle(false);
	}

	/**
	 * Check if a rectangle within this nodedisplay is visible for the user on the gui.
	 * 
	 * @param area
	 *            a rectangle with relative coordinates to this nodedisplay
	 * @return <code>true</code>, if this rectangle is visible to the user, <code>false</code> otherwise
	 */
	public boolean isRectangleVisible(Rectangle area) {
		if (parentNodedisplayComp == null) {
			final Rectangle bounds = getBounds();
			final Point parentSize = getParent().getSize();

			// Assume parent nodedisplays are in scrollpane
			final int x = -bounds.x;
			final int y = -bounds.y;
			final int x2 = x + parentSize.x - 1;
			final int y2 = y + parentSize.y - 1;

			if (area.x + area.width <= x) {
				return false;
			}
			if (area.x > x2) {
				return false;
			}
			if (area.y > y2) {
				return false;
			}
			if (area.y + area.height <= y) {
				return false;
			}
			return true;
		} else {
			final Rectangle bounds = getNextNodedisplayBounds(this);

			area.x += bounds.x;
			area.y += bounds.y;
			return parentNodedisplayComp.isRectangleVisible(area);
		}
	}

	/**
	 * Stop listening.
	 * 
	 * @param listener
	 *            listener for zoom-events
	 */
	public void removeZoomListener(INodedisplayZoomListener listener) {
		zoomListeners.remove(listener);
	}

	/**
	 * Show title or name for this panel.
	 */
	public void showTitle() {
		titleLabel.setVisible(true);
		nodedisplayLayout.setShowtitle(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.lml.core.model.ObjectStatus.Updatable#
	 * updateStatus(org.eclipse.ptp.rm.lml.core.elements .ObjectType, boolean, boolean)
	 */
	@Override
	public void updateStatus(ObjectType object, boolean mouseOver, boolean mouseDown) {

		if (parentNodedisplayComp == null) {
			if (innerComp != null) {
				innerComp.redraw();
			}
			return;
		}

		final ObjectType connectedObject = getConnectedObject();
		if (lguiItem.getObjectStatus() != null) {
			// Is this node a leaf?
			if (node.getLowerLevelCount() == 0) {
				if (lguiItem.getObjectStatus().isMouseOver(connectedObject)) {
					borderComp.setBorderWidth(nodedisplayLayout.getMouseborder().intValue());
				} else {
					borderComp.setBorderWidth(nodedisplayLayout.getBorder().intValue());
				}

				if (lguiItem.getObjectStatus().isAnyMouseDown() && !lguiItem.getObjectStatus().isMouseDown(connectedObject)) {
					// Change color
					innerComp.setBackground(ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(null)));
				} else {
					innerComp.setBackground(jobColor);
				}
			} else if (node.getLowerLevelCount() <= levelsPaintedByPaintListener) {
				// For rectangle-paint of
				// lowest-level-elements
				innerComp.redraw();
			}

		}
	}

	/**
	 * Adds a dispose-listener, which removes this nodedisplay and its children from <code>ObjectStatus</code>.
	 */
	private void addDisposeAction() {
		this.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				removeUpdatable();

				if (rectanglePaintListener != null) {
					rectanglePaintListener.dispose();
				}

				if (titleLabel != null) {
					titleLabel.dispose();
				}
			}

		});
	}

	/**
	 * Add listener, which reacts when user focuses this panel. This method is not needed, if elements on the lowest level are
	 * painted with rectangles instead of painting composites. These listeners are used for nodedisplay composites, which represent
	 * only one node.
	 */
	private void addLowestLevelListeners() {

		final MouseMoveListener mouseMove = new MouseMoveListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseMove(MouseEvent e) {
				mouseInteraction.mouseMoveAction(node);
			}
		};

		final MouseListener mouselistener = new MouseListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDown(MouseEvent e) {
				mouseInteraction.mouseDownAction(node);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.x >= 0 && e.x <= getSize().x && e.y >= 0 && e.y <= getSize().y) {
					mouseInteraction.mouseUpAction(node);
				}
			}
		};

		final Listener mouseexit = new Listener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				mouseInteraction.mouseExitAction();
			}

		};

		borderComp.addMouseMoveListener(mouseMove);
		borderComp.addMouseListener(mouselistener);
		borderComp.addListener(SWT.MouseExit, mouseexit);

		innerComp.addMouseMoveListener(mouseMove);
		innerComp.addMouseListener(mouselistener);
		innerComp.addListener(SWT.MouseExit, mouseexit);
	}

	/**
	 * Adds listeners to innerPanel, which react to user interaction on lowest level rectangles. Generates tooltips and forwards
	 * mouse actions to the object status. These listeners are used for nodedisplay composites, which paint lower nodes by using the
	 * paintlistener.
	 */
	private void addMouseListenerToInnerPanelWithRectPaintListener() {
		// Create a listener for both: mouse moving and mouse clicks
		final MouseMoveAndDownListener mouseListener = new MouseMoveAndDownListener();

		innerComp.addMouseMoveListener(mouseListener);
		innerComp.addMouseListener(mouseListener);
		innerComp.addListener(SWT.MouseExit, new Listener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				mouseInteraction.mouseExitAction();
			}

		});

	}

	/**
	 * Add Listener to titlelabel, which allows to zoom in and zoom out These action only inform zoom-listeners. They have to take
	 * care that zooming is really invoked.
	 */
	private void addZoomFunction() {

		// Zoom in by clicking on title-panels
		titleLabel.addMouseListener(new MouseListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDown(MouseEvent e) {

				titleLabel.setBackground(titleBackgroundColor);

				NodedisplayZoomEvent event = null;
				// Is this nodedisplay the root-display?
				if (parentNodedisplayComp == null) {
					event = new NodedisplayZoomEvent(null, false);
				} else {
					event = new NodedisplayZoomEvent(node.getData().getFullImpName(), true);
				}
				notifyZoomListeners(event);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		// Show different background if titlelabel is covered by the mouse
		titleLabel.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				titleLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
			}
		});

		titleLabel.addListener(SWT.MouseExit, new Listener() {

			@Override
			public void handleEvent(Event event) {
				titleLabel.setBackground(titleBackgroundColor);
			}
		});

	}

	/**
	 * Creates borderFrame and innerPanel.
	 * 
	 * @param borderColor
	 *            the color for the border
	 */
	private void createFramePanels(Color borderColor) {

		// At least insert one panel with backgroundcolor as bordercomposite
		borderComp = new BorderComposite(mainComp, SWT.NONE);

		borderComp.setBorderColor(borderColor);
		borderComp.setBorderWidth(nodedisplayLayout.getBorder().intValue());
		borderComp.setLayoutData(new BorderData(BorderLayout.MFIELD));

		// If this composite is not painted by a recpaintlistener
		if (node.getLowerLevelCount() == 0 || node.getLowerLevelCount() > levelsPaintedByPaintListener) {
			innerComp = new Composite(borderComp, SWT.None);
		}
		else {
			innerComp = new Composite(borderComp, SWT.NO_BACKGROUND);
		}

		mouseInteraction = new MouseInteraction(lguiItem, innerComp);

		if (node.getLowerLevelCount() == 0) {
			innerComp.setBackground(jobColor);
		} else {
			innerComp.setBackground(backgroundColor);
			// Do not set the central picture on lowest level nodedisplays
			if (centerPicture != null) {
				innerComp.setBackgroundImage(centerPicture);
			}
		}

	}

	/**
	 * Initializes the surrounding pictureFrame and inserts pictures for this nodedisplay-level.
	 */
	private void createPictureFrame() {

		pictureComp = new Composite(this, SWT.None);
		pictureComp.setLayout(new BorderLayout());
		// Redo layout when resized
		pictureComp.addListener(SWT.Resize, new Listener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				pictureComp.layout(true);
			}

		});

		insertPictures();

		pictureComp.setBackground(backgroundColor);
	}

	/**
	 * Check required instances to be <code>!=null</code> and return connected job-object for this node.
	 * 
	 * @return connected object to this node
	 */
	private ObjectType getConnectedObject() {
		if (lguiItem.getOIDToObject() == null) {
			return null;
		}
		if (node.getData().getDataElement() == null) {
			return null;
		}
		return lguiItem.getOIDToObject().getObjectById(node.getData().getDataElement().getOid());
	}

	/**
	 * This is a helper-function for inserting pictures. It checks if the given URL is trying to access a bundle-file. Therefor the
	 * protocol of the URL has to be equal to "bundleentry". If the bundle cannot be accessed or there is no file available this
	 * function will return null.
	 * 
	 * @param urlString
	 *            any url, a working example is "bundleentry:/images/img1.png", if this entry exists in this bundle
	 * @return an URL to a file within the bundle or null, if anything fails
	 */
	private URL getRealBundleURL(String urlString) {

		if (LMLUIPlugin.getDefault() == null) {
			return null;
		}

		// Extract the file from the URL-string
		final int firstColon = urlString.indexOf(':');
		if (firstColon == -1) {
			return null;
		}
		final String string = urlString.substring(0, firstColon);
		if (!string.equals("bundleentry")) { //$NON-NLS-1$
			return null;
		}
		final String file = urlString.substring(firstColon + 1);

		// Use a different bundle here, if the file should be taken from another bundle
		return LMLUIPlugin.getDefault().getBundle().getEntry(file);
	}

	/**
	 * Part of constructor, which is equal in two constructors therefore outsourced. Initializes and creates all inner composites.
	 * Invokes lower level nodedisplay creation.
	 * 
	 * @param currentNode
	 *            the node, which is shown by this nodedisplay
	 * @param layout
	 *            the layout-configuration for this nodedisplay
	 */
	private void init(Node<LMLNodeData> currentNode, Nodedisplayelement layout) {
		// Transfer parameters
		this.node = currentNode;
		nodedisplayLayout = layout;

		setLayout(new FillLayout());

		// Root-nodedisplays choose the amount of levels,
		// which are painted by listeners
		if (parentNodedisplayComp == null) {
			final CompositeListenerChooser chooser = new CompositeListenerChooser(node);
			levelsPaintedByPaintListener = chooser.getLevelsPaintedByPaintListener();
		}

		innerCompsList = new ArrayList<NodedisplayComp>();
		addDisposeAction();
		backgroundColor = ColorConversion.getColor(LMLColor.stringToColor(nodedisplayLayout.getBackground()));
		createPictureFrame();
		fontObject = this.getDisplay().getSystemFont();

		mainComp = new Composite(pictureComp, SWT.None);
		mainComp.setLayout(new BorderLayout());
		mainComp.setLayoutData(new BorderData(BorderLayout.MFIELD));

		if (lguiItem.getOIDToObject() != null) {
			// Is this nodedisplay connected to a job
			if (node.getData().getDataElement() != null) {
				jobColor = ColorConversion.getColor(lguiItem.getOIDToObject()
						.getColorById(node.getData().getDataElement().getOid()));
				title = nodedisplayLayout.isShowfulltitle() ? node.getData().getFullImpName() : node.getData().getImpName();
			} else {
				jobColor = ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(null));
				title = lguiItem.getNodedisplayAccess().getNodedisplayTitel(0);
			}

			insertTitleLabel();
			insertInnerPanel();

			lguiItem.getObjectStatus().addComponent(this);
		}

	}

	/**
	 * Insert all children of node-instance as recursive NodedisplayComps.
	 */
	private void insertInnerPanel() {

		// Bordercolor is defined by this parameter
		final Color borderColor = ColorConversion.getColor(LMLColor.stringToColor(nodedisplayLayout.getBordercolor()));

		createFramePanels(borderColor);

		// Is painting already finished?
		if (node.getLowerLevelCount() == 0) {

			if (!insertUsagebar()) {
				// Try to insert the usagebar, otherwise add listeners for lowest level rectangles
				addLowestLevelListeners();
			}

			return;
		}

		final List<Node<LMLNodeData>> reorderedChildren = (new RowColumnSorter<Node<LMLNodeData>>(node.getChildren())).reorder(
				nodedisplayLayout.isHighestrowfirst(), nodedisplayLayout.isHighestcolfirst(), nodedisplayLayout.getCols()
						.intValue());

		// Paint rects instead of use composites for lowest-level-rectangles
		if (node.getLowerLevelCount() <= levelsPaintedByPaintListener) {
			initRectanglePaintListener(reorderedChildren);
			addMouseListenerToInnerPanelWithRectPaintListener();

		} else { // insert lower nodedisplays, nest composites
			// Set Gridlayout only if needed
			final int cols = nodedisplayLayout.getCols().intValue();
			final GridLayout layout = new GridLayout(cols, true);
			layout.horizontalSpacing = nodedisplayLayout.getHgap().intValue();
			layout.verticalSpacing = nodedisplayLayout.getVgap().intValue();

			layout.marginWidth = 1;
			layout.marginHeight = 1;

			innerComp.setLayout(layout);

			int index = 0;

			for (final Node<LMLNodeData> node : reorderedChildren) {

				final NodedisplayComp innerComp = createNodedisplayComp(lguiItem, node, findLayout(node.getData()),
						levelsPaintedByPaintListener, this, index % cols, index / cols, this.innerComp, SWT.None);

				innerCompsList.add(innerComp);

				innerComp.setLayoutData(getDefaultGridData());

				index++;
			}
		}
	}

	/**
	 * Search for pictures in layout-definition and add them at appropriate position.
	 */
	private void insertPictures() {

		picturesList = new ArrayList<ImageComp>();

		// insert pictures
		final List<PictureType> lmlPictures = nodedisplayLayout.getImg();
		for (final PictureType picture : lmlPictures) {

			URL urlString = null;
			try {
				urlString = new URL(picture.getSrc());
			} catch (final MalformedURLException e1) {
				urlString = getRealBundleURL(picture.getSrc());
			}

			if (urlString == null) {
				continue;
			}

			ImageComp imageComp;
			try {
				imageComp = new ImageComp(pictureComp, SWT.None, urlString, picture.getWidth(), picture.getHeight());
			} catch (final IOException e) {
				e.printStackTrace();
				continue;
			}

			if (parentNodedisplayComp == null) {
				imageComp.setBackground(backgroundColor);
			} else {
				imageComp.setBackground(this.getParent().getBackground());
			}

			switch (picture.getAlign()) {
			case WEST:
				imageComp.setLayoutData(new BorderData(BorderLayout.WFIELD));
				break;
			case EAST:
				imageComp.setLayoutData(new BorderData(BorderLayout.EFIELD));
				break;
			case NORTH:
				imageComp.setLayoutData(new BorderData(BorderLayout.NFIELD));
				break;
			case SOUTH:
				imageComp.setLayoutData(new BorderData(BorderLayout.SFIELD));
				break;
			default:
				break;
			}

			// Add borderpics to pictures and save center-pic in
			// centerpic-variable

			if (picture.getAlign() == AlignType.CENTER) {
				centerPicture = imageComp.getImage();
			} else {
				picturesList.add(imageComp);
			}
		}
	}

	/**
	 * If a title should be shown, it will be inserted by this function.
	 * 
	 */
	private void insertTitleLabel() {
		if (nodedisplayLayout.isShowtitle()) {
			titleBackgroundColor = ColorConversion.getColor(LMLColor.stringToColor(nodedisplayLayout.getTitlebackground()));

			titleLabel = new Label(mainComp, SWT.None);
			titleLabel.setText(title);
			titleLabel.setFont(fontObject);
			titleLabel.setBackground(titleBackgroundColor);
			titleLabel.setLayoutData(new BorderData(BorderLayout.NFIELD));

			addZoomFunction();
		}
	}

	/**
	 * Insert a usagebar composite inside the inner panel. This usagebar uses the given usage-information from the nodedisplay tag.
	 * 
	 * @return true, if usagebar was inserted, false otherwise
	 */
	private boolean insertUsagebar() {
		if (!hasParentNodedisplay()) {
			if (node.getData() == null || node.getData().getLevelIds() == null) {
				return false;
			}
			if (nodedisplayLayout.getMaxlevel() == null) {
				return false;
			}
			if (node.getData().getLevelIds().size() < nodedisplayLayout.getMaxlevel().intValue()) {
				return false;
			}

			innerComp.setLayout(new FillLayout());

			final UsageType usageData = node.getData().generateUsage();

			usagebar = new Usagebar(usageData, lguiItem, innerComp, SWT.None);
			usagebar.setPaintScale(true);
			// Take all available space for the usagebar
			usagebar.setBarFactor(0.8);
			// Set frame sizes
			usagebar.setStandardFrame(nodedisplayLayout.getBorder().intValue());
			usagebar.setMouseOverFrame(nodedisplayLayout.getMouseborder().intValue());

			return true;
		}

		if (node.getData() != null) {
			if (node.getData().getDataElement() != null) {
				if (node.getData().getDataElement().getUsage() != null) {
					if (node.getData().isDataElementOnNodeLevel()) {

						innerComp.setLayout(new FillLayout());
						usagebar = new Usagebar(node.getData().getDataElement().getUsage(), lguiItem, innerComp, SWT.None);
						// Do not paint scales here
						usagebar.setPaintScale(false);
						// Take all available space for the usagebar
						usagebar.setBarFactor(1);
						// Set frame sizes
						usagebar.setStandardFrame(nodedisplayLayout.getBorder().intValue());
						usagebar.setMouseOverFrame(nodedisplayLayout.getMouseborder().intValue());

						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Remove recursively this component and all inner NodedisplayComp-instances from ObjectStatus. Call this function before this
	 * Composite is disposed
	 */
	private void removeUpdatable() {

		if (lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().removeComponent(this);
		}

		for (final NodedisplayComp nodedisplayComp : innerCompsList) {
			nodedisplayComp.removeUpdatable();
		}
	}

	/**
	 * Generate a new NodedisplayComp with all parameters. This function must be overridden by all subclasses. It implements the
	 * factory-method pattern. The factory for NodedisplayComp-instances is the specific class itself by providing this method.
	 * 
	 * @return generated MinSizeNodedisplayComp, which can be exchanged by instances of sub-class
	 */
	protected NodedisplayComp createNodedisplayComp(ILguiItem lguiItem, Node<LMLNodeData> node, Nodedisplayelement layout,
			int levelsPaintedByPaintListener, NodedisplayComp parentNodedisplay, int x, int y, Composite parent, int style) {

		return new NodedisplayComp(lguiItem, node, layout, levelsPaintedByPaintListener, parentNodedisplay, x, y, parent, style);
	}

	/**
	 * Adds rectangle-painting listener to innerPanel.
	 * 
	 * @param nodes
	 *            the nodes painted by this RectPaintListener
	 */
	protected void initRectanglePaintListener(List<Node<LMLNodeData>> nodes) {
		rectanglePaintListener = new RectanglePaintListener(nodes, this, innerComp);
		innerComp.addPaintListener(rectanglePaintListener);
	}

	/**
	 * Inform all listeners and parent listeners about a new zooming event.
	 * 
	 * @param event
	 *            the corresponding event describing what happened
	 */
	protected void notifyZoomListeners(INodedisplayZoomEvent event) {
		if (parentNodedisplayComp != null) {
			parentNodedisplayComp.notifyZoomListeners(event);
		}

		for (final INodedisplayZoomListener listener : zoomListeners) {
			listener.handleEvent(event);
		}
	}
}