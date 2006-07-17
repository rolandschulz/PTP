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
package org.eclipse.ptp.debug.external.core.cdi.model;

import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.SignalManager;

/**
 * @author Clement chu
 */
public class Signal extends PObject implements IPCDISignal {
	String name;
	boolean stop;
	boolean print;
	boolean pass;
	String desc;

	public Signal(Target target, IPCDISignal sig) {
		super(target);
		setSignal(sig);
	}
	
	public Signal(Target target, String name, boolean stop, boolean print, boolean pass, String desc) {
		super(target);
		this.name = name;
		this.stop = stop;
		this.print = print;
		this.pass = pass;
		this.desc = desc;
	}

	/**
	 * @see org.eclipse.ptp.debug.core.cdi.IPCDIInferiorSignaled#getMeaning()
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * @see org.eclipse.ptp.debug.core.cdi.IPCDIInferiorSignaled#getName()
	 */
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
	public void handle(boolean isIgnore, boolean isStop) throws PCDIException {
		SignalManager mgr = ((Session)getTarget().getSession()).getSignalManager();
		mgr.handle(this, isIgnore, isStop);
	}
	public void setHandle(boolean isIgnore, boolean isStop) {
		pass = !isIgnore;
		stop = isStop;
	}

	/**
	 * @see org.eclipse.ptp.debug.core.cdi.IPCDIInferiorSignaled#isIgnore()
	 */
	public boolean isIgnore() {
		return !isPass();
	}

	/**
	 * @see org.eclipse.ptp.debug.core.cdi.IPCDIInferiorSignaled#isStopSet()
	 */
	public boolean isStopSet() {
		return isStop();
	}

	/**
	 * Continue program giving it signal specified by the argument.
	 */
	public void signal() throws PCDIException {
		getTarget().resume(this);
	}
	
	public void setSignal(IPCDISignal sig) {
		this.name = sig.getName();
		this.stop = sig.isStop();
		this.print = sig.isPrint();
		this.pass = sig.isPass();
		this.desc = sig.getDescription();
	}
	
	public String toString() {
		return "Name: " + name + ", stop: " + stop + ", print: " + print + ", pass: " + pass + ", desc: " + desc; 
	}
}
