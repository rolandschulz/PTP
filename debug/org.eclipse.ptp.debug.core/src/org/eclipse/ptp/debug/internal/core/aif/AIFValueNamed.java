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

import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.AIFFactory;
import org.eclipse.ptp.debug.core.aif.IAIFTypeNamed;
import org.eclipse.ptp.debug.core.aif.IAIFValue;
import org.eclipse.ptp.debug.core.aif.IAIFValueNamed;
import org.eclipse.ptp.debug.core.aif.IValueParent;
import org.eclipse.ptp.debug.core.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueNamed extends ValueDerived implements IAIFValueNamed {
	IAIFValue value;
	String name;
	
	public AIFValueNamed(IValueParent parent, IAIFTypeNamed type, SimpleByteBuffer buffer) {
		super(parent, type);
		this.name = type.getName();
		parse(buffer);
	}
	protected void parse(SimpleByteBuffer buffer) {
		value = AIFFactory.getAIFValue(this, ((IAIFTypeNamed)type).getBaseType(), buffer);
	}
	public int getChildrenNumber() throws AIFException {
		return value.getChildrenNumber();
	}
	
	public String getValueString() throws AIFException {
		if (result == null) {
			result = value.getValueString();
		}
		return result;
	}	
	public IAIFValue getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
}
