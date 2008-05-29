/**********************************************************************
 * Copyright (c) 2007,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Default handler for the "Parallel Analysis" toolbar icon command -
 * the work would be done by the menus that get registered (by other plugins)
 * to appear beneath it, but  this itself can do something useful
 * by repeating the last-initiated submenu action.  ("Do the last thing again")
 * 
 * This class also acts as a single point to cache the current structured selection
 * so that when a menu item is selected, the current selection can be accessed.
 * It registers as a selection listener to actively listen for selection changes,
 * instead of relying on HandlerUtil which won't get the first one (e.g. before
 * the plugin is loaded).
 * 
 * @author Beth Tibbitts
 *
 */
public class AnalysisDropdownHandler extends AbstractHandler implements ISelectionListener {
	static protected RunAnalyseHandler lastAnalysisHandler=null;
	static protected IStructuredSelection lastAnalysisSelection=null;
	protected IStructuredSelection lastSelection=null;
	protected static AnalysisDropdownHandler instance=null;
	private static final boolean traceOn=false;

	/**
	 * Constructor: set singleton instance, and set up selection listener to 
	 * listen for selections so we can report them more efficiently
	 * than relying on HandlerUtil.getCurrentSelection();
	 * if the current selection isn't a structured selection (e.g. editor selection or something
	 * we don't care about) then we want the last structured selection.
	 * If we can't find a structured selection at the time this ctor is called,
	 * go get the selection in the Project Explorer; it's probably what we want
	 * (for the case when an editor has just been opened by double-clicking on its
	 * entry in the project explorer: it's still selected in the Proj explorer,
	 * but the editor now has the focus).
	 */
	public AnalysisDropdownHandler(){
		if(traceOn)System.out.println("AnalysisDropdownHandler() ctor... should not be >1 of these");
		assert(instance==null);  // we presume this is a singleton
		instance=this;
		ISelectionService ss=null;
		try {
			CommonPlugin p = CommonPlugin.getDefault();
			IWorkbench w = p.getWorkbench();
			IWorkbenchWindow wbw=w.getActiveWorkbenchWindow();//null
			// register to be notified of future selections
			ss = CommonPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getSelectionService();
			// String id=CommonPlugin.getDefault().getWorkbench().
			// getActiveWorkbenchWindow
			// ().getActivePage().getActivePart().getSite().getId();
			ss.addSelectionListener(this);
		} catch (Exception e) {
			Throwable t=e.getCause();
			e.printStackTrace();
			return;// FIXME
		}

		// and cache the selection that was in effect now.
		ISelection sel= ss.getSelection();//gives selection in ACTIVE PART.If editor was just opened, active part is probably the editor.
		//
		if(sel instanceof IStructuredSelection) {
			lastSelection=(IStructuredSelection)sel;
			if(traceOn)System.out.println("  ...got initial selection.");
		}
		// If we still don't know the selection then find out the selection in the
		// project explorer view - its guess is probably right.
		if (lastSelection == null) {
			String projExpID = "org.eclipse.ui.navigator.ProjectExplorer";
			ISelection apSel = ss.getSelection(projExpID);
			if (apSel != null & apSel instanceof IStructuredSelection) {
				if (!apSel.isEmpty()) {
					lastSelection = (IStructuredSelection) apSel;
				}
			}
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(traceOn)System.out.println("AnalysisDropdownHandler.execute()...");
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if(traceOn)System.out.println("selection: "+selection);
		
		if(lastAnalysisHandler!=null){
			if(traceOn)System.out.println("Last analysis was: "+lastAnalysisHandler);
			lastAnalysisHandler.execute(event);
		}
		else {
			final String msg = "No analysis has been done yet to repeat.";
			MessageDialog.openInformation(null, "Repeat Analysis", msg);
		}

	    return null;
	}
	/**
	 * Remember what was last executed, so that we can give a repeat performance
	 * @param handler
	 * @param selection - cache the selection too; this will be used as a last 
	 * resort if another analysis doesn't see a recent selection it likes.
	 */
	public static void setLastHandledAnalysis(RunAnalyseHandler handler, IStructuredSelection selection){
		lastAnalysisHandler = handler;
		if(selection!=null){
			lastAnalysisSelection=selection;
		}
		
		if(traceOn)System.out.println("lastAnalysisHandler set to: "+lastAnalysisHandler);
		
	}
	public static IStructuredSelection getLastAnalysisSelection(){
		return lastAnalysisSelection;
	}

	/**
	 * Implemented for SelectionListener interface:
	 * Listen for selection changes and cache the ones that might be
	 * interesting to us.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			lastSelection=(IStructuredSelection)selection;
			if(traceOn)System.out.println("ADDH.selectionChanged, got structured selection");
		}
		
	}
	/**
	 * The last structured selection seen
	 * @return
	 */
	public IStructuredSelection getLastSelection() {
		return lastSelection;
	}
	/**
	 * Get the singleton instance of this class, probably for accessing its
	 * knowledge about the current/last selection of interest.
	 * 
	 * @return
	 */
	public static AnalysisDropdownHandler getInstance() {
		if(instance==null) {
			instance=new AnalysisDropdownHandler();
		}
		return instance;
	}

}
