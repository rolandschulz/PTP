package org.eclipse.ptp.rm.jaxb.ui.sorters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;

/*
 * Sorts only on name.
 */
public class AttributeViewerSorter extends ViewerSorter implements IJAXBUINonNLSConstants {
	protected int toggle = 1;

	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		int result = 0;

		if (o1 instanceof AttributeViewerRowData && o2 instanceof AttributeViewerRowData) {
			AttributeViewerRowData r1 = (AttributeViewerRowData) o1;
			AttributeViewerRowData r2 = (AttributeViewerRowData) o2;
			String name1 = r1.getColumnDisplayValue(COLUMN_NAME);
			String name2 = r2.getColumnDisplayValue(COLUMN_NAME);
			result = name1.compareTo(name2);
		}

		return result * toggle;
	}

	public void toggle() {
		toggle *= -1;
	}

}
