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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.ptp.ui.views.ParallelMachineView;

/**
 * @author Clement chu
 *
 */
public class ChangeMachineAction extends MachineAction {
	public static final String name = "Machine";
    
	public ChangeMachineAction(AbstractParallelElementView view) {
		super(name, view);
	}
	
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
    	String curMachineName = ((ParallelMachineView)view).getCurrentMachineName();	
    	IPMachine[] macs = view.getUIManger().getModelManager().getUniverse().getSortedMachines();
    	for (int i=0; i<macs.length; i++) {
    		//FIXME id or name
    		addAction(dropDownMenuMgr, macs[i].getElementName(), curMachineName);
    	}		
	}
	
	private void addAction(MenuManager dropDownMenuMgr, String machine_name, String curMachineName) {
		IAction action = new InternalMachineAction(machine_name, view, IAction.AS_CHECK_BOX, this);
		action.setChecked(curMachineName.equals(machine_name));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
	}
	
	public void run(IElement[] elements) {}
	
	public void run() {
		IPMachine[] macs = view.getUIManger().getModelManager().getUniverse().getSortedMachines();
	    	for (int i=0; i<macs.length; i++) {
//	    		FIXME id or name
	    		if (((ParallelMachineView)view).getCurrentMachineName().equals(macs[i].getElementName())) {
	    			if (i + 1 < macs.length)
	    				run(null, macs[i+1].getElementName());
	    			else
	    				run(null, macs[0].getElementName());
	    			
	    			break;
	    		}
	    	}
	}
	
	public void run(IElement[] elements, String machine_name) {
		if (view instanceof ParallelMachineView) {
			ParallelMachineView pmView = ((ParallelMachineView)view);
			pmView.selectMachine(machine_name);			
			view.update();
			view.refresh();
		}
	}
}
