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
package org.eclipse.ptp.rm.lml.ui.providers.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.internal.rm.lml.core.model.UsageAdapter;
import org.eclipse.ptp.internal.rm.lml.core.model.UsagebarInterpreter;
import org.eclipse.ptp.internal.rm.lml.core.model.UsagebarInterpreter.JobComparator;
import org.eclipse.ptp.rm.lml.core.elements.JobType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.elements.UsageType;
import org.eclipse.ptp.rm.lml.core.elements.UsagebarType;
import org.eclipse.ptp.rm.lml.core.elements.UsagebarlayoutType;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.IUsagebarInterpreter;
import org.eclipse.ptp.rm.lml.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.core.model.ObjectStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * This painter is used to draw an overview of currently running jobs. The jobs are sorted by their
 * size (number of used CPUs). They are drawn in a horizontal bar. The bar gives a
 * brief overview of the supercomputer's used capacity. The painter can be used
 * as stand-alone widget or included as part of a nodedisplay.
 */
public class UsagebarPainter implements PaintListener {

	/**
	 * This inner class connects a job with the corresponding interval,
	 * on which this job is running.
	 * 
	 */
	public static class JobInterval {

		/**
		 * Coordinates in x-direction relative to this component.
		 * The connected job is painted in a rectangle starting with its left edge
		 * at start and ending with its right edge with end.
		 */
		public int start, end;

		/**
		 * Width of the bar in pixels, which is colored by the job color
		 */
		public int innerWidth;

		/**
		 * Inner width plus frame widths
		 */
		public int outerWidth;

		/**
		 * Number of pixels used for the frame
		 */
		public int frame;

		/**
		 * The job painted in this rectangle.
		 */
		public ObjectType job;

		/**
		 * Create a JobInterval saving a job together with the horizontal start- and
		 * end coordinate.
		 * 
		 * @param job
		 *            The job painted in this rectangle.
		 * @param start
		 *            the starting x-coordinate of the painted rectangle for this job.
		 * @param end
		 *            the end x-coordinate of the painted rectangle for this job.
		 * @param innerWidth
		 *            Width of the bar in pixels, which is colored by the job color
		 * @param outerWidth
		 *            Inner width plus frame widths
		 * @param frame
		 *            Number of pixels used for the frame
		 */
		public JobInterval(ObjectType job, int start, int end, int innerWidth, int outerWidth, int frame) {
			this.job = job;
			this.start = start;
			this.end = end;
			this.innerWidth = innerWidth;
			this.outerWidth = outerWidth;
			this.frame = frame;
		}
	}

	/**
	 * Helper function.
	 * 
	 * @param a
	 *            dividend
	 * @param b
	 *            divisor
	 * @return a/b if a%b==0 else a/b+1, while <code>/</code> is meant as integer division
	 */
	public static int ceilDiv(int a, int b) {
		if (a % b == 0) {
			return a / b;
		} else {
			return a / b + 1;
		}
	}

	/**
	 * LML-model for this painter.
	 */
	private final UsageAdapter usageAdapter;
	/**
	 * current layout for this usagebar
	 */
	private final UsagebarlayoutType layout;

	/**
	 * Saves absolute intervals in pixels within the horizontal bar. This is for mapping
	 * pixels on the screen to painted jobs
	 * 
	 */
	private List<JobInterval> jobIntervals;

	/**
	 * The list of jobs sorted by their size (number of CPUs per job)
	 */
	private final List<JobType> jobs;

	/**
	 * Width of normal frame around job-rectangles in pixels.
	 */
	private int standardFrame;

	/**
	 * Width of frame around job-rectangles of focussed jobs in pixels.
	 */
	private int mouseOverFrame;

	/**
	 * Instance for painting a scale below the usagebar.
	 */
	private final Scale scale;

	/**
	 * barFactor*(total height) of this panel are used for the colored bar painting
	 */
	private double barFactor = 0.6;

	/**
	 * In the scale lineFactor*(height of scale) are used for line-paintings.
	 * Right below the lines the tic-mark labels are painted.
	 * This factor is independent from the barFactor. It has values in the interval [0..1].
	 * A factor of 1 means that all space for the scale is used by lines.
	 */
	private double lineFactor = 0.2;

	/**
	 * Defines the minimum amount of tickmarks painted in the usagebar
	 */
	private static final int minTickmarks = 10;

	/**
	 * When tickmarks are adjusted automatically, they are made multiples of this value.
	 */
	private static final int tickMarkMultipleOf = 10;

	/**
	 * Area within the using composite, which is used for painting this usagebar
	 */
	private Rectangle paintArea;

	/**
	 * LML-handling instance used for easy access to LML-data.
	 */
	private final ILguiItem lguiItem;

