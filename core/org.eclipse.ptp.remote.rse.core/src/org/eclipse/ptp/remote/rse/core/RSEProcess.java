/*******************************************************************************
 * Copyright (c) 2006, 2013 PalmSource, Inc. and others
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

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.ptp.internal.remote.rse.core.DStoreHostShell;
import org.eclipse.ptp.internal.remote.rse.core.miners.SpawnerMiner;
import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.NullInputStream;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.internal.services.local.shells.LocalHostShell;
import org.eclipse.rse.services.shells.HostShellOutputStream;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;

@SuppressWarnings("restriction")
public class RSEProcess extends AbstractRemoteProcess implements IHostShellOutputListener {
	private final boolean mergeOutput;
	private final IHostShell hostShell;
	private InputStream inputStream = null;
	private InputStream errorStream = null;
	private HostShellOutputStream outputStream = null;
	private PipedOutputStream hostShellInput = null;
	private PipedOutputStream hostShellError = null;
	private DataElement fStatus;
	private boolean fSpawnErrorFound = false;
	private String fSpawnErrorMessage;
	private boolean fErrorReported = false;
	private int fIndex = 0;
	
	private IDomainListener fDomainListener = new IDomainListener() {
		

		public boolean listeningTo(DomainEvent e) {
			return e.getParent() == fStatus;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.dstore.extra.IDomainListener#domainChanged(org.eclipse.dstore.extra.DomainEvent)
		 * 
		 * Listens for events indicating that the data elements hanging off the status object have changed.
		 * We then look through any new events for elements of our special error type.
		 * 
		 * The reason we have the listener in addition to the check when we look for errors to report is that
		 * this way, we process events as they happen.  Otherwise when you go to report errors, it would have
		 * to loop through all the data elements right then and there, which could be costly if there were a lot
		 * of them.  This way we keep up to date as time goes on and there is not a big delay in reporting
		 * errors to the user.
		 */
		public synchronized void domainChanged(DomainEvent e) {
			
			if (!fSpawnErrorFound) {

				// troll through any new nested items, looking for launch errors
				while (fStatus != null && fIndex < fStatus.getNestedSize()) {
					DataElement element = fStatus.get(fIndex++);

					String type = element.getType();

					if (type.equals(SpawnerMiner.SPAWN_ERROR)) {
						// there was an error launching the command... spit an error message
						// out to stdout.  We don't output it to stderr as then it might
						// get interleaved with output from the remote side.
						String command = element.getName();
						final String message = Messages.bind(Messages.RSEProcess_0, command);

						synchronized (hostShellInput) {
							fSpawnErrorFound = true;
							fSpawnErrorMessage = message;
						}

					}
				}
			}
			
		}
			
	};


	@SuppressWarnings("restriction")
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
		
		if(this.hostShell.getStandardErrorReader() != null)
			this.hostShell.getStandardErrorReader().addOutputListener(this);
		
		if(hostShell instanceof DStoreHostShell) {
			fStatus = ((DStoreHostShell) hostShell).getStatus();
		}
		
		else if(hostShell instanceof org.eclipse.rse.internal.services.dstore.shells.DStoreHostShell) {
			fStatus = ((org.eclipse.rse.internal.services.dstore.shells.DStoreHostShell) hostShell).getStatus();
		}
		
		else if (hostShell instanceof LocalHostShell) {
			fStatus = null;
		}
		
		if(fStatus != null) {
			fStatus.getDataStore().getDomainNotifier().addDomainListener(fDomainListener );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
			// FIXME IOException when closing one of the streams will leave
			// others open
			// Ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
		// sh -c 'remotecmd ; echo "-->RSETAG<-- $?\"'
		// Then the output steram could be examined for -->RSETAG<-- to get the
		// exit value.
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return errorStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public synchronized int waitFor() throws InterruptedException {
		waitForHostShellTermination();

		try {
			// Wait a second to try to get some more output from the target
			// shell before closing.
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

	private void reportSpawnError() throws IOException {
		// always look for errors that haven't been found yet, otherwise we might exit
		// before they are found		
		synchronized (fDomainListener) {
			if (!fSpawnErrorFound) {

				// troll through any new nested items, looking for spawn errors
				while (fStatus != null && fIndex < fStatus.getNestedSize()) {
					DataElement element = fStatus.get(fIndex++);

					String type = element.getType();

					if (type.equals(SpawnerMiner.SPAWN_ERROR)) {
						// there was an error launching the command... spit an error message
						// out to stdout
						String command = element.getName();
						final String message = Messages.bind(Messages.RSEProcess_0, command);

						synchronized (hostShellInput) {
							fSpawnErrorFound = true;
							fSpawnErrorMessage = message;
						}

					}
				}
			}
		}
		
		synchronized (hostShellInput) {
			if (fSpawnErrorFound && !fErrorReported) {
				hostShellInput.write(fSpawnErrorMessage.getBytes());
				hostShellInput.flush();
				fErrorReported = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.AbstractRemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		try {
			exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}

	/**
	 * Process an RSE Shell event, by writing the lines of text contained in the
	 * event into the adapter's streams.
	 * 
	 * @see org.eclipse.rse.services.shells.IHostShellOutputListener#shellOutputChanged(org.eclipse.rse.services.shells.IHostShellChangeEvent)
	 */
	public void shellOutputChanged(IHostShellChangeEvent event) {
		IHostOutput[] input = event.getLines();
		OutputStream outputStream = event.isError() ? hostShellError : hostShellInput;
		if (input.length == 0) {
			/*
			 * Avoid closing the stream too quickly. This can cause reader
			 * threads to miss output from the shell
			 */
			waitForHostShellTermination();
			try {
				reportSpawnError();
				outputStream.close();
			} catch (IOException e) {
			}
			return;
		}
		try {
			for (int i = 0; i < input.length; i++) {
				outputStream.write(input[i].getString().getBytes());
				outputStream.write('\n');
				outputStream.flush();
			}
		} catch (IOException e) {
			// Ignore
		}
	}

	private synchronized void waitForHostShellTermination() {
		while (hostShell.isActive()) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				// ignore because we're polling to see if shell is still
				// active.
			}
		}
	}

}
