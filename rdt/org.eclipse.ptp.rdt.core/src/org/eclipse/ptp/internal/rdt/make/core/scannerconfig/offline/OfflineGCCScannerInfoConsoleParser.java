/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Martin Oberhuber (Wind River Systems) - bug 155096
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.make.core.scannerconfig.offline;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @since 4.1
 */
/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.make.core/src
 * Class: org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerInfoConsoleParser
 * Version:
 */	
public class OfflineGCCScannerInfoConsoleParser implements IScannerInfoConsoleParser{

		protected OfflineBuildOutputParserUtility fUtil = null;
		private String fDefaultMacroDefinitionValue= "1"; //$NON-NLS-1$
		protected IScannerInfoCollector collector;
		
		protected static final String DASHIDASH= "-I-"; //$NON-NLS-1$
	    protected static final String DASHI= "-I"; //$NON-NLS-1$
	    protected static final String DASHD= "-D"; //$NON-NLS-1$
	    
	    private boolean bMultiline = false;
	    private String sMultiline = ""; //$NON-NLS-1$
	    
	    protected static final String[] COMPILER_INVOCATION = {
            "gcc", "g++", "cc", "c++" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    };
	    
		protected String[] fCompilerCommands;

		public OfflineGCCScannerInfoConsoleParser(String baseDirectory, String buildDirectory, IScannerInfoCollector collector){
			
			if(buildDirectory!=null && buildDirectory.length()>0){
				fUtil = new OfflineBuildOutputParserUtility(baseDirectory, buildDirectory);
			}else{
				fUtil = new OfflineBuildOutputParserUtility(baseDirectory, null);
			}
			
			this.fCompilerCommands=computeCompilerCommands();
			this.collector = collector;
		}
		
