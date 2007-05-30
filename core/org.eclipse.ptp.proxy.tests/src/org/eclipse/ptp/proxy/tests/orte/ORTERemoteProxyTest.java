package org.eclipse.ptp.proxy.tests.orte;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.orte.core.rtsystem.ORTEProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeTerminateJobErrorEvent;
import org.junit.Test;

public class ORTERemoteProxyTest implements IProxyRuntimeEventListener {
	
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
	
	private String machineId;
	private String queueId;
	private String queueName = null;
	private final int rmId = 200; /* fake rm ID */

	@Test public void start_stop() {

		this.connected = false;
		this.running = false;
		this.shutdown = false;
		
		boolean error = false;
		boolean launchManually = false;
		String proxy = "orte/ptp_orte_proxy";

		ORTEProxyRuntimeClient client = new ORTEProxyRuntimeClient(proxy, rmId, launchManually);
		client.addProxyRuntimeEventListener(this);
		
		if (client.startup()) {
			lock.lock();
			try {
				while (!connected) {
					notConnected.await();
				}
				while (!running) {
					notRunning.await();
				}
				client.shutdown();
				while (!shutdown) {
					notShutdown.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		} else {
			error = true;
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
		String jobSubID = Long.toString(System.currentTimeMillis());
		int nProcs = 4;
		int firstNodeNum = 1;
		int nProcsPerNode = 1;
		
		String exe = "ls";
		String exePath = "/bin";
		String rm = "ORTE";
		
		IAttribute[] attr = null;

		String[] configArgs = null;
		String[] env = null;
		String dir = "/etc";
		
		ORTEProxyRuntimeClient client = new ORTEProxyRuntimeClient(proxy, rmId, launchManually);
		client.addProxyRuntimeEventListener(this);
		
		if (client.startup()) {
			lock.lock();
			try {
				while (!connected) {
					notConnected.await();
				}
				while (!running) {
					notRunning.await();
				}
				try {
					client.startEvents();
				} catch(IOException e) {
					error = true;
				}
				
				while (!haveQueue) {
					notHaveQueue.await();
				}

				if (!error) {
					JobRunConfiguration jobRunConfig = new JobRunConfiguration(exe, exePath, rm,
							queueName, attr, configArgs, env, dir);

					String[] args = getJobArgs(jobSubID, nProcs, firstNodeNum, nProcsPerNode, jobRunConfig);
					try {
						client.submitJob(args);
					} catch(IOException e) {
						error = true;
					}
				}

				while (!jobCompleted) {
					notJobCompleted.await();
				}
				
				client.shutdown();
				while (!shutdown) {
					notShutdown.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		} else {
			error = true;
		}
    	      
		assertEquals("ORTERemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
    	              false,
    	              error);

	}

	private String[] getJobArgs(String jobSubID, int nProcs, int firstNodeNum, int nProcsPerNode, JobRunConfiguration config) { 

		List<String> argList = new ArrayList<String>();
		
		argList.add(JobAttributes.getSubIdAttributeDefinition().getId() + "=" + jobSubID);
		argList.add(JobAttributes.getQueueIdAttributeDefinition().getId() + "=" + queueId);
		
		argList.add(JobAttributes.getExecutableNameAttributeDefinition().getId() + "=" + config.getExecName());
		String path = config.getPathToExec();
		if (path != null) {
			argList.add(JobAttributes.getExecutablePathAttributeDefinition().getId() + "=" + path);
		}
		argList.add(JobAttributes.getNumberOfProcessesAttributeDefinition().getId() + "=" + Integer.toString(nProcs));
		
		String dir = config.getWorkingDir();
		if (dir != null) {
			argList.add(JobAttributes.getWorkingDirectoryAttributeDefinition().getId() + "=" + dir);
		}
		String[] args = config.getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				argList.add(JobAttributes.getProgramArgumentsAttributeDefinition().getId() + "=" + args[i]);
			}
		}
		String[] env = config.getEnvironment();
		if (env != null) {
			for (int i = 0; i < env.length; i++) {
				argList.add(JobAttributes.getEnvironmentAttributeDefinition().getId() + "=" + env[i]);
			}
		}
		
		if (config.isDebug()) {
			argList.add(JobAttributes.getDebuggerExecutablePathAttributeDefinition().getId() + "=" + config.getDebuggerPath());
			String[] dbgArgs = config.getDebuggerArgs();
			if (dbgArgs != null) {
				for (int i = 0; i < dbgArgs.length; i++) {
					argList.add(JobAttributes.getDebuggerArgumentsAttributeDefinition().getId() + "=" + dbgArgs[i]);
				}
			}
		}

		return argList.toArray(new String[0]);
	}

	public void handleEvent(IProxyRuntimeAttributeDefEvent e) {
		System.out.println("got attribute def event");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeEventListener#handleProxyRuntimeErrorStateEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorStateEvent)
	 */
	public void handleEvent(IProxyRuntimeErrorStateEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeNewJobEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeNewMachineEvent e) {
		String[] args = e.getAttributes();
		if (args.length >= 2) {
			int parentId = Integer.parseInt(args[0]);
			int num = Integer.parseInt(args[1]);
			int pos = 2;
			for (int i = 0; i < num; i++) {
				RangeSet machineIds = new RangeSet(args[pos++]);
				int numArgs = Integer.parseInt(args[pos++]);
				pos += numArgs;
				System.out.println("new machine " + parentId + " " + machineIds.toString());
				for (String id : machineIds) {
					machineId = id;
					break;
				}
			}
		}
	}

	public void handleEvent(IProxyRuntimeNewNodeEvent e) {
		String[] args = e.getAttributes();
		try {
			if (args.length >= 2) {
				int parentId = Integer.parseInt(args[0]);
				int num = Integer.parseInt(args[1]);
				int pos = 2;
				for (int i = 0; i < num; i++) {
					RangeSet nodeIds = new RangeSet(args[pos++]);
					int numArgs = Integer.parseInt(args[pos++]);
					pos += numArgs;
					System.out.println("new node " + parentId + " "+ nodeIds.toString());
				}
			}
		} catch (NumberFormatException ex) {
			System.out.println("new node: bad arg " + args[0]);
		}
	}

	public void handleEvent(IProxyRuntimeNewQueueEvent e) {
		String[] args = e.getAttributes();
		if (args.length >= 2) {
			int parentId = Integer.parseInt(args[0]);
			int num = Integer.parseInt(args[1]);
			int pos = 2;
			for (int i = 0; i < num; i++) {
				RangeSet queueIds = new RangeSet(args[pos++]);
				int numArgs = Integer.parseInt(args[pos++]);
				for (int j = 0; j < numArgs; j++) {
					String[] kv = args[pos++].split("=");
					if (kv.length == 2) {
						System.out.println("new queue " + parentId + " " + queueIds.toString());
						for (String id : queueIds) {
							queueId = id;
							queueName = kv[1];
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
				}
			}
		}
	}

	public void handleEvent(IProxyRuntimeNodeChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeJobChangeEvent e) {
		String[] args = e.getAttributes();
		if (args.length >= 2) {
			int num = Integer.parseInt(args[0]);
			int pos = 1;
			for (int i = 0; i < num; i++) {
				RangeSet jobIds = new RangeSet(args[pos++]);
				int numArgs = Integer.parseInt(args[pos++]);
				for (int j = 0; j < numArgs; j++) {
				/*
				 * Find a state change, if any
				 */
					String[] kv = args[pos++].split("=");
					if (kv.length == 2 && kv[0].equals(JobAttributes.getStateAttributeDefinition().getId())){
						try {
							if (kv[1].equals(JobAttributes.State.TERMINATED.toString())) {
								System.out.println("job terminated!");
								lock.lock();
								try {
									jobCompleted = true;
									notJobCompleted.signal();
								} finally {
									lock.unlock();
								}
							}
						} catch (NumberFormatException e1) {
						}
					}
				}
			}
		}
	}

	public void handleEvent(IProxyRuntimeMachineChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeNewProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeProcessChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeQueueChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeMessageEvent e) {
		System.out.println("got runtime error: " + e.toString());
	}

	public void handleEvent(IProxyRuntimeConnectedStateEvent e) {
		lock.lock();
		try {
			connected = true;
			notConnected.signal();
		} finally {
			lock.unlock();
		}		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.IProxyRuntimeEventListener#handleProxyRuntimeRemoveAllEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveAllEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveJobEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveJobEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveMachineEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(
			IProxyRuntimeRemoveMachineEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveNodeEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IProxyRuntimeRemoveNodeEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveProcessEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(
			IProxyRuntimeRemoveProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeRemoveQueueEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(
			IProxyRuntimeRemoveQueueEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleEvent(IProxyRuntimeRunningStateEvent e) {
		lock.lock();
		try {
			running = true;
			notRunning.signal();
		} finally {
			lock.unlock();
		}
	}

	public void handleEvent(IProxyRuntimeShutdownStateEvent e) {
		lock.lock();
		try {
			shutdown = true;
			notShutdown.signal();
		} finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeStartupErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeStartupErrorEvent)
	 */
	public void handleEvent(
			IProxyRuntimeStartupErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeSubmitJobErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(
			IProxyRuntimeSubmitJobErrorEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener#handleProxyRuntimeTerminateJobErrorEvent(org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(
			IProxyRuntimeTerminateJobErrorEvent e) {
		// TODO Auto-generated method stub
		
	}
}
