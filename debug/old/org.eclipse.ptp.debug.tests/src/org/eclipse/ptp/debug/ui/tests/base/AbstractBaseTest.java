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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugArgsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugDataExpValueEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugEventListener;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInfoThreadsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugInitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugMemoryInfoEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugOKEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugPartialAIFEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSetThreadSelectEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalExitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSignalsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackInfoDepthEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStackframeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugTypeEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.IProxyDebugVarsEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugBreakpointSetEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugDataEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugErrorEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSignalEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugStepEvent;
import org.eclipse.ptp.debug.external.core.proxy.event.ProxyDebugSuspendEvent;
import org.eclipse.ptp.debug.ui.tests.AbstractDebugTest;

/**
 * @author clement
 */
public abstract class AbstractBaseTest extends AbstractDebugTest implements IProxyDebugEventListener {
	protected final String testExecName = "TestFromGreg";
	protected final String testAppName = "fromgreg.c";
	protected final String workPath = "/home/clement/Desktop/runtime-EclipseApplication/TestFromGreg";
	protected final String execPath = workPath + "/Debug";
	protected final String DebugHost = "127.0.0.1";
	protected final String DebugType = "gdb-mi"; //gdb-mi or test
	//common
	protected final long timeout = 60000; //default 1 hour
	protected BitList tasks = null;
	protected boolean time_up = false;
	protected boolean waitAgain = false;
	protected NullProgressMonitor monitor;
	protected ProxyDebugClient debugProxy = null;
	protected final ReentrantLock waitLock = new ReentrantLock();
	protected final Condition waitCondition = waitLock.newCondition();
	//command
	protected List<BitList> completedTasks = new ArrayList<BitList>();
	protected List<Object> completedReturns = new ArrayList<Object>();
	//time
	protected long time_start = 0;
	protected long time_end = 0;
	
