/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.mi.core.gdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.eclipse.ptp.debug.mi.core.gdb.command.CLICommand;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfo;
import org.eclipse.ptp.debug.mi.core.gdb.utils.PTY;




/**
 * GDB/MI Plugin.
 */
public class GDBSession {

	//The shared instance.
	private static GDBSession gdbSession = new GDBSession();

	// GDB init command file
	private static final String GDBINIT = ".gdbinit"; //$NON-NLS-1$

	// GDB command
	private static final String GDB = "gdb"; //$NON-NLS-1$

	/**
	 * The constructor
	 * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
	 */
	private GDBSession() {
		super();
	}

	/**
	 * Returns the singleton.
	 */
	public static GDBSession getDefault() {
		return gdbSession;
	}

	/*
    public MISession createSession() throws IOException, MIException {

        String[] args = new String[] {"gdb", "-q", "-nw", "-i", "mi1"};

        MIProcess pgdb = null;
        MISession session = null;
    	IMITTY pty = null;
		
		try {
			PTY pseudo = new PTY();
			pty = new MITTYAdapter(pseudo);
		} catch (IOException e) {
			// Should we not print/log this ?
		}        
        
        try {
        	pgdb = new MIProcessAdapter(args);
            session = createMISession(pgdb, pty, MISession.PROGRAM);
        } catch (MIException e) {
            pgdb.destroy();
            throw e;
        }

        return session;
    }
    */

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws MIException
	 */
	public MISession createSession(String gdb, File program, File cwd, String gdbinit) throws IOException, MIException {
		IMITTY pty = null;
		boolean failed = false;

		try {
			PTY pseudo = new PTY();
			pty = new MITTYAdapter(pseudo);
		} catch (IOException e) {
			// Should we not print/log this ?
		}

		try {
			return createSession(gdb, program, cwd, gdbinit, pty);
		} catch (IOException exc) {
			failed = true;
			throw exc;
		} catch (MIException exc) {
			failed = true;
			throw exc;
		} finally {
			if (failed) {
				// Shutdown the pty console.
				if (pty != null) {
					try {
						OutputStream out = pty.getOutputStream();
						if (out != null) {
							out.close();
						}
						InputStream in = pty.getInputStream();
						if (in != null) {
							in.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public MISession createSession(String gdb, File program, File cwd, String gdbinit, IMITTY pty) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}
		
		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		String[] args;
		if (pty != null) {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", "mi1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", "mi1", program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			}
		} else {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", "mi1"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", "mi1", program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			}
		}

		MIProcess pgdb = new MIProcessAdapter(args);

		MISession session;
		try {
			session = createMISession(pgdb, pty, MISession.PROGRAM);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		// Try to detect if we have been attach via "target remote localhost:port"
		// and set the state to be suspended.
		try {
			CLICommand cmd = new CLICommand("info remote-process"); //$NON-NLS-1$
			session.postCommand(cmd);
			MIInfo info = cmd.getMIInfo();
			if (info == null) {
				pgdb.destroy();
				throw new MIException("No answer"); //$NON-NLS-1$
			}
			//@@@ We have to manually set the suspended state when we attach
			session.getMIInferior().setSuspended();
			session.getMIInferior().update();
		} catch (MIException e) {
			// If an exception is thrown that means ok
			// we did not attach to any target.
		}
		return session;
	}

    
    
	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param int
	 * @param int
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(MIProcess process, IMITTY pty, int timeout, int type, int launchTimeout) throws MIException {
		return new MISession(process, pty, timeout, type, launchTimeout);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param type
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(MIProcess process, IMITTY pty, int type) throws MIException {
		GDBSession miPlugin = getDefault();
		int timeout = 10000;
		int launchTimeout = 30000;
		return createMISession(process, pty, timeout, type, launchTimeout);
	}

	public boolean isDebugging() {
		return true;
	}

	public void debugLog(String message) {
		if (getDefault().isDebugging()) {
			// Time stamp
			message = MessageFormat.format( "[{0}] {1}", new Object[] { new Long( System.currentTimeMillis() ), message } ); //$NON-NLS-1$
			// This is to verbose for a log file, better use the console.
			//	getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			// ALERT:FIXME: For example for big buffers say 4k length,
			// the console will simply blow taking down eclipse.
			// This seems only to happen in Eclipse-gtk and Eclipse-motif
			// on GNU/Linux, so it will be break in smaller chunks.
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.err.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.err.println(message);
			}
		}
	}

}
