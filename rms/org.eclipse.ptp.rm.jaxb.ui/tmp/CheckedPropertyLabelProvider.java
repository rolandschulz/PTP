package org.eclipse.ptp.rm.jaxb.ui.providers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.CheckedProperty;
import org.eclipse.swt.graphics.Image;

public class CheckedPropertyLabelProvider implements ITableLabelProvider, IJAXBUINonNLSConstants {

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		CheckedProperty p = (CheckedProperty) element;
		switch (columnIndex) {
		case 0:
			return p.getName();
		case 1:
			return p.getDescription();
		}
		return ZEROSTR;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
