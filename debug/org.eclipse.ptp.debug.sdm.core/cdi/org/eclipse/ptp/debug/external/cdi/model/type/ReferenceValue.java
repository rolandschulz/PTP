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
package org.eclipse.ptp.debug.external.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;

/**
 * @author Clement chu
 * 
 */
public class ReferenceValue extends DerivedValue implements ICDIReferenceValue {
	public ReferenceValue(Variable v) {
		super(v);
	}
	public ICDIValue referenceValue() throws CDIException {
		Value value = null;
		ICDIReferenceType rt = (ICDIReferenceType)getType();
		ICDIType t = rt.getComponentType();
		if (t instanceof ICDIBoolType) {
			value = new BoolValue(getVariable());
		} else if (t instanceof ICDICharType) {
			value = new CharValue(getVariable());
		} else if (t instanceof ICDIWCharType) {
			value = new WCharValue(getVariable());
		} else if (t instanceof ICDIShortType) {
			value = new ShortValue(getVariable());
		} else if (t instanceof ICDIIntType) {
			value = new IntValue(getVariable());
		} else if (t instanceof ICDILongType) {
			value = new LongValue(getVariable());
		} else if (t instanceof ICDILongLongType) {
			value = new LongLongValue(getVariable());
		} else if (t instanceof ICDIEnumType) {
			value = new EnumValue(getVariable());
		} else if (t instanceof ICDIFloatType) {
			value = new FloatValue(getVariable());
		} else if (t instanceof ICDIDoubleType) {
			value = new DoubleValue(getVariable());
		} else if (t instanceof ICDIFunctionType) {
			value = new FunctionValue(getVariable());
		} else if (t instanceof ICDIPointerType) {
			value = new PointerValue(getVariable());
//		} else if (t instanceof ICDIReferenceType) {
//			value = new ReferenceValue(getVariable());
		} else if (t instanceof ICDIArrayType) {
			value = new ArrayValue(getVariable());
		} else if (t instanceof ICDIStructType) {
			value = new StructValue(getVariable());
		} else {
			value = new Value(getVariable());
		}
		return value;		
	}
}
