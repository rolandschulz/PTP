package org.eclipse.ptp.debug.external.proxy;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.debug.external.cdi.Condition;
import org.eclipse.ptp.debug.external.cdi.Location;
import org.eclipse.ptp.debug.external.cdi.breakpoints.AddressBreakpoint;
import org.eclipse.ptp.debug.external.cdi.breakpoints.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;

public class ProxyDebugEvent {
	
	public static IProxyEvent toEvent(String str) {
		IProxyEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		FastBitSet set = new FastBitSet(args[1]);
		
		switch (type) {
		case IProxyEvent.EVENT_DBG_BPHIT:
		case IProxyEvent.EVENT_DBG_BPSET:
			Location loc = toLocation(decodeString(args[7]), decodeString(args[8]), decodeString(args[9]), args[10]);
			ICDIBreakpoint bpt = toBreakpoint(args[2], args[3], args[4], args[5], decodeString(args[6]), loc);
			evt = new ProxyDebugBreakpointEvent(set, type, bpt);
			break;
			
		case IProxyEvent.EVENT_DBG_SIGNAL:
			int sigTid = Integer.parseInt(args[4]);
			evt = new ProxyDebugSignalEvent(set, decodeString(args[2]), decodeString(args[3]), sigTid);
			break;
			
		case IProxyEvent.EVENT_DBG_EXIT:
			int status = Integer.parseInt(args[2]);
			evt = new ProxyDebugExitEvent(set, status);
			break;
			
		case IProxyEvent.EVENT_DBG_STEP:
			int stepTid = Integer.parseInt(args[2]);
			evt = new ProxyDebugStepEvent(set, stepTid);
			break;
			
		case IProxyEvent.EVENT_DBG_FRAMES:
			int numFrames = Integer.parseInt(args[2]);
			StackFrame[] frames = new StackFrame[numFrames];
			for (int i = 0; i < numFrames; i++) {
				int frameLevel = Integer.parseInt(args[5*i+3]);
				int line = Integer.parseInt(args[5*i+7]);
				//TODO StackFrame requires a thread
				frames[i] = null;//new StackFrame(null, frameLevel, decodeString(args[5*i+4]), decodeString(args[5*i+5]), line, args[5*i+6]);
			}
			evt = new ProxyDebugStackframeEvent(set, frames);
			break;
			
		case IProxyEvent.EVENT_DBG_DATA:
			evt = new ProxyDebugDataEvent(set, args[2], args[3]);
			break;
			
		case IProxyEvent.EVENT_DBG_TYPE:
			evt = new ProxyDebugTypeEvent(set, args[2]);
			break;
			
		case IProxyEvent.EVENT_DBG_VARS:
			int numVars = Integer.parseInt(args[2]);
			String[] vars = new String[numVars];
			for (int i = 0; i < numVars; i++) {
				vars[i] = decodeString(args[i+3]);
			}
			evt = new ProxyDebugVarsEvent(set, vars);
			break;
			
		case IProxyEvent.EVENT_DBG_INIT:
			int num_servers = Integer.parseInt(args[2]);
			evt = new ProxyDebugInitEvent(set, num_servers);
			break;
		}
		
		return evt;
	}
	
	public static String decodeString(String str) {
		String[] parts = str.split(":");
		int len = Integer.parseInt(parts[0], 16) - 1; // Skip trailing NULL
		byte[] strBytes = new byte[len];
		
		for (int i = 0, p = 0; i < len; i++, p += 2) {
			byte c = (byte) ((Character.digit(parts[1].charAt(p), 16) & 0xf) << 4);
			c |= (byte) ((Character.digit(parts[1].charAt(p+1), 16) & 0xf));
			strBytes[i] = c;
		}
		
		return new String(strBytes);
	}
	
	public static ICDIBreakpoint toBreakpoint(String id, String ignore, String spec, String del, String type, Location loc) {
		ICDIBreakpoint bpt;
		int typeVal;
		
		int idVal = Integer.parseInt(id);
		int ignoreVal = Integer.parseInt(ignore);
		ICDICondition cond = new Condition(ignoreVal, null, null);
		
		if (type.compareTo("breakpoint") == 0)
			typeVal = ICDIBreakpoint.REGULAR;
		else if (type.compareTo("hw") == 0)
			typeVal = ICDIBreakpoint.HARDWARE;
		else
			typeVal = ICDIBreakpoint.TEMPORARY;
		
		if (loc.getAddress() != null)
			bpt = new AddressBreakpoint(typeVal, loc, cond);
		else if (loc.getFunction() != null)
			bpt = new FunctionBreakpoint(typeVal, loc, cond);
		else
			bpt = new FunctionBreakpoint(typeVal, loc, cond);
	
		return bpt;
	}
	
	public static Location toLocation(String file, String func, String line, String addr) {
		Location loc;
	
		if (addr.length() > 0) {
			BigInteger addrVal = new BigInteger(addr);
			loc = new Location(addrVal);
		} else if (func.length() > 0) {
			loc = new Location(file, func);
		} else {
			int lineVal = Integer.parseInt(line);
			loc = new Location(file, lineVal);
		}
		
		return loc;
	}
}
