/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.events.NodedisplayZoomEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.RectangleSizeChangeEvent;
import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.events.IRectangleSizeChangeEvent;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.listeners.IRectangleSizeChangeListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.swt.widgets.Composite;

/**
 * This class defines functions, which must be provided
 * by a view, which shows a nodedisplay. A nodedisplay is
 * a graphical overview of a parallel system's state. It
 * has functions to zoom into parts of the view and to
 * update the output if LML-data changed.
 * An AbstractNodedisplayView implements zoom-listeners,
 * which are called by subclasses every time a new node
 * was set as root-node.
 */
public abstract class AbstractNodedisplayView extends LguiWidget {

	/**
	 * List of listeners for zooming-events
	 */
	private final List<INodedisplayZoomListener> zoomListeners;

	/**
	 * All listeners interested in rectangle minimum size changes
	 */
	private final List<IRectangleSizeChangeListener> rectangleListeners;

	/**
	 * If this attribute is set to a positive value, layout definitions will be
	 * overridden for the nodedisplay. The amount of nested tree levels
	 * is then set to this value.
	 */
	private int fixedLevel = -1;

	/**
	 * Creates an abstract nodedisplay view with a data-handler lgui
	 * a given parent composite and swt-style.
	 * 
	 * @param lguiItem
	 *            lml-data-handler
	 * @param parent
	 *            parent composite
	 * @param style
	 *            SWT-style
	 */
	public AbstractNodedisplayView(ILguiItem lguiItem, Composite parent, int style) {
		super(lguiItem, parent, style);

		zoomListeners = new LinkedList<INodedisplayZoomListener>();
		rectangleListeners = new LinkedList<IRectangleSizeChangeListener>();
	}

	/**
	 * Add a listener, which listens for rectangle size change-events.
	 * Every time the minimum size of painted rectangles changes a new event
	 * is send to the listeners.
	 * 
	 * @param listener
	 */
	public void addRectangleListener(IRectangleSizeChangeListener listener) {
		rectangleListeners.add(listener);
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
		zoomListeners.add(listener);
	}

	/**
	 * Decreases the size of painted rectangles.
	 */
	public void decreaseRectangles() {
		if (getMinimalRectangleSize() <= 0) {
			return;
		}
		setMinimalRectangleSize(getMinimalRectangleSize() - 1);
	}

	/**
	 * @return level of expansion of this nodedisplay or a negative value, if the layout definition is used currently
	 */
	public int getFixedLevel() {
		return fixedLevel;
	}

	/**
	 * Calculates the maximum depth of the wrapped nodedisplay.
	 * For example a nodedisplay with the two levels nodes and cores
	 * will return 2.
	 * 
	 * 
	 * @return the maximum depth of the nodedisplay's LML tree defined by its scheme
	 */
	public abstract int getMaximumNodedisplayDepth();

	/**
	 * @return minimal size of painted rectangles
	 */
	public abstract int getMinimalRectangleSize();

	/**
	 * @return minimum level of detail, which can be shown by this nodedisplay
	 */
	public abstract int getMinimumLevelOfDetail();

	/**
	 * @return currently shown nodedisplaycomp
	 */
	public abstract NodedisplayComp getRootNodedisplay();

	/**
	 * @return currently used maximum amount of nested levels in this nodedisplay.
	 */
	public abstract int getShownMaxLevel();

	/**
	 * Set node with impname as implicit name as root-node within this nodedisplay-panel.
	 * Call this function only if model did not changed.
	 * 
	 * @param impName
	 *            implicit name of a node, which identifies every node within a nodedisplay
	 * @return true, if root was changed, otherwise false
	 */
	public abstract boolean goToImpName(String impName);

	/**
	 * Increases the size of painted rectangles.
	 */
	public void increaseRectangles() {
		setMinimalRectangleSize(getMinimalRectangleSize() + 1);
	}

	/**
	 * @param listener
	 */
	public void removeRectangleListener(IRectangleSizeChangeListener listener) {
		rectangleListeners.remove(listener);
	}

	/**
	 * @param listener
	 */
	public void removeZoomListener(INodedisplayZoomListener listener) {
		zoomListeners.remove(listener);
	}

	/**
	 * The stack which saves the last zoom-levels is restarted
	 */
	public abstract void restartZoom();

	/**
	 * Set the level of expansion to this value.
	 * 
	 * @param level
	 *            level of expansion for this nodedisplay
	 */
	public void setFixedLevel(int level) {
		fixedLevel = level;
	}

	/**
	 * Set the maxLevel of detail, which the nodedisplay should render.
	 * Really changes the maxLevel in the nodedisplay's layout.
	 * 
	 * @param maxLevel
	 *            the maximum level of detail shown
	 */
	public abstract void setMaxLevel(int maxLevel);

	/**
	 * Define the minimum size of rectangles painted
	 * within this nodedisplay. This function informs
	 * the RectangleSizeChange-listeners about the size change.
	 * 
	 * @param size
	 *            width and height of painted rectangles
	 */
	public void setMinimalRectangleSize(int size) {
		notifyRectangleSizeChange(size);
	}

	/**
	 * Update view and repaint current data.
	 * This is done by creating a completely new nodedisplay.
	 * Tries to go to the implicit name, which was shown
	 * before.
	 */
	@Override
	public void update() {
		super.update();
	}

	/**
	 * Call this update if lguiitem changes. This update
	 * is called if another system is monitored.
	 * 
	 * @param lguiItem
	 *            new data-manager
	 */
	public abstract void update(ILguiItem lguiItem);

	/**
	 * Main update function. Sets a new lgui-handler
	 * and a new nodedisplay-model.
	 * 
	 * @param lguiItem
	 *            lguihandler
	 * @param nodedisplay
	 *            nodedisplay model
	 */
	public abstract void update(ILguiItem lguiItem, Nodedisplay nodedisplay);

	/**
	 * Set a child-element as root-element. This causes
	 * going into a more detailed view of this part of the
	 * nodedisplay.
	 */
	public abstract void zoomIn(String impName);

	/**
	 * Go one level higher in zoomstack
	 */
	public abstract void zoomOut();

	/**
	 * Notify all listeners that the rectangle size was changed
	 * to a square with newSize pixels width and height.
	 * 
	 * @param newSize
	 *            minimum width and height in pixels of the rectangles within this nodedisplayview
	 */
	protected void notifyRectangleSizeChange(int newSize) {
		final IRectangleSizeChangeEvent event = new RectangleSizeChangeEvent(newSize);
		for (final IRectangleSizeChangeListener listener : rectangleListeners) {
			listener.handleEvent(event);
		}
	}

	/**
	 * Inform all zoom listeners about the new event.
	 * 
	 * @param event
	 *            zoom-event
	 */
	protected void notifyZoom(INodedisplayZoomEvent event) {
		for (final INodedisplayZoomListener listener : zoomListeners) {
			listener.handleEvent(event);
		}
	}

	/**
	 * Notify all listeners, that a new node with
	 * implicit name impname is now root-node.
	 * 
	 * @param impName
	 *            full implicit name of new node or null for root-nodes
	 */
	protected void notifyZoom(String impName, boolean zoomIn) {
		final NodedisplayZoomEvent event = new NodedisplayZoomEvent(impName, zoomIn);
		notifyZoom(event);
	}

}
