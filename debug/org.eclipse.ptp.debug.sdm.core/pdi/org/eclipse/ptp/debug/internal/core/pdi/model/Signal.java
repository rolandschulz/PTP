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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;

/**
 * @author Clement chu
 */
public class Signal extends SessionObject implements IPDISignal {
	String name;
	boolean stop;
	boolean print;
	boolean pass;
	String desc;

	public Signal(Session session, BitList tasks, String name, boolean stop, boolean print, boolean pass, String desc) {
		super(session, tasks);
		this.name = name;
		this.stop = stop;
		this.print = print;
		this.pass = pass;
		this.desc = desc;
	}
	public String getDescription() {
		return desc;
	}
	public String getName() {
		return name;
	}
	public boolean isStop() {
		return stop;
	}
	public boolean isPass() {
		return pass;
	}
	public boolean isPrint() {
		return print;
	}
	public void setSignal(String name, boolean stop, boolean print, boolean pass, String desc) {
		this.name = name;
		this.stop = stop;
		this.print = print;
		this.pass = pass;
		this.desc = desc;
	}
	public void handle(boolean isIgnore, boolean isStop) throws PDIException {
		session.getSignalManager().handle(this, isIgnore, isStop);
	}
	public void setHandle(boolean isIgnore, boolean isStop) {
		pass = !isIgnore;
		stop = isStop;
	}
	public boolean isIgnore() {
		return !isPass();
	}
	public boolean isStopSet() {
		return isStop();
	}
	public void signal() throws PDIException {
		session.resume(getTasks(), this);
	}
	public String toString() {
		return "Name: " + name + ", stop: " + stop + ", print: " + print + ", pass: " + pass + ", desc: " + desc; 
	}
}
