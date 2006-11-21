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

import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.aif.IAIFTypeStruct;
import org.eclipse.ptp.debug.core.aif.ITypeIntegral;
import org.eclipse.ptp.debug.core.model.IPType;

/**
 * @author Clement chu
 *
 */
public class PType implements IPType {
	private IAIFType fAIFType;

	public PType(IAIFType aifType) {
		setAIFType(aifType);
	}
	public String getName() {
		return (fAIFType != null) ? fAIFType.toString() : null;
	}
	public void dispose() {
		fAIFType = null;
	}
	public int[] getArrayDimensions() {
		int[] dims = new int[0];
		IAIFType type = getAIFType();
		if (type instanceof IAIFTypeArray) {
			dims = new int[((IAIFTypeArray)type).getDimension()];
			for (int i=0; i<dims.length; i++) {
				IAIFTypeArray dim_arrType = ((IAIFTypeArray) type).getAIFTypeArray(i);
				dims[i] = dim_arrType.getRange();
			}
		}
		return dims;
	}
	public boolean isArray() {
		return (getAIFType() instanceof IAIFTypeArray);
	}
	public boolean isCharacter() {
		return (getAIFType() instanceof IAIFTypeChar);
	}
	public boolean isFloatingPointType() {
		return (getAIFType() instanceof IAIFTypeFloat);
	}
	public boolean isPointer() {
		return (getAIFType() instanceof IAIFTypePointer);
	}
	public boolean isReference() {
		return (getAIFType() instanceof IAIFTypeReference);
	}
	public boolean isStructure() {
		return (getAIFType() instanceof IAIFTypeStruct);
	}
	public boolean isSigned() {
		return (isIntegralType()) ? false: ((ITypeIntegral) getAIFType()).isSigned();
	}
	public boolean isIntegralType() {
		return (getAIFType() instanceof ITypeIntegral);
	}
	protected IAIFType getAIFType() {
		return fAIFType;
	}
	protected void setAIFType(IAIFType type) {
		fAIFType = type;
	}
	protected boolean isAggregate() {
		return (isArray() || isStructure() || isPointer() || isReference());
	}
}