	/**
	 * Holds algorithm to map node-ID to cpu-ID.
	 */
	private final IUsagebarInterpreter usagebarInterpreter;

	/**
	 * Composite, which uses this listener.
	 * Is used to gather information about the available painting
	 * area for this listener.
	 */
	private Composite usagebarComp;

	/**
	 * If true, a scale is painted below the usagebar.
	 */
	private boolean paintScale;

	/**
	 * Paint a usagebar on a complete composite.
	 * Thus the composite using this listener is passed to the constructor.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usageAdapter
	 *            the painted usagebar
	 * @param usagebarComp
	 *            composite, on which the usagebar is painted
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsageAdapter usageAdapter, Composite usagebarComp) {
		this(lguiItem, usageAdapter, new Rectangle(0, 0, usagebarComp.getSize().x, usagebarComp.getSize().y));
		this.usagebarComp = usagebarComp;
	}

	/**
	 * Create a new usagebarPainter by passing the corresponding lml-tag
	 * as UsagebarType-instance. Paint on the passed paintArea.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usageAdapter
	 *            the painted usagebar
	 * @param paintArea
	 *            rectangle on the composite, on which this listener paints
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsageAdapter usageAdapter, Rectangle paintArea) {

		this.lguiItem = lguiItem;

		this.usageAdapter = usageAdapter;
		this.paintArea = paintArea;
		layout = lguiItem.getLayoutAccess().getUsagebarLayout(usageAdapter.getId());

		// Init usagebar interpreter
		usagebarInterpreter = new UsagebarInterpreter(usageAdapter);

		// set defaults
		standardFrame = 1;
		mouseOverFrame = 3;

		scale = new Scale(0, 0, layout.getInterval().intValue());

		setShowModus(!usageAdapter.isUsageType() && layout.getScale().equals("nodes")); //$NON-NLS-1$

		paintScale = true;

		jobs = usageAdapter.getJob();
		final JobComparator comp = new JobComparator();

		Collections.sort(jobs, comp);
	}

	/**
	 * Constructor adapter for passing a UsagebarType.
	 * 
	 * Paint a usagebar on a complete composite.
	 * Thus the composite using this listener is passed to the constructor.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usagebar
	 *            the painted usagebar
	 * @param usagebarComp
	 *            composite, on which the usagebar is painted
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsagebarType usagebar, Composite usagebarComp) {
		this(lguiItem, new UsageAdapter(usagebar), usagebarComp);
	}

	/**
	 * Constructor adapter for passing a UsagebarType.
	 * 
	 * Create a new usagebarPainter by passing the corresponding lml-tag
	 * as UsagebarType-instance. Paint on the passed paintArea.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usagebar
	 *            the painted usagebar
	 * @param paintArea
	 *            rectangle on the composite, on which this listener paints
	 * 
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsagebarType usagebar, Rectangle paintArea) {
		this(lguiItem, new UsageAdapter(usagebar), paintArea);
	}

	/**
	 * Constructor adapter for passing a UsageType.
	 * 
	 * Paint a usagebar on a complete composite.
	 * Thus the composite using this listener is passed to the constructor.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usage
	 *            the painted usagebar
	 * @param usagebarComp
	 *            composite, on which the usagebar is painted
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsageType usage, Composite usagebarComp) {
		this(lguiItem, new UsageAdapter(usage), usagebarComp);
	}

	/**
	 * Constructor adapter for passing a UsageType.
	 * 
	 * Create a new usagebarPainter by passing the corresponding lml-tag
	 * as UsagebarType-instance. Paint on the passed paintArea.
	 * 
	 * @param lguiItem
	 *            LML-access helper
	 * @param usage
	 *            the painted usagebar
	 * @param paintArea
	 *            rectangle on the composite, on which this listener paints
	 */
	public UsagebarPainter(ILguiItem lguiItem, UsageType usage, Rectangle paintArea) {
		this(lguiItem, new UsageAdapter(usage), paintArea);
	}

	/**
	 * Fills the jobIntervals attribute with all jobs, which have to be painted.
	 */
	public void detectJobPositions() {
		// Update the paint area on its own if usagebarComp was passed to this listener
		// Otherwise updatePaintArea has to be called externally
		if (usagebarComp != null) {
			updatePaintArea(new Rectangle(0, 0, usagebarComp.getSize().x, usagebarComp.getSize().y));
		}

		jobIntervals = new ArrayList<JobInterval>();

		int cpuSum = 0;
		// paint bar
		for (final JobType job : jobs) {

			final JobInterval jobInt = getCurrentJobInterval(job, cpuSum);

			final int currentCPUs = job.getCpucount().intValue(); // current amount of cpus for this job
			cpuSum += currentCPUs;

			jobIntervals.add(jobInt);
		}

	}

