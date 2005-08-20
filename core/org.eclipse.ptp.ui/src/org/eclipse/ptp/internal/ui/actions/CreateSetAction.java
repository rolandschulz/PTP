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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
/**
 * @author clement chu
 *
 */
public class CreateSetAction extends GotoDropDownAction {
	public static final String name = "Create Set";
    
	public CreateSetAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
	}
	
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
    	String curID = view.getCurrentSetID();
		IElementHandler setManager = view.getCurrentElementHandler();
		if (setManager == null)
			return;

		IElementSet[] sets = setManager.getSortedSets();
	    	for (int i=1; i<sets.length; i++) {
	    		addAction(dropDownMenuMgr, sets[i].getID(), sets[i].getID(), curID);
	    	}		
	}
	
	protected void addAction(MenuManager dropDownMenuMgr, String e_name, String id, String curID) {
		IAction action = new InternalSetAction("Add to set: " + e_name, id, view, this);
		action.setEnabled(!curID.equals(id));
		dropDownMenuMgr.add(action);
	}	
	
	public void run(IElement[] elements) {
		run(elements, null);
	}
	
	public void run(IElement[] elements, String setID) {
		if (validation(elements)) {
			final IElementHandler setManager = view.getCurrentElementHandler();
			if (setManager == null)
				return;
			
			if (setID == null) {
				IInputValidator inputValidator = new IInputValidator() {
					public String isValid(String newText) {
						if (newText == null || newText.length() == 0)
							return "This field cannot be empty.";
						
						if (setManager.contains(newText))
							return "Entered set name (" + newText + ") is already used.";						

						return null;
					}
				};
				InputDialog inputDialog = new InputDialog(getShell(), "Create a new set name", "Please enter the new set name.", "", inputValidator);
				if (inputDialog.open() == InputDialog.CANCEL)
					return;

				String name = inputDialog.getValue();
				setID = view.getUIManger().createSet(elements, name, name, setManager);				
			} else
				view.getUIManger().addToSet(elements, setID, setManager);
			
			view.selectSet(setManager.getSet(setID));
			//Need to deselect all elements manually
			view.getCurrentSet().setAllSelect(false);
			view.update();
			view.refresh();
		}
	}
	
	private class InternalSetAction extends GotoAction {
		public InternalSetAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action);
		    setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
		}	
	}	
}
