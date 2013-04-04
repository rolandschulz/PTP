/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM - derivative work
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.LineBackgroundPainter;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider3;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Paints code lines disabled by preprocessor directives (#ifdef etc.) with a
 * configurable background color (default light gray).
 * 
 * @see LineBackgroundPainter
 * @since 4.2
 */
public class RemoteInactiveHighlighting implements ICReconcilingListener, ITextInputListener {

	public static final String INACTIVE_CODE_ENABLE = "inactiveCodeEnable"; //$NON-NLS-1$
	public static final String INACTIVE_CODE_COLOR = "inactiveCodeColor"; //$NON-NLS-1$
	public static final String INACTIVE_CODE_KEY = "inactiveCode"; //$NON-NLS-1$

	private LineBackgroundPainter fLineBackgroundPainter;
	private Job fUpdateJob;
	private Object fJobLock = new Object();
	private RemoteCEditor fEditor;
	private List<Position> fInactiveCodePositions = Collections.emptyList();
	private IDocument fDocument;

	private IPreferenceStore fPrefStore;
	private ISharedTextColors fSharedColors;

	public RemoteInactiveHighlighting(IPreferenceStore store, ISharedTextColors sharedColors) {
		this.fPrefStore = store;
		this.fSharedColors = sharedColors;
	}

	/**
	 * Schedule update of the inactive code positions in the background.
	 */
	private void scheduleJob() {
		synchronized (fJobLock) {
			if (fUpdateJob == null) {
				fUpdateJob = new Job(CEditorMessages.InactiveCodeHighlighting_job) {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						reconciled(null, true, monitor);
						return Status.OK_STATUS;
					}
				};
				fUpdateJob.setPriority(Job.DECORATE);
			}
			if (fUpdateJob.getState() == Job.NONE) {
				fUpdateJob.schedule();
			}
		}
	}

	public void install(RemoteCEditor editor) {
		fEditor = editor;
		installLinePainter();
		fDocument = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		fEditor.getViewer().addTextInputListener(this);
		fEditor.addReconcileListener(this);
	}

	private void installLinePainter() {
		ITextViewer viewer = fEditor.getViewer();
		fLineBackgroundPainter = new LineBackgroundPainter(viewer);
		fLineBackgroundPainter.enableCursorLine(false);
		fLineBackgroundPainter.setBackgroundColor(INACTIVE_CODE_KEY, getColor(INACTIVE_CODE_COLOR));
		((ITextViewerExtension2) viewer).addPainter(fLineBackgroundPainter);
	}

	private void uninstallLinePainter() {
		ITextViewer viewer = fEditor.getViewer();
		if (fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
			fLineBackgroundPainter.removeHighlightPositions(fInactiveCodePositions);
			fInactiveCodePositions = Collections.emptyList();
			fLineBackgroundPainter.deactivate(true);
			fLineBackgroundPainter.dispose();
			((ITextViewerExtension2) viewer).removePainter(fLineBackgroundPainter);
			fLineBackgroundPainter = null;
		}
	}

	public void updateInactiveCodeColor() {
		if (fLineBackgroundPainter != null) {
			fLineBackgroundPainter.setBackgroundColor(INACTIVE_CODE_KEY, getColor(INACTIVE_CODE_COLOR));
			fLineBackgroundPainter.redraw();
		}
	}

	private Color getColor(String key) {
		if (fPrefStore != null) {
			RGB rgb = PreferenceConverter.getColor(fPrefStore, key);
			return fSharedColors.getColor(rgb);
		}
		return null;
	}

	public void uninstall() {
		synchronized (fJobLock) {
			if (fUpdateJob != null && fUpdateJob.getState() == Job.RUNNING) {
				fUpdateJob.cancel();
			}
		}
		uninstallLinePainter();
		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			if (fEditor.getViewer() != null) {
				fEditor.getViewer().removeTextInputListener(this);
			}
			fEditor = null;
			fDocument = null;
		}
	}

	/**
	 * Force refresh.
	 */
	public void refresh() {
		scheduleJob();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.ICReconcilingListener#reconciled(IASTTranslationUnit, boolean, IProgressMonitor)
	 */
	public void reconciled(IASTTranslationUnit ast, final boolean force, IProgressMonitor progressMonitor) {
		// local ast is null, don't use it
		if (progressMonitor != null && progressMonitor.isCanceled()) {
			return;
		}
		final List<Position> newInactiveCodePositions = collectInactiveCodePositions();
		Runnable updater = new Runnable() {
			public void run() {
				if (fEditor != null && fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
					fLineBackgroundPainter.replaceHighlightPositions(fInactiveCodePositions, newInactiveCodePositions);
					fInactiveCodePositions = newInactiveCodePositions;
				}
			}
		};
		if (fEditor != null) {
			Display.getDefault().asyncExec(updater);
		}
	}

	private List<Position> collectInactiveCodePositions() {
		IProject project = ((RemoteCEditor) fEditor).getInputCElement().getCProject().getProject();
		IRemoteInactiveHighlightingService service = getInactiveHighlightingService(project);
		if(service == null)
			return Collections.emptyList();

		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy workingCopy = manager.getWorkingCopy(fEditor.getEditorInput());

		List<Position> positions = service.computeInactiveHighlightingPositions(fDocument, workingCopy);
		if (positions == null)
			positions = Collections.emptyList();

		return positions;
	}

	private IRemoteInactiveHighlightingService getInactiveHighlightingService(IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		if(serviceProvider instanceof IIndexServiceProvider3) {
			return ((IIndexServiceProvider3) serviceProvider).getRemoteInactiveHighlightingService();
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (fEditor != null && fLineBackgroundPainter != null && !fLineBackgroundPainter.isDisposed()) {
			fLineBackgroundPainter.removeHighlightPositions(fInactiveCodePositions);
			fInactiveCodePositions = Collections.emptyList();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		fDocument = newInput;
	}

}
