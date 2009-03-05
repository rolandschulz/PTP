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
package org.eclipse.ptp.perf.tau.selinstfort.popup.actions;


import java.util.LinkedHashSet;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.perf.tau.selinst.Selector;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Action for adding selective instrumentation of user defined events to a Photran source file via selection of source code in the editor 
 * @author wspear
 *
 */
public class Instrument implements IEditorActionDelegate {
	TextEditor textEditor;

	/**
	 * Saves a reference to the current active editor
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction,
	 *      IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (TextEditor) targetEditor;
	}

	/**
	 * Takes the position of the selected text and creates a user-defined selective instrumentation entry
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IFileEditorInput input = (IFileEditorInput)textEditor.getEditorInput();
		if (input != null) {
			input.getFile().getProject().getLocation().toOSString();
			String location = input.getFile().getProject().getLocation().toOSString();
	
			int insertregs=0;
			
			ITextSelection ts = (ITextSelection) textEditor.getSelectionProvider().getSelection();
	
			insertregs = ts.getStartLine()+1;
	
			class validateName implements IInputValidator{
	
				public String isValid(String newText) {
					if(newText.equals(""))
						return "Please enter valid text";
					return null;
				}
			}
			
			class validateValue implements IInputValidator{
	
				public String isValid(String newText) {
					String err="Please enter a valid double or variable name";
					if(newText.equals(""))
						return err;
					
					String fixed = newText.replaceAll("\\W", "");
					if(!newText.equals(fixed))
					{
						if(newText.length()-fixed.length()==1)
							if(newText.indexOf(".")>=0)
								if(fixed.replaceAll("\\d", "").equals(""))
									return null;
						return err;
					}
					return null;
				}
			}
			InputDialog namedialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "User Defined Event Name", "Please enter a unique name to associate with this user defined event", "", new validateName());
	
			if(namedialog.open() == Window.CANCEL)return;
			String testline = namedialog.getValue();
			
			
			InputDialog incdialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "User Defined Event Value", "Please enter a static value or available numeric variable", "", new validateValue());
			if(incdialog.open() == Window.CANCEL)return;
			String testinc = incdialog.getValue();
			
			String fixline = "TAU_"+testline.replaceAll("\\W", "");
			LinkedHashSet<String> instlines = new LinkedHashSet<String>();
			String initline1= "      integer "+fixline+"(2) / 0, 0 /";
			String initline2= "      save "+fixline;
			String regline =  "      call TAU_REGISTER_EVENT("+fixline+", \'"+testline+"\')";
			String evtline =  "      call TAU_EVENT("+fixline+", "+testinc+");";
			
	
			String nosselinitline1 = "entry file=\""+input.getFile().getName()+"\" routine=\"#\" code=\""+initline1+"\"";
			String nosselinitline2 = "entry file=\""+input.getFile().getName()+"\" routine=\"#\" code=\""+initline2+"\"";
			String nosselregline = "file=\""+input.getFile().getName()+"\" line="+insertregs+" code=\""+regline+"\"";
			String nosselinstline = "file=\""+input.getFile().getName()+"\" line="+insertregs+" code=\""+evtline+"\"";
			instlines.add(nosselinitline1);
			instlines.add(nosselinitline2);
			instlines.add(nosselregline);
			instlines.add(nosselinstline);
			
			Selector selectinst = new Selector(location);
			selectinst.addInst(instlines);
		}
	}

	/**
	 * Enables the action if text has been selected, otherwise, the action is
	 * disabled.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	
		action.setEnabled(true);
	}
}
