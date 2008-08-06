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
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.Activator;
import org.eclipse.ptp.rm.core.utils.DebugUtil;

public abstract class AbstractToolRuntimeSystemJob extends Job implements IToolRuntimeSystemJob {
	protected String jobID;
	protected String queueID;
	protected IRemoteProcess process = null;
	protected AttributeManager attrMgr;
	protected AbstractToolRuntimeSystem rtSystem;

	public AbstractToolRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(name);
		this.attrMgr = attrMgr;
		this.rtSystem = rtSystem;
		this.jobID = jobID;
		this.queueID = queueID;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IToolRuntimeSystemJob.class) {
			return (IToolRuntimeSystemJob) this;
		}
		return super.getAdapter(adapter);
	}

	public String getQueueID() {
		return queueID;
	}
	
	public String getJobID() {
		return jobID;
	}

	public AbstractToolRuntimeSystem getRtSystem() {
		return rtSystem;
	}

	public AttributeManager getAttrMgr() {
		return attrMgr;
	}

	protected void changeJobState(JobAttributes.State newState) {
		EnumeratedAttribute<JobAttributes.State> state = JobAttributes.getStateAttributeDefinition().create(newState);
		AttributeManager attrManager = new AttributeManager();
		attrManager.addAttribute(state);
		rtSystem.changeJob(jobID, attrManager);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		changeJobState(JobAttributes.State.STARTED);

		try {
			/*
			 * Calculate command and environment.
			 */
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "About to run RTS job #{0}.", jobID); //$NON-NLS-1$
			List<String> command = null;
			Map<String,String> environment = null;
			String directory = null;
			try {
				AttributeManager substitutionAttributeManager = createSubstitutionAttributes();
				environment = doCreateEnvironment(substitutionAttributeManager);
				directory = coCreateWorkingDirectory();
				BooleanAttribute debugAttr = attrMgr.getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
				if (debugAttr != null && debugAttr.getValue()) {
					command = doCreateDebugCommand(substitutionAttributeManager);
				} else {
					command = doCreateLaunchCommand(substitutionAttributeManager);
				}
				if (DebugUtil.RTS_JOB_TRACING) {
					System.out.println("Available substitution macros:"); //$NON-NLS-1$
					for (IAttribute<?, ?, ?> attr : substitutionAttributeManager.getAttributes()) {
						System.out.println(MessageFormat.format("  {0}={1}", attr.getDefinition().getId(), attr.getValueAsString())); //$NON-NLS-1$
					}
					System.out.println("Environment variables:"); //$NON-NLS-1$
					for (Entry<String, String> env : environment.entrySet()) {
						System.out.println(MessageFormat.format("  {0}={1}", env.getKey(), env.getValue())); //$NON-NLS-1$
					}
					System.out.println(MessageFormat.format("Workdir: {0}", directory)); //$NON-NLS-1$
					System.out.println(MessageFormat.format("Command: {0}", command.toString())); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to caculate command line for launch.", e);
			}

			try {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: handle prepare", jobID); //$NON-NLS-1$
				doBeforeExecution();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed before launch.", e);
			}

			/*
			 * Execute remote command for the job.
			 */
			try {
				IRemoteProcessBuilder processBuilder = rtSystem.createProcessBuilder(command, directory);
				processBuilder.environment().putAll(environment);
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: start", jobID); //$NON-NLS-1$
				process = processBuilder.start();
			} catch (IOException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to execute command.", e);
			}

			try {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: handle start", jobID); //$NON-NLS-1$
				doExecutionStarted();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed after launch.", e);
			}

			changeJobState(JobAttributes.State.RUNNING);

			try {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: wait to finish", jobID); //$NON-NLS-1$
				doWaitExecution();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed while waiting execution of command.", e);
			}

			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: exit value {1}", jobID, process.exitValue()); //$NON-NLS-1$
			if (process.exitValue() != 0) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), NLS.bind("Failed to run command, return exit value {0}.", process.exitValue()));
			}

//			try {
//				DebugUtil.trace(DebugUtil.COMMAND_TRACING, "RTS job #{0}: wait to finish", jobID); //$NON-NLS-1$
//				process.waitFor();
//			} catch (InterruptedException e) {
//				changeJobState(JobAttributes.State.ERROR);
//				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed while terminating the command.", e);
//			}

			try {
				DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: handle finish", jobID); //$NON-NLS-1$
				doExecutionFinished();
			} catch (CoreException e) {
				changeJobState(JobAttributes.State.ERROR);
				return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed after command finished.", e);
			}

			changeJobState(JobAttributes.State.TERMINATED);

			return new Status(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), NLS.bind("Command successfull, return exit value {0}.", process.exitValue()));

		} finally {
			DebugUtil.trace(DebugUtil.RTS_JOB_TRACING_MORE, "RTS job #{0}: cleanup", jobID); //$NON-NLS-1$
			final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(getQueueID()).getJobById(getJobID());
			switch (ipJob.getState()) {
			case TERMINATED:
			case ERROR:
				break;
			case PENDING:
			case RUNNING:
			case STARTED:
			case SUSPENDED:
			case UNKNOWN:
				changeJobState(JobAttributes.State.TERMINATED);
				break;
			}
			doExecutionCleanUp();
		}
	}


	abstract protected void doExecutionCleanUp();

	abstract protected void doWaitExecution() throws CoreException;

	abstract protected void doExecutionFinished() throws CoreException;

	abstract protected void doExecutionStarted() throws CoreException;

	abstract protected void doBeforeExecution() throws CoreException;

	abstract protected List<String> doCreateLaunchCommand(AttributeManager attributeManager) throws CoreException;

	abstract protected List<String> doCreateDebugCommand(AttributeManager attributeManager) throws CoreException;

	abstract protected Map<String, String> doCreateEnvironment(AttributeManager substitutionAttributeManager) throws CoreException;

	abstract protected String coCreateWorkingDirectory();

	protected AttributeManager createSubstitutionAttributes() throws CoreException {
		AttributeManager newAttributeManager = new AttributeManager(getAttrMgr().getAttributes());
//		AttributeManager newAttributeManager = new AttributeManager();
//		for (IAttribute<?, ?, ?> attribute : attributes) {
//			newAttributeManager.addAttribute(attribute.getDefinition().create(attribute.getValueAsString()));
//		}
		IAttribute<?,?,?> extrAttributes[] = getExtraSubstitutionVariables();
		newAttributeManager.addAttributes(extrAttributes);
		for (IAttributeDefinition<?, ?, ?> attributeDefinition : getDefaultSubstitutionAttributes()) {
			IAttribute<?, ?, ?> attribute = newAttributeManager.getAttribute(attributeDefinition.getId());
			if (attribute == null) {
				// Create one with default value
				try {
					newAttributeManager.addAttribute(attributeDefinition.create());
				} catch (IllegalValueException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), NLS.bind("Failed to create default attribute for {0}.", attributeDefinition.getName()), e));
				}
			}
		}
		return newAttributeManager;
	}

	abstract protected IAttribute<?, ?, ?>[] getExtraSubstitutionVariables() throws CoreException;

	abstract protected IAttributeDefinition<?, ?, ?>[] getDefaultSubstitutionAttributes();

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
