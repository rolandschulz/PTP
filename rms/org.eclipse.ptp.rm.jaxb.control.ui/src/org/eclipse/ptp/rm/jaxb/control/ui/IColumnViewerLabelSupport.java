/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Interface implemented by cell editor update models to support cell label
 * providers.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.ICellEditorUpdateModel
 * 
 * @author arossi
 * @since 1.1
 * 
 */
public interface IColumnViewerLabelSupport extends ITableColorProvider, ITableFontProvider {

	/**
	 * @param columnName
	 * @return image
	 */
	public Image getColumnImage(String columnName);

	/**
	 * @return description (only Attributes)
	 */
	public String getDescription();

	/**
	 * @param columnName
	 * @return the text label for the given column
	 */
	public String getDisplayValue(String columnName);

	/**
	 * @return tooltip (only Attributes)
	 */
	public String getTooltip();
}
