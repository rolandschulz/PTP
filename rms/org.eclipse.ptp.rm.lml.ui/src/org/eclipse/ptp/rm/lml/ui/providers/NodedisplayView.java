package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Parent composite of NodedisplayComp
 * This class allows to zoom or switch the viewports of
 * nodedisplaycomps. Every nodedisplaycomp is connected with
 * one instance of this class. NodedisplayView represents one zoomable
 * NodedisplayComp.
 */
public class NodedisplayView extends LguiWidget implements INodedisplayView {

	protected Nodedisplay model;// LML-Data-model for this view

	protected ScrolledComposite scrollpane;// creates scrollbars surrounding nodedisplay
	protected NodedisplayComp root = null;// root nodedisplay which is currently shown

	protected Stack<String> zoomstack = new Stack<String>();// Saves zoom-levels to zoom out later, saves full-implicit name of
															// nodes to create Displaynodes from these ids

	// Cursors for showing processing
	protected Cursor waitcursor;// Cursor to show while processing
	protected Cursor defaultcursor;// default cursor

	// List of listeners for zooming-events
	private final List<INodedisplayZoomListener> zoomlisteners;

	/**
	 * Create a composite as surrounding component for NodedisplayComps.
	 * This class encapsulates zooming functionality. It saves a stack
	 * 
	 * @param pmodel
	 * @param parent
	 */
	public NodedisplayView(ILguiItem lgui, Nodedisplay pmodel, Composite parent) {

		super(lgui, parent, SWT.None);

		zoomlisteners = new LinkedList<INodedisplayZoomListener>();

		model = pmodel;

		final RowLayout layout = new RowLayout();
		layout.pack = false;

		setLayout(layout);

		scrollpane = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);

		if (lgui != null && model != null)
			root = new NodedisplayComp(lgui, model, this, SWT.None);

		// Create cursors
		defaultcursor = this.getCursor();
		waitcursor = new Cursor(this.getDisplay(), SWT.CURSOR_WAIT);

