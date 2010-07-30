/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
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
import org.eclipse.ptp.proxy.messages.Messages;
import org.eclipse.ptp.proxy.util.ProtocolUtil;

public class ProxyPacket {
	public static final int PACKET_LENGTH_SIZE = 8;
	public static final int PACKET_CHANNEL_SIZE = 2;
	public static final int PACKET_ID_SIZE = 4;
	public static final int PACKET_TRANS_ID_SIZE = 8;
	public static final int PACKET_NARGS_SIZE = 8;
	public static final int PACKET_ARG_LEN_SIZE = 8;

	private boolean debug = false;

	private int packetID;
	private int packetTransID;
	private String[] packetArgs;

	private final Charset charset = Charset.forName("US-ASCII"); //$NON-NLS-1$
	private final CharsetEncoder encoder = charset.newEncoder();
	private final CharsetDecoder decoder = charset.newDecoder();

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
		if (this.packetArgs == null) {
			this.packetArgs = new String[0];
		}
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
	 * Read a full buffer from the socket. Guaranteed to read buf.remaining()
	 * bytes from the channel.
	 * 
	 * FIXME: Can this block if there is nothing available on the channel? If
	 * so, then there should be some kind of timeout to prevent the UI from
	 * hanging.
	 * 
	 * @throws IOException
	 *             if EOF
	 */
	private void fullRead(ReadableByteChannel channel, ByteBuffer buf) throws IOException {
		buf.clear();
		while (buf.hasRemaining()) {
			int n = channel.read(buf);
			if (n < 0) {
				throw new IOException(Messages.getString("ProxyPacket_2")); //$NON-NLS-1$
			}
		}
		buf.flip();
	}

	/**
	 * Write a full buffer to the socket. Guaranteed to write buf.remaingin()
	 * bytes to the channel.
	 * 
	 * FIXME: Can this block? If so, then there should be some kind of timeout
	 * to prevent the UI from hanging.
	 * 
	 * @param buf
	 * @throws IOException
	 */
	private void fullWrite(WritableByteChannel channel, ByteBuffer buf) throws IOException {
		while (buf.hasRemaining()) {
			int n = channel.write(buf);
			if (n < 0) {
				throw new IOException(Messages.getString("ProxyPacket_3")); //$NON-NLS-1$
			}
		}
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
	 * Process packets from the wire. Each packet comprises a length, header and
	 * a body formatted as follows:
	 * 
	 * LENGTH HEADER BODY
	 * 
	 * where:
	 * 
	 * LENGTH is an PACKET_LENGTH_SIZE hexadecimal number representing the total
	 * length of the HEADER and BODY sections.
	 * 
	 * HEADER consists of the following fields:
	 * 
	 * ' ' CMD_ID ':' TRANS_ID ':' NUM_ARGS
	 * 
	 * where:
	 * 
	 * CMD_ID is an PACKET_ID_SIZE hexadecimal number representing the type of
	 * this command. TRANS_ID is an PACKET_TRANS_ID_SIZE hexadecimal number
	 * representing the transaction ID of the command. NUM_ARGS is an
	 * PACKET_ARGS_LEN_SIZE hexadecimal number representing the number of
	 * arguments.
	 * 
	 * The command body is formatted as a list of NUM_ARGS string arguments,
	 * each preceded by a space (0x20) characters as follows:
	 * 
	 * ' ' LENGTH ':' BYTES ... ' ' LENGTH ':' BYTES
	 * 
	 * where:
	 * 
	 * LENGTH is an PACKET_ARGS_LEN_SIZE hexadecimal number representing the
	 * length of the string. BYTES are LENGTH bytes of the string. Any
	 * characters are permitted, including spaces
	 * 
	 * @return false if a protocol error occurs
	 * @throws IOException
	 *             if the connection is terminated (read returns < 0)
	 * 
	 */
	public boolean read(ReadableByteChannel channel) throws IOException {
		/*
		 * First EVENT_LENGTH_SIZE bytes are the length of the event
		 */
		ByteBuffer lengthBytes = ByteBuffer.allocate(PACKET_LENGTH_SIZE);
		fullRead(channel, lengthBytes);
		CharBuffer len_str = decoder.decode(lengthBytes);

		int len;
		try {
			len = Integer.parseInt(len_str.subSequence(0, PACKET_LENGTH_SIZE)
					.toString(), 16);
		} catch (NumberFormatException e) {
			if (debug) {
				System.out.println("] BAD PACKET LENGTH"); //$NON-NLS-1$
			} else {
				System.out.println("BAD PACKET LENGTH: \"" + len_str + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			throw new IOException(Messages.getString("ProxyPacket_0")); //$NON-NLS-1$
		}

		/*
		 * Read len bytes of rest of packet
		 */
		ByteBuffer packetBytes = ByteBuffer.allocate(len);
		fullRead(channel, packetBytes);
		CharBuffer packetBuf = decoder.decode(packetBytes);

		if (debug) {
			System.out.println("RECEIVE:[" + len_str + " -> " + packetBuf + "] -> " + Thread.currentThread().getName()); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}

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
			packetID = Integer.parseInt(packetBuf.subSequence(idStart, idEnd)
					.toString(), 16);
			packetTransID = Integer.parseInt(packetBuf.subSequence(transStart,
					transEnd).toString(), 16);
			int packetNumArgs = Integer.parseInt(packetBuf.subSequence(
					numArgsStart, numArgsEnd).toString(), 16);

			/*
			 * Extract rest of the arguments. Each argument is an 8 byte hex
			 * length, ':' and then the characters of the argument.
			 */

			packetArgs = new String[packetNumArgs];
			int argPos = numArgsEnd + 1;

			for (int i = 0; i < packetNumArgs; i++) {
				packetArgs[i] = ProtocolUtil.decodeString(packetBuf, argPos);
				argPos += packetArgs[i].length() + PACKET_ARG_LEN_SIZE + 2;
			}
		} catch (NumberFormatException e) {
			System.out.println("BAD PACKET FORMAT: \"" + packetBuf + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			throw new IOException(Messages.getString("ProxyPacket_1")); //$NON-NLS-1$
		} catch (IndexOutOfBoundsException e1) {
			return false;
		}

		return true;
	}

	public void send(WritableByteChannel channel) throws IOException {
		String body = ProtocolUtil.encodeIntVal(packetID, PACKET_ID_SIZE)
				+ ":" + ProtocolUtil.encodeIntVal(packetTransID, PACKET_TRANS_ID_SIZE) //$NON-NLS-1$
				+ ":" + ProtocolUtil.encodeIntVal(packetArgs.length, PACKET_ARG_LEN_SIZE); //$NON-NLS-1$

		for (String arg : packetArgs) {
			body += " " + ProtocolUtil.encodeString(arg); //$NON-NLS-1$
		}

		/*
		 * Note: command length includes the first space!
		 */
		String packet = ProtocolUtil.encodeIntVal(body.length() + 1,
				PACKET_LENGTH_SIZE)
				+ " " + body; //$NON-NLS-1$

		if (debug) {
			System.out
					.println("SEND:[" + packet + "] -> " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		fullWrite(channel, encoder.encode(CharBuffer.wrap(packet)));
	}

	/**
	 * Enable/disable protocol debugging
	 * 
	 * @param logging
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
