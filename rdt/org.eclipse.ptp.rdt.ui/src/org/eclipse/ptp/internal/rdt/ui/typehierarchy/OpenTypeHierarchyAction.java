/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.OpenTypeHierarchyAction
 * Version: 1.5
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.typehierarchy.Messages;
import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenTypeHierarchyAction extends SelectionDispatchAction {

	private ITextEditor fEditor;

	public OpenTypeHierarchyAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenTypeHierarchyAction_label);
		setToolTipText(Messages.OpenTypeHierarchyAction_tooltip);
	}
	
	public OpenTypeHierarchyAction(ITextEditor editor) {
		this(editor.getSite());
		fEditor= editor;
		setEnabled(fEditor != null && CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput()) != null);
	}
	
	@Override
	public void run(ITextSelection sel) {
		IProject project = ((CEditor) fEditor).getInputCElement().getCProject().getProject();
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

		if (serviceProvider instanceof IIndexServiceProvider) {
			ITypeHierarchyService service = ((IIndexServiceProvider) serviceProvider).getTypeHierarchyService();
			TypeHierarchyUtil.open(service, fEditor, sel);
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
					ITypeHierarchyService service = ((IIndexServiceProvider) serviceProvider).getTypeHierarchyService();
					TypeHierarchyUtil.open(service, elem, getSite().getWorkbenchWindow());
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
			setEnabled(TypeHierarchyUI.isValidInput(elem));
		}
		else {
			setEnabled(false);
		}
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
