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
package org.eclipse.ptp.rm.lml.ui.providers.support;

import java.text.DecimalFormat;

import org.eclipse.ptp.internal.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.model.IUsagebarInterpreter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;

/**
 * This class is used for managing a scale for a diagram. It is specially designed for
 * usagebars from lml-model. Saves minimum and maximum allowed value in the scale.
 * The scale can be painted by passing a GC-instance to the function paint.
 * Usagebars are also included into nodedisplays for level of detail visualization.
 * The scale provides two modes: showing CPU-count or showing node-count. A node is
 * a group of cpu. The amount of cpu per node is defined by the lml-usagebar-tag.
 * The first mode is chosen, if the usagebar-interpreter is set to null. Otherwise
 * the scale is painted in the second mode.
 * 
 * 
 * @author karbach
 */
public class Scale {

	/**
	 * Saves the font, which was last created by a function of this class.
	 * Saves the font, which still has to be released later.
	 */
	private Font lastCreatedFont;

	/**
	 * Minimum allowed value
	 */
	private double min;

	/**
	 * Maximum allowed value
	 */
	private double max;

	/**
	 * stepwidth between two tickmarks
	 */
	private double interval;

	/**
	 * color of the painted scale
	 */
	private Color color;

	/**
	 * Used for special non-equidistant scale for nodes, if null use equidistant scale.
	 * Holds algorithm to map CPU ID to node ID.
	 */
	private IUsagebarInterpreter usagebarInterpreter;

	/**
	 * If true, a string is painted indicating the unit used by this scale.
	 */
	private boolean paintUnit;

	/**
	 * Create a scale with value interval [pmin, pmax].
	 * The tickmark-distance is set to pinterval.
	 * Set color to black.
	 * 
	 * @param min
	 *            Minimum allowed value
	 * @param max
	 *            Maximum allowed value
	 * @param interval
	 *            stepwidth between two tickmarks
	 */
	public Scale(double min, double max, double interval) {
		if (min > max) {
			final double tmp = max;
			max = min;
			min = tmp;
		}

		this.min = min;
		this.max = max;
		setInterval(interval);
		color = null;

		usagebarInterpreter = null;
		paintUnit = true;
	}

	/**
	 * @return color of the scale
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return distance between two painted vertical lines
	 */
	public double getInterval() {
		return interval;
	}

	/**
	 * @return maximum allowed value
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @return minimum allowed value
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @return true, if this scale paints nodes as unit, otherwise (CPU as unit) false
	 */
	public boolean isShowingNodescale() {
		return usagebarInterpreter != null;
	}

	/**
	 * @return true, if used unit is painted, false otherwise
	 */
	public boolean isShowingUnit() {
		return paintUnit;
	}

	/**
	 * Paint the scale by using the Graphics-instance gc.
	 * The scale is painted in the rectangle defined by width and height.
	 * 
	 * @param gc
	 *            Graphics-instance for painting on
	 * @param xOff
	 *            offset for painting in x-direction in pixels
	 * @param yOff
	 *            offset for painting in y-direction in pixels
	 * @param width
	 *            width of the rectangle where to paint the scale
	 * @param height
	 *            height of the rectangle where to paint the scale
	 * @param lineFactor
	 *            percent value (0..1) defining how big the line should be painted
	 */
	public void paint(GC gc, int xOff, int yOff, int width, int height, double lineFactor) {

		if (color == null) {
			color = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
		}
		gc.setForeground(color);

		double diff = max - min;// is calculated in cpus

		double rmin = min;// min-variable for node-scale

		if (usagebarInterpreter != null) {// if scale shows nodes, conversion is needed
			rmin = usagebarInterpreter.getLastCpuInNode((int) min);
			diff = usagebarInterpreter.getLastCpuInNode((int) max) - rmin;
		}

		final int lineHeight = (int) (lineFactor * height);

		FontMetrics fontMetrics = gc.getFontMetrics();

		// Adjust font-height size
		while (fontMetrics.getHeight() > height * (1 - lineFactor)) {
			if (!decreaseFontSize(gc)) {
				break;
			}
			fontMetrics = gc.getFontMetrics();
		}

		fontMetrics = gc.getFontMetrics();

		// Adjust width of font
		final String last = showString(max);// longest possible value, if only numerical values are printed
		if (usagebarInterpreter == null) {
			while (getStringWidth(gc, last) > interval / diff * width) {
				if (!decreaseFontSize(gc)) {
					break;
				}
				fontMetrics = gc.getFontMetrics();
			}
		}
		else {
			while (getStringWidth(gc, last) > interval / (max - min) * width) {
				if (!decreaseFontSize(gc)) {
					break;
				}
				fontMetrics = gc.getFontMetrics();
			}
		}

		fontMetrics = gc.getFontMetrics();

		for (int i = 1; i * interval < max; i++) {

			int x = (int) (((i * interval - min) * width) / diff + xOff);

			if (usagebarInterpreter != null) { // draw node-scale
				final int cpu = usagebarInterpreter.getLastCpuInNode((int) Math.round(i * interval));
				x = (int) (((cpu - rmin) * width) / diff);
			}

			final String paintNumber = showString(i * interval);
			final int strWidth = getStringWidth(gc, paintNumber);

			if (x + strWidth / 2 >= xOff + width) {// Avoid tickmarks, where the painted number i not entirely visible
				break;
			}

			gc.drawLine(x, yOff, x, yOff + lineHeight - 1);

			gc.drawString(paintNumber, x - strWidth / 2, yOff + lineHeight, true);
		}

		paintUnit(gc, yOff, width);
	}

