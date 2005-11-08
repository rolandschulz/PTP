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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi.model;

import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.external.ExtFormat;
import org.eclipse.ptp.debug.external.cdi.Locator;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariableDescriptor;
import org.eclipse.ptp.debug.external.commands.GetAIFCommand;
import org.eclipse.ptp.debug.external.commands.StepFinishCommand;

public class StackFrame extends PTPObject implements ICDIStackFrame {
	Thread cthread;
	ICDIArgumentDescriptor[] argDescs;
	ICDILocalVariableDescriptor[] localDescs;
	Locator fLocator;
	int level = -1;
	String addr = "";
	String file = "";
	int line = -1;
	String func = "";

	public StackFrame(Thread thread, int level, String file, String func, int line, String addr) {
		super((Target)thread.getTarget());
		cthread = thread;
		this.level = level;
		this.addr = addr;
		this.file = file;
		this.line = line;
		this.func = func;
	}
	public ICDIThread getThread() {
		return cthread;
	}
	public ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException {
		if (argDescs == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			argDescs = mgr.getArgumentDescriptors(this);
		}
		else {
			Target target = (Target)getTarget();
			Session session = (Session)target.getSession();
			BitList tasks = session.createBitList(target.getTargetID());
			for (int i=0; i<argDescs.length; i++) {
				ArgumentDescriptor argDesc = (ArgumentDescriptor)argDescs[i];
				GetAIFCommand command = new GetAIFCommand(tasks, argDesc.getQualifiedName());
				session.getDebugger().postCommand(command);
				argDesc.setAIF(command.getAIF());
			}
		}
		return argDescs;
	}
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException {
		if (localDescs == null) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			localDescs = mgr.getLocalVariableDescriptors(this);
		}
		else {
			Target target = (Target)getTarget();
			Session session = (Session)target.getSession();
			BitList tasks = session.createBitList(target.getTargetID());
			for (int i=0; i<localDescs.length; i++) {
				LocalVariableDescriptor localDesc = (LocalVariableDescriptor)localDescs[i];
				GetAIFCommand command = new GetAIFCommand(tasks, localDesc.getQualifiedName());
				session.getDebugger().postCommand(command);
				localDesc.setAIF(command.getAIF());
			}
		}
		return localDescs;
	}
	public ICDIArgument createArgument(ICDIArgumentDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createArgument((ArgumentDescriptor)varDesc);
		}
		return null;
	}
	public ICDILocalVariable createLocalVariable(ICDILocalVariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ICDIArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createLocalVariable((LocalVariableDescriptor)varDesc);			
		}
		return null;
	}
	public ICDILocator getLocator() {
		BigInteger bigAddr = BigInteger.ZERO;
		if (fLocator == null) {
			bigAddr = ExtFormat.getBigInteger(getAddress());
			fLocator = new Locator(getFile(), getFunction(), getLine(), bigAddr);
		}
		return fLocator;
		//return new Locator("", "", 0, bigAddr);
	}	
	public boolean equals(ICDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			boolean equal =  cthread != null && cthread.equals(stack.getThread()) && getLevel() == stack.getLevel();
			if (equal) {
				ICDILocator otherLocator = stack.getLocator();
				ICDILocator myLocator = getLocator();
				if (Locator.equalString(myLocator.getFile(), otherLocator.getFile())) {
					if (Locator.equalString(myLocator.getFunction(), otherLocator.getFunction())) {
						return true;
					}
				}
			}
		}
		return super.equals(stackframe);
	}
	public void stepReturn() throws CDIException {
		finish();
	}
	public void stepReturn(ICDIValue value) throws CDIException {
		execReturn(value.toString());
	}
	protected void finish() throws CDIException {
		((Thread)getThread()).setCurrentStackFrame(this, false);
		Target target = (Target)getTarget();
		target.getDebugger().postCommand(new StepFinishCommand(((Session)target.getSession()).createBitList(target.getTargetID())));
	}	
	protected void execReturn(String value) throws CDIException {
		((Thread)getThread()).setCurrentStackFrame(this, false);
		Target target = (Target)getTarget();
		target.getDebugger().postCommand(new StepFinishCommand(((Session)target.getSession()).createBitList(target.getTargetID())));
	}

	public int getLevel() {
		return level;
	}
	public String getAddress() {
		return addr;
	}
	public String getFile() {
		return file;
	}
	public int getLine() {
		return line;
	}
	public String getFunction() {
		return func;
	}
}
