/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cldt.debug.mi.core.cdi.model.type;

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cldt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cldt.debug.mi.core.cdi.Session;
import org.eclipse.cldt.debug.mi.core.cdi.SourceManager;
import org.eclipse.cldt.debug.mi.core.cdi.model.Target;

/**
 */
public abstract class DerivedType extends Type implements ICDIDerivedType {

	ICDIType derivedType;

	public DerivedType(Target target, String typename) {
		super(target, typename);
	}

	public void setComponentType(ICDIType dtype) {
		derivedType = dtype;
	}

	public void setComponentType(String name) {
		Target target = (Target)getTarget();
		Session session = (Session)target.getSession();
		SourceManager sourceMgr = session.getSourceManager();
		try {
			derivedType = sourceMgr.getType((Target)getTarget(), name);
		} catch (CDIException e) {
			// Try after ptype.
			try {
				String ptype = sourceMgr.getDetailTypeName((Target)getTarget(), name);
				derivedType = sourceMgr.getType((Target)getTarget(), ptype);
			} catch (CDIException ex) {
			}
		}
		if (derivedType == null) {
			derivedType = new IncompleteType((Target)getTarget(), name);
		}
	}
}
