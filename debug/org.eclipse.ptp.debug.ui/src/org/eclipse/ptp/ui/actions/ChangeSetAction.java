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
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 *
 */
public class ChangeSetAction extends SetAction {
	public static final String name = "Change";
    
	public ChangeSetAction(AbstractParallelElementView view) {
		super(name, view);
	}
	
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
    	String curSetID = view.getCurrentSetID();    	
    	String rootSetID = view.getUIManger().getSetManager().getSetRoot().getID();
    	addAction(dropDownMenuMgr, rootSetID, curSetID);

    	IElementSet[] sets = view.getUIManger().getSetManager().getSortedSets();
    	if (sets.length > 1)
    		dropDownMenuMgr.add(new Separator());
    	for (int i=0; i<sets.length; i++) {
    		if (sets[i].getID().equals(rootSetID))
    			continue;
    		
    		addAction(dropDownMenuMgr, sets[i].getID(), curSetID);
    	}		
	}
	
	private void addAction(MenuManager dropDownMenuMgr, String setID, String curSetID) {
		IAction action = new InternalSetAction(setID, view, IAction.AS_CHECK_BOX, this);
		action.setChecked(curSetID.equals(setID));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
	}
	
	public void run(IElement[] elements) {}
	
	public void run() {
    	IElementSet[] sets = view.getUIManger().getSetManager().getSortedSets();
    	for (int i=0; i<sets.length; i++) {
    		if (view.getCurrentSetID().equals(sets[i].getID())) {
    			if (i + 1 < sets.length)
    				run(null, sets[i+1].getID());
    			else
    				run(null, sets[0].getID());
    			
    			break;
    		}
    	}
	}
	
	public void run(IElement[] elements, String setID) {
		view.selectSet(setID);
		view.getCurrentSet().setAllSelect(false);
		view.update();
		view.redraw();
	}
}
