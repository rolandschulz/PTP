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

package org.eclipse.ptp.debug.external.debugger;

import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.utils.Queue;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.PCDIException;
import org.eclipse.ptp.internal.core.CoreUtils;


public class ParallelDebugger extends AbstractDebugger {
	
	public class DebugEventThread extends Thread {
		AbstractDebugger dbg;
		
		public DebugEventThread(AbstractDebugger d) {
			super("IDebugger Event Thread"); //$NON-NLS-1$
			dbg = d;
		}
		
		public void run() {
			// Signal by the session of time to die.
			while (dbg.isExiting() != true) {
				IPCDIEvent event = null;
				int ev;
				// Wait for event from external debugger
				// DbgProgress(callback);
				
				// Convert to IPCDIEvent
				/*
				switch (ev) {
				case DBGEVENT_BPHIT:
					break;
					
				case DBGEVENT_STEPCOMPLETED:
					break
				
				case DBGEVENT_EXIT:
				}
				*/
				Queue eventQueue = dbg.getEventQueue();
				// removeItem() will block until an item is available.
				eventQueue.addItem(event);
			}
			System.out.println("EventThread exits");
		}
	}
	
	private DebugEventThread eventThread;
	private long cmdTimeout = 1000; // FIXME
	
	public native int DbgGo(int[] procs);
	
	private static int failed_load = 0;

	static {
        try { 
        		System.loadLibrary("ptp_dbg_jni");
        } catch(UnsatisfiedLinkError e) {
        		String str = "Unable to load library 'ptp_dbg_jni'.  Make sure "+
        				"the library exists and the VM arguments point to the directory where "+
        				"it resides.  In the 'Run...' set the VM Args to something like "+
        				"-Djava.library.path=[home directory]/[eclipse workspace]/org.eclipse.ptp.core/ompi";
        		System.err.println(str);
        		System.err.println(e.getMessage());
        		CoreUtils.showErrorDialog("Dynamic Library Load Failed", str, null);
        		failed_load = 1;
        }
    }

	protected void startDebugger(IPJob job) {
		if(failed_load == 1) {
			System.err.println("Unable to startup debugger because of a failed library load.");
			return;
		}
		//eventThread = new DebugEventThread(this);
	}
	
	protected void stopDebugger() {
		// Kill the event Thread ... if it is not us.
		if (!eventThread.equals(Thread.currentThread())) {			
			// Kill the event Thread.
			try {
				if (eventThread.isAlive()) {
					eventThread.interrupt();
					eventThread.join(cmdTimeout);
				}
			} catch (InterruptedException e) {
			}		
		}
	}
	
	public Process getDebuggerProcess() {
		return null;
	}
	
	public void restart() throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "restart");
	}
	
	public void run(String[] args) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "run");		
	}
	
	public void go(IPCDIDebugProcessSet procs) throws PCDIException {
		int rc = DbgGo(null);
		if (rc != 0)
			throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "go");
	}

	public void kill(IPCDIDebugProcessSet procs) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "kill");
	}

	public void halt(IPCDIDebugProcessSet procs) throws PCDIException {
	}
	
	public void stepInto(IPCDIDebugProcessSet procs, int count) throws PCDIException {
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws PCDIException {
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws PCDIException {
		throw new PCDIException(PCDIException.NOT_IMPLEMENTED, "stepFinish");
	}

	public void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDILineBreakpoint bpt) throws PCDIException {
	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIFunctionBreakpoint bpt) throws PCDIException {
	}

	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws PCDIException {
	}
	
	/**
	 * list stack frames for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws PCDIException {
		return null;
	}
	
	public void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		
	}
	
	/**
	 * evaluate expression for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public String evaluateExpression(IPCDIDebugProcessSet procs, String expr) throws PCDIException {
		return null;
	}
	
	/**
	 * get variable type for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public String getVariableType(IPCDIDebugProcessSet procs, String varName) throws PCDIException {
		return null;
	}
	
	/**
	 * list local variables for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDILocalVariable[] listLocalVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		return null;
	}
	
	/**
	 * list global variables for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDIGlobalVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws PCDIException {
		return null;
	}
	
	/**
	 * list arguments for first process in procs
	 * TODO: extend to support multiple processes
	 */
	public ICDIArgument[] listArguments(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws PCDIException {
		return null;
	}
}
