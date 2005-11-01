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
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.ICType;

public class CType implements ICType {
	private ICDIType fCDIType;

	public CType(ICDIType cdiType) {
		setCDIType(cdiType);
	}
	public String getName() {
		return (fCDIType != null) ? fCDIType.getTypeName() : null;
	}
	public void dispose() {
		fCDIType = null;
	}
	public int[] getArrayDimensions() {
		int length = 0;
		ICDIType type = getCDIType();
		while (type instanceof ICDIArrayType) {
			++length;
			type = (type instanceof ICDIDerivedType) ? ((ICDIDerivedType) type).getComponentType() : null;
		}
		int[] dims = new int[length];
		type = getCDIType();
		for (int i = 0; i < length; i++) {
			dims[i] = ((ICDIArrayType) type).getDimension();
			type = ((ICDIDerivedType) type).getComponentType();
		}
		return dims;
	}
	public boolean isArray() {
		return (getCDIType() instanceof ICDIArrayType);
	}
	public boolean isCharacter() {
		return (getCDIType() instanceof ICDICharType);
	}
	public boolean isFloatingPointType() {
		return (getCDIType() instanceof ICDIFloatingPointType);
	}
	public boolean isPointer() {
		return (getCDIType() instanceof ICDIPointerType);
	}
	public boolean isReference() {
		return (getCDIType() instanceof ICDIReferenceType);
	}
	public boolean isStructure() {
		return (getCDIType() instanceof ICDIStructType);
	}
	public boolean isUnsigned() {
		return (isIntegralType()) ? ((ICDIIntegralType) getCDIType()).isUnsigned() : false;
	}
	public boolean isIntegralType() {
		return (getCDIType() instanceof ICDIIntegralType);
	}
	protected ICDIType getCDIType() {
		return fCDIType;
	}
	protected void setCDIType(ICDIType type) {
		fCDIType = type;
	}
	protected boolean isAggregate() {
		return (isArray() || isStructure() || isPointer() || isReference());
	}
}
