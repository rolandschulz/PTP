package org.eclipse.ptp.proxy.tests.orte;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.internal.core.PUniverse;
import org.eclipse.ptp.orte.core.ORTEAttributes;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManager;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerFactory;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewQueuesEvent;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.junit.Test;

public class ORTEResourceManagerTest implements IResourceManagerListener {
	
	private boolean haveQueue = false;
	private boolean jobCompleted = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notHaveQueue = lock.newCondition();
	private Condition notJobCompleted = lock.newCondition();
	
	private IPQueue subQueue;
	private String queueName = null;

	@Test public void start_stop() {

		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";

		ORTEResourceManagerFactory rmf = new ORTEResourceManagerFactory();
		ORTEResourceManagerConfiguration rmc = new ORTEResourceManagerConfiguration(rmf, proxy, launchManually);
		PUniverse universe = new PUniverse();
		ORTEResourceManager rm = new ORTEResourceManager(universe.getNextResourceManagerId(), universe, rmc);
		rm.addResourceManagerListener(this);
		
		try {
			System.out.println("rm: starting");
			rm.startUp(null);
			System.out.println("rm: started");
			rm.shutdown();
			System.out.println("rm: shutdown");
		} catch (CoreException e) {
			e.printStackTrace();
		}

		assertEquals("ORTERemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
	              false,
	              error);

	}

	@Test public void submitJob() {

		this.haveQueue = false;
		this.jobCompleted = false;

		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";
		int jobID = 3;
		int nProcs = 4;
		int firstNodeNum = 1;
		int nProcsPerNode = 1;
		
		String exe = "ls";
		String exePath = "/bin";
		
		IAttribute[] attr = null;

		String[] configArgs = null;
		String[] env = null;
		String dir = "/etc";
		
		ORTEResourceManagerFactory rmf = new ORTEResourceManagerFactory();
		ORTEResourceManagerConfiguration rmc = new ORTEResourceManagerConfiguration(rmf, proxy, launchManually);
		PUniverse universe = new PUniverse();
		ORTEResourceManager rm = new ORTEResourceManager(universe.getNextResourceManagerId(), universe, rmc);
		rm.addResourceManagerListener(this);
		
		lock.lock();
		try {
			rm.startUp(null);
			System.out.println("rm: started");
			
			while (!haveQueue) {
				notHaveQueue.await();
			}

			if (!error) {
				JobRunConfiguration jobRunConfig = new JobRunConfiguration(exe, exePath, "ORTE",
						queueName, attr, configArgs, env, dir);

				AttributeManager attrMgr = new AttributeManager();
				
				try {
					attrMgr.setAttribute(QueueAttributes.getIdAttributeDefinition().create(subQueue.getID()));
					
					attrMgr.setAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(jobRunConfig.getExecName()));
					
					String path = jobRunConfig.getPathToExec();
					if (path != null) {
						attrMgr.setAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
					}
					
					attrMgr.setAttribute(ORTEAttributes.getNumberOfProcessesAttributeDefinition().create(nProcs));
							
					String wd = jobRunConfig.getWorkingDir();
					if (wd != null) {
						attrMgr.setAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
					}
					
					String[] argArr = jobRunConfig.getArguments();
					if (argArr != null) {
						attrMgr.setAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
					}
					
					String[] envArr = jobRunConfig.getEnvironment();
					if (envArr != null) {
						attrMgr.setAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
					}
					
					if (jobRunConfig.isDebug()) {
						attrMgr.setAttribute(JobAttributes.getDebuggerBackendPathAttributeDefinition().create(jobRunConfig.getDebuggerPath()));
						String[] dbgArgs = jobRunConfig.getDebuggerArgs();
						if (dbgArgs != null) {
							attrMgr.setAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition().create(dbgArgs));
						}
					}
					
					System.out.println("about to submit");
					rm.submitJob(attrMgr, new NullProgressMonitor());
				} catch (IllegalValueException e1) {
					error = true;
				} catch(CoreException e) {
					error = true;
				}
			}

			while (!jobCompleted && !error) {
				notJobCompleted.await();
			}
			
			rm.shutdown();
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

	public void handleErrorState(IResourceManager manager) {
		// TODO Auto-generated method stub
		
	}

	public void handleSuspendedState(AbstractResourceManager manager) {
		// TODO Auto-generated method stub
		
	}

	public void handleChangedJobsEvent(IResourceManagerChangedJobsEvent e) {
		/*
		 * Find a state change, if any
		 */
		for (IAttribute a : e.getChangedAttributes()) {
			if (a.getDefinition() == JobAttributes.getStateAttributeDefinition()) {
				if (((EnumeratedAttribute)a).getEnumValue() == JobAttributes.State.TERMINATED) {
					System.out.println("job terminated!");
					lock.lock();
					try {
						jobCompleted = true;
						notJobCompleted.signal();
					} finally {
						lock.unlock();
					}
					break;
				}
			}
		}
	}

	public void handleChangedMachinesEvent(IResourceManagerChangedMachinesEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleChangedNodesEvent(IResourceManagerChangedNodesEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleChangedProcessesEvent(IResourceManagerChangedProcessesEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleChangedQueuesEvent(IResourceManagerChangedQueuesEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleErrorStateEvent(IResourceManagerErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleNewJobsEvent(IResourceManagerNewJobsEvent e) {

	}

	public void handleNewMachinesEvent(IResourceManagerNewMachinesEvent e) {
		for (IPMachine machine : e.getNewMachines()) {
			System.out.println("new machine " + machine.getName());
		}
	}

	public void handleNewNodesEvent(IResourceManagerNewNodesEvent e) {
		for (IPNode node : e.getNewNodes()) {
			System.out.println("new node " + node.getName());
		}
	}

	public void handleNewProcessesEvent(IResourceManagerNewProcessesEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleNewQueuesEvent(IResourceManagerNewQueuesEvent e) {
		Collection<IPQueue> queues = e.getNewQueues();
		for (IPQueue queue : queues) {
			subQueue = queue;
			queueName = queue.getName();
			System.out.println("got queue: " + queueName);
			break;
		}
		
		lock.lock();
		try {
			haveQueue = true;
			notHaveQueue.signal();
		} finally {
			lock.unlock();
		}

	}

	public void handleShutdownStateEvent(IResourceManager resourceManager) {
		// TODO Auto-generated method stub
		
	}

	public void handleStartupStateEvent(IResourceManager resourceManager) {
		System.out.println("got startup state");
	}

	public void handleSuspendedStateEvent(AbstractResourceManager manager) {
		// TODO Auto-generated method stub
		
	}

}
