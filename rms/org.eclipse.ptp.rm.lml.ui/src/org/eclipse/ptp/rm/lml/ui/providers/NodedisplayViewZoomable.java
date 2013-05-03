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

import org.eclipse.ptp.rm.lml.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.events.IRectangleSizeChangeEvent;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.listeners.IRectangleSizeChangeListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.swt.widgets.Composite;

/**
 * This is super class of all Nodedisplays, which allow
 * to change rectangle sizes. It is abstract because instancing
 * this class is not allowed as it does not add visible
 * functionality to a Nodedisplay.
 * 
 * Extend this class to add widgets, which change the shown
 * rectangle sizes. For example buttons, slider or spinner
 * could be used to do so.
 * 
 */
public abstract class NodedisplayViewZoomable extends NodedisplayViewDecorator {

	/**
	 * The rectangle size, which is altered in subclasses
	 */
	private int rectangleSize;

	/**
	 * Create a zoomable nodedisplay from a given base nodedisplay.
	 * 
	 * @param nodedisplay
	 *            the base nodedisplay
	 * @param parent
	 *            the parent composite, into which this new composite is nested.
	 */
	public NodedisplayViewZoomable(AbstractNodedisplayView nodedisplay, Composite parent) {

		super(nodedisplay, parent);

		rectangleSize = NodedisplayCompMinSize.defaultMinSize;

		addZoomListener();

		setRectangleSize(rectangleSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update()
	 */
	@Override
	public void update() {
		super.update();
		nodedisplayView.setMinimalRectangleSize(getRectangleSize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem)
	 */
	@Override
	public void update(ILguiItem lguiItem) {
		super.update(lguiItem);
		nodedisplayView.setMinimalRectangleSize(getRectangleSize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.NodedisplayViewDecorator#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem,
	 * org.eclipse.ptp.rm.lml.core.elements.Nodedisplay)
	 */
	@Override
	public void update(ILguiItem lguiItem, Nodedisplay nodedislay) {
		super.update(lguiItem, nodedislay);
		nodedisplayView.setMinimalRectangleSize(getRectangleSize());
	}

	/**
	 * Add a listener for zooming-events. On every zoom
	 * the rectangle size is adjusted to the current size-selection.
	 */
	private void addZoomListener() {

		nodedisplayView.addZoomListener(new INodedisplayZoomListener() {

			public void handleEvent(INodedisplayZoomEvent event) {
				nodedisplayView.setMinimalRectangleSize(getRectangleSize());
			}
		});

		nodedisplayView.addRectangleListener(new IRectangleSizeChangeListener() {
			public void handleEvent(IRectangleSizeChangeEvent event) {
				handleNewRectangleSize(nodedisplayView.getMinimalRectangleSize());
				rectangleSize = nodedisplayView.getMinimalRectangleSize();
			}
		});
	}

	/**
	 * @return current rectangle size in pixels
	 */
	protected int getRectangleSize() {
		return rectangleSize;
	}

	/**
	 * This function is called every time somehow the rectangle size
	 * of the nodedisplayview was changed.
	 * 
	 * @param size
	 *            new rectangle size
	 */
	protected abstract void handleNewRectangleSize(int size);

	/**
	 * Set new rectangle size of painted physical elements
	 * in pixels.
	 * 
	 * @param rectangleSize
	 *            rectangle size in pixels
	 */
	protected void setRectangleSize(int rectangleSize) {
		this.rectangleSize = rectangleSize;
		nodedisplayView.setMinimalRectangleSize(getRectangleSize());
	}

}
