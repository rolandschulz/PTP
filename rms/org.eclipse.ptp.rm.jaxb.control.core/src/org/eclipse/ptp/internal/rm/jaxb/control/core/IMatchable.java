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

/**
 * Defines object for tokenizer to call to process matches against.
 * 
 * @author arossi
 * 
 */
public interface IMatchable {

	/**
	 * Match against the current stream segment.
	 * 
	 * @param segment
	 *            passed in from the tokenizer
	 * @return whether there was a successful match
	 * @throws StreamParserException
	 */
	public boolean doMatch(StringBuffer segment) throws StreamParserException;

	/**
	 * @return whether the tokenizer should promote this object to first in its
	 *         list.
	 */
	public boolean isSelected();

	/**
	 * Run post-processing operations on the object.
	 * 
	 * @throws StreamParserException
	 */
	public void postProcess() throws StreamParserException;

	/**
	 * @param selected
	 *            whether the tokenizer should promote this object to first in
	 *            its list.
	 */
	public void setSelected(boolean selected);
}
