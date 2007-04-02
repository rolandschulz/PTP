/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.debug.ui.tests.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.ui.testplugin.PTPDebugHelper;
import org.eclipse.ptp.debug.ui.tests.AbstractDebugTest;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * @author clement
 */
public abstract class AbstractBaseTest extends AbstractDebugTest implements IProxyDebugEventListener {
	protected final String testAppName = "main";
	protected final String testApp = testAppName + ".c";
	protected final String appPath = "/home/clement/Desktop/runtime-EclipseApplication/TestVar/Debug";
	//common
	protected final long timeout = 5000;
	protected BitList tasks = null;
	protected boolean waitAgain = false;
	protected Object LOCK = new Object();
	protected NullProgressMonitor monitor;
	protected ProxyDebugClient debugProxy = null;
	//command
	protected List<BitList> completedTasks = new ArrayList<BitList>();
	protected List<Object> completedReturns = new ArrayList<Object>();
	
	public AbstractBaseTest(String name, int nProcs, int firstNode, int NProcsPerNode) {
		super(name, nProcs, firstNode, NProcsPerNode);
		monitor = new NullProgressMonitor();
	}
	protected void tearDown() throws CoreException, IOException, PCDIException {
		if (debugProxy != null) {
			debugProxy.removeEventListener(this);
			debugProxy.sessionFinish();
			debugProxy.closeConnection();
		}
		debugProxy = null;
		tasks = null;
		super.tearDown();
	}
	public void startDebugServer() throws CoreException, IOException, InterruptedException {
		JobRunConfiguration jobConfig;
		int port = 0;

		assertTrue(new File(appPath).exists());
		debugProxy = new ProxyDebugClient();
		assertNotNull(debugProxy);
		debugProxy.sessionCreate();
		port = debugProxy.getSessionPort();
		assertTrue(port > 1000);
		
		jobConfig = PTPDebugHelper.getJobDebugConfiguration(appPath, testAppName, resourceMgrName, machineName, queueName, nProcs, firstNode, NProcsPerNode, "test", "127.0.0.1", port, "");
		assertNotNull(jobConfig);
		
		assertTrue(debugProxy.waitForConnect(monitor));
		debugProxy.addEventListener(this);

		assertTrue(new File(jobConfig.getPathToExec()).exists());
		debugProxy.debugStartSession(jobConfig.getExecName(), jobConfig.getPathToExec(), jobConfig.getWorkingDir(), jobConfig.getArguments());
		BitList t = createBitList();
		//wait suspend event
		waitEvent(t);
		assertTrue("Debugger is initialized: " + t.isEmpty(), t.isEmpty());
	}
	protected BitList createBitList() {
		BitList tasks = new BitList(nProcs);
		tasks.set(0, nProcs);
		return tasks;
	}
	protected BitList createBitList(int index) throws PCDIException {
		if (index < 0 || index > nProcs)
			throw new PCDIException("Invalid process number.");
		BitList tasks = new BitList(nProcs);
		tasks.set(index);
		return tasks;
	}
	protected BitList createBitList(int from, int to) throws PCDIException {
		if (from < 0 || to > nProcs)
			throw new PCDIException("Invalid process number.");
		BitList tasks = new BitList(nProcs);
		tasks.set(from, to);
		return tasks;
	}
	protected void waitEvent(BitList tasks) throws InterruptedException {
		synchronized (LOCK) {
			this.tasks = tasks;
			this.completedTasks.clear();
			this.completedReturns.clear();
			do {
				waitAgain = false;
				LOCK.wait(timeout);
			} while (waitAgain);
		}
	}
    protected void lockAgain() {
    	synchronized (LOCK) {
    		waitAgain = true;
    		LOCK.notify();
    	}
    }
	protected void notifyEvent(BitList evtTasks, Object result) {
		synchronized (LOCK) {
			completedTasks.add(evtTasks.copy());
			completedReturns.add(result);

			tasks.andNot(evtTasks);
			if (tasks.isEmpty()) {
				releaseLock();
			}
			else {
				//if return tasks is not equal to command tasks, wait again
				lockAgain();
			}
		}
	}
    protected void releaseLock() {
    	synchronized (LOCK) {
    		waitAgain = false;
    		LOCK.notifyAll();
    	}
    }
	/******************************************************
	 *  Event
	 ******************************************************/
	public synchronized void handleEvent(IProxyDebugEvent e) {
		switch (e.getEventID()) {
		case IProxyDebugEvent.EVENT_DBG_SUSPEND:
			if (e instanceof ProxyDebugBreakpointHitEvent) {
				//ProxyDebugBreakpointHitEvent bptHitEvent = (ProxyDebugBreakpointHitEvent)e;
			} else if (e instanceof ProxyDebugSuspendEvent) {
				//ProxyDebugSuspendEvent suspendEvent = (ProxyDebugSuspendEvent)e;
			} else if (e instanceof ProxyDebugStepEvent) {
				//ProxyDebugStepEvent stepEvent = (ProxyDebugStepEvent)e;
			} else if (e instanceof ProxyDebugSignalEvent) {
				//ProxyDebugSignalEvent sigEvent = (ProxyDebugSignalEvent)e;
			}
			System.err.println("EVENT_DBG_SUSPEND");
			notifyEvent(e.getBitSet(), null);
			break;
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			System.err.println("EVENT_DBG_BPSET: " + ((ProxyDebugBreakpointSetEvent)e).getBreakpointId());
			notifyEvent(e.getBitSet(), null);
			break;
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			System.err.println("EVENT_DBG_EXIT");
			notifyEvent(e.getBitSet(), null);
			break;
		case IProxyDebugEvent.EVENT_DBG_EXIT_SIGNAL:
			//ProxyDebugSignalExitEvent exitSigEvent = (ProxyDebugSignalExitEvent)e;
			break;
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			ProxyDebugErrorEvent errEvent = (ProxyDebugErrorEvent)e;
			System.err.println("Event Debug Error - code: " + errEvent.getErrorCode() + ", msg: " + errEvent.getErrorMessage());
			break;
		case IProxyDebugEvent.EVENT_DBG_OK:
			System.err.println("EVENT_DBG_OK");
			notifyEvent(e.getBitSet(), null);
			break;
		case IProxyDebugEvent.EVENT_DBG_INIT:
			System.err.println("EVENT_DBG_INIT");
			break;
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			//(ProxyDebugStackframeEvent)e);
			break;
		case IProxyDebugEvent.EVENT_DBG_TYPE:
			//(ProxyDebugTypeEvent)e);
			break;
		case IProxyDebugEvent.EVENT_DBG_DATA:
			System.err.println("EVENT_DBG_DATA");
			notifyEvent(e.getBitSet(), ((ProxyDebugDataEvent)e).getData());
			break;
		case IProxyDebugEvent.EVENT_DBG_VARS:
			//(ProxyDebugVarsEvent)e);			
			break;
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			//(ProxyDebugArgsEvent)e);			
			break;
		case IProxyDebugEvent.EVENT_DBG_SIGNALS:
			//ProxyDebugSignalsEvent signalsEvent = (ProxyDebugSignalsEvent)e;
			break;
		case IProxyDebugEvent.EVENT_DBG_THREADS:
			//(ProxyDebugInfoThreadsEvent)e);			
			break;
		case IProxyDebugEvent.EVENT_DBG_THREAD_SELECT:
			//ProxyDebugSetThreadSelectEvent setThreadSelectEvent = (ProxyDebugSetThreadSelectEvent)e;
			break;
		case IProxyDebugEvent.EVENT_DBG_STACK_INFO_DEPTH:
			//ProxyDebugStackInfoDepthEvent stackInfoDepthEvent = (ProxyDebugStackInfoDepthEvent)e;
			break;
		case IProxyDebugEvent.EVENT_DBG_DATA_READ_MEMORY:
			//ProxyDebugMemoryInfoEvent memoryInfoEvent = (ProxyDebugMemoryInfoEvent)e;
			break;
		case IProxyDebugEvent.EVENT_DBG_DATA_EVA_EX:
			//(ProxyDebugDataExpValueEvent)e);
			break;
		case IProxyDebugEvent.EVENT_DBG_PARTIAL_AIF:
			//(ProxyDebugPartialAIFEvent)e);
			break;
		default:
			System.err.println("UNKNOWN EVENT TYPE");
			break;
		}
	}
}
