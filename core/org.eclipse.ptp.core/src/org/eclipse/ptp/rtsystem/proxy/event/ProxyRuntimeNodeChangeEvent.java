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

package org.eclipse.ptp.rtsystem.proxy.event;

public class ProxyRuntimeNodeChangeEvent extends AbstractProxyRuntimeEvent implements IProxyRuntimeEvent {
	private int machID;
	private String key;
	private String val;
	private String nodeName;

	public ProxyRuntimeNodeChangeEvent(String[] args) {
		super(EVENT_RUNTIME_NODECHANGE);
		this.machID = Integer.parseInt(args[1]);
		this.nodeName = args[2];
		
		String text = new String("");
		for(int i=3; i<args.length; i++) {
			if(i == 3) text = args[i];
			else text = text + " " +args[i];
		}
		
		int idx = text.indexOf('=');
		key = text.substring(0, idx);
		val = text.substring(idx+1, text.length());
		
		System.out.println("key = '"+key+"', val = '"+val+"'");
	}
	
	public int getMachineID() { return this.machID; }
	public String getNodeName() { return this.nodeName; }
	public String getKey() { return this.key; }
	public String getValue() { return this.val; }
	
	public String toString() {
		return "EVENT_RUNTIME_NODECHANGE (machID="+this.machID+", nodeID="+this.nodeName+") key='"+this.key+"' val='"+this.val+"'";
	}
}
