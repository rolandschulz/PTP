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

import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;

/**
 * Interface for tokenizer (parser) attached to process streams. These can be
 * custom implementations of the extension point. The reference implementation
 * is directly configurable from the ResourceManager XML.<br>
 * <br>
 * Extension point: org.eclipse.ptp.rm.jaxb.core.streamParserTokenizer<br>
 * <br>
 * 
 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command.ConfigurableRegexTokenizer
 * 
 * @author arossi
 * 
 */
public interface IStreamParserTokenizer extends Runnable {

	/**
	 * Get the status of the parse. Only valid once the parse is complete.
	 * 
	 * @return status of the parse
	 */
	public IStatus getStatus();

	/**
	 * @param uuid
	 *            id associated with this resource manager operation (can be <code>null</code>).
	 * @param varMap
	 *            resource manager environment
	 */
	public void initialize(String uuid, IVariableMap varMap);

	/**
	 * @param stream
	 *            to be parsed
	 */
	public void setInputStream(InputStream stream);
}
