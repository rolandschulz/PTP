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

package org.eclipse.ptp.internal.rm.lml.core.model;

import org.eclipse.ptp.rm.lml.core.model.ITableColumnLayout;

/**
 * Class of the interface ITableColumnLayout
 */
public class TableColumnLayout implements ITableColumnLayout {

	/*
	 * Title of the column
	 */
	private String title;

	/*
	 * Width of the column
	 */
	private Double width;

	/*
	 * Style of the column
	 */
	private String style;

	/*
	 * Activity of the column.
	 */
	private final boolean active;

	/*
	 * Order of the data.
	 */
	private String order;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            title of the column
	 * @param width
	 *            width of the column
	 * @param style
	 *            style of the column
	 * @param activity
	 *            of the column
	 */
	public TableColumnLayout(String title, Double width, String style, boolean active, String order) {
		this.title = title;
		this.width = width;
		this.style = style;
		this.active = active;
		this.order = order;
		if (title == null) {
			this.title = new String();
		}
		if (width == null) {
			this.width = new Double(0);
		}
		if (style == null) {
			this.style = ITableColumnLayout.COLUMN_STYLE_LEFT;
		}
		if (order == null) {
			this.order = new String("alpha");
		}
	}

	public String getOrder() {
		return order;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getStyle()
	 */
	public String getStyle() {
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.IJobTableColumnLayout#getWidth()
	 */
	public Double getWidth() {
		return width;
	}

	public boolean isActive() {
		return active;
	}

}
