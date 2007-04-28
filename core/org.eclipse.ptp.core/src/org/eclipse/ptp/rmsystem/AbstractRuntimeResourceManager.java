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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.rtsystem.IRuntimeEventListener;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;

public abstract class AbstractRuntimeResourceManager extends
		AbstractResourceManager implements IRuntimeEventListener {

	private IRuntimeSystem runtimeSystem;
	private final ReentrantLock startupLock = new ReentrantLock();
	private final Condition startupCondition = startupLock.newCondition();
	private boolean started;
	private final ReentrantLock jobSubmissionLock = new ReentrantLock();
	private final Condition jobSubmissionCondition = jobSubmissionLock.newCondition();
	private IPJob newJob;
	private int jobSubId;
	private AttributeManager jobSubAttrs;
	
	public AbstractRuntimeResourceManager(int id, IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(id, universe, config);
		// nothing to do here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeAttributeDefinitionEvent(org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent)
	 *
	 * Note: this allows redefinition of attribute definitions. This is ok as long as they
	 * are only allowed during the initialization phase.
	 */
	public void handleRuntimeAttributeDefinitionEvent(IRuntimeAttributeDefinitionEvent e) {
		for (IAttributeDefinition attr : e.getDefinitions()) {
			getAttributeDefinitionManager().setAttributeDefinition(attr);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeDisconnectedEvent(org.eclipse.ptp.rtsystem.events.IRuntimeDisconnectedEvent)
	 */
	public void handleRuntimeConnectedStateEvent(IRuntimeConnectedStateEvent e) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeErrorEvent(org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent)
	 */
	public void handleRuntimeErrorEvent(IRuntimeErrorEvent e) {
		setState(ResourceManagerAttributes.State.ERROR,
				e.getMessage());
		fireError(e.getMessage());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeJobChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent)
	 */
	public void handleRuntimeJobChangeEvent(IRuntimeJobChangeEvent e) {
		boolean changed = false;
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		List<IPJob> jobs = new ArrayList<IPJob>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet jobIds = entry.getKey();
			changed = false;
			
			for (int id : jobIds) {
				IPJobControl job = getJobControl(id);
				if (job != null) {
					final boolean jobChanged = doUpdateJob(job, attrs);
					changed |= jobChanged;
					if (jobChanged) {
						jobs.add(job);
					}
				} else {
					System.out.println("JobChange: unknown job " + id);
				}
			}
			
			if (changed) {
				fireJobsChanged(jobs, Arrays.asList(attrs));
			}
			
			jobs.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeMachineChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent)
	 */
	public void handleRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent e) {
		boolean changed = false;
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		List<IPMachine> macs = new ArrayList<IPMachine>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet machineIds = entry.getKey();
			changed = false;
			
			for (int id : machineIds) {
				IPMachineControl machine = getMachineControl(id);
				if (machine != null) {
					final boolean macChanged = doUpdateMachine(machine, attrs);
					changed |= macChanged;
					if (macChanged) {
						macs.add(machine);
					}
				} else {
					System.out.println("MachineChange: unknown machine " + id);
				}
			}
			
			if (changed) {
				fireMachinesChanged(macs);
			}
			
			macs.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewJobEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent)
	 */
	public void handleRuntimeNewJobEvent(IRuntimeNewJobEvent e) {
		boolean changed = false;
		IPQueueControl queue = getQueueControl(e.getParentId());
		ElementAttributeManager mgr = e.getElementAttributeManager();

		List<IPJob> jobs = new ArrayList<IPJob>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			/*
			 * Combine job submission attributes with the job attributes. These are
			 * then added to the job.
			 */
			AttributeManager jobAttrs = new AttributeManager(jobSubAttrs.getAttributes());
			jobAttrs.setAttributes(entry.getValue().getAttributes());
			RangeSet jobIds = entry.getKey();
			changed = false;

			for (int id : jobIds) {
				IPJobControl job = getJobControl(id);
				if (job == null) {
					job = doCreateJob(queue, id, jobAttrs.getAttributes());
					jobs.add(job);
					addJob(id, job);
					changed = true;
					
					// TODO Fix launch code to eliminate this!
					jobSubmissionLock.lock();
					try {
						IIntegerAttribute jobSubAttr = (IIntegerAttribute) job.getAttribute(JobAttributes.getSubIdAttributeDefinition());
						if (jobSubAttr.getValue() == jobSubId) {
							newJob = job;
							jobSubmissionCondition.signal();
						}
					} finally {
						jobSubmissionLock.unlock();
					}
				}
			}
			
			if (changed) {
				fireNewJobs(jobs);
			}
			
			jobs.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewMachineEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent)
	 */
	public void handleRuntimeNewMachineEvent(IRuntimeNewMachineEvent e) {
		System.out.println(this + ": handleRuntimeNewMachineEvent");
		boolean changed = false;
		ElementAttributeManager mgr = e.getElementAttributeManager();

		List<IPMachine> machines = new ArrayList<IPMachine>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet machineIds = entry.getKey();
			changed = false;

			for (int id : machineIds) {
				IPMachineControl machine = getMachineControl(id);
				if (machine == null) {
					machine = doCreateMachine(id, attrs);
					addMachine(id, machine);
					machines.add(machine);
					changed = true;
				}
			}
			
			if (changed) {
				fireNewMachines(machines);
			}
			
			machines.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewNodeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent)
	 */
	public void handleRuntimeNewNodeEvent(IRuntimeNewNodeEvent e) {
		boolean changed = false;
		IPMachineControl machine = getMachineControl(e.getParentId());
		
		if (machine != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			List<IPNode> nodes = new ArrayList<IPNode>();
			
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				IAttribute[] attrs = entry.getValue().getAttributes();
				RangeSet nodeIds = entry.getKey();
				changed = false;
	
				for (int id : nodeIds) {
					IPNodeControl node = getNodeControl(id);
					if (node == null) {
						node = doCreateNode(machine, id, attrs);
						nodes.add(node);
						addNode(id, node);
						changed = true;
					}
				}
				
				if (changed) {
					fireNewNodes(nodes);
				}
				
				nodes.clear();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewProcessEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent)
	 */
	public void handleRuntimeNewProcessEvent(IRuntimeNewProcessEvent e) {
		boolean changed = false;
		IPJobControl job = getJobControl(e.getParentId());
		
		if (job != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			List<IPProcess> procs = new ArrayList<IPProcess>();
			
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				IAttribute[] attrs = entry.getValue().getAttributes();
				RangeSet processIds = entry.getKey();
				changed = false;
	
				for (int id : processIds) {
					IPProcessControl process = getProcessControl(id);
					if (process == null) {
						process = doCreateProcess(job, id, attrs);
						addProcess(id, process);
						procs.add(process);
						changed = true;
					}
				}
				
				if (changed) {
					fireNewProcesses(procs);
				}
				
				procs.clear();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewQueueEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent)
	 */
	public void handleRuntimeNewQueueEvent(IRuntimeNewQueueEvent e) {
		System.out.println(this + ": handleRuntimeNewQueueEvent");

		boolean changed = false;
		ElementAttributeManager mgr = e.getElementAttributeManager();
		
		List<IPQueue> queues = new ArrayList<IPQueue>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet queueIds = entry.getKey();
			changed = false;

			for (int id : queueIds) {
				IPQueueControl queue = getQueueControl(id);
				if (queue == null) {
					queue = doCreateQueue(id, attrs);
					addQueue(id, queue);
					queues.add(queue);
					changed = true;
				}
			}
			
			if (changed) {
				fireNewQueues(queues);
			}
			
			queues.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNodeChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent)
	 */
	public void handleRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent e) {
		boolean changed = false;
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		List<IPNode> changedNodes = new ArrayList<IPNode>();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet nodeIds = entry.getKey();
			changed = false;
			
			for (int id : nodeIds) {
				IPNodeControl node = getNodeControl(id);
				if (node != null) {
					final boolean nodeChanged = doUpdateNode(node, attrs);
					changed |= nodeChanged;
					if (nodeChanged) {
						changedNodes.add(node);
					}
				} else {
					System.out.println("NodeChange: unknown node " + id);
				}
			}
			
			if (changed) {
				fireNodesChanged(changedNodes);
			}
			
			changedNodes.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeProcessChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent)
	 */
	public void handleRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent e) {
		boolean changed = false;
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		
		List<IPProcess> procs = new ArrayList<IPProcess>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet processIds = entry.getKey();
			changed = false;
			
			for (int id : processIds) {
				IPProcessControl process = getProcessControl(id);
				if (process != null) {
					final boolean procChanged = doUpdateProcess(process, attrs);
					changed |= procChanged;
					if (procChanged) {
						procs.add(process);
					}
				} else {
					System.out.println("ProcessChange: unknown process " + id);
				}
			}
			
			if (changed) {
				fireProcessesChanged(procs);
			}
			
			procs.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeQueueChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent)
	 */
	public void handleRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent e) {
		boolean changed = false;
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		
		List<IPQueue> queues = new ArrayList<IPQueue>();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			IAttribute[] attrs = entry.getValue().getAttributes();
			RangeSet queueIds = entry.getKey();
			changed = false;
			
			for (int id : queueIds) {
				IPQueueControl queue = getQueueControl(id);
				if (queue != null) {
					final boolean queueChanged = doUpdateQueue(queue, attrs);
					changed |= queueChanged;
					if (queueChanged) {
						queues.add(queue);
					}
				} else {
					System.out.println("QueueChange: unknown queue " + id);
				}
			}
			
			if (changed) {
				fireQueuesChanged(queues);
			}
			
			queues.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRunningStateEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent)
	 */
	public void handleRuntimeRunningStateEvent(IRuntimeRunningStateEvent e) {
		startupLock.lock();
        CoreException exc = null;
		try {
            runtimeSystem.startEvents();
			started = true;
			startupCondition.signal();
		} catch (CoreException ex) {
            exc = ex;
        } finally {
			startupLock.unlock();
		}
        if (exc != null) {
            fireError(exc.getMessage());
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeShutdownStateEvent(org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent e) {
		startupLock.lock();
		try {
			started = false;
			startupCondition.signal();
		} finally {
			startupLock.unlock();
		}
	}

	/**
	 * close the connection.
	 */
	private void closeConnection() {
		runtimeSystem.shutdown();
		runtimeSystem.removeRuntimeEventListener(this);
	}

	/**
	 * @param system
	 * @throws CoreException 
	 */
	private void openConnection() throws CoreException {
		runtimeSystem.addRuntimeEventListener(this);
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

	/**
	 * Template pattern method to actually create the job.
	 *
	 * @param queue
	 * @param jobId
	 * @return
	 */
	abstract protected IPJobControl doCreateJob(IPQueueControl queue, int jobId, IAttribute[] attrs);
	
	/**
	 * Template pattern method to actually create the machine.
	 *
	 * @param machineId
	 * @return
	 */
	abstract protected IPMachineControl doCreateMachine(int machineId, IAttribute[] attrs);

	/**
	 * Template pattern method to actually create the node.
	 *
	 * @param machine
	 * @param nodeId
	 * @return
	 */
	abstract protected IPNodeControl doCreateNode(IPMachineControl machine, int nodeId, IAttribute[] attrs);

	/**
	 * Template pattern method to actually create the process.
	 *
	 * @param job
	 * @param processId
	 * @return
	 */
	abstract protected IPProcessControl doCreateProcess(IPJobControl job, int processId, IAttribute[] attrs);

	/**
	 * Template pattern method to actually create the queue.
	 *
	 * @param queueId
	 * @return
	 */
	abstract protected IPQueueControl doCreateQueue(int queueId, IAttribute[] attrs);

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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	protected void doShutdown(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		startupLock.lock();
		try {
			doBeforeCloseConnection();
			closeConnection();
			while (!monitor.isCanceled() && started) {
				try {
					startupCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is cancelled
				}
			}
			if (monitor.isCanceled()) {
				return;
			}
			doAfterCloseConnection();
		}
		finally {
			startupLock.unlock();
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		startupLock.lock();
		try {
			doBeforeOpenConnection();
			runtimeSystem = doCreateRuntimeSystem();
			openConnection();
			while (!monitor.isCanceled() && !started) {
				try {
					startupCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is cancelled
				}
			}
			if (monitor.isCanceled()) {
				//abortConnection(runtimeSystem);
				return;
			}
			doAfterOpenConnection();
		}
		finally {
			startupLock.unlock();
			monitor.done();
		}
	}
	
	//
	// TODO this needs to be changed to make job submission
	// asynchronous. Corresponding changes to the launch
	// configuration will be required.
	//
	protected IPJob doSubmitJob(AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		jobSubmissionLock.lock();
		try {
			newJob = null;
			
			/*
			 * Save submission attributes so they can be added to the job later.
			 */
			jobSubAttrs = attrMgr;
			
			// FIXME: generate a proper job submission id
			jobSubId++;
			
			runtimeSystem.submitJob(jobSubId, attrMgr);
			
			while (!monitor.isCanceled() && newJob != null) {
				try {
					jobSubmissionCondition.await(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Expect to be interrupted if monitor is cancelled
				}
			}
			if (monitor.isCanceled()) {
				//abortConnection(runtimeSystem);
				return null;
			}
		} finally {
			jobSubmissionLock.unlock();
			monitor.done();
		}
		return newJob;
	}
	
	protected void doTerminateJob(IPJob job) throws CoreException {
		runtimeSystem.terminateJob(job);
	}

	/**
	 * Template pattern method to actually update the job.
	 * 
	 * @param job
	 * @param attrs
	 * @return changes were made
	 */
	abstract protected boolean doUpdateJob(IPJobControl job, IAttribute[] attrs);

	/**
	 * Template pattern method to actually update the machine.
	 * 
	 * @param machine
	 * @param attrs
	 * @return changes were made
	 */
	abstract protected boolean doUpdateMachine(IPMachineControl machine, IAttribute[] attrs);

	/**
	 * Template pattern method to actually update the node.
	 * 
	 * @param node
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateNode(IPNodeControl node, IAttribute[] attrs);

	/**
	 * Template pattern method to actually update the process.
	 * 
	 * @param process
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateProcess(IPProcessControl node, IAttribute[] attrs);

	/**
	 * Template pattern method to actually update the queue.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateQueue(IPQueueControl queue, IAttribute[] attrs);

	/**
	 * @return the runtimeSystem
	 */
	protected IRuntimeSystem getRuntimeSystem() {
		return runtimeSystem;
	}

}