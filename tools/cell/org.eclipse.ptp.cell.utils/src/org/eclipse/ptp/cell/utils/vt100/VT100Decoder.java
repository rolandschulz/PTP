/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.utils.vt100;

import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * Decodes a stream of characters from a VT100 terminal. On each element
 * recognized in the stream, a listener is notified about the element. An
 * element might be an escape sequence or a sequence of characters.
 * <p>
 * Currently, only character sequences are supported. Escape codes are ignored.
 * <p>
 * Control Sequence Introducer is interpreted since this will be useful in
 * future, but Operating System Command is completely ignored.
 * 
 * @author dfferber
 * 
 */
public class VT100Decoder {
	// Internal parser states
	/** Receiving text. */
	private static final int PARSER_STATE_TEXT = 1;

	/**
	 * Escape code received. One more by must be read to know if the escape code
	 * is CSI or OSC.
	 */
	private static final int PARSER_STATE_ESCAPE = 2;

	/** Parsing Control Sequence Introducer (ESCAPE + [ ) */
	private static final int PARSER_STATE_CSI = 3;

	/** Parsing Operating Control Introducer (ESCAPE + ] ) */
	private static final int PARSER_STATE_OSC = 4;

	/** Current state of the internal parser. */
	private int currentMode = PARSER_STATE_TEXT;

	/** Character that sinalizes the Control Sequence Introducer (ESCAPE + [ ) */
	private static final byte ESC_CHAR = 0x1B;

	/** Character that sinalizes the Operating System Command (ESCAPE + ] ) */
	private static final byte BELL_CHAR = 0x07;

	/** The escape code that is currently being parsed. */
	private StringBuffer escapeSequence = null;

	/**
	 * Helper state machine that verifies if a candidate sequence of characters
	 * representes a valid CSI escape code.
	 */
	CSIParameter csiParameter = null;

	/**
	 * Listener that is notified about elements recognized in the stream.
	 */
	private VT100Listener listener;

	/** Default constructor. */
	public VT100Decoder(VT100Listener listener) {
		Debug.read();
		this.listener = listener;
	}

	private int BUFFER_SIZE = 1000;

	private byte[] buffer = new byte[BUFFER_SIZE];

	public void receive(byte[] bytes, int length) {
		if (Debug.DEBUG_VT100) {
			Debug.POLICY.trace("Received {0} bytes", length); //$NON-NLS-1$
		}
		int start = 0;
		byte b;

		while (start < length) {
			switch (currentMode) {
			case PARSER_STATE_TEXT:
				if (Debug.DEBUG_VT100) {
					Debug.POLICY.trace("In text mode"); //$NON-NLS-1$
				}
				/*
				 * Read a sequence of characters up to the buffer size, the size
				 * of the input or to the first escape code.
				 */
				int end = start;
				int max_end = start + BUFFER_SIZE;
				if (max_end > length) {
					max_end = length;
				}

				while (end < max_end) {
					if (bytes[end] == ESC_CHAR) {
						currentMode = PARSER_STATE_ESCAPE;
						break;
					}
					end++;
				}

				/*
				 * Notify the listener about the sequence of characters (if
				 * any).
				 */
				int text_length = end - start;
				if (text_length > 0) {
					System.arraycopy(bytes, start, buffer, 0, text_length);
					if (Debug.DEBUG_VT100) {
						Debug.POLICY.trace("Parsed {0} bytes of text.", length); //$NON-NLS-1$
						if (Debug.DEBUG_VT100_MORE) {
							Debug.POLICY.trace(new String(buffer, 0, length));
						}
					}
					try {
						listener.textSequence(buffer, text_length);
					} catch (Exception e) {
						Debug.POLICY.error(Debug.DEBUG_VT100, e);
					}
				}

				start = end;
				if (currentMode == PARSER_STATE_ESCAPE) {
					escapeSequence = new StringBuffer();
					escapeSequence.append(ESC_CHAR);
					start++; // skip ESC char
				}
				break;
			case PARSER_STATE_ESCAPE:
				/*
				 * Test if the escape sequence is CSI or CSC according to the
				 * next byte read.
				 */
				b = bytes[start];
				start++;
				escapeSequence.append((char) b);
				switch (b) {
				case '[':
					currentMode = PARSER_STATE_CSI;
					csiParameter = new CSIParameter();
					break;
				case ']':
					currentMode = PARSER_STATE_OSC;
					// this escape sequence is not implemented now.
					break;
				default:
					currentMode = PARSER_STATE_TEXT;
					break;
				}
				break;
			case PARSER_STATE_CSI:
				b = bytes[start];
				start++;
				escapeSequence.append((char) b);
				csiParameter.receive(b);
				if (!csiParameter.isValid()) {
					/*
					 * The escape sequence is not valid. Interpret it as a sequence of characters.
					 */
					String s = escapeSequence.toString();
					byte sbytes[] = s.getBytes();
					if (Debug.DEBUG_VT100) {
						Debug.POLICY.trace("Parsed {0} bytes of text (initially considered as CSI sequence).", length); //$NON-NLS-1$
						if (Debug.DEBUG_VT100_MORE) {
							Debug.POLICY.trace(new String(buffer, 0, length));
						}
					}
					listener.textSequence(sbytes, sbytes.length);
					currentMode = PARSER_STATE_TEXT;
				} else if (csiParameter.isComplete()) {
					// Simplified implementation. Does not consider CSI type.
					// Simply ignore the sequence.
					if (Debug.DEBUG_VT100) {
						Debug.POLICY.trace("Ignored CSI sequence {0}.", csiParameter.toString()); //$NON-NLS-1$
					}
					currentMode = PARSER_STATE_TEXT;
				}
				break;
			case PARSER_STATE_OSC:
				// Simplified implementation. Read until BELL.
				b = bytes[start];
				start++;
				escapeSequence.append((char) b);
				if (b == BELL_CHAR) {
					if (Debug.DEBUG_VT100) {
						Debug.POLICY.trace("Ignored OSC sequence {0}."); //$NON-NLS-1$
					}
					currentMode = PARSER_STATE_TEXT;
				}
				break;
			default:
				currentMode = PARSER_STATE_TEXT;
			}
		}
	}
}
