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
import org.eclipse.ptp.proxy.util.compression.IDecoder;
import org.eclipse.ptp.proxy.util.compression.IEncoder;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteCompress;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteUncompress;

/**
 * @since 5.0
 */
public class ProxyPacket {
	private static final int PACKET_LENGTH_SIZE = 4;

	/* packets above this size are considered for compression */
	private static final int SMALL_PACKET = 100;

	/*
	 * frequency table will preferably be updated with packets larger than this size. Avoids sending frequency table with small
	 * packets.
	 */
	private static final int LARGE_PACKET = 8192;

	/*
	 * allow compression if difference between original and compressed packet is >= that this
	 */
	private static final int COMPRESSION_DIFF = 100;

	/* update frequency table after processing these no. of bytes */
	private static final int COMPRESSION_UPDATE = 262144;

	/* bit set in flag byte to indicate compression */
	private static final int COMPRESSION_FLAG = 0x40;

	/* bit set in flag byte to indicate presence of compression table */
	private static final int COMPRESSION_TABLE_FLAG = 0x10;

	private boolean debug = false;

	private int fPacketFlags;

	private int fPacketID;
	private int fPacketTransID;
	private String[] fPacketArgs;
	private HuffmanByteCompress compressor;
	private HuffmanByteUncompress uncompressor;
	private final Charset fCharset = Charset.forName("ISO-8859-1"); //$NON-NLS-1$

	private final CharsetEncoder encoder = fCharset.newEncoder();
	private final CharsetDecoder decoder = fCharset.newDecoder();

	public ProxyPacket() {
		fPacketFlags = 0;
	}

