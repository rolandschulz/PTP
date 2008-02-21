/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.perf.tau.selinst.popup.actions;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.perf.tau.selinst.Selector;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to clear instrumention from a CDT/Photran element
 * @author wspear
 *
 */
public class Clear implements IObjectActionDelegate {
	
	/**
	 * Constructor for Action1.
	 */
	public Clear() {
		super();
	}
	IStructuredSelection selection;
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//If nothing is selected we can not proceed
		if(selection==null)
		{
			System.out.println("No Selection");
			return;
		}
		/**
		 * The current element being examined for removal
		 */
		ICElement cbit=null;
		/**
		 * The iterator over all selected elements
		 */
		Iterator<ICElement> selit = selection.iterator();
		/**
		 * The list of selected files
		 */
		HashSet<String> selfiles=new HashSet<String>();
		/**
		 * The list of selected routines
		 */
		HashSet<String> selrouts=new HashSet<String>();
		/**
		 * List of file-level 'simple' selection commands
		 */
		HashSet<String> clearFileSelSec=new HashSet<String>();
		/**
		 * List of routine-level 'simple' selection commands
		 */
		HashSet<String> clearRoutSelSec=new HashSet<String>();
		/**
		 * List of other, old-style instrumentation commands to clear
		 */
		HashSet<String> clearOtherSelSec=new HashSet<String>();
		/**
		 * The component of the selection line after the variable selection type
		 */
		String selSect=null;
		int type;
		int tot=0;
		while(selit.hasNext())
		{
			cbit=selit.next();
			type = cbit.getElementType();
			//For Selected Routines:
			if(type==ICElement.C_FUNCTION)
			{
				//Get the function declaration
				String fullsig = Selector.getFullSigniture((IFunctionDeclaration)cbit);
				clearRoutSelSec.add(" routine=\""+fullsig+"\"");
				//Selected routines
				selrouts.add(fullsig);
//				selSect=" file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+fullsig+"\"";
//
//				for(int i=0;i<SelectiveInstrument.instTypes.length;i++)
//				{
//					clearRoutSelSec.add(SelectiveInstrument.instTypes[i]+selSect);
//				}
				tot++;
			}
			else
			if(type == ICElement.C_UNIT)
			{
				selfiles.add(cbit.getElementName());
				
				selSect=" file=\""+cbit.getElementName()+"\" routine=\"#\"";
				
				clearFileSelSec.add("file=\""+cbit.getElementName()+"\"");
				
//				for(int i=0;i<SelectiveInstrument.instTypes.length;i++)
//				{
//					clearFileSelSec.add(SelectiveInstrument.instTypes[i]+selSect);
//				}
				
				clearOtherSelSec.add("file=\""+cbit.getElementName()+"\"");
				
				clearOtherSelSec.add("entry file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\"#\" code = \"");
				tot++;
			}
			else
				if(type==-1)
				{
					String fortclass =cbit.getClass().getName();
					if(fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine")||fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram"))
					{
						selrouts.add("#"+cbit.getElementName().toUpperCase());
						selSect=" file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+"#"+cbit.getElementName().toUpperCase()+"\"";
						
						for(int i=0;i<SelectiveInstrument.instTypes.length;i++)
						{
							clearOtherSelSec.add(SelectiveInstrument.instTypes[i]+selSect);
						}
					}
					tot++;
				}
		}
		if(tot>0)
		{
			Selector excludeinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
				excludeinst.clearFile(selfiles);
				excludeinst.clearRout(selrouts);
				
				excludeinst.clearGenInst(clearOtherSelSec);
				excludeinst.clearInstrumentSection(clearFileSelSec);
				excludeinst.clearInstrumentSection(clearRoutSelSec);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) selection;
		else
		{	//if the selection is invalid, stop
			this.selection = null;
			System.out.println("Invalid Selection");
		}		
	}

}
