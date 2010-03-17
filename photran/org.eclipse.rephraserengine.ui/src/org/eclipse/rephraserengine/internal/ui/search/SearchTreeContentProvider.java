/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.rephraserengine.internal.ui.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rephraserengine.ui.search.SearchResult;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @author Doug Schaefer
 *
 */
public class SearchTreeContentProvider implements ITreeContentProvider, ISearchContentProvider{

	private TreeViewer viewer;
	private SearchResult result;
	private Map<Object, Set<Object>> tree = new HashMap<Object, Set<Object>>();
	
	public Object[] getChildren(Object parentElement) {
		Set<Object> children = tree.get(parentElement);
		if (children == null)
			return new Object[0];
		return children.toArray();
	}

	public Object getParent(Object element) {
		Iterator<Object> p = tree.keySet().iterator();
		while (p.hasNext()) {
			Object parent = p.next();
			Set<Object> children = tree.get(parent);
			if (children.contains(element))
				return parent;
		}
		return null;
	}

 	public boolean hasChildren(Object element) {
 		return tree.get(element) != null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(result);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		result = (SearchResult)newInput;
		tree.clear();
		if (result != null) {
			Object[] elements = result.getElements();
			for (int i = 0; i < elements.length; ++i) {
				insertSearchElement((IFile)elements[i]);
			}
		}
	}

	private void insertChild(Object parent, Object child) {
		Object l_parent = parent;
		if (l_parent == null)	l_parent = result;
		Set<Object> children = tree.get(l_parent);
		if (children == null) {
			children = new HashSet<Object>();
			tree.put(l_parent, children);
		}
		children.add(child);
	}
	
	private void insertSearchElement(IFile element) {
		IContainer l_cur = element.getParent();
		insertChild(l_cur, element);
		while (l_cur != null) {
			insertChild(l_cur.getParent(), l_cur);
			l_cur = l_cur.getParent();
		}
	}
	
	public void elementsChanged(Object[] elements) {
		if (elements != null) {
			for (int i = 0; i < elements.length; ++i) {
				IFile element = (IFile)elements[i];
				if (result.getMatchCount(element) > 0)
					insertSearchElement(element);
				else
					remove(element);
			}
		}
		
		Display d= PlatformUI.getWorkbench().getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getTree().isDisposed()) {
					viewer.refresh();
				}
			}
		});
	}
	
	public void clear() {
		tree.clear();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
			    try {
			        viewer.refresh();
			    } catch (SWTException x) {
			        // Widget is disposed
			        // (occurs when running JUnit tests)
			    }
			}
		});
	}
	
	protected void remove(Object element) {
		Object parent = getParent(element);
		if (parent == null)
			// reached the search result
			return;
		
		Set<Object> siblings = tree.get(parent);
		siblings.remove(element);
		
		if (siblings.isEmpty()) {
			// remove the parent
			remove(parent);
			tree.remove(parent);
		}
	}
	
}
