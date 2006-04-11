/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.hover;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Clement chu
 * 
 */
public abstract class SubstitutionTextReader extends SingleCharReader {
	protected static final String LINE_DELIM= System.getProperty("line.separator", "\n");

	private Reader fReader;
	protected boolean fWasWhiteSpace;
	private int fCharAfterWhiteSpace;

	/**
	 * Tells whether white space characters are skipped.
	 */
	private boolean fSkipWhiteSpace= true;

	private boolean fReadFromBuffer;
	private StringBuffer fBuffer;
	private int fIndex;

	/** Constructor
	 * @param reader
	 */
	protected SubstitutionTextReader(Reader reader) {
		fReader= reader;
		fBuffer= new StringBuffer();
		fIndex= 0;
		fReadFromBuffer= false;
		fCharAfterWhiteSpace= -1;
		fWasWhiteSpace= true;
	}

	/**
	 * Implement to compute the substitution for the given character and
	 * if necessary subsequent characters. Use <code>nextChar</code>
	 * to read subsequent characters.
	 */
	protected abstract String computeSubstitution(int c) throws IOException;
	/**
	 * @return the internal reader.
	 */
	protected Reader getReader() {
		return fReader;
	}
	/**
	 * @return the next character.
	 * @throws IOException
	 */
	protected int nextChar() throws IOException {
		fReadFromBuffer= (fBuffer.length() > 0);
		if (fReadFromBuffer) {
			char ch= fBuffer.charAt(fIndex++);
			if (fIndex >= fBuffer.length()) {
				fBuffer.setLength(0);
				fIndex= 0;
			}
			return ch;
		} else {
			int ch= fCharAfterWhiteSpace;
			if (ch == -1) {
				ch= fReader.read();
			}
			if (fSkipWhiteSpace && Character.isWhitespace((char)ch)) {
				do {
					ch= fReader.read();
				} while (Character.isWhitespace((char)ch));
				if (ch != -1) {
					fCharAfterWhiteSpace= ch;
					return ' ';
				}
			} else {
				fCharAfterWhiteSpace= -1;
			}
			return ch;
		}
	}
	/* (non-Javadoc)
	 * @see java.io.Reader#read()
	 */
	public int read() throws IOException {
		int c;
		do {
			c= nextChar();
			while (!fReadFromBuffer) {
				String s= computeSubstitution(c);
				if (s == null)
					break;
				if (s.length() > 0)
					fBuffer.insert(0, s);
				c= nextChar();
			}

		} while (fSkipWhiteSpace && fWasWhiteSpace && (c == ' '));
		fWasWhiteSpace= (c == ' ' || c == '\r' || c == '\n');
		return c;
	}
    /* (non-Javadoc)
     * @see java.io.Reader#ready()
     */
    public boolean ready() throws IOException {
		return fReader.ready();
	}
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		fReader.close();
	}
	/* (non-Javadoc)
	 * @see java.io.Reader#reset()
	 */
	public void reset() throws IOException {
		fReader.reset();
		fWasWhiteSpace= true;
		fCharAfterWhiteSpace= -1;
		fBuffer.setLength(0);
		fIndex= 0;
	}
	/** Set skip white space
	 * @param state
	 */
	protected final void setSkipWhitespace(boolean state) {
		fSkipWhiteSpace= state;
	}
	/** Is skip white space
	 * @return
	 */
	protected final boolean isSkippingWhitespace() {
		return fSkipWhiteSpace;
	}
}
