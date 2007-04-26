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
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
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
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeShutdownStateEvent;
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
	
	private int machineId;
	private int queueId;
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
		int jobID = 3;
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

					String[] args = getJobArgs(jobID, nProcs, firstNodeNum, nProcsPerNode, jobRunConfig);
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

	private String[] getJobArgs(int jobID, int nProcs, int firstNodeNum, int nProcsPerNode, JobRunConfiguration config) { 

		List<String> argList = new ArrayList<String>();
		
		argList.add("jobID=" + Integer.toString(jobID));
		argList.add("queueID=" + Integer.toString(queueId));
		
		argList.add("execName=" + config.getExecName());
		String path = config.getPathToExec();
		if (path != null) {
			argList.add("pathToExec=" + path);
		}
		argList.add("numOfProcs=" + Integer.toString(nProcs));
		argList.add("procsPerNode=" + Integer.toString(nProcsPerNode));
		argList.add("firstNodeNum=" + Integer.toString(firstNodeNum));
		
		String dir = config.getWorkingDir();
		if (dir != null) {
			argList.add("workingDir=" + dir);
		}
		String[] args = config.getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				argList.add("progArg=" + args[i]);
			}
		}
		String[] env = config.getEnvironment();
		if (env != null) {
			for (int i = 0; i < env.length; i++) {
				argList.add("progEnv=" + env[i]);
			}
		}
		
		if (config.isDebug()) {
			argList.add("debuggerPath=" + config.getDebuggerPath());
			String[] dbgArgs = config.getDebuggerArgs();
			if (dbgArgs != null) {
				for (int i = 0; i < dbgArgs.length; i++) {
					argList.add("debuggerArg=" + dbgArgs[i]);
				}
			}
		}

		return argList.toArray(new String[0]);
	}

	public void handleProxyRuntimeAttributeDefEvent(IProxyRuntimeAttributeDefEvent e) {
		System.out.println("got attribute def event");
	}

	public void handleProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNewMachineEvent(IProxyRuntimeNewMachineEvent e) {
		String[] args = e.getArguments();
		if (args.length >= 2) {
			int parentId = Integer.parseInt(args[0]);
			int num = Integer.parseInt(args[1]);
			int pos = 2;
			for (int i = 0; i < num; i++) {
				RangeSet machineIds = new RangeSet(args[pos++]);
				int numArgs = Integer.parseInt(args[pos++]);
				pos += numArgs;
				System.out.println("new machine " + parentId + " " + machineIds.toString());
				for (int id : machineIds) {
					machineId = id;
					break;
				}
			}
		}
	}

	public void handleProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent e) {
		String[] args = e.getArguments();
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

	public void handleProxyRuntimeNewQueueEvent(IProxyRuntimeNewQueueEvent e) {
		String[] args = e.getArguments();
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
						for (int id : queueIds) {
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

	public void handleProxyRuntimeNodeChangeEvent(IProxyRuntimeNodeChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeJobChangeEvent(IProxyRuntimeJobChangeEvent e) {
		String[] args = e.getArguments();
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
					if (kv.length == 2 && kv[0].equals(JobAttributes.STATE_ATTR_ID)){
						try {
							if (kv[1].equals(JobAttributes.State.ABORTED.toString())) {
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

	public void handleProxyRuntimeMachineChangeEvent(IProxyRuntimeMachineChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNewProcessEvent(IProxyRuntimeNewProcessEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeProcessChangeEvent(IProxyRuntimeProcessChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeQueueChangeEvent(IProxyRuntimeQueueChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeErrorEvent(IProxyRuntimeErrorEvent e) {
		System.out.println("got runtime error: " + e.getDescription());
	}

	public void handleProxyRuntimeConnectedStateEvent(IProxyRuntimeConnectedStateEvent e) {
		lock.lock();
		try {
			connected = true;
			notConnected.signal();
		} finally {
			lock.unlock();
		}		
	}	

	public void handleProxyRuntimeRunningStateEvent(IProxyRuntimeRunningStateEvent e) {
		lock.lock();
		try {
			running = true;
			notRunning.signal();
		} finally {
			lock.unlock();
		}
	}

	public void handleProxyRuntimeShutdownStateEvent(IProxyRuntimeShutdownStateEvent e) {
		lock.lock();
		try {
			shutdown = true;
			notShutdown.signal();
		} finally {
			lock.unlock();
		}
	}
}
