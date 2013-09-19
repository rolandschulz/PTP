/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcess;

public class RemoteToolsProcessBuilder extends AbstractRemoteProcessBuilder {
	private final RemoteToolsConnection fConnection;
	private final RemoteToolsFileManager fFileMgr;
	private final Map<String, String> fRemoteEnv;
	private Map<String, String> fNewRemoteEnv = null;
	private final Set<Character> charSet = new HashSet<Character>();

	/**
	 * @since 4.0
	 */
	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, RemoteToolsFileManager fileMgr, List<String> command) {
		super(command);
		fConnection = conn;
		fFileMgr = fileMgr;
		fRemoteEnv = new HashMap<String, String>(conn.getEnv());

		// Create set of characters not to escape
		String trustedChars = null;
		trustedChars = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$ //$NON-NLS-2$
		trustedChars += "0123456789" + "/._-"; //$NON-NLS-1$ //$NON-NLS-2$
		CharacterIterator it = new StringCharacterIterator(trustedChars);
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			charSet.add(c);
		}
	}

	/**
	 * @since 4.0
	 */
	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, RemoteToolsFileManager fileMgr, String... command) {
		this(conn, fileMgr, Arrays.asList(command));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		IFileStore dir = super.directory();
		if (dir == null) {
			dir = fFileMgr.getResource(fConnection.getWorkingDirectory());
			directory(dir);
		}
		return dir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		if (fNewRemoteEnv == null) {
			fNewRemoteEnv = new HashMap<String, String>();
			fNewRemoteEnv.putAll(fRemoteEnv);
		}
		return fNewRemoteEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#getSupportedFlags ()
	 */
	@Override
	public int getSupportedFlags() {
		return ALLOCATE_PTY | FORWARD_X11;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start(int)
	 */
	@Override
	public IRemoteProcess start(int flags) throws IOException {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		List<String> cmdArgs = command();
		if (cmdArgs.size() < 1) {
			throw new IndexOutOfBoundsException();
		}

		String remoteCmd = ""; //$NON-NLS-1$

		for (int i = 0; i < cmdArgs.size(); i++) {
			if (i > 0) {
				remoteCmd += " "; //$NON-NLS-1$
			}
			remoteCmd += charEscapify(cmdArgs.get(i), charSet);
		}

		try {
			IRemoteExecutionManager exeMgr = fConnection.createExecutionManager();
			IRemoteExecutionTools exeTools = exeMgr.getExecutionTools();
			IRemoteScript script = exeTools.createScript();
			if (directory() != null) {
				String setWorkingDirStr = "cd " + charEscapify(directory().toURI().getPath(), charSet); //$NON-NLS-1$
				script.setScript(new String[] { setWorkingDirStr, remoteCmd });
			} else {
				script.setScript(remoteCmd);
			}

			/*
			 * There are two possibilities:
			 * 
			 * 1. Some environment variables have changed values, or new variables have been added. In this case we just want to
			 * send send the changed values rather than all the variables.
			 * 
			 * 2. Some of the existing variables have been removed. In this case, we need to resend the entire environment variable
			 * list.
			 */

			if (fNewRemoteEnv != null) {
				boolean clearEnv = false;

				/*
				 * See if any of the existing variables have been removed
				 */
				for (String key : fRemoteEnv.keySet()) {
					if (fNewRemoteEnv.get(key) == null) {
						clearEnv = true;
						break;
					}
				}

				if (clearEnv) {
					script.clearEnvironment();

					/*
					 * Add new/changed variables
					 */
					for (Entry<String, String> entry : fNewRemoteEnv.entrySet()) {
						script.addEnvironment(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
					}
				} else {
					/*
					 * Just add new or changed environment variables.
					 */
					for (Entry<String, String> entry : fNewRemoteEnv.entrySet()) {
						String oldValue = fRemoteEnv.get(entry.getKey());
						if (oldValue == null || !oldValue.equals(entry.getValue())) {
							script.addEnvironment(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
						}
					}
				}
			}

			script.setAllocateTerminal((flags & ALLOCATE_PTY) == ALLOCATE_PTY);
			script.setForwardX11((flags & FORWARD_X11) == FORWARD_X11);

			RemoteProcess process = exeTools.executeProcess(script);
			return new RemoteToolsProcess(process, redirectErrorStream());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private String charEscapify(String inputString, Set<Character> charSet) {
		if (inputString == null) {
			return null;
		}
		StringBuffer newString = new StringBuffer(""); //$NON-NLS-1$
		CharacterIterator it = new StringCharacterIterator(inputString);

		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (c == '\'') {
				newString.append("'\\\\\\''"); //$NON-NLS-1$
			} else if (c > 127 || charSet.contains(c)) { // Do not escape non-ASCII characters (> 127)
				newString.append(c);
			} else {
				newString.append("\\" + c); //$NON-NLS-1$
			}
		}
		inputString = newString.toString();
		return inputString;
	}
}