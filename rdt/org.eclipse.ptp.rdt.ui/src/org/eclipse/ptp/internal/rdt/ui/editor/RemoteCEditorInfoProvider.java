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
package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.CdtActionConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Remote enabled version of the CEditor. If this editor is opened on a file from a remote project then it will use
 * remote versions of certain actions. If this editor is opened on a file from a local project then it defaults back to
 * normal CEditor behavior.
 */
public class RemoteCEditorInfoProvider implements IRemoteCEditorInfoProvider {
	
	private RemoteCEditor editor;
	private IEditorInput input;
	
	/**
	 * Remote Semantic highlighting manager
	 */
	private RemoteSemanticHighlightingManager fRemoteSemanticManager;
	
	private RemoteCFoldingStructureProvider fRemoteFoldingProvider; 
	
	private RemoteInactiveHighlighting fRemoteInactiveCodeHighlighting;
	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#initializeEditor(org.eclipse.ptp.internal.rdt.editor.RemoteCEditor)
	 */
	public void initializeEditor(RemoteCEditor remoteCEditor) {
		editor = remoteCEditor;
	}

	
	protected RemoteCEditor getEditor() {
		return editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#preDoSetInput(org.eclipse.ui.IEditorInput)
	 */
	public void preDoSetInput(IEditorInput input) {
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#postDoSetInput(org.eclipse.ui.IEditorInput)
	 */
	public void postDoSetInput(IEditorInput input) throws CoreException {
		// nothing to do here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#isApplicableEditorInput(org.eclipse.ui.IEditorInput)
	 */
	public int isApplicableEditorInput(IEditorInput input) {
		ICProject cproject = EditorUtility.getCProject(input);
		if (cproject == null)
			return 0;
		IProject project = cproject.getProject();
		if (RemoteNature.hasRemoteNature(project))
			return 1;
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getTitleImage()
	 */
	public Image getTitleImage() {
		// use default - no need to provide a new one
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getDocumentProvider()
	 */
	public IDocumentProvider getDocumentProvider(IDocumentProvider oldProvider) {
		// uses the default document provider from CEditor - no need to provide a new one
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getTitle(org.eclipse.ui.IEditorInput)
	 */
	public String getTitle(IEditorInput input) {
		// use default - no need to provide a new one
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getAlternateInput(org.eclipse.ui.IEditorInput)
	 */
	public IEditorInput getAlternateInput(IEditorInput input) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getTitleTooltip()
	 */
	public String getTitleTooltip() {
		// use default - no need to provide a new one
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getOutlinePage(org.eclipse.ptp.internal.rdt.editor.RemoteCEditor)
	 */
	public CContentOutlinePage getOutlinePage(RemoteCEditor remoteCEditor) {
		if (isRemote()) {
			return new RemoteCContentOutlinePage(remoteCEditor);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPostCreatePartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void doPostCreatePartControl(Composite parent) {
		if (isRemote()) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RDTHelpContextIds.REMOTE_C_CPP_EDITOR);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
		if (IContextProvider.class.equals(required)) {
			return new RemoteCEditorHelpContextProvider(editor);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPrePerformSave(boolean)
	 */
	public boolean doPrePerformSave(boolean overwrite) {
		// nothing to do, just return true
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPostPerformSave()
	 */
	public void doPostPerformSave() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#createActions(org.eclipse.jface.text.source.IVerticalRuler)
	 */
	public void createActions(IVerticalRuler iVerticalRuler) {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		if (isRemote()) {
			// remove text search
			menu.remove("org.eclipse.search.text.ctxmenu"); //$NON-NLS-1$
			// remove refactoring menu for now
			menu.remove("org.eclipse.cdt.ui.refactoring.menu"); //$NON-NLS-1$
			
			//remove some refactor menu items in the Source submenu
			IMenuManager sourceMenu = (IMenuManager) menu.find("org.eclipse.cdt.ui.source.menu"); //$NON-NLS-1$
			if (sourceMenu != null) {
				sourceMenu.remove("AddIncludeOnSelection"); //$NON-NLS-1$
				sourceMenu.remove("org.eclipse.cdt.ui.refactor.getters.and.setters"); //$NON-NLS-1$
				sourceMenu.remove("org.eclipse.cdt.ui.refactor.implement.method"); //$NON-NLS-1$
			}
			
			//remove items that don't work well for remote projects
			menu.remove("OpenMacroExplorer"); //$NON-NLS-1$
			menu.remove("ToggleSourceHeader"); //$NON-NLS-1$
			
			//quick type hierarchy
			menu.remove("OpenHierarchy"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#dispose()
	 */
	public void dispose() {
		uninstallRemoteCodeFolding();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getAnnotationRulerColumnWidth()
	 */
	public int getAnnotationRulerColumnWidth() {
		// default
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#helpRequested(org.eclipse.swt.events.HelpEvent)
	 */
	public void helpRequested(HelpEvent e) {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#createUndoRedoActions()
	 */
	public void createUndoRedoActions() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPreRevertToSaved()
	 */
	public void doPreRevertToSaved() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPostRevertToSaved()
	 */
	public void doPostRevertToSaved() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#getInputCElement()
	 */
	public ICElement getInputCElement() {
		// default
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#reconciled(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean reconciled(IASTTranslationUnit ast, boolean force,
			IProgressMonitor progressMonitor) {
		// nothing to do, just return true
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPrePerformSaveAs(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean doPrePerformSaveAs(IProgressMonitor progressMonitor) {
		// nothing to do, just return true
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#doPostPerformSaveAs()
	 */
	public void doPostPerformSaveAs() {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#validateState(org.eclipse.ui.IEditorInput)
	 */
	public boolean validateState(IEditorInput input) {
		// nothing to do, just return true
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#rulerContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void rulerContextMenuAboutToShow(IMenuManager menu) {
		// nothing to do

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#collectContextMenuPreferencePages()
	 */
	public String[] collectContextMenuPreferencePages() {
		// default
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#createSelectionSearchGroup(org.eclipse.ptp.internal.rdt.editor.RemoteCEditor)
	 */
	public ActionGroup createSelectionSearchGroup(RemoteCEditor remoteCEditor) {
		return new SelectionSearchGroup(remoteCEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.editor.info.IRemoteCEditorInfoProvider#createOpenViewActionGroup(org.eclipse.ptp.internal.rdt.editor.RemoteCEditor)
	 */
	public ActionGroup createOpenViewActionGroup(RemoteCEditor remoteCEditor) {
		return new OpenViewActionGroup(remoteCEditor);
	}
	
	/**
	 * Returns true if the input element is a C element and it comes from a remote project. Also returns true if the
	 * editor is opened on an external translation unit that was navigated to from a remote resource.
	 */
	protected boolean isRemote() {
		ICElement element = editor.getInputCElement();
		if (element == null)
			return false;
		ICProject cProject = element.getCProject();
		if (cProject == null)
			return false;
		IProject project = cProject.getProject();
		return RemoteNature.hasRemoteNature(project);
	}
	
	private boolean isLocalServiceProvider() {
		ICProject cproject = EditorUtility.getCProject(input);
		if(cproject == null) { // external translation unit
			return true;
		}
		IServiceModelManager smm = ServiceModelManager.getInstance();
		
		if(smm.isConfigured(cproject.getProject())) {
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(cproject.getProject());
			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
	
			if (serviceProvider instanceof IIndexServiceProvider) {
				return !((IIndexServiceProvider)serviceProvider).isRemote();
			}
		}
		
		if (RemoteNature.hasRemoteNature(cproject.getProject())) {
			return false;
		}

		return true;
	}

	public SourceViewerConfiguration getSourceViewerConfiguration(IPreferenceStore store, SourceViewerConfiguration sourceViewerConfiguration) {
		if (!isLocalServiceProvider()) {
			// use remote source viewer configuration
			if (!(sourceViewerConfiguration instanceof RemoteCSourceViewerConfiguration)) {
				CTextTools textTools = CUIPlugin.getDefault().getTextTools();
				return new RemoteCSourceViewerConfiguration(textTools.getColorManager(), store, editor,
						ICPartitions.C_PARTITIONING);
			}
		}
		return null;
		
	}

	public void fillActionBars(IActionBars actionBars) {
		if (isRemote()) {
			// remove refactoring for now
			actionBars.setGlobalActionHandler(CdtActionConstants.RENAME, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.EXTRACT_CONSTANT, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.EXTRACT_LOCAL_VARIABLE, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.EXTRACT_METHOD, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.HIDE_METHOD, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.TOGGLE_FUNCTION, null);

			actionBars.setGlobalActionHandler(CdtActionConstants.GETTERS_AND_SETTERS, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.IMPLEMENT_METHOD, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.ADD_INCLUDE, null);
			actionBars.setGlobalActionHandler(CdtActionConstants.SORT_LINES, null);
		}
	}

	public boolean shouldProcessLocalParsingCompletions() {
		return isLocalServiceProvider();
	}

	/**
	 * Install Semantic Highlighting.
	 */
	public void installSemanticHighlighting(ISourceViewer sourceViewer, IPreferenceStore prefStore) {
		if (!isLocalServiceProvider() && fRemoteSemanticManager == null && isSemanticHighlightingEnabled(prefStore)) {
			fRemoteSemanticManager= new RemoteSemanticHighlightingManager();
			fRemoteSemanticManager.install(editor, (CSourceViewer) sourceViewer, CUIPlugin.getDefault().getTextTools().getColorManager(), prefStore);
		} else if (isLocalServiceProvider()) { //airplane mode
			//reset so when workspace is online, semantic highlighting gets triggered
			uninstallSemanticHighlighting();
		}
	}
	
	public void refreshRemoteSemanticManager() {
		if (fRemoteSemanticManager != null)
			fRemoteSemanticManager.refresh();
	}
	
	public boolean isSemanticHighlightingEnabled(IPreferenceStore prefStore) {
		return SemanticHighlightings.isEnabled(prefStore) && !(editor.isEnableScalablilityMode() && prefStore.getBoolean(PreferenceConstants.SCALABILITY_SEMANTIC_HIGHLIGHT));
	}
	
	public void installRemoteCodeFolding(ISourceViewer sourceViewer){
		String id= CUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.EDITOR_FOLDING_PROVIDER);

		// If the folding provider is the default one and we are not 
		// in off-line mode, then uninstall local folding and install remote folding:
		if (id.compareTo("org.eclipse.cdt.ui.text.defaultFoldingProvider") == 0 && !isLocalServiceProvider()) { //$NON-NLS-1$
			editor.uninstallProjectionModelUpdater();
			fRemoteFoldingProvider = new RemoteCFoldingStructureProvider();
			ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
			fRemoteFoldingProvider.install(editor, projectionViewer);
		}
	}
	
	public void uninstallRemoteCodeFolding() {
		if (fRemoteFoldingProvider != null)
			fRemoteFoldingProvider.uninstall();
	}

	public void uninstallSemanticHighlighting() {
		if (fRemoteSemanticManager != null) {
			fRemoteSemanticManager.uninstall();
			fRemoteSemanticManager= null;
		}
	}

	public boolean isInactiveHighlightingEnabled(IPreferenceStore prefStore) {
		return prefStore.getBoolean(CEditor.INACTIVE_CODE_ENABLE) && !editor.isEnableScalablilityMode();
	}

	public void installInactiveHighlighting(IPreferenceStore prefStore, ISharedTextColors colors) {
		if (fRemoteInactiveCodeHighlighting == null && !isLocalServiceProvider()
				&& isInactiveHighlightingEnabled(prefStore)) {
			fRemoteInactiveCodeHighlighting = new RemoteInactiveHighlighting(prefStore, colors);
			fRemoteInactiveCodeHighlighting.install(editor);
		} else if (isLocalServiceProvider()) {
			uninstallInactiveHighlighting();
		}
	}

	public void uninstallInactiveHighlighting() {
		if (fRemoteInactiveCodeHighlighting != null) {
			fRemoteInactiveCodeHighlighting.uninstall();
			fRemoteInactiveCodeHighlighting = null;
		}
	}

	public String getInactiveHighlightColorKey() {
		return RemoteInactiveHighlighting.INACTIVE_CODE_COLOR;
	}

	public void updateInactiveHighlightColor() {
		if (fRemoteInactiveCodeHighlighting != null)
			fRemoteInactiveCodeHighlighting.updateInactiveCodeColor();
	}

}
