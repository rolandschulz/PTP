package org.eclipse.ptp.etfw.internal;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.ExecTool;

public class LauncherTool extends ToolStep implements IToolLaunchConfigurationConstants {

	private static final boolean traceOn = false;

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

	/**
	 * False implies that no execution is to take place (either because of an
	 * error, or user request)
	 * */
	// private boolean runbuilt=false;

	// private final ExternalToolProcess tool;//=null;//Activator.getTool();//
	// .tools[0].toolPanes[0];;

	/** Executable (application) attribute name */
	private String appnameattrib = null;
	/** Executable (application) path attribute name */
	private String apppathattrib = null;
	private String appargattrib = null;

	private ILaunch launch = null;
	private LaunchConfigurationDelegate paraDel = null;
	private ExecTool tool = null;

	public LauncherTool(ILaunchConfiguration conf, ExecTool etool, String progPath, LaunchConfigurationDelegate pd, ILaunch launcher)
			throws CoreException {
		super(conf, Messages.LauncherTool_RunningApplication);
		launch = launcher;
		tool = etool;
		paraDel = pd;
		// String ana,String projnameatt,String apa,
		appnameattrib = conf.getAttribute(EXTOOL_EXECUTABLE_NAME_TAG, (String) null);
		apppathattrib = conf.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null);
		appargattrib = conf.getAttribute(EXTOOL_ATTR_ARGUMENTS_TAG, (String) null);
		this.progPath = progPath;

	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		// try {
		// System.out.println("The job that is actually running thinks it has mpi procs of: "+this.configuration.getAttribute("org.eclipse.ptp.rm.orte.ui.launchAttributes.numProcs",
		// -1));
		// } catch (CoreException e2) {
		// // TODO Auto-generated catch block
		// e2.printStackTrace();
		// }

		try {
			// System.out.println("In tauManger "+tauManager.getConfiguration().getAttribute(tmp,
			// -1)+" vs launch "+tmpConfig.getAttribute(tmp, -1));
			if (!performLaunch(paraDel, launch, monitor))
				return new Status(IStatus.WARNING,
						"com.ibm.jdg2e.concurrency", IStatus.WARNING, Messages.LauncherTool_NothingToRun, null); //$NON-NLS-1$
		} catch (Exception e) {
			try {
				cleanup();
			} catch (CoreException e1) {
			}
			return new Status(IStatus.ERROR, "com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.LauncherTool_ExecutionError, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.LauncherTool_ExecutionComplete, null); //$NON-NLS-1$
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
			// if(tool==null)
			// throw new Exception("No valid tool configuration found");

			// if(!runbuilt)
			// return false;

			ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
			application = confWC.getAttribute(appnameattrib, (String) null);

			if (progPath != null) {
				confWC.setAttribute(appnameattrib, progPath);
				if (apppathattrib != null) {
					IFile path = thisProject.getFile(progPath);
					// System.out.println(path.exists());
					// System.out.println(path.getLocation().toString());
					savePath = confWC.getAttribute(apppathattrib, (String) null);
					confWC.setAttribute(apppathattrib, path.getLocationURI().getPath());
				}
			}

			if (tool != null)// .prependExecution)
			{
				String prog = confWC.getAttribute(appnameattrib, EMPTY_STRING);
				// TODO: This needs to work for PTP too eventually
				String arg = confWC.getAttribute(appargattrib, EMPTY_STRING);
				saveApp = prog;
				saveArgs = arg;

				// List utilList=tool.execUtils;
				Map<String, String> envMap = new LinkedHashMap<String, String>();
				if (tool.execUtils != null && tool.execUtils.length > 0) {
					// Iterator utilIt=utilList.iterator();

					String firstExecUtil = getToolExecutable(tool.execUtils[0]);// tool.execUtils[0].toolCommand;//
																				// (String)utilIt.next();//confWC.getAttribute(EXEC_UTIL_LIST,
																				// (String)null);
					if (traceOn)
						System.out.println("PerfLaunchSteps, firstExecUtil=" + firstExecUtil); //$NON-NLS-1$

					// String
					// util1Path=BuildLaunchUtils.checkToolEnvPath(firstExecUtil);
					File f = new File(firstExecUtil);
					if (firstExecUtil == null || !f.exists())
						throw new Exception(Messages.LauncherTool_Tool + firstExecUtil + Messages.LauncherTool_NotFound);

					confWC.setAttribute(appnameattrib, firstExecUtil);

					String otherUtils = getToolArguments(tool.execUtils[0], configuration);// tool.execUtils[0].getArgs()+" "+tool.execUtils[0].getPaneArgs(configuration);

					for (int i = 1; i < tool.execUtils.length; i++) {
						// TODO: Check paths of other tools
						otherUtils += " " + getToolCommand(tool.execUtils[i], configuration);//tool.execUtils[i].getCommand(configuration); //$NON-NLS-1$
					}
					swappedArgs = true;
					String toArgs = otherUtils + " " + prog + " " + arg; //$NON-NLS-1$ //$NON-NLS-2$
					System.out.println("PerfLaunchSteps.performLaunch() on: " + firstExecUtil + "|" + toArgs); //$NON-NLS-1$ //$NON-NLS-2$
					confWC.setAttribute(appargattrib, toArgs);

					for (int i = 0; i < tool.execUtils.length; i++) {
						envMap.putAll(tool.execUtils[i].getEnvVars(configuration));
					}
				}
				if (tool.global != null) {
					envMap.putAll(tool.global.getEnvVars(configuration));
				}
				// System.out.println(envMap);
				if (envMap.size() > 0) {
					saveEnv = confWC.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);
					Map<String, String> newvars = null;
					if (saveEnv != null)
						newvars = new LinkedHashMap<String, String>(saveEnv);
					else
						newvars = new LinkedHashMap<String, String>();
					newvars.putAll(envMap);
					swappedEnv = true;
					confWC.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, newvars);
				}

			}
			configuration = confWC.doSave();

			boolean reRun = launch.isTerminated();
			if (reRun) {
				IProcess[] ip = launch.getProcesses();
				for (IProcess p : ip) {
					launch.removeProcess(p);
				}
			}

			paraDel.launch(configuration, ILaunchManager.RUN_MODE, launch, monitor);

			// IProcess[] ips=launch.getProcesses();

			// if(!launch.canTerminate())
			// {
			// System.out.println("Launch can not terminate!  Possible infinite loop!");
			// cleanup();
			// throw new OperationCanceledException();
			// }
			while (!launch.isTerminated())// &&!ips[0].isTerminated())
			{
				if (monitor.isCanceled()) {
					launch.terminate();
					cleanup();
					throw new OperationCanceledException();
				}
				Thread.sleep(1000);
			}

			// while(!ips[0].isTerminated())//&&!ips[0].isTerminated())
			// {
			// Thread.sleep(1000);
			// }

			// System.out.println("Launch supposedly complete");
			return true;
		} finally {
			cleanup();
		}
	}

	/**
	 * Restore the previous default build configuration and optionally remove
	 * the performance tool's build configuration Restore the previous launch
	 * configuration settings
	 * 
	 * @throws CoreException
	 */
	public void cleanup() throws CoreException {
		ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();

		if (apppathattrib != null && savePath != null) {
			confWC.setAttribute(apppathattrib, savePath);
		}

		if (tool != null && swappedArgs)// tool.prependExecution&&
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

}
