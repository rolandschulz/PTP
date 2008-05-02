/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert a string containing a command and arguments separated by whitespace into an
 * array of strings for each token. Preserve whitespace if the token is
 * quoted.
 */
public class ArgumentParser {
	private String cmdLine;
	private List<String> arguments = null;
	
	public ArgumentParser(String cmdLine) {
		this.cmdLine = cmdLine;
	}
	
	/**
	 * Parse the command line into arguments.
	 * 
	 * @return string array containing individual arguments
	 */
	public List<String> getArguments() {
		if (arguments != null) {
			return arguments;
		}
		
		List<String> result = new ArrayList<String>();
		StreamTokenizer tokens = new StreamTokenizer(new StringReader(cmdLine));
		tokens.resetSyntax();
		tokens.wordChars('a', 'z');
		tokens.wordChars('A', 'Z');
		tokens.wordChars('0', '9');
		tokens.wordChars('+', '/');
		tokens.wordChars(':', ':');
		tokens.wordChars('=', '=');
		tokens.wordChars('_', '_');
		tokens.whitespaceChars(0, ' ');
		tokens.quoteChar('"');
		tokens.quoteChar('\'');
		
		try {
			while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
				result.add(tokens.sval);
			}
			arguments = result;
		} catch (IOException e) {
		}
		
		return arguments;
	}
}
