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
package org.eclipse.ptp.debug.internal.core.pdi.aif;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueParent;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public abstract class ValueAggregate extends ValueParent implements IValueAggregate {
	protected List<IAIFValue> values = new ArrayList<IAIFValue>();
	
	public ValueAggregate(IValueParent parent, ITypeAggregate type, SimpleByteBuffer buffer) {
		super(parent, type);
		parse(buffer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValue#parse(org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory.SimpleByteBuffer)
	 */
	protected void parse(SimpleByteBuffer buffer) {
		ITypeAggregate typeAggregate = (ITypeAggregate)getType();
		int num_children = typeAggregate.getNumberOfChildren();
		for (int i=0; i<num_children; i++) {
			IAIFType aifType = typeAggregate.getType(i);
			IAIFValue val = AIFFactory.getAIFValue(getParent(), aifType, buffer);
			values.add(val);
			size += val.sizeof();
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValue#getChildrenNumber()
	 */
	public int getChildrenNumber() throws AIFException {
		return values.size();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue#getValueString()
	 */
	public String getValueString() throws AIFException {
		if (result == null) {
			result = getString();
		}
		return result;
	}
	
	private String getString() throws AIFException {
		String content = "{"; //$NON-NLS-1$
		int length = values.size();
		for (int i=0; i<length; i++) {
			IAIFValue value = (IAIFValue)values.get(i);
			content += value.getValueString();
			if (i < length - 1) {
				content += ","; //$NON-NLS-1$
			}
		}
		return content + "}"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.aif.IValueAggregate#getValue(int)
	 */
	public IAIFValue getValue(int index) {
		return values.get(index);
	}
}
