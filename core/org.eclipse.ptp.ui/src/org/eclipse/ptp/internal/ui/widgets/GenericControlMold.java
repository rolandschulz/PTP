/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.ui.widgets;


/**
 * @author Richard Maciel
 * 
 */
public abstract class GenericControlMold {
	int bitmask;

	/**
	 * This field is responsible for alloc bitset fields. It will always
	 * represent the smaller bitset index available. Its name comes from the
	 * abbr of exponent, because it indicates the n in the exponentiation
	 * exp(2,n)
	 */
	protected static int index = 0;

	public static final int NONE = 0;
	
	/** A label is shown above or on the left (default) of the component. */
	public static final int HAS_LABEL = 1 << index++;

	/** A label is shown above the component. Has effect only when HAS_LABEL is set. */
	public static final int LABELABOVE = 1 << index++;

	/** A tooltip is shown when the mouse moves over the component. */
	public static final int HASTOOLTIP = 1 << index++;

//	public static final int EQUALWIDTH = 1 << index++;

	/** A button is shown on the right of the component. */
	public static final int HASBUTTON = 1 << index++;

	public static final int GRID_DATA_ALIGNMENT_FILL = 1 << index++;

	public static final int GRID_DATA_GRAB_EXCESS_SPACE = 1 << index++;

	public static final int GRID_DATA_SPAN = 1 << index++;

	String label = null;

	String buttonLabel = null;

	String tooltip = null;

	/**
	 * @param hasLabel
	 */
	public GenericControlMold(int bitmask) {
		this.bitmask = bitmask;
	}

	public GenericControlMold(int bitmask, String label) {
		this.bitmask = bitmask;
		setLabel(label);
	}

	public int getBitmask() {
		return bitmask;
	}

	public void addBitmask(int bitmask) {
		this.bitmask |= bitmask;
	}

	public void removeBitmask(int bitmask) {
		this.bitmask &= ~bitmask;
	}

//	public void setBitmask(int bitmask) {
//		this.bitmask = bitmask;
//	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		if (label != null) {
			addBitmask(HAS_LABEL);
		} else {
			removeBitmask(HAS_LABEL);
		}
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		if (tooltip != null) {
			addBitmask(HASTOOLTIP);
		} else {
			removeBitmask(HASTOOLTIP);
		}
	}

	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
		if (buttonLabel != null) {
			addBitmask(HASBUTTON);
		} else {
			// not necessary, a button without label is allowed.
		}
	}

	protected boolean hasHeight() {
		return false;
	}
	protected boolean hasWidth() {
		return false;
	}
	protected int getHeight() {
		return 0;
	}
	protected int getWidth() {
		return 0;
	}
}
