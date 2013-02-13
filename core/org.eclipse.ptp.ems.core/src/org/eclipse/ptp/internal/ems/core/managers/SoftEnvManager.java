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
 * An {@link IEnvManager} for <a href="http://www.mcs.anl.gov/hs/software/systems/softenv/softenv-intro.html">SoftEnv</a>.
 * 
 * @author Jeff Overbey
 */
public final class SoftEnvManager extends AbstractEnvManager {

	/**
	 * Pattern that all module names must match.
	 * <p>
	 * This is used to distinguish valid module names from spurious output and error messages produced by Modules commands.
	 * <p>
	 * It is assumed that module names (matching this pattern) do not need to be escaped when used as arguments on a Bash command
	 * line (see {@link #getSoftAddBashCommand(String)}).
	 */
	private static final Pattern SOFTENV_COMMAND_PATTERN = Pattern.compile("[@+][A-Za-z0-9-_/.]+"); //$NON-NLS-1$

	@Override
	public String getName() {
		return "SoftEnv"; //$NON-NLS-1$
	}

	@Override
	public boolean checkForCompatibleInstallation(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return getDescription(pm) != null;
	}

	@Override
	public String getDescription(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		final Pattern pattern = Pattern.compile("^softenv is part of SoftEnv version ([^ \t\r\n]+).*"); //$NON-NLS-1$
		final List<String> output = runCommand(pm, true, "bash", "--login", "-c", "softenv -v"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (output == null) {
			return null;
		} else {
			for (final String line : output) {
				final Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					return "SoftEnv " + matcher.group(1); //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	@Override
	public String getInstructions() {
		return Messages.SoftEnvEnvManager_SelectSoftEnvCommands;
	}

	@Override
	public Set<String> determineAvailableElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		// NOTE: A clean exit is NOT required because -- for reasons I don't understand -- softenv -x may deliver
		// complete output, but Remote Tools does not think it has terminated and will hang until timeout
		final List<String> output = runCommand(pm, false, "bash", "--login", "-c", "softenv -x; exit"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (output == null) {
			return Collections.<String> emptySet();
		} else {
			return Collections.unmodifiableSet(collectModuleNamesFrom(output));
		}
	}

	private Set<String> collectModuleNamesFrom(List<String> output) {
		final Set<String> result = new TreeSet<String>(MODULE_NAME_COMPARATOR);
		for (String line : output) {
			line = line.trim();
			if (line.startsWith("<key>") && line.endsWith("</key>")) { //$NON-NLS-1$ //$NON-NLS-2$
				final String key = line.substring("<key>".length(), line.lastIndexOf("</key>")); //$NON-NLS-1$ //$NON-NLS-2$
				if (SOFTENV_COMMAND_PATTERN.matcher(key).matches()) {
					result.add(key);
				} else {
					// Ignore spurious output (e.g., errors reported when /etc/profile executes)
					System.err.printf("Output from softenv command includes \"%s\", which is not a valid command\n", key); //$NON-NLS-1$
				}
			}
		}
		return result;
	}

	@Override
	public Set<String> determineDefaultElements(IProgressMonitor pm) throws RemoteConnectionException, IOException {
		return Collections.<String> emptySet();
	}

	@Override
	protected List<String> getInitialBashCommands(boolean echo) {
		if (echo) {
			return Arrays.asList("echo resoft", "resoft"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return Arrays.asList("resoft"); //$NON-NLS-1$
		}
	}

	@Override
	protected List<String> getBashCommand(boolean echo, String softEnvCommand) {
		if (echo) {
			return Arrays.asList(
					String.format("echo 'soft add %s'", softEnvCommand), //$NON-NLS-1$
					String.format("soft add %s", softEnvCommand)); //$NON-NLS-1$);
		} else {
			return Arrays.asList(String.format("soft add %s", softEnvCommand)); //$NON-NLS-1$);
		}
	}
}
