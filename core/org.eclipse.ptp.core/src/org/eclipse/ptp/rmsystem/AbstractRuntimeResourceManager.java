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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.rtsystem.IRuntimeEventListener;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeEvent;
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

public abstract class AbstractRuntimeResourceManager extends
		AbstractResourceManager implements IRuntimeEventListener {

	public enum JobSubState {SUBMITTED, COMPLETED, ERROR}
	
	private class JobSubmission {
		private IPJob			job = null;
		private JobSubState		state = JobSubState.SUBMITTED;
		private IRuntimeEvent	event;
		
		/**
		 * @return the event
		 */
		public IRuntimeEvent getEvent() {
			return event;
		}
		
		/**
		 * @return the job
		 */
		public IPJob getJob() {
			return job;
		}
		
		/**
		 * @return the state
		 */
		public JobSubState getState() {
			return state;
		}
		
		/**
		 * @param event the event to set
		 */
		public void setEvent(IRuntimeEvent event) {
			this.event = event;
		}
		
		/**
		 * @param job the job to set
		 */
		public void setJob(IPJob job) {
			this.job = job;
		}
		
		/**
		 * @param error the error to set
		 */
		public void setState(JobSubState state) {
			this.state = state;
		}
	}
	private enum RMState {STARTING, STARTED, STOPPING, STOPPED, ERROR}
	
	private IRuntimeSystem runtimeSystem;
	private RMState state;
	
	private final ReentrantLock stateLock = new ReentrantLock();;
	private final Condition stateCondition = stateLock.newCondition();
	private final ReentrantLock subLock = new ReentrantLock();
	private final Condition subCondition = subLock.newCondition();;
	
	private Map<String, JobSubmission> jobSubmissions = new HashMap<String, JobSubmission>();
	
	public AbstractRuntimeResourceManager(String id, IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(id, universe, config);
		state = RMState.STOPPED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent)
	 *
	 * Note: this allows redefinition of attribute definitions. This is ok as long as they
	 * are only allowed during the initialization phase.
	 */
	public void handleEvent(IRuntimeAttributeDefinitionEvent e) {
		for (IAttributeDefinition<?,?,?> attr : e.getDefinitions()) {
			getAttributeDefinitionManager().setAttributeDefinition(attr);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent)
	 */
	public void handleEvent(IRuntimeConnectedStateEvent e) {
		// Ignore
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent)
	 */
	public void handleEvent(IRuntimeErrorStateEvent e) {
		/*
		 * Fatal error in the runtime system. Cancel any pending job submissions
		 * and inform upper levels of the problem.
		 */
		subLock.lock();
		try {
			for (JobSubmission sub : jobSubmissions.values()) {
				sub.setState(JobSubState.ERROR);
				sub.setEvent(e);
			}
			subCondition.signalAll();
		} finally {
			subLock.unlock();
		}
		
		stateLock.lock();
		try {
			RMState oldState = state;
			state = RMState.ERROR;
			if (oldState == RMState.STOPPING) {
				stateCondition.signal();
			}
			setState(ResourceManagerAttributes.State.ERROR);
			cleanUp();
		} finally {
			stateLock.unlock();
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent)
	 */
	public void handleEvent(IRuntimeJobChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		Map<IPQueueControl, List<IPJobControl>> map = 
			new HashMap<IPQueueControl, List<IPJobControl>>();
		
		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet jobIds = mgrEntry.getKey();
			List<IPJobControl> changedJobs;
			
			for (String elementId : jobIds) {
				IPJobControl job = getJobControl(elementId);
				if (job != null) {
					IPQueueControl queue = job.getQueueControl();
					changedJobs = map.get(queue);
					if (changedJobs == null) {
						changedJobs = new ArrayList<IPJobControl>();
						map.put(queue, changedJobs);
					}
					changedJobs.add(job);
				} else {
					System.out.println("JobChange: unknown node " + elementId);
				}
			}

			for (Map.Entry<IPQueueControl, List<IPJobControl>> entry : map.entrySet()) {
				doUpdateJobs(entry.getKey(), entry.getValue(), attrs);
			}
			
			map.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent)
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
					System.out.println("MachineChange: unknown machine " + elementId);
				}
			}
			
			doUpdateMachines(machines, attrs);
			machines.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent)
	 */
	public void handleEvent(IRuntimeMessageEvent e) {
		MessageAttributes.Level level = e.getLevel();
		// FIXME: implement logging
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent)
	 */
	public void handleEvent(IRuntimeNewJobEvent e) {
		IPQueueControl queue = getQueueControl(e.getParentId());
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager jobAttrs = entry.getValue();
			
			RangeSet jobIds = entry.getKey();
			List<IPJobControl> newJobs = new ArrayList<IPJobControl>(jobIds.size());

			for (String elementId : jobIds) {
				IPJobControl job = getJobControl(elementId);
				if (job == null) {
					job = doCreateJob(queue, elementId, jobAttrs);
					newJobs.add(job);
					
					StringAttribute jobSubAttr = 
						(StringAttribute) jobAttrs.getAttribute(JobAttributes.getSubIdAttributeDefinition());
					if (jobSubAttr != null) {
						/*
						 * Notify any submitJob() calls that the job has been created
						 */
						subLock.lock();
						try {
							JobSubmission sub = jobSubmissions.get(jobSubAttr.getValue());
							if (sub != null && sub.getState() == JobSubState.SUBMITTED) {
								sub.setJob(job);
								sub.setState(JobSubState.COMPLETED);
								subCondition.signalAll();
							}
				        } finally {
				        	subLock.unlock();
						}
					}
			 	}
			}
			
			addJobs(queue, newJobs);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent)
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent)
	 */
	public void handleEvent(IRuntimeNewNodeEvent e) {
		IPMachineControl machine = getMachineControl(e.getParentId());
		
		if (machine != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet nodeIds = entry.getKey();
				List<IPNodeControl> newNodes = new ArrayList<IPNodeControl>(nodeIds.size());
	
				for (String elementId : nodeIds) {
					IPNodeControl node = getNodeControl(elementId);
					if (node == null) {
						node = doCreateNode(machine, elementId, attrs);
						newNodes.add(node);
					}
				}
				
				addNodes(machine, newNodes);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent)
	 */
	public void handleEvent(IRuntimeNewProcessEvent e) {
		IPJobControl job = getJobControl(e.getParentId());
		
		if (job != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet processIds = entry.getKey();
				List<IPProcessControl> newProcesses = new ArrayList<IPProcessControl>(processIds.size());
	
				for (String elementId : processIds) {
					IPProcessControl process = getProcessControl(elementId);
					if (process == null) {
						process = doCreateProcess(job, elementId, attrs);
						newProcesses.add(process);
					}
				}
				
				addProcesses(job, newProcesses);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent)
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent)
	 */
	public void handleEvent(IRuntimeNodeChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		Map<IPMachineControl, List<IPNodeControl>> map = 
			new HashMap<IPMachineControl, List<IPNodeControl>>();
		
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
					System.out.println("NodeChange: unknown node " + elementId);
				}
			}

			for (Map.Entry<IPMachineControl, List<IPNodeControl>> entry : map.entrySet()) {
				doUpdateNodes(entry.getKey(), entry.getValue(), attrs);
			}
			
			map.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent)
	 */
	public void handleEvent(IRuntimeProcessChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		Map<IPJobControl, List<IPProcessControl>> map = 
			new HashMap<IPJobControl, List<IPProcessControl>>();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet processIds = mgrEntry.getKey();
			List<IPProcessControl> changedProcesses;
			
			for (String elementId : processIds) {
				IPProcessControl process = getProcessControl(elementId);
				if (process != null) {
					IPJobControl job = process.getJobControl();
					changedProcesses = map.get(job);
					if (changedProcesses == null) {
						changedProcesses = new ArrayList<IPProcessControl>();
						map.put(job, changedProcesses);
					}
					changedProcesses.add(process);
				} else {
					System.out.println("ProcessChange: unknown process " + elementId);
				}
			}
			
			for (Map.Entry<IPJobControl, List<IPProcessControl>> entry : map.entrySet()) {
				doUpdateProcesses(entry.getKey(), entry.getValue(), attrs);
			}
			
			map.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent)
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
					System.out.println("QueueChange: unknown queue " + elementId);
				}
			}
			
			doUpdateQueues(queues, attrs);
			queues.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IRuntimeRemoveAllEvent e) {
		cleanUp();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e) {
		Map<IPQueueControl, List<IPJobControl>> map = 
			new HashMap<IPQueueControl, List<IPJobControl>>();

		for (String elementId : e.getElementIds()) {
			IPJobControl job = getJobControl(elementId);
			if (job != null) {
				IPQueueControl queue = job.getQueueControl();
				List<IPJobControl> jobs = map.get(queue);
				if (jobs == null) {
					jobs = new ArrayList<IPJobControl>();
					map.put(queue, jobs);
				}
				jobs.add(job);
			}
		}
		
		for (Map.Entry<IPQueueControl, List<IPJobControl>> entry : map.entrySet()) {
			removeJobs(entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e) {
		Map<IResourceManager, List<IPMachineControl>> map = 
			new HashMap<IResourceManager, List<IPMachineControl>>();

		for (String elementId : e.getElementIds()) {
			IPMachineControl machine = getMachineControl(elementId);
			if (machine != null) {
				IResourceManager rm = machine.getResourceManager();
				List<IPMachineControl> machines = map.get(rm);
				if (machines == null) {
					machines = new ArrayList<IPMachineControl>();
					map.put(rm, machines);
				}
				machines.add(machine);
			}
		}
		
		for (Map.Entry<IResourceManager, List<IPMachineControl>> entry : map.entrySet()) {
			removeMachines(entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e) {
		Map<IPMachineControl, List<IPNodeControl>> map = 
			new HashMap<IPMachineControl, List<IPNodeControl>>();
		
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(IRuntimeRemoveProcessEvent e) {
		Map<IPJobControl, List<IPProcessControl>> map = 
			new HashMap<IPJobControl, List<IPProcessControl>>();

		for (String elementId : e.getElementIds()) {
			IPProcessControl process = getProcessControl(elementId);
			if (process != null) {
				IPJobControl job = process.getJobControl();
				List<IPProcessControl> processes = map.get(job);
				if (processes == null) {
					processes = new ArrayList<IPProcessControl>();
					map.put(job, processes);
				}
				processes.add(process);
			}
		}

		for (Map.Entry<IPJobControl, List<IPProcessControl>> entry : map.entrySet()) {
			removeProcesses(entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(IRuntimeRemoveQueueEvent e) {
		Map<IResourceManager, List<IPQueueControl>> map = 
			new HashMap<IResourceManager, List<IPQueueControl>>();

		for (String elementId : e.getElementIds()) {
			IPQueueControl queue = getQueueControl(elementId);
			if (queue != null) {
				IResourceManager rm = queue.getResourceManager();
				List<IPQueueControl> queues = map.get(rm);
				if (queues == null) {
					queues = new ArrayList<IPQueueControl>();
					map.put(rm, queues);
				}
				queues.add(queue);
			}
		}
		
		for (Map.Entry<IResourceManager, List<IPQueueControl>> entry : map.entrySet()) {
			removeQueues(entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent)
	 */
	public void handleEvent(IRuntimeRunningStateEvent e) {
		stateLock.lock();
        CoreException exc = null;
		try {
            if (state == RMState.STARTING) {
            	runtimeSystem.startEvents();
            	state = RMState.STARTED;
				stateCondition.signal();
            }
		} catch (CoreException ex) {
            exc = ex;
        } finally {
        	stateLock.unlock();
		}
        if (exc != null) {
            fireError(exc.getMessage());
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleEvent(IRuntimeShutdownStateEvent e) {
		stateLock.lock();
		try {
			RMState oldState = state;
			state = RMState.STOPPED;
			if (oldState == RMState.STOPPING) {
				stateCondition.signal();
			} else {
				/*
				 * This event has been generated by the runtime system. Let upper levels know
				 * that the RM has shut down.
				 */
				setState(ResourceManagerAttributes.State.STOPPED);
				cleanUp();
			}
		} finally {
			stateLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e) {
		/*
		 * Check for errors while starting.
		 */
		stateLock.lock();
		try {
			if (state == RMState.STARTING) {
				state = RMState.ERROR;
				stateCondition.signal();
				return;
			}
		} finally {
			stateLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		subLock.lock();
		try {
			/*
			switch (level) {
			case FATAL:
				/*
				 * Fatal error in the proxy. Terminate all job submissions.
				 *
				for (JobSubmission sub : jobSubmissions.values()) {
					sub.setState(JobSubState.ERROR);
					sub.setMessage(e);
				}
				subCondition.signalAll();
				break;*/
			if (e.getJobSubID() != null) {
				JobSubmission sub = jobSubmissions.get(e.getJobSubID());
				if (sub != null) {
					sub.setState(JobSubState.ERROR);
					sub.setEvent(e);
					subCondition.signalAll();
				}
			}
		} finally {
			subLock.unlock();
		}
		
		fireError(e.getErrorMessage());	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(
			IRuntimeTerminateJobErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Abort the RTS connection.
	 */
	private void abortConnection() {
		runtimeSystem.shutdown();
	}

	/**
	 * Close the RTS connection.
	 */
	private void closeConnection() {
		runtimeSystem.shutdown();
	}

	/**
	 * Open the RTS connection.
	 * 
	 * @throws CoreException 
	 */
	private void openConnection() throws CoreException {
		runtimeSystem.startup();
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doCleanUp()
	 */
	@Override
	protected void doCleanUp() {
		state = RMState.STOPPED;
	}
	
	/**
	 * Template pattern method to actually create the job.
	 *
	 * @param queue
	 * @param jobId
	 * @return
	 */
	abstract protected IPJobControl doCreateJob(IPQueueControl queue, String jobId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the machine.
	 *
	 * @param machineId
	 * @return
	 */
	abstract protected IPMachineControl doCreateMachine(String machineId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the node.
	 *
	 * @param machine
	 * @param nodeId
	 * @return
	 */
	abstract protected IPNodeControl doCreateNode(IPMachineControl machine, String nodeId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the process.
	 *
	 * @param job
	 * @param processId
	 * @return
	 */
	abstract protected IPProcessControl doCreateProcess(IPJobControl job, String processId, AttributeManager attrs);

	/**
	 * Template pattern method to actually create the queue.
	 *
	 * @param queueId
	 * @return
	 */
	abstract protected IPQueueControl doCreateQueue(String queueId, AttributeManager attrs);

	/**
	 * create a new runtime system
	 * @return the new runtime system
	 * @throws CoreException TODO
	 */
	protected abstract IRuntimeSystem doCreateRuntimeSystem()
	throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDisableEvents()
	 */
	protected void doDisableEvents() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doEnableEvents()
	 */
	protected void doEnableEvents() {
		// TODO Auto-generated method stub
		
	}

	protected List<IPJobControl> doRemoveTerminatedJobs(IPQueueControl queue) {
		List<IPJobControl> terminatedJobs = new ArrayList<IPJobControl>();
		
		if (queue != null) {
			for (IPJobControl job : queue.getJobControls()) {
				if (job.isTerminated()) {
					terminatedJobs.add(job);
				}
			}
			queue.removeJobs(terminatedJobs);
		}
		
		return terminatedJobs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	protected void doShutdown(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		stateLock.lock();
		try {
			doBeforeCloseConnection();
			closeConnection();
			state = RMState.STOPPING;
			while (state != RMState.STOPPED && state != RMState.ERROR) {
				try {
					stateCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
			}
			doAfterCloseConnection();
		}
		finally {
			stateLock.unlock();
			runtimeSystem.removeRuntimeEventListener(this);
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected boolean doStartup(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		stateLock.lock();
		try {
			if (state == RMState.STOPPED) {
				monitor.beginTask("Runtime resource manager startup", 10);
				doBeforeOpenConnection();
				monitor.subTask("Creating runtime system");
				runtimeSystem = doCreateRuntimeSystem();
				monitor.worked(1);
				runtimeSystem.addRuntimeEventListener(this);
				monitor.worked(2);
				monitor.subTask("Starting runtime system");
				if (!runtimeSystem.startup()) {
					monitor.worked(7);
					state = RMState.ERROR;
					return false;
				}
				monitor.worked(7);
				state = RMState.STARTING;
				while (!monitor.isCanceled() && state != RMState.STARTED && state != RMState.ERROR) {
					try {
						stateCondition.await(500, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					}
				}
				if (state == RMState.ERROR) {
					return false;
				}
				if (monitor.isCanceled()) {
					state = RMState.STOPPED;
					abortConnection();
					return false;
				}
				doAfterOpenConnection();
			}
		}
		finally {
			stateLock.unlock();
			monitor.done();
		}
		return true;
	}
	
	protected IPJob doSubmitJob(AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		subLock.lock();
		try {
			String jobSubId = runtimeSystem.submitJob(attrMgr);
			jobSubmissions.put(jobSubId, new JobSubmission());
			
			while (!monitor.isCanceled()) {
				JobSubmission sub = jobSubmissions.get(jobSubId);
				if (sub.getState() != JobSubState.SUBMITTED) {
					break;
				}
				try {
					subCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is canceled
				}
			}
			
			JobSubmission sub = jobSubmissions.remove(jobSubId);
			IPJob job = null;
			
			if (sub != null) {
				if (sub.getState() == JobSubState.SUBMITTED && monitor.isCanceled()) {
					/*
					 * The job submission process itself can't be canceled, so
					 * this will just cancel the job once it is queued.
					 * 
					 * If job is null, then we must wait for the submission to
					 * complete and the job to be created (this will need to happen
					 * in a thread).
					 */
					//FIXME: implement this
				} else {
					job = sub.getJob();
				}
			}
			return job;
		}
		finally {
			subLock.unlock();
			monitor.done();
		}
	}

	protected void doTerminateJob(IPJob job) throws CoreException {
		runtimeSystem.terminateJob(job);
	}

	/**
	 * Template pattern method to actually update the jobs.
	 * 
	 * @param job
	 * @param attrs
	 * @return changes were made
	 */
	abstract protected boolean doUpdateJobs(IPQueueControl queue,
			Collection<IPJobControl> jobs, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the machines.
	 * 
	 * @param machine
	 * @param attrs
	 * @return changes were made
	 */
	abstract protected boolean doUpdateMachines(Collection<IPMachineControl> machines, AttributeManager attrs);

	/**
	 * Template pattern method to update a collection of nodes.
	 * 
	 * @param machine parent machine
	 * @param nodes collection of nodes to update
	 * @param attrs new/changed attibutes for each node in the collection
	 * @return changes were made
	 */
	protected abstract boolean doUpdateNodes(IPMachineControl machine, 
			Collection<IPNodeControl> nodes, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the processes.
	 * 
	 * @param job parent job
	 * @param processes collection of processes to update
	 * @param attrs new/changed attibutes for each node in the collection
	 * @return changes were made
	 */
	protected abstract boolean doUpdateProcesses(IPJobControl job,
			Collection<IPProcessControl> processes, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the queues.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateQueues(Collection<IPQueueControl> queues, AttributeManager attrs);

	/**
	 * @return the runtimeSystem
	 */
	protected IRuntimeSystem getRuntimeSystem() {
		return runtimeSystem;
	}

}