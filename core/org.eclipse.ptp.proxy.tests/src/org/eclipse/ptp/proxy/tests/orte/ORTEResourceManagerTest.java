package org.eclipse.ptp.proxy.tests.orte;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.events.IJobChangedEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueNewJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueJobListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener;
import org.eclipse.ptp.internal.core.elements.PUniverse;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManager;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerFactory;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.junit.Test;

public class ORTEResourceManagerTest implements IResourceManagerQueueListener, IQueueJobListener, IJobListener {
	
	private boolean haveQueue = false;
	private boolean jobCompleted = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notHaveQueue = lock.newCondition();
	private Condition notJobCompleted = lock.newCondition();
	
	private IPQueue subQueue;
	private IPJob subJob;
	private String queueName = null;

	@Test public void start_stop() {

		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";

		ORTEResourceManagerFactory rmf = new ORTEResourceManagerFactory();
		ORTEResourceManagerConfiguration rmc = new ORTEResourceManagerConfiguration(rmf, proxy, launchManually);
		PUniverse universe = new PUniverse();
		ORTEResourceManager rm = new ORTEResourceManager(universe.getNextResourceManagerId(), universe, rmc);
		rm.addChildListener(this);
		
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
		int nProcs = 4;
		
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
		rm.addChildListener(this);
		
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
					attrMgr.addAttribute(QueueAttributes.getIdAttributeDefinition().create(subQueue.getID()));
					
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IJobChangedEvent)
	 */
	@SuppressWarnings("unchecked")
	public void handleEvent(IJobChangedEvent e) {
		/*
		 * Find a state change, if any
		 */
		for (IAttribute a : e.getAttributes()) {
			if (a.getDefinition() == JobAttributes.getStateAttributeDefinition()) {
				if (((EnumeratedAttribute<State>)a).getValue() == State.TERMINATED) {
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


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent)
	 */
	public void handleEvent(IQueueChangedJobEvent e) {
		// Don't care
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueNewJobEvent)
	 */
	public void handleEvent(IQueueNewJobEvent e) {
		subJob = e.getJob();
		System.out.println("got new job: " + subJob.getName());
		subJob.addElementListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent)
	 */
	public void handleEvent(IQueueRemoveJobEvent e) {
		subJob.removeElementListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
	 */
	public void handleEvent(IResourceManagerChangedQueueEvent e) {
		// Don't care
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewQueueEvent)
	 */
	public void handleEvent(IResourceManagerNewQueueEvent e) {
		IPQueue queue = e.getQueue();
		subQueue = queue;
		queueName = queue.getName();
		System.out.println("got queue: " + queueName);
		
		lock.lock();
		try {
			haveQueue = true;
			notHaveQueue.signal();
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
	 */
	public void handleEvent(IResourceManagerRemoveQueueEvent e) {
		// Don't care
	}

}
