/**********************************************************************
 * Copyright (c) 2004,2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.managedbuilder.makegen.gnu;

import org.eclipse.cldt.managedbuilder.core.BuildException;
import org.eclipse.cldt.managedbuilder.core.IConfiguration;
import org.eclipse.cldt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cldt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cldt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cldt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cldt.managedbuilder.core.ITool;
import org.eclipse.cldt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cldt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @since 2.0
 */
public class DefaultGCCDependencyCalculator implements IManagedDependencyGenerator {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_COMMAND;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/*
		 * For a given input, <path>/<resource_name>.<ext>, return a string containing
		 * 	echo -n $(@:%.<out_ext>=%.d) '<path>/' >> $(@:%.<out_ext>=%.d) && \
		 * 	<tool_command> -P -MM -MG <tool_flags> $< >> $(@:%.<out_ext>=%.d)
		 * 
		 */
		StringBuffer buffer = new StringBuffer();

		// Get what we need to create the dependency generation command
		IConfiguration config = info.getDefaultConfiguration();

		//	We need to check whether we have any resource specific build information.
		IResourceConfiguration resConfig = null;
		if( config != null ) resConfig = config.getResourceConfiguration(resource.getFullPath().toString());

		String inputExtension = resource.getFileExtension();
		String outputExtension = info.getOutputExtension(inputExtension);
		
		// Work out the build-relative path for the output files
		IContainer resourceLocation = resource.getParent();
		String relativePath = new String();
		if (resourceLocation != null) {
			relativePath += resourceLocation.getProjectRelativePath().toString();
		}
		if (relativePath.length() > 0) {
			relativePath +=  IManagedBuilderMakefileGenerator.SEPARATOR;
		}
		
		// Calculate the dependency rule
		// <path>/$(@:%.<out_ext>=%.d)
		String depRule = "$(@:%." + //$NON-NLS-1$
			outputExtension + 
			"=%." + //$NON-NLS-1$
			IManagedBuilderMakefileGenerator.DEP_EXT + 
			")"; //$NON-NLS-1$
		
		// Add the rule that will actually create the right format for the dep 
		buffer.append(IManagedBuilderMakefileGenerator.TAB + 
				IManagedBuilderMakefileGenerator.ECHO + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"-n" + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE +
				depRule + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"$(dir $@)" + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">" + //$NON-NLS-1$ 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				depRule + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.LOGICAL_AND + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.LINEBREAK);
		
		// Add the line that will do the work to calculate dependencies
		IManagedCommandLineInfo cmdLInfo = null;
		String buildCmd = null;
		String[] inputs= new String[1]; inputs[0] = IManagedBuilderMakefileGenerator.IN_MACRO;
		String outflag = "";  //$NON-NLS-1$
		String outputPrefix = "";   //$NON-NLS-1$
		String outputFile = "";   //$NON-NLS-1$
		if( resConfig != null) {
			ITool[] tools = resConfig.getTools(); 
			String cmd = tools[0].getToolCommand();
			String[] toolFlags = null;
			try { 
				toolFlags = tools[0].getCommandFlags();
			} catch( BuildException ex ) {
				// TODO add some routines to catch this
				toolFlags = EMPTY_STRING_ARRAY;
			}
			String[] flags = new String[toolFlags.length + 4];
			flags[0] = "-MM";  //$NON-NLS-1$
			flags[1] = "-MG";  //$NON-NLS-1$
			flags[2] = "-P";   //$NON-NLS-1$
			flags[3] = "-w";   //$NON-NLS-1$
			for (int i=0; i<toolFlags.length; i++) {
				flags[4+i] = toolFlags[i];
			}
			IManagedCommandLineGenerator cmdLGen = tools[0].getCommandLineGenerator();
			cmdLInfo = cmdLGen.generateCommandLineInfo( tools[0], cmd, flags, outflag, outputPrefix,
					outputFile, inputs, tools[0].getCommandLinePattern() );
			buildCmd = cmdLInfo.getCommandLine();
		} else {
			String cmd = info.getToolForSource(inputExtension);
			String buildFlags = "-MM -MG -P -w " + info.getFlagsForSource(inputExtension); //$NON-NLS-1$
			String[] flags = buildFlags.split( "\\s" ); //$NON-NLS-1$
			cmdLInfo = info.generateCommandLineInfo( inputExtension, flags, outflag, outputPrefix, 
					outputFile, inputs );
			// The command to build
			if( cmdLInfo == null ) buildCmd = 
				cmd + 
				IManagedBuilderMakefileGenerator.WHITESPACE +
				"-MM -MG -P -w " +  //$NON-NLS-1$
				buildFlags + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.IN_MACRO;
			else {
				buildCmd = cmdLInfo.getCommandLine();
			}
		}

		buffer.append(IManagedBuilderMakefileGenerator.TAB + 
				buildCmd +
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">>" +  //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				depRule);

		return buffer.toString();
	}

}
