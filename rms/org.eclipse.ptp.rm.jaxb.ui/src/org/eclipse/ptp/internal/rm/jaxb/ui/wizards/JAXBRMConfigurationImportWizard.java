/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.ui.wizards;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.internal.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.UIJob;

/**
 * For importing XML configurations from the installed plugins to the workspace
 * for possible editing and export.
 * 
 * @author arossi
 * 
 */
public class JAXBRMConfigurationImportWizard extends Wizard implements IImportWizard {

	private JAXBRMConfigurationImportWizardPage mainPage;

	public JAXBRMConfigurationImportWizard() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

	/*
	 * Loads available configurations.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.ConfigurationImportWizardTitle);
		setNeedsProgressMonitor(true);
		mainPage = new JAXBRMConfigurationImportWizardPage(Messages.ConfigurationImportWizardPageTitle);
		mainPage.loadConfigurations();
	}

	/*
	 * Copies file to resourceManagers project and refreshes it. (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		new UIJob(JAXBUIConstants.TARGET_CONFIGURATIONS) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				SubMonitor subMon = SubMonitor.convert(monitor, 20);
				try {
					URL selection = mainPage.getSelectedConfiguration();
					if (selection != null) {
						try {
							String name = mainPage.getSelectedName();
							IProject project = checkResourceManagersProject(subMon.newChild(10));
							if (project == null) {
								return Status.OK_STATUS;
							}

							int createTry = 1;
							IFile newConfig = project.getFile(name + JAXBUIConstants.SP + JAXBUIConstants.OPENP + createTry++
									+ JAXBUIConstants.CLOSP + JAXBUIConstants.DOT_XML);
							while (newConfig.exists()) {
								newConfig = project.getFile(name + JAXBUIConstants.SP + JAXBUIConstants.OPENP + createTry++
										+ JAXBUIConstants.CLOSP + JAXBUIConstants.DOT_XML);
							}
							newConfig.create(selection.openStream(), IResource.NONE, subMon.newChild(10));
						} catch (CoreException io) {
							JAXBUIPlugin.log(io);
						} catch (IOException io) {
							JAXBUIPlugin.log(io);
						}
					}
					return Status.OK_STATUS;
				} finally {
					if (monitor != null) {
						monitor.done();
					}
				}
			}
		}.schedule();
		return true;
	}

	/*
	 * By convention, "resourceManagers" project in the user's workspace.
	 */
	private static IProject checkResourceManagersProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 20);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(JAXBUIConstants.TARGET_CONFIGURATIONS);
		if (!project.exists()) {
			boolean create = MessageDialog
					.openQuestion(Display.getDefault().getActiveShell(), Messages.ResourceManagersNotExist_title,
							Messages.JAXBRMConfigurationImportWizard_createResourceManagersProject);
			if (!create) {
				return null;
			}
			project.create(subMon.newChild(10));
			if (!project.isOpen()) {
				project.open(subMon.newChild(10));
			}
		}
		return project;
	}
}
