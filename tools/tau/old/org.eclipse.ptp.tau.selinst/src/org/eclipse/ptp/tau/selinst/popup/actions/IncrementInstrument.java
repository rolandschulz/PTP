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

/*
 * "The Java Developer's Guide to Eclipse"
 *   by D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
 * 
 * (C) Copyright International Business Machines Corporation, 2004. 
 * All Rights Reserved.
 * 
 * Code or samples provided herein are provided without warranty of any kind.
 */

import java.util.LinkedHashSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.tau.selinst.Selector;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * A Workbench toolbar action is defined that counts the words in the selected
 * area of a text file. 
 * 
 * See Chapter 21 for a discussion of the implementation.
 * @see org.eclipse.ui.IEditorActionDelegate
 */
public class IncrementInstrument implements IEditorActionDelegate {
	//static final String WORD_DELIMITERS = " .,/?<>;:[]{}\\|`~!@#$%^&*()-_+=\n\r";

	CEditor textEditor;//TextEditor

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
	 * Counts the words in the selected text and displays the result in a dialog
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//System.out.println("Test");
		ICElement cele = textEditor.getInputCElement();

		//ICProject cproject = cele.getCProject();
		//String location = cproject.getResource().getLocation().toOSString();
		//String fileroot = cele.getParent().getResource().getLocation().toOSString();
		//String fileslash="*"+File.separator;
		//System.out.println(cele.getElementType());
		
		//boolean managed = ManagedBuildManager.canGetBuildInfo(cproject.getResource());
		//boolean useclean=true;//If the file is at the top level, we need both a slashed and a clean filename
		//if((location.length()!=fileroot.length()))//If it is deeper, we do not need the clean filename
		//	useclean=false;
		
		//System.out.println(cele.getElementType());
		//ITranslationUnit cunit = (ITranslationUnit)cele;
		
		int insertregs=0;
		//int insertstarts=0;
		int insertstops=0;
		//IInclude[] includes=null;
		/*
		try {
			IInclude[] includes = cunit.getIncludes();
			if(includes!=null && includes.length>0)
				insertregs = includes[includes.length-1].getSourceRange().getEndLine()+1;
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		//System.out.println(cele.getPath());
		//IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
		ITextSelection ts = (ITextSelection) textEditor.getSelectionProvider().getSelection();
		if(ts.getLength()<=0)
		{
			System.out.println("Please select the area you want to instrument.");
			//MessageDialog nosel = new MessageDialog(CUIPlugin.getActiveWorkbenchShell(), "Code Selection Error", null, null, insertstops, null, insertstops);
		}
		//System.out.println("Title "+textEditor.getTitle());
		//int tokenCount;
		insertregs = ts.getStartLine()+1;
		//insertstarts = insertregs+1;
		insertstops= ts.getEndLine()+2;
/*
		try {
			String text = document.get(ts.getOffset(), ts.getLength());
			tokenCount = new StringTokenizer(text, WORD_DELIMITERS)
					.countTokens();
		} catch (BadLocationException e) {
			tokenCount = 0;
		}*/
		//IInputValidator validator = new IInputValidator();
		class validateName implements IInputValidator{

			public String isValid(String newText) {
				if(newText.equals(""))
					return "Please enter valid text";
				//if(newText.replaceAll(regex, replacement))
				// TODO Auto-generated method stub
				return null;
			}
		}
		
		class validateValue implements IInputValidator{

			public String isValid(String newText) {
				
				//String fixednum=newText.
				String err="Please enter a valid double or variable name";
				if(newText.equals(""))
					return err;
				
				String fixed = newText.replaceAll("\\W", "");
				if(!newText.equals(fixed))
				{
					if(newText.length()-fixed.length()==1)
						if(newText.contains("."))
							if(fixed.replaceAll("\\d", "").equals(""))
								return null;
					return err;
				}
				
				//if(newText.replaceAll(regex, replacement))
				// TODO Auto-generated method stub
				return null;
			}
		}
		String[] opts={"Static Timer","Dynamic Timer","Static Phase","Dynamic Phase","Cancel"};
		MessageDialog timephase = new MessageDialog(CUIPlugin.getActiveWorkbenchShell(), "Instrumentation Type Selection",null,"Please select one of the following incremental user defined event types",MessageDialog.QUESTION,opts,0);
		//ListDialog timephase = new ListDialog(CUIPlugin.getActiveWorkbenchShell());
		//timephase.setAddCancelButton(true);
		//timephase.setInput(opts);
		if(timephase.open() == 4)return;//return;
		int optline = timephase.getReturnCode();
		String regline="";
		String startline="";
		String stopline="";
		boolean dynamic = false;
		switch(optline)
		{
			case 0: regline="{TAU_PROFILE_TIMER";startline="TAU_PROFILE_START";stopline="TAU_PROFILE_STOP";break;
			case 1: regline="TAU_PROFILE_TIMER_DYNAMIC";startline="TAU_PROFILE_START";stopline="TAU_PROFILE_STOP";dynamic=true;break;
			case 2: regline="{TAU_PHASE_CREATE_STATIC";startline="TAU_PHASE_START";stopline="TAU_PHASE_STOP";break;
			case 3: regline="TAU_PHASE_CREATE_DYNAMIC";startline="TAU_PHASE_START";stopline="TAU_PHASE_STOP";dynamic=true;break;
			default: return;
		}
		
