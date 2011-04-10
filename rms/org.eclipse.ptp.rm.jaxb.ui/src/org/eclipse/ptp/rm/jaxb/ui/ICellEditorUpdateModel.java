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

public interface ICellEditorUpdateModel extends IUpdateModel, IColumnViewerLabelSupport {

	boolean canEdit();

	CellEditor getCellEditor();

	String getReplacedValue(String pattern);

	String getTooltip();

	Object getValueForEditor();

	boolean isSelected();

	void setSelected(boolean selected);

	void setValueFromEditor(Object value);

	void setViewer(ViewerUpdateModel viewerModel);
}
