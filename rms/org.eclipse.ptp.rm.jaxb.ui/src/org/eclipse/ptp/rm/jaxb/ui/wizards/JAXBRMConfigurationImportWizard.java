/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
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
public class JAXBRMConfigurationImportWizard extends Wizard implements IImportWizard, IJAXBUINonNLSConstants {

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
		new UIJob(SP) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				URL selection = mainPage.getSelectedConfiguration();
				File newConfig = null;
				if (selection != null) {
					String name = mainPage.getSelectedName();
					File dir = resourceManagersDir();
					if (dir == null) {
						return Status.OK_STATUS;
					}

					newConfig = new File(dir, name + DOT_XML);
					BufferedReader br = null;
					FileWriter fw = null;
					try {
						fw = new FileWriter(newConfig, false);
						br = new BufferedReader(new InputStreamReader(selection.openStream()));
						while (true) {
							try {
								String line = br.readLine();
								if (null == line) {
									break;
								}
								fw.write(line);
								fw.write(LINE_SEP);
							} catch (EOFException eof) {
								break;
							} finally {
								fw.flush();
							}
						}
					} catch (IOException io) {
						JAXBUIPlugin.log(io);
					} finally {
						try {
							if (fw != null) {
								fw.close();
							}
							if (br != null) {
								br.close();
							}
						} catch (IOException io) {
						}
					}
				}
				if (newConfig != null) {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(RESOURCE_MANAGERS);
					try {
						project.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
					} catch (CoreException t) {
						JAXBUIPlugin.log(t);
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
		return true;
	}

	/*
	 * By convention, "resourceManagers" project in the user's workspace.
	 */
	private static File resourceManagersDir() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(RESOURCE_MANAGERS);
		if (project != null) {
			IPath path = project.getLocation();
			if (path != null) {
				File dir = path.toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
				return dir;
			}
		}
		WidgetActionUtils.warningMessage(Display.getDefault().getActiveShell(), Messages.ResourceManagersNotExist,
				Messages.ResourceManagersNotExist_title);
		return null;
	}
}
