/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author clement chu
 *
 */
public class ManageBreakpointRulerActionDelegate extends AbstractRulerActionDelegate {
	private IEditorPart activeEditor;
	private ToggleBreakpointRulerAction targetAction = null;
	
	public IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		targetAction = new ToggleBreakpointRulerAction(editor, rulerInfo);
		return targetAction;
	}

	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (activeEditor != null) {
			if (targetAction != null) {
				targetAction.dispose();
				targetAction = null;
			}
		}
		activeEditor = targetEditor;
		super.setActiveEditor(callerAction, targetEditor);
	}
	
	public void mouseDoubleClick(MouseEvent e) {
		targetAction.run();
	}
}
