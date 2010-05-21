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
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeClearFiltersCommand;

/**
 * Command class for CLEAR_FILTERS command. This command is used in proxy flow
 * control to reset filtering for events which meet the criteria specified in
 * the command.
 * 
 * @author David Wootton
 * 
 */
public class ProxyRuntimeClearFiltersCommand extends AbstractProxyCommand
		implements IProxyRuntimeClearFiltersCommand {
	/**
	 * Create an instance of a CLEAR_FILTERS command with no parameters
	 */
	public ProxyRuntimeClearFiltersCommand() {
		super(CLEAR_FILTERS);
	}

	/**
	 * Create an instance of a CLEAR_FILTERS command
	 * 
	 * @param transID
	 *            Transaction id for this command
	 * @param args
	 *            Parameters specifying filters to be cleared
	 */
	public ProxyRuntimeClearFiltersCommand(int transID, String args[]) {
		super(CLEAR_FILTERS, transID, args);
	}
}
