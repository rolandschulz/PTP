/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.ui;

/**
 * @since 1.1
 */
public interface IUpdateHandler {

	/**
	 * Supplies the error message from failed validation to the handler
	 * 
	 * @param source
	 *            the control which failed validation
	 * @param error
	 *            error message
	 */
	public void addError(String source, String error);

	/**
	 * Adds a widget-to-model mapping to the appropriate map.
	 * 
	 * @param control
	 *            widget or cell editor
	 * @param model
	 *            associate data model
	 */
	public void addUpdateModelEntry(Object control, IUpdateModel model);

	/**
	 * Empties the maps.
	 */
	public void clear();

	/**
	 * Returns the error associated with the first control (in alphabetical order)
	 * 
	 * @return the error for the first control
	 */
	public String getFirstError();

	/**
	 * Broadcasts update request to all other controls by invoking refresh on
	 * their model objects.<br>
	 * <br>
	 * 
	 * The order follows this logic: update all cell editors first, then have
	 * the viewers write their template strings, then refresh the rest of the
	 * widgets.<br>
	 * <br>
	 * 
	 * In order to catch any references in table or tree cells to the template
	 * output of other viewers, the first two update sets are iterated once.
	 * 
	 * The value can largely be ignored.
	 * 
	 * @param source
	 *            the control which has been modified
	 * @param value
	 *            the new value (if any) produced (unused here)
	 */
	public void handleUpdate(Object source, Object value);

	/**
	 * Remove the error message associated with the control
	 * 
	 * @param source
	 *            control which invalid value
	 */
	public void removeError(String source);
}
