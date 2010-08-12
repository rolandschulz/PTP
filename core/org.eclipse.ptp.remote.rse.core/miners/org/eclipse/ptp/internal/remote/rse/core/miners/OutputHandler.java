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
import java.util.ArrayList;
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
	
	private String fCwd;
	private BufferedReader fBufferedReader;
	private boolean fIsFinished;


	public OutputHandler(DataInputStream reader, String qualifier,
			boolean isTerminal, boolean isStdError, boolean isShell,
			CommandMinerThread commandThread, String cwd) {
		_reader = reader;
		_isStdError = isStdError;
		_isTerminal = isTerminal;
		fCommandThread = commandThread;
		_isShell = isShell;
		fCwd = cwd;

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
						
			/*
			 * if (lines.length == 0) { _reader. }
			 *  // don't do anything unless we require output if (_newCommand &&
			 * !_isTerminal) { doPrompt(); } } else
			 */
			for (int i = 0; i < lines.length; i++) {
				// first make sure it's not a multiline line
				String ln = lines[i];
				if (ln.indexOf('\n') > 0){
					String[] lns = ln.split("\n"); //$NON-NLS-1$
					for (int j = 0; j < lns.length; j++){
						String line = convertSpecialCharacters(lns[j]);
						fCommandThread.interpretLine(line, _isStdError);
					}
				}
				else {
					String line = convertSpecialCharacters(ln);
					fCommandThread.interpretLine(line, _isStdError);
				}
			}

			if (!_isTerminal){
				doPrompt();
			}

			fCommandThread.refreshStatus();
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
							fCommandThread.createPrompt(fCwd + '>', fCwd);							
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
		String[] output = new String[0];
		
		String line;
		ArrayList<String> outputLines = new ArrayList<String>();

		try {
			
			line = fBufferedReader.readLine();
			if(line != null) {
				outputLines.add(line);
			}
			
			else {
				// end of stream
				_endOfStream = true;
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// end of file?
			_endOfStream = true;
			e.printStackTrace();
		}
		
		return outputLines.toArray(output);

	}
	
	public synchronized void waitForInput() {
		// do nothing, we'll block on read if necessary
	}
}
