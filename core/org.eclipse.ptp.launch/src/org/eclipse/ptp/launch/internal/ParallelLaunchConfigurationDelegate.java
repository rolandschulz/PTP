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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
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
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
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
			launch.setPJob(job);
			try {
				setDefaultSourceLocator(launch, configuration);
				final IProject project = verifyProject(configuration);
				final IPath execPath = verifyExecutablePath(configuration);
				
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IRunnableWithProgress runnable = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								if (monitor.isCanceled())
									throw new InterruptedException("The job is cancalled."); //$NON-NLS-1$
								try {
									long timeout = PTPDebugUIPlugin.getDefault().getPreferenceStore().getLong(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
									//Wait for the incoming debug server connection. This can be canceled by the user.
									PTPDebugCorePlugin.getDebugModel().createDebugSession(timeout, debugger, launch, project, execPath, monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
							}
						};
						try {
							new ProgressMonitorDialog(PTPLaunchPlugin.getActiveWorkbenchShell()).run(true, true, runnable);
						} catch (InterruptedException e) {
							System.out.println("Error completing debug job launch: " + e.getMessage()); //$NON-NLS-1$
						} catch (InvocationTargetException e) {
							System.out.println("Error completing debug job launch: " + e.getMessage()); //$NON-NLS-1$
						}
					}
				});
			} catch (CoreException e) {
				//FIXME: Error dialog?
				System.out.println("Error completing debug job launch"); //$NON-NLS-1$
			}
			
		} else {
			new RuntimeProcess(launch, job, null);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (!(launch instanceof IPLaunch)) {
			abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Invalid_launch_object"), null, 0); //$NON-NLS-1$
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 250);
		monitor.setTaskName(MessageFormat.format("{0} . . .", new Object[] { "Launching " + configuration.getName() })); //$NON-NLS-1$ $NON-NLS-2$
		if (monitor.isCanceled()) {
			return;
		}
		IPTPDebugger debugger = null;
		IPJob job = null;
		
		//switch perspective
		switchPerspective(DebugUITools.getLaunchPerspective(configuration.getType(), mode));
		AttributeManager attrManager = getAttributeManager(configuration);
		try {
			IPreferenceStore store = PTPDebugUIPlugin.getDefault().getPreferenceStore();
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				// show ptp debug view
				showPTPDebugView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
				monitor.subTask("Configuring debug setting . . ."); //$NON-NLS-1$
				
				/*
				 * FIXME: all this code needs to be moved to the debug.external.core plugin and
				 * made into an interface.
				 */
				
				 // Create the debugger extension, then the connection point for the debug server. 
				 // The debug server is created when the job is launched via the submitJob() command.
				IPDebugConfiguration debugConfig = getDebugConfig(configuration);
				debugger = debugConfig.getDebugger();
				int port = debugger.getDebuggerPort(store.getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT));
				

				/*
				 * Work out the address and port to supply as arguments to the debug server. There are currently
				 * two cases:
				 * 
				 * 1. If port forwarding is enabled, then the address needs to be the address of the host that is 
				 * running the proxy (since this is where the tunnel begins), but accessible from the machine running 
				 * the debug server. Since the debug server machine may be on a local network (e.g. a node in a 
				 * cluster), it will typically NOT be the same address that is used to start the proxy. The port
				 * is a port that will be forwarded from the proxy machine back to the debugger port that
				 * was allocated on the local machine.
				 * 
				 * 2. If port forwarding is not enabled, then the address will be the address of the host running 
				 * Eclipse), and the port allocated when the debugger was created. NOTE: this assumes that the machine 
				 * running the debug server can contact the local host directly. In the case of the SDM, the "master" 
				 * debug server process can potentially run on any node in the cluster. In many environments, compute 
				 * nodes cannot communicate outside their local network.
				 * 
				 * In both cases, the address is obtained from the debugger page in the launch configuration, since
				 * in general it will be different for each connection. It is initialized to either the connection host
				 * address (port forwarding) or the local address (no port forwarding).
				 */
				String localAddress = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_HOST);
				IResourceManagerControl rm = (IResourceManagerControl)getResourceManager(configuration);
				if (rm != null) {
					IResourceManagerConfiguration conf = rm.getConfiguration();
					if (conf instanceof AbstractRemoteResourceManagerConfiguration) {
						AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
						if (remConf.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
							IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
							if (remoteServices == null) {
								abort(LaunchMessages.getFormattedResourceString("ParallelLaunchConfigurationDelegate.Invalid_remote_services_ID", //$NON-NLS-1$
										remConf.getRemoteServicesId()), null, 0);
							}
							IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
							IRemoteConnection connection = connMgr.getConnection(remConf.getConnectionName());
							if (connection == null) {
								abort(LaunchMessages.getFormattedResourceString("ParallelLaunchConfigurationDelegate.No_such_connection", //$NON-NLS-1$
										remConf.getConnectionName()), null, 0);
							}
							localAddress = "localhost"; //$NON-NLS-1$
							try {
								port = connection.forwardRemotePort(localAddress, port, monitor);
							} catch (RemoteConnectionException e) {
								abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Unable_to_forward_port"), e, 0); //$NON-NLS-1$
							}
							if (monitor.isCanceled()) {
								return;
							}
						} else {
							localAddress = remConf.getLocalAddress();
						}
					}
				}
				
				ArrayList<String> dbgArgs = new ArrayList<String>();
				dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
				dbgArgs.add("--debugger=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_TYPE)); //$NON-NLS-1$
				
				String dbgPath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_PATH);
				if (dbgPath.length() > 0) {
					dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
				}
				
				String dbgExtraArgs = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS);
				if (dbgExtraArgs.length() > 0) {
					dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" "))); //$NON-NLS-1$
				}
				
				dbgArgs.add("--port=" + port); //$NON-NLS-1$
			
				// remote setting
				String dbgExePath = getDebuggerExePath(configuration);
				if (dbgExePath == null) {
					dbgExePath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE);
				}
				verifyDebuggerPath(dbgExePath, configuration);
				
				IPath path = new Path(dbgExePath);
				attrManager.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
				attrManager.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toString()));

				String dbgWD = getDebuggerWorkDirectory(configuration);
				if (dbgWD != null) {
					StringAttribute wdAttr = (StringAttribute) attrManager.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
					if (wdAttr != null) {
						wdAttr.setValueAsString(dbgWD);
					} else {
						attrManager.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD));
					}
					attrManager.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(dbgWD + "/Debug")); //$NON-NLS-1$
				}
				attrManager.addAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition().create(dbgArgs.toArray(new String[0])));
				attrManager.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));
			}
			
			monitor.worked(10);
			monitor.subTask("Submitting the job . . ."); //$NON-NLS-1$
			
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
	 * Show the PTP Debug view
	 * 
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
	
	
	/**
	 * Used to force switching to the PTP Debug perspective
	 * 
	 * @param perspectiveID
	 */
	protected void switchPerspective(final String perspectiveID) {
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
			                if (page.getPerspective().getId().equals(perspectiveID))
			                    return;

							try {
				                window.getWorkbench().showPerspective(perspectiveID, window);
			                } catch (WorkbenchException e) { }
						}
					}
	            }
	        });
		}
	}
}
