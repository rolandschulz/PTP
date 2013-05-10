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
package org.eclipse.ptp.etfw.tau.selinst.popup.actions;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.etfw.tau.selinst.Selector;
import org.eclipse.ptp.etfw.tau.selinst.messages.Messages;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to clear instrumention from a CDT/Photran element
 * 
 * @author wspear
 * 
 */
public class Clear implements IObjectActionDelegate {

	IStructuredSelection selection;

	/**
	 * Constructor for Action1.
	 */
	public Clear() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		// If nothing is selected we can not proceed
		if (selection == null)
		{
			System.out.println(Messages.Clear_NoSelection);
			return;
		}
		/**
		 * The current element being examined for removal
		 */
		ICElement cbit = null;
		/**
		 * The iterator over all selected elements
		 */
		@SuppressWarnings("unchecked")
		final Iterator<ICElement> selit = selection.iterator();
		/**
		 * The list of selected files
		 */
		final HashSet<String> selfiles = new HashSet<String>();
		/**
		 * The list of selected routines
		 */
		final HashSet<String> selrouts = new HashSet<String>();
		/**
		 * List of file-level 'simple' selection commands
		 */
		final HashSet<String> clearFileSelSec = new HashSet<String>();
		/**
		 * List of routine-level 'simple' selection commands
		 */
		final HashSet<String> clearRoutSelSec = new HashSet<String>();
		/**
		 * List of other, old-style instrumentation commands to clear
		 */
		final HashSet<String> clearOtherSelSec = new HashSet<String>();
		/**
		 * The component of the selection line after the variable selection type
		 */
		String selSect = null;
		int type;
		int tot = 0;
		while (selit.hasNext())
		{
			cbit = selit.next();
			type = cbit.getElementType();
			// For Selected Routines:
			if (type == ICElement.C_FUNCTION)
			{
				// Get the function declaration
				final String fullsig = Selector.getFullSigniture((IFunctionDeclaration) cbit);
				clearRoutSelSec.add(" routine=\"" + fullsig + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				// Selected routines
				selrouts.add(fullsig);
				// selSect=" file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+fullsig+"\"";
				//
				// for(int i=0;i<SelectiveInstrument.instTypes.length;i++)
				// {
				// clearRoutSelSec.add(SelectiveInstrument.instTypes[i]+selSect);
				// }
				tot++;
			}
			else if (type == ICElement.C_UNIT)
			{
				selfiles.add(cbit.getElementName());

				selSect = " file=\"" + cbit.getElementName() + "\" routine=\"#\""; //$NON-NLS-1$ //$NON-NLS-2$

				clearFileSelSec.add("file=\"" + cbit.getElementName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

				// for(int i=0;i<SelectiveInstrument.instTypes.length;i++)
				// {
				// clearFileSelSec.add(SelectiveInstrument.instTypes[i]+selSect);
				// }

				clearOtherSelSec.add("file=\"" + cbit.getElementName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

				clearOtherSelSec.add("entry file=\"" + cbit.getUnderlyingResource().getName() + "\" routine=\"#\" code = \""); //$NON-NLS-1$ //$NON-NLS-2$
				tot++;
			}
			else if (type == -1)
			{
				final String fortclass = cbit.getClass().getName();
				if (fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine") || fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					selrouts.add("#" + cbit.getElementName().toUpperCase()); //$NON-NLS-1$
					selSect = " file=\"" + cbit.getUnderlyingResource().getName() + "\" routine=\"" + "#" + cbit.getElementName().toUpperCase() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

					for (final String instType : SelectiveInstrument.instTypes) {
						clearOtherSelSec.add(instType + selSect);
					}
				}
				tot++;
			}
		}
		if (tot > 0)
		{
			final Selector excludeinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
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
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		} else
		{ // if the selection is invalid, stop
			this.selection = null;
			System.out.println(Messages.Clear_InvalidSelection);
		}
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
