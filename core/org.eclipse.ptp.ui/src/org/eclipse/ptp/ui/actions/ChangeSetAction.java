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
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 *
 */
public class ChangeSetAction extends GotoDropDownAction {
	public static final String name = "Change";
    
	public ChangeSetAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
	    setDisabledImageDescriptor(ParallelImages.ID_ICON_CREATESET_DISABLE);
	}
	
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
	    	String curID = view.getCurrentSetID();    	
	    	addAction(dropDownMenuMgr, ISetManager.SET_ROOT_ID, ISetManager.SET_ROOT_ID, curID);
	
		ISetManager setManager = view.getCurrentSetManager();
		if (setManager == null)
			return;

		IElementSet[] sets = setManager.getSortedSets();
	    	if (sets.length > 1)
	    		dropDownMenuMgr.add(new Separator());
	    	for (int i=0; i<sets.length; i++) {
	    		if (sets[i].getID().equals(ISetManager.SET_ROOT_ID))
	    			continue;
	    		
	    		addAction(dropDownMenuMgr, sets[i].getID(), sets[i].getID(), curID);
	    	}		
	}
	
	protected void addAction(MenuManager dropDownMenuMgr, String e_name, String id, String curID) {
		IAction action = new InternalSetAction(e_name, id, view, this);
		action.setChecked(curID.equals(id));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
	}
	
	public void run(IElement[] elements) {}
	
	public void run() {
		ISetManager setManager = view.getCurrentSetManager();
		if (setManager == null)
			return;
		
	    	IElementSet[] sets = setManager.getSortedSets();
	    	for (int i=0; i<sets.length; i++) {
	    		if (view.getCurrentSetID().equals(sets[i].getID())) {
	    			if (i + 1 < sets.length)
	    				run(null, sets[i+1]);
	    			else
	    				run(null, sets[0]);
	    			
	    			break;
	    		}
	    	}
	}
	
	public void run(IElement[] elements, IElementSet set) {
		view.selectSet(set);
		view.update();
		view.refresh();
	}
	
	public void run(IElement[] elements, String id) {
		ISetManager setManager = view.getCurrentSetManager();
		if (setManager == null)
			return;
		
		run(elements, setManager.getSet(id));
	}

	private class InternalSetAction extends GotoAction {
		public InternalSetAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action);
		    setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
		    setDisabledImageDescriptor(ParallelImages.ID_ICON_CREATESET_DISABLE);
		}	
	}
}
