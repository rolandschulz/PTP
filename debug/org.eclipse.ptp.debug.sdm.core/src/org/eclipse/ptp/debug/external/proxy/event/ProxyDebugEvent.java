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

package org.eclipse.ptp.debug.external.proxy.event;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.ProxyEvent;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.aif.AIF;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.external.ExtFormat;
import org.eclipse.ptp.debug.external.cdi.Condition;
import org.eclipse.ptp.debug.external.cdi.Locator;
import org.eclipse.ptp.debug.external.cdi.breakpoints.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugStackframe;

public class ProxyDebugEvent extends ProxyEvent {
	
	public static IProxyEvent toEvent(String str) {
		IProxyDebugEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		BitList set = ProxyEvent.decodeBitSet(args[1]);
		
		switch (type) {
		case IProxyDebugEvent.EVENT_DBG_OK:
			evt = new ProxyDebugOKEvent(set);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ERROR:
			int errCode = Integer.parseInt(args[2]);
			evt = new ProxyDebugErrorEvent(set, errCode, decodeString(args[3]));
			break;

		case IProxyDebugEvent.EVENT_DBG_BPHIT:
			int hitId = Integer.parseInt(args[2]);
			evt = new ProxyDebugBreakpointHitEvent(set, hitId);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_BPSET:
			int setId = Integer.parseInt(args[2]);
			ICDILineLocation loc = toLineLocation(args[8], args[11]);
			ICDIBreakpoint bpt = toBreakpoint(args[4], args[5], args[6], args[7], loc);
			evt = new ProxyDebugBreakpointSetEvent(set, setId, bpt);
			break;

		case IProxyDebugEvent.EVENT_DBG_SIGNAL:
			int sigTid = Integer.parseInt(args[4]);
			ICDILocator sigLoc = null;
			
			if (!(args[5].compareTo("*") == 0)) {
				sigLoc = toLocator(args[6], args[7], args[8], args[9]);
			}

			evt = new ProxyDebugSignalEvent(set, decodeString(args[2]), decodeString(args[3]), sigTid, sigLoc);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_EXIT:
			int status = Integer.parseInt(args[2]);
			evt = new ProxyDebugExitEvent(set, status);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_STEP:
			ProxyDebugStackframe frame = toFrame(args[2], args[3], args[4], args[6], args[5]);
			evt = new ProxyDebugStepEvent(set, frame);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_FRAMES:
			int numFrames = Integer.parseInt(args[2]);
			ProxyDebugStackframe[] frames = new ProxyDebugStackframe[numFrames];
			for (int i = 0; i < numFrames; i++) {
				int frameLevel = Integer.parseInt(args[5*i+3]);
				int line = Integer.parseInt(args[5*i+7]);
				frames[i] = new ProxyDebugStackframe(frameLevel, decodeString(args[5*i+4]), decodeString(args[5*i+5]), line, decodeString(args[5*i+6]));
			}
			evt = new ProxyDebugStackframeEvent(set, frames);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_DATA:
			IAIF data = new AIF(decodeString(args[2]), decodeBytes(args[3]), decodeString(args[4]));
			evt = new ProxyDebugDataEvent(set, data);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_TYPE:
			evt = new ProxyDebugTypeEvent(set, decodeString(args[2]));
			break;
			
		case IProxyDebugEvent.EVENT_DBG_VARS:
			int numVars = Integer.parseInt(args[2]);
			String[] vars = new String[numVars];
			for (int i = 0; i < numVars; i++) {
				vars[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugVarsEvent(set, vars);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_ARGS:
			int numArgs = Integer.parseInt(args[2]);
			String[] arg_strs = new String[numArgs];
			for (int i = 0; i < numArgs; i++) {
				arg_strs[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugArgsEvent(set, arg_strs);
			break;
			
		case IProxyDebugEvent.EVENT_DBG_INIT:
			int num_servers = Integer.parseInt(args[2]);
			evt = new ProxyDebugInitEvent(set, num_servers);
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
	
	public static ICDIBreakpoint toBreakpoint(String ignoreStr, String spec, String del, String typeStr, ICDILineLocation loc) {
		ICDIBreakpoint bpt = null;
		int typeVal;
		
		int ignore = Integer.parseInt(ignoreStr);
		ICDICondition cond = new Condition(ignore, null, null);
		
		String type = decodeString(typeStr);

		if (type.compareTo("breakpoint") == 0)
			typeVal = ICDIBreakpoint.REGULAR;
		else if (type.compareTo("hw") == 0)
			typeVal = ICDIBreakpoint.HARDWARE;
		else
			typeVal = ICDIBreakpoint.TEMPORARY;
		
		bpt = new LineBreakpoint(typeVal, loc, cond);
	
		return bpt;
	}
	
	public static ICDILineLocation toLineLocation(String fileStr, String lineStr) {
		String file = decodeString(fileStr);
		int line = Integer.parseInt(lineStr);
		return new LineLocation(file, line);
	}
	
	public static ICDILocator toLocator(String fileStr, String funcStr, String addrStr, String lineStr) {
		String file = decodeString(fileStr);
		String func = decodeString(funcStr);
		int line = Integer.parseInt(lineStr);
		String addr = decodeString(addrStr);
		
		return new Locator(file, func, line, ExtFormat.getBigInteger(addr));
	}

	public static ProxyDebugStackframe toFrame(String level, String file, String func, String line, String addr)  {
		int stepLevel = Integer.parseInt(level);
		int stepLine = Integer.parseInt(line);
		return new ProxyDebugStackframe(stepLevel, decodeString(file), decodeString(func), stepLine, decodeString(addr));
	}
}
