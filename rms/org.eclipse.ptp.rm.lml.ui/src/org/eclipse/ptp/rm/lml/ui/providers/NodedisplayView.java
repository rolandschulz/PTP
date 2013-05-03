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
package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.ptp.internal.rm.lml.core.model.LMLCheck;
import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.internal.rm.lml.core.model.TreeExpansion;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;
import org.eclipse.ptp.rm.lml.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.ui.providers.support.ColorConversion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * This is the parent composite of NodedisplayComp. This class allows to zoom or switch the
 * viewports of nodedisplaycomps. Every nodedisplaycomp is connected with one
 * instance of this class. NodedisplayView represents one zoomable
 * NodedisplayComp.
 */
public class NodedisplayView extends AbstractNodedisplayView {

	/**
	 * Listens for the first painting of the widget after inserting a new nodedisplay.
	 * Calls the adjustScrollPaneSize-function on the first paint.
	 * This allows to resize the scrollpane after the first paint.
	 * 
	 */
	private class FirstPaintListener implements Listener {

		private int paintCount = 0;

		public FirstPaintListener() {
			reset();
		}

		@Override
		public void handleEvent(Event event) {
			if (paintCount < adjustCount) {
				adjustScrollPaneSize();
			}

			paintCount++;
		}

		public void reset() {
			paintCount = 0;
		}

	}

	/**
	 * This class listens for object mark events. Tries to scroll jobs
	 * to visible area in the nodes view as soon as a job is marked.
	 * 
	 * @author karbach
	 * 
	 */
	private class JobsVisibleListener implements ILMLListener {

		@Override
		public void handleEvent(ILguiAddedEvent event) {
		}

		@Override
		public void handleEvent(ILguiRemovedEvent event) {
		}

		@Override
		public void handleEvent(IMarkObjectEvent event) {
			if (root != null) {
				final String jobId = event.getOid();
				final Set<Point> points = new HashSet<Point>();
				root.detectJobPositions(points, jobId);

				final Point scrollSize = scrollComp.getSize();

				boolean visible = false;
				Point minPoint = null;
				for (final Point p : points) {
					if (p.x >= 0 && p.x < scrollSize.x
							&& p.y >= 0 && p.y < scrollSize.y) {
						visible = true;
					}
					if (minPoint == null || p.x < minPoint.x || (p.x == minPoint.x && p.y < minPoint.y)) {
						minPoint = p;
					}
				}

				if (!visible) {
					if (minPoint != null) {
						final Point origin = scrollComp.getOrigin();
						origin.x += minPoint.x - 5;
						origin.y += minPoint.y - 5;
						if (origin.x < 0) {
							origin.x = 0;
						}
						if (origin.y < 0) {
							origin.y = 0;
						}
						scrollComp.setOrigin(origin);
					}
				}
			}
		}

		@Override
		public void handleEvent(ISelectObjectEvent event) {
		}

		@Override
		public void handleEvent(ITableFilterEvent event) {
		}

		@Override
		public void handleEvent(ITableSortedEvent event) {
		}

		@Override
		public void handleEvent(IUnmarkObjectEvent event) {
		}

		@Override
		public void handleEvent(IUnselectedObjectEvent event) {
		}

		@Override
		public void handleEvent(IViewUpdateEvent event) {
		}
	}

