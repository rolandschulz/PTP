package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.data.WidgetRow;

public class WidgetRowTableContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			List<?> list = (List<?>) inputElement;
			return list.toArray();
		}
		if (inputElement instanceof WidgetRow) {
			return new Object[] { inputElement };
		}
		return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
