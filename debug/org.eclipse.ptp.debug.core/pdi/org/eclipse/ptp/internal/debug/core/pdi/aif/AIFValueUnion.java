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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueParent;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueUnion extends ValueParent implements IAIFValueUnion {
	private final Map<String, IAIFValue> values = new HashMap<String, IAIFValue>();

	public AIFValueUnion(IValueParent parent, IAIFTypeUnion type, SimpleByteBuffer buffer) {
		super(parent, type);
		parse(buffer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueUnion#getFieldValue
	 * (java.lang.String)
	 */
	public IAIFValue getFieldValue(String name) {
		return values.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		String content = "("; //$NON-NLS-1$
		int length = values.size();
		for (int i = 0; i < length; i++) {
			IAIFValue value = values.get(i);
			content += value.getValueString();
			if (i < length - 1) {
				content += ","; //$NON-NLS-1$
			}
		}
		return content + ")"; //$NON-NLS-1$
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
		IAIFTypeUnion typeUnion = (IAIFTypeUnion) getType();
		String[] names = typeUnion.getFieldNames();
		IAIFType[] types = typeUnion.getFieldTypes();
		for (int i = 0; i < names.length; i++) {
			IAIFValue val = AIFFactory.getAIFValue(getParent(), types[i], buffer);
			values.put(names[i], val);
			setSize(sizeof() + val.sizeof());
		}
	}
}
