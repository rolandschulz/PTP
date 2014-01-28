/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.ToolApp;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolAppType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolIOType;
import org.eclipse.ptp.internal.etfw.jaxb.util.ToolAppTypeUtil;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.remote.core.IRemoteResource;

/**
 * This is a re-implementation of ToolStep specifically for the ETFw JAXB workflow. It Manages the process of building instrumented
 * applications and collecting the resulting data.
 * 
 * @see ToolStep
 * 
 * @author Chris Navarro
 * 
 */
public abstract class ETFWToolStep extends Job implements IToolLaunchConfigurationConstants {

	/**
	 * Gets the project associated with the launch configuration held by this
	 * manager
	 * 
	 * @param configuration
	 * @return The project associated with this manager's launch configuration,
	 *         or null if no project is found
	 * @throws CoreException
	 */
	protected static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(configuration.getAttribute(EXTOOL_PROJECT_NAME_TAG, (String) null),
				(String) null);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}
		return null;
	}

	protected String projectLocation = null;

	protected String outputLocation = null;
	protected String projectBinary = null;

	protected String projectName = null;
	protected IProject thisProject = null;
	protected ILaunchConfiguration configuration = null;
	protected Map<String, String> IOMap = null;
	protected boolean isSyncProject = false;

	protected IBuildLaunchUtils utilBlob = null;

	protected ETFWToolStep(ILaunchConfiguration conf, String name, IBuildLaunchUtils utilBlob) throws CoreException {
		super(name);
		configuration = conf;

		thisProject = getProject(configuration);

		if (thisProject != null) {
			IRemoteResource remoteResource = (IRemoteResource) thisProject.getAdapter(IRemoteResource.class);
			if (remoteResource != null) {
				isSyncProject = RemoteSyncNature.hasNature(thisProject);
				projectLocation = remoteResource.getActiveLocationURI().getPath();
			} else {
				projectLocation = thisProject.getLocationURI().getPath();
			}

			outputLocation = projectLocation;
		}

		projectBinary = Messages.ToolStep_Unknown;
		projectName = Messages.ToolStep_Unknown;
		this.utilBlob = utilBlob;
		IOMap = new HashMap<String, String>();
	}

	private String createOutputPath(ToolIOType toolArg) {
		// String ostring="";
		final String opath = projectLocation + File.separator + toolArg.getId() + File.separator + BuildLaunchUtils.getNow();
		final File ofile = new File(opath);
		ofile.mkdirs();
		/*
		 * Create the opath. Do something smart if it is impossible.
		 */
		IOMap.put(toolArg.getId(), opath);

		return opath;
	}

	public ILaunchConfiguration getConfiguration() {
		return configuration;
	}

	protected List<String> getToolArgumentList(ToolAppType app, ILaunchConfiguration configuration) throws CoreException {
		if (app == null) {
			return null;
		}
		// Formerly replaced with projectLocation global variable. May be the
		// same?
		final List<String> allargs = ToolAppTypeUtil.getArguments(configuration, app.getToolPanes(), app.getToolArguments());// app.getArguments(configuration);

		for (int i = 0; i < allargs.size(); i++) {
			final String tmp = insertProjectValues(allargs.get(i));

			allargs.set(i, tmp);
		}
		String io = parseInput(app).trim();
		if (io.length() > 0) {
			allargs.add(io);
		}
		io = parseOutput(app).trim();
		if (io.length() > 0) {
			allargs.add(io);
		}
		return allargs;
	}

	protected String getToolArguments(ToolAppType app, ILaunchConfiguration configuration) throws CoreException {
		final List<String> argList = getToolArgumentList(app, configuration);
		String args = ""; //$NON-NLS-1$
		if (argList == null) {
			return (args);
		}

		for (final String a : argList) {
			args += a + " "; //$NON-NLS-1$
		}

		// Formerly replaced with projectLocation global variable. May be the
		// same?
		// String allargs=app.getArguments(configuration);
		// allargs=allargs.replaceAll(ToolsOptionsConstants.PROJECT_BUILD,
		// buildDir);
		// allargs=allargs.replaceAll(ToolsOptionsConstants.PROJECT_ROOT,
		// rootDir);
		// allargs=allargs+parseInput(app)+" "+parseOutput(app);
		return args;
	}

	/**
	 * Returns the full tool command; the full path to the executable used by
	 * app followed by any arguments, replacing the output location with the
	 * string provided by outputloc if necessary
	 */
	protected String getToolCommand(ToolAppType app, ILaunchConfiguration configuration) throws CoreException {
		final String command = getToolExecutable(app);
		if (command == null) {
			return null;
		}

		return command + " " + getToolArguments(app, configuration); //$NON-NLS-1$
	}

	/**
	 * Returns the full tool command; the full path to the executable used by
	 * app followed by any arguments, replacing the output location with the
	 * string provided by outputloc if necessary
	 */
	protected List<String> getToolCommandList(ToolAppType app, ILaunchConfiguration configuration) throws CoreException {
		final List<String> command = new ArrayList<String>();
		final String exec = getToolExecutable(app);
		if (exec == null) {
			return null;
		}

		command.add(exec.trim());

		final List<String> args = getToolArgumentList(app, configuration);
		if (args == null) {
			return command;
		}
		command.addAll(args);
		return command;
	}

	protected Map<String, String> getToolEnvVars(ToolApp app, ILaunchConfiguration configuration) throws CoreException {
		if (app == null) {
			return null;
		}
		final Map<String, String> map = app.getEnvVars(configuration);

		final Iterator<Entry<String, String>> mapIt = map.entrySet().iterator();
		Entry<String, String> ent;
		while (mapIt.hasNext()) {
			ent = mapIt.next();

			final String tmp = insertProjectValues(ent.getValue());

			ent.setValue(tmp);
		}

		return map;
	}

	/**
	 * Given a ToolApp app finds the full path to the associated executable if
	 * possible and returns it. If not it just returns the executable name
	 * 
	 * @param app
	 * @return
	 */
	protected String getToolExecutable(ToolAppType app) {
		String command = app.getToolCommand();

		String toolPath = utilBlob.getToolPath(app.getToolGroup());
		if (toolPath == null || toolPath.length() == 0) {
			toolPath = utilBlob.checkToolEnvPath(command);
		}
		if (toolPath != null && toolPath.length() > 0) {
			String fiSep = File.separator;
			if (toolPath.startsWith(UNIX_SLASH)) {
				fiSep = UNIX_SLASH;
			}
			command = toolPath + fiSep + command;
		}

		return command;
	}

	private String insertProjectValues(String tmp) {
		tmp = tmp.replaceAll(ToolsOptionsConstants.PROJECT_BUILD, outputLocation);
		tmp = tmp.replaceAll(ToolsOptionsConstants.PROJECT_ROOT, projectLocation);
		tmp = tmp.replaceAll(ToolsOptionsConstants.PROJECT_BINARY, projectBinary);
		tmp = tmp.replaceAll(ToolsOptionsConstants.PROJECT_NAME, projectName);
		return tmp;
	}

	private String parseInput(ToolAppType app) {
		String input = ""; //$NON-NLS-1$
		if (app.getInputArgs() != null) {
			for (final ToolIOType inputArg : app.getInputArgs()) {
				if (IOMap.containsKey(inputArg.getId())) {
					input += IOMap.get(inputArg.getId());
					/*
					 * If input type is not directory, get a file list from the
					 * specified directory
					 */
				} else {
					return ""; //$NON-NLS-1$
				}
			}
		}
		return input;
	}

	private String parseOutput(ToolAppType app) {
		String output = ""; //$NON-NLS-1$

		/*
		 * Make and the new directory, associate it with the ID and stick -that-
		 * in the iomap!
		 */
		if (app.getOutputArgs() != null) {
			for (final ToolIOType outputArg : app.getOutputArgs()) {
				// for (int i = 0; i < app.outputArgs.length; i++) {
				if (outputArg.getPathFlag() != null) {
					output += outputArg.getPathFlag() + " "; //$NON-NLS-1$
				}
				if (IOMap.containsKey(outputArg.getId())) {
					output += IOMap.get(outputArg.getId());
					/*
					 * If input type is not directory, get a file list from the
					 * specified directory
					 */
				} else {
					output += createOutputPath(outputArg);
				}
			}
		}

		return output;
	}

	public void setConfiguration(ILaunchConfiguration conf) {
		configuration = conf;
	}

	public abstract void setSuccessAttribute(String value);

}