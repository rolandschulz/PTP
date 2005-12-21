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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueChar;
import org.eclipse.ptp.debug.core.aif.IAIFValueFloatingPoint;
import org.eclipse.ptp.debug.core.aif.IAIFValueInt;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.aif.IAIFValueWChar;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIVariable;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.PVariableFormat;

/**
 * @author Clement chu
 *
 */
public class PValue extends AbstractPValue {
	private String fValueString = null;
	private List fVariables = Collections.EMPTY_LIST;
	private PType fType;

	protected PValue(PVariable parent) {
		super(parent);
	}
	protected PValue(PVariable parent, String message) {
		super(parent);
		setStatus(IPDebugElementStatus.ERROR, message);
	}
	public String getReferenceTypeName() throws DebugException {
		return (getParentVariable() != null) ? getParentVariable().getReferenceTypeName() : null;
	}
	public String getValueString() throws DebugException {
		if (fValueString == null && getUnderlyingValue() != null) {
			resetStatus();
			IPStackFrame cframe = getParentVariable().getStackFrame();
			boolean isSuspended = (cframe == null) ? getCDITarget().isSuspended() : cframe.isSuspended();
			if (isSuspended) {
				try {
					fValueString = processUnderlyingValue(getUnderlyingValue());
				} catch (CDIException e) {
					setStatus(IPDebugElementStatus.ERROR, e.getMessage());
				}
			}
		}
		return fValueString;
	}
	public boolean isAllocated() throws DebugException {
		return true;
	}
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[]) list.toArray(new IVariable[list.size()]);
	}
	protected synchronized List getVariables0() throws DebugException {
		if (!isAllocated() || !hasVariables())
			return Collections.EMPTY_LIST;
		if (fVariables.size() == 0) {
			try {
				List vars = getCDIVariables();
				fVariables = new ArrayList(vars.size());
				Iterator it = vars.iterator();
				while (it.hasNext()) {
					fVariables.add(PVariableFactory.createLocalVariable(this, (IPCDIVariable) it.next()));
				}
				resetStatus();
			} catch (DebugException e) {
				setStatus(IPDebugElementStatus.ERROR, e.getMessage());
			}
		}
		return fVariables;
	}
	public boolean hasVariables() throws DebugException {
		try {
			IAIFValue value = getUnderlyingValue();
			if (value != null)
				return value.getChildrenNumber() > 0;
		} catch (CDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return false;
	}
	public IAIFValue getUnderlyingValue() throws DebugException {
		return getAIF().getValue();
	}
	protected List getCDIVariables() throws DebugException {
		IPCDIVariable[] vars = new IPCDIVariable[0];
		try {
			vars = getParentVariable().getCDIVariable().getVariables();
		} catch (CDIException e) {
			requestFailed(e.getMessage(), e);
		}
		return Arrays.asList(vars);
	}
	protected synchronized void setChanged(boolean changed) {
		if (changed) {
			fValueString = null;
			resetStatus();
		}
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).setChanged(changed);
		}
	}
	public void dispose() {
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).dispose();
		}
	}
	private String processUnderlyingValue(IAIFValue aifValue) throws CDIException {
		if (aifValue != null) {
			if (aifValue instanceof IAIFValueChar)
				return getCharValueString((IAIFValueChar) aifValue);
			else if (aifValue instanceof IAIFValueInt)
				return getIntValueString((IAIFValueInt) aifValue);
			else if (aifValue instanceof IAIFValueFloatingPoint)
				return getFloatingPointValueString((IAIFValueFloatingPoint) aifValue);
			else if (aifValue instanceof IAIFValuePointer)
				return getPointerValueString((IAIFValuePointer) aifValue);
			else if (aifValue instanceof IAIFValueReference)
				return processUnderlyingValue(((IAIFValueReference) aifValue).referenceValue());
			else if (aifValue instanceof IAIFValueWChar)
				return getWCharValueString((IAIFValueWChar) aifValue);
			else
				return aifValue.getValueString();
		}
		return null;
	}
	private String getCharValueString(IAIFValueChar value) throws PCDIException {
		PVariableFormat format = getParentVariable().getFormat();
		char charValue = value.charValue();
		if (PVariableFormat.NATURAL.equals(format)) {
			return ((Character.isISOControl(charValue) && charValue != '\b' && charValue != '\t' && charValue != '\n' && charValue != '\f' && charValue != '\r') || charValue < 0) ? "" : "\'" + value.getValueString() + "\'";
		} else if (PVariableFormat.DECIMAL.equals(format)) {
			return Integer.toString((byte)charValue);
			//return (isSigned()) ? Integer.toString(byteValue): Integer.toString(value.shortValue());
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Integer.toString((byte)charValue);
			//String stringValue = (isSigned()) ? Integer.toHexString((byte) value.byteValue()) : Integer.toHexString(value.shortValue());
			sb.append((stringValue.length() > 2) ? stringValue.substring(stringValue.length() - 2) : stringValue);
			return sb.toString();
		}
		return null;
	}
	private String getIntValueString(IAIFValueInt value) throws CDIException {
		PVariableFormat format = getParentVariable().getFormat();
		String stringValue = value.getValueString();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
			return stringValue;
			//return (isSigned()) ? Integer.toString(value.intValue()) : Long.toString(value.longValue());
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			if (value.isShort()) {
				stringValue = Integer.toHexString(value.shortValue());
			} else if (value.isInt()) {
				stringValue = Integer.toHexString(value.intValue());
			} else if (value.isLong()) {
				stringValue = Long.toHexString(value.longValue());
			}
			//String stringValue = (isSigned()) ? Integer.toHexString(value.intValue()) : Long.toHexString(value.longValue());
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return null;
	}
	private String getFloatingPointValueString(IAIFValueFloatingPoint value) throws CDIException {
		if (value.isDouble()) {
			return getDoubleValueString(value.getValueString());
		} else if (value.isFloat()) {
			return getFloatValueString(value.getValueString());
		} else {
			return value.getValueString();
		}
	}
	private String getFloatValueString(String floatValue) throws CDIException {
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return floatValue;
		}

		Float flt = new Float(floatValue);
		if (flt.isNaN() || flt.isInfinite())
			return "";
		long longValue = flt.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return floatValue;
	}
	private String getDoubleValueString(String doubleValue) throws CDIException {
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return doubleValue;
		}

		Double dbl = new Double(doubleValue);
		if (dbl.isNaN() || dbl.isInfinite())
			return "";
		long longValue = dbl.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 16) ? stringValue.substring(stringValue.length() - 16) : stringValue);
			return sb.toString();
		}
		return doubleValue;
	}
	private String getPointerValueString(IAIFValuePointer value) throws CDIException {
		// TODO:IPF_TODO Workaround to solve incorrect handling of structures referenced by pointers or references
		IAddressFactory factory = ((PDebugTarget) getDebugTarget()).getAddressFactory();
		BigInteger pv = value.pointerValue();
		if (pv == null)
			return "";
		IAddress address = factory.createAddress(pv);
		if (address == null)
			return "";
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.HEXADECIMAL.equals(format))
			return address.toHexAddressString();
		if (PVariableFormat.DECIMAL.equals(format))
			return address.toString();
		return null;
	}
	private String getWCharValueString(IAIFValueWChar value) throws CDIException {
		return value.getValueString();
		/*
		if (getParentVariable() instanceof PVariable) {
			PVariableFormat format = getParentVariable().getFormat();
			if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
				return (isSigned()) ? Short.toString(value.shortValue()) : Integer.toString(value.intValue());
			} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
				StringBuffer sb = new StringBuffer("0x");
				String stringValue = Integer.toHexString((isSigned()) ? value.shortValue() : value.intValue());
				sb.append((stringValue.length() > 4) ? stringValue.substring(stringValue.length() - 4) : stringValue);
				return sb.toString();
			}
		}
		int size = ((PVariable) getParentVariable()).sizeof();
		if (size == 4) {
			PVariableFormat format = getParentVariable().getFormat();
			if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
				return (isSigned()) ? Integer.toString(value.intValue()) : Long.toString(value.longValue());
			} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
				StringBuffer sb = new StringBuffer("0x");
				String stringValue = (isSigned()) ? Integer.toHexString(value.intValue()) : Long.toHexString(value.longValue());
				sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
				return sb.toString();
			}
		}
		*/
	}
	/*
	private boolean isSigned() {
		boolean result = false;
		try {
			IPType type = getParentVariable().getType();
			if (type != null)
				result = type.isSigned();
		} catch (DebugException e) {
		}
		return result;
	}
	*/
	protected void reset() {
		resetStatus();
		fValueString = null;
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).resetValue();
		}
	}
	public IPType getType() throws DebugException {
		IAIFValue cdiValue = getUnderlyingValue();
		if (fType == null) {
			if (cdiValue != null) {
				synchronized (this) {
					if (fType == null) {
						fType = new PType(cdiValue.getType());
					}
				}
			}
		}
		return fType;
		// AbstractCVariable var = getParentVariable();
		// return ( var instanceof CVariable ) ? ((CVariable)var).getType() : null;
	}
	protected void preserve() {
		setChanged(false);
		resetStatus();
		Iterator it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).preserve();
		}
	}
}

