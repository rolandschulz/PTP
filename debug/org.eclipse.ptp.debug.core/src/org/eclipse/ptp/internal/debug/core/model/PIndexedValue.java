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
package org.eclipse.ptp.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class PIndexedValue extends AbstractPValue implements IIndexedValue {
	private final int fOffset;
	private final int fSize;
	private final IVariable[] fVariables;
	private final IPDIVariable pdiVariable;

	public PIndexedValue(AbstractPVariable parent, IPDIVariable variable, int offset, int size) {
		super(parent);
		fVariables = new IVariable[size];
		pdiVariable = variable;
		fOffset = offset;
		fSize = size;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#dispose()
	 */
	@Override
	public void dispose() {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).dispose();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.model.IPValue#getAIF()
	 */
	public IAIF getAIF() throws DebugException {
		try {
			return pdiVariable.getAIF();
		} catch (final PDIException e) {
			targetRequestFailed(e.getMessage(), e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return fOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return getAIF().getType().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	public int getSize() throws DebugException {
		return getSize0();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		return "[" + fOffset + " - " + fSize + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	public IVariable getVariable(int offset) throws DebugException {
		if (offset >= getSize()) {
			requestFailed(Messages.PIndexedValue_0, null);
		}
		return getVariables0(offset, 1)[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return getVariables0(getInitialOffset(), getSize());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		if (offset >= getSize()) {
			requestFailed(Messages.PIndexedValue_0, null);
		}
		if ((offset + length - 1) >= getSize()) {
			requestFailed(Messages.PIndexedValue_1, null);
		}
		return getVariables0(offset, length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getSize() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/**
	 * @param offset
	 * @return
	 */
	private int getPartitionIndex(int offset) {
		return offset / getPreferredPartitionSize();
	}

	/**
	 * @param index
	 * @return
	 */
	private int getPartitionSize(int index) {
		final int psize = getPreferredPartitionSize();
		final int size = getSize0();
		final int pcount = size / psize + 1;
		if (pcount - 1 < index) {
			return 0;
		}
		return (pcount - 1 == index) ? size % psize : psize;
	}

	/**
	 * @return
	 */
	private int getPreferredPartitionSize() {
		return 100;
	}

	/**
	 * @return
	 */
	private int getSize0() {
		return fSize;
	}

	/**
	 * @param offset
	 * @param length
	 * @return
	 * @throws DebugException
	 */
	private IVariable[] getVariables0(int offset, int length) throws DebugException {
		final IVariable[] result = new IVariable[length];
		final int firstIndex = getPartitionIndex(offset);
		final int lastIndex = getPartitionIndex(offset + Math.max(length - 1, 0));
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

	/**
	 * @param index
	 * @return
	 */
	private boolean isPartitionLoaded(int index) {
		return fVariables[index * getPreferredPartitionSize()] != null;
	}

	/**
	 * @param index
	 * @throws DebugException
	 */
	private void loadPartition(int index) throws DebugException {
		final int prefSize = getPreferredPartitionSize();
		final int psize = getPartitionSize(index);
		final int findex = index * prefSize;
		IPDIVariable[] pdiVars = new IPDIVariable[0];
		try {
			pdiVars = pdiVariable.getChildren(findex, psize);
		} catch (final PDIException e) {
			requestFailed(e.getMessage(), null);
		}
		for (int i = 0; i < pdiVars.length; ++i) {
			fVariables[i + findex] = PVariableFactory.createLocalVariable(this, pdiVars[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#preserve()
	 */
	@Override
	protected void preserve() {
		resetStatus();
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).preserve();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#reset()
	 */
	@Override
	protected void reset() {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).resetValue();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#setChanged(boolean)
	 */
	@Override
	protected void setChanged(boolean changed) {
		for (int i = 0; i < fVariables.length; ++i) {
			if (fVariables[i] != null) {
				((AbstractPVariable) fVariables[i]).setChanged(changed);
			}
		}
	}
}
