/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California.
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
package org.eclipse.ptp.rmsystem;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.AbstractJobSubmission;
import org.eclipse.ptp.core.AbstractJobSubmission.JobSubStatus;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes.State;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.rtsystem.IRuntimeEventListener;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRMChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveAllEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.utils.core.RangeSet;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author greg
 * 
 */
public abstract class AbstractRuntimeResourceManager extends AbstractResourceManager implements IRuntimeEventListener {

	private class JobSubmission extends AbstractJobSubmission {
		private ILaunchConfiguration configuration;
		private IPJob job = null;
		private String reason;

		public JobSubmission(int count) {
			super(count);
		}

		public JobSubmission(String id) {
			super(id);
		}

		/**
		 * @return the reason for the error
		 */
		public String getErrorReason() {
			return reason;
		}

		/**
		 * @return the job
		 */
		public IPJob getJob() {
			return job;
		}

		/**
		 * @return the configuration
		 */
		public ILaunchConfiguration getLaunchConfiguration() {
			return configuration;
		}

		/**
		 * @param job
		 *            the job to set
		 */
		public void setJob(IPJob job) {
			this.job = job;
		}

		/**
		 * @param configuaration
		 *            the configuration to set
		 */
		public void setLaunchConfiguration(ILaunchConfiguration configuration) {
			this.configuration = configuration;
		}
	}

	private final Map<String, JobSubmission> jobSubmissions = Collections.synchronizedMap(new HashMap<String, JobSubmission>());
	private IRuntimeSystem runtimeSystem;
	private volatile int jobSubIdCounter = 0;