	    /**
	     * @return Returns the collector.
	     */
	    protected IScannerInfoCollector getCollector() {
	        return collector;
	    }
	    protected boolean processCommand(String[] tokens) {
	        int compilerInvocationIdx= findCompilerInvocation(tokens);
	        if (compilerInvocationIdx<0) {
	        	return false;
	        }
	        
	        if (compilerInvocationIdx+1 >= tokens.length) {
	        	return false;
	        }

	        // Recognized gcc or g++ compiler invocation
	        List<String> includes = new CopyOnWriteArrayList<String>();
	        List<String> symbols = new CopyOnWriteArrayList<String>();
	        List<String> targetSpecificOptions = new CopyOnWriteArrayList<String>();

	        String fileName = null;
	        for (int j= compilerInvocationIdx+1; j < tokens.length; j++) {
				String token = tokens[j];
				if (token.equals(DASHIDASH)) {
				}
	        	else if (token.startsWith(DASHI)) {
	        		String candidate= null;
					if (token.length() > 2) {
						candidate= token.substring(2).trim();
					}
					else if (j+1 < tokens.length) {
						candidate= tokens[j+1];
						if (candidate.startsWith("-")) { //$NON-NLS-1$
							candidate= null;
						}
						else {
							j++;
						}
					}
					if (candidate != null && candidate.length() > 0) {
						if (fUtil != null) {
							candidate= fUtil.normalizePath(candidate);
						}
						if (!includes.contains(candidate)) {
							includes.add(candidate);
						}
					}
	        	}
	        	else if (token.startsWith(DASHD)) {
	        		String candidate= null;
					if (token.length() > 2) {
						candidate= token.substring(2).trim();
					}
					else if (j+1 < tokens.length) {
						candidate= tokens[j+1];
						if (candidate.startsWith("-")) { //$NON-NLS-1$
							candidate= null;
						}
						else {
							j++;
						}
					}
	        		if (candidate != null && candidate.length() > 0) {
	        			if (candidate.indexOf('=') == -1) {
	        				candidate+= '='+ fDefaultMacroDefinitionValue;
	        			}
	        			if (!symbols.contains(candidate)) {
	        				symbols.add(candidate);
	        			}
	        		}
	        	}
				else if (token.startsWith("-m") ||		 //$NON-NLS-1$
						token.startsWith("--sysroot") || //$NON-NLS-1$
	        			token.equals("-ansi") ||		 //$NON-NLS-1$
	        			token.equals("-nostdinc") ||	 //$NON-NLS-1$
	        			token.equals("-posix") ||		 //$NON-NLS-1$
	        			token.equals("-pthread") ||		 //$NON-NLS-1$
	        			token.startsWith("-O") ||		 //$NON-NLS-1$
	        			token.equals("-fno-inline") ||	 //$NON-NLS-1$
	        			token.startsWith("-finline") ||	 //$NON-NLS-1$
	        			token.equals("-fno-exceptions") ||	 //$NON-NLS-1$
	        			token.equals("-fexceptions") ||		 //$NON-NLS-1$
	        			token.equals("-fshort-wchar") ||	 //$NON-NLS-1$
	        			token.equals("-fshort-double") ||	 //$NON-NLS-1$
	        			token.equals("-fno-signed-char") ||	 //$NON-NLS-1$
	        			token.equals("-fsigned-char") ||	 //$NON-NLS-1$
	        			token.startsWith("-fabi-version="))	{  //$NON-NLS-1$
	        		if (!targetSpecificOptions.contains(token))
	        			targetSpecificOptions.add(token);
	        	}
	        	else if (fileName == null) {
	        		String possibleFileName = token.toLowerCase();
	        		if (possibleFileName.endsWith(".c") || 		 //$NON-NLS-1$
	        				possibleFileName.endsWith(".cpp") ||	 //$NON-NLS-1$
	        				possibleFileName.endsWith(".cc") ||		 //$NON-NLS-1$
	        				possibleFileName.endsWith(".cxx") ||	 //$NON-NLS-1$
	        				possibleFileName.endsWith(".C") ||		 //$NON-NLS-1$
	        				possibleFileName.endsWith(".CPP") ||	 //$NON-NLS-1$
	        				possibleFileName.endsWith(".CC") ||		 //$NON-NLS-1$
	        				possibleFileName.endsWith(".CXX") ||	 //$NON-NLS-1$
	        				possibleFileName.endsWith(".c++")) {	 //$NON-NLS-1$
	        			fileName = token;
	        		}
	        	}
	        }

	     
	        if (fileName == null || fileName.trim().length()==0) {
	        	return false;  // return when no file was given (analogous to GCCPerFileBOPConsoleParser)
	        }

	      
	      
	        IPath filePath = null;
	        List<String> translatedIncludes = includes;
	        if (includes.size() > 0) {
	        	if (fUtil != null) {
	        		 
	        				
	        		filePath = fUtil.getAbsolutePath(fileName);
	        		
	        		if(filePath!=null){
	        		
	        			translatedIncludes = fUtil.translateRelativePaths(filePath, fileName, includes);
	        		}
	        		
	        	}
	        	if (filePath == null && fUtil != null) {	// real world case
	        		// remove non-absolute include paths since there was no chance to translate them
	        		Iterator<String> iterator = translatedIncludes.iterator();
	        		while (iterator.hasNext()) {
	        			String include = iterator.next();
	        			IPath includePath = new Path(include);
	        			if (!includePath.isAbsolute() && !includePath.isUNC()) {	// do not translate UNC paths
	        				iterator.remove();
	        			}
	        		}
	        	}
	        }
	        
	        File jfile = null;
	        if(filePath!=null){ 	
	        	jfile =new File(filePath.toOSString());
	        }
	       
	        // Contribute discovered includes and symbols to the ScannerInfoCollector
	        if (translatedIncludes.size() > 0 || symbols.size() > 0) {
	        	Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
	        	scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, translatedIncludes);
	        	scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
	        	scannerInfo.put(ScannerInfoTypes.TARGET_SPECIFIC_OPTION, targetSpecificOptions);
	        	getCollector().contributeToScannerConfig(jfile, scannerInfo);

	        	OfflineTraceUtil.outputTrace("Discovered scanner info for file \'" + fileName + '\'',	//$NON-NLS-1$
	        			"Include paths", includes, translatedIncludes, "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$
	        }
			return true;
		}
	    /* (non-Javadoc)
	     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#processLine(java.lang.String)
	     */
	    public boolean processLine(String line) {
	        boolean rc = false;
	        int lineBreakPos = line.length()-1;
	        char[] lineChars = line.toCharArray();
	        while(lineBreakPos >= 0 && Character.isWhitespace(lineChars[lineBreakPos])) {
	        	lineBreakPos--;
	        }
	        if (lineBreakPos >= 0) {
	        	if (lineChars[lineBreakPos] != '\\'
	        	    || (lineBreakPos > 0 && lineChars[lineBreakPos-1] == '\\')) {
	        		lineBreakPos = -1;
	        	}
	        }
	        // check for multiline commands (ends with '\')
	        if (lineBreakPos >= 0) {
	       		sMultiline += line.substring(0, lineBreakPos);
	            bMultiline = true;
	            return rc;
	        }
	        if (bMultiline) {
	            line = sMultiline + line;
	            bMultiline = false;
	            sMultiline = ""; //$NON-NLS-1$
	        }
	        line= line.trim();
	        OfflineTraceUtil.outputTrace("AbstractGCCBOPConsoleParser parsing line: [", line, "]");    //$NON-NLS-1$ //$NON-NLS-2$
	        // make\[[0-9]*\]:  error_desc
	        int firstColon= line.indexOf(':');
	        String make = line.substring(0, firstColon + 1);
	        if (firstColon != -1 && make.indexOf("make") != -1) { //$NON-NLS-1$
	            boolean enter = false;
	            String msg = line.substring(firstColon + 1).trim();     
	            if ((enter = msg.startsWith("AbstractGCCBOPConsoleParser_EnteringDirectory")) || //$NON-NLS-1$
	                (msg.startsWith("AbstractGCCBOPConsoleParser_LeavingDirectory"))) { //$NON-NLS-1$
	                int s = msg.indexOf('`');
	                int e = msg.indexOf('\'');
	                if (s != -1 && e != -1) {
	                    String dir = msg.substring(s+1, e);
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
	    
	    private int getDirectoryLevel(String line) {
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
		 * of quotes, back-quotes, or double quotes.
		 * Outside of single-, double- or back-quotes a backslash escapes white-spaces, all quotes, 
		 * the backslash, '&' and '|'.
		 * A backslash used for escaping is removed.
		 * Quotes other than the back-quote plus '&&', '||', ';' are removed, also.
		 * @param line to tokenize
		 * @param escapeInsideDoubleQuotes if quotes need to be escaped [\"] in the resulting array of commands
		 * @return array of commands
		 */
		protected String[][] tokenize(String line, boolean escapeInsideDoubleQuotes) {
			ArrayList<String[]> commands= new ArrayList<String[]>();
			ArrayList<String> tokens= new ArrayList<String>();
			StringBuffer token= new StringBuffer();
			
			final char[] input= line.toCharArray();
			boolean nextEscaped= false;
			char currentQuote= 0;
			for (int i = 0; i < input.length; i++) {
				final char c = input[i];
				final boolean escaped= nextEscaped; nextEscaped= false;
				
				if (currentQuote != 0) {
					if (c == currentQuote) {
						if (escaped) {
							token.append(c);
						}
						else {
							if (c=='`') {
								token.append(c);	// preserve back-quotes
							}
							currentQuote= 0;
						}
					}
					else {
						if (escapeInsideDoubleQuotes && currentQuote == '"' && c == '\\') {
							nextEscaped= !escaped;
							if (escaped) {
								token.append(c);
							}
						}
						else {
							if (escaped) {
								token.append('\\');
							}
							token.append(c);
						}
					}
				}
				else {
					switch(c) {
					case '\\':
						if (escaped) {
							token.append(c);
						}
						else {
							nextEscaped= true;
						}
						break;
					case '\'': case '"': case '`':
						if (escaped) {
							token.append(c);
						}
						else {
							if (c == '`') {
								token.append(c);
							}
							currentQuote= c;
						}
						break;
					case ';':
						if (escaped) {
							token.append(c);
						}
						else {
							endCommand(token, tokens, commands);
						}
						break;
					case '&': case '|':
						if (escaped || i+1 >= input.length || input[i+1] != c) {
							token.append(c);
						}
						else {
							i++;
							endCommand(token, tokens, commands);
						}
						break;
						
					default:
						if (Character.isWhitespace(c)) {
							if (escaped) {
								token.append(c);
							}
							else {
								endToken(token, tokens);
							}
						}
						else {
							if (escaped) {
								token.append('\\');	// for windows put backslash back onto the token.
							}
							token.append(c);
						}
					}
				}
			}
			endCommand(token, tokens, commands);
			return commands.toArray(new String[commands.size()][]);
		}
		
		private void endCommand(StringBuffer token, ArrayList<String> tokens, ArrayList<String[]> commands) {
			endToken(token, tokens);
			if (!tokens.isEmpty()) {
				commands.add(tokens.toArray(new String[tokens.size()]));
				tokens.clear();
			}
		}
		private void endToken(StringBuffer token, ArrayList<String> tokens) {
			if (token.length() > 0) {
				tokens.add(token.toString());
				token.setLength(0);
			}
		}
	    
	    protected boolean processSingleLine(String line) {
	    	boolean rc= false;
			String[][] tokens= tokenize(line, true);
			for (int i = 0; i < tokens.length; i++) {
				String[] command = tokens[i];
				if (processCommand(command)) {
					rc= true;
				}
				else {  // go inside quotes, if the compiler is called per wrapper or shell script
					for (int j = 0; j < command.length; j++) {
						String[][] subtokens= tokenize(command[j], true);
						for (int k = 0; k < subtokens.length; k++) {
							String[] subcommand = subtokens[k];
							if (subcommand.length > 1) {  // only proceed if there is any additional info
								if (processCommand(subcommand)) {
									rc= true;
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
	    		final int searchFromOffset= Math.max(token.lastIndexOf('/'), token.lastIndexOf('\\')) + 1;
	    		for (int j=0; j < fCompilerCommands.length; j++) {
	    			if (token.indexOf(fCompilerCommands[j], searchFromOffset) != -1) {
	    				return i;
	    			}
	    		}
	    	}
	    	return -1;
	    }
	    

	    /**
	     * Returns array of additional compiler commands to look for
	     * 
	     * @return String[]
	     */
	    private String[] computeCompilerCommands() {
	    	/*
	    	if (project != null) {
		        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
		                getSCProfileInstance(project, ScannerConfigProfileManager.NULL_PROFILE_ID);
		        BuildOutputProvider boProvider = profileInstance.getProfile().getBuildOutputProviderElement();
		        if (boProvider != null) {
		            String compilerCommandsString = boProvider.getScannerInfoConsoleParser().getCompilerCommands();
		            if (compilerCommandsString != null && compilerCommandsString.length() > 0) {
		                String[] compilerCommands = compilerCommandsString.split(",\\s*"); //$NON-NLS-1$
		                if (compilerCommands.length > 0) {
		                    String[] compilerInvocation = new String[COMPILER_INVOCATION.length + compilerCommands.length];
		                    System.arraycopy(COMPILER_INVOCATION, 0, compilerInvocation, 0, COMPILER_INVOCATION.length);
		                    System.arraycopy(compilerCommands, 0, compilerInvocation, COMPILER_INVOCATION.length, compilerCommands.length);
		                    return compilerInvocation;
		                }
		            }
		        }
	    	}
	    	*/
	        return COMPILER_INVOCATION; 
	    }

	    protected OfflineBuildOutputParserUtility getUtility() {
	        return fUtil;
	    }
		public void shutdown() {
			// TODO Auto-generated method stub
			
		}
		public void startup(IProject project, IPath workingDirectory,
				IScannerInfoCollector collector,
				IMarkerGenerator markerGenerator) {
			// TODO Auto-generated method stub
			
		}

		
		

		
}
