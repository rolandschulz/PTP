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

package org.eclipse.ptp.proxy.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.util.ProtocolUtil;

public class ProxyPacket {
	public static final int PACKET_LENGTH_SIZE = 8;
	public static final int PACKET_CHANNEL_SIZE = 2;
	public static final int PACKET_ID_SIZE = 4;
	public static final int PACKET_TRANS_ID_SIZE = 8;
	public static final int PACKET_NARGS_SIZE = 8;
	public static final int PACKET_ARG_LEN_SIZE = 8;

	private int packetID;
	private int packetTransID;
	private String[] packetArgs;

	private Charset			charset = Charset.forName("US-ASCII");
	private CharsetEncoder	encoder = charset.newEncoder();
	private CharsetDecoder	decoder = charset.newDecoder();

	public ProxyPacket() {
	}

	public ProxyPacket(IProxyCommand cmd) {
		this.packetID = cmd.getCommandID();
		this.packetTransID = cmd.getTransactionID();
		this.packetArgs = cmd.getArguments();
	}

	public ProxyPacket(IProxyEvent event) {
		this.packetID = event.getEventID();
		this.packetTransID = event.getTransactionID();
		this.packetArgs = event.getAttributes();
	}

	/**
	 * Character set decoder
	 * 
	 * @return decoder
	 */
	public CharsetDecoder decoder() {
		return decoder;
	}
	
	/**
	 * Character set encoder
	 * 
	 * @return encoder
	 */
	public CharsetEncoder encoder() {
		return encoder;
	}

	/**
	 * Get the arguments
	 * 
	 * @return arguments
	 */
	public String[] getArgs() {
		return packetArgs;
	}
	
	/**
	 * Get the packet type
	 * 
	 * @return packet type
	 */
	public int getID() {
		return packetID;
	}
	
	/**
	 * Get the transaction ID for this packet
	 * 
	 * @return transaction ID
	 */
	public int getTransID() {
		return packetTransID;
	}
	
