/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.utils.vt100;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper state machine that parses a CSI escape sequence. They are formed by a
 * list of numeric parameters, separated by ";" and terminated by a non digit
 * character. The parameters are positional and ordered. It is not clear if a
 * parameter may be empty, therefore the parser accepts empty arguments. This
 * character defines the escape sequence type, given by the last non digit
 * character, that is the meaning of the escape sequence and how the parameters
 * shall be interpreted.
 * <p>
 * Currently, the parser ignores the escape sequence type and simply remembers
 * the parameters.
 * <p>
 * Characters are given one by one to be parsed. The parser stops as soon as a
 * valid escape sequence is found or when the character sequence is known to be
 * invalid. Appropriate flags are set to test these situations and the
 * respective getter may be used to test the flags.
 * 
 * @author Daniel Felix Ferber
 */
class CSIParameter {
	/** If the current parsed escape sequence is valid. */
	private boolean isValid;

	/**
	 * If the current parsed escape sequence is valid and all parameters from
	 * the escape code were read.
	 */
	private boolean isComplete;

	/** Ordered list of parameters that were parsed in the escape sequence. */
	private List parameters;

	/**
	 * Next candidate parameter that is being parsed. If valid, the parameter
	 * will be added to the list.
	 */
	private StringBuffer nextCandidateParameter = null;

	/**
	 * In future, will inform the type of the escape sequence. Now it is being
	 * ignored. The type is a character that defines the meaning for the escape
	 * sequence and the parameters.
	 */
	byte escapeSequenceType;

	/** Default constructor. */
	public CSIParameter() {
		parameters = new ArrayList();
		isValid = true;
		isComplete = false;
		escapeSequenceType = 0;
	}

	/**
	 * Parses one more byte that was read from the VT100 stream. The character,
	 * concatenated to the previously received characters, is supposed to form a
	 * valid escape sequence. If the character finishes the escape sequence, or if the
	 * character violates the parsing rules, then the appropriate flags are set.
	 * 
	 * @param b
	 *            the next character for the escape sequence.
	 */
	void receive(byte b) {
		if (!isValid) {
			/*
			 * There is nothing to do if the sequence is invalid.
			 */
			return;
		} else if (isComplete) {
			/*
			 * No more bytes are expected for the sequence. Therefore, the
			 * sequence is not valid anymore.
			 */
			isValid = false;
			isComplete = false;
		} else if (b == ';') {
			/*
			 * Add a new parameter only if the current parameter is not empty.
			 * Does not allow more than 30 parameters, since no reasonable
			 * escape code has more than 30.
			 */
			if (parameters.size() > 30) {
				isValid = false;
			} else {
				if (nextCandidateParameter != null) {
					parameters.add(nextCandidateParameter.toString());
				}
				nextCandidateParameter = new StringBuffer();
			}
		} else if (Character.isDigit((char) b)) {
			/*
			 * All parameters are small numbers.
			 */
			if (nextCandidateParameter == null) {
				nextCandidateParameter = new StringBuffer();
			} else {
				if (nextCandidateParameter.length() > 10) {
					isValid = false;
				} else {
					nextCandidateParameter.append(b);
				}
			}
		} else {
			/*
			 * The escape code is always terminated with a non digit character.
			 * This characters defines the meaning of the escape code and how
			 * the parameters shall be interpreted. Currently, the character is
			 * not being verified, nor being interpreted.
			 */
			if (nextCandidateParameter != null) {
				parameters.add(nextCandidateParameter.toString());
			}
			escapeSequenceType = b;
			isComplete = true;
		}
	}

	/**
	 * The type of the read escape sequence, if valid, or undefine if otherwise.
	 * 
	 * @return the escape sequence type
	 */
	public byte getType() {
		return escapeSequenceType;
	}

	/**
	 * Test if the escape sequence was completely read and requires no further
	 * bytes for parsing.
	 * 
	 * @return true if the escape sequence is complete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * Test if the escape sequence did not violate any parsing rule yet.
	 * 
	 * @return true if the escape sequence if valid
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * How many parameters were parsed.
	 * 
	 * @return the number of parameters
	 */
	public int size() {
		return parameters.size();
	}

	/**
	 * Returns the i-th parameter that was parsed.
	 * <p>
	 * In future, the parameter might be returned as an integer, since all
	 * parameters are expected to be integer numbers.
	 * @return the i-th parameter
	 */
	public String elementAt(int index) {
		return (String) parameters.get(index);
	}

	/**
	 * Returns a iterator to the parameters.
	 * 
	 * @return the iterator.
	 */
	public Iterator iterator() {
		return parameters.iterator();
	}
	
	@Override
	public String toString() {
		return parameters.toString();
	}
}
