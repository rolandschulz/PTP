package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.data.CheckedProperty;

public class CheckedPropertyContentProvider implements IStructuredContentProvider {
	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			List<CheckedProperty> cpList = new ArrayList<CheckedProperty>();
			List<?> in = (List<?>) inputElement;
			for (Object o : in) {
				if (o instanceof CheckedProperty) {
					cpList.add((CheckedProperty) o);
				} else {
					cpList.add(new CheckedProperty(o));
				}
			}
			return cpList.toArray();
		}
		return new Object[] { new CheckedProperty(inputElement) };
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
