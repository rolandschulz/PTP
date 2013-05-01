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
package org.eclipse.ptp.internal.debug.ui.views.variable;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.internal.debug.ui.PVariableManager.PVariableInfo;

/**
 * @author Clement chu
 */
public class PVariableViewerSorter extends ViewerSorter {
	private boolean[] sortOrder = new boolean[] { false, false, false, false };
	private int column = 0;
	
	public void setColumn(int column) {
		this.column = column;
		sortOrder[column] = !sortOrder[column];
	}
	public int getColumn() {
		return column;
	}
    public int compare(Viewer viewer, Object e1, Object e2) {
    	if (!(e1 instanceof PVariableInfo)) {
    		return super.compare(viewer, e1, e2);
    	}
    	
   		boolean isAccending = sortOrder[column];
		switch (column) {
		case 0:
			if (isAccending) {
				return ((PVariableInfo)e1).isEnabled()?1:-1;
			}
			else {
				return ((PVariableInfo)e2).isEnabled()?1:-1;
			}
		default:
	    	if (viewer instanceof StructuredViewer) {
	    		ITableLabelProvider lprov = (ITableLabelProvider) ((StructuredViewer)viewer).getLabelProvider();
				String name1= lprov.getColumnText(e1, column);
				String name2= lprov.getColumnText(e2, column);
				if (isAccending) {
					return name1.compareTo(name2);
				}
				else {
					return name2.compareTo(name1);
				}
	    	}
		}
    	return 0;
    }
}