	/** Constructor for non-GUI test
	 * @param name
	 * @param nProcs
	 * @param firstNode
	 * @param NProcsPerNode
	 */
	public AbstractBaseTest(String name, int nProcs, int firstNode, int NProcsPerNode) {
		super(name, nProcs, firstNode, NProcsPerNode);
		monitor = new NullProgressMonitor();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.tests.AbstractDebugTest#tearDown()
	 */
	protected void tearDown() throws CoreException, IOException, PCDIException {
		if (debugProxy != null) {
			System.err.println("-------------- tearDown() is called -----------------");
			debugProxy.removeProxyDebugEventListener(this);
			debugProxy.doShutdown();
		}
		debugProxy = null;
		tasks = null;
		super.tearDown();
	}
	/** Get time format
	 * @param ms milliseconds
	 * @return String format days hours minutes seconds milliseconds
	 */
	private String getTimeFormat(long ms) {
		//System.err.println("##### TEST TIME: " + ms);
		int d = 0;
		int h = 0;
		int m = 0;
		int s = 0;
		String format = "";
		if (ms < 1000) {
			format += ms + " ms";
			return format;
		}
		s = (int)(ms / 1000);
		ms = ms - (s * 1000);
		if (s < 60) {
			format += s + " s ";
			format += ms + " ms";
			return format;
		}
		m = s / 60;
		s = s - (m * 60);
		if (m < 60) {
			format += m + " min ";
			format += s + " s ";
			format += ms + " ms";
			return format;
		}
		h = m / 60;
		m = m - (h * 60);
		if (h < 24) {
			format += h + " hr ";
			format += m + " min ";
			format += s + " s ";
			format += ms + " ms";
			return format;
		}
		d = h / 24;
		h = h - (d * 24); 
		format += d + " day ";
		format += h + " hr ";
		format += m + " min ";
		format += s + " s ";
		format += ms + " ms";
		return format;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.tests.AbstractDebugTest#startDebugServer()
	 */
	public void startDebugServer() throws CoreException, IOException, InterruptedException {
		int port = 0;

		assertTrue(new File(workPath).exists());
		assertTrue(new File(execPath).exists());
		
		debugProxy = new ProxyDebugClient();
		assertNotNull(debugProxy);
		debugProxy.addProxyDebugEventListener(this);
		debugProxy.doConnect(10000);
		port = debugProxy.getSessionPort();
		assertTrue(port > 1000);

		String[] debugArgs = new String[3]; 
		debugArgs[0] = "--host="+DebugHost;
		debugArgs[1] = "--debugger="+DebugType;
		debugArgs[2] = "--port=" + port;
		System.err.println("*** If you start JUnit test with manually launch sdm, please type the following on command line ***");
		System.err.print(">>> mpirun -np " + (nProcs+1) + " ./sdm ");
		for (String arg : debugArgs) {
			System.err.print(arg + " ");
		}
		System.err.println();

		assertTrue(debugProxy.waitConnect(monitor));
		debugProxy.sessionHandleEvents();

		debugProxy.debugStartSession(testExecName, execPath, execPath, new String[0]);
		BitList t = createBitList();
		//wait suspend event
		waitEvent(t);
		assertTrue("Debugger is initialized: " + t.isEmpty(), t.isEmpty());
	}
	/** Create BitList with all processes
	 * @return
	 */
	protected BitList createBitList() {
		BitList tasks = new BitList(nProcs);
		tasks.set(0, nProcs);
		return tasks;
	}
	/** Create BitList with given task id
	 * @param index
	 * @return
	 * @throws PCDIException
	 */
	protected BitList createBitList(int index) throws PCDIException {
		if (index < 0 || index > nProcs)
			throw new PCDIException("Invalid process number.");
		BitList tasks = new BitList(nProcs);
		tasks.set(index);
		return tasks;
	}
	/** Create BitList with range of given task ids
	 * @param from
	 * @param to
	 * @return
	 * @throws PCDIException
	 */
	protected BitList createBitList(int from, int to) throws PCDIException {
		if (from < 0 || to > nProcs)
			throw new PCDIException("Invalid process number.");
		BitList tasks = new BitList(nProcs);
		tasks.set(from, to);
		return tasks;
	}
	/** Wait Event after send a command
	 * @param tasks
	 * @throws InterruptedException
	 */
	protected void waitEvent(BitList tasks) throws InterruptedException {
		waitLock.lock();
		try {
			this.tasks = tasks;
			completedTasks.clear();
			completedReturns.clear();
			time_up = true; 
			time_start = System.currentTimeMillis();
			do {
				waitAgain = false;
				waitCondition.await(timeout, TimeUnit.MILLISECONDS);
			} while (waitAgain);
			time_end = System.currentTimeMillis();
			if (time_up) {
				fail("#### TIME OUT ####");
			}
			printTime(time_start, time_end);
		} finally {
			waitLock.unlock();
		}
	}
	/** print time
	 * @param start_t
	 * @param end_t
	 */
	protected void printTime(long start_t, long end_t) {
		System.err.println("##### SPENT TIME: " + getTimeFormat(end_t - start_t));
	}
	/** event back
	 * @param evtTasks
	 * @param result
	 */
	protected void notifyEvent(BitList evtTasks, Object result) {
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
    /** re-lock
     * 
     */
    protected void lockAgain() {
		waitLock.lock();
		try {
	    	waitAgain = true;
			waitCondition.signal();
		} finally {
			waitLock.unlock();
		}
    }
    /** unlock
     * 
     */
    protected void releaseLock() {
		waitLock.lock();
		try {
	    	waitAgain = false;
	   		time_up = false;
			waitCondition.signal();
		} finally {
			waitLock.unlock();
		}
    }
    
    /**** IProxyDebugEventListener ****/
	public void handleProxyDebugArgsEvent(IProxyDebugArgsEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugArgsEvent");
	}
	public void handleProxyDebugBreakpointHitEvent(IProxyDebugBreakpointHitEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugBreakpointHitEvent");
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugBreakpointSetEvent(IProxyDebugBreakpointSetEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugBreakpointSetEvent: " + e.getBreakpointId());
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugDataEvent(IProxyDebugDataEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugDataEvent");
		notifyEvent(e.getBitSet(), e.getData());
	}
	public void handleProxyDebugDataExpValueEvent(IProxyDebugDataExpValueEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugDataExpValueEvent");
	}
	public void handleProxyDebugExitEvent(IProxyDebugExitEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugExitEvent - " + e.getExitStatus());
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugErrorEvent(IProxyDebugErrorEvent e) {
		System.err.println("!!!!!!!!! Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugErrorEvent - code: " + e.getErrorCode() + ", msg: " + e.getErrorMessage() + " !!!!!!!!!!!");
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugInfoThreadsEvent(IProxyDebugInfoThreadsEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugInfoThreadsEvent");
	}
	public void handleProxyDebugInitEvent(IProxyDebugInitEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugInitEvent");
	}
	public void handleProxyDebugMemoryInfoEvent(IProxyDebugMemoryInfoEvent e) {
		System.err.println("IProxyDebugMemoryInfoEvent");
	}
	public void handleProxyDebugOKEvent(IProxyDebugOKEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugOKEvent");
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugPartialAIFEvent(IProxyDebugPartialAIFEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugPartialAIFEvent");
	}
	public void handleProxyDebugSetThreadSelectEvent(IProxyDebugSetThreadSelectEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugSetThreadSelectEvent");
	}
	public void handleProxyDebugSignalEvent(IProxyDebugSignalEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugSignalEvent");
	}
	public void handleProxyDebugSignalExitEvent(IProxyDebugSignalExitEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugSignalExitEvent");
	}
	public void handleProxyDebugSignalsEvent(IProxyDebugSignalsEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugSignalsEvent");
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugStackframeEvent(IProxyDebugStackframeEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugStackframeEvent");
	}
	public void handleProxyDebugStackInfoDepthEvent(IProxyDebugStackInfoDepthEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugStackInfoDepthEvent");
	}
	public void handleProxyDebugStepEvent(IProxyDebugStepEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugStepEvent");
	}
	public void handleProxyDebugSuspendEvent(IProxyDebugSuspendEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugSuspendEvent");
		notifyEvent(e.getBitSet(), null);
	}
	public void handleProxyDebugTypeEvent(IProxyDebugTypeEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugTypeEvent");
	}
	public void handleProxyDebugVarsEvent(IProxyDebugVarsEvent e) {
		System.err.println("=== Tasks: " + BitList.showBitList(e.getBitSet()) + ": IProxyDebugVarsEvent");
	}

	/*********************************** 
	 * @deprecated
	 ***********************************/
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
