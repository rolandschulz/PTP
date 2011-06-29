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

package org.eclipse.ptp.rm.lml.internal.core.model;

import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;

/**
 * Class of the interface ITableColumnLayout
 */
public class TableColumnLayout implements ITableColumnLayout {

	/*
	 * Title of the column
	 */
	private final String title;

	/*
	 * Width of the column
	 */
	private final int width;

	/*
	 * Style of the column
	 */
	private final String style;

	/*
	 * Activity of the column.
	 */
	private final boolean active;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            title of the column
	 * @param size
	 *            width of the column
	 * @param width
	 *            style of the column
	 * @param activity
	 *            of the column
	 */
	public TableColumnLayout(String title, int size, String width, boolean active) {
		this.title = title;
		this.width = size;
		this.style = width;
		this.active = active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getStyle()
	 */
	public String getStyle() {
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getWidth()
	 */
	public int getWidth() {
		return width;
	}

	public boolean isActive() {
		return active;
	}

}