		addResizeListenerForScrollPane();
		addResizeListenerForNodedisplayView();
	}

	// Listener-access
	/**
	 * Add a listener, which listens for zoom-events.
	 * Every time a new node is set as root a new event
	 * is send to the listeners.
	 * 
	 * @param listener
	 */
	public void addZoomListener(INodedisplayZoomListener listener) {
		zoomlisteners.add(listener);
	}

	/**
	 * Decreases the size of painted rectangles.
	 */
	public void decreaseRectangles() {
		if (root == null)
			return;

		root.decreaseMinRectangleSize();
	}

	@Override
	public void dispose() {
		// Dispose created cursor
		waitcursor.dispose();
	}

	public int getMinimalRectangleSize() {
		if (root == null)
			return 0;

		return root.getMinimalRectangleSize();
	}

	public NodedisplayComp getRootNodedisplay() {
		return root;
	}

	/**
	 * @return access to scrollpane, which contains the root-nodedisplay
	 */
	public ScrolledComposite getScrollPane() {
		return scrollpane;
	}

	public boolean goToImpname(String impname) {
		return goToImpname(impname, false);
	}

	public void increaseRectangles() {
		if (root == null)
			return;

		root.increaseMinRectangleSize();
	}

	/**
	 * @param listener
	 */
	public void removeZoomListener(INodedisplayZoomListener listener) {
		zoomlisteners.remove(listener);
	}

	public void restartZoom() {
		zoomstack = new Stack<String>();
	}

	public void setMinimalRectangleSize(int size) {
		if (root == null)
			return;

		root.setMinmalRectangleSize(size);
	}

	@Override
	public void update() {
		update(lgui, getNewModel());
	}

	public void update(ILguiItem lgui) {
		this.lgui = lgui;
		update(lgui, getNewModel());
	}

	public void update(ILguiItem lgui, Nodedisplay pmodel) {
		super.update();

		this.lgui = lgui;

		restartZoom();
		model = pmodel;

		if (model != null)
			goToImpname(null, true);
	}

	public void zoomIn(String impname) {
		if (root == null)
			return;

		this.setCursor(waitcursor);

		String oldshown = root.getShownImpname();

		if (goToImpname(impname)) {
			if (oldshown == null)// Not allowed to insert null-values into ArrayDeque
				oldshown = "";
			zoomstack.push(oldshown);
		}

		this.setCursor(defaultcursor);
	}

	public void zoomOut() {
		this.setCursor(waitcursor);

		if (!zoomstack.isEmpty()) {
			String impname = zoomstack.pop();
			// Get back null-values
			if (impname.equals(""))
				impname = null;

			// Switch view to node with impname
			goToImpname(impname);
		}

		this.setCursor(defaultcursor);
	}

	/**
	 * Notify all listeners, that a new node with
	 * implicit name impname is now root-node.
	 * 
	 * @param impname
	 *            full implicit name of new node or null for root-nodes
	 */
	private void notifyZoom(String impname) {

		final NodedisplayZoomEvent event = new NodedisplayZoomEvent(impname);
		for (final INodedisplayZoomListener listener : zoomlisteners) {
			listener.handleEvent(event);
		}

	}

	/**
	 * Adds a listener, which adjusts rectangle-sizes everytime
	 * this view is resized.
	 */
	protected void addResizeListenerForNodedisplayView() {
		this.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				if (root != null) {
					root.showMinRectangleSizes();
				}
			}
		});
	}

	/**
	 * Adds a listener, which changes scrollbar-increments on
	 * every resize.
	 */
	protected void addResizeListenerForScrollPane() {

		scrollpane.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {

				checkSize();

				final ScrollBar xbar = scrollpane.getHorizontalBar();
				final ScrollBar ybar = scrollpane.getVerticalBar();
				if (xbar != null) {
					xbar.setPageIncrement(xbar.getThumb() / 2);
					xbar.setIncrement(xbar.getThumb() / 5);
				}
				if (ybar != null) {
					ybar.setPageIncrement(ybar.getThumb() / 2);
					ybar.setIncrement(ybar.getThumb() / 5);
				}
			}

			/**
			 * Checks that the scrollpane-size does not exceed the
			 * NodedisplayView-size.
			 */
			private void checkSize() {

				final Point s = scrollpane.getSize();
				final Point ns = NodedisplayView.this.getSize();

				if (s.x >= ns.x - 1) {
					s.x = ns.x - 2;
				}

				if (s.y >= ns.y) {
					s.y = ns.y - 1;
				}

				scrollpane.setSize(s);
			}
		});

	}

	/**
	 * Data has been updated. The new nodedisplay-model is needed.
	 * This function searches for the nodedisplay-instance, which
	 * is the successor of the last shown nodedisplay.
	 * 
	 * @return new Nodedisplay-model
	 */
	protected Nodedisplay getNewModel() {

		if (lgui == null)
			return null;

		String nodedisplayId = "";
		if (model != null)
			nodedisplayId = model.getId();

		Nodedisplay res = lgui.getNodedisplayAccess().getNodedisplayById(nodedisplayId);

		if (res == null) {
			final List<Nodedisplay> nodedisplays = lgui.getNodedisplayAccess().getNodedisplays();

			if (nodedisplays.size() > 0) {
				res = nodedisplays.get(0);
			}
		}

		return res;
	}

	/**
	 * Set node with impname as implicit name as root-node within this nodedisplay-panel
	 * 
	 * @param impname
	 *            implicit name of a node, which identifies every node within a nodedisplay
	 * @param modelChanged
	 *            if true a new nodedisplay is forced to be created, otherwise only
	 *            if the new impname differs from currently shown impname
	 * @return true, if root was changed, otherwise false
	 */
	protected boolean goToImpname(String impname, boolean modelChanged) {

		if (lgui == null)
			return false;

		String shownimpname = null;
		if (root != null)
			shownimpname = root.getShownImpname();

		// A new panel has to be created if the model is new
		if (!modelChanged) {
			// Do not create a new panel if panel is already on the right view
			if (shownimpname == null) {
				if (impname == null) {
					return false;
				}
			}
			else if (shownimpname.equals(impname)) {// Do not create new panel, if current viewport is the same to which this panel
													// should be set
				return false;
			}
		}

		NodedisplayComp newcomp = null;

		if (root != null) {
			root.dispose();// Delete old root-element
			System.gc();
		}

		if (impname != null) {
			final DisplayNode newnode = DisplayNode.getDisplayNodeFromImpName(lgui, impname, model);

			newcomp = new NodedisplayComp(lgui, model, newnode, this, SWT.None);
		}
		else
			newcomp = new NodedisplayComp(lgui, model, this, SWT.None);// if impname is null => go up to root-level

		root = newcomp;

		this.layout();
		root.layout();

		// Send event to all zoomlisteners
		notifyZoom(impname);

		return true;
	}

}
