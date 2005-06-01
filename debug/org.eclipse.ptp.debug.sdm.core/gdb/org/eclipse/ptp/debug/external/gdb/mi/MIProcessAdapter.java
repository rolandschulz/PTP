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

package org.eclipse.ptp.debug.external.gdb.mi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.eclipse.ptp.debug.external.gdb.mi.utils.ProcessFactory;
import org.eclipse.ptp.debug.external.gdb.mi.utils.Spawner;




/**
 */
public class MIProcessAdapter implements MIProcess {

	Process fGDBProcess;

	public MIProcessAdapter(String[] args) throws IOException {
		fGDBProcess = getGDBProcess(args);
	}

	/**
	 * Do some basic synchronisation, gdb may take some time to load for
	 * whatever reasons and we need to be able to let the user bailout.
	 * 
	 * @param args
	 * @return Process
	 * @throws IOException
	 */
	protected Process getGDBProcess(String[] args) throws IOException {
		int ONE_SECOND = 1000;

		if (GDBSession.getDefault().isDebugging()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < args.length; ++i) {
				sb.append(args[i]);
				sb.append(' ');
			}
			GDBSession.getDefault().debugLog(sb.toString());
		}
		final Process pgdb = ProcessFactory.getFactory().exec(args);
		Thread syncStartup = new Thread("GDB Start") { //$NON-NLS-1$
			public void run() {
				try {
					String line;
					InputStream stream = pgdb.getInputStream();
					Reader r = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(r);
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						//System.out.println("GDB " + line);
						if (line.endsWith("(gdb)")) { //$NON-NLS-1$
							break;
						}
					}
				} catch (Exception e) {
					// Do nothing, ignore the errors
				}				
                synchronized (pgdb) {
                    pgdb.notifyAll();
                }
			}
		};
		syncStartup.start();

		
        synchronized (pgdb) {
            int launchTimeout = 30000;
            while (syncStartup.isAlive()) {
                try {
                    pgdb.wait(launchTimeout);
                    break;
                } catch (InterruptedException e) {
                }
            }
        }
        try {
            syncStartup.interrupt();
            syncStartup.join(1000);
        } catch (InterruptedException e) {
        }
        return pgdb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.mi.core.MIProcess#canInterrupt()
	 */
	public boolean canInterrupt(MIInferior inferior) {
		return fGDBProcess instanceof Spawner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.mi.core.MIProcess#interrupt()
	 */
	public void interrupt(MIInferior inferior) {
		if (fGDBProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fGDBProcess;
			gdbSpawner.interrupt();
			int state;
			synchronized (inferior) {
				// Allow (5 secs) for the interrupt to propagate.
				for (int i = 0; inferior.isRunning() && i < 5; i++) {
					try {
						inferior.wait(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			// If we are still running try to drop the sig to the PID
			if (inferior.isRunning() && inferior.getInferiorPID() > 0) {
				// lets try something else.
				gdbSpawner.raise(inferior.getInferiorPID(), gdbSpawner.INT);
				synchronized (inferior) {
					for (int i = 0; inferior.isRunning() && i < 5; i++) {
						try {
							inferior.wait(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		return fGDBProcess.exitValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		return fGDBProcess.waitFor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		fGDBProcess.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return fGDBProcess.getErrorStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return fGDBProcess.getInputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return fGDBProcess.getOutputStream();
	}

}