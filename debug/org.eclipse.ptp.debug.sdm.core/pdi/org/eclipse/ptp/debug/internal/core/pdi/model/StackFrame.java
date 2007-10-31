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

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgument;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.Locator;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;


/**
 * @author clement
 *
 */
public class StackFrame extends SessionObject implements IPDIStackFrame {
	public class Arg {
		String name;
		String value;
		public Arg(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}
	}
	IPDIThread pthread;
	IPDIArgumentDescriptor[] argDescs;
	IPDILocalVariableDescriptor[] localDescs;
	Locator fLocator;

	//Frame details
	int level = -1;
	Arg[] args = new Arg[0];
	
	public StackFrame(Session session, Thread thread, int level, IPDILocator locator, Arg[] args) {
		super(session, thread.getTasks());
		this.pthread = thread;
		this.level = level;
		this.fLocator = (Locator)locator;
		if (args != null) 
			this.args = args;
	}
	public StackFrame(Session session, Thread thread, int level, String file, String func, int line, BigInteger addr, Arg[] args) {
		this(session, thread, level, new Locator(file, func, line, addr), args);
	}
	public IPDIThread getThread() {
		return pthread;
	}
	public IPDITarget getTarget() {
		return getThread().getTarget();
	}
	public IPDIArgumentDescriptor[] getArgumentDescriptors() throws PDIException {
		if (argDescs == null) {
			argDescs = session.getVariableManager().getArgumentDescriptors(this);
		}
		return argDescs;
	}
	public IPDILocalVariableDescriptor[] getLocalVariableDescriptors() throws PDIException {
		if (localDescs == null) {
			localDescs = session.getVariableManager().getLocalVariableDescriptors(this);
		}
		return localDescs;
	}
	public IPDIArgument createArgument(IPDIArgumentDescriptor varDesc) throws PDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return session.getVariableManager().createArgument((ArgumentDescriptor)varDesc);
		}
		return null;
	}
	public IPDILocalVariable createLocalVariable(IPDILocalVariableDescriptor varDesc) throws PDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((IPDIArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return session.getVariableManager().createLocalVariable((LocalVariableDescriptor)varDesc);			
		}
		return null;
	}
	public IPDILocator getLocator() {
		if (fLocator != null) {
			return fLocator;
		}
		return new Locator("", "", 0, BigInteger.ZERO);
	}
	public int getLevel() {
		return level;
	}
	public boolean equals(IPDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			boolean equal =  pthread != null && pthread.equals(stack.getThread()) && getLevel() == stack.getLevel();
			if (equal) {
				IPDILocator otherLocator = stack.getLocator();
				IPDILocator myLocator = getLocator();
				if (Locator.equalString(myLocator.getFile(), otherLocator.getFile())) {
					if (Locator.equalString(myLocator.getFunction(), otherLocator.getFunction())) {
						return true;
					}
				}
			}
		}
		return super.equals(stackframe);
	}
	public void stepReturn() throws PDIException {
		((Thread)getThread()).setCurrentStackFrame(this, false);
		session.stepReturn(getTasks(), 0);
	}
	public void stepReturn(IAIF aif) throws PDIException {
		((Thread)getThread()).setCurrentStackFrame(this, false);
		session.stepReturn(getTasks(), aif);
	}
}
