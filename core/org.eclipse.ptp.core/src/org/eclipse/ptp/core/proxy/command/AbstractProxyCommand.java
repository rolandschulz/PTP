package org.eclipse.ptp.core.proxy.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEventHandler;
import org.eclipse.ptp.core.proxy.event.IProxyEventListener;

public abstract class AbstractProxyCommand implements IProxyCommand, Runnable {
	
	protected IProxyClient			client;
	protected int					id;
	protected ArrayList<String>		args;
	protected boolean				logEvents = true;
	
	private IProxyEvent				event;
	
	protected IProxyEventHandler	handler;
	
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

	public void setEvent(IProxyEvent event) {
		this.event = event;
	}

	public IProxyEventHandler getEventHandler() {
		return handler;
	}

	public void setEventHandler(IProxyEventHandler handler) {
		this.handler = handler;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.proxy.IProxyCommand#run()
	 */
	public void run() {
		if (handler != null && event != null && event.getTransactionID() == id) {
			handler.handleEvent(this, event);
			event = null;
		}
	}

	public void handleEvent(IProxyEvent event) {
		if (handler != null && event.getTransactionID() == id) {
			handler.handleEvent(this, event);
		}
	}

	protected void addArgument(String arg) {
		args.add(arg);
	}

	protected void addArgument(Integer arg) {
		args.add(arg.toString());
	}
}
