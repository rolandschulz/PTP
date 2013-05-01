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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueString;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeDerived;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class PValue extends AbstractPValue {
	private String fValueString = null;
	private IPDIVariable fVariable;
	private List<IVariable> fVariables = Collections.emptyList();

	protected PValue(PVariable parent, IPDIVariable variable) {
		super(parent);
		fVariable = variable;
	}

	protected PValue(PVariable parent, String message) {
		super(parent);
		setStatus(IPDebugElementStatus.ERROR, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#dispose()
	 */
	@Override
	public void dispose() {
		final Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.model.IPValue#getAIF()
	 */
	public IAIF getAIF() throws DebugException {
		try {
			return fVariable.getAIF();
		} catch (final PDIException e) {
			targetRequestFailed(e.getMessage(), e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return (getParentVariable() != null) ? getParentVariable().getReferenceTypeName() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if (fValueString == null && getAIF() != null) {
			resetStatus();
			final IPStackFrame pframe = getParentVariable().getStackFrame();
			final boolean isSuspended = (pframe == null) ? getPDISession().isSuspended(getTasks()) : pframe.isSuspended();
			if (isSuspended) {
				try {
					if (fVariable == null) {
						targetRequestFailed(Messages.PValue_0, null);
					}
					fValueString = processUnderlyingValue(getAIF());
				} catch (final AIFException pe) {
					setStatus(IPDebugElementStatus.ERROR, pe.getMessage());
				}
			}
		}
		return fValueString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		final List<IVariable> list = getVariables0();
		return list.toArray(new IVariable[list.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		final IAIF aif = getAIF();
		if (aif != null) {
			final IAIFType type = aif.getType();
			if (type instanceof IAIFTypeAggregate || type instanceof IAIFTypeUnion || type instanceof ITypeDerived) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/**
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String getCharValueString(IAIFValueChar value) throws AIFException {
		final PVariableFormat format = getParentVariable().getFormat();
		final char charValue = value.charValue();
		if (PVariableFormat.NATURAL.equals(format)) {
			return value.getValueString();
		} else if (PVariableFormat.DECIMAL.equals(format)) {
			return Integer.toString((byte) charValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			final StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
			final String stringValue = Integer.toString((byte) charValue);
			sb.append((stringValue.length() > 2) ? stringValue.substring(stringValue.length() - 2) : stringValue);
			return sb.toString();
		}
		return null;
	}

	/**
	 * @param doubleValue
	 * @return
	 * @throws AIFException
	 */
	private String getDoubleValueString(String doubleValue) throws AIFException {
		final PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return doubleValue;
		}
		final Double dbl = new Double(doubleValue);
		if (dbl.isNaN() || dbl.isInfinite()) {
			return ""; //$NON-NLS-1$
		}
		final long longValue = dbl.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			final StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
			final String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 16) ? stringValue.substring(stringValue.length() - 16) : stringValue);
			return sb.toString();
		}
		return doubleValue;
	}

	/**
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String getFloatingPointValueString(IAIFValueFloat value) throws AIFException {
		if (value.isDouble()) {
			return getDoubleValueString(value.getValueString());
		} else if (value.isFloat()) {
			return getFloatValueString(value.getValueString());
		} else {
			return value.getValueString();
		}
	}

	/**
	 * @param floatValue
	 * @return
	 * @throws AIFException
	 */
	private String getFloatValueString(String floatValue) throws AIFException {
		final PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return floatValue;
		}
		final Float flt = new Float(floatValue);
		if (flt.isNaN() || flt.isInfinite()) {
			return ""; //$NON-NLS-1$
		}
		final long longValue = flt.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			final StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
			final String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return floatValue;
	}

	/**
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String getIntValueString(IAIFValueInt value) throws AIFException {
		final PVariableFormat format = getParentVariable().getFormat();
		String stringValue = value.getValueString();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
			return stringValue;
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			final StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
			if (value.isShort()) {
				stringValue = Integer.toHexString(value.shortValue());
			} else if (value.isInt()) {
				stringValue = Integer.toHexString(value.intValue());
			} else if (value.isLong()) {
				stringValue = Long.toHexString(value.longValue());
			}
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return null;
	}

	/**
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String getPointerValueString(IAIFValuePointer value) throws AIFException {
		final BigInteger pv = value.pointerValue();
		if (pv == null) {
			return ""; //$NON-NLS-1$
		}
		final PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.HEXADECIMAL.equals(format)) {
			return pv.toString(16);
		}
		if (PVariableFormat.DECIMAL.equals(format)) {
			return pv.toString(10);
		}
		return null;
	}

	/**
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String getWCharValueString(IAIFValueString value) throws AIFException {
		return value.getValueString();
	}

	/**
	 * @param type
	 * @param value
	 * @return
	 * @throws AIFException
	 */
	private String processUnderlyingValue(IAIFType type, IAIFValue value) throws AIFException {
		if (type instanceof IAIFTypeChar) {
			return getCharValueString((IAIFValueChar) value);
		} else if (type instanceof IAIFTypeInt) {
			return getIntValueString((IAIFValueInt) value);
		} else if (type instanceof IAIFTypeFloat) {
			return getFloatingPointValueString((IAIFValueFloat) value);
		} else if (type instanceof IAIFTypePointer) {
			return getPointerValueString((IAIFValuePointer) value);
		} else if (type instanceof IAIFTypeReference) {
			return processUnderlyingValue(type, ((IAIFValueReference) value).getParent());
		} else if (type instanceof IAIFTypeString) {
			return getWCharValueString((IAIFValueString) value);
		} else if (type instanceof IAIFTypeAggregate || type instanceof IAIFTypeUnion) {
			return "{...}"; //$NON-NLS-1$
		} else {
			return value.getValueString();
		}
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected List<IPDIVariable> getPDIVariables() throws DebugException {
		IPDIVariable[] vars = null;
		try {
			if (fVariable != null) {
				vars = fVariable.getChildren();
				if (vars == null) {
					vars = new IPDIVariable[0];
				}
			}
		} catch (final PDIException e) {
			requestFailed(e.getMessage(), e);
		}
		return Arrays.asList(vars);
	}

	/**
	 * @return
	 * @throws DebugException
	 */
	protected synchronized List<IVariable> getVariables0() throws DebugException {
		if (!isAllocated() || !hasVariables()) {
			return Collections.emptyList();
		}
		if (fVariables.size() == 0) {
			try {
				final List<IPDIVariable> vars = getPDIVariables();
				fVariables = new ArrayList<IVariable>(vars.size());
				final Iterator<IPDIVariable> it = vars.iterator();
				while (it.hasNext()) {
					fVariables.add(PVariableFactory.createLocalVariable(this, it.next()));
				}
				resetStatus();
			} catch (final DebugException e) {
				setStatus(IPDebugElementStatus.ERROR, e.getMessage());
			}
		}
		return fVariables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#preserve()
	 */
	@Override
	protected void preserve() {
		setChanged(false);
		resetStatus();
		final Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).preserve();
		}
	}

	/**
	 * @param aif
	 * @return
	 * @throws AIFException
	 */
	protected String processUnderlyingValue(IAIF aif) throws AIFException {
		if (aif != null) {
			return processUnderlyingValue(aif.getType(), aif.getValue());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.model.AbstractPValue#reset()
	 */
	@Override
	protected void reset() {
		resetStatus();
		fValueString = null;
		final Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.model.AbstractPValue#setChanged(boolean
	 * )
	 */
	@Override
	protected synchronized void setChanged(boolean changed) {
		if (changed) {
			fValueString = null;
			resetStatus();
		}
		final Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).setChanged(changed);
		}
	}
}
