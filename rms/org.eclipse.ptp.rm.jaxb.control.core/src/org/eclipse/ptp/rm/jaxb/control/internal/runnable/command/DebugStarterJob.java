/******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.internal.runnable.command;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 * Helper class for launching the debugger. The normal calling sequence is:
 * 
 * <pre>
 * IPDebugger debugger = DebugStarterJob.initialize(...);
 * ...
 * launch the debugger back-end job
 * ...
 * DebugStarterJob job = new DebugStarterJob(..., debugger,...);
 * job.schedule();
 * ...
 * launch the debugger front-end
 * ...
 * job.join()
 * if (!job.getResult().isOk()) {
 *    ... deal with errors ...
 * }
 * </pre>
 * 
 * @author greg
 * 
 */
public class DebugStarterJob extends Job {

	/**
	 * Initialize the debugger. This must be called prior to starting the job.
	 * 
	 * @param configuration
	 *            launch configuation
	 * @param monitor
	 *            progress monitor
	 * @return IPDebugger used when creating the starter job
	 * @throws CoreException
	 */
	public static IPDebugger initialize(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor);
		try {
			IPDebugConfiguration debugConfig = PTPDebugCorePlugin.getDefault().getDebugConfiguration(
					configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String) null));
			IPDebugger debugger = debugConfig.getDebugger();
			debugger.initialize(configuration, subMon.newChild(10));
			return debugger;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Mark the job as completed in the debugger UI. Should be called once the actual job has completed.
	 * 
	 * @param jobId
	 *            job ID of the job
	 */
	public static void terminate(String jobId) {
		IPJob job = fJobsMap.get(jobId);
		if (job != null) {
			job.getAttribute(JobAttributes.getStateAttributeDefinition()).setValue(JobAttributes.State.COMPLETED);
		}
	}

	private static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String name = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		if (name != null) {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		}
		return null;
	}

	private final ILaunchConfiguration fConfiguration;
	private final IPDebugger fDebugger;
	private final String fJobId;
	private final IVariableMap fVars;
	private final IResourceManager fRM;

	private static final Map<String, IPJob> fJobsMap = Collections.synchronizedMap(new HashMap<String, IPJob>());

	public DebugStarterJob(ILaunchConfiguration configuration, IPDebugger debugger, String jobId, IVariableMap vars,
			IResourceManager rm) {
		super("Debug Starter for job " + jobId); //$NON-NLS-1$
		fConfiguration = configuration;
		fDebugger = debugger;
		fJobId = jobId;
		fVars = vars;
		fRM = rm;
	}

	private void createDebugModel(String jobId, IVariableMap vars) {
		IPResourceManager prm = (IPResourceManager) fRM.getAdapter(IPResourceManager.class);
		AttributeManager attrMgr = new AttributeManager();

		attrMgr.addAttribute(JobAttributes.getJobIdAttributeDefinition().create(jobId));
		attrMgr.addAttribute(JobAttributes.getStateAttributeDefinition().create(JobAttributes.State.RUNNING));
		attrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));

		IPJob job = prm.newJob(jobId, attrMgr);
		fJobsMap.put(jobId, job);

		attrMgr = new AttributeManager();
		attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));

		AttributeType attr = (AttributeType) vars.get(JAXBControlConstants.MPI_NUMPROCS);
		String numProcsStr = String.valueOf(attr.getValue());
		int numProcs = Integer.parseInt(numProcsStr);
		BitSet procRanks = new BitSet(numProcs);
		procRanks.set(0, numProcs, true);
		job.addProcessesByJobRanks(procRanks, attrMgr);

		prm.addJobs(null, Arrays.asList(job));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			IPLaunch launch = (IPLaunch) fConfiguration.getAdapter(IPLaunch.class);
			if (launch != null) {
				launch.setJobId(fJobId);
				IProject project = getProject(fConfiguration);
				if (project != null) {
					/*
					 * Create a debug model for the ParallelDebugView
					 */
					createDebugModel(fJobId, fVars);

					IPSession session = PTPDebugCorePlugin.getDebugModel().createDebugSession(fDebugger, launch, project,
							subMon.newChild(2));

					if (subMon.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					/*
					 * NOTE: we assume these have already been verified prior to launch
					 */
					PropertyType p = (PropertyType) fVars.get(JAXBControlConstants.EXEC_PATH);
					String app = new Path((String) p.getValue()).lastSegment();
					p = (PropertyType) fVars.get(JAXBControlConstants.EXEC_DIR);
					String path = (String) p.getValue();
					p = (PropertyType) fVars.get(JAXBControlConstants.DIRECTORY);
					String cwd = (String) p.getValue();
					p = (PropertyType) fVars.get(JAXBControlConstants.PROG_ARGS);
					String[] args = new String[0];
					if (p != null) {
						args = new ArgumentParser((String) p.getValue()).getTokenArray();
					}

					session.connectToDebugger(subMon.newChild(8), app, path, cwd, args);
				}
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}