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

package org.eclipse.ptp.debug.external.core.proxy.event;

import java.math.BigInteger;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.ProxyEventFactory;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.external.core.cdi.Condition;
import org.eclipse.ptp.debug.external.core.cdi.Locator;
import org.eclipse.ptp.debug.external.core.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.core.cdi.model.DataReadMemoryInfo;
import org.eclipse.ptp.debug.external.core.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.core.cdi.model.Memory;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugSignal;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;

public class ProxyDebugEventFactory extends ProxyEventFactory {
	
	public IProxyEvent toEvent(int type, int transID, String[] args) {
		IProxyDebugEvent	evt = null;
		int numVars;
		String vars[];

		IProxyEvent e = super.toEvent(type, transID, args);
		if (e != null) {
			return e;
		}

		/*
		 * [0]: bit list
		 */
		BitList set = decodeBitSet(args[0]);
		
		switch (type) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			evt = new ProxyDebugOKEvent(transID, set);
			break;
			
		/**
		 * [1]: error code
		 * [2]: error message
		 */
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			int errCode = Integer.parseInt(args[1]);
			evt = new ProxyDebugErrorEvent(transID, set, errCode, args[2]);
			break;

		/**
		 * [1]: number of servers
		 */			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			int num_servers = Integer.parseInt(args[1]);
			evt = new ProxyDebugInitEvent(transID, set, num_servers);
			break;
			
		/**
		 * [1]: event reason
		 */
		case IProxyDebugEvent.EVENT_DBG_SUSPEND:
			switch (Integer.parseInt(args[1])) {
			/**
			 * [2]: bpt id
			 * [3]: thread id
			 * [4]: var changed list -> length
			 * [5]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_BPHIT:
				int hitId = Integer.parseInt(args[2]);
				int bpTid = Integer.parseInt(args[3]);
				numVars = Integer.parseInt(args[4]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+5];
				}
				evt = new ProxyDebugBreakpointHitEvent(transID, set, hitId, bpTid, vars);
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
			 * [13]: var changed list -> length
			 * [14]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_SIGNAL:
				int sigTid = Integer.parseInt(args[12]);
				IPCDILocator sigLoc = null;
				
				if (!(args[7].compareTo("*") == 0)) {
					sigLoc = toLocator(args[8], args[9], args[10], args[11]);
				}
	
				numVars = Integer.parseInt(args[13]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+14];
				}

				evt = new ProxyDebugSignalEvent(transID, set, args[2], args[6], sigLoc, sigTid, vars);
				break;
				
			/**
			 * [2]: frame -> level
			 * [3]: frame -> location -> file
			 * [4]: frame -> location -> function
			 * [5]: frame -> location -> address
			 * [6]: frame -> location -> line number
			 * [7]: thread id
			 * [8]: var changed list -> length
			 * [9]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_STEP:
				ProxyDebugStackframe frame = toFrame(args[2], args[3], args[4], args[6], args[5]);
				int stTid = Integer.parseInt(args[7]);
				numVars = Integer.parseInt(args[8]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+9];
				}
				evt = new ProxyDebugStepEvent(transID, set, frame, stTid, vars);
				break;
				
			/**
			 * [2]: frame -> level
			 * [3]: frame -> location -> file
			 * [4]: frame -> location -> function
			 * [5]: frame -> location -> address
			 * [6]: frame -> location -> line number
			 * [7]: thread id
			 * [8]: var changed list -> length
			 * [9]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_INT:
				IPCDILocator suspendLoc = toLocator(args[3], args[4], args[5], args[6]);
				int susTid = Integer.parseInt(args[7]);
				numVars = Integer.parseInt(args[8]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = args[i+9];
				}
				evt = new ProxyDebugSuspendEvent(transID, set, suspendLoc, susTid, vars);
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
			int setId = Integer.parseInt(args[1]);
			IPCDILineLocation loc = toLineLocation(args[7], args[10]);
			IPCDIBreakpoint bpt = toBreakpoint(args[3], args[4], args[5], args[6], loc);
			evt = new ProxyDebugBreakpointSetEvent(transID, set, setId, bpt);
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
			evt = new ProxyDebugSignalsEvent(transID, set, signals);
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
				evt = new ProxyDebugExitEvent(transID, set, status);
				break;
			/**
			 * [2]: signal info -> name
			 * [3]: signal info -> sig_stop
			 * [4]: signal info -> sig_print
			 * [5]: signal info -> sig_pass
			 * [6]: signal info -> description
			 */
			case IProxyDebugEvent.EVENT_DBG_EXIT_SIGNAL:
				evt = new ProxyDebugSignalExitEvent(transID, set, args[2], args[6]);
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
			ProxyDebugStackframe[] frames = new ProxyDebugStackframe[numFrames];
			for (int i = 0; i < numFrames; i++) {
				frames[i] = toFrame(args[5*i+2], args[5*i+3], args[5*i+4], args[5*i+6], args[5*i+5]);
			}
			evt = new ProxyDebugStackframeEvent(transID, set, frames);
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
			ProxyDebugStackframe th_frame = toFrame(args[2], args[3], args[4], args[6], args[7]);
			evt = new ProxyDebugSetThreadSelectEvent(transID, set, current_thread_id, th_frame);
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
			evt = new ProxyDebugInfoThreadsEvent(transID, set, thread_ids);
			break;

		/**
		 * [1]: stack depth
		 */
		case IProxyDebugEvent.EVENT_DBG_STACK_INFO_DEPTH:
			int depth = Integer.parseInt(args[1]);
			evt = new ProxyDebugStackInfoDepthEvent(transID, set, depth);
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
			Memory[] memories = new Memory[numMemories];
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
				memories[i] = new Memory(addr, ascii, data_str);
			}
			evt = new ProxyDebugMemoryInfoEvent(transID, set, toMemoryInfo(args[1], args[2], args[3], args[4], args[5], args[6], args[7], memories));
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
			evt = new ProxyDebugVarsEvent(transID, set, vars);
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
			evt = new ProxyDebugArgsEvent(transID, set, arg_strs);
			break;
			
		/**
		 * [1]: type name
		 */
		case IProxyDebugEvent.EVENT_DBG_TYPE:
			evt = new ProxyDebugTypeEvent(transID, set, args[1]);
			break;
			
		/**
		 * [1]: aif format
		 * [2]: aif data
		 * [3]: type description
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA:
			IAIF data = new AIF(args[1], decodeBytes(args[2]), args[3]);
			evt = new ProxyDebugDataEvent(transID, set, data);
			break;

		/**
		 * [1]: data value 
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA_EVA_EX:
			evt = new ProxyDebugDataExpValueEvent(transID, set, args[1]);
			break;

		/**
		 * [1]: aif format
		 * [2]: aif data
		 * [3]: type description
		 */
		case IProxyDebugEvent.EVENT_DBG_PARTIAL_AIF:
			IAIF partial_data = new AIF(args[1], decodeBytes(args[2]), args[3]);
			evt = new ProxyDebugPartialAIFEvent(transID, set, partial_data, args[4]);
			break;
		}
		return evt;
	}
	
	public static BigInteger decodeAddr(String str) {
		String[] parts = str.split(":");
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
	
	public static IPCDIBreakpoint toBreakpoint(String ignoreStr, String spec, String del, String type, IPCDILineLocation loc) {
		IPCDIBreakpoint bpt = null;
		int typeVal;
		
		int ignore = Integer.parseInt(ignoreStr);
		IPCDICondition cond = new Condition(ignore, null, null);
		
		if (type.compareTo("breakpoint") == 0)
			typeVal = IPCDIBreakpoint.REGULAR;
		else if (type.compareTo("hw") == 0)
			typeVal = IPCDIBreakpoint.HARDWARE;
		else
			typeVal = IPCDIBreakpoint.TEMPORARY;
		
		bpt = new LineBreakpoint(typeVal, loc, cond);
	
		return bpt;
	}
	
	public static IPCDILineLocation toLineLocation(String file, String lineStr) {
		int line = Integer.parseInt(lineStr);
		return new LineLocation(file, line);
	}
	
	public static IPCDILocator toLocator(String file, String func, String addr, String lineStr) {
		int line = Integer.parseInt(lineStr);
		
		return new Locator(file, func, line, ExtFormat.getBigInteger(addr));
	}

	public static ProxyDebugStackframe toFrame(String level, String file, String func, String line, String addr)  {
		int stepLevel = Integer.parseInt(level);
		return new ProxyDebugStackframe(stepLevel, toLocator(file, func, addr, line));
	}
	
	public static DataReadMemoryInfo toMemoryInfo(String addr, String nextRow, String prevRow, String nextPage, String prevPage, String numBytes, String totalBytes, Memory[] memories) {
		return new DataReadMemoryInfo(addr, Long.parseLong(nextRow), Long.parseLong(prevRow), Long.parseLong(nextPage), Long.parseLong(prevPage), Long.parseLong(numBytes), Long.parseLong(totalBytes), memories);
	}
	
	public static boolean toBoolean(int value) {
		return (value!=0);
	}
}