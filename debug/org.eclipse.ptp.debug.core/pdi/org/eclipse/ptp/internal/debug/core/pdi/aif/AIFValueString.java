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
package org.eclipse.ptp.internal.debug.core.pdi.aif;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueString;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueString extends AIFValue implements IAIFValueString {
	public AIFValueString(IAIFTypeString type, SimpleByteBuffer buffer) {
		super(type);
		parse(buffer);
		((AIFTypeString) type).setSizeof(sizeof());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValue#parse(org.eclipse
	 * .ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer)
	 */
	@Override
	protected void parse(SimpleByteBuffer buffer) {
		setSize(getSize(buffer));
		byte[] bytes = new byte[sizeof()];
		for (int i = 0; i < sizeof(); i++) {
			bytes[i] = buffer.get();
		}
		setResult(new String(bytes));
	}

	/**
	 * @param buffer
	 * @return
	 */
	public int getSize(SimpleByteBuffer buffer) {
		String hex = ""; //$NON-NLS-1$
		for (int i = 0; i < 2; i++) {
			hex += Integer.toHexString(0x0100 + (buffer.get() & 0x00FF)).substring(1);
		}
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		if (getResult() == null) {
			return ""; //$NON-NLS-1$
		}
		return getResult();
	}

	public static void main(String[] args) {
		int length = 30;
		byte[] bytes = new byte[2];

		bytes[0] = (byte) ((length >> 8) & 0xff);
		bytes[1] = (byte) (length & 0xff);

		System.err.println("---- bytes: " + bytes); //$NON-NLS-1$

		String hex = ""; //$NON-NLS-1$
		for (int i = 0; i < 2; i++) {
			hex += Integer.toHexString(0x0100 + (bytes[i] & 0x00FF));
			System.err.println("hex: " + hex); //$NON-NLS-1$
		}

		int test = 0;
		try {
			test = Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			test = -1;
		}
		System.out.println("---- test: " + test); //$NON-NLS-1$
	}
	/*
	 * int len = data.get(); len <<= 8; //2^8 len += data.get(); byte[] dst =
	 * new byte[len]; for (int i=0; i<len; i++) { dst[i] = data.get(); } return
	 * new String(dst);
	 */
}