	/**
	 * Process packets from the wire. Each packet comprises a length, header and a body 
	 * formatted as follows:
	 * 
	 * LENGTH HEADER BODY
	 * 
	 * where:
	 * 
	 * LENGTH	is an PACKET_LENGTH_SIZE hexadecimal number representing
	 * 			the total length of the event excluding the LENGTH field.
	 * 
	 * HEADER consists of the following fields:
	 * 
	 * ' ' CMD_ID ':' TRANS_ID ':' NUM_ARGS
	 * 
	 * where:
	 * 
	 * CMD_ID	is an PACKET_ID_SIZE hexadecimal number representing
	 * 			the type of this command.
	 * TRANS_ID	is an PACKET_TRANS_ID_SIZE hexadecimal number representing
	 * 			the transaction ID of the command.
	 * NUM_ARGS	is an PACKET_ARGS_LEN_SIZE hexadecimal number representing
	 * 			the number of arguments. 
	 * 
	 * The command body is formatted as a list of NUM_ARGS string arguments, each 
	 * preceded by a space (0x20) characters as follows:
	 * 	
	 * ' ' LENGTH ':' BYTES ... ' ' LENGTH ':' BYTES
	 * 
	 * where:
	 * 
	 * LENGTH	is an PACKET_ARGS_LEN_SIZE hexadecimal number representing
	 * 			the length of the string.
	 * BYTES	are LENGTH bytes of the string. Any characters are permitted, 
	 * 			including spaces
	 * 	
	 * @return	false if a protocol error occurs
	 * @throws	IOException if the connection is terminated (read returns < 0)
	 * 		
	 */
	public boolean read(ReadableByteChannel channel) throws IOException {
		/*
		 * First EVENT_LENGTH_SIZE bytes are the length of the event
		 */
		ByteBuffer lengthBytes = ByteBuffer.allocate(PACKET_LENGTH_SIZE);
		int readLen;
		
		readLen = fullRead(channel, lengthBytes);
		if (readLen != PACKET_LENGTH_SIZE) {
			return false;
		}
		
		CharBuffer len_str = decoder.decode(lengthBytes);
	
		int len = Integer.parseInt(len_str.subSequence(0, PACKET_LENGTH_SIZE).toString(), 16);
		
		/*
		 * Read len bytes of rest of event
		 */
		ByteBuffer eventBytes = ByteBuffer.allocate(len);
	
		readLen = fullRead(channel, eventBytes);
		if (readLen < PACKET_ID_SIZE + PACKET_TRANS_ID_SIZE + PACKET_NARGS_SIZE + 3) {
			return false;
		}
	
		CharBuffer eventBuf = decoder.decode(eventBytes);
		
		/*
		 * Extract transaction ID and event type
		 */
		
		int idStart = 1; // Skip ' '
		int idEnd = idStart + PACKET_ID_SIZE;
		int transStart = idEnd + 1; // Skip ':'
		int transEnd = transStart + PACKET_TRANS_ID_SIZE;
		int numArgsStart = transEnd + 1; // Skip ':'
		int numArgsEnd = numArgsStart + PACKET_NARGS_SIZE;
		
		try {
			packetID = Integer.parseInt(eventBuf.subSequence(idStart, idEnd).toString(), 16);
			packetTransID = Integer.parseInt(eventBuf.subSequence(transStart, transEnd).toString(), 16);
			int packetNumArgs = Integer.parseInt(eventBuf.subSequence(numArgsStart, numArgsEnd).toString(), 16);
			
			/*
			 * Extract rest of event arguments. Each argument is an 8 byte hex length, ':' and
			 * then the characters of the argument.
			 */
			
			packetArgs = new String[packetNumArgs];
			int argPos = numArgsEnd + 1;
			
			for (int i = 0; i < packetNumArgs; i++) {
				packetArgs[i] = ProtocolUtil.decodeString(eventBuf, argPos);
				argPos += packetArgs[i].length() + PACKET_ARG_LEN_SIZE + 2;
			}
		} catch (IndexOutOfBoundsException e1) {
			return false;
		}
		
		return true;
	}
	
	public void send(WritableByteChannel channel) throws IOException {
		String body = ProtocolUtil.encodeIntVal(packetID, PACKET_ID_SIZE) 
			+ ":" + ProtocolUtil.encodeIntVal(packetTransID, PACKET_TRANS_ID_SIZE)
			+ ":" + ProtocolUtil.encodeIntVal(packetArgs.length, PACKET_ARG_LEN_SIZE);

		for (String arg : packetArgs) {
			body += " " + ProtocolUtil.encodeString(arg);
		}

		/*
		 * Note: command length includes the first space!
		 */
		String packet = ProtocolUtil.encodeIntVal(body.length() + 1, 
				PACKET_LENGTH_SIZE) + " " + body;
		
		fullWrite(channel, encoder.encode(CharBuffer.wrap(packet)));
	}
	
	/**
	 * Read a full buffer from the socket.
	 * 
	 * @return	number of bytes read
	 * @throws	IOException if EOF
	 */
	private int fullRead(ReadableByteChannel channel, ByteBuffer buf) throws IOException {
		int n = 0;
		buf.clear();
		while (buf.remaining() > 0) {
			n = channel.read(buf);
			if (n < 0) {
				throw new IOException("EOF from proxy");
			}
		}
		buf.flip();
		return n;
	}
	
	/**
	 * Write a full buffer to the socket.
	 * 
	 * @param buf
	 * @return number of bytes written
	 * @throws IOException
	 */
	private int fullWrite(WritableByteChannel channel, ByteBuffer buf) throws IOException {
		int n = 0;
		while (buf.remaining() > 0) {
			n = channel.write(buf);
			if (n < 0) {
				throw new IOException("EOF from proxy");
			}
		}
		return n;
	}
}
