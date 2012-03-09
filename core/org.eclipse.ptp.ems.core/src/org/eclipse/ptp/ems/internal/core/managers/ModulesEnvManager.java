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
package org.eclipse.ptp.ems.internal.core.managers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.internal.core.Messages;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * An {@link IEnvManager} for <a href="http://modules.sf.net">Modules</a>.
 * 
 * @author Jeff Overbey
 */
public final class ModulesEnvManager extends AbstractEnvManager {

	/**
	 * Pattern that all module names must match.
	 * <p>
	 * This is used to distinguish valid module names from spurious output and error messages produced by Modules commands.
	 * <p>
	 * It is assumed that module names (matching this pattern) do not need to be escaped when used as arguments on a Bash command
	 * line (see {@link #getBashCommandForModuleLoad(String)}).
	 */
	private static final Pattern MODULE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9-_/.]+"); //$NON-NLS-1$

	@Override
	public String getName() {
		return "Modules"; //$NON-NLS-1$
	}

	@Override
	public boolean checkForCompatibleInstallation() throws RemoteConnectionException, IOException {
		return getDescription() != null;
	}

	@Override
	public String getDescription() throws RemoteConnectionException, IOException {
		final Pattern pattern = Pattern.compile("^  Modules Release ([^ \t\r\n]+).*"); //$NON-NLS-1$
		final List<String> output = runCommand(true, "bash", "--login", "-c", "module help"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (output == null) {
			return null;
		} else {
			for (final String line : output) {
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					return "Modules " + matcher.group(1); //$NON-NLS-1$
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
	public Set<String> determineAvailableElements() throws RemoteConnectionException, IOException {
		final List<String> output = runCommand(true, "bash", "--login", "-c", "module -t avail"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (output == null) {
			return Collections.<String> emptySet();
		} else {
			final Set<String> listedModules = collectModuleNamesFrom(output);
			final Set<String> unversionedModules = removeVersionNumbersFrom(listedModules);
			return Collections.unmodifiableSet(union(listedModules, unversionedModules));
		}
	}

	private Set<String> removeVersionNumbersFrom(Set<String> listedModules) {
		final TreeSet<String> result = new TreeSet<String>();
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

	private Set<String> collectModuleNamesFrom(List<String> output) {
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
		final Set<String> result = new TreeSet<String>(MODULE_NAME_COMPARATOR);
		for (final String line : output) {
			if (!line.equals("") && !line.endsWith(":")) { // Ignore blank lines and lines describing module locations //$NON-NLS-1$ //$NON-NLS-2$
				String moduleName = line;
				if (moduleName.endsWith("(default)")) { //$NON-NLS-1$
					moduleName = removeSuffix(moduleName, "(default)"); //$NON-NLS-1$
				}
				moduleName = moduleName.trim();

				if (MODULE_NAME_PATTERN.matcher(moduleName).matches()) {
					result.add(moduleName);
				} else {
					// Ignore spurious output (e.g., errors reported when /etc/profile executes)
					System.err.printf("Output from module command includes \"%s\", which is not a valid module name\n", moduleName); //$NON-NLS-1$
				}
			}
		}
		return result;
	}

	private String removeSuffix(String string, String suffix) {
		assert string != null && suffix != null && string.endsWith(suffix);
		return string.substring(0, string.length() - suffix.length());
	}

	@Override
	public Set<String> determineDefaultElements() throws RemoteConnectionException, IOException {
		final List<String> output = runCommand(true, "bash", "--login", "-c", "module -t list"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (output == null) {
			return Collections.<String> emptySet();
		} else {
			return Collections.unmodifiableSet(collectModuleNamesFrom(output));
		}
	}

	@Override
	protected List<String> getInitialBashCommands(boolean echo) {
		// In my experience, some Modules installations do not handle "module purge" correctly,
		// so individually unload all loaded modules instead
		if (echo) {
			return Arrays.asList("for MODULE in `module -t list 2>&1 | grep -E -v ':$|^[ ]*$'`; do echo module unload \"$MODULE\"; module unload \"$MODULE\"; done"); //$NON-NLS-1$
		} else {
			return Arrays.asList("for MODULE in `module -t list 2>&1 | grep -E -v ':$|^[ ]*$'`; do module unload \"$MODULE\"; done"); //$NON-NLS-1$
		}
	}

	@Override
	protected List<String> getBashCommand(boolean echo, String moduleName) {
		if (echo) {
			return Arrays.asList(
					String.format("echo 'module load %s'", moduleName), //$NON-NLS-1$
					String.format("module load %s", moduleName)); //$NON-NLS-1$);
		} else {
			return Arrays.asList(
					String.format("module load %s", moduleName)); //$NON-NLS-1$);
		}
	}
}
