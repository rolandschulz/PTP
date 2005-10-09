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

package org.eclipse.ptp.debug.external.proxy;

public class ProxyDebugStackframe {
	private int		level;
	private String	file;
	private String	func;
	private int		line;
	private String	addr;
	
	public ProxyDebugStackframe(int level, String file, String func, int line, String addr) {
		this.level = level;
		this.file = file;
		this.func = func;
		this.line = line;
		this.addr = addr;
	}
	
	public int getLevel() {
		return this.level;
	}

	public String getFile() {
		return this.file;
	}
	
	public String getFunc() {
		return this.func;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public String getAddr() {
		return this.addr;
	}
	
	public String toString() {
		String res = getLevel() + " file=\"" + getFile() + "\"";
		
		if (getFunc().compareTo("") != 0)
			res += " func=" + getFunc();
		if (getLine() != 0)
			res += " line=" + getLine();
		if (getAddr().compareTo("") != 0)
			res += " addr=" + getAddr();	
		
		return res;
	}
}
