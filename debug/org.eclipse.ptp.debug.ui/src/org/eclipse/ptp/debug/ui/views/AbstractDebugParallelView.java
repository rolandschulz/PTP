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
package org.eclipse.ptp.debug.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ptp.debug.core.IDebugParallelEventListener;
import org.eclipse.ptp.debug.core.IDebugParallelModelListener;
import org.eclipse.ui.part.ViewPart;
/**
 * @author clement chu
 *
 */
public abstract class AbstractDebugParallelView extends ViewPart implements ISelectionProvider, IDebugParallelModelListener {
	/**
	 * store debug event listener
	 */
	protected List listeners = new ArrayList(0);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugParallelModelListener#addDebugeEventListener(org.eclipse.ptp.debug.core.IDebugParallelEventListener)
	 */
	public void addDebugeEventListener(IDebugParallelEventListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugParallelModelListener#removeDebugeEventListener(org.eclipse.ptp.debug.core.IDebugParallelEventListener)
	 */
	public void removeDebugeEventListener(IDebugParallelEventListener listener) {
		listeners.remove(listener);
	}
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    	listeners.add(listener);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    	listeners.remove(listener);
    }	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		listeners.clear();
		super.dispose();
	}
	
	public abstract ISelection getSelection();	
}
