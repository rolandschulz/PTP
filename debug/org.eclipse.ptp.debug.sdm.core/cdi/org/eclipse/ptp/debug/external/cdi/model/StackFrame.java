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
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.Locator;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariableDescriptor;

public class StackFrame extends PTPObject implements ICDIStackFrame {
	Thread cthread;
	int sLevel;
	String sFile;
	String sFunction;
	int sLine;
	String sAddress;
	ICDIArgumentDescriptor[] argDescs;
	ICDILocalVariableDescriptor[] localDescs;
	Locator fLocator;

	public StackFrame(Thread thread, int level, String file, String function, int line, String address) {
		super((Target)thread.getTarget());
		cthread = thread;
		sLevel = level;
		sFile = file;
		sFunction = function;
		sLine = line;
		sAddress = address;
	}
	
	public BigInteger getBigInteger(String address) {
		int index = 0;
		int radix = 10;
		boolean negative = false;

		// Handle zero length
		address = address.trim();
		if (address.length() == 0) {
			return BigInteger.ZERO;
		}

		// Handle minus sign, if present
		if (address.startsWith("-")) { //$NON-NLS-1$
			negative = true;
			index++;
		}
		if (address.startsWith("0x", index) || address.startsWith("0X", index)) { //$NON-NLS-1$ //$NON-NLS-2$
			index += 2;
			radix = 16;
		} else if (address.startsWith("#", index)) { //$NON-NLS-1$
			index ++;
			radix = 16;
		} else if (address.startsWith("0", index) && address.length() > 1 + index) { //$NON-NLS-1$
			index ++;
			radix = 8;
		}

		if (index > 0) {
			address = address.substring(index);
		}
		if (negative) {
			address = "-" + address; //$NON-NLS-1$
		}
		try {
			return new BigInteger(address, radix);
		} catch (NumberFormatException e) {
			// ...
			// What can we do ???
		}
		return BigInteger.ZERO;
	}	

	public ICDILocator getLocator() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		BigInteger addr = BigInteger.ZERO;
		if (sFile != null && sFunction != null) {
			if (fLocator == null) {
				String address = sAddress;
				if (address != null) {
					addr = getBigInteger(address);
				}
				fLocator = new Locator(sFile, sFunction, sLine, addr);
			}
			return fLocator;
		}
		return new Locator("", "", 0, addr); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public ICDIThread getThread() {
		return cthread;
	}

	public int getLevel() {
		return sLevel;
	}

	public boolean equals(ICDIStackFrame stackframe) {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			boolean equal =  cthread != null &&
				cthread.equals(stack.getThread()) &&
				getLevel() == stack.getLevel();
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
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		
		// FIXME Donny, correct way to do this?
		((Thread) getThread()).stepReturn();
	}

	public void stepReturn(ICDIValue value) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
        if (localDescs == null) {
                Session session = (Session) getTarget().getSession();
                VariableManager mgr = session.getVariableManager();
                localDescs = mgr.getLocalVariableDescriptors(this);
        }
        return localDescs;
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
        if (argDescs == null) {
                Session session = (Session)getTarget().getSession();
                VariableManager mgr = session.getVariableManager();
                argDescs = mgr.getArgumentDescriptors(this);
        }
        return argDescs;
	}

	public ICDIArgument createArgument(ICDIArgumentDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		if (varDesc instanceof ArgumentDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createArgument((ArgumentDescriptor)varDesc);
		}
		return null;
	}

	public ICDILocalVariable createLocalVariable(ICDILocalVariableDescriptor varDesc) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ICDIArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager mgr = session.getVariableManager();
			return mgr.createLocalVariable((LocalVariableDescriptor)varDesc);			
		}
		return null;
	}
}
