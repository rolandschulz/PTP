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

package org.eclipse.ptp.core.proxy.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;

public abstract class AbstractProxyCommand implements IProxyCommand {
	
	protected IProxyClient			client;
	protected int					id;
	protected ArrayList<String>		args;
	protected boolean				logEvents = true;
	
	// CopyOnWriteArrayList used to reduce granularity in handleEvent and remove ConcurrentModificationException
	protected List<IProxyEventListener> listeners = Collections.synchronizedList(new CopyOnWriteArrayList<IProxyEventListener>());

	public abstract int getCommandID();
	
	protected AbstractProxyCommand(IProxyClient client) {
		super();
		this.client = client;
		this.id = client.newTransactionID();
		this.args = new ArrayList<String>();
	}
	
	protected AbstractProxyCommand(IProxyClient client, String[] args) {
		this(client);
		for (String arg : args) {
			this.args.add(arg);
		}
	}
	
	public void send() throws IOException {
		System.out.println("-> " + this.toString());
		client.sendCommand(this.getEncodedMessage());
	}
			
	/**
	 * Return the transaction id used by this command
	 */
	public int getTransactionID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.IProxyCommand#getEncodedMessage()
	 */
	public String getEncodedMessage()  {
		String msg = AbstractProxyClient.encodeIntVal(getCommandID(), CMD_ID_SIZE) 
						+ ":" + AbstractProxyClient.encodeIntVal(getTransactionID(), CMD_TRANS_ID_SIZE)
						+ ":" + AbstractProxyClient.encodeIntVal(args.size(), CMD_ARGS_LEN_SIZE);
		
		for (String arg : args) {
			msg += " " + AbstractProxyClient.encodeString(arg);
		}
		
		return msg;
	}

	protected void addArgument(String arg) {
		if (arg == null) {
			args.add("");
		} else {
			args.add(arg);
		}
	}

	protected void addArgument(int arg) {
		args.add(Integer.toString(arg));
	}

	protected void addArgument(boolean arg) {
		addArgument(arg?"1":"0");
	}

	protected void addArgument(long arg) {
		args.add(Long.toString(arg));
	}

	protected void addArgument(Character arg) {
		args.add(arg==null ? "" : arg.toString());
	}

	protected void addArguments(String[] args) {
		for (String arg : args) {
			addArgument(arg);
		}
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

}
