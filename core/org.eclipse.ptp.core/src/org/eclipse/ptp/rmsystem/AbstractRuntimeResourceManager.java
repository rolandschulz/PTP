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
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.rtsystem.IRuntimeEventListener;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
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
	private final ReentrantLock stateLock = new ReentrantLock();
	private final Condition stateCondition = stateLock.newCondition();
	private RMState state;
	private enum RMState {STARTING, STARTED, STOPPING, STOPPED, ERROR};
	
	public AbstractRuntimeResourceManager(String id, IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(id, universe, config);
		state = RMState.STOPPED;
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
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeMessageEvent(org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent)
	 */
	public void handleRuntimeMessageEvent(IRuntimeMessageEvent e) {
		stateLock.lock();
		try {
			if (state == RMState.STARTING) {
				state = RMState.ERROR;
				stateCondition.signal();
			}
		} finally {
			stateLock.unlock();
		}
		fireError(e.getText());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeJobChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent)
	 */
	public void handleRuntimeJobChangeEvent(IRuntimeJobChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet jobIds = entry.getKey();
			
			for (Integer id : jobIds) {
				String elementId = id.toString();
				IPJobControl job = getJobControl(elementId);
				if (job != null) {
					doUpdateJob(job, attrs);
				} else {
					System.out.println("JobChange: unknown job " + id);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeMachineChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent)
	 */
	public void handleRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet machineIds = entry.getKey();
			
			for (Integer id : machineIds) {
				String elementId = id.toString();
				IPMachineControl machine = getMachineControl(elementId);
				if (machine != null) {
					doUpdateMachine(machine, attrs);
				} else {
					System.out.println("MachineChange: unknown machine " + id);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewJobEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent)
	 */
	public void handleRuntimeNewJobEvent(IRuntimeNewJobEvent e) {
		IPQueueControl queue = getQueueControl(e.getParentId());
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager jobAttrs = entry.getValue();
			
			RangeSet jobIds = entry.getKey();

			for (Integer id : jobIds) {
				String elementId = id.toString();
				IPJobControl job = getJobControl(elementId);
				if (job == null) {
					job = doCreateJob(queue, elementId, jobAttrs);
					addJob(elementId, job);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewMachineEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent)
	 */
	public void handleRuntimeNewMachineEvent(IRuntimeNewMachineEvent e) {
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet machineIds = entry.getKey();

			for (Integer id : machineIds) {
				String elementId = id.toString();
				IPMachineControl machine = getMachineControl(elementId);
				if (machine == null) {
					machine = doCreateMachine(elementId, attrs);
					addMachine(elementId, machine);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewNodeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent)
	 */
	public void handleRuntimeNewNodeEvent(IRuntimeNewNodeEvent e) {
		IPMachineControl machine = getMachineControl(e.getParentId());
		
		if (machine != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet nodeIds = entry.getKey();
	
				for (Integer id : nodeIds) {
					String elementId = id.toString();
					IPNodeControl node = getNodeControl(elementId);
					if (node == null) {
						node = doCreateNode(machine, elementId, attrs);
						addNode(elementId, node);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewProcessEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent)
	 */
	public void handleRuntimeNewProcessEvent(IRuntimeNewProcessEvent e) {
		IPJobControl job = getJobControl(e.getParentId());
		
		if (job != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
	
			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet processIds = entry.getKey();
	
				for (Integer id : processIds) {
					String elementId = id.toString();
					IPProcessControl process = getProcessControl(elementId);
					if (process == null) {
						process = doCreateProcess(job, elementId, attrs);
						addProcess(elementId, process);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNewQueueEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent)
	 */
	public void handleRuntimeNewQueueEvent(IRuntimeNewQueueEvent e) {
		ElementAttributeManager mgr = e.getElementAttributeManager();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet queueIds = entry.getKey();

			for (Integer id : queueIds) {
				String elementId = id.toString();
				IPQueueControl queue = getQueueControl(elementId);
				if (queue == null) {
					queue = doCreateQueue(elementId, attrs);
					addQueue(elementId, queue);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeNodeChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent)
	 */
	public void handleRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet nodeIds = entry.getKey();
			
			for (Integer id : nodeIds) {
				String elementId = id.toString();
				IPNodeControl node = getNodeControl(elementId);
				if (node != null) {
					doUpdateNode(node, attrs);
				} else {
					System.out.println("NodeChange: unknown node " + id);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeProcessChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent)
	 */
	public void handleRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet processIds = entry.getKey();
			
			for (Integer id : processIds) {
				String elementId = id.toString();
				IPProcessControl process = getProcessControl(elementId);
				if (process != null) {
					doUpdateProcess(process, attrs);
				} else {
					System.out.println("ProcessChange: unknown process " + id);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeQueueChangeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent)
	 */
	public void handleRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();
		
		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet queueIds = entry.getKey();
			
			for (Integer id : queueIds) {
				String elementId = id.toString();
				IPQueueControl queue = getQueueControl(elementId);
				if (queue != null) {
					doUpdateQueue(queue, attrs);
				} else {
					System.out.println("QueueChange: unknown queue " + id);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRunningStateEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent)
	 */
	public void handleRuntimeRunningStateEvent(IRuntimeRunningStateEvent e) {
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
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeShutdownStateEvent(org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent e) {
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
	 * Abort the RTS connection.
	 */
	private void abortConnection() {
		runtimeSystem.shutdown();
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
	
	protected List<IPJob> doRemoveTerminatedJobs(IPQueueControl queue) {
		List<IPJob> jobs = new ArrayList<IPJob>();
		if (queue != null) {
			for (IPJobControl job : queue.getJobControls()) {
				if (job.isTerminated()) {
					jobs.add(job);
					queue.removeJob(job);
				}
			}
		}
		return jobs;
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
			doBeforeOpenConnection();
			runtimeSystem = doCreateRuntimeSystem();
			runtimeSystem.addRuntimeEventListener(this);
			openConnection();
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
		finally {
			stateLock.unlock();
			monitor.done();
		}
		return true;
	}
	
	protected String doSubmitJob(AttributeManager attrMgr) throws CoreException {
		return runtimeSystem.submitJob(attrMgr);
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
	abstract protected boolean doUpdateJob(IPJobControl job, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the machine.
	 * 
	 * @param machine
	 * @param attrs
	 * @return changes were made
	 */
	abstract protected boolean doUpdateMachine(IPMachineControl machine, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the node.
	 * 
	 * @param node
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateNode(IPNodeControl node, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the process.
	 * 
	 * @param process
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateProcess(IPProcessControl node, AttributeManager attrs);

	/**
	 * Template pattern method to actually update the queue.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected abstract boolean doUpdateQueue(IPQueueControl queue, AttributeManager attrs);

	/**
	 * @return the runtimeSystem
	 */
	protected IRuntimeSystem getRuntimeSystem() {
		return runtimeSystem;
	}

}