/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.model;

import java.util.List;

import org.eclipse.ptp.internal.rm.jaxb.control.core.data.ArgImpl;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;

/**
 * Base class for special model type which can be read-only. In the latter case, it has a list of args which must be resolved using
 * the refreshed environment, but it does not store a value. If it is instead provided with a name/reference, it acts in the default
 * manner, refreshing and storing the value entered.
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
	 *            arguments which need to be resolved using the refreshed environment.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 */
	protected DynamicControlUpdateModel(List<ArgType> dynamic, IUpdateHandler handler) {
		super(JAXBControlUIConstants.ZEROSTR, handler);
		this.dynamic = dynamic;
	}

	/**
	 * Default constructor for editable widget.
	 * 
	 * @param name
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 */
	protected DynamicControlUpdateModel(String name, IUpdateHandler handler) {
		super(name, handler);
	}

	/**
	 * @return the string which results from resolving the arguments.
	 */
	protected String getResolvedDynamic() {
		return ArgImpl.toString(null, dynamic, lcMap);
	}
}
