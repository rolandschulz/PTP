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
 * Command class for QUERY_ATTRIBUTES command. This command is used to query the
 * set of attributes associated with a model element
 * 
 * @author David Wootton
 * 
 */
public class ProxyRuntimeQueryAttributesCommand extends AbstractProxyCommand
		implements IProxyRuntimeGetAttributesCommand {
	/**
	 * Create an instance of a QUERY_ATTRIBUTES command with no parameters
	 */
	public ProxyRuntimeQueryAttributesCommand() {
		super(QUERY_ATTRIBUTES);
	}

	/**
	 * Create an instance of a QUERY_ATTRIBUTES command
	 * 
	 * @param transID
	 *            Transaction id for this command
	 * @param args
	 *            Parameters specifying set of attributes to be queried
	 */
	public ProxyRuntimeQueryAttributesCommand(int transID, String args[]) {
		super(QUERY_ATTRIBUTES, transID, args);
	}
}
