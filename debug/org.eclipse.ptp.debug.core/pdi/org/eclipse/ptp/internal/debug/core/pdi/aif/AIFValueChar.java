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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueChar;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

public class AIFValueChar extends ValueIntegral implements IAIFValueChar {
	private byte byteValue;

	public AIFValueChar(IAIFTypeChar type, SimpleByteBuffer buffer) {
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
		byteValue = buffer.get();
		setSize(getType().sizeof());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		char c = charValue();
		if (c > 0x20 && c < 0x7f) {
			return "'" + String.valueOf(c) + "'"; //$NON-NLS-1$//$NON-NLS-2$
		}
		return Integer.toString(c & (1 << 8 * sizeof()) - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueChar#charValue()
	 */
	public char charValue() throws AIFException {
		return (char) byteValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueChar#byteValue()
	 */
	public byte byteValue() throws AIFException {
		return byteValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValue#toString()
	 */
	@Override
	public String toString() {
		try {
			return toPrintable(charValue());
		} catch (AIFException e) {
			return Messages.AIFValueChar_1 + e.getMessage();
		}
	}

	private String toPrintable(char c) {
		if (c < 0x20) {
			switch (c) {
			case '\b':
				return "\\b"; //$NON-NLS-1$
			case '\f':
				return "\\f"; //$NON-NLS-1$
			case '\n':
				return "\\n"; //$NON-NLS-1$
			case '\r':
				return "\\r"; //$NON-NLS-1$
			case '\t':
				return "\\t"; //$NON-NLS-1$
			default:
				return ((c > 0xf) ? "\\u00" : "\\u000") + Integer.toHexString(c); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (c > 0xfff) {
			return "\\u" + Integer.toHexString(c); //$NON-NLS-1$
		}
		if (c > 0xff) {
			return "\\u0" + Integer.toHexString(c); //$NON-NLS-1$
		}
		if (c > 0x7e) {
			return "\\u00" + Integer.toHexString(c); //$NON-NLS-1$
		}
		return String.valueOf(c);
	}
}
