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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeAggregate;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueAggregate;

/**
 * @author Clement chu
 * 
 */
public abstract class AIFValueAggregate extends AIFValue implements IAIFValueAggregate {
	protected List values = new ArrayList();
	private int bufferLength = 0;
	
	public AIFValueAggregate(IAIFTypeAggregate type, byte[] data) {
		super(type, data);
		parse();
	}
	public int getBufferLength() {
		return bufferLength;
	}	
	
	protected void parse() {
		IAIFTypeAggregate typeAggregate = (IAIFTypeAggregate)getType();
		int length = typeAggregate.getNumberOfChildren();
		System.out.println("-- data len: " + data.length);
		ByteBuffer buffer = byteBuffer();
		for (int i=0; i<length; i++) {
			IAIFType aifType = typeAggregate.getType(i);
			//buffer.
			IAIFValue aifValue = AIFFactory.getAIFValue(aifType, byteBuffer(bufferLength).array());
			bufferLength += aifValue.getBufferLength();
			System.out.println("-- aifValue: " + aifValue.toString() + ", buffer len: " + bufferLength);
			//aifType
			//AIFFactory.getAIFValue();
			//values.add(AIFFactory.)
		}
	}
}

