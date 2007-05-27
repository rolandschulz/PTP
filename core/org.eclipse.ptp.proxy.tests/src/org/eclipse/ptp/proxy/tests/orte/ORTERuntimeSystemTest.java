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
					attrMgr.addAttribute(QueueAttributes.getIdAttributeDefinition().create(queueId));
					
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

	public void handleRuntimeAttributeDefinitionEvent(IRuntimeAttributeDefinitionEvent e) {
		System.out.println("got attribute def event");
	}

	public void handleRuntimeNewJobEvent(IRuntimeNewJobEvent e) {
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (Integer id : entry.getKey()) {
				StringAttribute attr = (StringAttribute) mgr.getAttribute(JobAttributes.getSubIdAttributeDefinition());
				if (attr.getValue().equals(jobSubmitID)) {
					jobId = id.toString();
					return;
				}
			}
		}
	}

	public void handleRuntimeNewMachineEvent(IRuntimeNewMachineEvent e) {
		for (RangeSet r : e.getElementAttributeManager().getElementIds()) {
			for (int id : r) {
				System.out.println("new machine " + id);
			}
		}
	}

	public void handleRuntimeNewNodeEvent(IRuntimeNewNodeEvent e) {
		String parentId = e.getParentId();
		for (RangeSet r : e.getElementAttributeManager().getElementIds()) {
			for (int id : r) {
				System.out.println("new node " + parentId + " "+ id);
			}
		}
	}

	public void handleRuntimeNewQueueEvent(IRuntimeNewQueueEvent e) {
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (Integer id : entry.getKey()) {
				StringAttribute attr = (StringAttribute) mgr.getAttribute(ElementAttributes.getNameAttributeDefinition());
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

	public void handleRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	public void handleRuntimeJobChangeEvent(IRuntimeJobChangeEvent e) {
		/*
		 * Find a state change, if any
		 */
		for (Map.Entry<RangeSet, AttributeManager> entry : e.getElementAttributeManager().getEntrySet()) {
			AttributeManager mgr = entry.getValue();
			for (Integer id : entry.getKey()) {
				if (jobId.equals(id)) {
					EnumeratedAttribute<State> a = (EnumeratedAttribute<State>) mgr.getAttribute(JobAttributes.getStateAttributeDefinition());
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

	public void handleRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleRuntimeNewProcessEvent(IRuntimeNewProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleRuntimeMessageEvent(IRuntimeMessageEvent e) {
		System.out.println("got runtime error: " + e.getText());
	}

	public void handleRuntimeConnectedStateEvent(IRuntimeConnectedStateEvent e) {
		lock.lock();
		try {
			connected = true;
			notConnected.signal();
		} finally {
			lock.unlock();
		}		
	}	

	public void handleRuntimeRunningStateEvent(IRuntimeRunningStateEvent e) {
		lock.lock();
		try {
			running = true;
			notRunning.signal();
		} finally {
			lock.unlock();
		}
	}

	public void handleRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent e) {
		lock.lock();
		try {
			shutdown = true;
			notShutdown.signal();
		} finally {
			lock.unlock();
		}
	}

}
