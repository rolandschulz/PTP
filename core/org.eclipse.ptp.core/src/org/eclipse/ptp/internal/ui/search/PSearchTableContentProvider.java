package org.eclipse.ptp.internal.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;

/**
 * @author Clement
 *
 */
public class PSearchTableContentProvider extends PSearchContentProvider implements IStructuredContentProvider {
	private TableViewer tableViewer;

	public PSearchTableContentProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PSearchResult)
			return ((PSearchResult)inputElement).getElements();
		return EMPTY_ARR;
	}

	public void elementsChanged(Object[] updatedElements) {
		if (result == null)
			return;

		int addCount= 0;
		int removeCount= 0;
		for (int i= 0; i < updatedElements.length; i++) {
			if (result.getMatchCount(updatedElements[i]) > 0) {
				if (tableViewer.testFindItem(updatedElements[i]) != null)
					tableViewer.refresh(updatedElements[i]);
				else
					tableViewer.add(updatedElements[i]);
				addCount++;
			} else {
				tableViewer.remove(updatedElements[i]);
				removeCount++;
			}
		}
	}

	public void clear() {
		refresh();
	}
	public void refresh() {
	    tableViewer.refresh();	    
	}	
}
