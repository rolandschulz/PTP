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

public class ProxyDebugBreakpoint {
	private String ignore; 
	private String spec;
	private String del;
	private String type;
	private ProxyDebugLineLocation loc;
	
	public ProxyDebugBreakpoint(String ignore, String spec, String del, 
			String type, ProxyDebugLineLocation loc) {
		this.ignore = ignore;
		this.spec = spec;
		this.del = del;
		this.type = type;
		this.loc = loc;
	}

	public String getIgnore() {
		return ignore;
	}
	
	public String getSpec() {
		return spec;
	}
	
	public String getDel() {
		return del;
	}
	
	public String getType() {
		return type;
	}
	
	public ProxyDebugLineLocation getLocation(){
		return loc;
	}
	
	public String toString() {
		return getIgnore() + " " + getSpec() + " " + 
			getDel() + " " + getType() + " " + getLocation().toString();	
	}
}
