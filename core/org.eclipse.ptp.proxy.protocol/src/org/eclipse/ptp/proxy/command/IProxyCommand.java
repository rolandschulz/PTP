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
	public String[] getArguments();
	
	/**
	 * @return the transaction id used by this command
	 */
	public int getTransactionID();
	
	/**
	 * Add an argument to the command
	 * 
	 * @param arg argument to add to the command
	 */
	public void addArgument(String arg);
}