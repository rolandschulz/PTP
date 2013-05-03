/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import org.eclipse.ptp.internal.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;

/**
 * Defines methods for an Assign action connected to the stream parser
 * (tokenizer).
 * 
 * @author arossi
 * 
 */
public interface IAssign {

	/**
	 * @param values
	 *            parsed values from match expression
	 * @throws StreamParserException
	 */
	public void assign(String[] values) throws StreamParserException;

	/**
	 * @return index of assign action next target
	 */
	public int getIndex();

	/**
	 * @param increment
	 *            by which to increase index number
	 */
	public void incrementIndex(int increment);

	/**
	 * @return whether to force the creation of a new object
	 * 
	 */
	public boolean isForceNew();

	/**
	 * @param target
	 *            current target for the assign action
	 */
	public void setTarget(AttributeType target);
}
