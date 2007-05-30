package org.eclipse.ptp.proxy.tests.orte;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.orte.core.rtsystem.ORTEProxyRuntimeClient;
import org.eclipse.ptp.orte.core.rtsystem.ORTERuntimeSystem;
import org.eclipse.ptp.rtsystem.IRuntimeEventListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent;
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
import org.junit.Test;

public class ORTERuntimeSystemTest implements IRuntimeEventListener {
	
	private boolean connected = false;
	private boolean running = false;
	private boolean haveQueue = false;
	private boolean jobCompleted = false;
	private boolean shutdown = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notConnected = lock.newCondition();
	private Condition notRunning = lock.newCondition();
	private Condition notHaveQueue = lock.newCondition();
	private Condition notJobCompleted = lock.newCondition();
	private Condition notShutdown = lock.newCondition();
	
	private String queueId = null;
	private String queueName = null;
	private String jobId;
	private String jobSubmitID;
	private final int rmId = 200; /* fake rm ID */

	@Test public void start_stop() {

		this.connected = false;
		this.running = false;
		this.shutdown = false;
		
		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";

		ORTEProxyRuntimeClient client = new ORTEProxyRuntimeClient(proxy, rmId, launchManually);
		ORTERuntimeSystem rtsystem = new ORTERuntimeSystem(client, new AttributeDefinitionManager());
		rtsystem.addRuntimeEventListener(this);
		
		lock.lock();
		try {
			rtsystem.startup();
			while (!connected) {
				notConnected.await();
			}
			System.out.println("test: connected");
			while (!running) {
				notRunning.await();
			}
			System.out.println("test: running");
			rtsystem.shutdown();
			while (!shutdown) {
				notShutdown.await();
			}
			System.out.println("test: shutdown");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

		assertEquals("ORTERemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
	              false,
	              error);

	}

	@Test public void submitJob() {

		this.connected = false;
		this.running = false;
		this.haveQueue = false;
		this.jobCompleted = false;
		this.shutdown = false;

		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";
		int nProcs = 4;
		
		String exe = "ls";
		String exePath = "/bin";
		String rm = "ORTE";
		
		IAttribute[] attr = null;

		String[] configArgs = null;
		String[] env = null;
		String dir = "/etc";
		
		AttributeDefinitionManager attrDefManager = new AttributeDefinitionManager();
		attrDefManager.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		attrDefManager.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
		ORTEProxyRuntimeClient client = new ORTEProxyRuntimeClient(proxy, rmId, launchManually);
		ORTERuntimeSystem rtsystem = new ORTERuntimeSystem(client, attrDefManager);
		rtsystem.addRuntimeEventListener(this);
		
		lock.lock();
		try {
			rtsystem.startup();
			while (!connected) {
				notConnected.await();
			}
			while (!running) {
				notRunning.await();
			}
			try {
				rtsystem.startEvents();
			} catch(CoreException e) {
				error = true;
			}
			
			while (!haveQueue) {
				notHaveQueue.await();
			}

			if (!error) {
				JobRunConfiguration jobRunConfig = new JobRunConfiguration(exe, exePath, rm,
						queueName, attr, configArgs, env, dir);
				
				AttributeManager attrMgr = new AttributeManager();
				
				try {
					attrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(queueId));
					
					attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(jobRunConfig.getExecName()));
					
					String path = jobRunConfig.getPathToExec();
					if (path != null) {
						attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
					}
					
					attrMgr.addAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition().create(nProcs));
							
					String wd = jobRunConfig.getWorkingDir();
					if (wd != null) {
						attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
					}
					
					String[] argArr = jobRunConfig.getArguments();
					if (argArr != null) {
						attrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
					}
					
					String[] envArr = jobRunConfig.getEnvironment();
					if (envArr != null) {
						attrMgr.addAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
					}
					
					System.out.println("about to submit");
					jobSubmitID = rtsystem.submitJob(attrMgr);
				} catch (IllegalValueException e1) {
					error = true;
				} catch(CoreException e) {
					error = true;
				}
			}

			while (!error && !jobCompleted) {
				notJobCompleted.await();
			}
			
			rtsystem.shutdown();
			while (!shutdown) {
				notShutdown.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
    	      
		assertEquals("ORTERemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
    	              false,
    	              error);

	}

	public void handleEvent(IRuntimeAttributeDefinitionEvent e) {
		System.out.println("got attribute def event");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeErrorStateEvent(org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent)
	 */
	public void handleEvent(IRuntimeErrorStateEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeNewJobEvent e) {
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (String id : entry.getKey()) {
				StringAttribute attr = mgr.getAttribute(JobAttributes.getSubIdAttributeDefinition());
				if (attr.getValue().equals(jobSubmitID)) {
					jobId = id.toString();
					return;
				}
			}
		}
	}

	public void handleEvent(IRuntimeNewMachineEvent e) {
		for (RangeSet r : e.getElementAttributeManager().getElementIds()) {
			for (String id : r) {
				System.out.println("new machine " + id);
			}
		}
	}

	public void handleEvent(IRuntimeNewNodeEvent e) {
		String parentId = e.getParentId();
		for (RangeSet r : e.getElementAttributeManager().getElementIds()) {
			for (String id : r) {
				System.out.println("new node " + parentId + " "+ id);
			}
		}
	}

	public void handleEvent(IRuntimeNewQueueEvent e) {
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (String id : entry.getKey()) {
				StringAttribute attr = mgr.getAttribute(ElementAttributes.getNameAttributeDefinition());
				if (attr != null) {
					queueId = id.toString();
					queueName = attr.getValueAsString();
					System.out.println("new queue " + queueName);
				}
			}
		}
		
		if (queueId != null) {
			lock.lock();
			try {
				haveQueue = true;
				notHaveQueue.signal();
			} finally {
				lock.unlock();
			}
		}
	}

	public void handleEvent(IRuntimeNodeChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(IRuntimeJobChangeEvent e) {
		/*
		 * Find a state change, if any
		 */
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (String id : entry.getKey()) {
				if (jobId.equals(id)) {
					EnumeratedAttribute<State> a = mgr.getAttribute(JobAttributes.getStateAttributeDefinition());
					if (a != null && a.getValue() == JobAttributes.State.TERMINATED) {
						System.out.println("job terminated!");
						lock.lock();
						try {
							jobCompleted = true;
							notJobCompleted.signal();
						} finally {
							lock.unlock();
						}
						return;
					}
				}
			}
		}
	}

	public void handleEvent(IRuntimeMachineChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeNewProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeProcessChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeQueueChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeMessageEvent e) {
		System.out.println("got runtime error: " + e.getText());
	}

	public void handleEvent(IRuntimeConnectedStateEvent e) {
		lock.lock();
		try {
			connected = true;
			notConnected.signal();
		} finally {
			lock.unlock();
		}		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveAllEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IRuntimeRemoveAllEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveJobEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveMachineEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveNodeEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveProcessEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(IRuntimeRemoveProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeRemoveQueueEvent(org.eclipse.ptp.rtsystem.events.IRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(IRuntimeRemoveQueueEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IRuntimeRunningStateEvent e) {
		lock.lock();
		try {
			running = true;
			notRunning.signal();
		} finally {
			lock.unlock();
		}
	}

	public void handleEvent(IRuntimeShutdownStateEvent e) {
		lock.lock();
		try {
			shutdown = true;
			notShutdown.signal();
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeStartupErrorEvent(org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeSubmitJobErrorEvent(org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleRuntimeTerminateJobErrorEvent(org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(
			IRuntimeTerminateJobErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

}
