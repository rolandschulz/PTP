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

import java.util.ArrayList;

import org.eclipse.ptp.core.proxy.event.ProxyEvent;

public class ProxyRuntimeNodeAttributeEvent extends AbstractProxyRuntimeEvent implements IProxyRuntimeEvent {
	private String[] keys;
	private String[] values;

	public ProxyRuntimeNodeAttributeEvent(String[] values) {
		super(EVENT_RUNTIME_NODEATTR);
		ArrayList k = new ArrayList(values.length - 1);
		ArrayList v = new ArrayList(values.length - 1);
		for(int i=0; i < values.length - 1; i++) {
			String tmp = new String(ProxyEvent.decodeString(values[i+1]));
			String[] tmp2 = tmp.split("=");
			if (tmp2.length == 2) {
				k.add(tmp2[0]);
				v.add(tmp2[1]);
			} else {
				System.out.println("Bad key=val node attribute pair : '"+tmp+"'");
			}
		}
		this.keys = (String[]) k.toArray(new String[k.size()]);
		this.values = (String[]) v.toArray(new String[v.size()]);
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

