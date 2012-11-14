package org.eclipse.ptp.rdt.sync.core;

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
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
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
import org.eclipse.ptp.rdt.sync.core.remotemake.SyncCommandLauncher;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

/**
 * Language settings provider to detect built-in compiler settings for GCC compiler.
 *
 * @since 8.1
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
	 */
	private class ConsoleParserAdapter implements ICBuildOutputParser {
		@Override
		public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker) throws CoreException {
			SyncGCCBuiltinSpecsDetector.this.cwdTracker = cwdTracker;
		}
		@Override
		public boolean processLine(String line) {
			if (detectedSettingEntries == null) {
				return false;
			} else {
				return SyncGCCBuiltinSpecsDetector.this.processLine(line);
			}
		}
		@Override
		public void shutdown() {
			SyncGCCBuiltinSpecsDetector.this.cwdTracker = null;
		}
	}

	@Override
	public void execute() {
		RDTSyncCorePlugin.log("Executing sync scanner discovery"); //$NON-NLS-1$
		super.execute();
	}
	
	@Override
	protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI, OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor) throws CoreException, IOException {
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(currentProject);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		int retval = 0;
		try {
			monitor.beginTask(ManagedMakeMessages.getFormattedString("AbstractBuiltinSpecsDetector.RunningScannerDiscovery",  getName()), //$NON-NLS-1$
					TICKS_EXECUTE_COMMAND + TICKS_OUTPUT_PARSING);

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
					args = new String[cmdArray.length-1];
					System.arraycopy(cmdArray, 1, args, 0, args.length);
				}
			}

			// Using GMAKE_ERROR_PARSER_ID as it can handle generated error messages
			// TODO: Add marker generator
			ErrorParserManager epm = new ErrorParserManager(currentProject, buildDirURI, null, new String[] {GMAKE_ERROR_PARSER_ID});
			ConsoleParserAdapter consoleParser = new ConsoleParserAdapter();
			consoleParser.startup(currentCfgDescription, epm);
			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			parsers.add(consoleParser);

			buildRunnerHelper.setLaunchParameters(launcher, program, args, buildDirURI, envp);
			buildRunnerHelper.prepareStreams(epm, parsers, console, new SubProgressMonitor(monitor, TICKS_OUTPUT_PARSING));

			buildRunnerHelper.greeting(ManagedMakeMessages.getFormattedString("AbstractBuiltinSpecsDetector.RunningScannerDiscovery",  getName())); //$NON-NLS-1$
			retval = buildRunnerHelper.build(new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();
		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "Error running Builtin Specs Detector" , e))); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
		return retval;
	}

	@Override
	protected String getSpecFile(String languageId) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		BuildScenario bs = bcm.getBuildScenarioForProject(currentProject);
		IRemoteConnection conn = null;
		try {
			conn = bs.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		final IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
		String specExt = getSpecFileExtension(languageId);
		String ext = ""; //$NON-NLS-1$
		if (specExt != null) {
			ext = '.' + specExt;
		}

		String specFileName = SPEC_FILE_BASE + ext;
		IPath workingLocation = new Path(bs.getLocation(currentProject));
		IPath fileLocation = workingLocation.append(".ptp-sync" + specFileName); //$NON-NLS-1$
		final IFileStore fileStore = fileManager.getResource(fileLocation.toString());
		final IFileInfo fileInfo = fileStore.fetchInfo();
		if (!fileInfo.exists()) {
			OutputStream os;
			try {
				os = fileStore.openOutputStream(EFS.NONE, null);
				os.write(10);
				os.close();
			} catch (CoreException e) {
				RDTSyncCorePlugin.log(e);
			} catch (IOException e) {
				RDTSyncCorePlugin.log(e);
			}
		}
		return fileLocation.toString();
	}

	/**
	 * Create and start the provider console.
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
					String msg = "Unable to find icon " + DEFAULT_CONSOLE_ICON + " in plugin " + CDT_MANAGEDBUILDER_UI_PLUGIN_ID; //$NON-NLS-1$ //$NON-NLS-2$
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