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
package org.eclipse.ptp.debug.external.core.cdi.model.variable;

import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariableDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.VariableManager;
import org.eclipse.ptp.debug.external.core.cdi.model.PObject;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;
import org.eclipse.ptp.debug.external.core.commands.GetPartialAIFCommand;

/**
 * @author Clement chu
 *
 */
public abstract class VariableDescriptor extends PObject implements IPCDIVariableDescriptor {
	// Casting info.
	String[] castingTypes;
	int castingIndex;
	int castingLength;

	String fName;
	int position;
	StackFrame fStackFrame;
	Thread fThread;
	int stackdepth;

	String qualifiedName = null;
	String fFullName = null;
	String fTypename = null;
	IAIFType fType = null;
	IAIFValue fValue = null;
	String sizeof = null;
	
	String keyName = null;
	
	public VariableDescriptor(VariableDescriptor desc) {
		super((Target)desc.getTarget());
		fName = desc.getName();
		fFullName = desc.fFullName;
		sizeof = desc.sizeof;
		fType = desc.fType;
		try {
			fStackFrame = (StackFrame)desc.getStackFrame();
			fThread = (Thread)desc.getThread();
		} catch (PCDIException e) {
		}
		position = desc.getPosition();
		stackdepth = desc.getStackDepth();
		castingIndex = desc.getCastingArrayStart();
		castingLength = desc.getCastingArrayEnd();
		castingTypes = desc.getCastingTypes();
	}
	public VariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth) {
		super(target);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
	}
	public int getPosition() {
		return position;
	}
	public int getStackDepth() {
		return stackdepth;
	}
	public void setCastingArrayStart(int start) {
		castingIndex = start;
	}
	public int getCastingArrayStart() {
		return castingIndex;
	}
	public void setCastingArrayEnd(int end) {
		castingLength = end;
	}
	public int getCastingArrayEnd() {
		return castingLength;
	}
	public void setCastingTypes(String[] t) {
		castingTypes = t;
	}
	public String[] getCastingTypes() {
		return castingTypes;
	}
	/**
	 * the variable was a cast encode, the string appropriately for GDB.
	 */
	public String encodeVariable() {
		String fn = getFullName();
		if (castingLength > 0 || castingIndex > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("*(");
			buffer.append('(').append(fn).append(')');
			buffer.append('+').append(castingIndex).append(')');
			buffer.append('@').append(castingLength);
			fn = buffer.toString();
		} else if (castingTypes != null && castingTypes.length > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < castingTypes.length; ++i) {
				if (castingTypes[i] != null && castingTypes[i].length() > 0) {
					if (buffer.length() == 0) {
						buffer.append('(').append(castingTypes[i]).append(')');
						buffer.append(fn);
					} else {
						buffer.insert(0, '(');
						buffer.append(')');
						StringBuffer b = new StringBuffer();
						b.append('(').append(castingTypes[i]).append(')');
						buffer.insert(0, b.toString());
					}
				}
			}
			fn = buffer.toString();
		}
		return fn;
	}	
	public String getFullName() {
		if (fFullName == null) {
			fFullName = getName();
		}
		return fFullName;
	}
	public String getName() {
		return fName;
	}
	public IAIFType getType() throws PCDIException {
		if (fType == null || fType == AIFFactory.UNKNOWNTYPE) {
			try {
				Target target = (Target)getTarget();
				GetPartialAIFCommand command = new GetPartialAIFCommand(target.getTask(), getQualifiedName(), "");
				target.getDebugger().postCommand(command);
				setAIF(command.getPartialAIF());
				keyName = command.getName();
			} catch (PCDIException e) {
				fType = AIFFactory.UNKNOWNTYPE;
				fValue = AIFFactory.UNKNOWNVALUE;
			}
		}
		return fType;
	}
	public void setAIF(IAIF aif) {
		fType = aif.getType();
		fValue = aif.getValue();
		fTypename = aif.getDescription();
	}
	public int sizeof() throws PCDIException {
		return getType().sizeof();
			/*
			Target target = (Target) getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			StackFrame frame = (StackFrame)getStackFrame();
			Thread thread = (Thread)getThread();
			if (frame != null) {
				target.setCurrentThread(frame.getThread(), false);				
				((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			} else if (thread != null) {
				target.setCurrentThread(thread, false);				
			}
			String exp = "sizeof(" + getTypeName() + ")";
			Session session = (Session)target.getSession();			
			try {
				EvaluteExpressionCommand command = new EvaluteExpressionCommand(session.createBitList(target.getTargetID()), exp);
				session.getDebugger().postCommand(command);
				sizeof = command.getExpressionValue();
			} finally {
				if (frame != null) {
					target.setCurrentThread(currentThread, false);
					currentThread.setCurrentStackFrame(currentFrame, false);
				} else if (thread != null) {
					target.setCurrentThread(currentThread, false);
				}
			}
			if (sizeof != null) {
				try {
					return Integer.parseInt(sizeof);
				} catch (NumberFormatException e) {
					throw new PCDIException(e.getMessage());
				}
			}
			*/
	}
	public IPCDIStackFrame getStackFrame() throws PCDIException {
		return fStackFrame;
	}
	public IPCDIThread getThread() throws PCDIException {
		return fThread;
	}
	public String getTypeName() throws PCDIException {
		if (fTypename == null) {
			fTypename = getType().toString();
		}
		return fTypename;
	}
	public String getQualifiedName() throws PCDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}

	public static boolean equalsCasting(VariableDescriptor var1, VariableDescriptor var2) {
		String[] castings1 = var1.getCastingTypes();
		String[] castings2 = var2.getCastingTypes();
		if (castings1 == null && castings2 == null) {
			return true;
		} else if (castings1 != null && castings2 != null && castings1.length == castings2.length) {
			for (int i = 0; i < castings1.length; ++i) {
				if (!castings1[i].equals(castings2[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public boolean equals(IPCDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName())
				&& desc.getCastingArrayStart() == getCastingArrayStart()
				&& desc.getCastingArrayEnd() == getCastingArrayEnd()
				&& equalsCasting(desc, this)) {

				// Check the threads
				IPCDIThread varThread = null;
				IPCDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (PCDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null) || (varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					IPCDIStackFrame varFrame = null;
					IPCDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (PCDIException e) {
						// ignore
					}
					if (ourFrame == null && varFrame == null) {
						return true;
					} else if (varFrame != null && ourFrame != null && varFrame.equals(ourFrame)) {
						if (desc.getStackDepth() == getStackDepth()) {
							if (desc.getPosition() == getPosition()) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return super.equals(varDesc);
	}
	public IPCDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsArray(this, start, length);
	}
	public IPCDIVariableDescriptor getVariableDescriptorAsType(String type) throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsType(this, type);
	}
	/*
	public IPCDIVariable[] getVariablesAsArray(int start, int length) throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariablesAsArray(this, start, length);
	}
	public IPCDIVariable createVariable() throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.createVariable(this);
	}
	public IPCDIVariable[] getVariables() throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariables(this);
	}
	*/
}
