/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Remote enabled version of the CEditor.
 * 
 * If this editor is opened on a file from a remote project then it will use
 * remote versions of certain actions.
 * 
 * If this editor is opened on a file from a local project then it defaults back
 * to normal CEditor behavior.
 * 
 * 
 * @author Mike Kucera
 */
public class RemoteCEditor extends CEditor {

	private IEditorInput input;

	/**
	 * Default constructor.
	 */
	public RemoteCEditor() {
		super();
		setEditorContextMenuId("#RemoteCEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#RemoteCEditorRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#RemoteCEditorOutlinerContext"); //$NON-NLS-1$
	}

	/**
	 * Returns true if the input element is a C element and it comes from a
	 * remote project. Also returns true if the editor is opened on an external
	 * translation unit that was navigated to from a remote resource.
	 */
	private boolean isRemote() {
		ICElement element = getInputCElement();
		if (element == null)
			return false;
		ICProject cProject = element.getCProject();
		if (cProject == null)
			return false;
		IProject project = cProject.getProject();
		return RemoteNature.hasRemoteNature(project);
	}

	// use our remote versions of these actions.

	@Override
	protected ActionGroup createSelectionSearchGroup() {
		return isRemote() ? new SelectionSearchGroup(this) : super.createSelectionSearchGroup();
	}

	@Override
	protected ActionGroup createOpenViewActionGroup() {
		return isRemote() ? new OpenViewActionGroup(this) : super.createOpenViewActionGroup();
	}

	/**
	 * This method overrides the CEditor createPartControl method in order to
	 * set the help for the Remote C/C++ editor.
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RDTHelpContextIds.REMOTE_C_CPP_EDITOR);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class required) {
		if (IContextProvider.class.equals(required)) {
			return new RemoteCEditorHelpContextProvider(this);
		}
		return super.getAdapter(required);
	}

	/**
	 * Override so that the remote version of the outline page is used.
	 */
	@Override
	public CContentOutlinePage getOutlinePage() {
		if (isRemote()) {
			if (fOutlinePage == null) {
				fOutlinePage = new RemoteCContentOutlinePage(this);
				fOutlinePage.addSelectionChangedListener(this);
			}
			setOutlinePageInput(fOutlinePage, getEditorInput());
			return fOutlinePage;
		} else {
			return super.getOutlinePage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.internal.ui.editor.CEditor#setPreferenceStore(org.eclipse
	 * .jface.preference.IPreferenceStore)
	 */
	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);

		ICProject cproject = EditorUtility.getCProject(input);

		if (cproject != null) {
			IProject project = cproject.getProject();
			if (RemoteNature.hasRemoteNature(project)) {
				// use remote source viewer configuration
				SourceViewerConfiguration sourceViewerConfiguration = getSourceViewerConfiguration();
				if (!(sourceViewerConfiguration instanceof RemoteCSourceViewerConfiguration)) {
					CTextTools textTools = CUIPlugin.getDefault().getTextTools();
					setSourceViewerConfiguration(new RemoteCSourceViewerConfiguration(textTools.getColorManager(), store, this,
							ICPartitions.C_PARTITIONING));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.internal.ui.editor.CEditor#doSetInput(org.eclipse.ui.
	 * IEditorInput)
	 */
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		// save a copy of the editor input for setPreferenceStore()
		// since it hasn't been stored in the editor yet
		this.input = input;
		super.doSetInput(input);
	}

	@Override
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		// remove text search
		menu.remove("org.eclipse.search.text.ctxmenu"); //$NON-NLS-1$
		// remove refactoring for now
		menu.remove("org.eclipse.cdt.ui.refactoring.menu"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.ui.editor.CEditor#
	 * shouldProcessLocalParsingCompletions()
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public boolean shouldProcessLocalParsingCompletions() {
		ICProject cproject = EditorUtility.getCProject(input);
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

}
