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
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 *
 */
public abstract class ParallelAction extends Action {
    protected /*static*/ ViewPart viewPart = null;
        
    public ParallelAction(ViewPart view) {
        this(view, false);
    }
    
    public ParallelAction(ViewPart view, boolean isEnable) {
        viewPart = view;
        init(isEnable);
    }
    
	public ParallelAction(ViewPart view, int style) {
		super(null, style);
		viewPart = view;
		init(false);
	}

	public /*static*/ ViewPart getViewPart() {
        return viewPart;
    }
    
    public Shell getShell() {
        return viewPart.getViewSite().getShell();
    }
    
    protected IModelManager getLaunchManager() {
        return PTPCorePlugin.getDefault().getModelManager();
    }
    
    protected abstract void init(boolean isEnable);    
    public abstract void run();    
}
