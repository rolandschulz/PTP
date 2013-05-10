package org.eclipse.ptp.internal.etfw;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.ExecTool;
import org.eclipse.ptp.etfw.toolopts.ToolApp;
import org.eclipse.ptp.internal.etfw.messages.Messages;

public class LauncherTool extends ToolStep implements IToolLaunchConfigurationConstants {

	/**
	 * The location of the binary rebuilt with performance instrumentation
	 */
	private String progPath = null;

	/**
	 * The name of the original application in the launch configuration
	 */
	private String application = null;

	private String saveApp = null;
	private String saveArgs = null;
	private String savePath = null;

	private boolean swappedArgs = false;

	private Map<String, String> saveEnv = null;
	private boolean swappedEnv = false;

	/** Executable (application) attribute name */
	private String appnameattrib = null;
	/** Executable (application) path attribute name */
	private String apppathattrib = null;
	private String appargattrib = null;

	private ILaunch launch = null;
	private LaunchConfigurationDelegate paraDel = null;
	private ExecTool tool = null;
	private IBuildLaunchUtils utilBlob = null;

	public LauncherTool(ILaunchConfiguration conf, ExecTool etool, String progPath, LaunchConfigurationDelegate pd,
			ILaunch launcher, IBuildLaunchUtils utilBlob)
			throws CoreException {
		super(conf, Messages.LauncherTool_RunningApplication, utilBlob);
		this.utilBlob = utilBlob;
		launch = launcher;
		tool = etool;
		paraDel = pd;
		appnameattrib = conf.getAttribute(EXTOOL_EXECUTABLE_NAME_TAG, (String) null);
		apppathattrib = conf.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null);
		appargattrib = conf.getAttribute(EXTOOL_ATTR_ARGUMENTS_TAG, (String) null);
		this.progPath = progPath;

	}

	/**
	 * Restore the previous default build configuration and optionally remove
	 * the performance tool's build configuration Restore the previous launch
	 * configuration settings
	 * 
	 * @throws CoreException
	 */
	public void cleanup() throws CoreException {
		final ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();

		if (apppathattrib != null && savePath != null) {
			confWC.setAttribute(apppathattrib, savePath);
		}

		if (tool != null && swappedArgs)
		{
			confWC.setAttribute(appnameattrib, saveApp);
			confWC.setAttribute(appargattrib, saveArgs);
		}

		if (tool != null && swappedEnv) {
			confWC.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, saveEnv);
		}

		confWC.setAttribute(appnameattrib, application);
		configuration = confWC.doSave();
	}

	/**
	 * Given a string which may be a valid path, returns the last directory in the path, or null if it is not a path.
	 * 
	 * @param path
	 * @return
	 */
	private String getDirectory(String path) {
		String projectDir = null;
		IFileStore pdStore = null;
		IFileInfo pdInfo = null;
		if (path != null)
		{
			pdStore = utilBlob.getFile(path);
			pdInfo = pdStore.fetchInfo();
			if (pdInfo.exists()) {
				if (!pdInfo.isDirectory()) {
					pdStore = pdStore.getParent();
				}
				projectDir = pdStore.toURI().getPath();
			}
		}

		return projectDir;
	}

	/**
	 * This launches the application and makes and adjustments to the build
	 * configuration if necessary
	 * 
	 * @param paraDel
	 * @param launch
	 * @param monitor
	 * @return True if the launch is attempted, false otherwise
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public boolean performLaunch(LaunchConfigurationDelegate paraDel, ILaunch launch, IProgressMonitor monitor) throws Exception {
		try {

			String path = null;
			String prog = null;
			final ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
			application = confWC.getAttribute(appnameattrib, (String) null);

			/*
			 * If progPath is a regular file then it is the actual executable from a managed build and we need to swap it out with
			 * our rebuilt version
			 * If it is a directory then it is the location of the binary provided by a makefile build and we can ignore it.
			 */
			IFileStore testStore = null;
			if (progPath != null) {
				testStore = utilBlob.getFile(progPath);
			}

			IFileStore progStore = utilBlob.getFile(projectLocation);
			boolean progStoreGood = false;
			if (progStore.fetchInfo().exists() && progPath != null) {
				progStore = progStore.getChild(progPath);
				if (progStore.fetchInfo().exists()) {
					progStoreGood = true;
				}
			}
			else {
				progStore = null;
			}

			if ((testStore != null && testStore.fetchInfo().exists() && !testStore.fetchInfo().isDirectory()) || progStoreGood) {
				if (apppathattrib != null) {
					savePath = confWC.getAttribute(apppathattrib, (String) null);
				} else {
					savePath = null;
				}

				// IFileStore testStore=utilBlob.getFile(progPath);
				// IFileStore progStore=utilBlob.getFile(projectLocation);
				if (progStoreGood) {
					// progStore=progStore.getChild(progPath);
					/*
					 * If the randomly generated JAXB path tags are in use we need to replace the values linked with those tags with
					 * the values used by ETFW
					 */
					String jaxbAtt = confWC.getAttribute(EXTOOL_JAXB_EXECUTABLE_PATH_TAG, EMPTY_STRING);
					if (jaxbAtt.length() > 0) {
						confWC.setAttribute(jaxbAtt, progStore.toURI().getPath());
					}
					jaxbAtt = confWC.getAttribute(EXTOOL_JAXB_EXECUTABLE_DIRECTORY_TAG, EMPTY_STRING);
					if (jaxbAtt.length() > 0) {
						confWC.setAttribute(jaxbAtt, progStore.getParent().toURI().getPath());
					}
				}

				if (testStore.fetchInfo().exists() && !testStore.fetchInfo().isDirectory()) {
					confWC.setAttribute(apppathattrib, testStore.toURI().getPath());
					confWC.setAttribute(appnameattrib, testStore.getName());
				}
				else {
					confWC.setAttribute(appnameattrib, progPath);
					if (apppathattrib != null) {
						// String path=null;
						if (isSyncProject)
						{
							path = progStore.toURI().getPath();// outputLocation;
						}
						else
						{
							path = thisProject.getFile(progPath).getLocationURI().getPath();
						}
						// savePath = confWC.getAttribute(apppathattrib, (String) null);
						confWC.setAttribute(apppathattrib, path);
					}
				}
			}

			if (tool != null)
			{

				prog = confWC.getAttribute(appnameattrib, EMPTY_STRING);
				boolean usepathforapp = false;
				if (prog == null || prog.equals(EMPTY_STRING)) {
					prog = confWC.getAttribute(apppathattrib, EMPTY_STRING);
					if (prog != null && !prog.equals(EMPTY_STRING)) {
						savePath = confWC.getAttribute(apppathattrib, (String) null);
						usepathforapp = true;
					}
				}
				// TODO: This needs to work for PTP too eventually
				final String arg = confWC.getAttribute(appargattrib, EMPTY_STRING);
				saveApp = prog;
				saveArgs = arg;
				final Map<String, String> envMap = new LinkedHashMap<String, String>();

				if (tool.execUtils != null && tool.execUtils.length > 0) {

					final String firstExecUtil = getToolExecutable(tool.execUtils[0]);

					if (firstExecUtil == null) {
						throw new Exception(Messages.LauncherTool_Tool + firstExecUtil + Messages.LauncherTool_NotFound);
					}

					if (!usepathforapp) {
						confWC.setAttribute(appnameattrib, firstExecUtil);
					}
					else {
						confWC.setAttribute(apppathattrib, firstExecUtil);
					}

					String otherUtils = getToolArguments(tool.execUtils[0], configuration);

					for (int i = 1; i < tool.execUtils.length; i++) {
						// TODO: Check paths of other tools
						otherUtils += SPACE + getToolCommand(tool.execUtils[i], configuration);
					}
					swappedArgs = true;

					String toArgs = otherUtils;

					if (!tool.replaceExecution) {

						toArgs = toArgs + SPACE + prog + SPACE + arg;

					}

					confWC.setAttribute(appargattrib, toArgs);

					String jaxbAtt = confWC.getAttribute(EXTOOL_JAXB_ATTR_ARGUMENTS_TAG, EMPTY_STRING);
					if (jaxbAtt != null && jaxbAtt.length() > 0) {
						confWC.setAttribute(jaxbAtt, toArgs);
					}
					jaxbAtt = confWC.getAttribute(EXTOOL_JAXB_EXECUTABLE_PATH_TAG, EMPTY_STRING);
					if (jaxbAtt != null && jaxbAtt.length() > 0) {
						confWC.setAttribute(jaxbAtt, firstExecUtil);
					}

					for (final ToolApp execUtil : tool.execUtils) {
						envMap.putAll(execUtil.getEnvVars(configuration));
					}

				}

				if (tool.global != null) {
					envMap.putAll(tool.global.getEnvVars(configuration));
				}

				if (envMap.size() > 0) {

					final String wd = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
					String projectDir = null;
					if (wd != null && wd.length() > 0) {
						projectDir = wd;
					} else {
						projectDir = getDirectory(path);
					}
					if (projectDir == null) {
						projectDir = getDirectory(prog);
					}
					if (projectDir == null) {
						projectDir = DOT;
					}
					if (projectDir != null) {
						this.outputLocation = projectDir;
						final Set<String> mapKeys = envMap.keySet();
						final Iterator<String> keyIt = mapKeys.iterator();
						while (keyIt.hasNext()) {
							final String key = keyIt.next();
							final String var = envMap.get(key);
							if (var.equals(PROJECT_DIR)) {
								envMap.put(key, projectDir);
							}
						}
					}
					saveEnv = confWC.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);
					Map<String, String> newvars = null;
					if (saveEnv != null) {
						newvars = new LinkedHashMap<String, String>(saveEnv);
					} else {
						newvars = new LinkedHashMap<String, String>();
					}
					newvars.putAll(envMap);
					swappedEnv = true;
					confWC.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, newvars);
					confWC.setAttribute(PROJECT_DIR, projectDir);
				}

			}
			configuration = confWC.doSave();

			final boolean reRun = launch.isTerminated();
			if (reRun) {
				final IProcess[] ip = launch.getProcesses();
				for (final IProcess p : ip) {
					launch.removeProcess(p);
				}
			}

			if (paraDel instanceof IToolLaunchConfigurationDelegate) {
				((IToolLaunchConfigurationDelegate) paraDel).setInitialized(true);
			}

			paraDel.launch(configuration, ILaunchManager.RUN_MODE, launch, monitor);

			while (!launch.isTerminated())
			{
				if (monitor.isCanceled()) {
					launch.terminate();
					if (paraDel instanceof IToolLaunchConfigurationDelegate) {
						((IToolLaunchConfigurationDelegate) paraDel).setInitialized(false);
					}
					cleanup();
					throw new OperationCanceledException();
				}
				Thread.sleep(1000);
			}
			return true;
		} finally {
			if (paraDel instanceof IToolLaunchConfigurationDelegate) {
				((IToolLaunchConfigurationDelegate) paraDel).setInitialized(false);
			}
			cleanup();
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		try {
			if (!performLaunch(paraDel, launch, monitor))
			{
				return new Status(IStatus.WARNING,
						"com.ibm.jdg2e.concurrency", IStatus.WARNING, Messages.LauncherTool_NothingToRun, null); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			try {
				cleanup();
			} catch (final CoreException e1) {
			}
			return new Status(IStatus.ERROR, "com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.LauncherTool_ExecutionError, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.LauncherTool_ExecutionComplete, null); //$NON-NLS-1$
	}

}
