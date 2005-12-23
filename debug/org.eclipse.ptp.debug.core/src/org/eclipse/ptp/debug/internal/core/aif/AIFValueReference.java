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
package org.eclipse.ptp.debug.internal.core.aif;

import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueReference;

/**
 * @author Clement chu
 * 
 */
public class AIFValueReference extends ValueDerived implements IAIFValueReference {
	public AIFValueReference(IAIFTypeReference type, byte[] data) {
		super(type);
		parse(data);
	}
	protected void parse(byte[] data) {
		size = data.length;
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = String.valueOf("");
		}
		return null;
	}
	public IAIFValue referenceValue() throws AIFException {
		IAIFValue value = null;
		/*
		IAIFTypeReference rt = (IAIFTypeReference)getType();
		IAIFType t = rt.getComponentType();
		if (t instanceof IAIFTypeBool) {
			value = new AIFValueBool(t);
		} else if (t instanceof IAIFTypeChar) {
			value = new AIFValueChar(t);
		} else if (t instanceof IAIFTypeWChar) {
			value = new AIFValueWChar(t);
		} else if (t instanceof IAIFTypeShort) {
			value = new AIFValueShort(t);
		} else if (t instanceof IAIFTypeInt) {
			value = new AIFValueInt(t);
		} else if (t instanceof IAIFTypeLong) {
			value = new AIFValueLong(t);
		} else if (t instanceof IAIFTypeLongLong) {
			value = new AIFValueLongLong(t);
		} else if (t instanceof IAIFTypeEnum) {
			value = new AIFValueEnum(t);
		} else if (t instanceof IAIFTypeFloat) {
			value = new AIFValueFloat(t);
		} else if (t instanceof IAIFTypeDouble) {
			value = new AIFValueDouble(t);
		} else if (t instanceof IAIFTypeFunction) {
			value = new AIFValueFunction(t);
		} else if (t instanceof IAIFTypePointer) {
			value = new AIFValuePointer(t);
//		} else if (t instanceof ICDIReferenceType) {
//			value = new ReferenceValue(getVariable());
		} else if (t instanceof IAIFTypeArray) {
			value = new AIFValueArray(t);			
		} else if (t instanceof IAIFTypeStruct) {
			value = new AIFValueStruct(t);
		} else {
			value = new AIFValueUnknown(t);
		}
		*/
		return value;		
	}
}
