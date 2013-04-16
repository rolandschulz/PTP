/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.make.core.scannerconfig.offline;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;


/**
 * @since 4.1
 */
/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.make.xlc.core/src
 * Class: org.eclipse.cdt.make.xlc.core.scannerconfig.XLCPerFileBuildOutputParser
 * Version:
 */	
@SuppressWarnings("restriction")
public class OfflineXLCBuildOutputParser implements IScannerInfoConsoleParser{

	
	List<String> fileExtensionList;
	
	protected static final String[] COMPILER_INVOCATION = { "xlc", "xlC" };//$NON-NLS-1$ //$NON-NLS-2$

	protected static final String DASHIDASH = "-I-"; //$NON-NLS-1$
	protected static final String DASHI = "-I"; //$NON-NLS-1$
	protected static final String DASHD = "-D"; //$NON-NLS-1$

	
	protected IScannerInfoCollector fCollector;
	protected IPath fWorkingDir;
	protected OfflineBuildOutputParserUtility fUtility;

	protected boolean fBMultiline = false;
	protected String fSMultiline = ""; //$NON-NLS-1$

	protected String[] fCompilerCommands = { "xlc", "xlC" }; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @return Returns the fCollector.
	 */
	protected IScannerInfoCollector getCollector() {
		return fCollector;
	}

	

