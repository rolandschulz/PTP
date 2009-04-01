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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProxyCommand implements IProxyCommand {
	
	private static List<IProxyCommand> pendingCommands = new ArrayList<IProxyCommand>();

	private int					commandID;
	private int					transactionID;
	private ArrayList<String>	args;
	
	protected AbstractProxyCommand(int commandID) {
		this.commandID = commandID;
		this.transactionID = newTransactionID(this);
		this.args = new ArrayList<String>();
	}

	protected AbstractProxyCommand(int commandID, int transID, String[] args) {
		this.commandID = commandID;
		this.transactionID = transID;
		for (String arg : args) {
			this.args.add(arg);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.command.IProxyCommand#addArgument(java.lang.String)
	 */
	public void addArgument(String arg) {
		if (arg == null) {
			args.add(""); //$NON-NLS-1$
		} else {
			args.add(arg);
		}
	}
	
	/**
	 * Mark the command that it has been completed so the transaction
	 * ID can be used by future commands.
	 */
	public void completed() {
		pendingCommands.add(transactionID, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.command.IProxyCommand#getArguments()
	 */
	public String[] getArguments() {
		return args.toArray(new String[args.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.command.IProxyCommand#getCommandID()
	 */
	public int getCommandID() {
		return commandID;
	}

	/**
	 * Return the transaction id used by this command
	 */
	public int getTransactionID() {
		return transactionID;
	}

	public String toString() {
		String str = this.getClass().getSimpleName() + " tid=" + getTransactionID(); //$NON-NLS-1$
		
		for (String arg : args) {
			if (arg == null) {
				str += " <null>"; //$NON-NLS-1$
			} else if (arg.equals("")) { //$NON-NLS-1$
				str += " \"\""; //$NON-NLS-1$
			} else {
				str += " " + arg; //$NON-NLS-1$
			}
		}
		return str;
	}

	/*
	 * Find first available slot. Use slot ID as transaction ID.
	 * 
	 * @return new transaction ID
	 */
	private int newTransactionID(IProxyCommand cmd) {
		int transID = 0;
		
		for (; transID < pendingCommands.size(); transID++) {
			if (pendingCommands.get(transID) == null) {
				break;
			}
		}
		
		pendingCommands.add(transID, cmd);
		
		return transID;
	}

	/**
	 * @param arg
	 */
	protected void addArgument(boolean arg) {
		addArgument(arg?"1":"0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param arg
	 */
	protected void addArgument(Character arg) {
		args.add(arg==null ? "" : arg.toString()); //$NON-NLS-1$
	}

	/**
	 * @param arg
	 */
	protected void addArgument(int arg) {
		args.add(Integer.toString(arg));
	}

	/**
	 * @param arg
	 */
	protected void addArgument(long arg) {
		args.add(Long.toString(arg));
	}

	/**
	 * @param args
	 */
	protected void addArguments(String[] args) {
		for (String arg : args) {
			addArgument(arg);
		}
	}

}
