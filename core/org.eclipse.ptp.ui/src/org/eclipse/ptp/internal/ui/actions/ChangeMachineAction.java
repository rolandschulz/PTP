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
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.ptp.ui.views.ParallelMachineView;

/**
 * @author Clement chu
 *
 */
public class ChangeMachineAction extends GotoDropDownAction {
	public static final String name = "Machine";
    
	public ChangeMachineAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_ICON_MACHINE_NORMAL);
	}
	
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
		if (view instanceof ParallelMachineView) {
			ParallelMachineView pmView = (ParallelMachineView)view;
		    	String curMachineID = pmView.getCurrentMachineID();	
		    	IPMachine[] macs = pmView.getMachineManager().getMachines();
		    	for (int i=0; i<macs.length; i++) {
		    		addAction(dropDownMenuMgr, macs[i].getElementName(), macs[i].getIDString(), curMachineID);
		    	}		
		}
	}
	
	protected void addAction(MenuManager dropDownMenuMgr, String machine_name, String id, String curID) {
		IAction action = new InternalMachineAction(machine_name, id, getViewPart(), this);
		action.setChecked(curID.equals(id));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
	}
	
	public void run(IElement[] elements) {}
	
	public void run() {
		if (view instanceof ParallelMachineView) {
			ParallelMachineView pmView = ((ParallelMachineView)view);
			IPMachine[] macs = pmView.getMachineManager().getMachines();
			for (int i=0; i<macs.length; i++) {
		    		//FIXME id or name
		    		if (pmView.getCurrentMachineID().equals(macs[i].getIDString())) {
		    			if (i + 1 < macs.length)
		    				run(null, macs[i+1].getIDString());
		    			else
		    				run(null, macs[0].getIDString());
		    			
		    			break;
		    		}
			}
	    }
	}
	
	public void run(IElement[] elements, String id) {
		if (view instanceof ParallelMachineView) {
			ParallelMachineView pmView = ((ParallelMachineView)view);
			pmView.selectMachine(id);			
			pmView.update();
			pmView.refresh();
		}
	}
	
	private class InternalMachineAction extends GotoAction {
		public InternalMachineAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action);
		    setImageDescriptor(ParallelImages.ID_ICON_MACHINE_NORMAL);
		}		
	}
}
