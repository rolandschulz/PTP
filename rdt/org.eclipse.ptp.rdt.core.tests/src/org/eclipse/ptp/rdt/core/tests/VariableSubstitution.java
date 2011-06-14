/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to substitute variables in a text file.
 * 
 * @author Mike Kucera
 *
 */
public class VariableSubstitution {

	private final Map<String,String> variables;
	
	// (.*?)\$\{(\w+)\}
	// regular expression contains two capturing groups
	private static final String REGEXP = "(.*?)\\$\\{(\\w+)\\}";
	private static final Pattern VARIABLE_PATTERN = Pattern.compile(REGEXP);
	
	
	public VariableSubstitution(Map<String,String> variables) {
		if(variables == null)
			throw new NullPointerException();
		this.variables = variables;
	}

	
	public String substitute(String s) throws IOException {
		StringWriter stringWriter = new StringWriter();
		StringReader stringReader = new StringReader(s);
		try {
			substitute(stringReader, stringWriter);
		} finally {
			stringReader.close();
		}
		// a StringWriter does not need to be closed
		return stringWriter.toString();
	}
	
	
	public void substitute(Reader reader, Writer writer) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		
		String line;
		while((line = bufferedReader.readLine()) != null) {
			substitute(writer, line);
			writer.write('\n');
		}
	}
	
	
	private void substitute(Writer writer, String line) throws IOException {
		Matcher matcher = VARIABLE_PATTERN.matcher(line);

		if(!matcher.find()) {// no variables found in this line
			writer.write(line);
			return;
		}
		
		int end;
		do {
			end = matcher.end();
			writer.write(matcher.group(1)); // first group is the text before the variable
			
			String varName = matcher.group(2); // second group is the variable name
			String varValue = variables.get(varName);
			
			if(varValue == null)
				throw new IOException("Unknown variable: ${" + varName + "}");
			
			writer.write(varValue);
			
		} while(matcher.find());
		
		
		writer.write(line.substring(end));
	}
	
	
}
