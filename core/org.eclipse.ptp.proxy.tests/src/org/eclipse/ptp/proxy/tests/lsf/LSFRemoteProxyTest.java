package org.eclipse.ptp.proxy.tests.lsf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.util.RangeSet;
import org.eclipse.ptp.lsf.core.rtsystem.LSFProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeMessageEvent;
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

public class LSFRemoteProxyTest implements IProxyRuntimeEventListener {
	
	private boolean connected = false;
	private boolean running = false;
	private boolean shutdown = false;
	private ReentrantLock lock = new ReentrantLock();
	private Condition notConnected = lock.newCondition();
	private Condition notRunning = lock.newCondition();
	private Condition notShutdown = lock.newCondition();
	
	private int machine = -1;
	private int rmId = 200;

	@Test public void start_stop() {

		this.connected = false;
		this.running = false;
		this.shutdown = false;
		
		boolean error = false;
		boolean launchManually = false;
		String proxy = "lsf/ptp_lsf_proxy";

		LSFProxyRuntimeClient client = new LSFProxyRuntimeClient(proxy, rmId, launchManually);
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

		assertEquals("LSFRemoteProxyTest: Proxy Client: FAILURE, machine not found",
	              true,
	              machine < 0);

		assertEquals("LSFRemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
	              false,
	              error);

	}

	@Test public void submitJob() {

		this.connected = false;
		this.running = false;
		this.shutdown = false;

		boolean error = false;
		boolean launchManually = false;
		String proxy = "lsf/ptp_lsf_proxy";
		int jobID = 3;
		int nProcs = 4;
		int firstNodeNum = 1;
		int nProcsPerNode = 1;
		
		String exe = "foo";
		String exePath = "/usr/local/bin";
		String rm = "LSF";
		String machine = "flash";
		String queue = "default";
		
		IAttribute[] attr = null;

		String[] configArgs = null;
		String[] env = null;
		String dir = "/home/myworkingdir";
		
		JobRunConfiguration jobRunConfig = new JobRunConfiguration(exe, exePath, rm,
				queue, attr, configArgs, env, dir);

		LSFProxyRuntimeClient client = new LSFProxyRuntimeClient(proxy, rmId, launchManually);
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

				String[] args = getJobArgs(jobID, nProcs, firstNodeNum, nProcsPerNode, jobRunConfig);
				try {
					client.submitJob(args);
				} catch(IOException e) {
					error = true;
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
    	      
		assertEquals("LSFRemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
    	              false,
    	              error);

	}

	private String[] getJobArgs(int jobID, int nProcs, int firstNodeNum, int nProcsPerNode, JobRunConfiguration config) { 

		List<String> argList = new ArrayList<String>();
		
		argList.add("jobID");
		argList.add(Integer.toString(jobID));
		
		argList.add("execName");
		argList.add(config.getExecName());
		String path = config.getPathToExec();
		if (path != null) {
			argList.add("pathToExec");
			argList.add(path);
		}
		argList.add("numOfProcs");
		argList.add(Integer.toString(nProcs));
		argList.add("procsPerNode");
		argList.add(Integer.toString(nProcsPerNode));
		argList.add("firstNodeNum");
		argList.add(Integer.toString(firstNodeNum));
		
		String dir = config.getWorkingDir();
		if (dir != null) {
			argList.add("workingDir");
			argList.add(dir);
		}
		String[] args = config.getArguments();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				argList.add("progArg");
				argList.add(args[i]);
			}
		}
		String[] env = config.getEnvironment();
		if (env != null) {
			for (int i = 0; i < env.length; i++) {
				argList.add("progEnv");
				argList.add(env[i]);
			}
		}
		
		if (config.isDebug()) {
			argList.add("debuggerPath");
			argList.add(config.getDebuggerPath());
			String[] dbgArgs = config.getDebuggerArgs();
			if (dbgArgs != null) {
				for (int i = 0; i < dbgArgs.length; i++) {
					argList.add("debuggerArg");
					argList.add(dbgArgs[i]);
				}
			}
		}

		return argList.toArray(new String[0]);
	}
		
	public void handleProxyRuntimeAttributeDefEvent(IProxyRuntimeAttributeDefEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNewMachineEvent(IProxyRuntimeNewMachineEvent e) {
		String[] args = e.getAttributes();
		if (args.length >= 2) {
			RangeSet machineIds = new RangeSet(args[2]);
			for (int id : machineIds) {
				this.machine = id;
				break;
			}
		}
	}

	public void handleProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNewQueueEvent(IProxyRuntimeNewQueueEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeNodeChangeEvent(IProxyRuntimeNodeChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void handleProxyRuntimeJobChangeEvent(IProxyRuntimeJobChangeEvent e) {
		// TODO Auto-generated method stub
		
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

	public void handleProxyRuntimeMessageEvent(IProxyRuntimeMessageEvent e) {
		System.err.println("got runtime error: " + e.toString());
		
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
	}}
