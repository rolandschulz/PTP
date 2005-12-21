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
package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.model.IPType;

/**
 * @author Clement chu
 * 
 */
public class PIndexedValue extends AbstractPValue implements IIndexedValue {
	private IAIFValueArray fCDIValue;
	private IVariable[] fVariables;
	private int fOffset;
	private int fSize;
	private IPType fType;

	public PIndexedValue(AbstractPVariable parent, IAIFValueArray cdiValue, int offset, int size) {
		super(parent);
		fVariables = new IVariable[size];
		fCDIValue = cdiValue;
		fOffset = offset;
		fSize = size;
	}
	protected void setChanged(boolean changed) {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).setChanged(changed);
			}
		}
	}
	public void dispose() {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).dispose();
			}
		}
	}
	protected void reset() {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).resetValue();
			}
		}
	}
	protected void preserve() {
		resetStatus();
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).preserve();
			}
		}
	}
	public IPType getType() throws DebugException {
		if (fType == null) {
			synchronized (this) {
				if (fType == null) {
					IAIF aif = getAIF();
					if (aif != null) {
						fType = new PType(aif.getType());
					}
				}
			}
		}
		return fType;
	}
	public String getReferenceTypeName() throws DebugException {
		IPType type = getType();
		return (type != null) ? type.getName() : "";
	}
	public String getValueString() throws DebugException {
		return "";
	}
	public boolean isAllocated() throws DebugException {
		return true;
	}
	public IVariable[] getVariables() throws DebugException {
		return getVariables0(getInitialOffset(), getSize());
	}
	public boolean hasVariables() throws DebugException {
		return getSize() > 0;
	}
	public IVariable getVariable(int offset) throws DebugException {
		if (offset >= getSize()) {
			requestFailed(CoreModelMessages.getString("CIndexedValue.0"), null);
		}
		return getVariables0(offset, 1)[0];
	}
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		if (offset >= getSize()) {
			requestFailed(CoreModelMessages.getString("CIndexedValue.1"), null);
		}
		if ((offset + length - 1) >= getSize()) {
			requestFailed(CoreModelMessages.getString("CIndexedValue.2"), null);
		}
		return getVariables0(offset, length);
	}
	public int getSize() throws DebugException {
		return getSize0();
	}
	public int getInitialOffset() {
		return fOffset;
	}
	protected IAIFValueArray getCDIValue() {
		return fCDIValue;
	}
	private int getPartitionSize(int index) {
		int psize = getPreferredPartitionSize();
		int size = getSize0();
		int pcount = size / psize + 1;
		if (pcount - 1 < index)
			return 0;
		return (pcount - 1 == index) ? size % psize : psize;
	}
	private int getPartitionIndex(int offset) {
		return offset / getPreferredPartitionSize();
	}
	private int getPreferredPartitionSize() {
		return 100;
	}
	private IVariable[] getVariables0(int offset, int length) throws DebugException {
		IVariable[] result = new IVariable[length];
		int firstIndex = getPartitionIndex(offset);
		int lastIndex = getPartitionIndex(offset + Math.max(length - 1, 0));
		for (int i = firstIndex; i <= lastIndex; ++i) {
			synchronized (this) {
				if (!isPartitionLoaded(i)) {
					loadPartition(i);
				}
			}
		}
		System.arraycopy(fVariables, offset, result, 0, length);
		return result;
	}
	private boolean isPartitionLoaded(int index) {
		return fVariables[index * getPreferredPartitionSize()] != null;
	}
	private void loadPartition(int index) throws DebugException {
		int prefSize = getPreferredPartitionSize();
		int psize = getPartitionSize(index);
		
		IPCDIVariable[] cdiVars = new IPCDIVariable[0]; 
		try {
			cdiVars = getParentVariable().getCDIVariable().getVariablesAsArray(index * prefSize, psize);
		} catch (CDIException e) {
			requestFailed(e.getMessage(), null);
		}
		for (int i=0; i<psize; ++i) {
			fVariables[i + index * prefSize] = PVariableFactory.createLocalVariable(getParentVariable(), cdiVars[i]);
		}
	}
	private int getSize0() {
		return fSize;
	}
}
