package org.eclipse.cldt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.cldt.core.model.IWorkingCopy;
import org.eclipse.cldt.internal.ui.editor.FortranEditor;
import org.eclipse.cldt.internal.ui.editor.IReconcilingParticipant;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.cldt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.ITextEditor;


public class FortranReconcilingStrategy implements IReconcilingStrategy {


	private ITextEditor fEditor;	
	private IWorkingCopyManager fManager;
	private IProgressMonitor fProgressMonitor;


	public FortranReconcilingStrategy(FortranEditor editor) {
		fEditor= editor;
		fManager= FortranUIPlugin.getDefault().getWorkingCopyManager();
	}
	
	/**
	 * @see IReconcilingStrategy#reconcile(document)
	 */
	public void setDocument(IDocument document) {
	}	


	/*
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	/**
	 * @see IReconcilingStrategy#reconcile(region)
	 */
	public void reconcile(IRegion region) {
		reconcile();
	}


	/**
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, region)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		reconcile();
	}
	
	private void reconcile() {
		try {
			ITranslationUnit tu = fManager.getWorkingCopy(fEditor.getEditorInput());		
			if (tu != null && tu.isWorkingCopy()) {
				IWorkingCopy workingCopy = (IWorkingCopy)tu;
				// reconcile
				synchronized (workingCopy) {
					workingCopy.reconcile(true, fProgressMonitor);
				}
			}
			
			// update participants
			if (fEditor instanceof IReconcilingParticipant /*&& !fProgressMonitor.isCanceled()*/) {
				IReconcilingParticipant p= (IReconcilingParticipant) fEditor;
				p.reconciled(true);
			}
			
		} catch(CModelException e) {
				
		}
 	}	
}
