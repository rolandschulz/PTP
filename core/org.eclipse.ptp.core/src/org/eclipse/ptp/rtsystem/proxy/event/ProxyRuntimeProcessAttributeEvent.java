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

import org.eclipse.ptp.core.util.BitList;

public class ProxyRuntimeProcessAttributeEvent extends AbstractProxyRuntimeEvent implements IProxyRuntimeEvent {
	private int			jobid;
	private BitList		commProcs;
	private String		keyValue;
	private int[]		diffProcs;
	private String[]	keyValues;

	public ProxyRuntimeProcessAttributeEvent(int jobid, BitList commProcs, String kv, int[] diffProcs, String[] kvs) {
		super(EVENT_RUNTIME_PROCATTR);
		this.jobid = jobid;
		this.commProcs = commProcs;
		this.keyValue = kv;
		this.diffProcs = diffProcs;
		this.keyValues = kvs;
	}
	
	public int getJobID() {
		return this.jobid;
	}
	
	public BitList getCommProcs() {
		return this.commProcs;
	}
	
	public String getKeyValue() {
		return this.keyValue;
	}
	
	public int[] getDiffProcs() {
		return this.diffProcs;
	}
	
	public String[] getKeyValues() {
		return this.keyValues;
	}
	
	public String toString() {
		String res = "EVENT_RUNTIME_PROCATTR job=" + this.jobid + " {" + this.commProcs.toString() + "}:<" + this.keyValue + "> ";
		for (int i = 0; i < this.diffProcs.length; i++) {
			res += " [" + this.diffProcs[i] + "]:<" + this.keyValues[i] + ">";
		}
		return res;
	}
}
