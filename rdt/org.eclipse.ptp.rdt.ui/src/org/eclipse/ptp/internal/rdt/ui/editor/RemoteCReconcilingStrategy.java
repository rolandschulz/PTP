/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ptp.internal.rdt.core.model.RemoteModelWorkingCopy;
import org.eclipse.ptp.internal.rdt.core.model.RemoteReconcileWorkingCopyOperation;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;

public class RemoteCReconcilingStrategy extends CReconcilingStrategy {

	private ITextEditor fEditor;
	private IWorkingCopyManager fManager;
	private IProgressMonitor fProgressMonitor;
	
	public RemoteCReconcilingStrategy(ITextEditor editor) {
		super(editor);
		fEditor = editor;
		fManager= CUIPlugin.getDefault().getWorkingCopyManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.CReconcilingStrategy#initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		//set up working copy on initial reconcile
		reconcile(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.CReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion region) {
		reconcile(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.CReconcilingStrategy#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}
	
	private void reconcile(final boolean initialReconcile) {
		boolean computeAST= fEditor instanceof ICReconcilingListener;
		RemoteModelWorkingCopy rmWorkingCopy = null;
		IWorkingCopy workingCopy = fManager.getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null) {
			return;
		}
		boolean forced= false;
		try {
			// reconcile
			synchronized (workingCopy) {
				forced= workingCopy.isConsistent();
				
				RemoteReconcileWorkingCopyOperation op = new RemoteReconcileWorkingCopyOperation(workingCopy, computeAST, true);
		        op.runOperation(fProgressMonitor);
		        rmWorkingCopy = op.fRmWorkingCopy;
			}
		} catch (OperationCanceledException oce) {
			// document was modified while parsing
		} catch (CModelException e) {
			IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, "Error in CDT UI during reconcile", e);  //$NON-NLS-1$
			UIPlugin.log(status);
		} finally {
			try {
				synchronized (rmWorkingCopy) {
					((ICReconcilingListener)fEditor).reconciled(null, true, fProgressMonitor);
				}
			} catch(Exception e) {
				IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, "Error in CDT UI during reconcile", e);  //$NON-NLS-1$
				UIPlugin.log(status);
			}
		}
		
 	}
}
