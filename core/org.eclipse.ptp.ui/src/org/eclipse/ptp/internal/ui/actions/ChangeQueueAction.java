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
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.JobManager;
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
			String curQueueId = pmView.getQueueID();
			final AbstractUIManager jobManager = ((AbstractUIManager)pmView.getUIManager());
			for (IResourceManager rm : jobManager.getResourceManagers()) {
				MenuManager cascade = new MenuManager(rm.getName());
				dropDownMenuMgr.add(cascade);
				for (IPQueue queue : rm.getQueues()) {
					addAction(cascade, queue.getName(), queue.getID(),
							curQueueId, queue);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#addAction(org.eclipse.jface.action.MenuManager, java.lang.String, java.lang.String, java.lang.String)
	 */
	protected void addAction(MenuManager dropDownMenuMgr, String queueName,
			String id, String curID, Object data) {
		IAction action = new InternalQueueAction(queueName, id,
				getViewPart(), this, data);
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
			final JobManager manager = ((JobManager)pmView.getUIManager());
			IPQueue queue = manager.getQueue();
			if (queue != null) {
				IPQueue[] queues = queue.getResourceManager().getQueues();
				for (int i=0; i<queues.length; i++) {
		    		if (queue == queues[i]) {
		    			if (i + 1 < queues.length)
		    				run(null, queues[i+1].getID(), queues[i+1]);
		    			else
		    				run(null, queues[0].getID(), queues[0]);
		    			
		    			break;
		    		}
				}
			}
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#run(org.eclipse.ptp.ui.model.IElement[], java.lang.String)
	 */
	public void run(IElement[] elements, String id, Object data) {
		if (view instanceof ParallelJobView) {
			ParallelJobView pmView = ((ParallelJobView)view);
			pmView.selectQueue((IPQueue) data);			
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
				AbstractParallelElementView view, GotoDropDownAction action, Object data) {
			super(name, id, view, action, data);
		    setImageDescriptor(ParallelImages.ID_IMG_RM_ON);
		}
	}
}
