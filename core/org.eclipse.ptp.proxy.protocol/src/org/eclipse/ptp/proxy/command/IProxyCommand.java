/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.command;


public interface IProxyCommand {

	public static final int CMD_LENGTH_SIZE = 8;
	public static final int CMD_ID_SIZE = 4;
	public static final int CMD_TRANS_ID_SIZE = 8;
	public static final int CMD_ARGS_LEN_SIZE = 8;
	
	/*
	 * Base command ids
	 */
	public static final int QUIT = 0;

	/**
	 * Mark command as completed
	 */
	public void completed();
		
	/**
	 * @return the transaction id used by this command
	 */
	public int getCommandID();
	
	/**
	 * @return the encoded message buffer to be sent to the proxy
	 */
	public String getEncodedMessage();
	
	/**
	 * @return the transaction id used by this command
	 */
	public int getTransactionID();
		
}