/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlighting;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingPresenter;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingReconciler;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.ptp.internal.rdt.ui.editor.RemoteSemanticHighlightingManager;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;


/**
 * Remote Semantic highlighting reconciler
 */

public class RemoteSemanticHighlightingReconciler extends SemanticHighlightingReconciler {

	/** Reconcile operation lock. */
	private final Object fReconcileLock= new Object();
	
	private boolean fIsReconciling= false;

	/** Highlightings */
	protected RemoteSemanticHighlightingManager.HighlightingStyle[] fHighlightings;

	/** Highlightings - cache for background thread, only valid during {@link #reconciled(IASTTranslationUnit, boolean, IProgressMonitor)} */
	protected RemoteSemanticHighlightingManager.HighlightingStyle[] fJobHighlightings;
	
	/** The Remote C editor this semantic highlighting reconciler is installed on */
	private RemoteCEditor fRemoteEditor;
	
	/** Background job */
	private Job fJob;
	/** Background job lock */
	private final Object fJobLock= new Object();

	
	/**
	 * @param positions are separated by commas. Element x = the offset, 
	 * x+1 = the length, x+2 is the Highlightings index.
	 */
	private void parsePositions(String positions) {
		String [] positionList = positions.split(","); //$NON-NLS-1$
		
		for(int i=0; i < positionList.length; i+=3) {
			int lgtIndex = Integer.parseInt(positionList[i+2]);
			if (fJobHighlightings[lgtIndex].isEnabled()){
				int offset = Integer.parseInt(positionList[i]);
				int length = Integer.parseInt(positionList[i+1]);
				addPosition(offset, length, fJobHighlightings[lgtIndex]);
			} 
		}
		sortPositions();
	}

	/**
	 * Add a position with the given range and highlighting iff it does not exist already.
	 * 
	 * @param offset The range offset
	 * @param length The range length
	 * @param highlighting The highlighting
	 */
	private void addPosition(int offset, int length, HighlightingStyle highlighting) {
		boolean isExisting= false;
		// TODO: use binary search
		for (int i= 0, n= fRemovedPositions.size(); i < n; i++) {
			HighlightedPosition position= fRemovedPositions.get(i);
			if (position == null)
				continue;
			if (position.isEqual(offset, length, highlighting)) {
				isExisting= true;
				fRemovedPositions.set(i, null);
				fNOfRemovedPositions--;
				break;
			}
		}

		if (!isExisting) {
			HighlightedPosition position= fJobPresenter.createHighlightedPosition(offset, length, highlighting);
			fAddedPositions.add(position);
		}
	}
	
	private IRemoteSemanticHighlightingService getSemanticHighlightingService(IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		if (!(serviceProvider instanceof IIndexServiceProvider2)) {
			return null;
		}
		IRemoteSemanticHighlightingService service = ((IIndexServiceProvider2) serviceProvider).getRemoteSemanticHighlightingService();
		return service;
	}
	
	
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		// ensure at most one thread can be reconciling at any time
		synchronized (fReconcileLock) {
			if (fIsReconciling)
				return;
			fIsReconciling= true;
		}
		
		TextPresentation textPresentation= null;
		boolean updatePresentation = false;
		
		fJobPresenter= fPresenter;
		fJobSemanticHighlightings= fSemanticHighlightings;
		fJobHighlightings= fHighlightings;
		
		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null || fJobHighlightings == null)
				return;
			
			IWorkingCopyManager fManager= CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy workingCopy= fManager.getWorkingCopy(fRemoteEditor.getEditorInput());
			
			if (workingCopy == null) {
				return;
			}
			
			fJobPresenter.setCanceled(progressMonitor != null && progressMonitor.isCanceled());
			
			startReconcilingPositions();
			
			if (!fJobPresenter.isCanceled()) {
				IProject project = ((RemoteCEditor) fRemoteEditor).getInputCElement().getCProject().getProject();
				IRemoteSemanticHighlightingService rsgs = getSemanticHighlightingService(project);
				
				/* 
				 * Send the working copy to the server. It will return the found positions in a comma 
				 * separated list. 
				 */
				String foundPositions = rsgs.computeSemanticHighlightingPositions(workingCopy);
				if (foundPositions != null && foundPositions.compareTo("") != 0) { //$NON-NLS-1$
					parsePositions(foundPositions);
					updatePresentation = true;
				}
			}
			
			if (!fJobPresenter.isCanceled() && updatePresentation) {
				//Update the presentation just as is done with the local C Editor
				textPresentation= fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
				updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);
			}
			stopReconcilingPositions();
		
		} finally {
			fJobPresenter= null;
			fJobSemanticHighlightings= null;
			fJobHighlightings= null;
			synchronized (fReconcileLock) {
				fIsReconciling= false;
			}
		}
	}
	
	private void sortPositions() {
		List<HighlightedPosition> oldPositions= fRemovedPositions;
		List<HighlightedPosition> newPositions= new ArrayList<HighlightedPosition>(fNOfRemovedPositions);
		for (int i= 0, n= oldPositions.size(); i < n; i ++) {
			HighlightedPosition current= oldPositions.get(i);
			if (current != null)
				newPositions.add(current);
		}
		fRemovedPositions= newPositions;
		// positions need to be sorted by ascending offset
		Collections.sort(fAddedPositions, new Comparator<Position>() {
			public int compare(final Position p1, final Position p2) {
				return p1.getOffset() - p2.getOffset();
			}});
	}

	
	/**
	 * Update the presentation.
	 *
	 * @param textPresentation the text presentation
	 * @param addedPositions the added positions
	 * @param removedPositions the removed positions
	 */
	protected void updatePresentation(TextPresentation textPresentation, List<HighlightedPosition> addedPositions, List<HighlightedPosition> removedPositions) {
		Runnable runnable= fJobPresenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
		if (runnable == null)
			return;

		RemoteCEditor editor= fRemoteEditor;
		
		if (editor == null)
			return;

		IWorkbenchPartSite site= editor.getSite();
		if (site == null)
			return;

		Shell shell= site.getShell();
		if (shell == null || shell.isDisposed())
			return;

		Display display= shell.getDisplay();
		if (display == null || display.isDisposed())
			return;

		display.asyncExec(runnable);
	}
	
	/**
	 * Install this reconciler on the given editor, presenter and highlightings.
	 * @param editor the editor
	 * @param sourceViewer the source viewer
	 * @param fPresenter2 the semantic highlighting presenter
	 * @param semanticHighlightings the semantic highlightings
	 * @param highlightings the highlightings
	 */
	public void install(RemoteCEditor editor, ISourceViewer sourceViewer, SemanticHighlightingPresenter fPresenter2, SemanticHighlighting[] semanticHighlightings, RemoteSemanticHighlightingManager.HighlightingStyle[] highlightings) {
		fPresenter= fPresenter2;
		fSemanticHighlightings= semanticHighlightings;
		fHighlightings= highlightings;

		fRemoteEditor= editor;

		if (fRemoteEditor != null) {
			fRemoteEditor.addReconcileListener(this);
		}
	}
	
	/**
	 * Uninstall this reconciler from the editor
	 */
	public void uninstall() {
		if (fPresenter != null)
			fPresenter.setCanceled(true);

		if (fRemoteEditor != null) {
			fRemoteEditor.removeReconcileListener(this);
			fRemoteEditor= null;
		}

		fSemanticHighlightings= null;
		fHighlightings= null;
		fPresenter= null;
	}

	/**
	 * Refreshes the highlighting.
	 */
	public void refresh() {
		reconciled(null, true, new NullProgressMonitor());
	}

}
