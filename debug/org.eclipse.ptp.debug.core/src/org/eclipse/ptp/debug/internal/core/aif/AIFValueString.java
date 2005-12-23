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
import org.eclipse.ptp.debug.core.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.aif.IAIFValueString;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public class AIFValueString extends ValueIntegral implements IAIFValueString {
	ByteBuffer byteBuffer;
	
	public AIFValueString(IAIFTypeString type, byte[] data) {
		super(type);
		parse(data);
	}
	protected void parse(byte[] data) {
		byteBuffer = byteBuffer(data);
		//TODO -- redo it
		size = data.length;
	}
	public String getValueString() throws PCDIException {
		if (result == null) {
			result = stringValue();
		}
		return result;
	}
	private String stringValue() throws PCDIException {
		return new String(byteBuffer.array());
		/*
		int len = data.get();
		len <<= 8; //2^8
		len += data.get();
		byte[] dst = new byte[len];
		for (int i=0; i<len; i++) {
		    dst[i] = data.get();
		}
		return new String(dst);
		*/
	}
}
