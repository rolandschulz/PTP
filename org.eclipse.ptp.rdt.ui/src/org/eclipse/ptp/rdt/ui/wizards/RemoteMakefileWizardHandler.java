/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.net.URI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.STDWizardHandler;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.core.remotemake.RemoteMakeBuilder;
import org.eclipse.ptp.rdt.core.resources.RemoteMakeNature;
import org.eclipse.swt.widgets.Composite;

/**
 * This class handles what happens during project creation when the 
 * user has selected "Remote Makefile Project".
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class RemoteMakefileWizardHandler extends STDWizardHandler {

	public RemoteMakefileWizardHandler(Composite p, IWizard w) {
		super(p, w);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.STDWizardHandler#createProject(org.eclipse.core.resources.IProject, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void createProject(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("", 100); //$NON-NLS-1$
		
			ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
			ICProjectDescription des = mngr.createProjectDescription(project, false, !onFinish);
			ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
			ManagedProject mProj = new ManagedProject(des);
			info.setManagedProject(mProj);
	
			cfgs = CfgHolder.unique(fConfigPage.getCfgItems(defaults));
			cfgs = CfgHolder.reorder(cfgs);
				
			for (int i=0; i<cfgs.length; i++) {
				String s = (cfgs[i].getToolChain() == null) ? "0" : ((ToolChain)(cfgs[i].getToolChain())).getId();  //$NON-NLS-1$
				Configuration cfg = new Configuration(mProj, (ToolChain)cfgs[i].getToolChain(), ManagedBuildManager.calculateChildId(s, null), cfgs[i].getName());
				IBuilder bld = cfg.getEditableBuilder();
				if (bld != null) {
					if(bld.isInternalBuilder()){
						IConfiguration prefCfg = ManagedBuildManager.getPreferenceConfiguration(false);
						IBuilder prefBuilder = prefCfg.getBuilder();
						cfg.changeBuilder(prefBuilder, ManagedBuildManager.calculateChildId(cfg.getId(), null), prefBuilder.getName());
						bld = cfg.getEditableBuilder();
						bld.setBuildPath(null);
					}
					bld.setManagedBuildOn(false);
				} else {
					System.out.println(UIMessages.getString("StdProjectTypeHandler.3")); //$NON-NLS-1$
				}
				cfg.setArtifactName(removeSpaces(project.getName()));
				CConfigurationData data = cfg.getConfigurationData();
				des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			}
			mngr.setProjectDescription(project, des);
			
			// remove all builders from the project... I really wish there was a less hacky way to do this that wasn't so damn slow
			IProjectDescription projectDescription = project.getDescription();
			projectDescription.setBuildSpec(new ICommand[0]);
			project.setDescription(projectDescription, IProject.FORCE, new NullProgressMonitor());
			RemoteMakeNature.addToBuildSpec(project, RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID, new NullProgressMonitor());
			RemoteMakeNature.addNature(project, new NullProgressMonitor());
			
			// set the build directory by default to be that of the project... the usual workspace macro doesn't work as the workspace resides locally
			// and the project resides remotely
			
			URI projectLocation = project.getLocationURI();
			// assume that the path portion of the URI corresponds to the path on the remote machine
			// this may not work if the remote machine does not use UNIX paths but we have no real way of knowing the path
			// format, so we hope for the best...
			IPath buildPath = Path.fromPortableString(projectLocation.getPath());
			
			IManagedBuildInfo mbsInfo = ManagedBuildManager.getBuildInfo(project);	
			mbsInfo.getDefaultConfiguration().getBuildData().setBuilderCWD(buildPath);
			mbsInfo.setDirty(true);
			ManagedBuildManager.saveBuildInfo(project, true);
			
			doTemplatesPostProcess(project);
			doCustom(project);
			
			//turn off append local environment variables for remote projects
			StorableEnvironment vars = EnvironmentVariableManager.fUserSupplier.getWorkspaceEnvironmentCopy();
			vars.setAppendContributedEnvironment(false);
			vars.setAppendEnvironment(false);
			EnvironmentVariableManager.fUserSupplier.setWorkspaceEnvironment(vars);
		} finally {
			monitor.done();
		}
	}


}
