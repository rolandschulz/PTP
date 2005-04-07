/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;

/**
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
