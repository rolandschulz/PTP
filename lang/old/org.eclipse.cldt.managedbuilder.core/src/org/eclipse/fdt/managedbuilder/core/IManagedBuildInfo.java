/**********************************************************************
 * Copyright (c) 2003,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 ***********************************************************************/
package org.eclipse.fdt.managedbuilder.core;

import java.util.List;

import org.eclipse.fdt.managedbuilder.makegen.IManagedDependencyGenerator;

/*
 *  There is a ManagedBuildInfo per FDT managed build project.  Here are
 *  some notes on their usage:
 *   o  You can look up the managed build info associated with a FDT
 *      project by using ManagedBuildManager.getBuildInfo(IProject).
 *   o  Given a ManagedBuildInfo, you can retrieve the associated FDT
 *      managed build system project by using getManagedProject.
 *   o  The usage model of a ManagedBuildInfo is:
 *      1. Call setDefaultConfiguration to set the context
 *      2. Call other methods (e.g. getBuildArtifactName) which get
 *         information from the default configuration, and the other managed
 *         build system model elements that can be reached from the
 *         configuration. 
 */
public interface IManagedBuildInfo {
	public static final String DEFAULT_CONFIGURATION = "defaultConfig";	//$NON-NLS-1$
	public static final String DEFAULT_TARGET = "defaultTarget";	//$NON-NLS-1$

	/*
	 * Note:  "Target" routines are only currently applicable when loading a FDT 2.0
	 *        or earlier managed build project file (.cdtbuild)
	 */
	
	/**
	 * Add a new target to the build information for the receiver
	 * 
	 * @param target
	 */
	public void addTarget(ITarget target);
		
	/**
	 * Answers <code>true</code> if the build system knows how to 
	 * build a file with the extension passed in the argument.
	 *  
	 * @param srcExt
	 * @return
	 */
	public boolean buildsFileType(String srcExt);

	/**
	 * Returns <code>IManagedCommandLineInfo</code> for source with extension
	 * @param sourceExtension - source extension
	 * @param flags - build flags
	 * @param outputFlag - output flag for build tool
	 * @param outputPrefix 
	 * @param outputName
	 * @param inputResources
	 * @return IManagedCommandLineInfo
	 */
	public IManagedCommandLineInfo generateCommandLineInfo( String sourceExtension, String[] flags, 
			String outputFlag, String outputPrefix, String outputName, String[] inputResources );

	/**
	 * Answers a <code>String</code> containing the arguments to be passed to make. 
	 * For example, if the user has selected a build that keeps going on error, the 
	 * answer would contain {"-k"}.
	 * 
	 * @return String
	 */
	public String getBuildArguments();

	
	/**
	 * Answers the file extension for the receivers build goal without a separator.
	 * 
	 * @return the extension or an empty string if none is defined
	 */
	public String getBuildArtifactExtension();
	
	/**
	 * Returns the name of the artifact to build for the receiver.
	 * 
	 * @return
	 */
	public String getBuildArtifactName();

	/**
	 * Answers a <code>String</code> containing the make command invocation 
	 * for the default configuration.
	 */
	public String getBuildCommand();

	/**
	 * Answers the command needed to remove files on the build machine
	 * 
	 * @return
	 */
	public String getCleanCommand();

	/**
	 * Answers the name of the default configuration, for example <code>Debug</code>  
	 * or <code>Release</code>.
	 * 
	 * @return
	 */
	public String getConfigurationName();
	
	/**
	 * Answers a <code>String</code> array containing the names of all the configurations
	 * defined for the project.
	 *  
	 * @return
	 */
	public String[] getConfigurationNames();

	/**
	 * Get the default configuration associated with the receiver
	 * 
	 * @return
	 */
	public IConfiguration getDefaultConfiguration();
	
	/**
	 * @param sourceExtension
	 * @return
	 */
	public IManagedDependencyGenerator getDependencyGenerator(String sourceExtension);

	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool in the configuration
	 * defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForConfiguration(String extension);
	
	/**
	 * Returns a <code>String</code> containing the flags, including 
	 * those overridden by the user, for the tool that handles the 
	 * type of source file defined by the argument.
	 * 
	 * @param extension
	 * @return
	 */
	public String getFlagsForSource(String extension);

	/**
	 * Answers the libraries the project links in.
	 * 
	 * @param extension
	 * @return
	 */
	public String[] getLibsForConfiguration(String extension);

	/**
	 * Returns the ManagedProject associated with this build info
	 * 
	 * @return IManagedProject
	 */
	public IManagedProject getManagedProject( );
	
	/**
	 * Answers the extension that will be built by the current configuration
	 * for the extension passed in the argument or <code>null</code>.
	 * 
	 * @param resourceName
	 * @return
	 */
	public String getOutputExtension(String resourceExtension);
	
	/**
	 * Answers the flag to be passed to the build tool to produce a specific output 
	 * or an empty <code>String</code> if there is no special flag. For example, the
	 * GCC tools use the '-o' flag to produce a named output, for example
	 * 		gcc -c foo.c -o foo.o
	 * 
	 * @param outputExt
	 * @return
	 */
	public String getOutputFlag(String outputExt);
	
