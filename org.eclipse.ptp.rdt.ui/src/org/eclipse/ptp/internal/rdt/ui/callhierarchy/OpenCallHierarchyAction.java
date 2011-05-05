/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.OpenCallHierarchyAction
 * Version: 1.10
 */
package org.eclipse.ptp.internal.rdt.ui.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.internal.ui.callhierarchy.CHMessages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.callhierarchy.ICallHierarchyService;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;


public class OpenCallHierarchyAction extends SelectionDispatchAction {
	private ITextEditor fEditor;

	public OpenCallHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(CHMessages.OpenCallHierarchyAction_label);
		setToolTipText(CHMessages.OpenCallHierarchyAction_tooltip);
	}
	
	public OpenCallHierarchyAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor= editor;
		setEnabled(fEditor != null && CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}
	@Override
	public void run(ITextSelection sel) {

		if (fEditor instanceof CEditor) {
			IProject project = ((CEditor) fEditor).getInputCElement().getCProject().getProject();
			IServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

			if (serviceProvider instanceof IIndexServiceProvider) {
				ICallHierarchyService chService = ((IIndexServiceProvider) serviceProvider).getCallHierarchyService();
				CallHierarchyUtil.open(chService, fEditor, sel);
			}

		}
	}
	@Override
	public void run(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			Object selectedObject= selection.getFirstElement();
			ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
			if (elem != null) {
				IProject project = elem.getCProject().getProject();
				IServiceModelManager smm = ServiceModelManager.getInstance();
				IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

				IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

				IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

				if (serviceProvider instanceof IIndexServiceProvider) {
					ICallHierarchyService chService = ((IIndexServiceProvider) serviceProvider).getCallHierarchyService();
					CallHierarchyUtil.open(chService, getSite().getWorkbenchWindow(), elem);
				}
			}
		}
	}
	@Override
	public void selectionChanged(ITextSelection sel) {
	}
	@Override		
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			setEnabled(false);
			return;
		}
		
		Object selectedObject= selection.getFirstElement();
		ICElement elem= (ICElement) getAdapter(selectedObject, ICElement.class);
		if (elem != null) {
			setEnabled(isValidElement(elem));
		}
		else {
			setEnabled(false);
		}
	}

	private boolean isValidElement(ICElement elem) {
		if (elem instanceof IFunctionDeclaration) {
			return true;
		}
		if (elem instanceof IVariableDeclaration) {
			return !(elem instanceof IEnumeration);
		}
		if (elem instanceof IEnumerator) {
			return true;
		}
		return false;
	}
	@SuppressWarnings("rawtypes")
	private Object getAdapter(Object object, Class desiredClass) {
		if (desiredClass.isInstance(object)) {
			return object;
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable= (IAdaptable) object;
			return adaptable.getAdapter(desiredClass);
		}
		return null;
	}
}
