/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.cldt.internal.ui.util;

import org.eclipse.cldt.internal.ui.editor.FortranEditor;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;

/**
 * 
 * @since 2.1
 */
public class CUIHelp {

	public static void setHelp(FortranEditor editor, StyledText text, String contextId) {
		CUIHelpListener listener= new CUIHelpListener(editor, contextId);
		text.addHelpListener(listener);
	}

	private static class CUIHelpListener implements HelpListener {

		private String fContextId;
		private FortranEditor fEditor;

		public CUIHelpListener(FortranEditor editor, String contextId) {
			fContextId= contextId;
			fEditor= editor;
		}

		/*
		* @see HelpListener#helpRequested(HelpEvent)
		* 
		*/
		public void helpRequested(HelpEvent e) {
			try {
				CHelpDisplayContext.displayHelp(fContextId, fEditor);
			} catch (CoreException x) {
				FortranUIPlugin.getDefault().log(x);
			}
		}
	}

}
