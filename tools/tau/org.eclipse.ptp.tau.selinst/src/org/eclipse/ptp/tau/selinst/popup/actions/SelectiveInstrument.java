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
package org.eclipse.ptp.tau.selinst.popup.actions;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.tau.selinst.Selector;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to exclude a CDT/Photran element from instrumentation
 * @author wspear
 *
 */
public class SelectiveInstrument implements IObjectActionDelegate {
	public static final int INCLUDE=0;
	public static final int EXCLUDE=1;
	
	//These two lists must always be 1-1
	protected static final String[] instTypes={"loops",                                "io",                                "memory",                                "dynamic timer",                      "static timer",                        "dynamic phase",                       "static phase"};
	protected static final String[] instIDs=  {"org.eclipse.ptp.tau.selinst.instloops","org.eclipse.ptp.tau.selinst.instio","org.eclipse.ptp.tau.selinst.instmemory","org.eclipse.ptp.tau.selinst.dyntime","org.eclipse.ptp.tau.selinst.stattime","org.eclipse.ptp.tau.selinst.dynphase","org.eclipse.ptp.tau.selinst.statphase"};

	/**
	 * Constructor for Action1.
	 */
	public SelectiveInstrument() {
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
		if(selection==null)
		{
			System.out.println("No Selection");
			return;
		}
		ICElement cbit=null;
		Iterator selit = selection.iterator();
		HashSet selfiles=new HashSet();
		HashSet selrouts=new HashSet();
		int type;
		String selType="";
		int incex=-1;
		int idDex=-1;
		String selID=action.getId();
		
		if(selID.equals("org.eclipse.ptp.tau.selinst.excludeselect"))
			incex=EXCLUDE;
		else if(selID.equals("org.eclipse.ptp.tau.selinst.includeselect"))
			incex=INCLUDE;
		else
		for(idDex=0;idDex<instIDs.length;idDex++)
		{
			if(selID.equals(instIDs[idDex]))
			{
				selType=instTypes[idDex];
				break;
			}
		}
		
//		if(selID.equals("org.eclipse.ptp.tau.selinst.instloops"))
//			selType="loops";
//		else if(selID.equals("org.eclipse.ptp.tau.selinst.instio"))
//			selType="io";
//		else if(selID.equals("org.eclipse.ptp.tau.selinst.instmemory"))
//			selType="memory";
//		else 


		if(!selType.equals("")||incex>=0)
			while(selit.hasNext())
			{
				cbit=(ICElement)selit.next();
				type = cbit.getElementType();
				if(type==ICElement.C_FUNCTION)
				{
					String fullsig=Selector.getFullSigniture((IFunctionDeclaration)cbit);
					if(incex>=0)
					{
						selrouts.add(fullsig);
					}
					else if(!selType.equals(""))
					{
						if(idDex<=2)
							selrouts.add(selType+" file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+fullsig+"\"");
						else
							selrouts.add(selType+" routine=\""+fullsig+"\"");
					}
				}
				else
					if(type == ICElement.C_UNIT)
					{
						if(incex>=0)
						{
							selfiles.add(cbit.getElementName());
						}
						else if(!selType.equals(""))
						{
							if(idDex<=2)
								selfiles.add(selType+" file=\""+cbit.getElementName()+"\" routine=\"#\"");
							else
								selfiles.add(selType+" routine=\"#\"");
						}
					}
					else
						if(type==-1)
						{
							String fortclass =cbit.getClass().getName();
							if(fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine")||fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram"))
							{
								if(incex>=0)
								{
									selrouts.add("#"+cbit.getElementName().toUpperCase());
								}
								else if(!selType.equals(""))
								{
									if(idDex<=2)
										selrouts.add(selType+" file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+"#"+cbit.getElementName().toUpperCase()+"\"");
									else
										selrouts.add(selType+" routine=\""+"#"+cbit.getElementName().toUpperCase()+"\"");
								}
							}
						}
			}
		if(selfiles.size()>0||selrouts.size()>0)
		{
			Selector selinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
			if(selfiles.size()>0)
			{	if(incex==INCLUDE)
					selinst.includeFile(selfiles);
				else if(incex==EXCLUDE)
					selinst.excludeFile(selfiles);
				else if(!selType.equals(""))
					selinst.addInst(selfiles);
			}
			if(selrouts.size()>0)
			{
				if(incex==INCLUDE)
					selinst.includeRout(selrouts);
				else if(incex==EXCLUDE)
					selinst.excludeRout(selrouts);
				else if(!selType.equals(""))
					selinst.addInst(selrouts);
			}
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