	/**
	 * Returns array of additional compiler commands to look for
	 * 
	 * @return String[]
	 */
	protected String[] computeCompilerCommands() {
		
		return COMPILER_INVOCATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#processLine
	 * (java.lang.String)
	 */
	public boolean processLine(String line) {
		boolean rc = false;
		int lineBreakPos = line.length() - 1;
		char[] lineChars = line.toCharArray();
		while (lineBreakPos >= 0 && Character.isWhitespace(lineChars[lineBreakPos])) {
			lineBreakPos--;
		}
		if (lineBreakPos >= 0) {
			if (lineChars[lineBreakPos] != '\\' || (lineBreakPos > 0 && lineChars[lineBreakPos - 1] == '\\')) {
				lineBreakPos = -1;
			}
		}
		// check for multiline commands (ends with '\')
		if (lineBreakPos >= 0) {
			fSMultiline += line.substring(0, lineBreakPos);
			fBMultiline = true;
			return rc;
		}
		if (fBMultiline) {
			line = fSMultiline + line;
			fBMultiline = false;
			fSMultiline = ""; //$NON-NLS-1$
		}
		line = line.trim();
		try{
			OfflineTraceUtil.outputTrace("XLCBuildOutputParser parsing line: [", line, "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}catch(NoClassDefFoundError e){
			//no problem, as this may be called from a standalone indexer
		}
		// make\[[0-9]*\]: error_desc
		int firstColon = line.indexOf(':');
		String make = line.substring(0, firstColon + 1);
		if (firstColon != -1 && make.indexOf("make") != -1) { //$NON-NLS-1$
			boolean enter = false;
			String msg = line.substring(firstColon + 1).trim();
			if ((enter = msg.startsWith("Entering directory")) || //$NON-NLS-1$
					(msg.startsWith("Leaving directory"))) { //$NON-NLS-1$
				int s = msg.indexOf('`');
				int e = msg.indexOf('\'');
				if (s != -1 && e != -1) {
					String dir = msg.substring(s + 1, e);
					if (getUtility() != null) {
						getUtility().changeMakeDirectory(dir, getDirectoryLevel(line), enter);
					}
					return rc;
				}
			}
		}
		// call sublclass to process a single line
		return processSingleLine(line.trim());
	}

	protected synchronized OfflineBuildOutputParserUtility getUtility() {
		

		return fUtility;
	}

	protected int getDirectoryLevel(String line) {
		int s = line.indexOf('[');
		int num = 0;
		if (s != -1) {
			int e = line.indexOf(']');
			String number = line.substring(s + 1, e).trim();
			try {
				num = Integer.parseInt(number);
			} catch (NumberFormatException exc) {
			}
		}
		return num;
	}



	/**
	 * Tokenizes a line into an array of commands. Commands are separated by
	 * ';', '&&' or '||'. Tokens are separated by whitespace unless found inside
	 * of quotes, back-quotes, or double quotes. Outside of single-, double- or
	 * back-quotes a backslash escapes white-spaces, all quotes, the backslash,
	 * '&' and '|'. A backslash used for escaping is removed. Quotes other than
	 * the back-quote plus '&&', '||', ';' are removed, also.
	 * 
	 * @param line
	 *            to tokenize
	 * @return array of commands
	 */
	protected String[][] tokenize(String line, boolean escapeInsideDoubleQuotes) {
		ArrayList<String[]> commands = new ArrayList<String[]>();
		ArrayList<String> tokens = new ArrayList<String>();
		StringBuffer token = new StringBuffer();

		final char[] input = line.toCharArray();
		boolean nextEscaped = false;
		char currentQuote = 0;
		for (int i = 0; i < input.length; i++) {
			final char c = input[i];
			final boolean escaped = nextEscaped;
			nextEscaped = false;

			if (currentQuote != 0) {
				if (c == currentQuote) {
					if (escaped) {
						token.append(c);
					} else {
						if (c == '`') {
							token.append(c); // preserve back-quotes
						}
						currentQuote = 0;
					}
				} else {
					if (escapeInsideDoubleQuotes && currentQuote == '"' && c == '\\') {
						nextEscaped = !escaped;
						if (escaped) {
							token.append(c);
						}
					} else {
						if (escaped) {
							token.append('\\');
						}
						token.append(c);
					}
				}
			} else {
				switch (c) {
				case '\\':
					if (escaped) {
						token.append(c);
					} else {
						nextEscaped = true;
					}
					break;
				case '\'':
				case '"':
				case '`':
					if (escaped) {
						token.append(c);
					} else {
						if (c == '`') {
							token.append(c);
						}
						currentQuote = c;
					}
					break;
				case ';':
					if (escaped) {
						token.append(c);
					} else {
						endCommand(token, tokens, commands);
					}
					break;
				case '&':
				case '|':
					if (escaped || i + 1 >= input.length || input[i + 1] != c) {
						token.append(c);
					} else {
						i++;
						endCommand(token, tokens, commands);
					}
					break;

				default:
					if (Character.isWhitespace(c)) {
						if (escaped) {
							token.append(c);
						} else {
							endToken(token, tokens);
						}
					} else {
						if (escaped) {
							token.append('\\'); // for windows put backslash
												// back onto the token.
						}
						token.append(c);
					}
				}
			}
		}
		endCommand(token, tokens, commands);
		return commands.toArray(new String[commands.size()][]);
	}

	protected void endCommand(StringBuffer token, ArrayList<String> tokens, ArrayList<String[]> commands) {
		endToken(token, tokens);
		if (!tokens.isEmpty()) {
			commands.add(tokens.toArray(new String[tokens.size()]));
			tokens.clear();
		}
	}

	protected void endToken(StringBuffer token, ArrayList<String> tokens) {
		if (token.length() > 0) {
			tokens.add(token.toString());
			token.setLength(0);
		}
	}

	protected boolean processSingleLine(String line) {
		boolean rc = false;
		String[][] tokens = tokenize(line, true);
		for (int i = 0; i < tokens.length; i++) {
			String[] command = tokens[i];
			if (processCommand(command)) {
				rc = true;
			} else { // go inside quotes, if the compiler is called per wrapper
						// or shell script
				for (int j = 0; j < command.length; j++) {
					String[][] subtokens = tokenize(command[j], true);
					for (int k = 0; k < subtokens.length; k++) {
						String[] subcommand = subtokens[k];
						if (subcommand.length > 1) { // only proceed if there is
														// any additional info
							if (processCommand(subcommand)) {
								rc = true;
							}
						}
					}
				}
			}
		}
		return rc;
	}

	protected int findCompilerInvocation(String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			final String token = tokens[i].toLowerCase();
			final int searchFromOffset = Math.max(token.lastIndexOf('/'), token.lastIndexOf('\\')) + 1;
			for (int j = 0; j < fCompilerCommands.length; j++) {
				if (token.indexOf(fCompilerCommands[j], searchFromOffset) != -1) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public OfflineXLCBuildOutputParser(String baseDirectory, String buildDirectory, IScannerInfoCollector collector, List<String> fileExtensionList){
		
		if(buildDirectory!=null && buildDirectory.length()>0){
			fUtility = new OfflineBuildOutputParserUtility(baseDirectory, buildDirectory);
		}else{
			fUtility = new OfflineBuildOutputParserUtility(baseDirectory, null);
		}
		
		this.fileExtensionList = fileExtensionList;
		this.fCollector = collector;
	}
		
	protected List<String> getFileExtensionsList() {
		return this.fileExtensionList;
	}
	
	protected String[] getFileExtensions() {
		return getFileExtensionsList().toArray((new String[0]));
	}

	protected boolean processCommand(String[] tokens) {
		try {
			
		
		int compilerInvocationIndex = findCompilerInvocation(tokens);
		if (compilerInvocationIndex < 0) {
			return false;
		}

		
		int extensionsIndex = -1;
		boolean found = false;
		String filePath = null;
		
		
		int automakeSrcIndex = findAutoMakeSourceIndex(tokens);
		if(automakeSrcIndex != -1) {
			filePath = getAutoMakeSourcePath(tokens[automakeSrcIndex]);
			int k = filePath.lastIndexOf('.');
			if (k != -1 && (filePath.length() - k < 5)) {
				String fileExtension = filePath.substring(k+1);
				extensionsIndex = getFileExtensionsList().indexOf(fileExtension);
				if (extensionsIndex != -1) {
					found = true;
				}
			}
		}
		
		if (!found) {
			for (int i = compilerInvocationIndex + 1; i < tokens.length; i++) {
				String token = tokens[i];
				int k = token.lastIndexOf('.');
				if (k != -1 && (token.length() - k < 5)) {
					String fileExtension = token.substring(k+1);
					extensionsIndex = getFileExtensionsList().indexOf(fileExtension);
					if (extensionsIndex != -1) {
						filePath = token;
						found = true;
						break;
					}
				}
			}
		}
		
		if (!found) {
			OfflineTraceUtil.outputTrace("Error identifying file name :1", tokens, OfflineTraceUtil.EOL); //$NON-NLS-1$
			return false;
		}
		
		if (filePath.indexOf(getFileExtensions()[extensionsIndex]) == -1) {
			OfflineTraceUtil.outputTrace("Error identifying file name :2", tokens, OfflineTraceUtil.EOL); //$NON-NLS-1$
			return false;
		}
		if (getUtility() != null) {
			IPath pFilePath = fUtility.getAbsolutePath(filePath);
					
			String shortFileName = pFilePath.removeFileExtension().lastSegment();

			for (int i = compilerInvocationIndex + 1; i < tokens.length; i++) {
				String token = tokens[i];
				if (token.equals("-include")) { //$NON-NLS-1$
					++i;
				} else if (token.equals("-imacros")) { //$NON-NLS-1$
					++i;
				} else if (token.equals(filePath)) {
					tokens[i] = "LONG_NAME"; //$NON-NLS-1$
				} else if (token.startsWith(shortFileName)) {
					tokens[i] = token.replaceFirst(shortFileName, "SHORT_NAME"); //$NON-NLS-1$
				}
			}

			File file = new File(pFilePath.toOSString());
			
			if (true /*file != null*/) {
				CCommandDSC cmd = getUtility().getNewCCommandDSC(tokens, compilerInvocationIndex, extensionsIndex > 0);
				List<CCommandDSC> cmdList = new CopyOnWriteArrayList<CCommandDSC>();
				cmdList.add(cmd);
				Map<ScannerInfoTypes, List<CCommandDSC>> sc = new HashMap<ScannerInfoTypes, List<CCommandDSC>>(1);
				sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);
				getCollector().contributeToScannerConfig(file, sc);
				
			} 
		}
 		return true;
		
		}
		catch(Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	protected String getAutoMakeSourcePath(String string) {
		// path may be enclosed in single quotes
		int firstQuoteIndex = string.indexOf('\'');
		int lastQuoteIndex = string.lastIndexOf('\'');
		if(firstQuoteIndex != -1 && lastQuoteIndex != -1)
			return string.substring(firstQuoteIndex, lastQuoteIndex);
		else {
			// just take everything after the equals sign
			int equalsIndex = string.indexOf('=');
			if(equalsIndex != -1 && equalsIndex < string.length())
				return string.substring(equalsIndex+1);
			
		}
		return null;
	}

	protected int findAutoMakeSourceIndex(String[] tokens) {
		for (int i = 0; i < tokens.length; i++) {
			final String token = tokens[i].toLowerCase();
			if(token.indexOf("source=") != -1) //$NON-NLS-1$
				return i;
		}
		return -1;
	}



	public void shutdown() {
		// TODO Auto-generated method stub
		
	}



	public void startup(IProject project, IPath workingDirectory,
			IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
