/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.managedbuilder.core;

import org.eclipse.cldt.managedbuilder.internal.core.ProjectType;
import org.eclipse.core.resources.IResource;

/**
 * This class represents targets for the managed build process.  A target
 * is some type of resource built using a given collection of tools.
 *
 * Note: This class was deprecated in 2.1
 */
public interface ITarget extends IBuildObject {
	public static final String TARGET_ELEMENT_NAME = "target";	//$NON-NLS-1$
	public static final String ARTIFACT_NAME = "artifactName";	//$NON-NLS-1$
	public static final String BINARY_PARSER = "binaryParser";	//$NON-NLS-1$
	public static final String ERROR_PARSERS = "errorParsers";	//$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cleanCommand";	//$NON-NLS-1$
	public static final String DEFAULT_EXTENSION = "defaultExtension";	//$NON-NLS-1$
	public static final String EXTENSION = "extension";	//$NON-NLS-1$
	public static final String IS_ABSTRACT = "isAbstract";	//$NON-NLS-1$
	public static final String IS_TEST = "isTest";	//$NON-NLS-1$
	public static final String MAKE_COMMAND = "makeCommand";	//$NON-NLS-1$
	public static final String MAKE_ARGS = "makeArguments";	//$NON-NLS-1$
	public static final String OS_LIST = "osList";	//$NON-NLS-1$
	public static final String ARCH_LIST = "archList";	//$NON-NLS-1$
	public static final String PARENT = "parent";	//$NON-NLS-1$
	
	/**
	 * Creates a configuration for the target populated with the tools and
	 * options settings from the parent configuration.  As options and tools
	 * change in the parent, unoverridden values are updated in the child
	 * config as well.
	 * 
	 * @param parent The <code>IConfigurationV2</code> to use as a settings template
	 * @param id The unique id the new configuration will have
	 * @return IConfigurationV2
	 */
	public IConfigurationV2 createConfiguration(IConfigurationV2 parent, String id);

	/**
	 * Creates a new configuration for the target.  It is populated with
	 * the tools defined for that target and options set at their defaults.
	 * 
	 * @param id id for this configuration.
	 * @return IConfigurationV2
	 */
	public IConfigurationV2 createConfiguration(String id);
	
	/**
	 * Answers the extension that should be applied to build artifacts created by 
	 * this target.
	 * 
	 * @return String
	 */
	public String getArtifactExtension();	

	/**
	 * Get the name of the final build artifact.
	 * 
	 * @return String
	 */
	public String getArtifactName();
	
	/**
	 * @param isDirty
	 */
	public void setDirty(boolean isDirty);

	/**
	 * Answers the unique ID of the binary parser associated with the target.
	 * 
	 * @return String
	 */
	public String getBinaryParserId();

	/**
	 * Answers the semicolon separated list of unique IDs of the error parsers associated with the target.
	 * 
	 * @return String
	 */
	public String getErrorParserIds();

	/**
	 * Answers the ordered list of unique IDs of the error parsers associated with the target.
	 * 
	 * @return String[]
	 */
	public String[] getErrorParserList();
	
	/**
	 * Answers the OS-specific command to remove files created by the build
	 *  
	 * @return String
	 */
	public String getCleanCommand();

	/**
	 * Returns all of the configurations defined by this target.
	 * 
	 * @return IConfigurationV2[]
	 */
	public IConfigurationV2[] getConfigurations();

	/**
	 * Get the default extension that should be applied to build artifacts
	 * created by this target.
	 * 
	 * @return String
	 * @deprecated
	 */
	public String getDefaultExtension();	

	/**
	 * Answers the command line arguments to pass to the make utility used 
	 * by the receiver to build a project.
	 * 
	 * @return
	 */
	public String getMakeArguments();
	
	/**
	 * Answers the name of the make utility for the target.
	 *  
	 * @return String
	 */
	public String getMakeCommand();

