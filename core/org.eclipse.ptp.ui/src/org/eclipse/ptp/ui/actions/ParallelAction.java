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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.swt.widgets.Shell;

/**
 * @author clement chu
 *
 */
public abstract class ParallelAction extends Action {
	protected AbstractParallelElementView view = null;
	
	/** Constructor
	 * @param text name of action
	 * @param view
	 */
	public ParallelAction(String text, AbstractParallelElementView view) {
		this(text, IAction.AS_PUSH_BUTTON, view);
	}
	
	/** Constructor
	 * @param text name of action
	 * @param style style of action
	 * @param view
	 */
	public ParallelAction(String text, int style, AbstractParallelElementView view) {
		super(text, style);
		this.view = view;
	    setToolTipText(text);
	    setEnabled(false);
	    setId(text);
	}
	
	/** Get view part
	 * @return
	 */
	public AbstractParallelElementView getViewPart() {
        return view;
    }
    
    /** Get Shell
     * @return
     */
    public Shell getShell() {
        return view.getViewSite().getShell();
    }
    
    /** run action
     * @param elements action acts with these elements
     */
    public abstract void run(IElement[] elements);
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
    	ISelection selection = getViewPart().getSelection();
    	if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
        	Object[] objs = ((IStructuredSelection)selection).toArray();
        	IElement[] elements = new IElement[objs.length];
        	System.arraycopy(objs, 0, elements, 0, objs.length);
        	run(elements);
    	}
    	else
    		run(new IElement[0]);
    }
    
	/** Validation of given elements
	 * @param elements elements to be doing validation
	 * @return true if valid
	 */
	protected boolean validation(IElement[] elements) {
		if (elements == null || elements.length == 0) {
			UIUtils.showErrorDialog("No selected elements", "Please select some elements first", null);
			return false;
		}
		return true;
	}
}
