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

package org.eclipse.ptp.proxy.debug.client;

import java.math.BigInteger;

import org.eclipse.ptp.proxy.util.ProtocolUtil;

public class ProxyDebugLocator {
	private String		file;
	private String		function;
	private int			line;
	private BigInteger	address;
	
	public ProxyDebugLocator(String file, String function, String line, String address) {
		this.file = file;
		this.function = function;
		this.line = Integer.parseInt(line);
		this.address = ProtocolUtil.decodeAddress(address);
	}
	
	public String getFile() {
		return file;
	}
	
	public String getFunction() {
		return function;
	}

	public int getLineNumber() {
		return line;
	}

	public BigInteger getAddress() {
		return address;
	}
	
	public String toString() {
		return file + ":" + function + ":" + line + ":" + address.toString();	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
