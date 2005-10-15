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

package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public class ProxyEvent {
	private static String join(int start, String[] strs, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i = start; i < strs.length; i++) {
			if (i > start)
				buf.append(delim);
			buf.append(strs[i]);
		}
		return buf.toString();
	}

	public static IProxyEvent toEvent(String str) {
		IProxyEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		switch (type) {
		case IProxyEvent.EVENT_OK:
			evt = new ProxyOKEvent(join(1, args, " "));
			break;
			
		case IProxyEvent.EVENT_ERROR:
			int errCode = Integer.parseInt(args[1]);
			evt = new ProxyErrorEvent(errCode, decodeString(args[2]));
			break;
			
		default:
			evt = new ProxyErrorEvent(ProxyErrorEvent.EVENT_ERR_EVENT, "Invalid event type");
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
	
	public static BitList decodeBitSet(String str) {
		String[] parts = str.split(":");
		int len = Integer.parseInt(parts[0], 16); // Skip trailing NULL
		return new BitList(len, parts[1]);
	}
}