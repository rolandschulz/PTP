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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueFloat extends ValueParent implements IAIFValueFloat {
	private ByteBuffer byteBuffer;

	public AIFValueFloat(IAIFTypeFloat type, SimpleByteBuffer buffer) {
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
		if (isFloat()) {
			return String.valueOf(floatValue());
		} else if (isDouble()) {
			return String.valueOf(doubleValue());
		} else {
			return new String(byteBuffer.array());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat#floatValue()
	 */
	public float floatValue() throws AIFException {
		try {
			return byteBuffer.getFloat();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat#doubleValue()
	 */
	public double doubleValue() throws AIFException {
		try {
			return byteBuffer.getDouble();
		} catch (BufferUnderflowException e) {
			return 0;
		} finally {
			byteBuffer.rewind();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat#isDouble()
	 */
	public boolean isDouble() {
		return (getType().sizeof() == 8);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat#isFloat()
	 */
	public boolean isFloat() {
		return (getType().sizeof() == 4);
	}
}