	/**
	 * @since 5.0
	 */
	public AbstractRuntimeResourceManager(IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(universe, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent)
	 * 
	 * Note: this allows redefinition of attribute definitions. This is ok as
	 * long as they are only allowed during the initialization phase.
	 */
	public void handleEvent(IRuntimeAttributeDefinitionEvent e) {
		for (IAttributeDefinition<?, ?, ?> attr : e.getDefinitions()) {
			getAttributeDefinitionManager().setAttributeDefinition(attr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeConnectedStateEvent)
	 */
	public void handleEvent(IRuntimeConnectedStateEvent e) {
		// Ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeErrorStateEvent)
	 */
	public void handleEvent(IRuntimeErrorStateEvent e) {
		/*
		 * Fatal error in the runtime system. Cancel any pending job submissions
		 * and inform upper levels of the problem.
		 */
		synchronized (jobSubmissions) {
			for (JobSubmission sub : jobSubmissions.values()) {
				sub.setError(Messages.AbstractRuntimeResourceManager_6);
			}
			jobSubmissions.clear();
		}

		setState(ResourceManagerAttributes.State.ERROR);
		fireError(Messages.AbstractRuntimeResourceManager_6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeJobChangeEvent)
	 */
	public void handleEvent(IRuntimeJobChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		List<IPJobControl> changedJobs = new ArrayList<IPJobControl>();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet jobIds = mgrEntry.getKey();

			for (String elementId : jobIds) {
				IPJobControl job = getJobControl(elementId);
				if (job != null) {
					changedJobs.add(job);
				} else {
					PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_7 + elementId);
				}
			}

			doUpdateJobs(changedJobs, attrs);
			changedJobs.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeMachineChangeEvent)
	 */
	public void handleEvent(IRuntimeMachineChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		List<IPMachineControl> machines = new ArrayList<IPMachineControl>();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet machineIds = entry.getKey();

			for (String elementId : machineIds) {
				IPMachineControl machine = getMachineControl(elementId);
				if (machine != null) {
					machines.add(machine);
				} else {
					System.out.println(Messages.AbstractRuntimeResourceManager_8 + elementId);
				}
			}

			doUpdateMachines(machines, attrs);
			machines.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeErrorEvent)
	 */
	public void handleEvent(IRuntimeMessageEvent e) {
		// MessageAttributes.Level level = e.getLevel();
		int severity = IStatus.ERROR;
		switch (e.getLevel()) {
		case DEBUG:
		case INFO:
			severity = IStatus.INFO;
			break;
		case FATAL: /* should this map to cancel instead? */
		case ERROR:
		case UNDEFINED:
			severity = IStatus.ERROR;
			break;
		case WARNING:
			severity = IStatus.WARNING;
		}
		StatusManager.getManager().handle(new Status(severity, PTPCorePlugin.PLUGIN_ID, e.getText()),
				(severity == IStatus.ERROR) ? StatusManager.SHOW : StatusManager.LOG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewJobEvent)
	 */
	public void handleEvent(IRuntimeNewJobEvent e) {
		IPQueueControl queue = getQueueControl(e.getParentId());

		ElementAttributeManager mgr = e.getElementAttributeManager();
		Collection<IPJobControl> newJobs = new ArrayList<IPJobControl>();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager jobAttrs = entry.getValue();

			RangeSet jobIds = entry.getKey();

			for (String elementId : jobIds) {
				IPJobControl job = getJobControl(elementId);
				if (job == null) {
					job = doCreateJob(elementId, jobAttrs);
					newJobs.add(job);

					StringAttribute jobSubAttr = jobAttrs.getAttribute(JobAttributes.getSubIdAttributeDefinition());
					if (jobSubAttr != null) {
						/*
						 * Notify any submitJob() calls that the job has been
						 * created
						 */

						JobSubmission sub;
						synchronized (jobSubmissions) {
							sub = jobSubmissions.remove(jobSubAttr.getValue());
						}
						if (sub != null) {
							sub.setJob(job);
							job.setLaunchConfiguration(sub.getLaunchConfiguration());
							sub.setStatus(JobSubStatus.SUBMITTED);
						}
					}
				}
			}

		}
		addJobs(queue, newJobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewMachineEvent)
	 */
	public void handleEvent(IRuntimeNewMachineEvent e) {
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet machineIds = entry.getKey();
			List<IPMachineControl> newMachines = new ArrayList<IPMachineControl>(machineIds.size());

			for (String elementId : machineIds) {
				IPMachineControl machine = getMachineControl(elementId);
				if (machine == null) {
					machine = doCreateMachine(elementId, attrs);
					newMachines.add(machine);
				}
			}

			addMachines(newMachines);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewNodeEvent)
	 */
	public void handleEvent(IRuntimeNewNodeEvent e) {
		IPMachineControl machine = getMachineControl(e.getParentId());

		if (machine != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
			Collection<IPNodeControl> newNodes = new ArrayList<IPNodeControl>();

			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet nodeIds = entry.getKey();

				for (String elementId : nodeIds) {
					IPNodeControl node = getNodeControl(elementId);
					if (node == null) {
						node = doCreateNode(machine, elementId, attrs);
						newNodes.add(node);
					}
				}

			}
			addNodes(machine, newNodes);
		} else {
			PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_1 + e.getParentId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewProcessEvent)
	 */
	public void handleEvent(IRuntimeNewProcessEvent e) {
		final String jobId = e.getParentId();
		final IPJobControl job = getJobControl(jobId);

		if (job != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();

			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet processJobRanks = entry.getKey();
				BitSet newProcessJobRanks = getProcessJobRanks(processJobRanks);

				addProcessesByJobRanks(job, newProcessJobRanks, attrs);
			}
		} else {
			PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_2 + e.getParentId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewQueueEvent)
	 */
	public void handleEvent(IRuntimeNewQueueEvent e) {
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet queueIds = entry.getKey();
			List<IPQueueControl> newQueues = new ArrayList<IPQueueControl>(queueIds.size());

			for (String elementId : queueIds) {
				IPQueueControl queue = getQueueControl(elementId);
				if (queue == null) {
					queue = doCreateQueue(elementId, attrs);
					newQueues.add(queue);
				}
			}

			addQueues(newQueues);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNodeChangeEvent)
	 */
	public void handleEvent(IRuntimeNodeChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		Map<IPMachineControl, List<IPNodeControl>> map = new HashMap<IPMachineControl, List<IPNodeControl>>();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet nodeIds = mgrEntry.getKey();
			List<IPNodeControl> changedNodes;

			for (String elementId : nodeIds) {
				IPNodeControl node = getNodeControl(elementId);
				if (node != null) {
					IPMachineControl machine = node.getMachineControl();
					changedNodes = map.get(machine);
					if (changedNodes == null) {
						changedNodes = new ArrayList<IPNodeControl>();
						map.put(machine, changedNodes);
					}
					changedNodes.add(node);
				} else {
					PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_9 + elementId);
				}
			}

			for (Map.Entry<IPMachineControl, List<IPNodeControl>> entry : map.entrySet()) {
				doUpdateNodes(entry.getKey(), entry.getValue(), attrs);
			}

			map.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeProcessChangeEvent)
	 */
	public void handleEvent(IRuntimeProcessChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		String jobId = e.getJobId();
		IPJobControl job = getJobControl(jobId);

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet processJobRanks = mgrEntry.getKey();

			// convert the RangeSet to a BitSet
			BitSet changedProcessesJobRanks = getProcessJobRanks(processJobRanks);

			doUpdateProcesses(job, changedProcessesJobRanks, attrs);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeQueueChangeEvent)
	 */
	public void handleEvent(IRuntimeQueueChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		List<IPQueueControl> queues = new ArrayList<IPQueueControl>();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet queueIds = entry.getKey();

			for (String elementId : queueIds) {
				IPQueueControl queue = getQueueControl(elementId);
				if (queue != null) {
					queues.add(queue);
				} else {
					PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_3 + elementId);
				}
			}

			doUpdateQueues(queues, attrs);
			queues.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IRuntimeRemoveAllEvent e) {
		cleanUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e) {
		Set<IPJobControl> removedJobs = new HashSet<IPJobControl>();

		for (String elementId : e.getElementIds()) {
			IPJobControl job = getJobControl(elementId);
			if (job != null) {
				removedJobs.add(job);
			}
		}

		removeJobs(removedJobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e) {
		Map<IPResourceManager, List<IPMachineControl>> map = new HashMap<IPResourceManager, List<IPMachineControl>>();

		for (String elementId : e.getElementIds()) {
			IPMachineControl machine = getMachineControl(elementId);
			if (machine != null) {
				IPResourceManager rm = (IPResourceManager) machine.getParent();
				List<IPMachineControl> machines = map.get(rm);
				if (machines == null) {
					machines = new ArrayList<IPMachineControl>();
					map.put(rm, machines);
				}
				machines.add(machine);
			}
		}

		for (Map.Entry<IPResourceManager, List<IPMachineControl>> entry : map.entrySet()) {
			removeMachines(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e) {
		Map<IPMachineControl, List<IPNodeControl>> map = new HashMap<IPMachineControl, List<IPNodeControl>>();

		for (String elementId : e.getElementIds()) {
			IPNodeControl node = getNodeControl(elementId);
			if (node != null) {
				IPMachineControl machine = node.getMachineControl();
				List<IPNodeControl> nodes = map.get(machine);
				if (nodes == null) {
					nodes = new ArrayList<IPNodeControl>();
					map.put(machine, nodes);
				}
				nodes.add(node);
			}
		}

		for (Map.Entry<IPMachineControl, List<IPNodeControl>> entry : map.entrySet()) {
			removeNodes(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(IRuntimeRemoveProcessEvent e) {
		final RangeSet jobRanks = e.getProcessJobRanks();
		final BitSet removedProcessJobRanks = getProcessJobRanks(jobRanks);

		IPJobControl job = getJobControl(e.getJobId());

		job.removeProcessesByJobRanks(removedProcessJobRanks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(IRuntimeRemoveQueueEvent e) {
		Map<IPResourceManager, List<IPQueueControl>> map = new HashMap<IPResourceManager, List<IPQueueControl>>();

		for (String elementId : e.getElementIds()) {
			IPQueueControl queue = getQueueControl(elementId);
			if (queue != null) {
				IPResourceManager rm = (IPResourceManager) queue.getParent();
				List<IPQueueControl> queues = map.get(rm);
				if (queues == null) {
					queues = new ArrayList<IPQueueControl>();
					map.put(rm, queues);
				}
				queues.add(queue);
			}
		}

		for (Map.Entry<IPResourceManager, List<IPQueueControl>> entry : map.entrySet()) {
			removeQueues(entry.getKey(), entry.getValue());
		}
	}

	public void handleEvent(IRuntimeRMChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet rmIds = mgrEntry.getKey();

			for (String elementId : rmIds) {
				if (getID().equals(elementId)) {
					doUpdateRM(attrs);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRunningStateEvent)
	 */
	public void handleEvent(IRuntimeRunningStateEvent e) {
		ResourceManagerAttributes.State state = State.STARTED;

		try {
			runtimeSystem.startEvents();
		} catch (CoreException ex) {
			state = State.ERROR;
			fireError(ex.getMessage());
		}

		setState(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleEvent(IRuntimeShutdownStateEvent e) {
		setState(ResourceManagerAttributes.State.STOPPED);
		cleanUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e) {
		setState(ResourceManagerAttributes.State.ERROR);
		fireError(e.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		if (e.getJobSubID() != null) {
			JobSubmission sub;
			synchronized (jobSubmissions) {
				sub = jobSubmissions.remove(e.getJobSubID());
			}
			if (sub != null) {
				sub.setError(e.getErrorMessage());
			}
		}
		fireSubmitJobError(e.getJobSubID(), e.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(IRuntimeTerminateJobErrorEvent e) {
		IPJob job = this.getJobControl(e.getJobID());
		String name = e.getJobID();
		if (job != null) {
			name = job.getName();
		}
		fireError(NLS.bind(Messages.AbstractRuntimeResourceManager_4, new Object[] { name, e.getErrorMessage() }));
	}

	/**
	 * 
	 */
	protected abstract void doAfterCloseConnection();

	/**
	 * 
	 */
	protected abstract void doAfterOpenConnection();

	/**
	 * 
	 */
	protected abstract void doBeforeCloseConnection();

	/**
	 * 
	 */
	protected abstract void doBeforeOpenConnection();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doCleanUp()
	 */
	@Override
	protected void doCleanUp() {
		/*
		 * Cancel any pending job submissions.
		 */
		synchronized (jobSubmissions) {
			for (JobSubmission sub : jobSubmissions.values()) {
				sub.setStatus(JobSubStatus.CANCELLED);
			}
			jobSubmissions.clear();
		}
	}

	/**
	 * Template pattern method to actually create the job.
	 * 
	 * @param queue
	 * @param jobId
	 * @return
	 * @since 5.0
	 */
	protected abstract IPJobControl doCreateJob(String jobId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the machine.
	 * 
	 * @param machineId
	 * @return
	 */
	protected abstract IPMachineControl doCreateMachine(String machineId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the node.
	 * 
	 * @param machine
	 * @param nodeId
	 * @return
	 */
	protected abstract IPNodeControl doCreateNode(IPMachineControl machine, String nodeId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the queue.
	 * 
	 * @param queueId
	 * @return
	 */
	protected abstract IPQueueControl doCreateQueue(String queueId, AttributeManager attrs);

	/**
	 * create a new runtime system
	 * 
	 * @return the new runtime system
	 * @throws CoreException
	 *             TODO
	 */
	protected abstract IRuntimeSystem doCreateRuntimeSystem() throws CoreException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDisableEvents()
	 */
	@Override
	protected void doDisableEvents() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doEnableEvents()
	 */
	@Override
	protected void doEnableEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	protected List<IPJobControl> doRemoveTerminatedJobs() {
		List<IPJobControl> terminatedJobs = new ArrayList<IPJobControl>();

		for (IPJobControl job : getJobControls()) {
			if (job.getState() == JobAttributes.State.COMPLETED) {
				terminatedJobs.add(job);
			}
		}
		removeJobs(terminatedJobs);

		return terminatedJobs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		doBeforeCloseConnection();

		runtimeSystem.shutdown();

		doAfterCloseConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		monitor.subTask(Messages.AbstractRuntimeResourceManager_11);

		runtimeSystem = doCreateRuntimeSystem();

		if (monitor.isCanceled()) {
			return;
		}

		monitor.worked(10);
		monitor.subTask(Messages.AbstractRuntimeResourceManager_5);

		runtimeSystem.addRuntimeEventListener(this);

		try {
			runtimeSystem.startup(subMon.newChild(90));
		} catch (CoreException e) {
			runtimeSystem.removeRuntimeEventListener(this);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doSubmitJob(org.eclipse
	 * .debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.core.attributes.AttributeManager,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IPJob doSubmitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IPJob job = null;

		try {
			JobSubmission sub = new JobSubmission(jobSubIdCounter++);
			sub.setLaunchConfiguration(configuration);
			synchronized (jobSubmissions) {
				jobSubmissions.put(sub.getId(), sub);
			}

			runtimeSystem.submitJob(sub.getId(), attrMgr);

			JobSubStatus state = sub.waitFor(monitor);

			switch (state) {
			case CANCELLED:
				/*
				 * Once a job has been sent to the RM, it can't be canceled, so
				 * this will just cause the submitJob command to throw an
				 * exception. The job will still eventually get created.
				 */
				synchronized (jobSubmissions) {
					jobSubmissions.remove(sub.getId());
				}
				throw new CoreException(new Status(IStatus.CANCEL, PTPCorePlugin.getUniqueIdentifier(), IStatus.CANCEL,
						Messages.AbstractRuntimeResourceManager_cancelled, null));

			case SUBMITTED:
				job = sub.getJob();
				break;

			case ERROR:
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR,
						sub.getErrorReason(), null));
			}
		} finally {
			monitor.done();
		}

		return job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doTerminateJob(org.eclipse
	 * .ptp.core.elements.IPJob)
	 */
	@Override
	protected void doTerminateJob(IPJob job) throws CoreException {
		runtimeSystem.terminateJob(job);
	}

	/**
	 * Template pattern method to actually update the jobs.
	 * 
	 * @param jobs
	 * @param attrs
	 * @return changes were made
	 * @since 5.0
	 */
	protected abstract boolean doUpdateJobs(Collection<IPJobControl> jobs, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the machines.
	 * 
	 * @param machine
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateMachines(Collection<IPMachineControl> machines, AttributeManager attrs);

	/**
	 * Template pattern method to update a collection of nodes.
	 * 
	 * @param machine
	 *            parent machine
	 * @param nodes
	 *            collection of nodes to update
	 * @param attrs
	 *            new/changed attibutes for each node in the collection
	 * @return changes were made
	 */
	protected abstract boolean doUpdateNodes(IPMachineControl machine, Collection<IPNodeControl> nodes, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the processes.
	 * 
	 * @param job
	 *            parent job
	 * @param processJobRanks
	 *            collection of process job ranks representing processes to
	 *            update
	 * @param attrs
	 *            new/changed attributes for each node in the collection
	 * @return changes were made
	 * @since 4.0
	 */
	protected abstract boolean doUpdateProcesses(IPJobControl job, BitSet processJobRanks, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the queues.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateQueues(Collection<IPQueueControl> queues, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the queues.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateRM(AttributeManager attrs);

	/**
	 * @param sJobRank
	 * @return
	 * @since 4.0
	 */
	protected Integer getProcessJobRank(String sJobRank) {
		Integer procId;
		try {
			procId = Integer.valueOf(sJobRank);
		} catch (NumberFormatException e) {
			return null;
		}
		return procId;
	}

	/**
	 * @param processJobRanks
	 * @return
	 * @since 4.0
	 */
	protected BitSet getProcessJobRanks(RangeSet processJobRanks) {
		final BitSet bitSet = new BitSet(processJobRanks.size());
		for (String sRank : processJobRanks) {
			Integer rank = getProcessJobRank(sRank);
			if (rank != null) {
				bitSet.set(rank);
			} else {
				PTPCorePlugin.log(NLS.bind(Messages.AbstractRuntimeResourceManager_12, sRank));
			}
		}
		return bitSet;
	}

	/**
	 * @return the runtimeSystem
	 */
	protected IRuntimeSystem getRuntimeSystem() {
		return runtimeSystem;
	}

}