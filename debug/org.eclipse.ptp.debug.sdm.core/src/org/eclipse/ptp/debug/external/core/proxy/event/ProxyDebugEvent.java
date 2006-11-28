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
import org.eclipse.ptp.core.proxy.event.ProxyEvent;
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

public class ProxyDebugEvent extends ProxyEvent {
	
	/**
	 * [0]: event type
	 * [1]: bit list
	 */
	public static IProxyEvent toEvent(String str) {
		int numVars;
		String[] vars;
		IProxyDebugEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		BitList set = ProxyEvent.decodeBitSet(args[1]);
		
		switch (type) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			evt = new ProxyDebugOKEvent(set);
			break;
			
		/**
		 * [2]: error code
		 * [3]: error message
		 */
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			int errCode = Integer.parseInt(args[2]);
			evt = new ProxyDebugErrorEvent(set, errCode, decodeString(args[3]));
			break;

		/**
		 * [2]: number of servers
		 */			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			int num_servers = Integer.parseInt(args[2]);
			evt = new ProxyDebugInitEvent(set, num_servers);
			break;
			
		/**
		 * [2]: event reason
		 */
		case IProxyDebugEvent.EVENT_DBG_SUSPEND:
			switch (Integer.parseInt(args[2])) {
			/**
			 * [3]: bpt id
			 * [4]: thread id
			 * [5]: var changed list -> length
			 * [6]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_BPHIT:
				int hitId = Integer.parseInt(args[3]);
				int bpTid = Integer.parseInt(args[4]);
				numVars = Integer.parseInt(args[5]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = decodeString(args[i+6]);
				}
				evt = new ProxyDebugBreakpointHitEvent(set, hitId, bpTid, vars);
				break;
				
			/**
			 * [3]: signal info -> name
			 * [4]: signal info -> sig_stop
			 * [5]: signal info -> sig_print
			 * [6]: signal info -> sig_pass
			 * [7]: signal info -> description
			 * [8]: frame -> level
			 * [9]: frame -> location -> file
			 * [10]: frame -> location -> function
			 * [11]: frame -> location -> address
			 * [12]: frame -> location -> line number
			 * [13]: thread id
			 * [14]: var changed list -> length
			 * [15]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_SIGNAL:
				int sigTid = Integer.parseInt(args[13]);
				IPCDILocator sigLoc = null;
				
				if (!(args[8].compareTo("*") == 0)) {
					sigLoc = toLocator(args[9], args[10], args[11], args[12]);
				}
	
				numVars = Integer.parseInt(args[14]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = decodeString(args[i+15]);
				}

				evt = new ProxyDebugSignalEvent(set, decodeString(args[3]), decodeString(args[7]), sigLoc, sigTid, vars);
				break;
				
			/**
			 * [3]: frame -> level
			 * [4]: frame -> location -> file
			 * [5]: frame -> location -> function
			 * [6]: frame -> location -> address
			 * [7]: frame -> location -> line number
			 * [8]: thread id
			 * [9]: var changed list -> length
			 * [10]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_STEP:
				ProxyDebugStackframe frame = toFrame(args[3], args[4], args[5], args[7], args[6]);
				int stTid = Integer.parseInt(args[8]);
				numVars = Integer.parseInt(args[9]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = decodeString(args[i+10]);
				}
				evt = new ProxyDebugStepEvent(set, frame, stTid, vars);
				break;
				
			/**
			 * [3]: frame -> level
			 * [4]: frame -> location -> file
			 * [5]: frame -> location -> function
			 * [6]: frame -> location -> address
			 * [7]: frame -> location -> line number
			 * [8]: thread id
			 * [9]: var changed list -> length
			 * [10]: var changed list -> name
			 */
			case IProxyDebugEvent.EVENT_DBG_SUSPEND_INT:
				IPCDILocator suspendLoc = toLocator(args[4], args[5], args[6], args[7]);
				int susTid = Integer.parseInt(args[8]);
				numVars = Integer.parseInt(args[9]);
				vars = new String[numVars];
				for (int i = 0; i<numVars; i++) {
					vars[i] = decodeString(args[i+10]);
				}
				evt = new ProxyDebugSuspendEvent(set, suspendLoc, susTid, vars);
				break;
			}
			break;
		/**
		 * [2]: bpt id
		 * [3]: breakpoint -> bpt id
		 * [4]: breakpoint -> ignore
		 * [5]: breakpoint -> special
		 * [6]: breakpoint -> deleted
		 * [7]: breakpoint -> type
		 * [8]: breakpoint -> location -> file
		 * [9]: breakpoint -> location -> function
		 * [10]: breakpoint -> location -> address
		 * [11]: breakpoint -> location -> line number
		 * [12]: breakpoint -> hit count
		 */
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			int setId = Integer.parseInt(args[2]);
			IPCDILineLocation loc = toLineLocation(args[8], args[11]);
			IPCDIBreakpoint bpt = toBreakpoint(args[4], args[5], args[6], args[7], loc);
			evt = new ProxyDebugBreakpointSetEvent(set, setId, bpt);
			break;

		/**
		 * [2]: signal info list -> length
		 * [3]: signal info list -> name
		 * [4]: signal info list -> sig_stop
		 * [5]: signal info list -> sig_print
		 * [6]: signal info list -> sig_pass
		 * [7]: signal info list -> description
		 */
		case IProxyDebugEvent.EVENT_DBG_SIGNALS:
			int numSignals = Integer.parseInt(args[2]);
			ProxyDebugSignal[] signals = new ProxyDebugSignal[numSignals];
			for (int i = 0; i<numSignals; i++) {
				signals[i] = new ProxyDebugSignal(decodeString(args[5*i+3]), toboolean(Integer.parseInt(args[5*i+4])), toboolean(Integer.parseInt(args[5*i+5])), toboolean(Integer.parseInt(args[5*i+6])), decodeString(args[5*i+7]));
			}
			evt = new ProxyDebugSignalsEvent(set, signals);
			break;
			
		/**
		 * [2]: event reason
		 */
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			switch (Integer.parseInt(args[2])) {
			/**
			 * [3]: exit status
			 */
			case IProxyDebugEvent.EVENT_DBG_EXIT_NORMAL:
				int status = Integer.parseInt(args[3]);
				evt = new ProxyDebugExitEvent(set, status);
				break;
			/**
			 * [3]: signal info -> name
			 * [4]: signal info -> sig_stop
			 * [5]: signal info -> sig_print
			 * [6]: signal info -> sig_pass
			 * [7]: signal info -> description
			 */
			case IProxyDebugEvent.EVENT_DBG_EXIT_SIGNAL:
				evt = new ProxyDebugSignalExitEvent(set, decodeString(args[3]), decodeString(args[7]));
				break;
			}
			break;
			
		/**
		 * [2]: frame list -> length
		 * [3]: frame list -> level
		 * [4]: frame list -> location -> file
		 * [5]: frame list -> location -> function
		 * [6]: frame list -> location -> address
		 * [7]: frame list -> location -> line number
		 */
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			int numFrames = Integer.parseInt(args[2]);
			ProxyDebugStackframe[] frames = new ProxyDebugStackframe[numFrames];
			for (int i = 0; i < numFrames; i++) {
				frames[i] = toFrame(args[5*i+3], args[5*i+4], args[5*i+5], args[5*i+7], args[5*i+6]);
			}
			evt = new ProxyDebugStackframeEvent(set, frames);
			break;

		/**
		 * [2]: thread id
		 * [3]: frame -> level
		 * [4]: frame -> location -> file
		 * [5]: frame -> location -> function
		 * [6]: frame -> location -> address
		 * [7]: frame -> location -> line number
		 */
		case IProxyDebugEvent.EVENT_DBG_THREAD_SELECT:
			int current_thread_id = Integer.parseInt(args[2]);
			ProxyDebugStackframe th_frame = toFrame(args[3], args[4], args[5], args[7], args[6]);
			evt = new ProxyDebugSetThreadSelectEvent(set, current_thread_id, th_frame);
			break;
		
		/**
		 * [2]: current thread id
		 * [3]: thread ids list -> length
		 * [4]: thread ids list -> thread id
		 */
		case IProxyDebugEvent.EVENT_DBG_THREADS:
			int numThreads = Integer.parseInt(args[3]);
			String[] thread_ids = new String[numThreads + 1];
			thread_ids[0] = args[2];
			for (int i=1; i<thread_ids.length; i++) {
				thread_ids[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugInfoThreadsEvent(set, thread_ids);
			break;

		/**
		 * [2]: stack depth
		 */
		case IProxyDebugEvent.EVENT_DBG_STACK_INFO_DEPTH:
			int depth = Integer.parseInt(args[2]);
			evt = new ProxyDebugStackInfoDepthEvent(set, depth);
			break;

		/**
		 * [2]: address
		 * [3]: next row
		 * [4]: prev row
		 * [5]: next page
		 * [6]: prev page
		 * [7]: number of bytes
		 * [8]: total bytes
		 * [9]: memory list -> length
		 * [10]: memory list -> address
		 * [11]: memory list -> ascii
		 * [12]: memory list -> data list -> length
		 * [13]: memory list -> data list -> value
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA_READ_MEMORY:
			int numMemories = Integer.parseInt(args[9]);
			Memory[] memories = new Memory[numMemories];
			int data_len = 0;
			for (int i=0; i<numMemories; i++) {
				int new_data_len = Integer.parseInt(args[data_len*i+12]);
				String addr = decodeString(args[data_len*i+10]);
				String ascii = decodeString(args[data_len*i+11]);
				String[] data_str = new String[new_data_len];
				for (int j=0; j<new_data_len; j++) {
					data_str[j] = decodeString(args[data_len*i+13+j]);
				}
				data_len = new_data_len;
				memories[i] = new Memory(addr, ascii, data_str);
			}
			evt = new ProxyDebugMemoryInfoEvent(set, toMemoryInfo(args[2], args[3], args[4], args[5], args[6], args[7], args[8], memories));
			break;
			
		/**
		 * [2]: var list -> length
		 * [3]: var list -> name
		 */
		case IProxyDebugEvent.EVENT_DBG_VARS:
			numVars = Integer.parseInt(args[2]);
			vars = new String[numVars];
			for (int i = 0; i < numVars; i++) {
				vars[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugVarsEvent(set, vars);
			break;

		/**
		 * [2]: arg list -> length
		 * [3]: arg list -> name
		 */
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			int numArgs = Integer.parseInt(args[2]);
			String[] arg_strs = new String[numArgs];
			for (int i = 0; i < numArgs; i++) {
				arg_strs[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugArgsEvent(set, arg_strs);
			break;
			
		/**
		 * [2]: type name
		 */
		case IProxyDebugEvent.EVENT_DBG_TYPE:
			evt = new ProxyDebugTypeEvent(set, decodeString(args[2]));
			break;
			
		/**
		 * [2]: aif format
		 * [3]: aif data
		 * [4]: type description
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA:
			IAIF data = new AIF(decodeString(args[2]), decodeBytes(args[3]), decodeString(args[4]));
			evt = new ProxyDebugDataEvent(set, data);
			break;

		/**
		 * [2]: data value 
		 */
		case IProxyDebugEvent.EVENT_DBG_DATA_EVA_EX:
			evt = new ProxyDebugDataExpValueEvent(set, decodeString(args[2]));
			break;

		/**
		 * [2]: aif format
		 * [3]: aif data
		 * [4]: type description
		 */
		case IProxyDebugEvent.EVENT_DBG_PARTIAL_AIF:
			IAIF partial_data = new AIF(decodeString(args[2]), decodeBytes(args[3]), decodeString(args[4]));
			evt = new ProxyDebugPartialAIFEvent(set, partial_data, decodeString(args[5]));
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
	
	public static IPCDIBreakpoint toBreakpoint(String ignoreStr, String spec, String del, String typeStr, IPCDILineLocation loc) {
		IPCDIBreakpoint bpt = null;
		int typeVal;
		
		int ignore = Integer.parseInt(ignoreStr);
		IPCDICondition cond = new Condition(ignore, null, null);
		
		String type = decodeString(typeStr);

		if (type.compareTo("breakpoint") == 0)
			typeVal = IPCDIBreakpoint.REGULAR;
		else if (type.compareTo("hw") == 0)
			typeVal = IPCDIBreakpoint.HARDWARE;
		else
			typeVal = IPCDIBreakpoint.TEMPORARY;
		
		bpt = new LineBreakpoint(typeVal, loc, cond);
	
		return bpt;
	}
	
	public static IPCDILineLocation toLineLocation(String fileStr, String lineStr) {
		String file = decodeString(fileStr);
		int line = Integer.parseInt(lineStr);
		return new LineLocation(file, line);
	}
	
	public static IPCDILocator toLocator(String fileStr, String funcStr, String addrStr, String lineStr) {
		String file = decodeString(fileStr);
		String func = decodeString(funcStr);
		int line = Integer.parseInt(lineStr);
		String addr = decodeString(addrStr);
		
		return new Locator(file, func, line, ExtFormat.getBigInteger(addr));
	}

	public static ProxyDebugStackframe toFrame(String level, String file, String func, String line, String addr)  {
		int stepLevel = Integer.parseInt(level);
		return new ProxyDebugStackframe(stepLevel, toLocator(file, func, addr, line));
	}
	public static DataReadMemoryInfo toMemoryInfo(String addr, String nextRow, String prevRow, String nextPage, String prevPage, String numBytes, String totalBytes, Memory[] memories) {
		return new DataReadMemoryInfo(decodeString(addr), Long.parseLong(nextRow), Long.parseLong(prevRow), Long.parseLong(nextPage), Long.parseLong(prevPage), Long.parseLong(numBytes), Long.parseLong(totalBytes), memories);
	}
	public static boolean toboolean(int value) {
		return (value!=0);
	}
}