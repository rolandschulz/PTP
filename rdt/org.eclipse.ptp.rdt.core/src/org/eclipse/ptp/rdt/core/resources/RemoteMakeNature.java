/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.resources;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.remotemake.RemoteMakeBuilder;

/**
 * Project nature for remote standard make projects.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 */
public class RemoteMakeNature implements IProjectNature {

	public static final String NATURE_ID = "org.eclipse.ptp.rdt.core.remoteMakeNature"; //$NON-NLS-1$
	private IProject fProject;

	/**
	 * Adds this nature to the given project.  Since this requires modifying the project
	 * description, this may be a long running operation.
	 * 
	 * @param project
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures= description.getNatureIds();
		for (int i= 0; i < prevNatures.length; i++) {
			if (NATURE_ID.equals(prevNatures[i]))
				return;
		}
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Gets the build spec for a given builder from the project description.
	 * 
	 * @param description
	 * @param builderID
	 * @return ICommand
	 */
	public static ICommand getBuildSpec(IProjectDescription description, String builderID) {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				return commands[i];
			}
		}
		return null;
	}

	/**
	 * Sets the build spec on the given project description.
	 * 
	 * @param description
	 * @param newCommand
	 * @return IProjectDescription
	 */
	public static IProjectDescription setBuildSpec(IProjectDescription description, ICommand newCommand) {

		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldCommand = getBuildSpec(description, newCommand.getBuilderName());
		ICommand[] newCommands;

		if (oldCommand == null) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
			newCommands[0] = newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i].getBuilderName().equals(oldCommand.getBuilderName())) {
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		return description;
	}	

	/**
	 * Adds a builder to the build spec for the project.
	 * 
	 * @param project
	 * @param builderID
	 * @param mon
	 * @throws CoreException
	 */
	public static void addToBuildSpec(IProject project, String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		boolean found = false;
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			ICommand[] newCommands = new ICommand[commands.length + 1];
			// Add it before other builders. See 1FWJK7I: ITPJCORE:WIN2000
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			description.setBuildSpec(newCommands);
			project.setDescription(description, mon);
		}
	}

	/**
	 * Removes a builder from the project's build spec.
	 * 
	 * @param project
	 * @param builderID
	 * @param mon
	 * @throws CoreException
	 */
	public static void removeFromBuildSpec(IProject project, String builderID, IProgressMonitor mon) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				break;
			}
		}
		project.setDescription(description, mon);
	}

	/**
	 * Adds the remote make builder to the project's build spec.
	 * 
	 * @throws CoreException
	 */
	public void addBuildSpec() throws CoreException {
		addToBuildSpec(getProject(), RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID, null);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		addBuildSpec();
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(MakeCorePlugin.getDefault().getPluginPreferences(), RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID, false);
		IMakeBuilderInfo projectInfo = MakeCorePlugin.createBuildInfo(getProject(), RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID);
		projectInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, "")); //$NON-NLS-1$
		projectInfo.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, info.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "make")); //$NON-NLS-1$

		projectInfo.setUseDefaultBuildCmd(info.isDefaultBuildCmd());
		projectInfo.setStopOnError(info.isStopOnError());

		projectInfo.setAutoBuildEnable(info.isAutoBuildEnable());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, "")); //$NON-NLS-1$
		
		projectInfo.setIncrementalBuildEnable(info.isIncrementalBuildEnabled());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, "")); //$NON-NLS-1$

		projectInfo.setFullBuildEnable(info.isIncrementalBuildEnabled());

		projectInfo.setCleanBuildEnable(info.isCleanBuildEnabled());
		projectInfo.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, info.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, "")); //$NON-NLS-1$

		projectInfo.setErrorParsers(info.getErrorParsers());
		projectInfo.setAppendEnvironment(info.appendEnvironment());
		projectInfo.setEnvironment(info.getEnvironment());
	}

	/**
	 * Removes the remote make builder from the project's build spec.
	 * 
	 * @throws CoreException
	 */
	public void removeBuildSpec() throws CoreException {
		removeFromBuildSpec(getProject(), RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		removeBuildSpec();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		fProject = project;
	}
	
	/**
	 * Returns true if the given project has the remote make nature.
	 * @throws NullPointerException if project is null
	 */
	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			RDTLog.logError(e);
			return false;
		}
	}
	
}
