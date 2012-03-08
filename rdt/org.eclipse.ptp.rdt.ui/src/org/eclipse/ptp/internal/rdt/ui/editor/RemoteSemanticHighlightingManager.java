/*******************************************************************************
 *  Copyright (c) 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.swt.SWT;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlighting;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingPresenter;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.internal.ui.text.CSourceViewerScalableConfiguration;

/**
 * Semantic highlighting manager.
 * Cloned from JDT.
 * 
 * @since 4.0
 */
public class RemoteSemanticHighlightingManager extends SemanticHighlightingManager {
	
	/** Remote Semantic highlighting reconciler */
	private RemoteSemanticHighlightingReconciler fRemoteReconciler;
	/** The editor */
	private RemoteCEditor remoteCEditor;

	/**
	 * Install the semantic highlighting on the given editor infrastructure
	 *
	 * @param editor The remote C editor
	 * @param sourceViewer The source viewer
	 * @param colorManager The color manager
	 * @param preferenceStore The preference store
	 */
	public void install(RemoteCEditor editor, CSourceViewer sourceViewer, IColorManager colorManager, IPreferenceStore preferenceStore) {
		remoteCEditor= editor;
		fSourceViewer= sourceViewer;
		fColorManager= colorManager;
		fPreferenceStore= preferenceStore;
		if (remoteCEditor != null) {
			fConfiguration= new CSourceViewerScalableConfiguration(colorManager, preferenceStore, editor, ICPartitions.C_PARTITIONING);
			fPresentationReconciler= (CPresentationReconciler) fConfiguration.getPresentationReconciler(sourceViewer);
		} else {
			fConfiguration= null;
			fPresentationReconciler= null;
		}

		fPreferenceStore.addPropertyChangeListener(this);

		if (isEnabled())
			enable();
	}

	/**
	 * Install the semantic highlighting on the given source viewer infrastructure. No reconciliation will be performed.
	 *
	 * @param sourceViewer the source viewer
	 * @param colorManager the color manager
	 * @param preferenceStore the preference store
	 * @param hardcodedRanges the hard-coded ranges to be highlighted
	 */
	public void install(CSourceViewer sourceViewer, IColorManager colorManager, IPreferenceStore preferenceStore, HighlightedRange[][] hardcodedRanges) {
		fHardcodedRanges= hardcodedRanges;
		install(null, sourceViewer, colorManager, preferenceStore);
	}

	/**
	 * Enable semantic highlighting.
	 */
	protected void enable() {
		initializeHighlightings();

		fPresenter= new SemanticHighlightingPresenter();
		fPresenter.install(fSourceViewer, fPresentationReconciler);

		if (remoteCEditor != null) {
			fRemoteReconciler= new RemoteSemanticHighlightingReconciler();
			fRemoteReconciler.install(remoteCEditor, fSourceViewer, fPresenter, fSemanticHighlightings, fHighlightings);
		} else {
			fPresenter.updatePresentation(null, createHardcodedPositions(), new HighlightedPosition[0]);
		}
	}

	/**
	 * Uninstall the semantic highlighting
	 */
	public void uninstall() {
		disable();

		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(this);
			fPreferenceStore= null;
		}

		remoteCEditor= null;
		fSourceViewer= null;
		fColorManager= null;
		fConfiguration= null;
		fPresentationReconciler= null;
		fHardcodedRanges= null;
	}

	/**
	 * Disable semantic highlighting.
	 */
	protected void disable() {
		if (fRemoteReconciler != null) {
			fRemoteReconciler.uninstall();
			fRemoteReconciler= null;
		}

		if (fPresenter != null) {
			fPresenter.uninstall();
			fPresenter= null;
		}

		if (fSemanticHighlightings != null)
			disposeHighlightings();
	}


	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		handlePropertyChangeEvent(event);
	}

	/**
	 * Handle the given property change event
	 *
	 * @param event The event
	 */
	protected boolean handlePropertyChangeEvent(PropertyChangeEvent event) {		
		if (fPreferenceStore == null)
			return false; // Uninstalled during event notification

		if (fConfiguration != null)
			fConfiguration.handlePropertyChangeEvent(event);

		if (SemanticHighlightings.affectsEnablement(fPreferenceStore, event)) {
			if (isEnabled())
				enable();
			else
				disable();
		}

		if (!isEnabled())
			return false;
		
		boolean refreshNeeded= false;

		for (int i= 0, n= fSemanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting= fSemanticHighlightings[i];

			String colorKey= SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			if (colorKey.equals(event.getProperty())) {
				adaptToTextForegroundChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}

			String boldKey= SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
			if (boldKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.BOLD);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}

			String italicKey= SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
			if (italicKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.ITALIC);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}

			String strikethroughKey= SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting);
			if (strikethroughKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.STRIKETHROUGH);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}

			String underlineKey= SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting);
			if (underlineKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.UNDERLINE);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}

			String enabledKey= SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
			if (enabledKey.equals(event.getProperty())) {
				adaptToEnablementChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded= true;
				continue;
			}
		}
		
		if (refreshNeeded && fRemoteReconciler != null)
			fRemoteReconciler.refresh();
		
		return refreshNeeded;		
	}

	/**
	 * Force refresh of highlighting.
	 */
	public void refresh() {
		if (fRemoteReconciler != null) {
			fRemoteReconciler.refresh();
		}
	}
}