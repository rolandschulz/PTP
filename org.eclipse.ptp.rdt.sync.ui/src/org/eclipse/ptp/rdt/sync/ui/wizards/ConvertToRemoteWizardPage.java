/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTHelpContextIds;
import org.eclipse.ptp.rdt.sync.core.make.RemoteMakeBuilder;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteMakeNature;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.widgets.ServiceProviderConfigurationWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Converts existing CDT projects to RDT projects.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author vkong
 */
public class ConvertToRemoteWizardPage extends ConvertProjectWizardPage {

	/**
	 * @since 2.0
	 */
	protected static final String WZ_TITLE = "WizardProjectConversion.title"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	protected static final String WZ_DESC = "WizardProjectConversion.description"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	protected ServiceProviderConfigurationWidget fServiceModelWidget;
	/**
	 * @since 2.0
	 */
	protected Group remoteServices;

	/**
	 * @since 2.0
	 */
	protected Map<IProject, IServiceConfiguration> projectConfigs = new HashMap<IProject, IServiceConfiguration>();

	/**
	 * Constructor for ConvertToRemoteWizardPage.
	 * 
	 * @param pageName
	 */
	public ConvertToRemoteWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Method getWzTitleResource returns the correct Title Label for this class
	 * overriding the default in the superclass.
	 */
	@Override
	protected String getWzTitleResource() {
		return Messages.WizardProjectConversion_title;
	}

	/**
	 * Method getWzDescriptionResource returns the correct description Label for
	 * this class overriding the default in the superclass.
	 */
	@Override
	protected String getWzDescriptionResource() {
		return Messages.WizardProjectConversion_description;
	}

	/**
	 * Returns true for: - non-hidden projects - non-RDT projects - projects
	 * that does not have remote systems temporary nature - projects that are
	 * located remotely
	 */
	@Override
	public boolean isCandidate(IProject project) {
		boolean a = false;
		boolean b = false;
		boolean c = false;
		boolean d = false;
		a = !project.isHidden();
		try {
			// b = !project.hasNature(RemoteNature.REMOTE_NATURE_ID);
			try {
				ServiceModelManager.getInstance().getConfigurations(project);
			} catch (ProjectNotConfiguredException e) {
				b = true;
			}
			c = !project.hasNature("org.eclipse.rse.ui.remoteSystemsTempNature"); //$NON-NLS-1$

			IFileStore fileStore = EFS.getStore(project.getLocationURI());
			if (fileStore != null) {
				if (!(fileStore instanceof LocalFile)) {
					d = true;
				}
			}

		} catch (CoreException e) {
			RDTSyncUIPlugin.log(e);
		}

		return a && b && c && d;
	}

	/**
	 * Add remote nature and configure remote services for the project
	 */
	@Override
	public void convertProject(IProject project, String bsId, IProgressMonitor monitor) throws CoreException {
		convertProject(project, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#convertProject
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	@Override
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		convertProject(project, monitor);
	}

	/**
	 * @since 2.0
	 */
	protected void convertProject(IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.WizardProjectConversion_monitor_convertingToRemoteProject, 3);
		try {
			RemoteSyncNature.addRemoteNature(project, new SubProgressMonitor(monitor, 1));
			RemoteMakeNature.updateProjectDescription(project, RemoteMakeBuilder.BUILDER_ID, monitor);
			configureServicesForRemoteProject(project);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected void addToMainPage(Composite container) {
		remoteServices = new Group(container, SWT.SHADOW_IN);
		remoteServices.setText(Messages.WizardProjectConversion_servicesTableLabel);
		remoteServices.setLayout(new FillLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 350;
		remoteServices.setLayoutData(data);

		fServiceModelWidget = new ServiceProviderConfigurationWidget(remoteServices, SWT.NONE);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IProject project = (IProject) ((IStructuredSelection) e.getSelection()).getFirstElement();
				if (project != null) {
					changeProject(project);
				}
			}
		});
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				IProject project = (IProject) e.getElement();
				if (e.getChecked() && project != null) {
					changeProject(project);
				}
			}
		});
		Shell shell = getContainer().getShell();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, RDTHelpContextIds.CONVERTING_TO_REMOTE_PROJECT);
	}

	@Override
	public void doRun(IProgressMonitor monitor, String projectID, String bsId) throws CoreException {
		monitor.beginTask(Messages.ConvertToRemoteWizardPage_0, 2);
		fServiceModelWidget.applyChangesToConfiguration();
		super.doRun(new SubProgressMonitor(monitor, 1), projectID, bsId);
		try {
			ServiceModelManager.getInstance().saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncUIPlugin.log(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @since 2.0
	 */
	protected IServiceConfiguration getConfig(IProject project) {
		IServiceConfiguration config = projectConfigs.get(project);
		if (config == null) {
			config = ServiceModelManager.getInstance().newServiceConfiguration(project.getName());
			projectConfigs.put(project, config);
		}
		return config;
	}

	/**
	 * @since 2.0
	 */
	protected void changeProject(IProject project) {
		IServiceConfiguration config = getConfig(project);
		fServiceModelWidget.applyChangesToConfiguration();
		fServiceModelWidget.setServiceConfiguration(config);
		remoteServices.setText(MessageFormat.format(Messages.WizardProjectConversion_servicesTableForProjectLabel,
				new Object[] { project.getName() }));
	}

	/**
	 * @since 2.0
	 */
	protected void configureServicesForRemoteProject(IProject project) {
		ServiceModelManager.getInstance().addConfiguration(project, getConfig(project));
	}

}
