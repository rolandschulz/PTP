/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 *  M Venkataramana - original code: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;

public class ColumnDescriptor implements IJAXBUINonNLSConstants {

	private final String columnName;
	private final Integer width;
	private final String alignment;
	private final Boolean resizable;
	private final Boolean moveable;
	private final String tooltip;
	private final String foreground;
	private final String background;

	public ColumnDescriptor(ColumnData data) {
		columnName = data.getName();
		width = data.getWidth();
		alignment = data.getAlignment();
		resizable = data.isResizable();
		moveable = data.isMoveable();
		tooltip = data.getTooltip();
		foreground = data.getForeground();
		background = data.getBackground();
	}

	public int getAlignment() {
		return WidgetBuilderUtils.getStyle(alignment);
	}

	public String getBackground() {
		return background;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getForeground() {
		return foreground;
	}

	public String getTooltip() {
		if (tooltip == null) {
			return ZEROSTR;
		}
		return tooltip;
	}

	public int getWidth() {
		return width;
	}

	public boolean isAlignSpecified() {
		return alignment != null;
	}

	public boolean isBackgroundSpecified() {
		return background != null;
	}

	public boolean isForegroundSpecified() {
		return foreground != null;
	}

	public boolean isMoveable() {
		return moveable != null && moveable;
	}

	public boolean isResizable() {
		return resizable == null || resizable;
	}

	public boolean isWidthSpecified() {
		return width != null;
	}
}
