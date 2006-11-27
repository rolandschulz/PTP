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

import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeArray;

public class AIFTypeArray extends TypeDerived implements IAIFTypeArray {
	private int size = 1;
	private int low = 0;
	private int high = 0;
	private int dimension = 0;

	//char[]: [r0..3is4]c
	//2D[3][2]: [r0..1is4][r0..2is4]is4
	//2D[2][3]: [r0..2is4][r0..1is4]is4
	//int[]: [r0..9is4]is4
	//char[]: [r0..4is4]c
	//separate into two parts:  1-[r0..1is4], 2-[r0..2is4]is4
	public AIFTypeArray(String format, IAIFType basetype) {
		super(basetype);
		parse(format);
	}
	private void parse(String fmt) {
		int low_pos = AIFFactory.getDigitPos(fmt, 1);
		low = Integer.parseInt(fmt.substring(1, low_pos));
		int up_pos = AIFFactory.getDigitPos(fmt, low_pos+2);
		high = Integer.parseInt(fmt.substring(low_pos+2, up_pos));
		size = size * (high-low+1) * basetype.sizeof();
	}
	public int getLow() {
		return low;
	}
	public int getHigh() {
		return high;
	}
	public int getRange() {
		return (high-low) + 1;
	}
	public IAIFType getFoundationType() {
		if (basetype instanceof IAIFTypeArray) {
			return ((IAIFTypeArray)basetype).getFoundationType();
		}
		return basetype;
	}
	public int getDimension() {
		if (dimension == 0) {
			dimension = 1;
			if (basetype instanceof IAIFTypeArray) {
				dimension += ((IAIFTypeArray)basetype).getDimension();
			}
		}
		return dimension;
	}
	public boolean isDimensionArray() {
		return (basetype instanceof IAIFTypeArray);
	}
	public IAIFTypeArray getAIFTypeArray(int dim_pos) {
		if (dim_pos == 0)
			return this;

		if (dim_pos < 0 || dim_pos > getDimension()-1)
			return null;

		return ((IAIFTypeArray)basetype).getAIFTypeArray(dim_pos-1);
	}
	public int sizeof() {
		return size;
	}
	public String toString(int range) {
		return getAIFTypeArray(range).toString();
	}
	public String toString() {
		return "[r" + low + ".." + high + "U]" + basetype.toString();
	}
	public static void main(String[] args) {
		String fmt = "[r0..2is4][r0..5is4][r0..5is4][r0..5is4][r0..5is4]is4";
		IAIFType type = AIFFactory.getAIFType(fmt);
		if (type instanceof IAIFTypeArray) {
			System.err.println("dim: " + ((IAIFTypeArray)type).getDimension());
			System.err.println("base: " + ((IAIFTypeArray)type).getBaseType());
			System.err.println("foundation: " + ((IAIFTypeArray)type).getFoundationType());
		}
		System.err.println("size: " + type.sizeof());
		System.err.println("1st: " + ((IAIFTypeArray)type).getAIFTypeArray(0));
	}
	
	/*
	public IAIFTypeRange[] getRanges() {
		return (IAIFTypeRange[])ranges.toArray(new IAIFTypeRange[0]);
	}
	public IAIFTypeRange getRange(int index) {
		return (IAIFTypeRange)ranges.get(index);
	}
	public int getLower(int index) {
		return getRange(index).getLower();
	}
	public int getUpper(int index) {
		return getRange(index).getUpper();
	}
	public IAIFType getInternalType(int index) {
		return getRange(index).getInternalType();
	}	
	private void parse(String fmt) {
		while (fmt.length() > 0) {
			fmt = parseRange(fmt);
		}
		size = size * basetype.sizeof();
	}
	protected String parseRange(String fmt) {
		int pos = fmt.indexOf(AIFFactory.SIGN_CLOSE);
		ranges.add(getRange(fmt.substring(1, pos)));
		return fmt.substring(pos+1);
	}
	//range: rL..UT
	protected IAIFTypeRange getRange(String fmt) {
		int low_pos = AIFFactory.getDigitPos(fmt, 1);
		int lower = Integer.parseInt(fmt.substring(1, low_pos));
		int up_pos = AIFFactory.getDigitPos(fmt, low_pos+2);
		int upper = Integer.parseInt(fmt.substring(low_pos+2, up_pos));
		size = size * (upper-lower+1);
		//AIFFactory.getAIFType(fmt.substring(up_pos))
		return new AIFTypeRange(lower, upper, null);
	}
	public String toString(int dimension) {
		IAIFTypeRange range = (IAIFTypeRange)ranges.get(dimension);
		//range.getInternalType().toString()
		return "[r" + range.getLower() + ".." + range.getUpper() + "U" +  "]";		
	}
	public String toString() {
		String content = "";
		for (int i=0; i<ranges.size(); i++) {
			content += toString(i);
		}
		content += getBaseType().toString();
		return content;
	}
	
	class AIFTypeRange implements IAIFTypeRange {
		int lower;
		int upper;
		//TODO -- not important for that
		IAIFType interalType;
		
		AIFTypeRange(int lower, int upper, IAIFType interalType) {
			this.lower = lower;
			this.upper = upper;
			this.interalType = interalType;
		}
		public int getLower() {
			return lower;
		}
		public int getUpper() {
			return upper;
		}
		public IAIFType getInternalType() {
			return interalType;
		}
	}
	*/
}
