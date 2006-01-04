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

import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFType;
import org.eclipse.ptp.debug.core.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueUnion;
import org.eclipse.ptp.debug.core.aif.ITypeAggregate;

/**
 * @author Clement chu
 * 
 */
public class AIFValueUnion extends ValueAggregate implements IAIFValueUnion {
	public AIFValueUnion(IAIFTypeUnion type, byte[] data) {
		super(type, data);
	}
	
	protected void parse(byte[] data) {
		size = data.length;
		ITypeAggregate typeAggregate = (ITypeAggregate)getType();
		int length = typeAggregate.getNumberOfChildren();
		for (int i=0; i<length; i++) {
			IAIFType aifType = typeAggregate.getType(i);
			byte[] newData = createByteArray(data, (size - aifType.sizeof()), aifType.sizeof());
			IAIFValue val = AIFFactory.getAIFValue(aifType, newData);
			values.add(val);
		}
	}	
}
