package org.eclipse.ptp.internal.ui.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.ui.ParallelElementContentProvider;
import org.eclipse.swt.widgets.Display;

/**
 * @author Clement
 *
 */
public class PSearchTreeContentProvider extends PSearchContentProvider implements ITreeContentProvider {
	private AbstractTreeViewer fTreeViewer;
	protected Map fChildrenMap;
	private ParallelElementContentProvider fContentProvider;

	public PSearchTreeContentProvider(AbstractTreeViewer viewer) {
		fTreeViewer = viewer;
		fContentProvider = new ParallelElementContentProvider(false, false);
	}

	public Object getParent(Object child) {
	    return fContentProvider.getParent(child);
	}
	
	protected boolean exists(Object element) {
		if (element == null)
			return false;

		return true;
	}
	
	public Object[] getElements(Object inputElement) {
		Object[] elements = getChildren(inputElement);
		Arrays.sort(elements);
		return elements;
	}
	
	public Object[] getChildren(Object parentElement) {
		Set children = (Set)fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}	
	
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}	

	public void dispose() {
	    fChildrenMap.clear();
	    super.dispose();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		initialize((PSearchResult) newInput);
	}
	
	protected synchronized void initialize(PSearchResult result) {
		this.result = result;
		fChildrenMap= new HashMap();
		if (result != null) {
			Object[] elements = result.getElements();
        	Arrays.sort(elements);

			for (int i= 0; i < elements.length; i++) {
				insert(elements[i], false);
			}
		}
	}
	
	protected void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fTreeViewer.add(parent, child);
			} else {
				if (refreshViewer)
					fTreeViewer.refresh(parent);
				return;
			}
			child= parent;
			parent= getParent(child);
		}

		if (insertChild(result, child)) {
			if (refreshViewer)
				fTreeViewer.add(result, child);
		}
	}

	private boolean insertChild(Object parent, Object child) {
		Set children = (Set) fChildrenMap.get(parent);
		if (children == null) {
			children = new HashSet();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}	

	protected void remove(Object element, boolean refreshViewer) {
		// precondition here:  _result.getMatchCount(child) <= 0
		if (hasChildren(element)) {
			if (refreshViewer)
				fTreeViewer.refresh(element);
		} else {
			if (result.getMatchCount(element) == 0) {
				fChildrenMap.remove(element);
				Object parent= getParent(element);
				if (parent != null) {
					removeFromSiblings(element, parent);
					remove(parent, refreshViewer);
				} else {
					removeFromSiblings(element, result);
					if (refreshViewer)
						fTreeViewer.refresh();
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}	
	
	private void removeFromSiblings(Object element, Object parent) {
		Set siblings= (Set) fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}
	
	public synchronized void elementsChanged(Object[] updatedElements) {
		if (result == null)
			return;
		for (int i= 0; i<updatedElements.length; i++) {
			if (result.getMatchCount(updatedElements[i]) > 0)
				insert(updatedElements[i], true);
			else
				remove(updatedElements[i], true);
		}
	}
	
	public void clear() {
		initialize(result);
		refresh();
	}
	public void refresh() {
	    Display.getDefault().asyncExec(new Runnable() {
	       public void run() {
	           fTreeViewer.refresh();	    
	       }
	    });
	}
}
