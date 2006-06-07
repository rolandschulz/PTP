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

package org.eclipse.ptp.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.internal.ui.MachineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPUIPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.ptp.ui";

	//The shared instance.
	private static PTPUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	private MachineManager machineManager = null;
	private JobManager jobManager = null;

	private List jobList = Collections.synchronizedList(new ArrayList());
	
	public PTPUIPlugin() {
		super();
		plugin = this;
	}
	public void start(BundleContext context) throws Exception {
		super.start(context);
		machineManager = new MachineManager();
		jobManager = new JobManager();
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		machineManager.shutdown();
		jobManager.shutdown();
		machineManager = null;
		jobManager = null;
		plugin = null;
		resourceBundle = null;
		jobList.clear();
	}
	
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}	
	
	public MachineManager getMachineManager() {
		return machineManager;
	}
	public JobManager getJobManager() {
		return jobManager;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.ui.UIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.ui", path);
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}
    public String getPluginPath() {
        try {
            return Platform.resolve(Platform.getBundle(PLUGIN_ID).getEntry("/")).getPath();
        } catch (IOException e) {
        	return null;
        }
    }	
	
	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}	
	
	public String getCurrentPerspectiveID() {
		return getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
	}
	
	public void addPersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().addPerspectiveListener(listener);
	}
	public void removePersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().removePerspectiveListener(listener);
	}
	
	/***** LOG *****/
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Internal Error", e));
	}
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}		
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException)t).getStatus();
		} else {
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Error within PTP UI: ", t);
			log(status);	
		}
		errorDialog(shell, title, message, status);
	}
	public static void errorDialog(Shell shell, String title, IStatus s) {
		errorDialog(shell, title, s.getMessage(), s);
	}
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		if (s != null && message != null && message.equals(s.getMessage()))
			message = null;

		ErrorDialog.openError(shell, title, message, s);
	}

	public void refreshRuntimeSystem(boolean queue, boolean force) {
		if (queue) {
			refreshRuntimeSystemInQueue(getShell(), false);
		}
		else {
			refreshRuntimeSystemNow(getShell(), force);
		}
	}
	
	private boolean refreshRuntimeSystemNow(Shell shell, final boolean force) {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						PTPCorePlugin.getDefault().getModelPresentation().refreshRuntimeSystems(monitor, force);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			return true;
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			ErrorDialog.openError(shell, UIMessage.REFRESH_SYSTEM_JOB_NAME + " error", e.getMessage(), new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, e.getMessage(), e.getTargetException()));
		}
		return false;
	}

	private void refreshRuntimeSystemInQueue(final Shell shell, final boolean force) {
		synchronized (jobList) {
			//final IJobManager jManager = Platform.getJobManager();
			Job job = new Job(UIMessage.REFRESH_SYSTEM_JOB_NAME) {
				public IStatus run(final IProgressMonitor monitor) {
					if (!monitor.isCanceled()) {
						try {
							PTPCorePlugin.getDefault().getModelPresentation().refreshRuntimeSystems(monitor, force);
						} catch (CoreException e) {
							return e.getStatus();
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.INTERACTIVE);
	        job.addJobChangeListener(jlistener);
			PlatformUI.getWorkbench().getProgressService().showInDialog(shell, job);
			jobList.add(job);
			if (jobList.size() == 1) {
				//make sure each time only run one job
				job.schedule();
			}
		}
	}
	
	private IJobChangeListener jlistener = new IJobChangeListener() {
		public void sleeping(IJobChangeEvent event) {}
		public void scheduled(IJobChangeEvent event) {}
		public void running(IJobChangeEvent event) {}
		public void done(IJobChangeEvent event) {
			synchronized (jobList) {
				Job job = event.getJob();
				if (!job.getResult().isOK()) {
					//remove all jobs if the previous result is not ok
					for (Iterator i=jobList.iterator(); i.hasNext();) {
						job = (Job)i.next();
						job.cancel();
						job.removeJobChangeListener(this);
					}
					jobList.clear();
				}
				else {
					jobList.remove(job);
					job.removeJobChangeListener(this);
					if (jobList.size() > 0) {
						//when finished a job, run another if there are more jobs
						job = (Job)jobList.get(0);
						job.schedule();
					}
				}
			}
		}
        public void awake(IJobChangeEvent event) {}
        public void aboutToRun(IJobChangeEvent event) {}
    };
}
