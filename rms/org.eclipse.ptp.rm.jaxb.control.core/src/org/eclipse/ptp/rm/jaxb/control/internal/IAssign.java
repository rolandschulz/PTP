/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal;

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
	 * @throws Throwable
	 */
	public void assign(String[] values) throws Throwable;

	/**
	 * @return index of assign action next target
	 */
	public int getIndex();

	/**
	 * @param target
	 *            current target for the assign action
	 */
	public void setTarget(Object target);
}
