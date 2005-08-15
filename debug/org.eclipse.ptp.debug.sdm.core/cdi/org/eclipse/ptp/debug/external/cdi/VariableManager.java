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
package org.eclipse.ptp.debug.external.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.Argument;
import org.eclipse.ptp.debug.external.cdi.model.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.LocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.LocalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Variable;
import org.eclipse.ptp.debug.external.cdi.model.VariableDescriptor;
import org.eclipse.ptp.debug.external.simulator.SimStackFrame;
import org.eclipse.ptp.debug.external.simulator.SimVariable;

/**
 */
public class VariableManager extends Manager {

	public VariableManager(Session session) {
		super(session, true);
	}

	protected void update(Target target) throws CDIException {
		// Auto-generated method stub
		System.out.println("VariableManager.update()");
	}
	
	public ICDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		SimStackFrame sFrame = frame.getSimStackFrame();
		Target target = (Target)frame.getTarget();
		int level = frame.getLevel();
		SimVariable[] args = sFrame.getArgs();

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				ArgumentDescriptor arg = new ArgumentDescriptor(target, null, frame, args[i].getName(), null, args.length - i, level);
				argObjects.add(arg);
			}
		}
		return (ICDIArgumentDescriptor[]) argObjects.toArray(new ICDIArgumentDescriptor[0]);
	}
	
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		SimStackFrame sFrame = frame.getSimStackFrame();
		Target target = (Target)frame.getTarget();
		int level = frame.getLevel();
		SimVariable[] args = sFrame.getLocalVars();

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				LocalVariableDescriptor arg = new LocalVariableDescriptor(target, null, frame, args[i].getName(), null, args.length - i, level);
				argObjects.add(arg);
			}
		}
		return (ICDILocalVariableDescriptor[]) argObjects.toArray(new ICDILocalVariableDescriptor[0]);
	}
	
	public Variable createVariable(VariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$			
	}
	
	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws CDIException {
		System.out.println("VariableManager.createLocalVariable()");
		StackFrame frame = (StackFrame) varDesc.getStackFrame();
		SimVariable[] vars = frame.getSimStackFrame().getLocalVars();
		for (int i = 0; i < vars.length; i++) {
			if (varDesc.getName().equals(vars[i].getName()))
				return new LocalVariable(varDesc, vars[i]);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}
	
	public Argument createArgument(ArgumentDescriptor argDesc) throws CDIException {
		System.out.println("VariableManager.createArgument()");
		StackFrame frame = (StackFrame) argDesc.getStackFrame();
		SimVariable[] vars = frame.getSimStackFrame().getArgs();
		for (int i = 0; i < vars.length; i++) {
			if (argDesc.getName().equals(vars[i].getName()))
				return new Argument(argDesc, vars[i]);
		}
		throw new CDIException(CDIResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

}
