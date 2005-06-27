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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.internal.core.ParallelModelAdapter;

/**
 *
 */
public abstract class PSearchContentProvider implements IStructuredContentProvider {
	protected PSearchResult result;
	protected final Object[] EMPTY_ARR= new Object[0];

    private ParallelModelAdapter launchAdapter = new ParallelModelAdapter() {
        public void run() {
            result.removeAll();
            refresh();
        }
        public void abort() {
            refresh();
        }
        public void exit() {
            result.removeAll();
            refresh();
        }
    	public void start() {
    	    result.removeAll();
            refresh();
    	}
    	public void stopped() {
            refresh();
    	}
        
        public void execStatusChangeEvent(Object object) {
            refresh();
        }
        public void sysStatusChangeEvent(Object object) {
            refresh();
        }
        public void processOutputEvent(Object object) {
            refresh();
        }
        public void errorEvent(Object object) {
            refresh();
        }
        public void updatedStatusEvent() {
            refresh();
        }
    };
    
    public PSearchContentProvider() {
        PTPCorePlugin.getDefault().getModelManager().addParallelLaunchListener(launchAdapter);
    }

	public void dispose() {
        PTPCorePlugin.getDefault().getModelManager().removeParallelLaunchListener(launchAdapter);
	}
	
	public Object[] getElements(Object inputElement) {
		return null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		initialize((PSearchResult) newInput);
	}

	protected void initialize(PSearchResult result) {
		this.result= result;
	}

	public abstract void elementsChanged(Object[] updatedElements);
	public abstract void clear();
	public abstract void refresh();
}
