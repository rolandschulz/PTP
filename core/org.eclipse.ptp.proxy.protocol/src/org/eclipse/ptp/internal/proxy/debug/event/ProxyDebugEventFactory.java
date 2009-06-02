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

package org.eclipse.ptp.internal.proxy.debug.event;

import java.math.BigInteger;

import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugBreakpoint;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLineLocation;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemory;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemoryInfo;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugSignal;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEventFactory;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.ProxyEventFactory;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public class ProxyDebugEventFactory extends ProxyEventFactory implements IProxyDebugEventFactory {
	
	public IProxyEvent toEvent(ProxyPacket packet) {
		IProxyDebugEvent	evt = null;
		int numVars;
		String vars[];

		IProxyEvent e = super.toEvent(packet);
		if (e != null) {
			return e;
		}

		String[] args = packet.getArgs();
		
		/*
		 * [0]: bit list
		 */
		String bits = args[0];
		
		switch (packet.getID()) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			evt = new ProxyDebugOKEvent(packet.getTransID(), bits);
			break;
			
		/**
		 * [1]: error code
		 * [2]: error message
		 */
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			int errCode = Integer.parseInt(args[1]);
			evt = new ProxyDebugErrorEvent(packet.getTransID(), bits, errCode, args[2]);
			break;
		
		/**
		 * [1]: event reason
		 */
		case IProxyDebugEvent.EVENT_DBG_SUSPEND:
			switch (Integer.parseInt(args[1])) {
			/**
			 * [2]: bpt id
			 * [3]: thread id
			 * [4]: depth
			 * [5]: var changed list -> length
			 * [6]: var changed list -> name
			 * 
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_BPHIT:
				int hitId = Integer.parseInt(args[2]);
				int bpTid = Integer.parseInt(args[3]);
				int bpDep = Integer.parseInt(args[4]);
				numVars = Integer.parseInt(args[5]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+6];
				}
				evt = new ProxyDebugBreakpointHitEvent(packet.getTransID(), bits, hitId, bpTid, bpDep, vars);
				break;
				
				
			/**
			 * [2]: signal info -> name
			 * [3]: signal info -> sig_stop
			 * [4]: signal info -> sig_print
			 * [5]: signal info -> sig_pass
			 * [6]: signal info -> description
			 * [7]: frame -> level
			 * [8]: frame -> location -> file
			 * [9]: frame -> location -> function
			 * [10]: frame -> location -> address
			 * [11]: frame -> location -> line number
			 * [12]: thread id
			 * [13]: depth
			 * [14]: var changed list -> length
			 * [15]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_SIGNAL:
				int sigTid = Integer.parseInt(args[12]);
				int sigDep = Integer.parseInt(args[13]);
				ProxyDebugStackFrame sigFrame = null;
				
				if (!(args[7].compareTo("*") == 0)) { //$NON-NLS-1$
					sigFrame = toFrame(args[7], args[8], args[9], args[11], args[10]);
				}
	
				numVars = Integer.parseInt(args[14]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+15];
				}

				evt = new ProxyDebugSignalEvent(packet.getTransID(), bits, args[2], args[6], sigFrame, sigTid, sigDep, vars);
				break;
				
			/**
			 * [2]: frame -> level
			 * [3]: frame -> location -> file
			 * [4]: frame -> location -> function
			 * [5]: frame -> location -> address
			 * [6]: frame -> location -> line number
			 * [7]: thread id
			 * [8]: depth
			 * [9]: var changed list -> length
			 * [10]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_STEP:
				ProxyDebugStackFrame frame = toFrame(args[2], args[3], args[4], args[6], args[5]);
				int stTid = Integer.parseInt(args[7]);
				int stDep = Integer.parseInt(args[8]);
				numVars = Integer.parseInt(args[9]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+10];
				}
				evt = new ProxyDebugStepEvent(packet.getTransID(), bits, frame, stTid, stDep, vars);
				break;
				
			/**
			 * [2]: frame -> level
			 * [3]: frame -> location -> file
			 * [4]: frame -> location -> function
			 * [5]: frame -> location -> address
			 * [6]: frame -> location -> line number
			 * [7]: thread id
			 * [8]: depth
			 * [9]: var changed list -> length
			 * [10]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_INT:
				ProxyDebugStackFrame suspendFrame = toFrame(args[2], args[3], args[4], args[6], args[5]);
				int susTid = Integer.parseInt(args[7]);
				int susDep = Integer.parseInt(args[8]);
				numVars = Integer.parseInt(args[9]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+10];
				}
				evt = new ProxyDebugSuspendEvent(packet.getTransID(), bits, suspendFrame, susTid, susDep, vars);
				break;
			}
			break;
			
		/**
		 * [1]: bpt id
		 * [2]: breakpoint -> bpt id
		 * [3]: breakpoint -> ignore
		 * [4]: breakpoint -> special
		 * [5]: breakpoint -> deleted
		 * [6]: breakpoint -> type
		 * [7]: breakpoint -> location -> file
		 * [8]: breakpoint -> location -> function
		 * [9]: breakpoint -> location -> address
		 * [10]: breakpoint -> location -> line number
		 * [11]: breakpoint -> hit count
		 */
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			int bpId = Integer.parseInt(args[1]);
			ProxyDebugLineLocation loc = toLineLocation(args[7], args[10]);
			ProxyDebugBreakpoint bpt = toBreakpoint(args[3], args[4], args[5], args[6], loc);
			evt = new ProxyDebugBreakpointSetEvent(packet.getTransID(), bits, bpId, bpt);
			break;

		/**
		 * [1]: signal info list -> length
		 * [2]: signal info list -> name
		 * [3]: signal info list -> sig_stop
		 * [4]: signal info list -> sig_print
		 * [5]: signal info list -> sig_pass
		 * [6]: signal info list -> description
		 */
		case IProxyDebugEvent.EVENT_DBG_SIGNALS:
			int numSignals = Integer.parseInt(args[1]);
			ProxyDebugSignal[] signals = new ProxyDebugSignal[numSignals];
			for (int i = 0; i<numSignals; i++) {
				signals[i] = new ProxyDebugSignal(args[5*i+2], toBoolean(Integer.parseInt(args[5*i+3])), toBoolean(Integer.parseInt(args[5*i+4])), toBoolean(Integer.parseInt(args[5*i+5])), args[5*i+6]);
			}
			evt = new ProxyDebugSignalsEvent(packet.getTransID(), bits, signals);
			break;
			
		/**
		 * [1]: event reason
		 */
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			switch (Integer.parseInt(args[1])) {
			/**
			 * [2]: exit status
			 */
			case IProxyDebugEvent.EVENT_DBG_EXIT_NORMAL:
				int status = Integer.parseInt(args[2]);
				evt = new ProxyDebugExitEvent(packet.getTransID(), bits, status);
				break;
			/**
			 * [2]: signal info -> name
			 * [3]: signal info -> sig_stop
			 * [4]: signal info -> sig_print
			 * [5]: signal info -> sig_pass
			 * [6]: signal info -> description
			 */
			case IProxyDebugEvent.EVENT_DBG_EXIT_SIGNAL:
				evt = new ProxyDebugSignalExitEvent(packet.getTransID(), bits, args[2], args[6]);
				break;
			}
			break;
			
		/**
		 * [1]: frame list -> length
		 * [2]: frame list -> level
		 * [3]: frame list -> location -> file
		 * [4]: frame list -> location -> function
		 * [5]: frame list -> location -> address
		 * [6]: frame list -> location -> line number
		 */
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			int numFrames = Integer.parseInt(args[1]);
			ProxyDebugStackFrame[] frames = new ProxyDebugStackFrame[numFrames];
			for (int i = 0; i < numFrames; i++) {
				frames[i] = toFrame(args[5*i+2], args[5*i+3], args[5*i+4], args[5*i+6], args[5*i+5]);
			}
			evt = new ProxyDebugStackframeEvent(packet.getTransID(), bits, frames);
			break;

		/**
		 * [1]: thread id
		 * [2]: frame -> level
		 * [3]: frame -> location -> file
		 * [4]: frame -> location -> function
		 * [5]: frame -> location -> address
		 * [6]: frame -> location -> line number
		 */
		case IProxyDebugEvent.EVENT_DBG_THREAD_SELECT:
			int current_thread_id = Integer.parseInt(args[1]);
			ProxyDebugStackFrame th_frame = toFrame(args[2], args[3], args[4], args[6], args[5]);
			evt = new ProxyDebugSetThreadSelectEvent(packet.getTransID(), bits, current_thread_id, th_frame);
			break;
		
		/**
		 * [1]: current thread id
		 * [2]: thread ids list -> length
		 * [3]: thread ids list -> thread id
		 */
		case IProxyDebugEvent.EVENT_DBG_THREADS:
			int numThreads = Integer.parseInt(args[2]);
			String[] thread_ids = new String[numThreads + 1];
			thread_ids[0] = args[1];
			for (int i=1; i<thread_ids.length; i++) {
				thread_ids[i] = args[i+2];
			}
			evt = new ProxyDebugInfoThreadsEvent(packet.getTransID(), bits, thread_ids);
			break;

		/**
		 * [1]: stack depth
		 */
		case IProxyDebugEvent.EVENT_DBG_STACK_INFO_DEPTH:
			int depth = Integer.parseInt(args[1]);
			evt = new ProxyDebugStackInfoDepthEvent(packet.getTransID(), bits, depth);
			break;

		/**
		 * [1]: address
		 * [2]: next row
		 * [3]: prev row
		 * [4]: next page
		 * [5]: prev page
		 * [6]: number of bytes
		 * [7]: total bytes
		 * [8]: memory list -> length
		 * [9]: memory list -> address
		 * [10]: memory list -> ascii
		 * [11]: memory list -> data list -> length
		 * [12]: memory list -> data list -> value
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA_READ_MEMORY:
			int numMemories = Integer.parseInt(args[8]);
			ProxyDebugMemory[] memories = new ProxyDebugMemory[numMemories];
			int data_len = 0;
			for (int i=0; i<numMemories; i++) {
				int new_data_len = Integer.parseInt(args[data_len*i+11]);
				String addr = args[data_len*i+9];
				String ascii = args[data_len*i+10];
				String[] data_str = new String[new_data_len];
				for (int j=0; j<new_data_len; j++) {
					data_str[j] = args[data_len*i+12+j];
				}
				data_len = new_data_len;
				memories[i] = new ProxyDebugMemory(addr, ascii, data_str);
			}
			evt = new ProxyDebugMemoryInfoEvent(packet.getTransID(), bits, toMemoryInfo(args[1], args[2], args[3], args[4], args[5], args[6], args[7], memories));
			break;
			
		/**
		 * [1]: var list -> length
		 * [2]: var list -> name
		 */
		case IProxyDebugEvent.EVENT_DBG_VARS:
			numVars = Integer.parseInt(args[1]);
			vars = new String[numVars];
			for (int i = 0; i < numVars; i++) {
				vars[i] = args[i+2];
			}
			evt = new ProxyDebugVarsEvent(packet.getTransID(), bits, vars);
			break;

		/**
		 * [1]: arg list -> length
		 * [2]: arg list -> name
		 */
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			int numArgs = Integer.parseInt(args[1]);
			String[] arg_strs = new String[numArgs];
			for (int i = 0; i < numArgs; i++) {
				arg_strs[i] = args[i+2];
			}
			evt = new ProxyDebugArgsEvent(packet.getTransID(), bits, arg_strs);
			break;
			
		/**
		 * [1]: type name
		 */
		case IProxyDebugEvent.EVENT_DBG_TYPE:
			evt = new ProxyDebugTypeEvent(packet.getTransID(), bits, args[1]);
			break;
			
		/**
		 * [1]: aif format
		 * [2]: aif data
		 * [3]: type description
		 * [4]: name
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA:
			ProxyDebugAIF data = new ProxyDebugAIF(args[1], args[2], args[3]);
			evt = new ProxyDebugDataEvent(packet.getTransID(), bits, data, args[4]);
			break;
		}
		return evt;
	}
	
	public static BigInteger decodeAddr(String str) {
		String[] parts = str.split(":"); //$NON-NLS-1$
		int len = Integer.parseInt(parts[0], 16) - 1; // Skip trailing NULL
		byte[] strBytes = new byte[len];
		
		for (int i = 0, p = 0; i < len; i++, p += 2) {
			byte c = (byte) ((Character.digit(parts[1].charAt(p), 16) & 0xf) << 4);
			c |= (byte) ((Character.digit(parts[1].charAt(p+1), 16) & 0xf));
			strBytes[i] = c;
		}
		
		BigInteger a = new BigInteger(strBytes);
		return a;
	}
	
	public static ProxyDebugBreakpoint toBreakpoint(String ignore, String spec, String del, String type, ProxyDebugLineLocation loc) {
		return new ProxyDebugBreakpoint(ignore, spec, del, type, loc);
	}
	
	public static ProxyDebugLineLocation toLineLocation(String file, String line) {
		return new ProxyDebugLineLocation(file, line);
	}
	
	public static ProxyDebugLocator toLocator(String file, String func, String addr, String line) {
		return new ProxyDebugLocator(file, func, line, addr);
	}

	public static ProxyDebugStackFrame toFrame(String level, String file, String func, String line, String addr)  {
		int stepLevel = Integer.parseInt(level);
		return new ProxyDebugStackFrame(stepLevel, toLocator(file, func, addr, line));
	}
	
	public static ProxyDebugMemoryInfo toMemoryInfo(String addr, String nextRow, String prevRow, String nextPage, String prevPage, 
			String numBytes, String totalBytes, ProxyDebugMemory[] memories) {
		return new ProxyDebugMemoryInfo(addr, nextRow, prevRow, nextPage, prevPage, numBytes, totalBytes, memories);
	}
	
	public static boolean toBoolean(int value) {
		return (value!=0);
	}
}