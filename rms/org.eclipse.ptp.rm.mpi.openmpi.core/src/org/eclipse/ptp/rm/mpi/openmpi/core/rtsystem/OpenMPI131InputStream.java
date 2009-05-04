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
 * Attempt to clean up quasi-XML output by 1.3.1 and greater. This is easier
 * than 1.3.0 as the output is reasonably well formed XML. All that is
 * missing is the root element.
 */
public class OpenMPI131InputStream extends FilterInputStream {
	private final static String startTag = "<ompi>"; //$NON-NLS-1$
	private final static String endTag = "</ompi>"; //$NON-NLS-1$
	
	private final static int PROLOG = 0;
	private final static int XML = 1;
	private final static int EPILOG = 2;
	private final static int EOF = 3;

	private int state = PROLOG;
	
	private StringBuffer extra = new StringBuffer(6);
	
	protected OpenMPI131InputStream(InputStream in) {
		super(in);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		while (state != EOF) {
			switch (state) {
			case PROLOG:
				extra.insert(0, startTag);
				state = XML;
				break;
				
			case XML:
				if (extra.length() > 0) {
					int ch = extra.charAt(0);
					extra.deleteCharAt(0);
					return ch;
				}
				
				int ch = super.read();

				if (ch < 0) {
					extra.append(endTag);
					state = EPILOG;
				} else {
					return ch;
				}
				break;
			
			case EPILOG:
				if (extra.length() > 0) {
					ch = extra.charAt(0);
					extra.deleteCharAt(0);
					return ch;
				}
				
				state = EOF;
				break;
			}
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
		switch (state) {
		case PROLOG:
			return startTag.length();
			
		case XML:
			return extra.length() + endTag.length() + super.available();
			
		case EPILOG:
			return extra.length();
		}
		
		return 0;
	}
	
}
