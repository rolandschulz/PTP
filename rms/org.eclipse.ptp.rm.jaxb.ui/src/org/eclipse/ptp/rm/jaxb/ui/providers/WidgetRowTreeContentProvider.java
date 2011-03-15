package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.data.WidgetRow;

public class WidgetRowTreeContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof WidgetRow) {
			WidgetRow row = (WidgetRow) parentElement;
			List<WidgetRow> children = row.getChildren();
			if (children != null) {
				return children.toArray();
			}
		}
		return new Object[0];
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

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof WidgetRow) {
			WidgetRow parent = (WidgetRow) element;
			if (parent.getChildren() == null) {
				return false;
			}
			return !parent.getChildren().isEmpty();
		}
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
