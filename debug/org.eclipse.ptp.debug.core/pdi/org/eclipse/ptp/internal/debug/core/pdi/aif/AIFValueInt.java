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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

public class AIFValueInt extends ValueIntegral implements IAIFValueInt {
	private ByteBuffer byteBuffer;

	public AIFValueInt(IAIFTypeInt type, SimpleByteBuffer buffer) {
		super(type);
		parse(buffer);
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
		byte[] dst = new byte[getType().sizeof()];
		for (int i = 0; i < dst.length; i++) {
			dst[i] = buffer.get();
		}
		byteBuffer = ByteBuffer.wrap(dst, 0, dst.length);
		setSize(getType().sizeof());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		if (isShort()) {
			return String.valueOf(shortValue());
		} else if (isInt()) {
			return String.valueOf(intValue());
		} else if (isLong()) {
			return String.valueOf(longValue());
		} else {
			return new String(byteValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#isLong()
	 */
	public boolean isLong() {
		return (sizeof() == 8);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#isShort()
	 */
	public boolean isShort() {
		return (sizeof() == 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#isInt()
	 */
	public boolean isInt() {
		return (sizeof() == 4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#byteValue()
	 */
	public byte[] byteValue() throws AIFException {
		return byteBuffer.array();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#longValue()
	 */
	public long longValue() throws AIFException {
		try {
			return byteBuffer.getLong();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#shortValue()
	 */
	public short shortValue() throws AIFException {
		try {
			return byteBuffer.getShort();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt#intValue()
	 */
	public int intValue() throws AIFException {
		try {
			return byteBuffer.getInt();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();
		}
	}
}
