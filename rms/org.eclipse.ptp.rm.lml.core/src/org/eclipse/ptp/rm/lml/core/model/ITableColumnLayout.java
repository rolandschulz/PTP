/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.core.model;

/**
 * This interface presents the layout of one column of a table.
 */
public interface ITableColumnLayout {
	public static String COLUMN_STYLE_LEFT = "LEFT"; //$NON-NLS-1$
	public static String COLUMN_STYLE_RIGHT = "RIGHT"; //$NON-NLS-1$
	public static String COLUMN_STYLE_CENTER = "CENTER"; //$NON-NLS-1$

	public static String SORT_DIRECTION_UP = "UP"; //$NON-NLS-1$
	public static String SORT_DIRECTION_DOWN = "DOWN"; //$NON-NLS-1$
	public static String SORT_DIRECTION_NONE = "NONE"; //$NON-NLS-1$

	public static String COLUMN_TYPE_MANDATORY = "mandatory"; //$NON-NLS-1$
	public static String COLUMN_TYPE_OPTIONAL = "optional"; //$NON-NLS-1$

	public String getOrder();

	/**
	 * Getting the style (LEFT or RIGHT) of the column.
	 * 
	 * @return style of the column
	 */
	public String getStyle();

	/**
	 * Getting the title of the column.
	 * 
	 * @return title of the column
	 */
	public String getTitle();

	/**
	 * Getting the width of the column.
	 * 
	 * @return width of the table
	 */
	public Double getWidth();

	/**
	 * @return if table column is active or not
	 */
	public boolean isActive();
}
