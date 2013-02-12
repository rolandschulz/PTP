/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.editor;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.internal.rdt.editor.actions.PrintAction;
import org.eclipse.ptp.internal.rdt.editor.preferences.PrintPreferencePage;
import org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider;
import org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProviderSaveAsExtension;
import org.eclipse.ptp.rdt.editor.info.RemoteCInfoProviderUtilities;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Remote enabled version of the CEditor.
 * @author Mike Kucera
 */
public class RemoteCEditor extends CEditor implements HelpListener {

	private IEditorInput input;
	private List<IRemoteCEditorInfoProvider> infoProviders;
	private IRemoteCEditorInfoProvider provider;
	ISourceViewer viewer;
	/**
	 * Default constructor.
	 */
	public RemoteCEditor() {
		super();
		setEditorContextMenuId("#RemoteCEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#RemoteCEditorRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#RemoteCEditorOutlinerContext"); //$NON-NLS-1$
	}

	@Override
	protected ActionGroup createSelectionSearchGroup() {
		if (provider != null) {
			ActionGroup selectionSearchGroup = provider.createSelectionSearchGroup(this);
			
			if (selectionSearchGroup != null)
				return selectionSearchGroup;			
		}
		return super.createSelectionSearchGroup();
	}

	@Override
	protected ActionGroup createOpenViewActionGroup() {		
		if (provider != null) {
			ActionGroup openViewActionGroup = provider.createOpenViewActionGroup(this);
			
			if (openViewActionGroup != null)
				return openViewActionGroup;
			
		}
		return super.createOpenViewActionGroup();
	}

