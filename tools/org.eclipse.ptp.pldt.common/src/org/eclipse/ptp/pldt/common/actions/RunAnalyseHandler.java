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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * RunAnalyseHandler - keeps state information for the analysis handlers in
 * the dropdown analysis menu.
 * Common behavior is in the RunAnalyseHandlerBase class.
 * 
 * That is, the AnalysisDropdownHandler will repeat the action of the last RunAnalyseHandler 
 * that was executed - they are cached as one of these.
 * 
 * @author Beth Tibbitts

 */

public abstract class RunAnalyseHandler extends AbstractHandler {
	/**
	 * the current selection is cached here
	 */
	protected IStructuredSelection selection;

	public RunAnalyseHandler(){
		
	}

	/**
	 * Get the current selection from the handler event. If it's a structured selection
	 * (e.g. resource in the project explorer) then return it.
	 * If it's e.g. a text selection in the editor, we don't care about that
	 * 
	 * Note that we  cache the last structured selection )like the action version
	 * of this is) since we don't get selection changed events.
	 * Can we somehow get those events? then this would be more accurate
	 * 
	 * @param event
	 * @return the current selection if it's a structured selection e.g. in the navigator
	 */
	public IStructuredSelection getSelection(ExecutionEvent event) {
		
		
		ISelection curSel = HandlerUtil.getCurrentSelection(event);
		if (curSel instanceof IStructuredSelection) {
			selection = (IStructuredSelection) curSel;
		}
		if(selection == null) {
			selection = AnalysisDropdownHandler.getInstance().getLastSelection();
		}
		// If there isn't a current selection appropriate for us,
		// get the last one used in any analysis.
		// Since we now register as a selection listener,
		// I doubt this is ever utilized.
		if (selection == null) {
			selection = AnalysisDropdownHandler.getLastAnalysisSelection();
		}
		return selection;
	
	}

}