package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;

public class CommandJobStreamsProxy implements ICommandJobStreamsProxy {

	private ICommandJobStreamMonitor out;
	private ICommandJobStreamMonitor err;

	private boolean fClosed = false;
	private boolean fStarted = false;

	public synchronized void close() {
		if (!fClosed) {
			if (out != null) {
				out.close();
			}
			if (err != null) {
				err.close();
			}
			fClosed = true;
		}
	}

	public void closeInputStream() throws IOException {
		throw new IOException(Messages.UnsupportedWriteException);
	}

	public IStreamMonitor getErrorStreamMonitor() {
		return err;
	}

	public IStreamMonitor getOutputStreamMonitor() {
		return out;
	}

	public void setErrMonitor(ICommandJobStreamMonitor err) {
		this.err = err;
	}

	public void setOutMonitor(ICommandJobStreamMonitor out) {
		this.out = out;
	}

	public synchronized void startMonitors() {
		if (!fClosed && !fStarted) {
			if (out != null) {
				out.startMonitoring();
			}
			if (err != null) {
				err.startMonitoring();
			}
			fStarted = true;
		}
	}

	public void write(String input) throws IOException {
		throw new IOException(Messages.UnsupportedWriteException);
	}
}