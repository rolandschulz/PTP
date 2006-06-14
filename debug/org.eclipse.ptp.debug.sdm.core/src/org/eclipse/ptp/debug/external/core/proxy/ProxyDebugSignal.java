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
package org.eclipse.ptp.debug.external.core.proxy;

/**
 * @author Clement chu
 */
public class ProxyDebugSignal {
	private String name;
	private boolean stop;
	private boolean pass;
	private boolean print;
	private String desc;
	
	public ProxyDebugSignal(String name, boolean stop, boolean print, boolean pass, String desc) {
		this.name = name;
		this.stop = stop;
		this.print = print;
		this.pass = pass;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}
	public boolean isStop() {
		return stop;
	}
	public boolean isPrint() {
		return print;
	}
	public boolean isPass() {
		return pass;
	}
	public String getDescription(){
		return desc;
	}
	public String toString() {
		return getName() + " " + isStop() + " " + isPrint() + " " + isPass() + " " + getDescription();	
	}
}
