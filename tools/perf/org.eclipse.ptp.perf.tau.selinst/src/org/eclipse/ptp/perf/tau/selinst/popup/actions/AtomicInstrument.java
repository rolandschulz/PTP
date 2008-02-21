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

/*
 * Thanks to "The Java Developer's Guide to Eclipse"
 *   by D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
 */

import java.util.LinkedHashSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
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

/**
 * Action for adding selective instrumentation of user defined events to a CDT source file via selection of source code in the editor 
 */
public class AtomicInstrument implements IEditorActionDelegate {
	CEditor textEditor;

	/**
	 * Saves a reference to the current active editor
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction,
	 *      IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (CEditor) targetEditor;
	}

	/**
	 * Takes the position of the selected text and creates a user-defined selective instrumentation entry
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ICElement cele = textEditor.getInputCElement();
		ICProject cproject = cele.getCProject();
		String location = cproject.getResource().getLocation().toOSString();
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
		
		String fixline = testline.replaceAll("\\W", "");
		LinkedHashSet<String> instlines = new LinkedHashSet<String>();
		String regline = "TAU_REGISTER_EVENT(TAU__"+fixline+", \\\""+testline+"\\\");";
		String evtline = "TAU_EVENT(TAU__"+fixline+", "+testinc+");";
		String nosselregline = "file =\""+cele.getElementName()+"\" line="+insertregs+" code=\""+regline+" "+evtline+"\"";
		instlines.add(nosselregline);
		
		Selector selectinst = new Selector(location);
		selectinst.addInst(instlines);
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