	/**
	 * This method overrides the CEditor createPartControl method in order to set the help for the Remote C/C++ editor.
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		if (provider != null){
			if (isSemanticHighlightingEnabled())
				provider.installSemanticHighlighting(getSourceViewer(), getPreferenceStore());
			provider.doPostCreatePartControl(parent);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required) {
		if (ISourceViewer.class.equals(required))
			return getSourceViewer();
		else if (provider != null) {
			Object result = provider.getAdapter(required);
			if (result != null)
				return result;
		}
		return super.getAdapter(required);
	}

	/**
	 * Override so that the remote version of the outline page is used.
	 */
	@Override
	public CContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			if (provider != null) {
				CContentOutlinePage page = provider.getOutlinePage(this);
				if (page != null) {
					fOutlinePage = page;
					fOutlinePage.addSelectionChangedListener(this);
					setOutlinePageInput(fOutlinePage, getEditorInput());
					return fOutlinePage;
				}
			}		
		}
		
		return super.getOutlinePage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.CEditor#setPreferenceStore(org.eclipse
	 * .jface.preference.IPreferenceStore)
	 */
	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		
		if (provider != null) {
			SourceViewerConfiguration newSourceViewerConfiguration = provider.getSourceViewerConfiguration(store, getSourceViewerConfiguration());
			if (newSourceViewerConfiguration != null)
				setSourceViewerConfiguration(newSourceViewerConfiguration);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.CEditor#doSetInput(org.eclipse.ui. IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		// save a copy of the editor input for setPreferenceStore()
		// since it hasn't been stored in the editor yet
		this.input = input;

		// figure out if there are any applicable information providers
		provider = RemoteCInfoProviderUtilities.getApplicableEditorInfoProvider(infoProviders, input);

		// tell the info provider that the input is going to be set
		if (provider != null) {
			IDocumentProvider oldProvider = getDocumentProvider();
			IDocumentProvider p = provider.getDocumentProvider(oldProvider);
			if (p != null) {
				//disconnect any old input if necessary
				IEditorInput oldInput = getEditorInput();
				if (oldInput!=null && oldProvider!=null)
					oldProvider.disconnect(oldInput);
				//set the new document provider
				setDocumentProvider(p);
			}
			// check if the new editor info provider has an alternate input
			IEditorInput alternateInput = provider.getAlternateInput(input);
			if (alternateInput != null)
				input = alternateInput;
			provider.preDoSetInput(input);
		}

		super.doSetInput(input);

		// tell all the info providers that the input has been set
		if (provider != null) {
			Image img = provider.getTitleImage();
			if (img != null)
				setTitleImage(img);
			String title = provider.getTitle(input);
			if (title != null)
				setPartName(input.getName());
			provider.postDoSetInput(input);
		}

	}

	@Override
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);

		if (provider != null)
			provider.editorContextMenuAboutToShow(menu);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		
		if (provider != null)
			provider.fillActionBars(actionBars);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.CEditor# shouldProcessLocalParsingCompletions()
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public boolean shouldProcessLocalParsingCompletions() {
		if (provider != null)
			return provider.shouldProcessLocalParsingCompletions();
		
		return super.shouldProcessLocalParsingCompletions();
	}

	@Override
	protected boolean isSemanticHighlightingEnabled() {
		if (provider != null) {
			return provider.isSemanticHighlightingEnabled(getPreferenceStore());
		}		
		return super.isSemanticHighlightingEnabled();	
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();

		// VVV: tell all the info providers that the editor is being initialized
		infoProviders = RemoteCInfoProviderUtilities.getEditorInfoProviders();
		for (IRemoteCEditorInfoProvider provider : infoProviders)
			provider.initializeEditor(this);
	}

	public void displayMessage(String message) {
		setStatusLineMessage(message);
	}

	@Override
	public String getTitleToolTip() {
		if (provider != null) {
			String titleTooltip = provider.getTitleTooltip();
			if (titleTooltip != null)
				return titleTooltip;
		}
		return super.getTitleToolTip();
	}

	@Override
	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
		if (provider != null) {
			if (!provider.doPrePerformSave(overwrite))
				return;
		}
		if (provider != null && provider instanceof IRemoteCEditorInfoProviderSaveAsExtension 
				&& ((IRemoteCEditorInfoProviderSaveAsExtension)provider).forceSaveToSaveAs(input)) {
			performSaveAs(progressMonitor);
		} else {
			super.performSave(overwrite, progressMonitor);
		}

		if (provider != null)
			provider.doPostPerformSave();
	}

	@Override
	protected void createActions() {
		super.createActions();
		if (provider != null)
			provider.createActions(getVerticalRuler());
		IAction print = getAction(ITextEditorActionConstants.PRINT);
		setAction(ITextEditorActionConstants.PRINT, new PrintAction(print, this));
	}

	@Override
	public void dispose() {
		if (provider != null)
			provider.dispose();
		
		super.dispose();
	}

	@Override
	protected IVerticalRulerColumn createAnnotationRulerColumn(CompositeRuler ruler) {
		if (provider != null && provider.getAnnotationRulerColumnWidth() != 0)
			return new AnnotationRulerColumn(provider.getAnnotationRulerColumnWidth(), getAnnotationAccess());
		return super.createAnnotationRulerColumn(ruler);
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		viewer = super.createSourceViewer(parent, ruler, styles);
		// add ability to provide context help
		viewer.getTextWidget().addHelpListener(this);
		
		if(provider != null && isFoldingEnabled())  {
			provider.installRemoteCodeFolding(viewer);  
		}
		
		return viewer;
	}

	@Override
	public void helpRequested(HelpEvent e) {
		if (provider != null)
			provider.helpRequested(e);
	}

	@Override
	protected void createUndoRedoActions() {
		super.createUndoRedoActions();
		if (provider != null)
			provider.createUndoRedoActions();
	}

	@Override
	public void doRevertToSaved() {
		if (provider != null)
			provider.doPreRevertToSaved();
		super.doRevertToSaved();
		if (provider != null)
			provider.doPostRevertToSaved();

	}

	@Override
	public void aboutToBeReconciled() {
		if (provider != null)
			provider.aboutToBeReconciled();
		super.aboutToBeReconciled();
	}

	@Override
	public IWorkingCopy getInputCElement() {
		if (provider != null) {
			ICElement result = provider.getInputCElement();
			if (result != null)
				return (IWorkingCopy) result;
		}
		return super.getInputCElement();
	}

	@Override
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		boolean doReconcile = true;
		if (provider != null)
			doReconcile = provider.reconciled(ast, force, progressMonitor);
		if (doReconcile)
			super.reconciled(ast, force, progressMonitor);
	}

	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		boolean doSaveAs = true;
		if (provider != null)
			doSaveAs = provider.doPrePerformSaveAs(progressMonitor);

		if (doSaveAs)
			super.performSaveAs(progressMonitor);

		if (provider != null)
			provider.doPostPerformSaveAs();
	}

	@Override
	protected void validateState(IEditorInput input) {
		boolean doValidation = true;
		if (provider != null)
			doValidation = provider.validateState(input);
		if (doValidation)
			super.validateState(input);
	}
	
	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		if (provider!=null)
			provider.rulerContextMenuAboutToShow(menu);
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		String[] result = super.collectContextMenuPreferencePages();
		if (provider!=null) {
			String[] additional = provider.collectContextMenuPreferencePages();
			if (additional!=null && additional.length>0) {
				String[] newResult = new String[result.length+additional.length+1];
				System.arraycopy(result, 0, newResult, 0, result.length);
				System.arraycopy(additional, 0, newResult, result.length, additional.length);
				newResult[newResult.length-1] = PrintPreferencePage.PAGE_ID;
				result = newResult;
			}
		}
		return result;
	}
	
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		super.handlePreferenceStoreChanged(event);
		
		if (provider != null && SemanticHighlightings.affectsEnablement(getPreferenceStore(), event )
				|| (isEnableScalablilityMode() && PreferenceConstants.SCALABILITY_SEMANTIC_HIGHLIGHT.equals(event.getProperty()))) {
			if (isSemanticHighlightingEnabled()) {
				provider.installSemanticHighlighting(getSourceViewer(), getPreferenceStore());
				provider.refreshRemoteSemanticManager();
			} else {
				provider.uninstallSemanticHighlighting();
			}
			return;
		}
	}
	
	public void uninstallProjectionModelUpdater() {
		super.uninstallProjectionModelUpdater();
	}
	
	/**
	 * allows the dirty indicator to be reset in the case where a save/upload to a remote host fails.
	 */
	public void updateDirtyIndicator() {
		// this needs to be done on the main thread
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				firePropertyChange(PROP_DIRTY);
			}
		});
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String [] { "org.eclipse.ptp.rdt.editor.RemoteCEditorScope" }); //$NON-NLS-1$
	}
}
