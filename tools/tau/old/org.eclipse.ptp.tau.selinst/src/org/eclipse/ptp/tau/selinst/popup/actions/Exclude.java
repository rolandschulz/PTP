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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.tau.selinst.Selector;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class Exclude implements IObjectActionDelegate {

	/**
	 * Constructor for Action1.
	 */
	public Exclude() {
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
		//System.out.println("Selection");
		//selection.
		//ITranslationUnit source;
		ICElement cbit=null;
		Iterator selit = selection.iterator();
		HashSet selfiles=new HashSet();
		HashSet selrouts=new HashSet();
		int type;
		int tot=0;
		while(selit.hasNext())
		{
			//source=(ITranslationUnit)selit.next();
			cbit=(ICElement)selit.next();
			//System.out.println(cbit.getElementType());
			//int x =cbit.C_FUNCTION;
			//System.out.println(cbit.getElementName());
			type = cbit.getElementType();
			if(type==ICElement.C_FUNCTION)
			{
				IFunctionDeclaration fun = (IFunctionDeclaration)cbit;
				try {
					String returntype = Selector.fixStars(fun.getReturnType());
					String signature = Selector.fixStars(fun.getSignature());
					selrouts.add(returntype+" "+signature+"#");
				} catch (CModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tot++;
			}
			else
			if(type == ICElement.C_UNIT)
			{
				//ICProject cproject = cbit.getCProject();
				//String location = cproject.getResource().getLocation().toOSString();
				//String fileroot = cbit.getParent().getResource().getLocation().toOSString();
				//String fileslash="*"+File.separator;

				//boolean managed = ManagedBuildManager.canGetBuildInfo(cproject.getResource());
				//boolean useclean=true;//If the file is at the top level, we need both a slashed and a clean filename
				//if((location.length()!=fileroot.length()))//If it is deeper, we do not need the clean filename
				//	useclean=false;
				//selfiles.add(fileslash+cbit.getElementName());
				//if(useclean)
				selfiles.add(cbit.getElementName());
				tot++;
			}
			else
				if(type==-1)
				{
					String fortclass =cbit.getClass().getName();
					//System.out.println(fortclass);
					if(fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine")||fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram"))
					{
						selrouts.add("#"+cbit.getElementName().toUpperCase());
					}
					tot++;
				}
			//cbit.
		}
		if(tot>0)
		{
			//String dah = cbit.getCProject().getResource().getLocation().toOSString();
			//System.out.println("DAH "+dah);//cbit.getCProject().getUnderlyingResource().getFullPath().makeAbsolute()); 
			Selector excludeinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
			if(selfiles.size()>0)
				excludeinst.excludeFile(selfiles);
			if(selrouts.size()>0)
				excludeinst.excludeRout(selrouts);
		}
		/*
		ICElement xth=(ICElement)selection.getFirstElement();
		System.out.println(xth.toString()+" "+xth.getElementType()+" "+xth.getElementName());
		if(xth.getElementType()==xth.C_UNIT)
		{
			source = (ITranslationUnit)xth;
		}
		
		
		if(xth.getElementType()==xth.C_FUNCTION)
		{
			IFunction fun=(IFunction)xth;
			//fun.
			try {
				System.out.println("fun: "+fun.toString()+" "+fun.getSignature()+" "+fun.getReturnType());
				
				String[] stuff = fun.getParameterTypes();
				for(int i=0;i<stuff.length;i++)
					System.out.println(stuff[i]);
				
			} catch (CModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		/*Shell shell = new Shell();
		MessageDialog.openInformation(
			shell,
			"Selinst Plug-in",
			"Select was executed.");*/
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
