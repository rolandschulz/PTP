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

import org.eclipse.ptp.proxy.util.ProtocolUtil;

public abstract class AbstractProxyCommand implements IProxyCommand {
	
	private static List<IProxyCommand> pendingCommands = new ArrayList<IProxyCommand>();

	private int					id;
	private ArrayList<String>	args;
	
	protected AbstractProxyCommand() {
		super();
		this.id = newTransactionID(this);
		this.args = new ArrayList<String>();
	}
	
	protected AbstractProxyCommand(String[] args) {
		this();
		for (String arg : args) {
			this.args.add(arg);
		}
	}
	
	/**
	 * Mark the command that it has been completed so the transaction
	 * ID can be used by future commands.
	 */
	public void completed() {
		pendingCommands.add(id, null);
	}
	
	public abstract int getCommandID();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyCommand#getEncodedMessage()
	 */
	public String getEncodedMessage()  {
		String msg = ProtocolUtil.encodeIntVal(getCommandID(), CMD_ID_SIZE) 
						+ ":" + ProtocolUtil.encodeIntVal(getTransactionID(), CMD_TRANS_ID_SIZE)
						+ ":" + ProtocolUtil.encodeIntVal(args.size(), CMD_ARGS_LEN_SIZE);
		
		for (String arg : args) {
			msg += " " + ProtocolUtil.encodeString(arg);
		}
		
		return msg;
	}
	
	/**
	 * Return the transaction id used by this command
	 */
	public int getTransactionID() {
		return id;
	}

	public String toString() {
		String str = this.getClass().getSimpleName() + " tid=" + getTransactionID();
		
		for (String arg : args) {
			if (arg == null) {
				str += " <null>";
			} else if (arg.equals("")) {
				str += " \"\"";
			} else {
				str += " " + arg;
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

	protected void addArgument(boolean arg) {
		addArgument(arg?"1":"0");
	}

	protected void addArgument(Character arg) {
		args.add(arg==null ? "" : arg.toString());
	}

	protected void addArgument(int arg) {
		args.add(Integer.toString(arg));
	}

	protected void addArgument(long arg) {
		args.add(Long.toString(arg));
	}

	protected void addArgument(String arg) {
		if (arg == null) {
			args.add("");
		} else {
			args.add(arg);
		}
	}

	protected void addArguments(String[] args) {
		for (String arg : args) {
			addArgument(arg);
		}
	}

}