	/**
	 * Answers the prefix that should be prepended to the name of the build 
	 * artifact. For example, a library foo, should have the prefix 'lib' and 
	 * the extension '.a', so the final goal would be 'libfoo.a' 
	 * 
	 * @param extension
	 * @return the prefix or an empty string
	 */
	public String getOutputPrefix(String outputExtension);
	
	/**
	 * Returns the currently selected configuration.  This is used while the project
	 * property pages are displayed
	 * 
	 * @return IConfiguration
	 */
	public IConfiguration getSelectedConfiguration();

	/**
	 * Get the target specified in the argument.
	 * 
	 * @param id
	 * @return
	 */
	public ITarget getTarget(String id);

	/**
	 * Get all of the targets associated with the receiver.
	 * 
	 * @return
	 */
	public List getTargets();

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the extension.
	 * 
	 * @param extension the file extension of the build goal
	 * @return a String containing the command line invocation for the tool
	 */
	public String getToolForConfiguration(String extension);

	/**
	 * Returns a <code>String</code> containing the command-line invocation 
	 * for the tool associated with the source extension.
	 * 
	 * @param sourceExtension the file extension of the file to be built
	 * @return a String containing the command line invocation for the tool
	 */
	public String getToolForSource(String sourceExtension);
	
	/**
	 * Answers a <code>String</code> array containing the contents of the 
	 * user objects option, if one is defined for the target.
	 * 
	 * @param extension the file ecxtension of the build target
	 * @return
	 */
	public String[] getUserObjectsForConfiguration(String extension);

	
	/**
	 * Answers the version of the build information in the format 
	 * @return a <code>String</code> containing the build information 
	 * version
	 */
	public String getVersion();
	
	/**
	 * Answers true if the build model has been changed by the user.
	 * 
	 * @return boolean
	 */
	public boolean isDirty();
	
	/**
	 * Answers <code>true</code> if the extension matches one of the special 
	 * file extensions the tools for the configuration consider to be a header file. 
	 * 
	 * @param ext the file extension of the resource
	 * @return boolean
	 */
	public boolean isHeaderFile(String ext);
	
	/**
	 * Gets the read only status of Managed Build Info
	 * 
	 * @return <code>true</code> if Managed Build Info is read only
	 * otherwise returns <code>false</code>
	 */
	public boolean isReadOnly();
	
	/**
	 * Gets the "valid" status of Managed Build Info.  Managed Build Info is invalid
	 * if the loading of, or conversion to, the Managed Build Info failed. 
	 * 
	 * @return <code>true</code> if Managed Build Info is valid,
	 * otherwise returns <code>false</code>
	 */
	public boolean isValid();
	
	/**
	 * Answers whether the receiver has been changed and requires the 
	 * project to be rebuilt. When a project is first created, it is 
	 * assumed that the user will need it to be fully rebuilt. However 
	 * only option and tool command changes will trigger the build 
	 * information for an existing project to require a rebuild.
	 * <p>
	 * Clients can reset the state to force or clear the rebuild status 
	 * using <code>setRebuildState()</code>
	 * @see ManagedBuildInfo#setRebuildState(boolean)
	 * 
	 * @return <code>true</code> if the resource managed by the 
	 * receiver needs to be rebuilt
	 */
	public boolean needsRebuild();

	public void removeTarget(String id);
	
	/**
	 * Set the primary configuration for the receiver.
	 * 
	 * @param configuration The <code>IConfiguration</code> that will be used as the default
	 * for all building.
	 */
	public void setDefaultConfiguration(IConfiguration configuration);
	
	/**
	 * 
	 * @param configuration
	 * @return
	 */
	public boolean setDefaultConfiguration(String configName);
	
	/**
	 * Sets the dirty flag for the build model to the value of the argument.
	 * 
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);
	
	/**
	 * Sets the valid flag for the build model to the value of the argument.
	 * 
	 * @param isValid
	 */
	public void setValid(boolean isValid);

	/**
	 * Sets the ManagedProject associated with this build info
	 * 
	 * @param project
	 */
	public void setManagedProject(IManagedProject project);
	
	/**
	 * sets the read only status of Managed Build Info
	 * 
	 * @param readOnly 
	 */
	public void setReadOnly(boolean readOnly);
	
	/**
	 * Sets the rebuild state in the receiver to the value of the argument. 
	 * This is a potentially expensive option, so setting it to true should 
	 * only be done if a project resource or setting has been modified in a 
	 * way that would invalidate the previous build.  
	 *  
	 * @param <code>true</code> will force a rebuild the next time the project builds
	 */
	public void setRebuildState(boolean rebuild);
	
	/**
	 * Sets the currently selected configuration. This is used while the project 
	 * property pages are displayed
	 * 
	 * @param configuration the user selection
	 */
	public void setSelectedConfiguration(IConfiguration configuration);
}
