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
package org.eclipse.ptp.debug.external.cdi.model.variable;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.SourceManager;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.PTPObject;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.type.IncompleteType;

public abstract class VariableDescriptor extends PTPObject implements ICDIVariableDescriptor {
	
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
	protected ICDIType fType = null;
	protected String fTypename = null;
	String sizeof = null;
	IAIF aif = null;
	
	public VariableDescriptor(VariableDescriptor desc) {
		super((Target)desc.getTarget());
		fName = desc.getName();
		fFullName = desc.fFullName;
		sizeof = desc.sizeof;
		fType = desc.fType;
		aif = desc.getAIF();
		try {
			fStackFrame = (StackFrame)desc.getStackFrame();
			fThread = (Thread)desc.getThread();
		} catch (CDIException e) {
		}
		position = desc.getPosition();
		stackdepth = desc.getStackDepth();
		castingIndex = desc.getCastingArrayStart();
		castingLength = desc.getCastingArrayEnd();
		castingTypes = desc.getCastingTypes();
	}
	
	public VariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth, IAIF aif) {
		super(target);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
		this.aif = aif;
	}
	public IAIF getAIF() {
		return aif;
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
	
	public String encodeVariable() {
		String fn = getFullName();
		if (castingLength > 0 || castingIndex > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("*(");
			buffer.append('(').append(fn).append(')');
			if (castingIndex != 0) {
				buffer.append('+').append(castingIndex);
			}
			buffer.append(')');
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
	public ICDIType getType() throws CDIException {
		if (fType == null) {
			String nametype = getTypeName();
			Target target = (Target)getTarget();
			Session session = (Session) target.getSession();
			SourceManager sourceMgr = session.getSourceManager();
			try {
				fType = sourceMgr.getType(target, nametype);
			} catch (CDIException e) {
				// Try with ptype.
				try {
					String ptype = sourceMgr.getDetailTypeName(target, nametype);
					fType = sourceMgr.getType(target, ptype);
				} catch (CDIException ex) {
					// Some version of gdb does not work on the name of the class
					// ex: class data foo --> ptype data --> fails
					// ex: class data foo --> ptype foo --> succeed
					StackFrame frame = (StackFrame)getStackFrame();
					if (frame == null) {
						Thread thread = (Thread)getThread();
						if (thread != null) {
							frame = thread.getCurrentStackFrame();
						} else {
							frame = ((Thread)target.getCurrentThread()).getCurrentStackFrame();
						}
					}
					try {
						String ptype = sourceMgr.getDetailTypeNameFromVariable(frame, getQualifiedName());
						fType = sourceMgr.getType(target, ptype);
					} catch (CDIException e2) {
						// give up.
					}
				}
			}
			if (fType == null) {
				fType = new IncompleteType(target, nametype);
			}
		}
		return fType;
	}
	public int sizeof() throws CDIException {
		if (sizeof == null) {
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
				sizeof = target.getDebugger().evaluateExpression(session.createBitList(target.getTargetID()), exp);
			} finally {
				if (frame != null) {
					target.setCurrentThread(currentThread, false);
					currentThread.setCurrentStackFrame(currentFrame, false);
				} else if (thread != null) {
					target.setCurrentThread(currentThread, false);
				}
			}
		}

		if (sizeof != null) {
			try {
				return Integer.parseInt(sizeof);
			} catch (NumberFormatException e) {
				throw new CDIException(e.getMessage());
			}
		}
		return 0;
	}
	public ICDIStackFrame getStackFrame() throws CDIException {
		return fStackFrame;
	}

	public ICDIThread getThread() throws CDIException {
		return fThread;
	}
	public String getTypeName() throws CDIException {
		if (aif != null) {
			//TODO - fix the toString later
			fTypename = aif.getType().toString();
		}
		else {
			Target target = (Target)getTarget();
			StackFrame frame = (StackFrame)getStackFrame();
			if (frame == null) {
				Thread thread = (Thread)getThread();
				if (thread != null) {
					frame = thread.getCurrentStackFrame();
				} else {
					frame = ((Thread)target.getCurrentThread()).getCurrentStackFrame();
				}
			}
			Session session = (Session) target.getSession();
			SourceManager sourceMgr = session.getSourceManager();
			if (frame != null) {
				fTypename = sourceMgr.getTypeNameFromVariable(frame, getQualifiedName());
			} else {
				fTypename = sourceMgr.getTypeName(target, getQualifiedName());
			}
		}
		return fTypename;
	}
	public String getQualifiedName() throws CDIException {
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
	public boolean equals(ICDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName())
				&& desc.getCastingArrayStart() == getCastingArrayStart()
				&& desc.getCastingArrayEnd() == getCastingArrayEnd()
				&& equalsCasting(desc, this)) {

				// Check the threads
				ICDIThread varThread = null;
				ICDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (CDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null) ||
						(varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					ICDIStackFrame varFrame = null;
					ICDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (CDIException e) {
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
	public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsArray(this, start, length);
	}
	public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.getVariableDescriptorAsType(this, type);
	}
	
	public ICDIVariable createVariable() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.createVariable(this);
	}
}
