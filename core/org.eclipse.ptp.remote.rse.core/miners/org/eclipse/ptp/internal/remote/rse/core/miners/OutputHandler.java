/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [243699] [dstore] Loop in OutputHandler
 * David McKnight     (IBM)   [249715] [dstore][shells] Unix shell does not echo command
 * David McKnight   (IBM)        - [282919] [dstore] server shutdown results in exception in shell io reading
 * David McKnight (IBM) - [286671] Dstore shell service interprets &lt; and &gt; sequences
 * David McKnight     (IBM)   [287305] [dstore] Need to set proper uid for commands when using SecuredThread and single server for multiple clients[
 * Peter Wang         (IBM)   [299422] [dstore] OutputHandler.readLines() not compatible with servers that return max 1024bytes available to be read
 * David McKnight   (IBM)     [302996] [dstore] null checks and performance issue with shell output
 * David McKnight   (IBM)     [309338] [dstore] z/OS USS - invocation of 'env' shell command returns inconsistently organized output
 * David McKnight   (IBM)     [312415] [dstore] shell service interprets &lt; and &gt; sequences - handle old client/new server case
 * Chris Recoskie (IBM) - Cloned to PTP for use with SpawnerMiner
 *******************************************************************************/

package org.eclipse.ptp.internal.remote.rse.core.miners;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.dstore.core.model.Handler;

/**
 * The OutputHandler class is used to listen to a particular output or error stream, 
 * interpret that information and create DataElements for it for use on the client.
 */
/**
 * The OutputHandler class is used to listen to a particular output or error
 * stream, interpret that information and create DataElements for it for use on
 * the client.
 */
public class OutputHandler extends Handler {


	private DataInputStream _reader;
	private boolean _isStdError;

	private boolean _isTerminal;

	private CommandMinerThread fCommandThread;

	private boolean _isShell;


	private boolean _endOfStream = false;

	private List<String> _encodings;
	
	private BufferedReader fBufferedReader;
	private boolean fIsFinished;
	
	char[] buf = new char[1024]; 
	int readindex=0, writeindex=0;


	public OutputHandler(DataInputStream reader, String qualifier,
			boolean isTerminal, boolean isStdError, boolean isShell,
			CommandMinerThread commandThread) {
		_reader = reader;
		_isStdError = isStdError;
		_isTerminal = isTerminal;
		fCommandThread = commandThread;
		_isShell = isShell;

		_encodings = new ArrayList<String>();
		String system = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

		if (system.startsWith("z")) { //$NON-NLS-1$
			_encodings.add("IBM-1047"); //$NON-NLS-1$
			/*
			 * _encodings.add("Cp1047"); _encodings.add("Cp037");
			 * _encodings.add("UTF8");
			 */
		} else {
			String specialEncoding = System
					.getProperty("dstore.stdin.encoding"); //$NON-NLS-1$
			if (specialEncoding != null) {
				_encodings.add(specialEncoding);
			}
			_encodings.add(System.getProperty("file.encoding")); //$NON-NLS-1$
		}
		
		// try to find an encoding that will work
		for (String encoding : _encodings) {
			try {
				fBufferedReader = new BufferedReader(new InputStreamReader(_reader, encoding));
				break;
			} catch (UnsupportedEncodingException e) {
				// shouldn't really happen
				e.printStackTrace();
			}
		}

	}



	public synchronized void handle() {
		String[] lines = readLines();
		if (lines != null) {
			if (lines.length > 0) {
				/*
				 * if (lines.length == 0) { _reader. } // don't do anything unless we require output if (_newCommand &&
				 * !_isTerminal) { doPrompt(); } } else
				 */
				for (int i = 0; i < lines.length; i++) {
					// first make sure it's not a multiline line
					String ln = lines[i];
					if (ln.indexOf('\n') > 0) {
						String[] lns = ln.split("\n"); //$NON-NLS-1$
						for (int j = 0; j < lns.length; j++) {
							String line = convertSpecialCharacters(lns[j]);
							fCommandThread.interpretLine(line, _isStdError);
						}
					} else {
						String line = convertSpecialCharacters(ln);
						fCommandThread.interpretLine(line, _isStdError);
					}
				}

				if (!_isTerminal) {
					doPrompt();
				}

				fCommandThread.refreshStatus();
			}
		} else {
			finish();
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.model.Handler#finish()
	 */
	@Override
	public synchronized void finish() {
		if (!fIsFinished) {
			fIsFinished = true;
			super.finish();
			try {
				fBufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}



	private String convertSpecialCharacters(String input){
		if (fCommandThread._supportsCharConversion){
		   // needed to ensure xml characters aren't converted in xml layer			
			StringBuffer output = new StringBuffer();

			for (int idx = 0; idx < input.length(); idx++)
			{
				char currChar = input.charAt(idx);
				switch (currChar)
				{
				case '&' :
					output.append("&#38;"); //$NON-NLS-1$
					break;
				case ';' :
					output.append("&#59;"); //$NON-NLS-1$
					break;
				default :
					output.append(currChar);
					break;
				}
			}
			return output.toString();
		}
		else {
			return input;
		}
	}

	private void doPrompt() {
		try {
			if (!_isStdError && _isShell && (_reader.available() == 0)) {
				if (!_isTerminal) {
					try {
						Thread.sleep(200);
						if (_reader.available() == 0) {
							// create fake prompt
							fCommandThread.createPrompt(fCommandThread.getCWD() + '>', fCommandThread.getCWD());							
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (IOException e) {
			fCommandThread._dataStore.trace(e);
		}
	}

	private String[] readLines() {
		if (_endOfStream || fBufferedReader == null) {
			return null;
		}
		
		ArrayList<String> outputLines = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();

		try {
			
			boolean lineFound = false;
			do {
				
				int currVal = fBufferedReader.read();

				switch (currVal) {
				case -1:
					_endOfStream = true;
					// if we have a string in progress, return whatever we have left as a line
					if(sb.length() > 0) {
						outputLines.add(sb.toString());
						sb = new StringBuilder();
					}
					break;

				case '\r':	
				case '\n':
					// never append newlines, we'll return them as individual lines instead
					// instead take what we have so far and create a new line out of it
					if(sb.length() > 0) {
						outputLines.add(sb.toString());
						sb = new StringBuilder();
						lineFound = true;
					}
					
					// mark our position so that we can rewind to here
					fBufferedReader.mark(65536); // means we can read up to 64 KB and still recover
					break;

			
				default:
					sb.append((char) currVal);
					break;
				}
			} while(!_endOfStream && !lineFound);
			
			// we've now created as many lines as we can, but we may have partially read a line... rewind the stream if we have a partial line
			if(sb.length() > 0) {
				fBufferedReader.reset();
			}
			
		} catch (UnsupportedEncodingException e) {
			fCommandThread._dataStore.trace(e);
		} catch (IOException e) {
			// end of file?
			_endOfStream = true;
			fCommandThread._dataStore.trace(e);
		}
		
		return outputLines.toArray(new String[0]);

	}
	
	public synchronized void waitForInput() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {

		}
	}
}
