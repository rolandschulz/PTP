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
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.ptp.ui.views.ParallelJobView;

/**
 * @author Clement chu
 *
 */
public class ChangeJobViewAction extends GotoDropDownAction {
	public static final String name = "Job View";
    
	/** Constructor
	 * @param view
	 */
	public ChangeJobViewAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_ICON_JOB_NORMAL);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#createDropDownMenu(org.eclipse.jface.action.MenuManager)
	 */
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
		if (view instanceof ParallelJobView) {
			ParallelJobView jView = (ParallelJobView)view;
	    	String current_view = jView.getCurrentView();
    		addAction(dropDownMenuMgr, "Both", ParallelJobView.BOTH_VIEW, current_view);
    		addAction(dropDownMenuMgr, "Job", ParallelJobView.JOB_VIEW, current_view);
    		addAction(dropDownMenuMgr, "Process", ParallelJobView.PRO_VIEW, current_view);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#addAction(org.eclipse.jface.action.MenuManager, java.lang.String, java.lang.String, java.lang.String)
	 */
	protected void addAction(MenuManager dropDownMenuMgr, String view_name, String id, String curID) {
		IAction action = new InternalJobAction(view_name, id, getViewPart(), this);
		action.setChecked(curID.equals(id));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.ParallelAction#run(org.eclipse.ptp.ui.model.IElement[])
	 */
	public void run(IElement[] elements) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (view instanceof ParallelJobView) {
			ParallelJobView jView = ((ParallelJobView)view);
			String current_view = jView.getCurrentView();
			if (current_view.equals(ParallelJobView.BOTH_VIEW))
				run(null, ParallelJobView.JOB_VIEW);
			else if (current_view.equals(ParallelJobView.JOB_VIEW))
				run(null, ParallelJobView.PRO_VIEW);
			else
				run(null, ParallelJobView.BOTH_VIEW);
    	}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#run(org.eclipse.ptp.ui.model.IElement[], java.lang.String)
	 */
	public void run(IElement[] elements, String id) {
		if (view instanceof ParallelJobView) {
			ParallelJobView jView = ((ParallelJobView)view);
			jView.changeView(id);
		}
	}

	/** Inner internal job action
	 * @author clement
	 *
	 */
	private class InternalJobAction extends GotoAction {
		public InternalJobAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action);
		    setImageDescriptor(ParallelImages.ID_ICON_JOB_NORMAL);
		}	
	}
}