	/**
	 * Set new painting color.
	 * 
	 * @param color
	 *            new used color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Set a new tickmark distance.
	 * 
	 * @param interval
	 *            new distance
	 */
	public void setInterval(double interval) {
		this.interval = Math.abs(interval);
		if (this.interval == 0) {
			this.interval = 1;
		}
	}

	/**
	 * Set right end of scale.
	 * 
	 * @param max
	 *            new max-value.
	 */
	public void setMax(double max) {
		if (max > min) {
			this.max = max;
		}
	}

	/**
	 * Set left end of scale.
	 * 
	 * @param pmin
	 *            new min-value
	 */
	public void setMin(double min) {
		if (min < max) {
			this.min = min;
		}
	}

	/**
	 * Switch unit painting behaviour. If true the used unit is painted.
	 * Otherwise no unit is painted.
	 * 
	 * @param paintUnit
	 */
	public void setPaintUnit(boolean paintUnit) {
		this.paintUnit = paintUnit;
	}

	/**
	 * Set UsagebarInterpreter reference for mapping nodes to cpu
	 * to implement the special (non equidistant) scale.
	 * Set to null if equidistant scale of CPUs should be shown.
	 * 
	 * @param pusage
	 *            algorithm for mapping nodes to cpu
	 */
	public void setUsagebarInterpreter(IUsagebarInterpreter usage) {
		usagebarInterpreter = usage;
	}

	/**
	 * Overwrite this function to alter string-output of the scale.
	 * The function defines, how scale values should be printed.
	 * Converts a double-value into a formatted string.
	 * 
	 * Here: round the value 2.5 -> 2 ; 2.51 -> 3, if usagebar==null
	 * 
	 * if usagebar!=null call getNodecount to draw a special scale
	 * 
	 * @param value
	 *            numeric value
	 * @return formatted string for this value
	 */
	public String showString(double value) {
		return new DecimalFormat("0").format(value);//$NON-NLS-1$
	}

	/**
	 * Decrease the font size of the passed Graphics-instance.
	 * 
	 * @param gc
	 *            Graphics-instance, which should be altered
	 */
	private boolean decreaseFontSize(GC gc) {
		if (gc.getFont().getFontData().length == 0) {
			return false;
		}

		final FontData fontData[] = gc.getFont().getFontData();
		final int currentHeight = fontData[0].getHeight();
		if (currentHeight <= 1) {
			return false;
		}

		return (setFontSize(gc, currentHeight - 1));
	}

	/**
	 * Calculates the width of the toPrint string painted on the
	 * current GC.
	 * 
	 * @param gc
	 *            the graphics context, on which the string will be painted
	 * @param toPrint
	 *            the string, which is painted
	 * @return the width of this string
	 */
	private int getStringWidth(GC gc, String toPrint) {
		return gc.stringExtent(toPrint).x;
	}

	/**
	 * Paint a string indicating, which unit type was used for the painted scale.
	 * The string is placed at the lower right corner of the usagebar.
	 * 
	 * @param gc
	 *            the graphics context
	 * @param yOff
	 *            the height of the usagebar without the scale's height, the y-start coordinate of the scale
	 * @param width
	 *            the width of the scale-paint-area
	 */
	private void paintUnit(GC gc, int yOff, int width) {

		final String xAxis = isShowingNodescale() ? Messages.UsagebarPainter_NodeText : Messages.UsagebarPainter_CPUText;

		final FontMetrics fontMetrics = gc.getFontMetrics();

		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
		final int textBorder = 5;
		gc.fillRectangle(width - getStringWidth(gc, xAxis) - 2 * textBorder, yOff - (fontMetrics.getHeight() + textBorder) / 2,
				getStringWidth(gc, xAxis) + 2 * textBorder, fontMetrics.getHeight() + textBorder);

		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
		gc.drawString(xAxis, width - getStringWidth(gc, xAxis) - textBorder, yOff + textBorder / 2
				- (fontMetrics.getHeight() + textBorder) / 2, true);
	}

	/**
	 * Set a new font-size for the gc-instance.
	 * 
	 * @param gc
	 *            the graphical system
	 * @param size
	 *            the new size
	 * @return true, if new size was set successfully, false otherwise
	 */
	private boolean setFontSize(GC gc, int size) {
		if (gc.getFont().getFontData().length == 0) {
			return false;
		}

		final FontData fontData[] = gc.getFont().getFontData();
		// Alter all included fonts
		for (final FontData data : fontData) {
			data.setHeight(size);
		}

		final Font newFont = new Font(gc.getDevice(), fontData);
		gc.setFont(newFont);

		// Release fonts, which were created before
		if (lastCreatedFont != null) {
			lastCreatedFont.dispose();
		}

		lastCreatedFont = newFont;

		return gc.getFont().getFontData()[0].getHeight() == size;
	}

}
