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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.messages.Messages;
import org.eclipse.ptp.proxy.util.ProtocolUtil;
import org.eclipse.ptp.proxy.util.VarInt;

/**
 * @since 5.0
 */
public class ProxyPacket {
	private static final int PACKET_LENGTH_SIZE = 4;

	private boolean debug = false;

	private int fPacketFlags;
	private int fPacketID;
	private int fPacketTransID;
	private String[] fPacketArgs;

	private final Charset fCharset = Charset.forName("US-ASCII"); //$NON-NLS-1$
	private final CharsetEncoder encoder = fCharset.newEncoder();
	private final CharsetDecoder decoder = fCharset.newDecoder();

	public ProxyPacket() {
		fPacketFlags = 0;
	}

	public ProxyPacket(IProxyCommand cmd) {
		this();
		fPacketID = cmd.getCommandID();
		fPacketTransID = cmd.getTransactionID();
		fPacketArgs = cmd.getArguments();
	}

	public ProxyPacket(IProxyEvent event) {
		this();
		fPacketID = event.getEventID();
		fPacketTransID = event.getTransactionID();
		fPacketArgs = event.getAttributes();
		if (fPacketArgs == null) {
			fPacketArgs = new String[0];
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
	 * @param buf
	 *            buffer containing the result of the read
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
	 * Write an array of buffers to the socket. Guarantees to write the whole
	 * buffer array.
	 * 
	 * NOTE: Originally used GatherByteChannel but the performance is pathetic.
	 * This implementation requires a buffer copy, which should be eliminated.
	 * 
	 * FIXME: Can this block? If so, then there should be some kind of timeout
	 * to prevent the UI from hanging.
	 * 
	 * @param bufs
	 *            array of buffers to write
	 * @param len
	 *            total number of bytes to write
	 * @throws IOException
	 */
	private void fullWrite(WritableByteChannel channel, List<ByteBuffer> bufs, long len) throws IOException {
		long n;
		ByteBuffer bb = ByteBuffer.allocate((int) len);
		for (ByteBuffer b : bufs) {
			bb.put(b);
		}
		bb.flip();
		while ((n = channel.write(bb)) < len) {
			if (n < 0) {
				throw new IOException(Messages.getString("ProxyPacket_2")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Get the arguments
	 * 
	 * @return arguments
	 */
	public String[] getArgs() {
		return fPacketArgs;
	}

	/**
	 * Get the packet type
	 * 
	 * @return packet type
	 */
	public int getID() {
		return fPacketID;
	}

	/**
	 * Get the transaction ID for this packet
	 * 
	 * @return transaction ID
	 */
	public int getTransID() {
		return fPacketTransID;
	}

	/**
	 * Process packets from the wire.
	 * 
	 * @return false if a protocol error occurs
	 * @throws IOException
	 *             if the connection is terminated (read returns < 0)
	 * 
	 */
	public boolean read(ReadableByteChannel channel) throws IOException {
		/*
		 * First PACKET_LENGTH_SIZE bytes are the length of the event
		 */
		ByteBuffer lengthBytes = ByteBuffer.allocate(PACKET_LENGTH_SIZE);
		fullRead(channel, lengthBytes);
		int len;
		try {
			len = lengthBytes.getInt();
		} catch (BufferUnderflowException e) {
			System.out.println("BAD PACKET LENGTH"); //$NON-NLS-1$
			throw new IOException(Messages.getString("ProxyPacket_0")); //$NON-NLS-1$
		}

		/*
		 * Read len bytes of rest of packet
		 */
		ByteBuffer packetBytes = ByteBuffer.allocate(len);
		fullRead(channel, packetBytes);

		if (debug) {
			System.out.println("RECEIVE:[" + len + "] -> " + Thread.currentThread().getName()); //$NON-NLS-1$//$NON-NLS-2$
		}

		/*
		 * Get flags (not currently used)
		 */
		VarInt val = new VarInt(packetBytes);
		if (!val.isValid()) {
			throw new IOException(Messages.getString("ProxyPacket_1")); //$NON-NLS-1$
		}
		fPacketFlags = val.getValue();

		/*
		 * Extract event type
		 */
		val = new VarInt(packetBytes);
		if (!val.isValid()) {
			throw new IOException(Messages.getString("ProxyPacket_1")); //$NON-NLS-1$
		}
		fPacketID = val.getValue();

		/*
		 * Get transaction ID
		 */
		val = new VarInt(packetBytes);
		if (!val.isValid()) {
			throw new IOException(Messages.getString("ProxyPacket_3")); //$NON-NLS-1$
		}
		fPacketTransID = val.getValue();

		val = new VarInt(packetBytes);
		if (!val.isValid()) {
			throw new IOException(Messages.getString("ProxyPacket_4")); //$NON-NLS-1$
		}

		/*
		 * Extract rest of the arguments. Each argument is a 1 byte type
		 * followed by the argument value.
		 * 
		 * XXX: all arguments are assumed to be string attributes for now
		 */

		int packetNumArgs = val.getValue();
		fPacketArgs = new String[packetNumArgs];

		for (int i = 0; i < packetNumArgs; i++) {
			switch (packetBytes.get()) {
			case ProtocolUtil.TYPE_STRING_ATTR:
			case ProtocolUtil.TYPE_INTEGER:
			case ProtocolUtil.TYPE_BITSET:
			case ProtocolUtil.TYPE_STRING:
			case ProtocolUtil.TYPE_INTEGER_ATTR:
			case ProtocolUtil.TYPE_BOOLEAN_ATTR:
			default: // ignore argument type for now
				fPacketArgs[i] = ProtocolUtil.decodeStringAttributeType(packetBytes, decoder);
				break;
			}
		}

		return true;
	}

	/**
	 * @since 5.0
	 */
	public void send(WritableByteChannel channel) throws IOException {
		ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

		buffers.add(ByteBuffer.allocate(4)); // buffer for len
		buffers.add(new VarInt(fPacketFlags).getBytes());
		buffers.add(new VarInt(fPacketID).getBytes());
		buffers.add(new VarInt(fPacketTransID).getBytes());
		buffers.add(new VarInt(fPacketArgs.length).getBytes());

		for (String arg : fPacketArgs) {
			ProtocolUtil.encodeStringAttributeType(buffers, arg, fCharset);
		}

		/*
		 * Calculate length of output buffer
		 */
		int len = 0;
		for (ByteBuffer b : buffers) {
			len += b.remaining();
		}
		buffers.get(0).putInt(len - 4).rewind();

		if (debug) {
			System.out.println("SEND -> " + Thread.currentThread().getName()); //$NON-NLS-1$
		}

		fullWrite(channel, buffers, len);
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
