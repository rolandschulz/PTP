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
import org.eclipse.ptp.debug.core.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.aif.IAIFValueNamed;
import org.eclipse.ptp.debug.core.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.aif.IValueParent;
import org.eclipse.ptp.debug.core.aif.AIFFactory.SimpleByteBuffer;

/**
 * @author Clement chu
 * 
 */
public class AIFValueReference extends ValueParent implements IAIFValueReference {
	private String name = null;
	
	public AIFValueReference(IValueParent parent, IAIFTypeReference type, SimpleByteBuffer buffer) {
		super(parent, type);
		parse(buffer);
		this.name = type.getName();
	}
	protected void parse(SimpleByteBuffer buffer) {
		size = type.sizeof();
	}
	public String getName() {
		return name;
	}
	public String getValueString() throws AIFException {
		if (result == null) {
			result = (getParent()==null)?"unknown value":"ref: " + getName();
		}
		return result;
	}
	public IValueParent getParent() {
		if (parent instanceof IAIFValueNamed) {
			if (((IAIFValueNamed)parent).getName().equals(getName())) {
				return parent.getParent();
			}
		}		
		if (parent == null) {
			return null;
		}

		parent = parent.getParent();
		return getParent();
	}
}
