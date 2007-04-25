/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.ptp.ui.views.ParallelJobView;

/**
 * @author Randy M. Roberts (rsqrd)
 *
 */
public class ChangeQueueAction extends GotoDropDownAction {
	public static final String name = "Queue";
    
	/** Constructor
	 * @param view
	 */
	public ChangeQueueAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_IMG_RM_ON);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#createDropDownMenu(org.eclipse.jface.action.MenuManager)
	 */
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
		if (view instanceof ParallelJobView) {
			ParallelJobView pmView = (ParallelJobView)view;
			String curQueueID = pmView.getQueueID();
			final AbstractUIManager jobManager = ((AbstractUIManager)pmView.getUIManager());
			IResourceManager[] rms = jobManager.getResourceManagers();
			for (int ir=0; ir<rms.length; ++ir) {
				MenuManager cascade = new MenuManager(rms[ir].getName());
				dropDownMenuMgr.add(cascade);
				IPQueue[] queues = rms[ir].getQueues();
				for (int iq=0; iq<queues.length; ++iq) {
					addAction(cascade, queues[iq].getName(), queues[iq].getIDString(),
							curQueueID);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#addAction(org.eclipse.jface.action.MenuManager, java.lang.String, java.lang.String, java.lang.String)
	 */
	protected void addAction(MenuManager dropDownMenuMgr, String queueName,
			String id, String curID) {
		IAction action = new InternalQueueAction(queueName, id,
				getViewPart(), this);
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
			ParallelJobView pmView = ((ParallelJobView)view);
			final AbstractUIManager manager = ((AbstractUIManager)pmView.getUIManager());
			IPQueue[] queues = manager.getQueues();
			for (int i=0; i<queues.length; i++) {
		    		if (pmView.getQueueID().equals(queues[i].getIDString())) {
		    			if (i + 1 < queues.length)
		    				run(null, queues[i+1].getIDString());
		    			else
		    				run(null, queues[0].getIDString());
		    			
		    			break;
		    		}
			}
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#run(org.eclipse.ptp.ui.model.IElement[], java.lang.String)
	 */
	public void run(IElement[] elements, String id) {
		if (view instanceof ParallelJobView) {
			ParallelJobView pmView = ((ParallelJobView)view);
			pmView.selectQueue(id);			
			pmView.update();
			pmView.updateTitle();
			pmView.refresh(true);
		}
	}
	
	/** Inner internal queue action
	 * @author rsqrd
	 *
	 */
	private class InternalQueueAction extends GotoAction {
		public InternalQueueAction(String name, String id,
				AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action);
		    setImageDescriptor(ParallelImages.ID_IMG_RM_ON);
		}

		public void run(IElement[] elements) {
			super.run(elements);
		}		
	}
}
