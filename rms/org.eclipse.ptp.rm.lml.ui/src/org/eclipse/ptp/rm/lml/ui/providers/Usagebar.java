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

import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.elements.UsagebarType;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.ui.providers.support.MouseInteraction;
import org.eclipse.ptp.rm.lml.ui.providers.support.UsagebarPainter;
import org.eclipse.ptp.rm.lml.ui.providers.support.UsagebarPainter.JobInterval;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This widget is mainly a wrapper around the UsagebarPainter. The painter
 * paints an overview about the system's load with current jobs. Each job is
 * painted as a rectangle, which's size is proportional to its count of cpu.
 * The result is a simple bar consisting of rectangles for each job.
 * 
 */
public class Usagebar extends LguiWidget implements Updatable {

	/**
	 * The used paintlistener, which does the main work of painting the job's rectangles.
	 */
	private final UsagebarPainter usagebarPainter;

	/**
	 * Standard mouse action handling.
	 */
	private final MouseInteraction mouse;

	/**
	 * Create a standalone widget showing a usagebar.
	 * 
	 * @param usagebar
	 *            the LML-model of one usagebar
	 * @param lguiItem
	 *            wrapper for easy LML-access
	 * @param parent
	 *            parent composite
	 * @param style
	 *            SWT style
	 */
	public Usagebar(UsagebarType usagebar, ILguiItem lguiItem, Composite parent, int style) {

		super(lguiItem, parent, style);
		// Let the paintlistener do its work
		usagebarPainter = new UsagebarPainter(getLguiItem(), usagebar, this);
		addPaintListener(usagebarPainter);

		addListener();
		// Register this component to objectstatus for update callbacks
		lguiItem.getObjectStatus().addComponent(this);

		mouse = new MouseInteraction(lguiItem, this);
	}

	/**
	 * Create a standalone widget showing a usagebar.
	 * 
	 * @param usagebar
	 *            the LML-model of one usagebar
	 * @param lguiItem
	 *            wrapper for easy LML-access
	 * @param parent
	 *            parent composite
	 * @param style
	 *            SWT style
	 */
	public Usagebar(UsageType usagebar, ILguiItem lguiItem, Composite parent, int style) {

		super(lguiItem, parent, style);
		// Let the paintlistener do its work
		usagebarPainter = new UsagebarPainter(getLguiItem(), usagebar, this);
		addPaintListener(usagebarPainter);

		addListener();
		// Register this component to objectstatus for update callbacks
		lguiItem.getObjectStatus().addComponent(this);

		mouse = new MouseInteraction(lguiItem, this);
	}

	/**
	 * Add Mouselistener to react on mouse interaction.
	 */
	public void addListener() {

		// React to mouse-over-events
		addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				mouse.mouseMoveAction(usagebarPainter.getJobAtPosition(e.x));
			}
		});

		// React to mouse-clicks and mouse-exit-events
		addMouseListener(new MouseListener() {

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
				mouse.mouseDownAction(usagebarPainter.getJobAtPosition(e.x));
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {
				mouse.mouseUpAction(usagebarPainter.getJobAtPosition(e.x));
			}

		});

		addListener(SWT.MouseExit, new Listener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			@Override
			public void handleEvent(Event event) {
				mouse.mouseExitAction();
			}

		});

	}

	@Override
	public void dispose() {
		super.dispose();
		lguiItem.getObjectStatus().removeComponent(this);
	}

	/**
	 * @return forwarded job intervals from the usagebarpainter
	 */
	public List<JobInterval> getJobIntervals() {
		usagebarPainter.detectJobPositions();// Update job intervals
		return usagebarPainter.getJobIntervals();
	}

	/**
	 * @return true, if the scale is painted.
	 */
	public boolean isPaintingScale() {
		return usagebarPainter.isPaintingScale();
	}

	/**
	 * Define how much space is taken by the bar consisting of colored rectangles
	 * and how much space is given to the scale below the bar.
	 * 
	 * @param factor
	 *            percentage value between 0 and 1
	 */
	public void setBarFactor(double factor) {
		usagebarPainter.setBarFactor(factor);
	}

	/**
	 * The barFactor defines the space for the scale. Within the scale
	 * one can decide how much space is given to tic-mark lines and their
	 * corresponding label texts. The lineFactor defines as a percentage value
	 * how much of the available space is given to the tic mark lines.
	 * 
	 * @param factor
	 *            percentage value between 0 and 1
	 */
	public void setLineFactor(double factor) {
		usagebarPainter.setLineFactor(factor);
	}

	/**
	 * Set the frame, which is painted as border around a rectangle when the job is focused
	 * 
	 * @param mouseFrame
	 *            amount of pixels for the frame
	 */
	public void setMouseOverFrame(int mouseFrame) {
		usagebarPainter.setMouseOverFrame(mouseFrame);
	}

	/**
	 * Set, if a scale should be painted.
	 * 
	 * @param paintScale
	 *            true, if the scale should be painted, false otherwise.
	 */
	public void setPaintScale(boolean paintScale) {
		usagebarPainter.setPaintScale(paintScale);
	}

	/**
	 * Set the frame, which is painted as border around a rectangle in default case
	 * 
	 * @param standardFrame
	 *            amount of pixels for the frame
	 */
	public void setStandardFrame(int standardFrame) {
		usagebarPainter.setStandardFrame(standardFrame);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rm.lml.core.model.ObjectStatus.Updatable#updateStatus(org.eclipse.ptp.rm.lml.core.elements
	 * .ObjectType, boolean, boolean)
	 */
	@Override
	public void updateStatus(ObjectType object, boolean mouseOver, boolean mouseDown) {
		if (!isDisposed()) {
			redraw();
		}
	}

}
