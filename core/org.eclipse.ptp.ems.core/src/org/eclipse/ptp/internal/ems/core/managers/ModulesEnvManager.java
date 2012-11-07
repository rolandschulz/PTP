/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.core.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.internal.ems.core.messages.Messages;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * An {@link IEnvManager} for <a href="http://modules.sf.net">Modules</a>.
 * 
 * @author Jeff Overbey
 */
public final class ModulesEnvManager extends AbstractEnvManager {

	/** Command used by {@link #getDescription(IProgressMonitor)}. Output must match {@value #MODULES_SIGNATURE}. */
	private static final String CMD_MODULE_HELP = "module help"; //$NON-NLS-1$

	/** Command used by {@link #determineDefaultElements(IProgressMonitor)} */
	private static final String CMD_MODULE_LIST = "module list -t"; //$NON-NLS-1$

	/** Command used by {@link #determineAvailableElements(IProgressMonitor)} */
	private static final String CMD_MODULE_AVAIL = "module avail -t"; //$NON-NLS-1$

	/** Command used to unload all loaded modules. */
	private static final String CMD_MODULE_PURGE = "module purge >/dev/null 2>&1"; //$NON-NLS-1$
	// In my experience, some Modules installations do not handle "module purge" correctly,
	// so individually unload all loaded modules instead
	// "for MODULE in `module -t list 2>&1 | grep -E -v ':$|^[ ]*$'`; do echo module unload \"$MODULE\"; module unload \"$MODULE\" >/dev/null 2>&1; done"; //$NON-NLS-1$

	/** Format string for the command to load a particular module. */
	private static final String CMDFMT_MODULE_LOAD = "module load %s"; //$NON-NLS-1$

	/** Format string for a command to echo a line of output. */
	private static final String CMDFMT_ECHO = "echo '%s'"; //$NON-NLS-1$

	/**
	 * Pattern that must be matched by (at least) one line of the output of {@link #CMD_MODULE_HELP} in order for the environment
	 * management system to be detected. The version number will be extracted from capture group
	 * {@value #MODULES_SIGNATURE_VERSION_CAPTURE_GROUP}.
	 */
	private static final Pattern MODULES_SIGNATURE = Pattern.compile("^(  Modules Release|Modules Release) ((Tcl )?[^ \t\r\n]+).*"); //$NON-NLS-1$

	/** Capture group for the version number in {@link #MODULES_SIGNATURE}. */
	private static final int MODULES_SIGNATURE_VERSION_CAPTURE_GROUP = 2;

	/**
	 * Pattern that all module names must match.
	 * <p>
	 * This is used to distinguish valid module names from spurious output and error messages produced by Modules commands.
	 * <p>
	 * It is assumed that module names (matching this pattern) do not need to be escaped when used as arguments on a Bash command
	 * line (see {@link #getBashCommandForModuleLoad(String)}).
	 */
	private static final Pattern MODULE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9-_/.+]+"); //$NON-NLS-1$

	@Override
	public String getName() {
		return "Modules"; //$NON-NLS-1$
	}

	@Override
	public boolean checkForCompatibleInstallation(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return getDescription(pm) != null;
	}

	@Override
	public String getDescription(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		final List<String> output = runCommandInBashLoginShell(pm, CMD_MODULE_HELP);
		if (output == null) {
			return null;
		} else {
			for (final String line : output) {
				final Matcher matcher = MODULES_SIGNATURE.matcher(line);
				if (matcher.find()) {
					return "Modules " + matcher.group(MODULES_SIGNATURE_VERSION_CAPTURE_GROUP); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	@Override
	public String getInstructions() {
		return Messages.ModulesEnvManager_SelectModulesToBeLoaded;
	}

	@Override
	public List<String> determineAvailableElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		final List<String> output = runCommandInBashLoginShell(pm, CMD_MODULE_AVAIL);
		if (output == null) {
			return Collections.<String> emptyList();
		} else {
			final List<String> collectedModules = collectModuleNamesFrom(output);
			final Set<String> listedModules = new TreeSet<String>(collectedModules);
			final Set<String> unversionedModules = new TreeSet<String>(removeVersionNumbersFrom(collectedModules));
			return Collections.unmodifiableList(new ArrayList<String>(union(listedModules, unversionedModules)));
		}
	}

	private List<String> removeVersionNumbersFrom(List<String> listedModules) {
		final List<String> result = new ArrayList<String>();
		for (final String moduleName : listedModules) {
			final int slashPosition = moduleName.indexOf('/');
			if (slashPosition < 0) {
				result.add(moduleName);
			} else {
				result.add(moduleName.substring(0, slashPosition));
			}
		}
		return result;
	}

	private Set<? extends String> union(Set<String> set1, Set<String> set2) {
		final TreeSet<String> result = new TreeSet<String>();
		result.addAll(set1);
		result.addAll(set2);
		return result;
	}

	private List<String> collectModuleNamesFrom(List<String> output) {
		/*
		 * Output should resemble the following:
		 * 
		 * /usr/local/modules.forge/modulefiles:
		 * dot
		 * module-cvs
		 * module-info
		 * modules
		 * null
		 * use.own
		 * /usr/local/modules.forge/cue:
		 * /usr/local/modules.forge/apps:
		 * R/2.13.2(default)
		 * chemistry/Amber-11.1.5
		 */
		final List<String> result = new ArrayList<String>();
		for (final String line : output) {
			if (!shouldIgnore(line)) {
				String moduleName = line;
				if (moduleName.endsWith("(default)")) { //$NON-NLS-1$
					moduleName = removeSuffix(moduleName, "(default)"); //$NON-NLS-1$
				}
				moduleName = moduleName.trim();

				// Ignore spurious output (e.g., errors reported when /etc/profile executes) and duplicates
				if (MODULE_NAME_PATTERN.matcher(moduleName).matches() && !result.contains(moduleName)) {
					result.add(moduleName);
				}
			}
		}
		return result;
	}

	private boolean shouldIgnore(final String line) {
		return line.equals("") // Ignore blank lines //$NON-NLS-1$
				|| line.endsWith(":") // Ignore lines describing module locations //$NON-NLS-1$
				|| line.startsWith("-----") // Ignore lines describing module locations (Tcl) //$NON-NLS-1$
				|| line.equals("No Modulefiles Currently Loaded."); //$NON-NLS-1$
	}

	private String removeSuffix(String string, String suffix) {
		assert string != null && suffix != null && string.endsWith(suffix);
		return string.substring(0, string.length() - suffix.length());
	}

	@Override
	public List<String> determineDefaultElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		final List<String> output = runCommandInBashLoginShell(pm, CMD_MODULE_LIST);
		if (output == null) {
			return Collections.<String> emptyList();
		} else {
			return Collections.unmodifiableList(collectModuleNamesFrom(output));
		}
	}

	@Override
	protected List<String> getInitialBashCommands(boolean echo) {
		final String purgeCommand = CMD_MODULE_PURGE;
		final String echoCommand = String.format(CMDFMT_ECHO, purgeCommand);
		if (echo) {
			return Arrays.asList(echoCommand, purgeCommand);
		} else {
			return Arrays.asList(purgeCommand);
		}
	}

	@Override
	protected List<String> getBashCommand(boolean echo, String moduleName) {
		final String loadCommand = String.format(CMDFMT_MODULE_LOAD, moduleName);
		final String echoCommand = String.format(CMDFMT_ECHO, loadCommand);
		if (echo) {
			return Arrays.asList(echoCommand, loadCommand);
		} else {
			return Arrays.asList(loadCommand);
		}
	}
}
