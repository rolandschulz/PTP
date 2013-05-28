/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.remotemake.SyncCommandLauncher;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

/**
 * Language settings provider to detect built-in compiler settings for GCC compiler, modified to work with synchronized projects.
 * The goal of this class is to rely on superclasses as much as possible and minimize code copying while modifying the code to:
 * 1) Run processes on the remote machine for the current build configuration
 * 2) Convert paths to UNC notation with connection name prepended:
 * syntax: //<connection name>/<discovered path>
 */
public class SyncGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	private static final String CDT_MANAGEDBUILDER_UI_PLUGIN_ID = "org.eclipse.cdt.managedbuilder.ui"; //$NON-NLS-1$
	private static final String SCANNER_DISCOVERY_CONSOLE = "org.eclipse.cdt.managedbuilder.ScannerDiscoveryConsole"; //$NON-NLS-1$
	private static final String SCANNER_DISCOVERY_GLOBAL_CONSOLE = "org.eclipse.cdt.managedbuilder.ScannerDiscoveryGlobalConsole"; //$NON-NLS-1$
	private static final String DEFAULT_CONSOLE_ICON = "icons/obj16/inspect_sys.gif"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$

	private static final int MONITOR_SCALE = 100;
	private static final int TICKS_OUTPUT_PARSING = 1 * MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * MONITOR_SCALE;

	/**
	 * Internal ICConsoleParser to handle individual run for one language.
	 * This is basically a copy of: {@link
	 * org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector.ConsoleParserAdapter()} necessary
	 * because that class is private.
	 */
	private class ConsoleParserAdapter implements ICBuildOutputParser {
		@Override
		public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker) throws CoreException {
			SyncGCCBuiltinSpecsDetector.this.cwdTracker = cwdTracker;
		}

		@Override
		public boolean processLine(String line) {
			return SyncGCCBuiltinSpecsDetector.this.processLine(line);
		}

		@Override
		public void shutdown() {
			SyncGCCBuiltinSpecsDetector.this.cwdTracker = null;
		}
	}

	/**
	 * A copy of: {@link org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector#runForLanguage()}
	 * modified to use the sync command launcher and to not run if spec file is null (see code comments). Note that this method is
	 * called by "runForLanguage," it does not override it. Thus, all of the setup for running is done twice. Specifically, a
	 * BuildRunnerHelper is built twice. Ideally, CDT would provide an extension point to change the command launcher, as it does for
	 * builds.
	 *
	 * @return ICommandLauncher status of run
	 */
	@Override
	protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI,
			OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor) throws CoreException, IOException {
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(currentProject);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		int retval = ICommandLauncher.COMMAND_CANCELED;
		boolean closeAttempted = false;
		try {
			monitor.beginTask(ManagedMakeMessages.getFormattedString(Messages.SyncGCCBuiltinSpecsDetector_0, getName()),
					TICKS_EXECUTE_COMMAND + TICKS_OUTPUT_PARSING);

			// Do not run if spec file is undefined. This can happen if the detector is called when no current project is defined.
			// The spec file is project-independent for CDT but not for synchronized projects, because the spec file location
			// depends on the current project's sync configuration.
			if (specFile == null) {
				return retval;
			}

			IConsole console;
			if (super.isConsoleEnabled()) {
				console = startProviderConsole();
			} else {
				// that looks in extension points registry and won't find the id, this console is not shown
				console = CCorePlugin.getDefault().getConsole(ManagedBuilderCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
			}
			console.start(currentProject);

			ICommandLauncher launcher = new SyncCommandLauncher();
			launcher.setProject(currentProject);

			IPath program = new Path(""); //$NON-NLS-1$
			String[] args = new String[0];
			String[] cmdArray = CommandLineUtil.argumentsToArray(command);
			if (cmdArray != null && cmdArray.length > 0) {
				program = new Path(cmdArray[0]);
				if (cmdArray.length > 1) {
					args = new String[cmdArray.length - 1];
					System.arraycopy(cmdArray, 1, args, 0, args.length);
				}
			}

			// Using GMAKE_ERROR_PARSER_ID as it can handle generated error messages
			// TODO: Add marker generator - superclass marker generator is private.
			ErrorParserManager epm = new ErrorParserManager(currentProject, buildDirURI, null,
					new String[] { GMAKE_ERROR_PARSER_ID });
			ConsoleParserAdapter consoleParser = new ConsoleParserAdapter();
			consoleParser.startup(currentCfgDescription, epm);
			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			parsers.add(consoleParser);

			buildRunnerHelper.setLaunchParameters(launcher, program, args, currentProject.getLocationURI(), envp);
			buildRunnerHelper.prepareStreams(epm, parsers, console, new SubProgressMonitor(monitor, TICKS_OUTPUT_PARSING));

			buildRunnerHelper.greeting(ManagedMakeMessages.getFormattedString(Messages.SyncGCCBuiltinSpecsDetector_1, getName()));
			retval = buildRunnerHelper.build(new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			closeAttempted = true;
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();
		} catch (Exception e) {
			if (closeAttempted) {
				ManagedBuilderCorePlugin.log(e);
			} else {
				ManagedBuilderCorePlugin.log(new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
						Messages.SyncGCCBuiltinSpecsDetector_2, e)));
				try {
					buildRunnerHelper.close();
				} catch (IOException e1) {
					ManagedBuilderCorePlugin.log(e1);
				}
			}
		} finally {
			monitor.done();
		}
		return retval;
	}

	/**
	 * This method intercepts and modifies the scanner discovery entries. It changes include paths to UNC notation with the correct
	 * connection name prepended.
	 */
	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		if (entries == null) {
			super.setSettingEntries(entries);
			return;
		}
		SyncConfig config = SyncConfigManager.getActive(currentProject);
		if (config.getSyncProviderId() == null) {
			// For local configurations, no special processing is needed.
			super.setSettingEntries(entries);
			return;
		}

		IRemoteConnection conn = null;
		try {
			conn = config.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			// Impossible to build includes properly without connection name
			super.setSettingEntries(entries);
			return;
		}

		List<ICLanguageSettingEntry> newEntries = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSettingEntry entry : entries) {
			if ((entry instanceof CIncludePathEntry) && ((entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == 0)) {
				String oldPath = ((CIncludePathEntry) entry).getValue();
				// Bug 402350: Sync scanner discovery has corrupt remote paths when using Windows
				if (oldPath.startsWith("C:")) { //$NON-NLS-1$
					oldPath = oldPath.substring(2);
				}
				String newPath = "//" + conn.getName() + oldPath; //$NON-NLS-1$
				ICLanguageSettingEntry newEntry = new CIncludePathEntry(newPath, entry.getFlags());
				newEntries.add(newEntry);
			} else {
				newEntries.add(entry);
			}
		}
		super.setSettingEntries(newEntries);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector#getSpecFile(java.lang.String)
	 */
	@Override
	protected String getSpecFile(String languageId) {
		// This check is only necessary for synchronized projects, since file location is dependent on the current project.
		if (currentProject == null) {
			return null;
		}
		SyncConfig config = SyncConfigManager.getActive(currentProject);
		// For local configurations, we can fall back to the original implementation.
		if (config.getSyncProviderId() == null) {
			return super.getSpecFile(languageId);
		}

		// Build spec file name
		String specFileName = SPEC_FILE_BASE;
		String ext = getSpecFileExtension(languageId);
		if (ext != null) {
			specFileName = specFileName + '.' + ext;
		}
		IPath workingLocation = new Path(config.getLocation(currentProject));
		// TODO: Get rid of .ptp-sync string literal.
		// TODO: What if .ptp-sync does not exist?
		// TODO: What if remote system does not use '/' path separator? (assumed by IPath.toString())
		IPath fileLocation = workingLocation.append(".ptp-sync").append(specFileName); //$NON-NLS-1$

		// Create spec file if it doesn't exist
		IRemoteConnection conn = null;
		try {
			conn = config.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			return fileLocation.toString();
		}

		final IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
		final IFileStore fileStore = fileManager.getResource(fileLocation.toString());
		final IFileInfo fileInfo = fileStore.fetchInfo();
		if (!fileInfo.exists()) {
			OutputStream os;
			try {
				os = fileStore.openOutputStream(EFS.NONE, null);
				os.write('\n');
				os.close();
			} catch (CoreException e) {
				Activator.log(e);
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return fileLocation.toString();
	}

	/**
	 * Create and start the provider console.
	 * This is basically a copy of:
	 * {@link org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector#startProviderConsole()}
	 * necessary because that class is private.
	 * 
	 * @return CDT console.
	 */
	private IConsole startProviderConsole() {
		IConsole console = null;

		if (super.isConsoleEnabled() && currentLanguageId != null) {
			String extConsoleId;
			if (currentProject != null) {
				extConsoleId = SCANNER_DISCOVERY_CONSOLE;
			} else {
				extConsoleId = SCANNER_DISCOVERY_GLOBAL_CONSOLE;
			}
			ILanguage ld = LanguageManager.getInstance().getLanguage(currentLanguageId);
			if (ld != null) {
				String consoleId = ManagedBuilderCorePlugin.PLUGIN_ID + '.' + getId() + '.' + currentLanguageId;
				String consoleName = getName() + ", " + ld.getName(); //$NON-NLS-1$
				URL defaultIcon = Platform.getBundle(CDT_MANAGEDBUILDER_UI_PLUGIN_ID).getEntry(DEFAULT_CONSOLE_ICON);
				if (defaultIcon == null) {
					String msg = Messages.SyncGCCBuiltinSpecsDetector_3 + DEFAULT_CONSOLE_ICON
							+ Messages.SyncGCCBuiltinSpecsDetector_4 + CDT_MANAGEDBUILDER_UI_PLUGIN_ID;
					ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg));
				}

				console = CCorePlugin.getDefault().getConsole(extConsoleId, consoleId, consoleName, defaultIcon);
			}
		}

		if (console == null) {
			// that looks in extension points registry and won't find the id, this console is not shown
			console = CCorePlugin.getDefault().getConsole(ManagedBuilderCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
		}

		return console;
	}
}