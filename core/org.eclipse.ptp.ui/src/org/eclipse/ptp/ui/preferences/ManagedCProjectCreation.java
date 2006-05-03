/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.preferences;

import java.util.Arrays;
import java.util.List;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.ui.PTPUIPlugin;

/**
 * @author Clement chu
 *
 */
public class ManagedCProjectCreation extends SimulationProjectCreation {
	/** Constructor
	 * @param projectName
	 * @param fileName
	 */
	public ManagedCProjectCreation(String projectName, String fileName) {
		super(projectName, fileName);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.SimulationProjectCreation#getFileExtension()
	 */
	public String getFileExtension() {
		return ".c";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.SimulationProjectCreation#getTemplateFile()
	 */
	protected String getTemplateFile() {
		return "ctemplate.txt";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.SimulationProjectCreation#getEditorID()
	 */
	protected String getEditorID() {
		//hardcode the C editor
		return "org.eclipse.cdt.ui.editor.CEditor";
	}
	
	/** Get project ID
	 * @return
	 */
	private String getProjectID() {
		return ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID;		
	}
	/** Add nature to project
	 * @param newProject
	 * @param monitor
	 * @throws CoreException
	 */
	private void addNature(IProject newProject, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("", 2);
		monitor.subTask("Adding nature...");
		ManagedCProjectNature.addManagedNature(newProject, new SubProgressMonitor(monitor, 1));
		monitor.subTask("Adding nature...");
		ManagedCProjectNature.addManagedBuilder(newProject, new SubProgressMonitor(monitor, 1));
		monitor.done();
	}
	/** Get default project type
	 * @return
	 */
	private IProjectType getDefaultProjectType() {
		IProjectType[] types = ManagedBuildManager.getDefinedProjectTypes();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();
		for (int index = 0; index < types.length; ++index) {
			IProjectType type = types[index];
			if (!type.isAbstract() && !type.isTestProjectType()) {
				if (type.isSupported()) {
					IConfiguration[] configs = type.getConfigurations();
					for (int j = 0; j < configs.length; ++j) {
						IToolChain tc = configs[j].getToolChain();
						List osList = Arrays.asList(tc.getOSList());
						if (osList.contains("all") || osList.contains(os)) {
							List archList = Arrays.asList(tc.getArchList());
							if (archList.contains("all") || archList.contains(arch))
								return type;
						}
					}
				}
			}
		}
		return null;
	}
	/** Get default configuration
	 * @param type
	 * @return
	 */
	private IConfiguration[] getDefaultConfigurations(IProjectType type) {
		if (type != null)
			return type.getConfigurations();
		return new IConfiguration[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.SimulationProjectCreation#createProject(org.eclipse.core.resources.IProjectDescription, org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void createProject(IProjectDescription description, IProject newProject, IProgressMonitor monitor) throws CoreException {
		CCorePlugin.getDefault().createCProject(description, newProject, monitor, getProjectID());
		//add project nature
		addNature(newProject, new SubProgressMonitor(monitor, 2));

		// Add the ManagedProject to the project
		IManagedProject newManagedProject = null;
		IManagedBuildInfo info = null;
		try {
			info = ManagedBuildManager.createBuildInfo(newProject);
			IProjectType parent = getDefaultProjectType();
			newManagedProject = ManagedBuildManager.createManagedProject(newProject, parent);
			if (newManagedProject != null) {
				IConfiguration[] selectedConfigs = getDefaultConfigurations(parent);
				for (int i = 0; i < selectedConfigs.length; i++) {
					IConfiguration config = selectedConfigs[i];
					int id = ManagedBuildManager.getRandomNumber();
					IConfiguration newConfig = newManagedProject.createConfiguration(config, config.getId() + "." + id);
					newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
				}
				// Now add the first supported config in the list as the default
				IConfiguration defaultCfg = null;
				IConfiguration[] newConfigs = newManagedProject.getConfigurations();
				for(int i = 0; i < newConfigs.length; i++) {
					if(newConfigs[i].isSupported()){
						defaultCfg = newConfigs[i];
						break;
					}
				}
				
				if(defaultCfg == null && newConfigs.length > 0)
					defaultCfg = newConfigs[0];
				
				if(defaultCfg != null) {
					ManagedBuildManager.setDefaultConfiguration(newProject, defaultCfg);
					ManagedBuildManager.setSelectedConfiguration(newProject, defaultCfg);
				}
				ManagedBuildManager.setNewProjectVersion(newProject);
				ICDescriptor desc = null;
				try {
					desc = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
					desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
				} catch (CoreException e) {
					PTPUIPlugin.log(e);
				}
			}
		} catch (BuildException e) {
			PTPUIPlugin.log(e);
		}

		// Modify the project settings
		if (newProject != null) {
			setIndexer(newProject, new SubProgressMonitor(monitor, 2));
		}
		
		// Save the build options
		monitor.subTask("Saving project...");
		if (info != null) {
			info.setValid(true);
			ManagedBuildManager.saveBuildInfo(newProject, false);
		}
		
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
		if (initResult.getCode() != IStatus.OK)
			PTPUIPlugin.log(initResult);

		monitor.done();
	}
	
    /** Set project indexer 
     * @param newProject
     * @param monitor
     * @throws CoreException
     */
    public void setIndexer(IProject newProject, IProgressMonitor monitor) throws CoreException {
		final String indexerID = CCorePlugin.DEFAULT_INDEXER_UNIQ_ID;
    	if (monitor == null)
			monitor = new NullProgressMonitor();

    	monitor.beginTask("Setting indexer", 2);
		if (indexerID != null) {
			if (newProject != null) {
				ICDescriptorOperation op = new ICDescriptorOperation() {
					public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
						descriptor.remove(CCorePlugin.INDEXER_UNIQ_ID);
						descriptor.create(CCorePlugin.INDEXER_UNIQ_ID, indexerID);
						monitor.worked(1);
					}
				};
 				CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(newProject, op, monitor);
				//Only send out an index changed notification if the indexer has actually changed
 				//FIXME temp for 3.1 cdt
				//CCorePlugin.getDefault().getCoreModel().getIndexManager().indexerChangeNotification(newProject);
			}
		}
		monitor.done();
    }
}
