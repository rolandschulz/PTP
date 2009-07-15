/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.core.model.ProgressMonitorAndCanceler;
import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.cdt.internal.ui.text.CSourceViewerScalableConfiguration;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class RemoteCSourceViewerConfiguration extends
	CSourceViewerScalableConfiguration {

	public RemoteCSourceViewerConfiguration(IColorManager colorManager,
			IPreferenceStore preferenceStore, ITextEditor editor,
			String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (super.getReconciler(sourceViewer) == null)
			return null;
		
		if (fTextEditor != null) {
			RemoteCCompositeReconcilingStrategy strategy=
				new RemoteCCompositeReconcilingStrategy(sourceViewer, fTextEditor, getConfiguredDocumentPartitioning(sourceViewer));
			MonoReconciler reconciler= new CReconciler(fTextEditor, strategy);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new ProgressMonitorAndCanceler());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}
}

