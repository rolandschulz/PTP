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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class Memory implements IPDIMemory {
	private final String addr;
	private long[] data = new long[0];
	private final List<Integer> badOffsets = new ArrayList<Integer>();
	private String ascii = ""; //$NON-NLS-1$

	public Memory(String addr, String ascii, String[] values) {
		this.addr = addr;
		this.ascii = ascii;
		parse(values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIMemory#getAddress()
	 */
	public String getAddress() {
		return addr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIMemory#getAscii()
	 */
	public String getAscii() {
		return ascii;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIMemory#getBadOffsets()
	 */
	public int[] getBadOffsets() {
		int[] data = new int[badOffsets.size()];
		for (int i = 0; i < data.length; ++i) {
			Integer o = badOffsets.get(i);
			data[i] = o.intValue();
		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIMemory#getData()
	 */
	public long[] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIMemory#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Messages.Memory_1 + addr + "\""); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
		buffer.append(Messages.Memory_3);
		for (int i = 0; i < data.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append('"').append(Long.toHexString(data[i])).append('"');
		}
		buffer.append(']');
		if (ascii.length() > 0) {
			buffer.append(Messages.Memory_4 + ascii + "\""); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
		}
		return buffer.toString();
	}

	/**
	 * @param values
	 */
	private void parse(String[] values) {
		data = new long[values.length];
		for (int i = 0; i < values.length; i++) {
			try {
				data[i] = Long.decode(values[i].trim()).longValue();
			} catch (NumberFormatException e) {
				badOffsets.add(new Integer(i));
				data[i] = 0;
			}
		}
	}
}
