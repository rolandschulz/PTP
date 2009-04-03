/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *	
 * Contributors (PTP version): 
 * IBM Corporation - Initial API and implementation
 * 				   - Adapted to HostShellProcessAdapter due to RSE bug
 * 
 * Contributors (RSE version):
 * Ewa Matejska (PalmSource) - initial version
 * Martin Oberhuber (Wind River) - adapt to IHostOutput API (bug 161773, 158312)
 * Martin Oberhuber (Wind River) - moved from org.eclipse.rse.remotecdt (bug 161777)
 * Martin Oberhuber (Wind River) - renamed from HostShellAdapter (bug 161777)
 * Martin Oberhuber (Wind River) - improved Javadoc
 *******************************************************************************/
package org.eclipse.ptp.remote.rse.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.NullInputStream;
import org.eclipse.rse.services.shells.HostShellOutputStream;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;

public class RSEProcess extends AbstractRemoteProcess implements IHostShellOutputListener {
	private boolean mergeOutput;
	private IHostShell hostShell;
	private InputStream inputStream = null;
	private InputStream errorStream = null;
	private HostShellOutputStream outputStream = null;
	private PipedOutputStream hostShellInput = null;
	private PipedOutputStream hostShellError = null;
	
	public RSEProcess(IHostShell hostShell, boolean mergeOutput) throws IOException {
		this.hostShell = hostShell;
		this.mergeOutput = mergeOutput;
		hostShellInput = new PipedOutputStream();
		if (mergeOutput) {
			hostShellError = hostShellInput;
			errorStream = new NullInputStream();
		} else {
			hostShellError = new PipedOutputStream();
			errorStream = new PipedInputStream(hostShellError);
		}
		inputStream = new PipedInputStream(hostShellInput);
		outputStream = new HostShellOutputStream(hostShell);
		this.hostShell.getStandardOutputReader().addOutputListener(this);
		this.hostShell.getStandardErrorReader().addOutputListener(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public synchronized void destroy() {
		hostShell.exit();
		notifyAll();
		try {
			hostShellInput.close();
			if (!mergeOutput) {
				hostShellError.close();
			}
			inputStream.close();
			errorStream.close();
			outputStream.close();
		} catch (IOException e) {
			//FIXME IOException when closing one of the streams will leave others open
			// Ignore
		}	
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public synchronized int exitValue() {
		if (hostShell.isActive()) {
			throw new IllegalThreadStateException();
		}
		// No way to tell what the exit value was.
		// TODO it would be possible to get the exit value
		// when the remote process is started like this:
		//   sh -c 'remotecmd ; echo "-->RSETAG<-- $?\"'
		// Then the output steram could be examined for -->RSETAG<-- to get the exit value.
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return errorStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public synchronized int waitFor() throws InterruptedException {
		while(hostShell.isActive()) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				// ignore because we're polling to see if shell is still active.
			}
		}
		
		try {
			// Wait a second to try to get some more output from the target shell before closing.
			wait(1000);
			// Allow for the data from the stream to be read if it's available
			if (inputStream.available() != 0 || errorStream.available() != 0) {
				throw new InterruptedException();
			}
			hostShellInput.close();
			if (!mergeOutput) {
				hostShellError.close();
			}
			inputStream.close();
			errorStream.close();
			outputStream.close();
		} catch (IOException e) {
			// Ignore
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteProcess#isCompleted()
	 */
	public boolean isCompleted() {
		try {
			exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}
	
	/**
	 * Process an RSE Shell event, by writing the lines of text contained
	 * in the event into the adapter's streams.
	 * @see org.eclipse.rse.services.shells.IHostShellOutputListener#shellOutputChanged(org.eclipse.rse.services.shells.IHostShellChangeEvent)
	 */
	public void shellOutputChanged(IHostShellChangeEvent event) {
		IHostOutput[] input = event.getLines();
		OutputStream outputStream = event.isError() ? hostShellError : hostShellInput;
		if (input.length == 0) {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
			return;
		}
		try {
			for(int i = 0; i < input.length; i++) {
				outputStream.write(input[i].getString().getBytes());
				outputStream.write('\n');
				outputStream.flush();
			}
		} catch(IOException e) {
			// Ignore
		}
	}
	
}
