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
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSetFiltersCommand;

/**
 * Command class for SET_FILTERS command. This command is used in proxy flow
 * control to set filtering for events which meet the criteria specified in the
 * command.
 * 
 * @author David Wootton
 * 
 */
public class ProxyRuntimeSetFiltersCommand extends AbstractProxyCommand
		implements IProxyRuntimeSetFiltersCommand {

	/**
	 * Create an instance of a SET_FILTERS command with no parameters
	 */
	public ProxyRuntimeSetFiltersCommand() {
		super(SET_FILTERS);
	}

	/**
	 * Create an instance of a SET_FILTERS command
	 * 
	 * @param transID
	 *            Transaction id for this command
	 * @param args
	 *            Parameters specifying filters to be set
	 */
	public ProxyRuntimeSetFiltersCommand(int transID, String args[]) {
		super(SET_FILTERS, transID, args);
	}

}
