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

import org.eclipse.ptp.debug.core.aif.IAIFType;

public class AIFTypeArray extends AIFType {
	private IAIFType baseType;
	private int[] range = {0, 0};
	
	public AIFTypeArray(IAIFType base, int[] range) {
		this.baseType = base;
		this.range = range;
	}
	public int getLowIndex() {
		return getLowIndex(1);
	}
	public int getHighIndex() {
		return getHighIndex(1);
	}
	public IAIFType getBaseType() {
		return baseType;
	}
	public int getDimension() {
		return (range.length / 2);
	}
	public int getLowIndex(int dim_pos) {
		if (dim_pos > getDimension())
			return 0;
		return range[(dim_pos * 2) - 2];
	}
	public int getHighIndex(int dim_pos) {
		if (dim_pos > getDimension())
			return 0;
		return range[(dim_pos * 2) - 1];
	}
	public int[] getRange() {
		return range;
	}
	public String toString() {
		String output = "";
		for (int i=0; i<getDimension(); i++) {
			output += "[" + String.valueOf(getLowIndex(i+1)) + ".." + String.valueOf(getHighIndex(i+1)) + "]"; 
		}
		return output += getBaseType().toString();
	}
}
