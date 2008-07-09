/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.editor.GotoMatchingBracketAction
 * Version: 1.4
 */

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;


public class GotoMatchingBracketAction extends Action {

	public final static String GOTO_MATCHING_BRACKET= "GotoMatchingBracket"; //$NON-NLS-1$
	
	private final CEditor fEditor;
	
	public GotoMatchingBracketAction(CEditor editor) {
		super(CEditorMessages.getString("GotoMatchingBracket.label")); //$NON-NLS-1$
		Assert.isNotNull(editor);
		fEditor= editor;
		setEnabled(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_MATCHING_BRACKET_ACTION);
	}
	
	public void run() {
		fEditor.gotoMatchingBracket();
	}
}
