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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.core.Activator;

public abstract class AbstractToolRuntimeSystemJob extends Job implements IToolRuntimeSystemJob {
	protected String jobID;
	protected IRemoteProcess process = null;
	protected AttributeManager attrMgr;
	protected AbstractToolRuntimeSystem rtSystem;

	public AbstractToolRuntimeSystemJob(String jobID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(name);
		this.attrMgr = attrMgr;
		this.rtSystem = rtSystem;
		this.jobID = jobID;
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

	public AbstractToolRuntimeSystem getRtSystem() {
		return rtSystem;
	}

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
			 * Calculate command and environment.
			 */
			List<String> command = null;
			Map<String,String> environment = null;
			try {
				AttributeManager substitutionAttributeManager = createSubstitutionAttributes();
				environment = doCreateEnvironment(substitutionAttributeManager);
				command = doCreateCommand(substitutionAttributeManager);
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
				processBuilder.environment().putAll(environment);
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
	}


	abstract protected void doExecutionCleanUp();

	abstract protected void doWaitExecution() throws CoreException;

	abstract protected void doExecutionFinished() throws CoreException;

	abstract protected void doExecutionStarted() throws CoreException;

	abstract protected void doBeforeExecution() throws CoreException;

	abstract protected List<String> doCreateCommand(AttributeManager attributeManager) throws CoreException;

	abstract protected Map<String, String> doCreateEnvironment(AttributeManager substitutionAttributeManager) throws CoreException;
	
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
