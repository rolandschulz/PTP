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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.aif.IValueAggregate;

/**
 * @author Clement chu
 * 
 */
public abstract class ValueAggregate extends AIFValue implements IValueAggregate {
	protected List values = new ArrayList();
	
	public ValueAggregate(ITypeAggregate type, byte[] data) {
		super(type);
		parse(data);
	}

	public int getChildrenNumber() throws AIFException {
		return values.size();
	}
	protected void parse(byte[] data) {
		ITypeAggregate typeAggregate = (ITypeAggregate)getType();
		int length = typeAggregate.getNumberOfChildren();
		int from = 0;
		System.out.println("---data len: " + data.length);
		for (int i=0; i<length; i++) {
			IAIFType aifType = typeAggregate.getType(i);
			byte[] newData = createByteArray(data, from, aifType.sizeof());
			IAIFValue val = AIFFactory.getAIFValue(aifType, newData);
			values.add(val);
			from += val.sizeof();
		}
		size = from;
	}
	private byte[] createByteArray(byte[] data, int from, int size) {
		System.out.println("---data len: " + data.length  + ", from: " + from + ", size: " + size);
		byte[] newByte = new byte[size];
		System.arraycopy(data, from, newByte, 0, size);
		return newByte;
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = getString();
		}
		return result;
	}
	private String getString() throws AIFException {
		String content = "{";
		int length = values.size();
		for (int i=0; i<length; i++) {
			IAIFValue value = (IAIFValue)values.get(i);
			content += value.getValueString();
			if (i < length - 1) {
				content += ",";
			}
		}
		return content + "}";
	}
	public IAIFValue getValue(int index) {
		return (IAIFValue)values.get(index);
	}
}
