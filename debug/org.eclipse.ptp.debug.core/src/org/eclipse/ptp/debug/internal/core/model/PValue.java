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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueChar;
import org.eclipse.ptp.debug.core.aif.IAIFValueFloat;
import org.eclipse.ptp.debug.core.aif.IAIFValueInt;
import org.eclipse.ptp.debug.core.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.aif.IAIFValueString;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.aif.ITypeDerived;
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
	private IPCDIVariable fVariable;

	protected PValue(PVariable parent, IPCDIVariable variable) {
		super(parent);
		fVariable = variable;		
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
			IPStackFrame pframe = getParentVariable().getStackFrame();
			boolean isSuspended = (pframe == null) ? getCDITarget().isSuspended() : pframe.isSuspended();
			if (isSuspended) {
				try {
					if (fVariable == null) {
						targetRequestFailed("No variable found", null);
					}
					fValueString = processUnderlyingValue(fVariable.getType(), fVariable.getValue());
				} catch (PCDIException pe) {
					setStatus(IPDebugElementStatus.ERROR, pe.getMessage());
				} catch (AIFException e) {
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
					fVariables.add(PVariableFactory.createLocalVariable(this, (IPCDIVariable)it.next()));
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
			IPCDIVariable var = getCurrentVariable();
			if (var != null) {
				//return var.getChildrenNumber()>0;
				IAIFType type = var.getType();
				if (type instanceof ITypeAggregate) {
					return true;
				}
				if (type instanceof ITypeDerived) {
					return true;
				}
			}
		} catch (PCDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
		return false;
	}
	public IPCDIVariable getCurrentVariable() {
		return fVariable;
	}
	public IAIFValue getUnderlyingValue() {
		try {
			return getCurrentVariable().getValue();
		} catch (PCDIException e) {
			return null;
		}
	}
	protected List getCDIVariables() throws DebugException {
		IPCDIVariable[] vars = null;
		try {
			IPCDIVariable var = getCurrentVariable();
			vars = var.getChildren();
			if (vars == null) {
				vars = new IPCDIVariable[0];
			}
		} catch (PCDIException e) {
			requestFailed(e.getMessage(), e);
		}
		return Arrays.asList(vars);
	}
	protected synchronized void setChanged(boolean changed) {
		if (changed) {
			fValueString = null;
			resetStatus();
		}
		else {
			//if (getCDITarget().getConfiguration() instanceof IPCDITargetConfiguration2 && ((IPCDITargetConfiguration2)getCDITarget().getConfiguration()).supportsPassiveVariableUpdate())
				//fValueString = null;
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
	protected String processUnderlyingValue(IAIFType aifType, IAIFValue aifValue) throws AIFException {
		if (aifValue != null) {
			if (aifType instanceof IAIFTypeChar)
				return getCharValueString((IAIFValueChar) aifValue);
			else if (aifType instanceof IAIFTypeInt)
				return getIntValueString((IAIFValueInt) aifValue);
			else if (aifType instanceof IAIFTypeFloat)
				return getFloatingPointValueString((IAIFValueFloat) aifValue);
			else if (aifType instanceof IAIFTypePointer)
				return getPointerValueString((IAIFValuePointer) aifValue);
			else if (aifType instanceof IAIFTypeReference)
				return processUnderlyingValue(aifType, ((IAIFValueReference) aifValue).getParent());
			else if (aifType instanceof IAIFTypeString)
				return getWCharValueString((IAIFValueString) aifValue);
			else if (aifType instanceof ITypeAggregate)
				return "{...}";
			else
				return aifValue.getValueString();
		}
		return null;
	}
	private String getCharValueString(IAIFValueChar value) throws AIFException {
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
	private String getIntValueString(IAIFValueInt value) throws AIFException {
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
	private String getFloatingPointValueString(IAIFValueFloat value) throws AIFException {
		if (value.isDouble()) {
			return getDoubleValueString(value.getValueString());
		} else if (value.isFloat()) {
			return getFloatValueString(value.getValueString());
		} else {
			return value.getValueString();
		}
	}
	private String getFloatValueString(String floatValue) throws AIFException {
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
	private String getDoubleValueString(String doubleValue) throws AIFException {
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
	private String getPointerValueString(IAIFValuePointer value) throws AIFException {
		//IAIFValue baseValue = value.getValue();
		//if (baseValue instanceof IValueAggregate) {//if base type is not primitive type display address;
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
		//}
		//else {
			//return baseValue.getValueString();
		//}
		return null;
	}
	private String getWCharValueString(IAIFValueString value) throws AIFException {
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
		IAIFValue aifValue = getUnderlyingValue();
		if (fType == null) {
			if (aifValue != null) {
				synchronized (this) {
					if (fType == null) {
						fType = new PType(aifValue.getType());
					}
				}
			}
		}
		return fType;
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
	private String getLongValueString(IAIFValueLong value) throws PCDIException {
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
	private String getLongLongValueString(IAIFValueLongLong value) throws PCDIException {
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
	private String getShortValueString(IAIFValueShort value) throws PCDIException {
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