	/**
	 * Listener, which waits for the nodedisplay to zoom
	 * As a result the view should be changed to the
	 * desired new root-node
	 * 
	 * Zooms in on TREEZOOMIN events and out on
	 * TREEZOOMOUT-events.
	 * 
	 */
	private class ZoomListener implements INodedisplayZoomListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener#handleEvent(org.eclipse.ptp.rm.lml.core.events.
		 * INodedisplayZoomEvent)
		 */
		@Override
		public void handleEvent(INodedisplayZoomEvent event) {
			switch (event.getZoomType()) {
			case TREEZOOMIN:
				zoomIn(event.getNewNodeName());
				break;
			case TREEZOOMOUT:
				zoomOut();
				break;
			}
		}

	}

	/**
	 * Saves the currently displayed LML-node.
	 */
	private Node<LMLNodeData> shownNode;

	/**
	 * Count of readjustments after the nodedisplay was changed
	 */
	private final int adjustCount = 10;

	/**
	 * Defines the maximum size of pixels of the nodedisplay
	 */
	private final int maximumSize = (1 << 16) - 1;

	/**
	 * LML-Data-model for this view
	 */
	private Nodedisplay nodedisplay;

	/**
	 * Layout for this nodedisplay
	 */
	private NodedisplaylayoutType nodedisplayLayout;

	/**
	 * creates scrollbars surrounding nodedisplay
	 */
	private final ScrolledComposite scrollComp;

	/**
	 * root nodedisplay which is currently shown
	 */
	private NodedisplayComp root = null;

	/**
	 * Saves zoom-levels to zoom out later, saves full-implicit name of nodes to
	 * create Displaynodes from these ids
	 */
	private Stack<String> zoomStack = new Stack<String>();

	/**
	 * Cursor to show while processing
	 */
	private final Cursor waitCursor;

	/**
	 * Default cursor saved for restoring.
	 */
	private final Cursor defaultCursor;

	/**
	 * If <code>true</code>, rectangles take at least a given minimal space.
	 * Otherwise rectangles can become even zero sized.
	 */
	private final boolean minSizeRectangles;

	/**
	 * Listens for the first paint and updates the painting on the first paint call.
	 */
	private final FirstPaintListener firstPaintListener;

	/**
	 * Reacts on job mark events. Tries to make jobs visible by scrolling.
	 */
	private final JobsVisibleListener jobsListener;

	/**
	 * Minimal size of width and height of painted rectangles
	 * Is only used, if minSizeRectangles is true
	 */
	private int minRectangleSize = NodedisplayCompMinSize.defaultMinSize;

	/**
	 * Saves the currently shown maximum level in the nodedisplay
	 */
	private int shownLevel;

	/**
	 * Simple constructor, which defines on its own if minimum sized
	 * or arbitrarily sized rectangles are painted.
	 * 
	 * @param lguiItem
	 *            lgui handling instance
	 * @param nodedisplay
	 *            nodedisplay painted by this view
	 * @param parent
	 *            parent composite of this nodedisplayview
	 */
	public NodedisplayView(ILguiItem lguiItem, Nodedisplay nodedisplay, Composite parent) {
		this(lguiItem, nodedisplay, parent, true);
	}

	/**
	 * Create a composite as surrounding component for NodedisplayComps. This
	 * class encapsulates zooming functionality. It also saves a stack for zooming.
	 * 
	 * @param lguiItem
	 *            lgui handling instance
	 * @param nodedisplay
	 *            nodedisplay painted by this view
	 * @param parent
	 *            parent composite of this nodedisplayview
	 * @param minSizeRectangles
	 *            If true, rectangles take at least a given minimal space.
	 */
	public NodedisplayView(ILguiItem lguiItem, Nodedisplay nodedisplay, Composite parent, boolean minSizeRectangles) {

		super(lguiItem, parent, SWT.None);

		this.minSizeRectangles = minSizeRectangles;

		this.nodedisplay = nodedisplay;

		findNodedisplayLayout();

		setLayout(new FillLayout());
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		scrollComp = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);

		this.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeNodedisplay();
				getDisplay().removeFilter(SWT.Paint, firstPaintListener);
				if (jobsListener != null) {
					LMLManager.getInstance().removeListener(jobsListener);
				}
				if (waitCursor != null) {
					waitCursor.dispose();
				}
			}
		});

		jobsListener = new JobsVisibleListener();
		LMLManager.getInstance().addListener(jobsListener, null);

		// Listen for the first paints of every new nodedisplay
		// and adjust it to the new size
		firstPaintListener = new FirstPaintListener();
		this.getDisplay().addFilter(SWT.Paint, firstPaintListener);

		if (lguiItem != null && nodedisplay != null) {
			root = createRootNodedisplay();
			initNewNodedisplay();
		}

		// Create cursors
		defaultCursor = this.getCursor();
		waitCursor = new Cursor(this.getDisplay(), SWT.CURSOR_WAIT);

		addResizeListenerForScrollPane();
		checkEmptyScreen();
	}

	/**
	 * Disposes and sets root nodedisplay to null.
	 * This can be called to release the data hold by
	 * the currently active nodedisplay. Also releases
	 * created colors.
	 */
	public void disposeNodedisplay() {
		ColorConversion.disposeColors();
		if (root != null) {
			root.dispose();
			root = null;
		}
	}

	@Override
	public int getMaximumNodedisplayDepth() {
		if (nodedisplay == null) {
			return 0;
		}
		if (nodedisplay.getScheme() == null) {
			return 0;
		}
		return LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#getMinimalRectangleSize()
	 */
	@Override
	public int getMinimalRectangleSize() {
		if (minSizeRectangles) {
			if (root != null) {
				return minRectangleSize;
			}
			return 0;
		}
		return 0;
	}

	@Override
	public int getMinimumLevelOfDetail() {
		if (shownNode != null && shownNode.getData() != null) {
			return shownNode.getData().getLevelIds().size();
		}
		else {
			return 0;
		}
	}

	/**
	 * @return currently shown nodedisplaycomp
	 */
	@Override
	public NodedisplayComp getRootNodedisplay() {
		return root;
	}

	/**
	 * @return access to scrollpane, which contains the root-nodedisplay
	 */
	public ScrolledComposite getScrollComp() {
		return scrollComp;
	}

	@Override
	public int getShownMaxLevel() {
		return shownLevel;
	}

	/**
	 * Set node with impname as implicit name as root-node within this
	 * nodedisplay-panel. Call this function only if model did not changed.
	 * 
	 * @param impName
	 *            implicit name of a node, which identifies every node within a
	 *            nodedisplay
	 * @return <code>true</code>, if root was changed, otherwise <code>false</code>
	 */
	@Override
	public boolean goToImpName(String impName) {
		return goToImpName(impName, false);
	}

	/**
	 * Set node with impname as implicit name as root-node within this
	 * nodedisplay-panel.
	 * 
	 * @param impName
	 *            implicit name of a node, which identifies every node within a
	 *            nodedisplay
	 * @param modelChanged
	 *            if true a new nodedisplay is forced to be created, otherwise
	 *            only if the new impname differs from currently shown impname
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpName(String impName, boolean modelChanged) {

		if (lguiItem == null) {
			return false;
		}

		String shownImpName = null;
		if (root != null) {
			shownImpName = root.getShownImpName();
		}

		// A new panel has to be created if the model is new
		if (!modelChanged) {
			// Do not create a new panel if panel is already on the right view
			if (shownImpName == null) {
				if (impName == null) {
					return false;
				}
			} else if (shownImpName.equals(impName)) {
				/*
				 * Do not create new panel, if current viewport is the same to
				 * which this panel should be set
				 */
				return false;
			}
		}

		NodedisplayComp newNodedisplayComp = null;

		if (root != null) {
			root.dispose();// Delete old root-element
			root = null;
		}

		if (impName != null) {
			newNodedisplayComp = createNodedisplayFromImpName(impName);
		} else {
			/*
			 * if impname is null => go up to root-level
			 */
			newNodedisplayComp = createRootNodedisplay();
		}

		root = newNodedisplayComp;
		initNewNodedisplay();

		this.layout();
		root.layout();

		return true;
	}

	/**
	 * The stack which saves the last zoom-levels is restarted.
	 */
	@Override
	public void restartZoom() {
		zoomStack = new Stack<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#setFixedLevel(int)
	 */
	@Override
	public void setFixedLevel(int level) {
		// Make sure that level is not greater then the deepest level defined in the nodedisplay's scheme
		if (nodedisplay != null) {
			if (nodedisplay.getScheme() != null) {
				if (LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme()) < level) {
					level = LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme());
				}
			}
		}

		super.setFixedLevel(level);
	}

	@Override
	public void setMaxLevel(int maxLevel) {
		final int minlevel = getMinimumLevelOfDetail();
		// Check constraints for the maxLevel
		if (maxLevel < minlevel) {
			maxLevel = minlevel;
		}
		// Make sure that level is not greater then the deepest level defined in the nodedisplay's scheme
		if (nodedisplay != null) {
			if (nodedisplay.getScheme() != null) {
				if (LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme()) < maxLevel) {
					maxLevel = LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme());
				}
			}
		}

		final int nodeLevel = shownNode.getData().getLevelIds().size();
		lguiItem.getLayoutAccess().setMaxLevelOnLevel(nodedisplayLayout, nodeLevel, maxLevel);
	}

	/**
	 * Really adjusts the maxlevel attribute defined in this Nodedisplay's layout.
	 * Checks wether this level is allowed.
	 * 
	 * @param level
	 *            the new maxlevel of detail shown by the nodedisplay at current zoom position
	 */
	public void setMaxLevelInLayout(int level) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#setMinimalRectangleSize(int)
	 */
	@Override
	public void setMinimalRectangleSize(int size) {

		if (minSizeRectangles) {
			minRectangleSize = size;
			if (root != null) {
				((IMinSizeNodedisplay) root).setMinimumRectangleHeight(minRectangleSize);
				((IMinSizeNodedisplay) root).setMinimumRectangleWidth(minRectangleSize);

				Point newMinSize = ((IMinSizeNodedisplay) root).getMinimalSize();

				// Check the new root's size. There is a bug in SWT, if the nodedisplay
				// is larger than 65536. Do not increase the rectangle size, if this would happen.
				// Otherwise parts of the nodedisplay are painted over with black rectangles
				// Decrease the minRectangleSize until the newMinSize is smaller than the maximumSize
				while (minRectangleSize > 1 && (newMinSize.x > maximumSize || newMinSize.y > maximumSize)) {
					minRectangleSize--;

					((IMinSizeNodedisplay) root).setMinimumRectangleHeight(minRectangleSize);
					((IMinSizeNodedisplay) root).setMinimumRectangleWidth(minRectangleSize);
					newMinSize = ((IMinSizeNodedisplay) root).getMinimalSize();
				}
			}
			super.setMinimalRectangleSize(minRectangleSize);
			// Tell the scrollpane about the change
			adjustScrollPaneSize();
		}
	}

	@Override
	public void update() {
		update(getLguiItem(), getNewModel());
	}

	@Override
	public void update(ILguiItem lguiItem) {
		setLguiItem(lguiItem);
		update(lguiItem, getNewModel());
	}

	@Override
	public void update(ILguiItem lguiItem, Nodedisplay nodedisplay) {
		super.update();

		String lastShown = null;
		if (root != null) {
			lastShown = root.getShownImpName();
		}

		final ILguiItem oldLguiItem = this.lguiItem;
		final Nodedisplay oldNodedisplay = this.nodedisplay;

		setLguiItem(lguiItem);
		this.nodedisplay = nodedisplay;

		// Restart zooming if another resource is monitored now
		if (isNodedisplayChanged(oldLguiItem, oldNodedisplay, lguiItem, nodedisplay)) {
			restartZoom();
			lastShown = null;
		}

		findNodedisplayLayout();

		if (nodedisplay != null) {
			goToImpName(lastShown, true);
		}

		checkEmptyScreen();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#zoomIn(java.lang.String)
	 */
	@Override
	public void zoomIn(String impName) {
		if (root == null) {
			return;
		}

		this.setCursor(waitCursor);

		String oldShown = root.getShownImpName();

		if (goToImpName(impName)) {
			if (oldShown == null) {
				oldShown = ""; //$NON-NLS-1$
			}
			zoomStack.push(oldShown);
		}

		notifyZoom(impName, true);

		this.setCursor(defaultCursor);
	}

	/*
	 * (non-Javadoc)
	 * Go one level higher in zoomstack
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#zoomOut()
	 */
	@Override
	public void zoomOut() {
		this.setCursor(waitCursor);

		if (!zoomStack.isEmpty()) {
			String impName = zoomStack.pop();
			// Get back null-values
			if (impName.equals("")) { //$NON-NLS-1$
				impName = null;
			}

			// Switch view to node with impname
			goToImpName(impName);
			notifyZoom(impName, false);
		}
		else {
			// If zoom-stack is empty go to root level view
			goToImpName(null);
			notifyZoom(null, true);
		}

		this.setCursor(defaultCursor);
	}

	/**
	 * Adds a listener, which changes scrollbar-increments on every resize.
	 */
	private void addResizeListenerForScrollPane() {

		scrollComp.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				adjustScrollBarIncrements();
				// After resize it might happen, that the nodedisplay minsize depends
				// on the current size (for example if images are painted).
				// Thus the nodedisplay has to be painted and resized a few times untill
				// the final size is achieved.
				firstPaintListener.reset();
			}
		});

	}

	/**
	 * Adjust the sizes of page increments of the scrollbars depending on
	 * the thumb size.
	 */
	private void adjustScrollBarIncrements() {
		final ScrollBar xBar = scrollComp.getHorizontalBar();
		final ScrollBar yBar = scrollComp.getVerticalBar();
		if (xBar != null) {
			xBar.setPageIncrement(xBar.getThumb() / 2);
			xBar.setIncrement(xBar.getThumb() / 5);
		}
		if (yBar != null) {
			yBar.setPageIncrement(yBar.getThumb() / 2);
			yBar.setIncrement(yBar.getThumb() / 5);
		}
	}

	/**
	 * Make scrollpane big enough so that lowest level rectangles
	 * are painted with their minimum allowed size
	 */
	private void adjustScrollPaneSize() {
		if (root != null && root instanceof IMinSizeNodedisplay) {
			final IMinSizeNodedisplay minSizeNodedisplay = (IMinSizeNodedisplay) root;
			final Point minSize = minSizeNodedisplay.getMinimalSize();
			scrollComp.setMinSize(minSize);
			adjustScrollBarIncrements();
		}
	}

	/**
	 * If there is no nodedisplay created to display,
	 * this method call sets the scrollpane to invisible.
	 */
	private void checkEmptyScreen() {
		if (lguiItem != null && nodedisplay != null) {
			scrollComp.setVisible(true);
		} else {
			scrollComp.setVisible(false);
		}
	}

	/**
	 * Pass an implicit name to this function.
	 * This name is used for the creation of LMLNodeData, which
	 * represents the data model for the created nodedisplay.
	 * 
	 * @param impName
	 *            implicit name of a physical element within the nodedisplay, which is model for this nodedisplayview
	 * @return a nodedisplay displaying the passed node
	 */
	private NodedisplayComp createNodedisplayFromImpName(String impName) {
		LMLNodeData nodeData = null;
		try {
			nodeData = new LMLNodeData(impName, nodedisplay);
		} catch (final IllegalArgumentException er) {
			nodeData = new LMLNodeData("", nodedisplay); //$NON-NLS-1$
		}
		final Node<LMLNodeData> newNode = new Node<LMLNodeData>(nodeData);

		// Search for nodedisplayelement
		final Nodedisplayelement layout = findLayout(nodeData);

		// Expand the newnode
		int maxLevel = 10;
		if (layout.getMaxlevel() != null) {
			maxLevel = layout.getMaxlevel().intValue();
		}
		if (getFixedLevel() > 0) {
			maxLevel = getFixedLevel();
		}
		// Make sure that maxLevel is not bigger than maximum level withint the nodedisplay's scheme
		if (nodedisplay != null) {
			if (nodedisplay.getScheme() != null) {
				final int deepest = LMLCheck.getDeepestSchemeLevel(nodedisplay.getScheme());
				if (deepest < maxLevel) {
					maxLevel = deepest;
				}
			}
		}

		TreeExpansion.expandLMLNode(newNode, maxLevel);
		TreeExpansion.generateUsagebarsForAllLeaves(newNode);
		shownLevel = maxLevel;
		shownNode = newNode;

		return createChildNodedisplay(newNode, layout);
	}

	/**
	 * Search for a layout-section for an LMLNodeData-instance.
	 * This function uses the nodedisplayLayout, which should
	 * contain a layout for the nodedisplay displayed by this view.
	 * 
	 * @param nodeData
	 *            LMLNodeData, for which a layout is searched
	 * 
	 * @return lml-Nodedisplay-layout-section for this nodeData, or
	 *         default-layout if no layout is defined
	 */
	private Nodedisplayelement findLayout(LMLNodeData nodeData) {

		Nodedisplayelement result = null;

		if (nodedisplayLayout == null) {
			return NodedisplayAccess.getDefaultLayout();
		}

		if (nodeData == null || nodeData.isRootNode()) {// Root-level => return
			// el0-Nodedisplayelement
			if (nodedisplayLayout.getEl0() != null) {
				return nodedisplayLayout.getEl0();
			} else {
				return NodedisplayAccess.getDefaultLayout();
			}
		}

		if (nodedisplayLayout.getEl0() == null) {
			return NodedisplayAccess.getDefaultLayout();
		}

		// Copy level-numbers
		final ArrayList<Integer> levelNumbers = LMLCheck.copyArrayList(nodeData.getLevelIds());

		// deeper-level => traverse layout-tree
		result = LMLCheck.getNodedisplayElementByLevels(levelNumbers, nodedisplayLayout.getEl0());

		if (result == null) {
			return NodedisplayAccess.getDefaultLayout();
		} else {
			return result;
		}
	}

	/**
	 * Find first possible nodedisplaylayout for the current nodedisplay.
	 */
	private void findNodedisplayLayout() {
		if (nodedisplay != null && lguiItem != null) {
			nodedisplayLayout = lguiItem.getLayoutAccess().getLayoutForNodedisplay(nodedisplay.getId());
		}
	}

	/**
	 * Data has been updated. The new nodedisplay-model is needed. This function
	 * searches for the nodedisplay-instance, which is the successor of the last
	 * shown nodedisplay.
	 * 
	 * @return new Nodedisplay-model
	 */
	private Nodedisplay getNewModel() {

		if (lguiItem == null) {
			return null;
		}

		String nodedisplayId = ""; //$NON-NLS-1$
		if (nodedisplay != null) {
			nodedisplayId = nodedisplay.getId();
		}
		Nodedisplay result = null;
		if (lguiItem.getNodedisplayAccess() != null) {
			result = lguiItem.getNodedisplayAccess().getNodedisplayById(nodedisplayId);

			if (result == null) {
				final List<Nodedisplay> nodedisplays = lguiItem.getNodedisplayAccess().getNodedisplays();

				if (nodedisplays.size() > 0) {
					result = nodedisplays.get(0);
				}
			}

		}
		return result;
	}

	/**
	 * Call functions necessary for every new nodedisplay.
	 */
	private void initNewNodedisplay() {
		firstPaintListener.reset();
		root.addZoomListener(new ZoomListener());
		updateScrollPane();
		setMinimalRectangleSize(NodedisplayCompMinSize.defaultMinSize);
		adjustScrollPaneSize();
	}

	/**
	 * After a new nodedisplay has been inserted, this function
	 * must be called to update the scrollpane's view.
	 */
	private void updateScrollPane() {
		scrollComp.setContent(root);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		this.layout(true);
		scrollComp.layout(true);
	}

	/**
	 * Factory method for creating a nodedisplay composite.
	 * Creates either a NodedisplayComp or a NodedisplayCompMinSize
	 * depending on the value of minSizeRectangles;
	 * 
	 * @param newNode
	 *            Node, which should be shown
	 * @param layout
	 *            corresponding layout
	 * @return a nodedisplay comp displaying the given node
	 */
	protected NodedisplayComp createChildNodedisplay(Node<LMLNodeData> newNode, Nodedisplayelement layout) {
		if (minSizeRectangles) {
			return new NodedisplayCompMinSize(lguiItem, newNode, layout, scrollComp, SWT.None);
		}
		else {
			return new NodedisplayComp(lguiItem, newNode, layout, scrollComp, SWT.None);
		}

	}

	/**
	 * Factory method for creating a root nodedisplay composite.
	 * Creates either a NodedisplayComp or a NodedisplayCompMinSize
	 * depending on the value of minSizeRectangles;
	 * 
	 * @return a nodedisplay comp displaying the root node
	 */
	protected NodedisplayComp createRootNodedisplay() {
		shownLevel = nodedisplayLayout.getEl0().getMaxlevel().intValue();
		return createNodedisplayFromImpName(""); //$NON-NLS-1$
	}

	/**
	 * Check if the models changed completely. Return <code>false</code> if the nodedisplays are identically or successors of each
	 * other. newnode succeeds to oldnode, if an update was made
	 * with the given lguiitems.
	 * 
	 * @param oldLguiItem
	 *            old lgui-item from before update
	 * @param oldNodedisplay
	 *            old nodedisplay, which was shown
	 * @param newLguiItem
	 *            new lgui-item after update
	 * @param newNodedisplay
	 *            new nodedisplay-model
	 * @return true if monitored system was changed, false otherwise
	 */
	protected boolean isNodedisplayChanged(ILguiItem oldLguiItem, Nodedisplay oldNodedisplay, ILguiItem newLguiItem,
			Nodedisplay newNodedisplay) {
		if (oldNodedisplay == null || newNodedisplay == null) {
			return true;
		}
		if (oldNodedisplay == newNodedisplay) {
			return false;
		}
		if (oldLguiItem == newLguiItem && oldNodedisplay.getId().equals(newNodedisplay.getId())) {
			return false;
		}
		return true;
	}

}