	/**
	 * @since 5.0
	 */
	public ProxyPacket(IDecoder uncomp) {
		this();
		if (!(uncomp instanceof HuffmanByteUncompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		uncompressor = (HuffmanByteUncompress) uncomp;
	}

	/**
	 * @since 5.0
	 */
	public ProxyPacket(IEncoder comp) {
		this();
		if (!(comp instanceof HuffmanByteCompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		compressor = (HuffmanByteCompress) comp;
	}

	public ProxyPacket(IProxyCommand cmd) {
		this();
		fPacketID = cmd.getCommandID();
		fPacketTransID = cmd.getTransactionID();
		fPacketArgs = cmd.getArguments();
	}

	/**
	 * @since 5.0
	 */
	public ProxyPacket(IProxyCommand cmd, IDecoder uncomp) {
		this(cmd);
		if (!(uncomp instanceof HuffmanByteUncompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		uncompressor = (HuffmanByteUncompress) uncomp;
	}

	/**
	 * @since 5.0
	 */
	public ProxyPacket(IProxyCommand cmd, IEncoder comp) {
		this(cmd);
		if (!(comp instanceof HuffmanByteCompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		compressor = (HuffmanByteCompress) comp;
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
	 * @since 5.0
	 */
	public ProxyPacket(IProxyEvent event, IDecoder uncomp) {
		this(event);
		if (!(uncomp instanceof HuffmanByteUncompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		uncompressor = (HuffmanByteUncompress) uncomp;
	}

	/**
	 * @since 5.0
	 */
	public ProxyPacket(IProxyEvent event, IEncoder comp) {
		this(event);
		if (!(comp instanceof HuffmanByteCompress)) {
			throw new RuntimeException(Messages.getString("ProxyPacket_6")); //$NON-NLS-1$
		}
		compressor = (HuffmanByteCompress) comp;
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
	 * @return false if the connection has closed (read returns < 0)
	 * @throws IOException
	 *             if a protocol error occurs
	 * 
	 */
	public boolean read(ReadableByteChannel channel) throws IOException {
		/*
		 * First PACKET_LENGTH_SIZE bytes are the length of the event
		 */
		ByteBuffer lengthBytes = ByteBuffer.allocate(PACKET_LENGTH_SIZE);
		if (fullRead(channel, lengthBytes) < 0) {
			return false;
		}
		int len;
		VarInt val;
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
		if (fullRead(channel, packetBytes) < 0) {
			return false;
		}

		if (debug) {
			System.out.println("RECEIVE:[" + len + "] -> " + Thread.currentThread().getName()); //$NON-NLS-1$//$NON-NLS-2$
		}

		/*
		 * Get flags
		 */
		fPacketFlags = packetBytes.get();

		if ((fPacketFlags & COMPRESSION_FLAG) != 0) {
			if (debug) {
				System.out.println("Received compressed packet."); //$NON-NLS-1$
			}
			if (uncompressor == null) {
				throw new IOException(Messages.getString("ProxyPacket_5")); //$NON-NLS-1$
			}
			if ((fPacketFlags & COMPRESSION_TABLE_FLAG) != 0) {
				uncompressor.notifyFrequencyUpdate();
			}
			packetBytes = uncompressor.apply(packetBytes.slice());
		}

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
		 * Extract rest of the arguments. Each argument is a 1 byte type followed by the argument value.
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
		buffers.add(ByteBuffer.allocate(1));
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
		buffers.get(1).put((byte) fPacketFlags).rewind();

		if (debug) {
			System.out.println("SEND -> " + Thread.currentThread().getName()); //$NON-NLS-1$
		}

		/* before sending try to compress */
		if (len > SMALL_PACKET && compressor != null) {
			buffers = compressPacket(buffers, len);
			len = buffers.get(0).getInt() + 4;
			buffers.get(0).rewind();
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

	/**
	 * Compresses the given buffers using registered compressor.
	 * 
	 * @param buffers
	 *            The buffers to compress
	 * @param len
	 *            The length of the input buffer
	 * @return ArrayList containing compressed ByteBuffers.
	 */
	private ArrayList<ByteBuffer> compressPacket(ArrayList<ByteBuffer> buffers, int len) {
		ByteBuffer compressedPacket;
		final ByteBuffer bb = ByteBuffer.allocate(len);
		List<ByteBuffer> subList;
		int flagByte;
		int limit;
		boolean updated = false;

		if (compressor == null) {
			return buffers;
		}

		flagByte = buffers.get(1).get();
		buffers.get(1).rewind();

		subList = buffers.subList(2, buffers.size());
		for (final ByteBuffer b : subList) {
			bb.put(b);
			b.rewind();
		}
		limit = bb.limit();

		final int tableUpdateCount = compressor.getBytesAccumulated();
		if (tableUpdateCount > COMPRESSION_UPDATE) {
			if (len > LARGE_PACKET || tableUpdateCount > (COMPRESSION_UPDATE << 1)) {
				compressor.updateHuffmanTable();
			}
		}

		/* compress the packet */

		updated = compressor.getIncludeTableFlag();
		compressedPacket = compressor.apply(bb);

		/*
		 * If compression is not good enough, return original packet. If frequency table is updated, send compressed packet anyway.
		 */
		if (limit - compressedPacket.limit() > COMPRESSION_DIFF || updated) {
			flagByte |= COMPRESSION_FLAG;
			if (updated) {
				flagByte |= COMPRESSION_TABLE_FLAG;
			}
			compressedPacket.rewind();
			buffers = new ArrayList<ByteBuffer>(3);
			buffers.add(ByteBuffer.allocate(4));
			buffers.add(ByteBuffer.allocate(1));
			buffers.add(compressedPacket);
			/* 1 byte for flag */
			buffers.get(0).putInt(compressedPacket.limit() + 1).rewind();
			buffers.get(1).put((byte) flagByte).rewind();
			if (debug) {
				System.out.println("Original size: " + bb.limit()); //$NON-NLS-1$
				System.out.println("New size:" + compressedPacket.limit()); //$NON-NLS-1$
			}
		}
		return buffers;
	}

	/**
	 * Read a full buffer from the socket. Guaranteed to read buf.remaining() bytes from the channel.
	 * 
	 * FIXME: Can this block if there is nothing available on the channel? If so, then there should be some kind of timeout to
	 * prevent the UI from hanging.
	 * 
	 * @param buf
	 *            buffer containing the result of the read
	 * @returns the number of bytes read, or -1 on EOF
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private int fullRead(ReadableByteChannel channel, ByteBuffer buf) throws IOException {
		int n;
		int len = buf.remaining();
		buf.clear();
		while ((n = channel.read(buf)) < len) {
			if (n <= 0) {
				break;
			}
			len -= n;
		}
		buf.flip();
		return n;
	}

	/**
	 * Write an array of buffers to the socket. Guarantees to write the whole buffer array.
	 * 
	 * NOTE: Originally used GatherByteChannel but the performance is pathetic. This implementation requires a buffer copy, which
	 * should be eliminated.
	 * 
	 * FIXME: Can this block? If so, then there should be some kind of timeout to prevent the UI from hanging.
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
	 * Return some default values for the Huffman frequency table based on experimentation.
	 * 
	 * @since 5.0
	 */
	public static int[] getDefaultHuffmanTable() {
		final int frequencyTable[] = { 422, 420, 418, 416, 414, 412, 410, 408, 406, 404, 402, 400, 398, 396, 394, 392, 390, 388,
				386, 384, 382, 380, 378, 376, 374, 372, 370, 368, 366, 364, 362, 1802, 450, 448, 446, 444, 442, 440, 438, 436, 434,
				432, 430, 428, 426, 424, 1652, 1550, 1500, 1450, 1400, 1350, 1300, 1250, 1200, 1150, 1100, 1050, 1000, 950, 900,
				850, 800, 750, 4500, 2950, 2900, 2850, 4400, 2800, 2750, 2700, 4300, 2700, 2650, 2600, 2550, 2500, 4200, 2450,
				2400, 2350, 2300, 2250, 2200, 2150, 2100, 2050, 2000, 1950, 1900, 1850, 1800, 500, 1700, 1650, 5000, 4000, 3950,
				3900, 4900, 3850, 3800, 3750, 4800, 3700, 3650, 3600, 3550, 3500, 4700, 3450, 3400, 3350, 3300, 3250, 4600, 3200,
				3150, 3100, 3050, 3000, 750, 700, 650, 600, 550, 360, 358, 356, 354, 352, 350, 348, 346, 344, 342, 340, 338, 336,
				334, 332, 330, 328, 326, 324, 322, 320, 318, 316, 314, 312, 310, 308, 306, 304, 302, 300, 298, 296, 294, 292, 290,
				288, 286, 284, 282, 280, 278, 276, 274, 272, 270, 268, 266, 264, 262, 260, 258, 256, 254, 252, 250, 248, 246, 244,
				242, 240, 238, 236, 234, 232, 230, 228, 226, 224, 222, 220, 218, 216, 214, 212, 210, 208, 206, 204, 202, 200, 198,
				196, 194, 192, 190, 188, 186, 184, 182, 180, 178, 176, 174, 172, 170, 168, 166, 164, 162, 160, 158, 156, 154, 152,
				150, 148, 146, 144, 142, 140, 138, 136, 134, 132, 130, 128, 126, 124, 122, 120, 118, 116, 114, 112, 110, 108, 106,
				104 };
		return frequencyTable;
	}
}
