/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
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
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Default handler for the "Parallel Analysis" toolbar icon command -
 * the work would be done by the menus that get registered (by other plugins)
 * to appear beneath it, but  this itself can do something useful
 * by repeating the last-initiated submenu action.  ("Do the last thing again")
 * 
 * @author tibbitts
 *
 */
public class AnalysisDropdownHandler extends AbstractHandler {
	static protected RunAnalyseHandler lastAnalysisHandler=null;
	static protected IStructuredSelection lastAnalysisSelection=null;
	private static final boolean traceOn=false;


	@Override
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

}
