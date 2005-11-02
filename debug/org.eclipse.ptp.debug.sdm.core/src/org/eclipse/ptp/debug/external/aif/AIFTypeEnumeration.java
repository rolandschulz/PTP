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

package org.eclipse.ptp.debug.external.aif;

public class AIFTypeEnumeration extends AIFType {
	private int		id = -1;
	private String[]	names;
	private int[]	values;
	
	public AIFTypeEnumeration(String[] names, int[] values) {
		//TODO if (names.length != values.length)
		//	throw new AIFValueException;
		
		this.names = names;
		this.values = values;
	}

	public AIFTypeEnumeration(int id, String[] names, int[] values) {
		this(names, values);
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public String toString() {
		String res = "<";
		
		if (this.id >= 0)
			res += Integer.toString(this.id) + "|";
			
		for (int i = 0; i < this.names.length; i++) {
			if (i > 0)
				res += ",";
			res += this.names[i] + "=" + this.values[i];
		}

		return res + ">";
	}
}
