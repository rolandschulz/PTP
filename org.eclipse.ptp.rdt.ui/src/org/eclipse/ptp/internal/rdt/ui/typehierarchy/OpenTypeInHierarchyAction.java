/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.OpenTypeInHierarchyAction
 * Version: 1.4
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.browser.opentype.ElementSelectionDialog;
import org.eclipse.cdt.internal.ui.typehierarchy.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenTypeInHierarchyAction implements IWorkbenchWindowActionDelegate {

	private static final int[] VISIBLE_TYPES = { 
		ICElement.C_CLASS, ICElement.C_STRUCT, ICElement.C_TYPEDEF, ICElement.C_ENUMERATION,
		ICElement.C_UNION };

	private IWorkbenchWindow fWorkbenchWindow;

	public OpenTypeInHierarchyAction() {
	}

	public void run(IAction action) {
		ElementSelectionDialog dialog = new ElementSelectionDialog(getShell());
		configureDialog(dialog);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		ITypeInfo info = (ITypeInfo) dialog.getFirstResult();
		if (info == null)
			return;
		
		ICElement[] elements= null;
		ITypeReference location = info.getResolvedReference();
		if (location != null) {
			elements= location.getCElements();
		}
		if (elements == null || elements.length == 0) {
			String title = Messages.OpenTypeInHierarchyAction_errorTitle;
			String message = NLS.bind(Messages.OpenTypeInHierarchyAction_errorNoDefinition, info.getQualifiedTypeName().toString());
			MessageDialog.openError(getShell(), title, message);
		} 
		else {
			IProject project = elements[0].getCProject().getProject();
			IServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

			if (serviceProvider instanceof IIndexServiceProvider) {
				ITypeHierarchyService service = ((IIndexServiceProvider) serviceProvider).getTypeHierarchyService();
				TypeHierarchyUtil.open(service, elements[0], fWorkbenchWindow);
			}
		}
	}
	
	private void configureDialog(ElementSelectionDialog dialog) {
		dialog.setDialogSettings(getClass().getName());
		dialog.setVisibleTypes(VISIBLE_TYPES);
		dialog.setTitle(Messages.OpenTypeInHierarchyAction_title);
		dialog.setUpperListLabel(Messages.OpenTypeInHierarchyAction_upperListLabel);
		dialog.setMessage(Messages.OpenTypeInHierarchyAction_message);

		if (fWorkbenchWindow != null) {
			IWorkbenchPage page= fWorkbenchWindow.getActivePage();
			if (page != null) {
				IWorkbenchPart part= page.getActivePart();
				if (part instanceof ITextEditor) {
					ISelection sel= ((ITextEditor) part).getSelectionProvider().getSelection();
					if (sel instanceof ITextSelection) {
						String txt= ((ITextSelection) sel).getText();
						if (txt.length() > 0 && txt.length() < 80) {
							dialog.setFilter(txt, true);
						}
					}
				}
			}
		}
	}

	private Shell getShell() {
		return fWorkbenchWindow.getShell();
	}

	public void dispose() {
		fWorkbenchWindow= null;
	}
	
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow= window;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
