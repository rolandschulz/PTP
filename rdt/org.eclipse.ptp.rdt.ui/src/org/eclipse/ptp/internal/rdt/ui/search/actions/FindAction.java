/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.FindAction
 * Version: 1.38
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.ui.search.ISearchService;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;

public abstract class FindAction extends SelectionParseAction {
	public FindAction(CEditor editor){
		super( editor );
	}
	
	public FindAction(IWorkbenchSite site){
		super( site );
	}
	
	@Override
	public void run() {
		ISearchQuery searchJob = null;

		ISelection selection = getSelection();
	 	if (selection instanceof IStructuredSelection) {
	 		Object object = ((IStructuredSelection)selection).getFirstElement();
	 		if (object instanceof ISourceReference)
	 			searchJob = createQuery((ISourceReference) object);
		} else if (selection instanceof ITextSelection) {
			ITextSelection selNode = (ITextSelection)selection;
			ICElement element = fEditor.getInputCElement();
			while (element != null && !(element instanceof ITranslationUnit))
				element = element.getParent();
			if (element != null) {
				searchJob = createQuery(element, selNode);
			}
		} 

	 	if (searchJob == null) {
	 		showStatusLineMessage(CSearchMessages.CSearchOperation_operationUnavailable_message);
	 		return;
	 	}

        clearStatusLine();
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(searchJob);
	}

	protected ISearchQuery createQuery(ISourceReference object) {
		IProject project = object.getTranslationUnit().getCProject().getProject();
		ISearchService service = getSearchService(project);
		return service.createSearchElementQuery(Scope.WORKSPACE_ROOT_SCOPE, getScope(), object, getLimitTo());
	}

	private ISearchService getSearchService(IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		if (!(serviceProvider instanceof IIndexServiceProvider2)) {
			return null;
		}
		ISearchService service = ((IIndexServiceProvider2) serviceProvider).getSearchService();
		return service;
	}

	protected ISearchQuery createQuery(ICElement element, ITextSelection selNode) {
		IProject project = element.getCProject().getProject();
		ISearchService service = getSearchService(project);
		return service.createSearchTextSelectionQuery(Scope.WORKSPACE_ROOT_SCOPE, getScope(),
				(ITranslationUnit)element, selNode, getLimitTo());
	}
	
    abstract protected String getScopeDescription(); 

	abstract protected ICElement[] getScope();
	
	abstract protected int getLimitTo();
    
}
