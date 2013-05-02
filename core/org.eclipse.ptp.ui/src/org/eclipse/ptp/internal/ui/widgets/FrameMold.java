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
public class FrameMold {
	protected static int bitIndex = 0;
	
	public static final int HAS_DESCRIPTION = 1 << bitIndex++;
	public static final int HAS_FRAME = 1 << bitIndex++;
	public static final int HAS_EXPAND = 1 << bitIndex++;
	public static final int COLUMNS_EQUAL_WIDTH = 1 << bitIndex++;
	
	int bitmask;
	
	String title;
	String description;	
	int columns;
	String expandButtonLabel;
	String shrinkButtonLabel;

	public FrameMold() {
		this.bitmask = 0;
		this.columns = 1;
	}
	
	public FrameMold(String title, boolean expandable) {
		this.bitmask = (expandable ? HAS_EXPAND : 0);
		this.columns = 1;
		setTitle(title);
	}
	
	public FrameMold(String title, int columns, boolean expandable) {
		this.bitmask = (expandable ? HAS_EXPAND : 0);
		this.columns = columns;
		setTitle(title);
	}
	
	public FrameMold(String title) {
		this.bitmask = 0;
		this.columns = 1;
		setTitle(title);
	}
	
	public FrameMold(String title, int columns) {
		this.bitmask = 0;
		this.columns = columns;
		setTitle(title);
	}
	
	public FrameMold(int columns) {
		this.bitmask = 0;
		this.columns = columns;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		if (description == null) {
			bitmask &= ~HAS_DESCRIPTION;
		} else {
			bitmask |= HAS_DESCRIPTION; 
		}
	}
	
	public void addOption(int bitmask) {
		this.bitmask |= bitmask;
	}
	
	public void removeOption(int bitmask) {
		this.bitmask &= ~bitmask;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		if (title != null) {
			bitmask |= HAS_FRAME; 
		}
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public String getExpandLabel() {
		return expandButtonLabel;
	}
	
	public String getShrinkButtonLabel() {
		return shrinkButtonLabel;
	}
	
	public void setExpandButtonLabel(String expandLabel) {
		this.expandButtonLabel = expandLabel;
		this.bitmask |= HAS_EXPAND;
	}
	
	public void setShrinkButtonLabel(String shrinkLabel) {
		this.shrinkButtonLabel = shrinkLabel;
		this.bitmask |= HAS_EXPAND;
	}
	
	public void setButtonLabels(String expandLabel, String shrinkLabel) {
		this.shrinkButtonLabel = shrinkLabel;
		this.expandButtonLabel = expandLabel;
	}
}
