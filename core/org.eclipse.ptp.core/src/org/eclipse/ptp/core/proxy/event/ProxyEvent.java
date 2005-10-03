package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.FastBitSet;

public class ProxyEvent {
	
	public static IProxyEvent toEvent(String str) {
		IProxyEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		FastBitSet set = decodeBitSet(args[1]);
		
		switch (type) {
		case IProxyEvent.EVENT_OK:
			evt = new ProxyOKEvent(set);
			break;
			
		case IProxyEvent.EVENT_ERROR:
			int errCode = Integer.parseInt(args[2]);
			evt = new ProxyErrorEvent(set, errCode, decodeString(args[3]));
			break;
			
		default:
			evt = new ProxyErrorEvent(set, ProxyErrorEvent.EVENT_ERR_EVENT, "Invalid event type");
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
	
	public static FastBitSet decodeBitSet(String str) {
		String[] parts = str.split(":");
		int len = Integer.parseInt(parts[0], 16); // Skip trailing NULL
		return new FastBitSet(len, parts[1]);
	}
}