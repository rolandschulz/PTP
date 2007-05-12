/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public abstract class AbstractProxyEventFactory implements IProxyEventFactory {
	public abstract IProxyEvent toEvent(int type, int transID, String[] args);
	
	public static String join(int start, String[] strs, String delim) {
		StringBuffer buf = new StringBuffer();
		for (int i = start; i < strs.length; i++) {
			if (i > start)
				buf.append(delim);
			buf.append(strs[i]);
		}
		return buf.toString();
	}

	public static byte[] decodeBytes(String str) {
		int len = str.length()/2;
		byte[] strBytes = new byte[len];
		
		for (int i = 0, p = 0; i < len; i++, p += 2) {
			byte c = (byte) ((Character.digit(str.charAt(p), 16) & 0xf) << 4);
			c |= (byte) ((Character.digit(str.charAt(p+1), 16) & 0xf));
			strBytes[i] = c;
		}
		
		return strBytes;
	}
	
	public static BitList decodeBitSet(String str) {
		String[] parts = str.split(":");
		int len = Integer.parseInt(parts[0], 16); // Skip trailing NULL
		return new BitList(len, parts[1]);
	}

}