	/**
	 * Returns the configuration with the given id, or <code>null</code> if not found.
	 * 
	 * @param id
	 * @return IConfigurationV2
	 */
	public IConfigurationV2 getConfiguration(String id);
	
	/**
	 * Gets the resource that this target is applied to.
	 * 
	 * @return IResource
	 */
	public IResource getOwner();

	/**
	 * Answers the <code>ITarget</code> that is the parent of the receiver.
	 * 
	 * @return ITarget
	 */
	public ITarget getParent();
	
	/**
	 * Answers an array of operating systems the target can be created on.
	 * 
	 * @return String[]
	 */
	public String[] getTargetOSList();
		 
	 /**
	  * Answers an array of architectures the target can be created on.
	  * 
	  * @return String[]
	  */
	 public String[] getTargetArchList();

	/**
	 * Returns the list of platform specific tools associated with this
	 * platform.
	 * 
	 * @return ITool[]
	 */
	public ITool[] getTools();

	/**
	 * Answers the tool in the receiver with the ID specified in the argument, 
	 * or <code>null</code> 
	 * 
	 * @param id
	 * @return
	 */
	public ITool getTool(String id);
	
	/**
	 * Answers true if the receiver has a make command that differs from its 
	 * parent specification.
	 * 
	 * @return boolean
	 */
	public boolean hasOverridenMakeCommand();
	
	/**
	 * Returns whether this target is abstract.
	 * @return boolean 
	 */
	public boolean isAbstract();

	/**
	 * Answers <code>true</code> the receiver has changes that need to be saved 
	 * in the project file, else <code>false</code>.
	 * 
	 * @return boolean 
	 */
	public boolean isDirty();
	
	/**
	 * Answers <code>true</code> if the receiver is a target that is defined 
	 * for testing purposes only, else <code>false</code>. A test target will 
	 * not be shown in the UI but can still be manipulated programmatically.
	 * 
	 * @return boolean
	 */
	public boolean isTestTarget();

	/**
	 * Answers whether the receiver has been changed and requires the 
	 * project to be rebuilt.
	 * 
	 * @return <code>true</code> if the receiver contains a change 
	 * that needs the project to be rebuilt
	 */
	public boolean needsRebuild();

	/**
	 * Removes the configuration with the ID specified in the argument.
	 * 
	 * @param id 
	 */
	public void removeConfiguration(String id);
	
	/**
	 * Set (override) the extension that should be appended to the build artifact
	 * for the receiver.
	 *  
	 * @param extension
	 */
	public void setArtifactExtension(String extension);

	/**
	 * Set the name of the artifact that will be produced when the receiver
	 * is built.
	 * 
	 * @param name
	 */
	public void setArtifactName(String name);

	/**
	 * Sets the arguments to be passed to the make utility used by the 
	 * receiver to produce a build goal.
	 * 
	 * @param makeArgs
	 */
	public void setMakeArguments(String makeArgs);

	/**
	 * Sets the make command for the receiver to the value in the argument.
	 * 
	 * @param command
	 */
	public void setMakeCommand(String command);

	/**
	 * Sets the semicolon separated list of error parser ids
	 * 
	 * @param ids
	 */
	public void setErrorParserIds(String ids);
	
	/**
	 * Set the rebuild state of the receiver.
	 * 
	 * @param <code>true</code> will force a rebuild the next time the project builds 
	 * @see org.eclipse.cldt.managedbuilder.core.IManagedBuildInfo#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild);

	/**
	 * Sets the resource that owns the receiver.
	 * 
	 * @param resource
	 */
	public void updateOwner(IResource resource);

	/**
	 * Converts a CDT V2.0 target into a ProjectType + Configuration + Toolchain +
	 * Builder + TargetPlatform.
	 *
	 */
	public void convertToProjectType();

	/**
	 * Returns the <code>ProjectType</code> that this Target has been converted to,
	 * or <code>null</code> if it has not been converted. 
	 *
	 * @return ProjectType
	 */
	public ProjectType getCreatedProjectType();
}
