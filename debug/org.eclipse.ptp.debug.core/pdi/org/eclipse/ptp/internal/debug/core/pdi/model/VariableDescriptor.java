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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 * 
 */
public abstract class VariableDescriptor extends SessionObject implements IPDIVariableDescriptor {
	/**
	 * @param var1
	 * @param var2
	 * @return
	 */
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

	// Casting info.
	protected String[] castingTypes;
	protected int castingIndex;

	protected int castingLength;
	protected String fName;
	protected int position;
	protected IPDIStackFrame fStackFrame;
	protected IPDIThread fThread;

	protected int stackdepth;
	protected String qualifiedName = null;
	protected String fFullName = null;
	protected String fTypename = null;
	protected String varId = null;
	protected IAIF aif = null;

	public VariableDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame stack, String n, String fn,
			int pos, int depth) {
		super(session, tasks);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
	}

	public VariableDescriptor(IPDISession session, IPDIVariableDescriptor desc) {
		super(session, desc.getTasks());
		this.fName = desc.getName();
		this.fFullName = desc.getFullName();
		try {
			this.fStackFrame = desc.getStackFrame();
			this.fThread = desc.getThread();
		} catch (PDIException e) {
		}
		this.position = desc.getPosition();
		this.stackdepth = desc.getStackDepth();
		this.castingIndex = desc.getCastingArrayStart();
		this.castingLength = desc.getCastingArrayEnd();
		this.castingTypes = desc.getCastingTypes();
	}

	/*
	 * FIXME -- it designs for GDB
	 */
	/**
	 * @return
	 */
	public String encodeVariable() {
		String fn = getFullName();
		if (castingLength > 0 || castingIndex > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("*("); //$NON-NLS-1$
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

	/**
	 * @param varDesc
	 * @return
	 */
	public boolean equalDescriptors(IPDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName()) && desc.getCastingArrayStart() == getCastingArrayStart()
					&& desc.getCastingArrayEnd() == getCastingArrayEnd() && equalsCasting(desc, this)) {

				// Check the threads
				IPDIThread varThread = null;
				IPDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (PDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null)
						|| (varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					IPDIStackFrame varFrame = null;
					IPDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (PDIException e) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getAIF()
	 */
	public IAIF getAIF() throws PDIException {
		if (aif == null) {
			Target target = (Target) fStackFrame.getTarget();
			Thread currentThread = target.getCurrentThread();
			IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(fStackFrame.getThread(), false);
				((Thread) fStackFrame.getThread()).setCurrentStackFrame(fStackFrame, false);

				IPDIEvaluatePartialExpressionRequest request = session.getRequestFactory().getEvaluatePartialExpressionRequest(
						getTasks(), getQualifiedName(), varId, false);
				session.getEventRequestManager().addEventRequest(request);
				aif = request.getPartialAIF(getTasks());
				varId = request.getId(getTasks());
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return aif;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * getCastingArrayEnd()
	 */
	public int getCastingArrayEnd() {
		return castingLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * getCastingArrayStart()
	 */
	public int getCastingArrayStart() {
		return castingIndex;
	}

	/**
	 * @return
	 */
	public String[] getCastingTypes() {
		return castingTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getFullName()
	 */
	public String getFullName() {
		if (fFullName == null) {
			fFullName = getName();
		}
		return fFullName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getPosition()
	 */
	public int getPosition() {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getQualifiedName
	 * ()
	 */
	public String getQualifiedName() throws PDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getStackDepth
	 * ()
	 */
	public int getStackDepth() {
		return stackdepth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getStackFrame
	 * ()
	 */
	public IPDIStackFrame getStackFrame() throws PDIException {
		return fStackFrame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getThread()
	 */
	public IPDIThread getThread() throws PDIException {
		return fThread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getTypeName()
	 */
	public String getTypeName() throws PDIException {
		if (fTypename == null) {
			fTypename = getAIF().getType().toString();
		}
		return fTypename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * getVariableDescriptorAsArray(int, int)
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PDIException {
		return session.getVariableManager().getVariableDescriptorAsArray(this, start, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * getVariableDescriptorAsType(java.lang.String)
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsType(String type) throws PDIException {
		return session.getVariableManager().getVariableDescriptorAsType(this, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#getId()
	 */
	public String getId() {
		return varId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#setAIF(org
	 * .eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	public void setAIF(IAIF aif) {
		this.aif = aif;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * setCastingArrayEnd(int)
	 */
	public void setCastingArrayEnd(int end) {
		castingLength = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#
	 * setCastingArrayStart(int)
	 */
	public void setCastingArrayStart(int start) {
		castingIndex = start;
	}

	/**
	 * @param t
	 */
	public void setCastingTypes(String[] t) {
		castingTypes = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor#sizeof()
	 */
	public int sizeof() throws PDIException {
		return getAIF().getType().sizeof();
	}
}
