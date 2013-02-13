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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * API for update models connected to viewer cell editors.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateModel
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IColumnViewerLabelSupport
 * 
 * @author arossi
 * @since 1.1
 * 
 */
public interface ICellEditorUpdateModel extends IUpdateModel, IColumnViewerLabelSupport {

	/**
	 * @return whether the cell is editable
	 */
	public boolean canEdit();

	/**
	 * @return the cell editor for this model
	 */
	public CellEditor getCellEditor();

	/**
	 * @return viewer to which it belongs
	 */
	public Viewer getParent();

	/**
	 * Insert name and value into template as indicated by the pattern
	 * 
	 * @param pattern
	 *            template for constructing an output string representing all
	 *            the attribute names and values in the viewer
	 * @return the output string
	 */
	public String getReplacedValue(String pattern);

	/**
	 * @return text to display as tooltip
	 */
	public String getTooltip();

	/**
	 * @return the underlying value type appropriate for the cell editor in
	 *         question
	 */
	public Object getValueForEditor();

	/**
	 * @return whether this item (row) is checked in the viewer
	 */
	public boolean isChecked();

	/**
	 * @param background
	 *            any defined colors for each column of the viewer
	 */
	public void setBackground(Color[] background);

	/**
	 * @param checked
	 *            whether this item (row) is checked in the viewer
	 */
	public void setChecked(boolean checked);

	/**
	 * @param font
	 *            any defined fonts for each column of the viewer
	 */
	public void setFont(Font[] font);

	/**
	 * @param foreground
	 *            any defined colors for each column of the viewer
	 */
	public void setForeground(Color[] foreground);

	/**
	 * Translate the editor value into a type appropriate for storage
	 * 
	 * @param value
	 *            from the editor
	 */
	public void setValueFromEditor(Object value);

	/**
	 * @param viewerModel
	 *            the model of the viewer to which this cell model belongs
	 */
	public void setViewer(IUpdateModel viewerModel);
}
