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
package org.eclipse.ptp.rtsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IJobStatus;
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
import org.eclipse.ptp.utils.core.BitSetIterable;
import org.eclipse.ptp.utils.core.RangeSet;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * @author greg
 * @since 5.0
 * 
 */
public abstract class AbstractRuntimeResourceManagerMonitor extends AbstractResourceManagerMonitor implements IRuntimeEventListener {
	private class StreamMonitor implements IStreamMonitor, IJobChildListener {
		private final ListenerList fListeners = new ListenerList();
		private final StringBuffer fContents = new StringBuffer();
		private final IPJob fJob;
		private final StringAttributeDefinition fAttrDef;
		private final boolean fPrefix;

		public StreamMonitor(IPJob job, StringAttributeDefinition attrDef, boolean prefix) {
			fJob = job;
			fAttrDef = attrDef;
			fPrefix = prefix;
			job.addChildListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse
		 * .debug.core.IStreamListener)
		 */
		public synchronized void addListener(IStreamListener listener) {
			fListeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
		 */
		public synchronized String getContents() {
			return fContents.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
		 */
		public void handleEvent(IChangedProcessEvent e) {
			final boolean hasOutput = e.getAttributes().getAttribute(fAttrDef) != null;
			if (hasOutput) {
				final BitSet indices = e.getProcesses();
				for (Integer index : new BitSetIterable(indices)) {
					StringAttribute stdout = fJob.getProcessAttribute(fAttrDef, index);
					if (stdout != null) {
						String text = ""; //$NON-NLS-1$
						if (fPrefix) {
							text = "[" + index + "] "; //$NON-NLS-1$ //$NON-NLS-2$
						}
						text += stdout.getValueAsString() + "\n"; //$NON-NLS-1$
						fContents.append(text);
						fireStreamAppended(text);
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewProcessEvent)
		 */
		public void handleEvent(INewProcessEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
		 */
		public void handleEvent(IRemoveProcessEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse
		 * .debug.core.IStreamListener)
		 */
		public synchronized void removeListener(IStreamListener listener) {
			fListeners.remove(listener);
		}

		private void fireStreamAppended(String text) {
			for (Object listener : fListeners.getListeners()) {
				((IStreamListener) listener).streamAppended(text, this);
			}
		}

	}

	private class StreamsProxy implements IStreamsProxy {
		private final IStreamMonitor fErrorStreamMonitor;
		private final IStreamMonitor fOutputStreamMonitor;

		/*
		 * TODO: obtain prefix flag from launch configuration
		 */
		private final boolean fPrefix = false;

		public StreamsProxy(IPJob job) {
			fErrorStreamMonitor = new StreamMonitor(job, ProcessAttributes.getStderrAttributeDefinition(), fPrefix);
			fOutputStreamMonitor = new StreamMonitor(job, ProcessAttributes.getStdoutAttributeDefinition(), fPrefix);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IStreamsProxy#getErrorStreamMonitor()
		 */
		public IStreamMonitor getErrorStreamMonitor() {
			return fErrorStreamMonitor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IStreamsProxy#getOutputStreamMonitor()
		 */
		public IStreamMonitor getOutputStreamMonitor() {
			return fOutputStreamMonitor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IStreamsProxy#write(java.lang.String)
		 */
		public void write(String input) throws IOException {
			// Not supported yet
		}

	}

	private final Map<String, IStreamsProxy> fStreamProxies = new HashMap<String, IStreamsProxy>();

	/**
	 * @since 5.0
	 */
	public AbstractRuntimeResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor#doAddJob(java
	 * .lang.String, org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	@Override
	public void doAddJob(String jobId, IJobStatus status) {
		// All jobs are special
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor#doRemoveJob(java
	 * .lang.String)
	 */
	@Override
	public void doRemoveJob(String jobId) {
		// All jobs are special
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor#doUpdateJob(java
	 * .lang.String, org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	@Override
	public void doUpdateJob(String jobId, IJobStatus status) {
		// No need to update
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
			getRuntimeSystem().getAttributeDefinitionManager().setAttributeDefinition(attr);
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
		getResourceManager().fireResourceManagerError(Messages.AbstractRuntimeResourceManager_6);
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
		List<IPJob> changedJobs = new ArrayList<IPJob>();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet jobIds = mgrEntry.getKey();

			for (String jobId : jobIds) {
				IPJob job = getPResourceManager().getJobById(jobId);
				if (job != null) {
					changedJobs.add(job);
				} else {
					PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_7 + jobId);
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
		List<IPMachine> machines = new ArrayList<IPMachine>();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet machineIds = entry.getKey();

			for (String elementId : machineIds) {
				IPMachine machine = getPResourceManager().getMachineById(elementId);
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
		IPQueue queue = getPResourceManager().getQueueById(e.getParentId());

		ElementAttributeManager mgr = e.getElementAttributeManager();
		Collection<IPJob> newJobs = new ArrayList<IPJob>();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager jobAttrs = entry.getValue();

			RangeSet jobIds = entry.getKey();

			for (String elementId : jobIds) {
				IPJob job = getPResourceManager().getJobById(elementId);
				if (job == null) {
					job = doCreateJob(elementId, jobAttrs);
					newJobs.add(job);
					fStreamProxies.put(elementId, new StreamsProxy(job));
				}
			}

		}
		if (newJobs.size() > 0) {
			getPResourceManager().addJobs(queue, newJobs);
		}
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
			List<IPMachine> newMachines = new ArrayList<IPMachine>(machineIds.size());

			for (String elementId : machineIds) {
				IPMachine machine = getPResourceManager().getMachineById(elementId);
				if (machine == null) {
					machine = doCreateMachine(elementId, attrs);
					newMachines.add(machine);
				}
			}

			if (newMachines.size() > 0) {
				getPResourceManager().addMachines(newMachines);
			}
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
		IPMachine machine = getPResourceManager().getMachineById(e.getParentId());

		if (machine != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();
			Collection<IPNode> newNodes = new ArrayList<IPNode>();

			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet nodeIds = entry.getKey();

				for (String elementId : nodeIds) {
					IPNode node = getPResourceManager().getNodeById(elementId);
					if (node == null) {
						node = doCreateNode(machine, elementId, attrs);
						newNodes.add(node);
					}
				}

			}
			if (newNodes.size() > 0) {
				getPResourceManager().addNodes(machine, newNodes);
			}
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
		final IPJob job = getPResourceManager().getJobById(jobId);

		if (job != null) {
			ElementAttributeManager mgr = e.getElementAttributeManager();

			for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
				AttributeManager attrs = entry.getValue();
				RangeSet processJobRanks = entry.getKey();
				BitSet newProcessJobRanks = getProcessJobRanks(processJobRanks);

				getPResourceManager().addProcessesByJobRanks(job, newProcessJobRanks, attrs);
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
			List<IPQueue> newQueues = new ArrayList<IPQueue>(queueIds.size());

			for (String elementId : queueIds) {
				IPQueue queue = getPResourceManager().getQueueById(elementId);
				if (queue == null) {
					queue = doCreateQueue(elementId, attrs);
					newQueues.add(queue);
				}
			}

			if (newQueues.size() > 0) {
				getPResourceManager().addQueues(newQueues);
			}
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
		Map<IPMachine, List<IPNode>> map = new HashMap<IPMachine, List<IPNode>>();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet nodeIds = mgrEntry.getKey();
			List<IPNode> changedNodes;

			for (String elementId : nodeIds) {
				IPNode node = getPResourceManager().getNodeById(elementId);
				if (node != null) {
					IPMachine machine = node.getMachine();
					changedNodes = map.get(machine);
					if (changedNodes == null) {
						changedNodes = new ArrayList<IPNode>();
						map.put(machine, changedNodes);
					}
					changedNodes.add(node);
				} else {
					PTPCorePlugin.log(Messages.AbstractRuntimeResourceManager_9 + elementId);
				}
			}

			for (Map.Entry<IPMachine, List<IPNode>> entry : map.entrySet()) {
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
		IPJob job = getPResourceManager().getJobById(jobId);

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet processJobRanks = mgrEntry.getKey();

			// convert the RangeSet to a BitSet
			BitSet changedProcessesJobRanks = getProcessJobRanks(processJobRanks);

			updateProcessesByJobRanks(job, changedProcessesJobRanks, attrs);

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
		List<IPQueue> queues = new ArrayList<IPQueue>();

		for (Map.Entry<RangeSet, AttributeManager> entry : eMgr.getEntrySet()) {
			AttributeManager attrs = entry.getValue();
			RangeSet queueIds = entry.getKey();

			for (String elementId : queueIds) {
				IPQueue queue = getPResourceManager().getQueueById(elementId);
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
		removeAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e) {
		Set<IPJob> removedJobs = new HashSet<IPJob>();

		for (String elementId : e.getElementIds()) {
			IPJob job = getPResourceManager().getJobById(elementId);
			if (job != null) {
				removedJobs.add(job);
			}
		}

		getPResourceManager().removeJobs(removedJobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e) {
		List<IPMachine> machines = new ArrayList<IPMachine>();
		for (String elementId : e.getElementIds()) {
			IPMachine machine = getPResourceManager().getMachineById(elementId);
			if (machine != null) {
				machines.add(machine);
			}
		}
		getPResourceManager().removeMachines(machines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e) {
		Map<IPMachine, List<IPNode>> map = new HashMap<IPMachine, List<IPNode>>();

		for (String elementId : e.getElementIds()) {
			IPNode node = getPResourceManager().getNodeById(elementId);
			if (node != null) {
				IPMachine machine = node.getMachine();
				List<IPNode> nodes = map.get(machine);
				if (nodes == null) {
					nodes = new ArrayList<IPNode>();
					map.put(machine, nodes);
				}
				nodes.add(node);
			}
		}

		for (Map.Entry<IPMachine, List<IPNode>> entry : map.entrySet()) {
			getPResourceManager().removeNodes(entry.getKey(), entry.getValue());
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

		IPJob job = getPResourceManager().getJobById(e.getJobId());

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
		List<IPQueue> queues = new ArrayList<IPQueue>();
		for (String elementId : e.getElementIds()) {
			IPQueue queue = getPResourceManager().getQueueById(elementId);
			if (queue != null) {
				queues.add(queue);
			}
		}
		getPResourceManager().removeQueues(queues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRMChangeEvent)
	 */
	public void handleEvent(IRuntimeRMChangeEvent e) {
		ElementAttributeManager eMgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> mgrEntry : eMgr.getEntrySet()) {
			AttributeManager attrs = mgrEntry.getValue();
			RangeSet rmIds = mgrEntry.getKey();

			for (String elementId : rmIds) {
				if (getPResourceManager().getID().equals(elementId)) {
					getPResourceManager().addAttributes(attrs.getAttributes());
					getResourceManager().fireResourceManagerChanged();
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
		try {
			getRuntimeSystem().startEvents();
		} catch (CoreException ex) {
			getResourceManager().fireResourceManagerError(ex.getMessage());
		}

		getResourceManager().fireResourceManagerStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleEvent(IRuntimeShutdownStateEvent e) {
		getResourceManager().fireResourceManagerStopped();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e) {
		getResourceManager().fireResourceManagerError(e.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		// Handled by control side
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(IRuntimeTerminateJobErrorEvent e) {
		// Handled by control side
	}

	private void removeAll() {
		getPResourceManager().removeJobs(Arrays.asList(getPResourceManager().getJobs()));
		getPResourceManager().removeQueues(Arrays.asList(getPResourceManager().getQueues()));
		getPResourceManager().removeMachines(Arrays.asList(getPResourceManager().getMachines()));
	}

	/**
	 * Update attributes on a collection of processes. If the nodeId attribute
	 * is specified then the processes will be moved to the new node.
	 * 
	 * @param job
	 *            parent of processes
	 * @param processJobRanks
	 *            set of job ranks for processes
	 * @param attrs
	 *            attributes to update
	 * @return true if updated
	 * @since 5.0
	 */
	private boolean updateProcessesByJobRanks(IPJob job, BitSet processJobRanks, AttributeManager attrs) {
		final StringAttributeDefinition nodeIdAttributeDefinition = ProcessAttributes.getNodeIdAttributeDefinition();

		StringAttribute newNodeId = attrs.getAttribute(nodeIdAttributeDefinition);

		// if we are update a node attribute we must move processes from one
		// node
		// to another
		if (newNodeId != null) {

			IPNode newNode = getPResourceManager().getNodeById(newNodeId.getValue());
			// add the job and the process job ranks to the new node
			newNode.addJobProcessRanks(job, processJobRanks);

			// Remove the process job ranks from the nodes that no longer
			// contain these processes
			Set<StringAttribute> oldNodeIds = job.getProcessAttributes(nodeIdAttributeDefinition, processJobRanks);
			oldNodeIds.remove(newNodeId);
			for (StringAttribute oldNodeId : oldNodeIds) {
				IPNode oldNode = getPResourceManager().getNodeById(oldNodeId.getValue());
				if (oldNode != null) {
					oldNode.removeJobProcessRanks(job, processJobRanks);
				}
			}
		}

		/*
		 * Update attributes on the job (where they live for the processes)
		 */
		job.addProcessAttributes(processJobRanks, attrs);

		return true;
	}

	/**
	 * Create the job.
	 * 
	 * @param jobId
	 * @param attrs
	 * @return
	 */
	protected IPJob doCreateJob(String jobId, AttributeManager attrs) {
		return getPResourceManager().newJob(jobId, attrs);
	}

	/**
	 * Create the machine.
	 * 
	 * @param machineId
	 * @param attrs
	 * @return
	 */
	protected IPMachine doCreateMachine(String machineId, AttributeManager attrs) {
		return getPResourceManager().newMachine(machineId, attrs);
	}

	/**
	 * Create the node.
	 * 
	 * @param machine
	 * @param nodeId
	 * @param attrs
	 * @return
	 */
	protected IPNode doCreateNode(IPMachine machine, String nodeId, AttributeManager attrs) {
		return getPResourceManager().newNode(machine, nodeId, attrs);
	}

	/**
	 * Template pattern method to actually create the queue.
	 * 
	 * @param queueId
	 * @return
	 */
	protected IPQueue doCreateQueue(String queueId, AttributeManager attrs) {
		return getPResourceManager().newQueue(queueId, attrs);
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
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		getRuntimeSystem().removeRuntimeEventListener(this);
		removeAll();
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
		getRuntimeSystem().addRuntimeEventListener(this);
	}

	/**
	 * Template pattern method to actually update the jobs.
	 * 
	 * @param job
	 * @param attrs
	 * @return changes were made
	 */
	protected void doUpdateJobs(Collection<IPJob> jobs, AttributeManager attrs) {
		getPResourceManager().addJobAttributes(jobs, attrs.getAttributes());
		for (IPJob job : jobs) {
			getResourceManager().fireJobChanged(job.getID());
		}

	}

	/**
	 * Template pattern method to actually update the machines.
	 * 
	 * @param machine
	 * @param attrs
	 * @return changes were made
	 */
	protected void doUpdateMachines(Collection<IPMachine> machines, AttributeManager attrs) {
		getPResourceManager().addMachineAttributes(machines, attrs.getAttributes());
	}

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
	protected void doUpdateNodes(IPMachine machine, Collection<IPNode> nodes, AttributeManager attrs) {
		machine.addNodeAttributes(nodes, attrs.getAttributes());
	}

	/**
	 * Template pattern method to actually update the queues.
	 * 
	 * @param queue
	 * @param attrs
	 * @return changes were made
	 */
	protected void doUpdateQueues(Collection<IPQueue> queues, AttributeManager attrs) {
		getPResourceManager().addQueueAttributes(queues, attrs.getAttributes());
	}

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
	 * Get the streams proxy associated with the job. Only valid when the job
	 * has started running, otherwise returns null.
	 * 
	 * @param jobId
	 * @return streams proxy for the job, or null if the job is not running
	 */
	protected IStreamsProxy getProxy(String jobId) {
		return fStreamProxies.get(jobId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor#getResourceManager
	 * ()
	 */
	@Override
	protected AbstractRuntimeResourceManager getResourceManager() {
		return (AbstractRuntimeResourceManager) super.getResourceManager();
	}

	protected IRuntimeSystem getRuntimeSystem() {
		return getResourceManager().getRuntimeSystem();
	}
}