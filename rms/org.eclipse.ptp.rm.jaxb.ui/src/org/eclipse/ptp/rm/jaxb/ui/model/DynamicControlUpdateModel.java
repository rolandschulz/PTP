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

import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ArgImpl;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;

/**
 * Base class for special model type which can be read-only. In the latter case,
 * it has a list of args which must be resolved using the refreshed environment,
 * but it does not store a value. If it is instead provided with a
 * name/reference, it acts in the default manner, refreshing and storing the
 * value entered.
 * 
 * @author arossi
 * 
 */
public abstract class DynamicControlUpdateModel extends AbstractUpdateModel {

	protected List<ArgType> dynamic;

	/**
	 * Constructor for read-only widget.
	 * 
	 * @param dynamic
	 *            arguments which need to be resolved using the refreshed
	 *            environment.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 */
	protected DynamicControlUpdateModel(List<ArgType> dynamic, ValueUpdateHandler handler) {
		super(ZEROSTR, handler);
		this.dynamic = dynamic;
	}

	/**
	 * Default constructor for editable widget.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 */
	protected DynamicControlUpdateModel(String name, ValueUpdateHandler handler) {
		super(name, handler);
	}

	/**
	 * @return the string which results from resolving the arguments.
	 */
	protected String getResolvedDynamic() {
		return ArgImpl.toString(null, dynamic, lcMap);
	}

	/*
	 * Only saves for editable subclasses. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@Override
	protected void storeValue() {
		if (canSave) {
			super.storeValue();
		}
	}
}
