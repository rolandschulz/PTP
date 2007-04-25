package org.eclipse.ptp.core.proxy.command;

import java.io.IOException;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public interface IProxyCommand extends Runnable {

	public static final int CMD_LENGTH_SIZE = 8;
	public static final int CMD_ID_SIZE = 4;
	public static final int CMD_TRANS_ID_SIZE = 8;
	public static final int CMD_ARGS_LEN_SIZE = 8;
	
	/*
	 * Base command ids
	 */
	public static final int CMD_QUIT = 0;
	public static final int CMD_INIT = 1;
	public static final int CMD_MODEL_DEF = 2;
	public static final int CMD_START_EVENTS = 3;
	public static final int CMD_STOP_EVENTS = 4;
	public static final int CMD_SUBMIT_JOB = 5;
	public static final int CMD_TERM_JOB = 6;

	/**
	 * Send the command
	 */
	public void send() throws IOException;
		
	/**
	 * @return the transaction id used by this command
	 */
	public int getCommandID();
		
	/**
	 * @return the transaction id used by this command
	 */
	public int getTransactionID();
	
	/**
	 * @return the encoded message buffer to be sent to the proxy
	 */
	public String getEncodedMessage();

	/**
	 * Handle (dispatch) the
	 * 
	 * @param event
	 */
	public void handleEvent(IProxyEvent event);
	
	/**
	 * Set the event to be handled in the run method
	 * 
	 * @param event = the event to handle
	 */
	public void setEvent(IProxyEvent event);
		
}