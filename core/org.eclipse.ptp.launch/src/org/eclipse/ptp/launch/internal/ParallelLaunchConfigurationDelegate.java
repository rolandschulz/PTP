/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.internal;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPTPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
/**
 * 
 */
public class ParallelLaunchConfigurationDelegate 
	extends AbstractParallelLaunchConfigurationDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.internal.AbstractParallelLaunchConfigurationDelegate#doCompleteJobLaunch(org.eclipse.ptp.core.elements.IPJob)
	 */
	protected void doCompleteJobLaunch(ILaunchConfiguration configuration, String mode, final IPLaunch launch, 
			AttributeManager mgr, final IPTPDebugger debugger, IPJob job) {
		launch.setAttribute(ElementAttributes.getIdAttributeDefinition().getId(), job.getID());
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			// show ptp debug view
			showPTPDebugView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
			launch.setPJob(job);
			try {
				setDefaultSourceLocator(launch, configuration);
				IBinaryObject exeFile = verifyBinary(configuration);
				IProject project = verifyProject(configuration);
				final IFile exec = project.getFile(getProgramName(configuration));
				final IPath execPath = exeFile.getPath();
				
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IRunnableWithProgress runnable = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									long timeout = PTPDebugUIPlugin.getDefault().getPreferenceStore().getLong(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
									//Wait for the incoming debug server connection. This can be cancelled by the user.
									PTPDebugCorePlugin.getDebugModel().createDebugSession(timeout, debugger, launch, exec, execPath, monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
							}
						};
						try {
							new ProgressMonitorDialog(PTPLaunchPlugin.getActiveWorkbenchShell()).run(true, true, runnable);
						} catch (InterruptedException e) {
							System.out.println("Error completing debug job launch: " + e.getMessage());
						} catch (InvocationTargetException e) {
							System.out.println("Error completing debug job launch: " + e.getMessage());
						}
					}
				});
			} catch (CoreException e) {
				//FIXME: Error dialog?
				System.out.println("Error completing debug job launch");
			}
			
		} else {
			new RuntimeProcess(launch, job, null);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, 
			IProgressMonitor monitor) throws CoreException {
		if (!(launch instanceof IPLaunch)) {
			abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Invalid_launch_object"), null, 0);
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 250);
		monitor.setTaskName(MessageFormat.format("{0} . . .", new Object[] { "Launching " + configuration.getName() }));
		if (monitor.isCanceled()) {
			return;
		}
		IPTPDebugger debugger = null;
		IPJob job = null;
		
		AttributeManager attrManager = getAttributeManager(configuration);

		try {
			IPreferenceStore store = PTPDebugUIPlugin.getDefault().getPreferenceStore();
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				monitor.subTask("Configuring debug setting . . .");
				
				ArrayList<String> dbgArgs = new ArrayList<String>();
				dbgArgs.add("--host=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_HOST));
				dbgArgs.add("--debugger=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_TYPE));
				
				String dbgPath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_PATH);
				if (dbgPath.length() > 0) {
					dbgArgs.add("--debugger_path=" + dbgPath);
				}
				
				String dbgExtraArgs = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS);
				if (dbgExtraArgs.length() > 0) {
					dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" ")));
				}
				
				 // Create the debugger extension, then the connection point for the debug server. 
				 // The debug server is created when the job is launched via the submitJob() command.
				IPDebugConfiguration debugConfig = getDebugConfig(configuration);
				debugger = debugConfig.getDebugger();
				int timeout = store.getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
				dbgArgs.add("--port=" + debugger.getDebuggerPort(timeout));
			
				// remote setting
				String dbgExePath = getDebuggerExePath(configuration);
				if (dbgExePath == null) {
					dbgExePath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE);
				}
				verifyDebuggerPath(dbgExePath);
				
				IPath path = new Path(dbgExePath);
				attrManager.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
				attrManager.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toOSString()));

				String dbgWD = getDebuggerWorkDirectory(configuration);
				if (dbgWD != null) {
					StringAttribute wdAttr = (StringAttribute) attrManager.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
					if (wdAttr != null) {
						wdAttr.setValueAsString(dbgWD);
					} else {
						attrManager.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD));
					}
					attrManager.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(dbgWD + "/Debug"));
				}
				attrManager.addAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition().create(dbgArgs.toArray(new String[0])));
				attrManager.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));
			}
			
			monitor.worked(10);
			monitor.subTask("Submitting the job . . .");
			
			submitJob(configuration, mode, (IPLaunch)launch, attrManager, debugger, monitor);
			
			monitor.worked(10);
		} catch (CoreException e) {
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				PTPDebugCorePlugin.getDebugModel().shutdownSession(job);
			}
			if (e.getStatus().getCode() != IStatus.CANCEL) {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param viewID
	 */
	protected void showPTPDebugView(final String viewID) {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null && !display.isDisposed()) {
			display.syncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PTPLaunchPlugin.getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							try {
								page.showView(viewID, null, IWorkbenchPage.VIEW_CREATE);
							} catch (PartInitException e) {}
						}
					}
				}
			});
		}
	}
}
