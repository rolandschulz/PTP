/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rtsystem;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.Activator;

public abstract class AbstractToolRuntimeSystemJob extends Job implements IToolRuntimeSystemJob {
//	protected String jobSubID;
	protected String jobID;
//	protected String name;
//	protected String user;
	protected IRemoteProcess process = null;
	protected AttributeManager attrMgr;
	protected AbstractToolRuntimeSystem rtSystem;
	
	public AbstractToolRuntimeSystemJob(String jobID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(name);
		this.attrMgr = attrMgr;
		this.rtSystem = rtSystem;
//		this.name = name;
//		this.user = System.getenv("USER");
		this.jobID = jobID;
//		this.jobSubID = jobSubID;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IToolRuntimeSystemJob.class) {
			return (IToolRuntimeSystemJob) this;
		}
		return super.getAdapter(adapter);
	}

	public String getJobID() {
		return jobID;
	}
//
//	public String getJobSubID() {
//		return jobSubID;
//	}
//
//	public String getJobName() {
//		return name;
//	}
//	
//	public String getUser() {
//		return user;
//	};

	public AbstractToolRuntimeSystem getRtSystem() {
		return rtSystem;
	}

//	public AbstractToolRMConfiguration getRMConfiguration() {
//		return getRtSystem().rmConfiguration;
//	}

	public AttributeManager getAttrMgr() {
		return attrMgr;
	}

	protected void changeJobState(JobAttributes.State newState) {
		EnumeratedAttribute<JobAttributes.State> state = JobAttributes.getStateAttributeDefinition().create(newState);
		rtSystem.changeJobAttributes(jobID, state);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		changeJobState(JobAttributes.State.STARTED);
		
		try {
			/*
			 * Calculate command.
			 */
			List<String> command = null;
			try {
				command = doCreateCommand();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to caculate command line for launch.", e);
			}

			try {
				doBeforeExecution();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed before launch.", e);
			}

			/*
			 * Execute remote command for the job.
			 */
			try {
				IRemoteProcessBuilder processBuilder = rtSystem.createProcessBuilder(command);
				process = processBuilder.start();
			} catch (IOException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to execute command.", e);
			}

			changeJobState(JobAttributes.State.RUNNING);

			try {
				doExecutionStarted();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed after launch.", e);
			}

			try {
				doWaitExecution();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed while waiting execution of command.", e);
			}

			if (process.exitValue() != 0) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), NLS.bind("Failed to run command, return exit value {0}.", process.exitValue()));
			}

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed while terminating the command.", e);
			}

			try {
				doExecutionFinished();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed after command finished.", e);
			}

			changeJobState(JobAttributes.State.TERMINATED);

			return new Status(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), NLS.bind("Command successfull, return exit value {0}.", process.exitValue()));

		} finally { 
			doExecutionCleanUp();
		}

//		IToolRuntimeSystemJob job = rtSystem.jobs.get(jobID);
//		rtSystem.runningJobQueue.remove(job);
//		rtSystem.jobs.remove(job);

//		state.setValue(JobAttributes.State.RUNNING);
//		rtSystem.changeJobAttributes(jobID, state, nprocs);
//		rtSystem.runningJobQueue.add(this);


		/*
		 * Get attributes required to start job. The launch command contains the
		 * launcher executable and its arguments. The application is specified
		 * by a place holder.
		 * TODO: use variable substitution for the place holder.
		 * TODO: implement program path with place holder.
		 * TODO: implement program arguments with place holder.
		 * TODO: append remote installation path to launch command
		 */
	}

	abstract protected void doExecutionCleanUp();

	abstract protected void doWaitExecution() throws CoreException;

	abstract protected void doExecutionFinished() throws CoreException;

	abstract protected void doExecutionStarted() throws CoreException;

	abstract protected void doBeforeExecution() throws CoreException;

	abstract protected List<String> doCreateCommand() throws CoreException;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.rm.core.rtsystem.IToolRuntimeSystemJob#terminate()
	 */
	public void terminate() {
		if (process != null) {
			process.destroy();
		}
		doTerminateJob();
	}

	abstract protected void doTerminateJob();

	@Override
	protected void canceling() {
		terminate();
		super.canceling();
	}
}
