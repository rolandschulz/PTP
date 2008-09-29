/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal.wizards;

import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ptp.cell.examples.ui.debug.Debug;
import org.eclipse.ptp.cell.examples.ui.internal.ExampleMessages;
import org.eclipse.ptp.cell.examples.ui.internal.ExampleProjectWizardRegistry;
import org.eclipse.ptp.cell.examples.ui.internal.ExampleUIPlugin;
import org.eclipse.ptp.cell.examples.ui.internal.ProjectWizardDefinition;
import org.eclipse.ptp.cell.examples.ui.internal.wizards.ExampleProjectCreationOperation;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;


/**
 * @author laggarcia
 * @since 1.1.1
 * 
 */
public class ExampleProjectCreationWizard extends Wizard implements INewWizard,
		IExecutableExtension {

	protected final String WIZARD_ID = "id"; //$NON-NLS-1$

	protected final String WIZARD_NAME = "name"; //$NON-NLS-1$

	private final String WEB_BROWSER_ID = "org.eclipse.ui.browser.editor"; //$NON-NLS-1$

	protected IConfigurationElement wizardConfigElement;

	protected List projectWizardDefinitionList;

	public final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * 
	 */
	public ExampleProjectCreationWizard() {
		super();
		setDialogSettings(ExampleUIPlugin.getDefault().getDialogSettings());
	}

	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		Iterator i = projectWizardDefinitionList.listIterator();
		while (i.hasNext()) {
			addPage(new ExampleProjectCreationWizardPage(
					(ProjectWizardDefinition) i.next()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		ExampleProjectCreationOperation runnable = new ExampleProjectCreationOperation(
				this);
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);

		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException e) {
			MessageDialog.openError(getShell(),
					ExampleMessages.operationErrorTitle, e.getMessage());
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		BasicNewProjectResourceWizard.updatePerspective(wizardConfigElement);
		IResource fileToOpen = runnable.getFileToOpen();
		if (fileToOpen != null) {
			openFile(fileToOpen);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(wizardConfigElement.getAttribute(WIZARD_NAME));
		setNeedsProgressMonitor(true);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		wizardConfigElement = config;
		projectWizardDefinitionList = ExampleProjectWizardRegistry
				.getExampleProjectRegistry().getProjectWizardDefinitionList(
						wizardConfigElement.getAttribute(WIZARD_ID));
	}

	private void openFile(final IResource file) {
		if (file.getType() != IResource.FILE) {
			return;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		final IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						IDE.openEditor(activePage, (IFile) file,
								WEB_BROWSER_ID, true);
					} catch (Exception e) {
						Debug.POLICY.logError(e);
					}
				}
			});
			BasicNewResourceWizard.selectAndReveal(file, activePage
					.getWorkbenchWindow());
		}
	}

}
