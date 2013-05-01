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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueArray;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueArray extends ValueDerived implements IAIFValueArray {
	private IAIFValue[] values = new IAIFValue[0];
	private SimpleByteBuffer buffer = null;

	public AIFValueArray(IAIFTypeArray type, SimpleByteBuffer buffer) {
		super(type);
		this.buffer = buffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueArray#getValues()
	 */
	public IAIFValue[] getValues() {
		if (values.length == 0 && buffer != null) {
			parse(buffer);
			buffer = null;
		}
		return values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		String content = "["; //$NON-NLS-1$
		IAIFValue[] aifValues = getValues();
		for (int i = 0; i < aifValues.length; i++) {
			content += aifValues[i].getValueString();
			if (i < aifValues.length - 1) {
				content += ","; //$NON-NLS-1$
			}
		}
		content += "]"; //$NON-NLS-1$
		return content;
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
		IAIFTypeArray arrType = (IAIFTypeArray) getType();
		IAIFType baseType = arrType.getBaseType();
		this.values = new IAIFValue[arrType.getRange().getSize()];
		for (int i = 0; i < values.length; i++) {
			values[i] = AIFFactory.getAIFValue(this, baseType, buffer);
			setSize(sizeof() + values[i].sizeof());
		}
	}
}
