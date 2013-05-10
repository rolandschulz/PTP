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
 * Action to exclude a CDT/Photran element from instrumentation
 * 
 * @author wspear
 * 
 */
public class SelectiveInstrument implements IObjectActionDelegate {
	public static final int INCLUDE = 0;
	public static final int EXCLUDE = 1;

	// These two lists must always be 1-1
	protected static final String[] instTypes = {
			"loops", "io", "memory", "dynamic timer", "static timer", "dynamic phase", "static phase" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	protected static final String[] instIDs = {
			"org.eclipse.ptp.etfw.tau.selinst.instloops", "org.eclipse.ptp.etfw.tau.selinst.instio", "org.eclipse.ptp.etfw.tau.selinst.instmemory", "org.eclipse.ptp.etfw.tau.selinst.dyntime", "org.eclipse.ptp.etfw.tau.selinst.stattime", "org.eclipse.ptp.etfw.tau.selinst.dynphase", "org.eclipse.ptp.etfw.tau.selinst.statphase" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	IStructuredSelection selection;

	/**
	 * Constructor for Action1.
	 */
	public SelectiveInstrument() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (selection == null)
		{
			System.out.println(Messages.SelectiveInstrument_NoSelection);
			return;
		}
		ICElement cbit = null;
		@SuppressWarnings("unchecked")
		final Iterator<Object> selit = selection.iterator();
		final HashSet<String> selfiles = new HashSet<String>();
		final HashSet<String> selrouts = new HashSet<String>();
		int type;
		String selType = ""; //$NON-NLS-1$
		int incex = -1;
		int idDex = -1;
		final String selID = action.getId();

		if (selID.equals("org.eclipse.ptp.etfw.tau.selinst.excludeselect")) {
			incex = EXCLUDE;
		} else if (selID.equals("org.eclipse.ptp.etfw.tau.selinst.includeselect")) {
			incex = INCLUDE;
		} else {
			for (idDex = 0; idDex < instIDs.length; idDex++)
			{
				if (selID.equals(instIDs[idDex]))
				{
					selType = instTypes[idDex];
					break;
				}
			}
		}

		// if(selID.equals("org.eclipse.ptp.etfw.tau.selinst.instloops"))
		// selType="loops";
		// else if(selID.equals("org.eclipse.ptp.etfw.tau.selinst.instio"))
		// selType="io";
		// else if(selID.equals("org.eclipse.ptp.etfw.tau.selinst.instmemory"))
		// selType="memory";
		// else

		if (!selType.equals("") || incex >= 0) {
			while (selit.hasNext())
			{
				cbit = (ICElement) selit.next();
				type = cbit.getElementType();
				if (type == ICElement.C_FUNCTION)
				{
					final String fullsig = Selector.getFullSigniture((IFunctionDeclaration) cbit);
					if (incex >= 0)
					{
						selrouts.add(fullsig);
					}
					else if (!selType.equals("")) //$NON-NLS-1$
					{
						if (idDex <= 2) {
							selrouts.add(selType
									+ " file=\"" + cbit.getUnderlyingResource().getName() + "\" routine=\"" + fullsig + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
						else {
							selrouts.add(selType + " routine=\"" + fullsig + "\""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				else if (type == ICElement.C_UNIT)
				{
					if (incex >= 0)
					{
						selfiles.add(cbit.getElementName());
					}
					else if (!selType.equals("")) //$NON-NLS-1$
					{
						if (idDex <= 2) {
							selfiles.add(selType + " file=\"" + cbit.getElementName() + "\" routine=\"#\""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							selfiles.add(selType + " routine=\"#\""); //$NON-NLS-1$
						}
					}
				}
				else if (type == -1)
				{
					final String fortclass = cbit.getClass().getName();
					if (fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine") || fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						if (incex >= 0)
						{
							selrouts.add("#" + cbit.getElementName().toUpperCase()); //$NON-NLS-1$
						}
						else if (!selType.equals("")) //$NON-NLS-1$
						{
							if (idDex <= 2) {
								selrouts.add(selType
										+ " file=\"" + cbit.getUnderlyingResource().getName() + "\" routine=\"" + "#" + cbit.getElementName().toUpperCase() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							}
							else {
								selrouts.add(selType + " routine=\"" + "#" + cbit.getElementName().toUpperCase() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}
						}
					}
				}
			}
		}
		if (selfiles.size() > 0 || selrouts.size() > 0)
		{
			final Selector selinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
			if (selfiles.size() > 0)
			{
				if (incex == INCLUDE) {
					selinst.includeFile(selfiles);
				} else if (incex == EXCLUDE) {
					selinst.excludeFile(selfiles);
				} else if (!selType.equals("")) {
					selinst.addInst(selfiles);
				}
			}
			if (selrouts.size() > 0)
			{
				if (incex == INCLUDE) {
					selinst.includeRout(selrouts);
				} else if (incex == EXCLUDE) {
					selinst.excludeRout(selrouts);
				} else if (!selType.equals("")) {
					selinst.addInst(selrouts);
				}
			}
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
			System.out.println(Messages.SelectiveInstrument_InvalidSelection);
		}
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