/*
	private String getLongValueString(IAIFValueLong value) throws CDIException {
		try {
			PVariableFormat format = getParentVariable().getFormat();
			if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
				if (!isSigned()) {
					BigInteger bigValue = new BigInteger(value.getValueString());
					return bigValue.toString();
				}
				return Long.toString(value.longValue());
			} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
				StringBuffer sb = new StringBuffer("0x");
				if (isSigned()) {
					sb.append(Long.toHexString(value.longValue()));
				} else {
					BigInteger bigValue = new BigInteger(value.getValueString());
					sb.append(bigValue.toString(16));
				}
				return sb.toString();
			}
		} catch (NumberFormatException e) {
		}
		return null;
	}
	private String getLongLongValueString(IAIFValueLongLong value) throws CDIException {
		try {
			PVariableFormat format = getParentVariable().getFormat();
			if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
				if (!isSigned()) {
					BigInteger bigValue = new BigInteger(value.getValueString());
					return bigValue.toString();
				}
				return Long.toString(value.longValue());
			} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
				StringBuffer sb = new StringBuffer("0x");
				if (isSigned()) {
					sb.append(Long.toHexString(value.longValue()));
				} else {
					BigInteger bigValue = new BigInteger(value.getValueString());
					sb.append(bigValue.toString(16));
				}
				return sb.toString();
			}
		} catch (NumberFormatException e) {
		}
		return null;
	}
	private String getShortValueString(IAIFValueShort value) throws CDIException {
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
			return (isSigned()) ? Short.toString(value.shortValue()) : Integer.toString(value.intValue());
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Integer.toHexString((isSigned()) ? value.shortValue() : value.intValue());
			sb.append((stringValue.length() > 4) ? stringValue.substring(stringValue.length() - 4) : stringValue);
			return sb.toString();
		}
		return null;
	}
*/
