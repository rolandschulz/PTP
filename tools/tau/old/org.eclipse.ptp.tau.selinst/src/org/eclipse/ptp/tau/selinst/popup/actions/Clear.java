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
		if(selection==null)
		{
			System.out.println("No Selection");
			return;
		}
		ICElement cbit=null;
		Iterator selit = selection.iterator();
		HashSet selfiles=new HashSet();
		HashSet selrouts=new HashSet();
		HashSet clearFileLoops=new HashSet();
		HashSet clearRoutLoops=new HashSet();
		HashSet clearInst=new HashSet();
		int type;
		int tot=0;
		//String fileslash="*"+File.separator;
		while(selit.hasNext())
		{
			cbit=(ICElement)selit.next();
			type = cbit.getElementType();
			if(type==ICElement.C_FUNCTION)
			{
				IFunctionDeclaration fun = (IFunctionDeclaration)cbit;
				try {
					String returntype = Selector.fixStars(fun.getReturnType());
					String signature = Selector.fixStars(fun.getSignature());
					//System.out.println("Before "+returntype);
					//returntype = returntype);//returntype.replaceAll("//*", " *");
					//System.out.println("After: "+returntype);
					
					
					selrouts.add(returntype+" "+signature+"#");
					clearRoutLoops.add("loops file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+returntype+" "+signature+"#"+"\"");
					//clearRoutLoops.add("loops file=\""+fileslash+cbit.getUnderlyingResource().getName()+"\" routine=\""+returntype+" "+signature+"#"+"\"");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tot++;
			}
			else
			if(type == ICElement.C_UNIT)
			{
				selfiles.add(cbit.getElementName());
				//selfiles.add(fileslash+cbit.getElementName());
				clearFileLoops.add("loops file=\""+cbit.getElementName()+"\" routine=\"#\"");
				//clearFileLoops.add("loops file=\""+fileslash+cbit.getElementName()+"\" routine=\"#\"");
				clearInst.add("file = \""+cbit.getElementName()+"\"");
				
				clearInst.add("entry file = \""+cbit.getUnderlyingResource().getName()+"\" routine = \"#\" code = \"");
				
				//clearInst.add("file = \""+fileslash+cbit.getElementName()+"\"");
				tot++;
			}
			else
				if(type==-1)
				{
					String fortclass =cbit.getClass().getName();
					//System.out.println(fortclass);
					if(fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$Subroutine")||fortclass.equals("org.eclipse.photran.internal.core.model.FortranElement$MainProgram"))
					{
						//clearInst.add("loops file=\""+fileslash+cbit.getUnderlyingResource().getName()+"\" routine=\""+"#"+cbit.getElementName().toUpperCase()+"\"");
						
						selrouts.add("#"+cbit.getElementName().toUpperCase());
						clearInst.add("loops file=\""+cbit.getUnderlyingResource().getName()+"\" routine=\""+"#"+cbit.getElementName().toUpperCase()+"\"");
					}
					tot++;
				}
		}
		if(tot>0)
		{
			Selector excludeinst = new Selector(cbit.getCProject().getResource().getLocation().toOSString());
			//if(selfiles.size()>0)
			//{
				excludeinst.clearFile(selfiles);
				excludeinst.remInst(clearFileLoops);
				excludeinst.clearGenInst(clearInst);
				//excludeinst.remFileLoops();
			//}
			//if(selrouts.size()>0)
			//{
				excludeinst.clearRout(selrouts);
				excludeinst.remInst(clearRoutLoops);
			//}
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
