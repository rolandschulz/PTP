/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Attempt to clean up quasi-XML output by 1.3.0
 * 
 * We know the following:
 * - the XML is malformed because it doesn't include a root element
 * - XML between the <map> and </map> tags should be well formed
 * - The XML finishes after the </map> and there may be additonal output
 * 
 * We need to:
 * - inject a root element
 * - ignore everything after the last XML </map> tag
 * 
 * To simplify the parsing (so we don't need to build a full XML parser) we assume
 * that:
 * - the XML begins with a <map> element
 * - there is only one <map> element
 * - the final XML tag is </map>
 */
public class OpenMPI130InputStream extends FilterInputStream {
	private final static int PROLOG = 0;
	private final static int START_TAG = 1;
	private final static int XML = 2;
	private final static int EPILOG = 3;

	private int state = PROLOG;
	
	private StringBuffer prolog = new StringBuffer();
	private StringBuffer epilog = new StringBuffer(6);
	
	protected OpenMPI130InputStream(InputStream in) {
		super(in);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		while (state != EPILOG) {
			int ch = super.read();
			if (ch < 0) {
				return -1;
			}
			switch (state) {
			case PROLOG:
				// Ignore anything except a less-than
				if (ch == '<') {
					state = START_TAG;
					prolog.append((char)ch);
				}
				break;
				
			case START_TAG:
				// Find matching greater-than. If we encounter
				// another less-than, assume that this is the new 
				// starting point
				switch (ch) {
				case '<':
					prolog.delete(0, prolog.length());
					break;
					
				case '>':
					state = XML;
					prolog.insert(0, "<ompi>"); //$NON-NLS-1$
					break;
				}
				prolog.append((char)ch);
				break;
			
			case XML:
				epilog.append((char)ch);
				ch = epilog.charAt(0);
				if (epilog.length() >= 6) {
					if (epilog.substring(epilog.length()-6).equals("</map>")) { //$NON-NLS-1$
						epilog.append("</ompi>"); //$NON-NLS-1$
						state = EPILOG;
						break;
					}
				}
				if (prolog.length() > 0) {
					ch = prolog.charAt(0);
					prolog.deleteCharAt(0);
					return ch;
				}
				epilog.deleteCharAt(0);
				return ch;
			}
		}
		
		if (prolog.length() > 0) {
			int ch = prolog.charAt(0);
			prolog.deleteCharAt(0);
			return ch;
		}
		if (epilog.length() > 0) {
			int ch = epilog.charAt(0);
			epilog.deleteCharAt(0);
			return ch;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}

		int pos = off;
		
		for (int i = 0; i < len && available() > 0; i++) {
			int ch = read();
			if (ch < 0) {
				return -1;
			}
			b[pos++] = (byte) (ch & 0xff);
		}
		
		return pos - off;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return state == PROLOG ? 1 : prolog.length() + epilog.length();
	}
	
}