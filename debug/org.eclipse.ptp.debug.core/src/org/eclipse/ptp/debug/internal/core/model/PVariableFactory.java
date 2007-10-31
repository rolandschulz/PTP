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
package org.eclipse.ptp.debug.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.debug.core.model.IPGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;

/**
 * @author Clement chu
 * 
 */
public class PVariableFactory {
	public static PVariable createLocalVariable(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject) {
		return new PLocalVariable(parent, pdiVariableObject);
	}
	public static PVariable createLocalVariableWithError(PDebugElement parent, IPDIVariableDescriptor pdiVariableObject, String message) {
		return new PLocalVariable(parent, pdiVariableObject, message);
	}
	public static IPGlobalVariableDescriptor createGlobalVariableDescriptor(final String name, final IPath path) {
		return new IPGlobalVariableDescriptor() {
			public String getName() {
				return name;
			}
			public IPath getPath() {
				return (path != null) ? path : new Path("");
			}
			public String toString() {
				return MessageFormat.format("{0}::{1}", new Object[] { getPath().toOSString(), getName() });
			}
			public boolean equals(Object obj) {
				if (!(obj instanceof IPGlobalVariableDescriptor))
					return false;
				IPGlobalVariableDescriptor d = (IPGlobalVariableDescriptor) obj;
				return (getName().compareTo(d.getName()) == 0 && getPath().equals(d.getPath()));
			}
		};
	}
	public static PGlobalVariable createGlobalVariable(PDebugElement parent, IPGlobalVariableDescriptor descriptor, IPDIVariableDescriptor pdiVariableObject) {
		return new PGlobalVariable(parent, descriptor, pdiVariableObject);
	}
}
