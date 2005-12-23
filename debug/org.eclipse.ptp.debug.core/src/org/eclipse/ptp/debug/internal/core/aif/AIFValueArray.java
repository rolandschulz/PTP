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
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.aif.IAIFTypeRange;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueArray;

/**
 * @author Clement chu
 * 
 */
public class AIFValueArray extends ValueDerived implements IAIFValueArray {
	private Object[] values;
	private int current_dimension_position = 0;
	private int current_position = 0;
	private IAIFValueArray parentArray;

	public AIFValueArray(IAIFValueArray parsentArray, int current_pos) {
		super((IAIFTypeArray)parsentArray.getType());
		this.parentArray = parsentArray;
		current_dimension_position = parentArray.getCurrentDimensionPosition()+1;
		current_position = current_pos;
	}
	public AIFValueArray(IAIFTypeArray type, byte[] data) {
		super(type);
		parse(data);
		parentArray = this;
	}
	public IAIFValueArray getParent() {
		return parentArray;
	}
	public int getChildrenNumber() throws AIFException {
		return getCurrentValues().length;
	}	
	public int getCurrentDimensionPosition() {
		return current_dimension_position;
	}
	public int getCurrentPosition() {
		return current_position;
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = getString();
		}
		return result;
	}
	protected void parse(byte[] data) {
		IAIFTypeArray arrType = (IAIFTypeArray)type;
		ByteBuffer buffer = byteBuffer(data);
		values = parseRange(buffer, 1, arrType.getBaseType(), arrType.getDimension());
	}
	private Object[] parseRange(ByteBuffer dataBuf, int dim_pos, IAIFType baseType, int dimension) {
		IAIFTypeArray arrType = (IAIFTypeArray)type;
		IAIFTypeRange range = arrType.getRange(dim_pos-1);
		int lower = range.getLower();
		int upper = range.getUpper();
		int inner_length = upper-lower+1;
		Object[] innerValues = new Object[inner_length];
		
		for (int j=0; j<inner_length; j++) {
			if (dim_pos < dimension) {
				innerValues[j] = parseRange(dataBuf, dim_pos+1, baseType, dimension);
			}
			else {
				byte[] dst = new byte[baseType.sizeof()];
				for (int h=0; h<dst.length; h++) {
					if (!dataBuf.hasRemaining()) {
						break;
					}
					dst[h] = dataBuf.get();
				}
				innerValues[j] = AIFFactory.getAIFValue(baseType, dst);
				size += ((IAIFValue)innerValues[j]).sizeof(); 
			}
		}
		return innerValues;
	}
	public Object[] getValues() {
		if (values == null)
			return parentArray.getValues();
		return values;
	}
	private String getString() {
		return getString("[", getValues()) + "]";
	}
	public String getString(String content, Object[] objs) {
		for (int i=0; i<objs.length; i++) {
			Object obj = objs[i];
			if (obj instanceof IAIFValue) {
				String tmp = obj.toString();
				if (tmp.length() == 0) {
					content = content.substring(0, content.length()-1);
				} else {
					content += obj.toString();
				}
			} else if (obj instanceof Object[]) {
				content += getString("[", (Object[])obj) + "]";
			}
			if (i < objs.length - 1) {
				content += ",";
			}
		}
		return content;
	}
	public Object[] getCurrentValues() throws AIFException {
		int dimension = ((IAIFTypeArray)type).getDimension();
		if (current_dimension_position > dimension) {
			throw new AIFException("Dimension is out of bound");
		}
		if (current_dimension_position == 0)
			return getValues();
		
		Object[] objs = getParent().getCurrentValues();
		return (Object[])objs[current_position];
	}
	public boolean hasMoreDimension(Object[] objs) {
		if (objs.length == 0)
			return false;
		return (objs[0] instanceof Object[]);
	}
	/*
	public String toString() {
		String output = "[";
		for (int i=0; i<vals.length; i++) {
			if (vals[i] != null) {
				if (i > 0)
					output += ", ";
				output += vals[i].toString();
			}
		}
		return output + "]";
	}
	*/
}