		InputDialog namedialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "User Defined Event Name", "Please enter a unique name to associate with this user defined event", "", new validateName());
		//MessageDialog.openInformation(null, "Line Number","Start of line: " + insertevts);//"JDG2E: Word Count","Number of words: " + tokenCount
		//System.out.println("Need to get -project- of document input.");
		if(namedialog.open() == Window.CANCEL)return;
		String testline = namedialog.getValue();//"Instrument Me!";
		
		
		//InputDialog typdialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "TAU Event Type", "Please enter a valid TAU Event designator, or leave blank for none", "", null);// new validateValue()
		//if(typdialog.open() == typdialog.CANCEL)return;
		//String typtinc = typdialog.getValue();
		
		//InputDialog groupdialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "TAU Group", "Please enter a valid TAU group name", "TAU_USER", new validateName());
		//if(groupdialog.open() == groupdialog.CANCEL)return;
		//String taugroup = groupdialog.getValue();
		LinkedHashSet instlines = new LinkedHashSet();
		String fixline = "TAU__"+testline.replaceAll("\\W", "");
		//System.out.println(fixline);
		
		
		
		if(dynamic)
		{
			String counter=fixline+"_counter";
			String buffer=fixline+"_buffer";
			String dynavars = "{char "+buffer+"[1024]; static int "+counter+"=0;  sprintf("+buffer+",\\\""+testline+" %d\\\","+counter+"++);";
			regline=dynavars+regline+"("+fixline+", "+buffer+", \\\"\\\""+", TAU_USER);";;
		}
		else
		regline   += "("+fixline+", \\\""+testline+"\\\""+", \\\"\\\""+", TAU_USER);";
		startline += "("+fixline+");";//, "+testinc+"
		stopline  += "("+fixline+");}";//, "+testinc+"
		/*
		String selregline   = "file = \""+fileslash+cele.getElementName()+"\" line = "+insertregs+" code = \""+regline+" "+startline+"\"";
		//String selstartline = "file = \""+fileslash+cele.getElementName()+"\" line = "+insertstarts+" code = \""+startline+"\"";
		String selstopline  = "file = \""+fileslash+cele.getElementName()+"\" line = "+insertstops+" code = \""+stopline+"\"";
		
		
		instlines.add(selregline);
		//instlines.add(selstartline);
		instlines.add(selstopline);
		*/
		//if(useclean){
		String nosselregline   = "file = \""+cele.getElementName()+"\" line = "+insertregs+" code = \""+regline+" "+startline+"\"";
		//String nosselstartline = "file = \""+cele.getElementName()+"\" line = "+insertstarts+" code = \""+startline+"\"";
		String nosselstopline  = "file = \""+cele.getElementName()+"\" line = "+insertstops+" code = \""+stopline+"\"";
		instlines.add(nosselregline);
		//instlines.add(selstartline);
		instlines.add(nosselstopline);
		//}
		//System.out.println(selregline);
		//System.out.println(selinstline);
		
		
		
		Selector selectinst = new Selector(cele.getCProject().getResource().getLocation().toOSString());
		selectinst.addInst(instlines);
	}

	/**
	 * Enables the action if text has been selected, otherwise, the action is
	 * disabled.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		/*if (selection != null && selection instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) selection;

			if (ts.getLength() == 0) {
				System.out.println("TextEditorWordCountAction disabled");
				action.setEnabled(false);
			} else {
				System.out.println("TextEditorWordCountAction enabled");
				action.setEnabled(true);
			}
		} else {
			System.out.println("TextEditorWordCountAction disabled");
			action.setEnabled(false);
		}*/
		action.setEnabled(true);
	}
}
