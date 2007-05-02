package org.eclipse.ptp.core.proxy.command;

import java.io.IOException;

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
		
}