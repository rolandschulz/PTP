/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.CHelpDisplayContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @since 2.0
 */
public class RemoteCEditorHelpContextProvider implements IContextProvider {

	private final ITextEditor editor;

	public RemoteCEditorHelpContextProvider(ITextEditor editor) {
		this.editor = editor;
	}

	public IContext getContext(Object arg0) {
		String selected = getSelectedString(editor);
		IContext context = HelpSystem.getContext(RDTHelpContextIds.REMOTE_C_CPP_EDITOR);
		if (context != null) {
			if (selected != null && selected.length() > 0) {
				try {
					context = new CHelpDisplayContext(context, editor, selected);
				} catch (CoreException exc) {
				}
			}
		}
		return context;
	}

	public int getContextChangeMask() {
		return SELECTION;
	}

	public String getSearchExpression(Object arg0) {
		return getSelectedString(editor);
	}

	private static String getSelectedString(ITextEditor editor) {
		try {
			ITextSelection selection = (ITextSelection) editor.getSite().getSelectionProvider().getSelection();
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			IRegion region = CWordFinder.findWord(document, selection.getOffset());
			if (region != null)
				return document.get(region.getOffset(), region.getLength());
		} catch (Exception e) {
		}

		return null;
	}
}
