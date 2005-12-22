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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public abstract class AIFValue implements IAIFValue {
	protected IAIFType type = null;
	protected String result = null;
	protected int size;
	
	public AIFValue(IAIFType type) {
		this.type = type;
	}
	public IAIFType getType() {
		return type;
	}
	public int getChildrenNumber() throws PCDIException {
		return 0;
	}
	public boolean hasChildren() throws PCDIException {
		return (getChildrenNumber() > 0);
	}
	protected ByteBuffer byteBuffer(byte[] data) {
		return byteBuffer(data, 0);
	}
	protected ByteBuffer byteBuffer(byte[] data, int offset) {
		ByteBuffer bBuffer = ByteBuffer.wrap(data, offset, data.length);
		bBuffer.order(ByteOrder.nativeOrder());
		return bBuffer;
	}
	public String toString() {
		try {
			return getValueString();
		} catch (PCDIException e) {
			return "err: " + e.getMessage();
		}
	}
	
	protected abstract void parse(byte[] data);
	public int sizeof() {
		return size;
	}
}
