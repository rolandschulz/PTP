package org.eclipse.ptp.internal.debug.ui.views.locations;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class PLocationViewerSorter extends ViewerSorter {
	private boolean[] sortDescending = new boolean[4];
	private int column = 0;
	
	public void setColumn(int column) {
		if (column < 0 || column > sortDescending.length)
			throw new AssertionError();
		if (this.column != column) {
			this.column = column;
		} else {
			sortDescending[column] = !sortDescending[column];
		}
	}
	
	public int getColumn() {
		return column;
	}
	
    public int compare(Viewer viewer, Object e1, Object e2) {
   		boolean isDescending = sortDescending[column];
    	if (viewer instanceof StructuredViewer) {
    		ITableLabelProvider lprov = (ITableLabelProvider) ((StructuredViewer) viewer).getLabelProvider();
			String name1 = lprov.getColumnText(e1, column);
			String name2 = lprov.getColumnText(e2, column);
			if (isDescending) {
				return name2.compareTo(name1);
			} else {
				return name1.compareTo(name2);
			}
    	}
    	return 0;
    }
    
}