	/**
	 * Use this function in mouse-over events of surrounding
	 * widgets. Retrieve the job-instance at the x-coordinate x
	 * passed to this function.
	 * 
	 * @param x
	 *            the relative x-coordinate for this listener. (x=0 means left edge of this listener's paint area)
	 * @return job painted on this x-coordinate or null if there is no job at this position
	 */
	public ObjectType getJobAtPosition(int x) {
		if (jobIntervals == null) {
			return null;
		}
		for (final JobInterval jobInterval : jobIntervals) {
			if (x >= jobInterval.start && x <= jobInterval.end) {
				return jobInterval.job;
			}
		}
		return null;
	}

	/**
	 * @return absolute intervals in pixels, in which the job rectangles are painted
	 */
	public List<JobInterval> getJobIntervals() {
		return jobIntervals;
	}

	/**
	 * @return width of frame around job-rectangles of focussed jobs in pixels.
	 */
	public int getMouseOverFrame() {
		return mouseOverFrame;
	}

	/**
	 * @return current scale instance
	 */
	public Scale getScale() {
		return scale;
	}

	/**
	 * @return width of normal frame around job-rectangles in pixels.
	 */
	public int getStandardFrame() {
		return standardFrame;
	}

	/**
	 * @return true, if the scale is painted.
	 */
	public boolean isPaintingScale() {
		return paintScale;
	}

