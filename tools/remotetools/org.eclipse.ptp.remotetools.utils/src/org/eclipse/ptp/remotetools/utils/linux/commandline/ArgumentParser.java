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
package org.eclipse.ptp.remotetools.utils.linux.commandline;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ArgumentParser {
	List tokens;
	
	/**
	 * Create a command line representation from the string with a shell command line.
	 * The command line is parsed and split on spaces. Quoted or escaped spaces are preserved..
	 */
	public ArgumentParser(String commandline) {
		this.tokens = parseCommandline(commandline);
	}

	/**
	 * Create a command line representation from an array of strings.
	 * The first element of the array is assumed to be the command, the remaining, the arguments.
	 * The elements are not parsed not (un)escaped., but taked as the are.
	 */
	public ArgumentParser(String tokenArray[]) {
		this(Arrays.asList(tokenArray));
	}
	
	/**
	 * Create a command line representation from an array of strings.
	 * The first element of the list is assumed to be the command, the remaining, the arguments.
	 * The elements are not parsed not (un)escaped., but taked as the are.
	 */
	public ArgumentParser(List tokenList) {
		this.tokens = new ArrayList(tokenList);
	}
	
	/**
	 * Create a command line representation from the command and an array of parameters.
	 * The elements are not parsed not (un)escaped., but taked as the are.
	 */
	public ArgumentParser(String command, String parameterArray[]) {
		this(command, Arrays.asList(parameterArray));
	}
	
	/**
	 * Create a command line representation from the command and an list of parameters.
	 * The elements are not parsed not (un)escaped., but taked as the are.
	 */
	public ArgumentParser(String command, List parameterList) {
		this.tokens = new ArrayList();
		this.tokens.add(command);
		this.tokens.addAll(parameterList);
	}
	
	private static List parseCommandline(String commandline) {
		ArrayList result = new ArrayList();
		StringCharacterIterator iterator = new StringCharacterIterator(commandline);
	    
		for (iterator.first(); iterator.current() != CharacterIterator.DONE; iterator.next()) {
	    	
	    	char currentChar = iterator.current();
	    	
	    	if (Character.isWhitespace(currentChar)) {
	    		// Skip white space
	    		continue;
	    	}
	    	
	    	// Read token
	    	StringBuffer buffer = new StringBuffer();
	    	for (; iterator.current() != CharacterIterator.DONE; iterator.next()) {
	    		char tokenChar = iterator.current();
		    	if (Character.isWhitespace(tokenChar)) {
		    		break;
		    	}
	    		switch (tokenChar) {
	    		case '"':
	    		{
	    			iterator.next(); // Skip quote
	    			while ((iterator.current() != CharacterIterator.DONE) && (iterator.current() != '"')) {
	    				char innerChar = iterator.current();
	    				switch (innerChar) {
	    				case '\\':
	    					char nextChar = iterator.next();
	    					switch (nextChar) {
	    					case CharacterIterator.DONE:
	    						break;
	    					case '"':
	    						// Add the character, but remove the escape
	    						buffer.append(nextChar);
	    						iterator.next();
	    						continue;
	    					default:
	    						// Add the character and keep escape
	    						buffer.append(innerChar);
	    						buffer.append(nextChar);
	    						iterator.next();
	    						continue;
	    					}
	    				default:
	    					buffer.append(innerChar);
	    					iterator.next();
	    					continue;
	    				}
	    			}
	    		}
	    		case '\'':
	    		{
	    			iterator.next(); // Skip quote
	    			while ((iterator.current() != CharacterIterator.DONE) && (iterator.current() != '\'')) {
	    				buffer.append(iterator.current());
	    				iterator.next();
	    			}
	    			continue;
	    		}
	    		case '\\':
	    		{
	    			char nextChar = iterator.next();
	    			switch (nextChar) {
	    			case CharacterIterator.DONE:
	    				break;
	    			case '\n':
	    				// Ignore
	    				continue;
	    			default:
	    				// Add the character, but remove the escape
	    				buffer.append(nextChar);
	    				continue;
	    			}
	    		}
	    		default:
	    			buffer.append(tokenChar);
	    			continue;
	    		}
	    	}
	    	result.add(buffer.toString());
	    }
		return result;
	}
	
	/**
	 * Convert all tokens in a full command line that can be executed in a shell.
	 * @param fullEscape If every special character shall be escaped. If false, only white spaces
	 * are escaped and the shell will interpret the special chars. If true, then all special chars are
	 * quoted.
	 * @return
	 */
	public String getCommandLine(boolean fullEscape) {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = this.tokens.iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			String token = (String) iterator.next();
			if (! first) {
				buffer.append(' ');
			} else {
				first = false;
			}
			buffer.append(escapeToken(token, fullEscape));
		}
		return buffer.toString();
	}
	
	private StringBuffer escapeToken(String token, boolean fullEscape) {
		StringBuffer buffer = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(token);
	    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
	    	if (Character.isWhitespace(c)) {
	    		buffer.append('\\');
	    		buffer.append(c);
	    		continue;
	    	}
	    	switch (c) {
	    	case '(':
	    	case ')':
	    	case '[':
	    	case ']':
	    	case '{':
	    	case '}':
	    	case '|':
	    	case '\\':
	    	case '*':
	    	case '&':
	    	case '^':
	    	case '%':
	    	case '$':
	    	case '#':
	    	case '@':
	    	case '!':
	    	case '~':
	    	case '`':
	    	case '\'':
	    	case '"':
	    	case ':':
	    	case ';':
	    	case '?':
	    	case '>':
	    	case '<':
	    	case '\n':
	    		if (fullEscape) {
	    			buffer.append('\\');
	    		}
		    	buffer.append(c);
	    		continue;
	    	case ' ':
    			buffer.append('\\');
		    	buffer.append(c);
	    		continue;    			
	    	default:
		    	buffer.append(c);
	    		continue;
	    	}
	     }
	    return buffer;
	}

	/**
	 * Returns a List of all entries of the command line.
	 * @return The Array
	 */
	public String[] getTokenArray() {
		return (String[]) this.tokens.toArray(new String[this.tokens.size()]);
	}
	
	/**
	 * Returns a List of all entries of the command line.
	 * @return The List
	 */
	public List getTokenList() {
		return new ArrayList(this.tokens);
	}
	
	/**
	 * Returns the command of the command line, assuming that the first entry is always the command.
	 * @return The command or null if the command lines has no command nor arguments.
	 * @return
	 */
	public String getCommand() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return (String) this.tokens.get(0);
	}
	
	/**
	 * Returns the command of the command line, assuming that the first entry is always the command.
	 * @return The command or null if the command lines has no command nor arguments.
	 * @param fullEscape If every special character shall be escaped. If false, only white spaces
	 * are escaped and the shell will interpret the special chars. If true, then all special chars are
	 * quoted.
	 * @return
	 */
	public String getEscapedCommand(boolean fullEscalpe) {
		if (this.tokens.size() == 0) {
			return null;
		}
		return escapeToken((String) this.tokens.get(0), fullEscalpe).toString();		
	}
	
	/**
	 * Returns a list of all arguments, assuming that the first entry is the command name.
	 * @return The Array or null if the command lines has no command nor arguments.
	 */
	public String[] getParameterArray() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return (String[]) this.tokens.subList(1, this.tokens.size()).toArray(new String[this.tokens.size()-1]);
	}
	
	/**
	 * Returns a list of all arguments, assuming that the first entry is the command name.
	 * @return The List or null if the command lines has no command nor arguments.
	 */
	public List getParameterList() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return new ArrayList(this.tokens.subList(1, this.tokens.size()));
	}
	
	/**
	 * Returns the total number of entries.
	 * @return
	 */
	public int getSize() {
		return this.tokens.size();
	}
	
	/**
	 * Returns a representation of the command line for debug purposes.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = this.tokens.iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			String token = (String) iterator.next();
			if (! first) {
				buffer.append('\n');
			} else {
				first = false;
			}
			buffer.append(token);
		}
		return buffer.toString();
	}
	
	public static void main(String[] args) {
		ArgumentParser parser = new ArgumentParser("teste", new String[] {"oi", "tudo!", "tudo bem?"});
		System.out.println(parser.getCommandLine(false));
		System.out.println(parser.getCommandLine(true));
		
		parser = new ArgumentParser(" teste"); System.out.println(parser);
		parser = new ArgumentParser("teste "); System.out.println(parser);
		parser = new ArgumentParser("teste"); System.out.println(parser);
		parser = new ArgumentParser("teste a"); System.out.println(parser);
		parser = new ArgumentParser("teste  a"); System.out.println(parser);
		parser = new ArgumentParser("teste a "); System.out.println(parser);
		parser = new ArgumentParser("teste	a"); System.out.println(parser);
		parser = new ArgumentParser("teste a	"); System.out.println(parser);
		parser = new ArgumentParser("teste	a	"); System.out.println(parser);
		parser = new ArgumentParser("teste a b"); System.out.println(parser);
		parser = new ArgumentParser("teste a b "); System.out.println(parser);
		parser = new ArgumentParser("teste a b c "); System.out.println(parser);
		parser = new ArgumentParser("teste\\ a b"); System.out.println(parser);
		parser = new ArgumentParser("teste \\ab"); System.out.println(parser);
		parser = new ArgumentParser("teste a\\ b\\ c "); System.out.println(parser);
		parser = new ArgumentParser("teste a\\'c b"); System.out.println(parser);
		parser = new ArgumentParser("teste a\\\"c b"); System.out.println(parser);
		parser = new ArgumentParser("teste a 'b c' d"); System.out.println(parser);		
		parser = new ArgumentParser("teste a 'b\\e' d"); System.out.println(parser);		
		parser = new ArgumentParser("teste a 'b c d"); System.out.println(parser);		
		parser = new ArgumentParser("teste a \"b c\" d"); System.out.println(parser);		
		parser = new ArgumentParser("teste a \"b c\"d"); System.out.println(parser);	
		
		parser = new ArgumentParser(new String[] {}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a", "a"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av", "a"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d", "a"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a", "b b"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"av", "b b"}); System.out.println(parser.getCommandLine(true));
		parser = new ArgumentParser(new String[] {"a d", "b b"}); System.out.println(parser.getCommandLine(true));

	}
	
}
