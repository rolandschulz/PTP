package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.WidgetRow;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

public class WidgetRowTableLabelProvider implements ITableLabelProvider, IJAXBUINonNLSConstants {

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		WidgetRow row = (WidgetRow) element;
		List<Control> list = row.getControls();
		if (list != null) {
			Control c = list.get(columnIndex);
			if (c != null) {
				return WidgetActionUtils.getValueString(c);
			}
		}
		return ZEROSTR;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
