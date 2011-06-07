/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.List;
import java.util.Stack;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Parent composite of NodedisplayComp This class allows to zoom or switch the
 * viewports of nodedisplaycomps. Every nodedisplaycomp is connected with one
 * instance of this class. NodedisplayView represents one zoomable
 * NodedisplayComp.
 */
public class NodedisplayView extends LguiWidget {
	/*
	 * LML-Data-model for this view
	 */
	private Nodedisplay nodedisplay;
	/*
	 * creates scrollbars surrounding nodedisplay
	 */
	private final ScrolledComposite scrollpane;
	/*
	 * root nodedisplay which is currently shown
	 */
	private NodedisplayComp root = null;

	/*
	 * Saves zoom-levels to zoom out later, saves full-implicit name of nodes to
	 * create Displaynodes from these ids
	 */
	private Stack<String> zoomstack = new Stack<String>();

	/*
	 * Cursor to show while processing
	 */
	private final Cursor waitcursor;
	/*
	 * default cursor
	 */
	private final Cursor defaultcursor;

	/**
	 * Create a composite as surrounding component for NodedisplayComps. This
	 * class encapsulates zooming functionality. It saves a stack
	 * 
	 * @param pmodel
	 * @param parent
	 */
	public NodedisplayView(ILguiItem lguiItem, Nodedisplay nodedisplay, Composite parent) {

		super(lguiItem, parent, SWT.None);

		this.nodedisplay = nodedisplay;

		setLayout(new FillLayout());

		scrollpane = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);

		if (lguiItem != null && nodedisplay != null) {
			root = new NodedisplayComp(lguiItem, nodedisplay, this, SWT.None);
		}

		// Create cursors
		defaultcursor = this.getCursor();
		waitcursor = new Cursor(this.getDisplay(), SWT.CURSOR_WAIT);

		addResizeListenerForScrollPane();
		checkEmptyScreen();
	}

	@Override
	public void dispose() {
		// Dispose created cursor
		waitcursor.dispose();
	}

	/**
	 * @return currently shown nodedisplaycomp
	 */
	public NodedisplayComp getRootNodedisplay() {
		return root;
	}

	/**
	 * @return access to scrollpane, which contains the root-nodedisplay
	 */
	public ScrolledComposite getScrollPane() {
		return scrollpane;
	}

	/**
	 * Set node with impname as implicit name as root-node within this
	 * nodedisplay-panel. Call this function only if model did not changed.
	 * 
	 * @param impname
	 *            implicit name of a node, which identifies every node within a
	 *            nodedisplay
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpname(String impname) {
		return goToImpname(impname, false);
	}

	/**
	 * Set node with impname as implicit name as root-node within this
	 * nodedisplay-panel
	 * 
	 * @param impname
	 *            implicit name of a node, which identifies every node within a
	 *            nodedisplay
	 * @param modelChanged
	 *            if true a new nodedisplay is forced to be created, otherwise
	 *            only if the new impname differs from currently shown impname
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpname(String impname, boolean modelChanged) {

		if (lguiItem == null) {
			return false;
		}

		String shownimpname = null;
		if (root != null) {
			shownimpname = root.getShownImpname();
		}

		// A new panel has to be created if the model is new
		if (!modelChanged) {
			// Do not create a new panel if panel is already on the right view
			if (shownimpname == null) {
				if (impname == null) {
					return false;
				}
			} else if (shownimpname.equals(impname)) {
				/*
				 * Do not create new panel, if current viewport is the same to
				 * which this panel should be set
				 */
				return false;
			}
		}

		NodedisplayComp newcomp = null;

		if (root != null) {
			root.dispose();// Delete old root-element
			System.gc();
		}

		if (impname != null) {
			final DisplayNode newnode = DisplayNode.getDisplayNodeFromImpName(lguiItem, impname, nodedisplay);

			newcomp = new NodedisplayComp(lguiItem, nodedisplay, newnode, this, SWT.None);
		} else {
			/*
			 * if impname is null => go up to root-level
			 */
			newcomp = new NodedisplayComp(lguiItem, nodedisplay, this, SWT.None);
		}

		root = newcomp;

		this.layout();
		root.layout();

		return true;
	}

	/**
	 * The stack which saves the last zoom-levels is restarted
	 */
	public void restartZoom() {
		zoomstack = new Stack<String>();
	}

	/**
	 * Update view and repaint current data. This is done by creating a
	 * completely new nodedisplay. Tries to go to the implicitname, which was
	 * shown before.
	 */
	@Override
	public void update() {
		super.update();

		final String shownImpName = null;
		/*
		 * if(root!=null) shownImpName = root.getShownImpname();
		 */
		restartZoom();
		nodedisplay = getNewModel();

		if (nodedisplay != null) {
			goToImpname(shownImpName, true);
		}
		checkEmptyScreen();
	}

	/**
	 * Call this update if lguiitem changes. This update is calles if another
	 * system is monitored.
	 * 
	 * @param lgui
	 *            new data-manager
	 */
	public void update(ILguiItem lgui) {
		this.lguiItem = lgui;

		update();
	}

	public void zoomIn(String impname) {
		if (root == null) {
			return;
		}

		this.setCursor(waitcursor);

		String oldshown = root.getShownImpname();

		if (goToImpname(impname)) {
			if (oldshown == null) {
				oldshown = ""; //$NON-NLS-1$
			}
			zoomstack.push(oldshown);
		}

		this.setCursor(defaultcursor);
	}

	/**
	 * Go one level higher in zoomstack
	 */
	public void zoomOut() {
		this.setCursor(waitcursor);

		if (!zoomstack.isEmpty()) {
			String impname = zoomstack.pop();
			// Get back null-values
			if (impname.equals("")) { //$NON-NLS-1$
				impname = null;
			}

			// Switch view to node with impname
			goToImpname(impname);
		}

		this.setCursor(defaultcursor);
	}

	/**
	 * Adds a listener, which changes scrollbar-increments on every resize.
	 */
	private void addResizeListenerForScrollPane() {

		scrollpane.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
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
		});

	}

	private void checkEmptyScreen() {
		if (lguiItem != null) {
			setVisible(true);
		} else {
			setVisible(false);
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
		Nodedisplay res = null;
		if (lguiItem.getNodedisplayAccess() != null) {
			res = lguiItem.getNodedisplayAccess().getNodedisplayById(nodedisplayId);

			if (res == null) {
				final List<Nodedisplay> nodedisplays = lguiItem.getNodedisplayAccess().getNodedisplays();

				if (nodedisplays.size() > 0) {
					res = nodedisplays.get(0);
				}
			}

		}
		return res;
	}

}
