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
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Extend this class to add functions to a NodedisplayView.
 * All function calls from AbstractNodedisplayView are delegated to
 * an inner NodedisplayView. This view is inserted as center-
 * composite of a Borderlayout. If you want to add a composite
 * around a nodedisplayview, you can extend this class and add
 * the widgets. Concrete decorators can be nested to create
 * nodedisplayviews with a set of desired functions.
 * 
 */
public class NodedisplayViewDecorator extends AbstractNodedisplayView {

	/**
	 * Inner NodedisplayView shown in center of this composite
	 */
	protected AbstractNodedisplayView nodedisplayView;

	/**
	 * Creates a wrapper composite, which acts like a NodedisplayView
	 * but has additional functions and widgets.
	 * 
	 * @param nodedisplayView
	 *            this new decorator becomes parent of the passed nodedisplayview.
	 *            Most function calls to the decorator are delegated to this instance.
	 *            The passed nodedisplayview is placed in the center of the decorator.
	 * @param parent
	 *            parent composite of this decorator
	 */
	public NodedisplayViewDecorator(AbstractNodedisplayView nodedisplayView, Composite parent) {

		super(nodedisplayView.getLguiItem(), parent, SWT.None);

		setLayout(new BorderLayout());

		this.nodedisplayView = nodedisplayView;
		this.nodedisplayView.setParent(this);
		this.nodedisplayView.setLayoutData(new BorderData(BorderLayout.MFIELD));
		// Add listener, which forwards nodedisplayview-events to this decorated
		// nodedisplayview. Listeners, which are added to "this", will receive
		// Zoom events from inner nodedisplayview
		this.nodedisplayView.addZoomListener(new INodedisplayZoomListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener#handleEvent(org.eclipse.ptp.rm.lml.core.events.
			 * INodedisplayZoomEvent)
			 */
			@Override
			public void handleEvent(INodedisplayZoomEvent event) {
				notifyZoom(event);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#decreaseRectangles()
	 */
	@Override
	public void decreaseRectangles() {
		nodedisplayView.decreaseRectangles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#getFixedLevel()
	 */
	@Override
	public int getFixedLevel() {
		return nodedisplayView.getFixedLevel();
	}

	@Override
	public int getMaximumNodedisplayDepth() {
		return nodedisplayView.getMaximumNodedisplayDepth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#getMinimalRectangleSize()
	 */
	@Override
	public int getMinimalRectangleSize() {
		return nodedisplayView.getMinimalRectangleSize();
	}

	@Override
	public int getMinimumLevelOfDetail() {
		return nodedisplayView.getMinimumLevelOfDetail();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#getRootNodedisplay()
	 */
	@Override
	public NodedisplayComp getRootNodedisplay() {
		return nodedisplayView.getRootNodedisplay();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#getShownMaxLevel()
	 */
	@Override
	public int getShownMaxLevel() {
		return nodedisplayView.getShownMaxLevel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#goToImpname(java.lang.String)
	 */
	@Override
	public boolean goToImpName(String impName) {
		return nodedisplayView.goToImpName(impName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#increaseRectangles()
	 */
	@Override
	public void increaseRectangles() {
		nodedisplayView.increaseRectangles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#restartZoom()
	 */
	@Override
	public void restartZoom() {
		nodedisplayView.restartZoom();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#setFixedLevel(int)
	 */
	@Override
	public void setFixedLevel(int level) {
		nodedisplayView.setFixedLevel(level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.LguiWidget#setLgui(org.eclipse.ptp.rm.lml.core.model.ILguiItem)
	 */
	@Override
	public void setLguiItem(ILguiItem lguiItem) {
		nodedisplayView.setLguiItem(lguiItem);
	}

	@Override
	public void setMaxLevel(int maxLevel) {
		nodedisplayView.setMaxLevel(maxLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#setMinimalRectangleSize(int)
	 */
	@Override
	public void setMinimalRectangleSize(int size) {
		nodedisplayView.setMinimalRectangleSize(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#update()
	 */
	@Override
	public void update() {
		nodedisplayView.update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem)
	 */
	@Override
	public void update(ILguiItem lguiItem) {
		nodedisplayView.update(lguiItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#update(org.eclipse.ptp.rm.lml.core.model.ILguiItem,
	 * org.eclipse.ptp.rm.lml.core.elements.Nodedisplay)
	 */
	@Override
	public void update(ILguiItem lguiItem, Nodedisplay nodedisplay) {
		nodedisplayView.update(lguiItem, nodedisplay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#zoomIn(java.lang.String)
	 */
	@Override
	public void zoomIn(String impName) {
		nodedisplayView.zoomIn(impName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView#zoomOut()
	 */
	@Override
	public void zoomOut() {
		nodedisplayView.zoomOut();
	}

}
