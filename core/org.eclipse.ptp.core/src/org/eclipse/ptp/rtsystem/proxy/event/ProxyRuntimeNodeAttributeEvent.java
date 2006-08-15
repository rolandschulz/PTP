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

import org.eclipse.ptp.core.proxy.event.ProxyEvent;

public class ProxyRuntimeNodeAttributeEvent extends AbstractProxyRuntimeEvent implements IProxyRuntimeEvent {
	private String[] keys;
	private String[] values;

	public ProxyRuntimeNodeAttributeEvent(String[] values) {
		super(EVENT_RUNTIME_NODEATTR);
		int s = values.length - 1;
		this.keys = new String[s];
		this.values = new String[s];
		for(int i=0; i<s; i++) {
			String tmp = new String(ProxyEvent.decodeString(values[i+1]));
			System.out.println("key=val node attribute pair : '"+tmp+"'");
			String[] tmp2 = tmp.split("=");
			/*
			for(int j=0; j<tmp2.length; j++) {
				System.out.println("tmp2 = "+tmp2[j]);
			}
			*/
			this.keys[i] = tmp2[0];
			this.values[i] = tmp2[1];
		}
	}
	
	public String[] getValues() {
		return this.values;
	}
	
	public String[] getKeys() {
		return this.keys;
	}
	
	public String toString() {
		return "EVENT_RUNTIME_NODEATTR " + this.keys+" : "+ this.values;
	}
}

