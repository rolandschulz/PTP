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