	/**
	 * @return true, if scale-unit is nodes, false otherwise
	 */
	public boolean isShowingNodescale() {
		return scale.isShowingNodescale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	public void paintControl(PaintEvent event) {

		// Update the paint area on its own if usagebarComp was passed to this listener
		// Otherwise updatePaintArea has to be called externally
		if (usagebarComp != null) {
			updatePaintArea(new Rectangle(0, 0, usagebarComp.getSize().x, usagebarComp.getSize().y));
		}

		final GC gc = event.gc;

		final OIDToObject oidToObject = lguiItem.getOIDToObject();
		final ObjectStatus objectStatus = lguiItem.getObjectStatus();

		final int width = paintArea.width;
		final int barHeight = (int) (paintArea.height * barFactor); // Height of the bar

		jobIntervals = new ArrayList<JobInterval>();

		final int allCPU = usageAdapter.getCpuCount().intValue();

		int cpuSum = 0;

		// paint bar
		for (final JobType job : jobs) {

			final JobInterval jobInt = getCurrentJobInterval(job, cpuSum);

			final int currentCPUs = job.getCpucount().intValue(); // current amount of cpus for this job
			cpuSum += currentCPUs;

			Color jobColor = ColorConversion.getColor(oidToObject.getColorById(job.getOid()));

			final ObjectType jobObject = jobInt.job;

			if (!objectStatus.isMouseDown(jobObject) && objectStatus.isAnyMouseDown()) {
				jobColor = ColorConversion.getColor(oidToObject.getColorById(null));
			}

			gc.setBackground(event.display.getSystemColor(SWT.COLOR_BLACK));
			gc.fillRectangle(jobInt.start, paintArea.y, jobInt.outerWidth, barHeight);

			gc.setBackground(jobColor);
			gc.fillRectangle(jobInt.start + jobInt.frame, jobInt.frame + paintArea.y, jobInt.outerWidth - 2 * jobInt.frame,
					barHeight - 2 * jobInt.frame);

			jobIntervals.add(jobInt);
		}

		// Paint empty part
		if (cpuSum < allCPU) {
			final int x = (cpuSum * width) / allCPU;

			gc.setBackground(event.display.getSystemColor(SWT.COLOR_BLACK));
			gc.fillRectangle(x + paintArea.x, 0 + paintArea.y, width - x, barHeight);

			Color jobColor = ColorConversion.getColor(oidToObject.getColorById("empty")); //$NON-NLS-1$

			if (!objectStatus.isMouseDown(oidToObject.getObjectById("empty")) && objectStatus.isAnyMouseDown()) { //$NON-NLS-1$
				jobColor = ColorConversion.getColor(oidToObject.getColorById(null));
			}

			gc.setBackground(jobColor);

			gc.fillRectangle(x + standardFrame + paintArea.x, standardFrame + paintArea.y, width - x - 2 * standardFrame, barHeight
					- 2 * standardFrame);
		}

		// Paint the scale if desired
		if (paintScale) {
			final int scaleHeight = paintArea.height - barHeight;
			scale.paint(gc, paintArea.x, barHeight + paintArea.y, width, scaleHeight, lineFactor);
		}
	}

	/**
	 * Define how much space is taken by the bar consisting of colored rectangles
	 * and how much space is given to the scale below the bar.
	 * 
	 * @param factor
	 *            percentage value between 0 and 1
	 */
	public void setBarFactor(double factor) {
		if (factor >= 0 && factor <= 1) {
			barFactor = factor;
		}
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
		if (factor >= 0 && factor <= 1) {
			lineFactor = factor;
		}
	}

	/**
	 * Set the frame, which is painted as border around a rectangle when the job is focused
	 * 
	 * @param mouseFrame
	 *            amount of pixels for the frame
	 */
	public void setMouseOverFrame(int mouseFrame) {
		mouseOverFrame = mouseFrame;
	}

	/**
	 * Set, if a scale should be painted.
	 * 
	 * @param paintScale
	 *            true, if the scale should be painted, false otherwise.
	 */
	public void setPaintScale(boolean paintScale) {
		this.paintScale = paintScale;
	}

	/**
	 * Sets the mode of the scale to unit nodes, if showNodes is true.
	 * Otherwise the scale-unit is cpus.
	 * 
	 * @param showNodes
	 *            true => scale-unit is nodes, otherwise cpu
	 */
	public void setShowModus(boolean showNodes) {
		scale.setInterval(layout.getInterval().intValue());

		// adjust scale
		scale.setMax(showNodes ? usagebarInterpreter.getNodeCount() : usageAdapter.getCpuCount().intValue());

		if (showNodes) {
			scale.setUsagebarInterpreter(usagebarInterpreter);
		}
		else {
			scale.setUsagebarInterpreter(null);
		}

		adjustScale();
	}

	/**
	 * Set the frame, which is painted as border around a rectangle in default case
	 * 
	 * @param standardFrame
	 *            amount of pixels for the frame
	 */
	public void setStandardFrame(int standardFrame) {
		this.standardFrame = standardFrame;
	}

	/**
	 * Set a new paint area in case of resize of the using composite.
	 * 
	 * @param paintArea
	 *            the new rectangle within the using composite, in which this listener is painting.
	 */
	public void updatePaintArea(Rectangle paintArea) {
		this.paintArea = paintArea;
	}

	/**
	 * Automatically adjusts the scale for usagetypes, which are not configured by
	 * a corresponding layout tag. Tries to let the scale paint minTickmarks many
	 * tickmarks.
	 */
	protected void adjustScale() {
		if (!usageAdapter.isUsageType()) {
			// Do not adjust scale for a configured usagebar
			return;
		}

		final double max = scale.getMax();
		final double min = 0.0;

		// Create at least as many intervals as defined by minTickmarsk
		int interval = (int) ((max - min) / minTickmarks);
		// let the interval be a multiple of tickMarkMultipleOf
		interval -= interval % tickMarkMultipleOf;
		if (interval <= 0) {
			// Try less tickmarks
			interval = (int) ((max - min) / (minTickmarks / 2));
			interval -= interval % tickMarkMultipleOf;
			if (interval <= 0) {
				interval = 1;
			}
		}

		scale.setInterval(interval);
	}

	/**
	 * Generate the pixel interval for the passed job. This
	 * function is used by the functions for painting and detecting the job positions,
	 * which have to be called separately.
	 * 
	 * @param job
	 *            the job object, for which the interval has to be generated
	 * @param cpuSum
	 *            the current sum of cpus, which are painted before this job
	 * @return the job interval where to paint the current job
	 */
	protected JobInterval getCurrentJobInterval(JobType job, int cpuSum) {
		final int width = paintArea.width;
		final int allCPU = usageAdapter.getCpuCount().intValue();
		final OIDToObject oidToObject = lguiItem.getOIDToObject();
		final ObjectType jobObject = oidToObject.getObjectById(job.getOid());

		final int barHeight = (int) (paintArea.height * barFactor); // Height of the bar

		final int currentCPUs = job.getCpucount().intValue(); // current amount of cpus for this job

		final int x = (cpuSum * width) / allCPU;

		int outerWidth = ((cpuSum + currentCPUs) * width) / allCPU - x + 1;

		if (outerWidth + x >= width) {
			outerWidth--;
		}

		int frame = standardFrame;
		// React to mouse-over-event
		if (lguiItem.getObjectStatus().isMouseOver(jobObject)) {
			frame = mouseOverFrame;
		}
		// avoid negative widths and heights
		if (outerWidth - 2 * frame < 0) {
			frame = 1;
		}
		if (barHeight - 2 * frame < 0) {
			frame = 1;
		}
		final int innerWidth = outerWidth - 2 * frame;

		// Save interval for this job
		final int end = ((cpuSum + currentCPUs) * width) / allCPU - 1;

		return new JobInterval(jobObject, x + paintArea.x, end + paintArea.x, innerWidth, outerWidth, frame);
	}
}
