/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction
 * Version: 1.89
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.rdt.core.RDTLog;

/**
 * Navigates to the definition of a name, or to the declaration if invoked on the definition.
 */
public class OpenDeclarationsAction extends SelectionParseAction {
	
	ITextSelection fTextSelection;
	
	/**
	 * Creates a new action with the given editor
	 */
	public OpenDeclarationsAction(CEditor editor) {
		super( editor );
		setText(CEditorMessages.OpenDeclarations_label);
		setToolTipText(CEditorMessages.OpenDeclarations_tooltip);
		setDescription(CEditorMessages.OpenDeclarations_description);
	}

	@Override
	public void run() {
		OpenDeclarationsJob job = createJob();
		if (job != null)
			job.schedule();
	}

	/**
	 * For the purpose of regression testing.
	 * @since 4.0
	 */
	public void runSync() throws CoreException {
		OpenDeclarationsJob job = createJob();
		if (job != null)
			job.performNavigation(new NullProgressMonitor());
	}
	
	private OpenDeclarationsJob createJob() {
		String text= computeSelectedWord();
		OpenDeclarationsJob job= null;
		ICElement elem= fEditor.getInputCElement();
		if (elem instanceof ITranslationUnit && fTextSelection != null) {
			job= new OpenDeclarationsJob(this, (ITranslationUnit) elem, fTextSelection, text, fEditor);
		}
		return job;
	}


	private String computeSelectedWord() {
		fTextSelection = getSelectedStringFromEditor();
		String text = null;
		if (fTextSelection != null) {
			if (fTextSelection.getLength() > 0) {
				text= fTextSelection.getText();
			} else {
				IDocument document= fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				IRegion reg= CWordFinder.findWord(document, fTextSelection.getOffset());
				if (reg != null && reg.getLength() > 0) {
					try {
						text = document.get(reg.getOffset(), reg.getLength());
					} catch (BadLocationException e) {
						RDTLog.logError(e);
					}
				}
			}
		}
		return text;
	}
}

