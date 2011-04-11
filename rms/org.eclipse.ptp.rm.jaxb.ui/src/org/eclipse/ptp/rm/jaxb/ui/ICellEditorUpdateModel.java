/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * API for update models connected to viewer cell editors.
 * 
 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel
 * @see org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport
 * 
 * @author arossi
 * 
 */
public interface ICellEditorUpdateModel extends IUpdateModel, IColumnViewerLabelSupport {

	/**
	 * @return whether the cell is editable
	 */
	boolean canEdit();

	/**
	 * @return the cell editor for this model
	 */
	CellEditor getCellEditor();

	/**
	 * Insert name and value into template as indicated by the pattern
	 * 
	 * @param pattern
	 *            template for constructing an output string representing all
	 *            the attribute names and values in the viewer
	 * @return the output string
	 */
	String getReplacedValue(String pattern);

	/**
	 * @return text to display as tooltip
	 */
	String getTooltip();

	/**
	 * @return the underlying value type appropriate for the cell editor in
	 *         question
	 */
	Object getValueForEditor();

	/**
	 * @return whether this item (row) is checked in the viewer
	 */
	boolean isChecked();

	/**
	 * @param background
	 *            any defined colors for each column of the viewer
	 */
	void setBackground(Color[] background);

	/**
	 * @param whether
	 *            this item (row) is checked in the viewer
	 */
	void setChecked(boolean checked);

	/**
	 * @param font
	 *            any defined fonts for each column of the viewer
	 */
	void setFont(Font[] font);

	/**
	 * @param foreground
	 *            any defined colors for each column of the viewer
	 */
	void setForeground(Color[] foreground);

	/**
	 * Translate the editor value into a type appropriate for storage
	 * 
	 * @param value
	 *            from the editor
	 */
	void setValueFromEditor(Object value);

	/**
	 * @param viewerModel
	 *            the model of the viewer to which this cell model belongs
	 */
	void setViewer(ViewerUpdateModel viewerModel);
}
