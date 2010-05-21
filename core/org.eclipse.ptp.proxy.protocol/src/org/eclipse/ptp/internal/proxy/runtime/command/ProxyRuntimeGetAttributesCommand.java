/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeGetAttributesCommand;

/**
 * Command class for GET_ATTRIBUTES command. This command is used to query the
 * values of attributes associated with a model element
 * 
 * @author David Wootton
 * 
 */
public class ProxyRuntimeGetAttributesCommand extends AbstractProxyCommand
		implements IProxyRuntimeGetAttributesCommand {
	/**
	 * Create an instance of a GET_ATTRIBUTES command with no parameters
	 */
	public ProxyRuntimeGetAttributesCommand() {
		super(GET_ATTRIBUTES);
	}

	/**
	 * Create an instance of a GET_ATTRIBUTES command
	 * 
	 * @param transID
	 *            Transaction id for this command
	 * @param args
	 *            Parameters specifying set of attribute values to be obtained
	 */
	public ProxyRuntimeGetAttributesCommand(int transID, String args[]) {
		super(GET_ATTRIBUTES, transID, args);
	}
}
