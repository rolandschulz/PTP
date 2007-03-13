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
package org.eclipse.ptp.debug.external.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Clement chu
 * 
 */
public class Memory {
	String addr;
	long [] data = new long[0];
	List<Integer> badOffsets = new ArrayList<Integer>();
	String ascii = "";
	
	/** Constructor
	 * @param addr
	 * @param ascii
	 * @param values
	 */
	public Memory(String addr, String ascii, String[] values) {
		this.addr = addr;
		this.ascii = ascii;
		parse(values);
	}
	/** Convert values into long array 
	 * @param values
	 */
	private void parse(String[] values) {
		data = new long[values.length];
		for (int i=0; i<values.length; i++) {
			try {
				data[i] = Long.decode(values[i].trim()).longValue();
			} catch (NumberFormatException e) {
				badOffsets.add(new Integer(i));
				data[i] = 0;
			}
		}
	}
	
	/** Get address
	 * @return
	 */
	public String getAddress() {
		return addr;
	}
	/** Get long array of data
	 * @return
	 */
	public long [] getData() {
		return data;
	}
	/** Get int array of bad offsets
	 * @return
	 */
	public int[] getBadOffsets() {
		int[] data = new int[badOffsets.size()];
		for (int i = 0; i < data.length; ++i) {
			Integer o = (Integer)badOffsets.get(i);
			data[i] = o.intValue();
		}
		return data;
	}
	/** Get ascii
	 * @return
	 */
	public String getAscii() {
		return ascii;
	}
	
	/** Get string format
	 * @return
	 */
	public String toSting() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("addr=\"" + addr + "\"");
		buffer.append("data=[");
		for (int i = 0 ; i < data.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append('"').append(Long.toHexString(data[i])).append('"');
		}
		buffer.append(']');
		if (ascii.length() > 0) {
			buffer.append(",ascii=\"" + ascii + "\"");
		}
		return buffer.toString();
	}	
}

