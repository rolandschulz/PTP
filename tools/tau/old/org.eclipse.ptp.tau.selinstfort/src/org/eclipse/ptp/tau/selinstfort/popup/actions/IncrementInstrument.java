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
package org.eclipse.ptp.tau.selinstfort.popup.actions;

import java.util.LinkedHashSet;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
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

	AbstractFortranEditor textEditor;//TextEditor

	/**
	 * Saves a reference to the current active editor
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction,
	 *      IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (AbstractFortranEditor) targetEditor;
	}

	/**
	 * Counts the words in the selected text and displays the result in a dialog
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		textEditor.getIFile().getProject().getLocation().toOSString();
		String location = textEditor.getIFile().getProject().getLocation().toOSString();
		//String fileroot = textEditor.getIFile().getParent().getLocation().toOSString();
		//String fileslash="*"+File.separator;
		
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
		boolean dynamic=false;
		switch(optline)
		{
			case 0: regline="TAU_PROFILE_TIMER";startline="TAU_PROFILE_START";stopline="TAU_PROFILE_STOP";break;
			case 1: regline="TAU_PROFILE_TIMER_DYNAMIC";startline="TAU_PROFILE_START";stopline="TAU_PROFILE_STOP";dynamic=true;break;
			case 2: regline="TAU_PHASE_CREATE_STATIC";startline="TAU_PHASE_START";stopline="TAU_PHASE_STOP";break;
			case 3: regline="TAU_PHASE_CREATE_DYNAMIC";startline="TAU_PHASE_START";stopline="TAU_PHASE_STOP";dynamic=true;break;
			default: return;
		}
		
		InputDialog namedialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), "User Defined Event Name", "Please enter a unique name to associate with this user defined event", "", new validateName());
		//MessageDialog.openInformation(null, "Line Number","Start of line: " + insertevts);//"JDG2E: Word Count","Number of words: " + tokenCount
		//System.out.println("Need to get -project- of document input.");
		if(namedialog.open() == Window.CANCEL)return;
		String testline = namedialog.getValue();//"Instrument Me!";
		
		String fixline = "TAU_"+testline.replaceAll("\\W", "");
		//System.out.println(fixline);
		
		LinkedHashSet instlines = new LinkedHashSet();
		String initline1= "      integer "+fixline+"(2)/ 0, 0 /";
		String initline2= "      save "+fixline;
		//instlines.add(initline1);
		//instlines.add(initline2);
		String selbufdef=null;//   = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+bufdef+"\"";
		String selcountdef=null;// = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+countdef+"\"";
		String selsavecount=null;//  = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+savecount+"\"";
		String selincrement=null;//= "file = \""+textEditor.getIFile().getName()+"\" line = "+insertregs+" code = \""+increment+"\"";
		String selwritebuf=null;// =
		if(dynamic)
		{
			String counter = fixline+"_C";
			String buffer = fixline+"_B";
			
			int len = testline.length()+9+1;
			String bufdef =   "      character(len="+len+")"+buffer;
			String countdef = "      integer "+counter+"/ -1 /";
			String savecount =  "      save "+counter;
			
			String increment = "      "+counter+"="+counter+"+1";
			String writebuf =  "      write ("+buffer+",'(a"+(testline.length()+1)+",i9)') '"+testline+" ',"+counter;
			
			
			selbufdef   = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+bufdef+"\"";
			selcountdef = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+countdef+"\"";
			selsavecount  = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+savecount+"\"";
			selincrement= "file = \""+textEditor.getIFile().getName()+"\" line = "+insertregs+" code = \""+increment+"\"";
			selwritebuf = "file = \""+textEditor.getIFile().getName()+"\" line = "+insertregs+" code = \""+writebuf+"\"";
			
			regline = "      call "+regline+"("+fixline+","+buffer+")";
		}
		else
		regline   = "      call "+regline+"("+fixline+", \'"+testline+"\')";
		
		startline = "      call "+startline+"("+fixline+")";//, "+testinc+"
		stopline  = "      call "+stopline+"("+fixline+")";//, "+testinc+"
		
		
		//if(useclean){
		String nosselinitline1 = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+initline1+"\"";
		String nosselinitline2 = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+initline2+"\"";
		//String nosselregline   = "entry file = \""+textEditor.getIFile().getName()+"\" routine = \"#\" code = \""+regline+"\"";
		/*Tau compiler script doesn't always put items with the same line insertion number in the same order, so put at program entry*/
		String nosselregline   = "file = \""+textEditor.getIFile().getName()+"\" line = "+insertregs+" code = \""+regline+"\"";
		String nosselstartline = "file = \""+textEditor.getIFile().getName()+"\" line = "+insertregs+" code = \""+startline+"\"";
		String nosselstopline  = "file = \""+textEditor.getIFile().getName()+"\" line = "+insertstops+" code = \""+stopline+"\"";
		instlines.add(nosselinitline1);
		instlines.add(nosselinitline2);
		if(dynamic)
		{
			instlines.add(selbufdef);
			instlines.add(selcountdef);
			instlines.add(selsavecount);
			instlines.add(selincrement);
			instlines.add(selwritebuf);
		}
		instlines.add(nosselregline);
		instlines.add(nosselstartline);
		instlines.add(nosselstopline);
		//}
		//System.out.println(selregline);
		//System.out.println(selinstline);
		
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
