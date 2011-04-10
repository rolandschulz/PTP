/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;

public abstract class DynamicControlUpdateModel extends AbstractUpdateModel {

	protected List<Arg> dynamic;

	protected DynamicControlUpdateModel(List<Arg> dynamic, ValueUpdateHandler handler) {
		super(ZEROSTR, handler);
		this.dynamic = dynamic;
	}

	protected DynamicControlUpdateModel(String name, ValueUpdateHandler handler) {
		super(name, handler);
	}

	protected String getResolvedDynamic() {
		return ArgImpl.toString(null, dynamic, lcMap);
	}

	@Override
	protected void storeValue() {
		if (canSave) {
			super.storeValue();
		}
	}
}
