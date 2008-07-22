package org.eclipse.ptp.rm.core.utils;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.ptp.core.PTPCorePlugin;

/** An {@link IInputStreamListener} that forwards output to a piped stream.
 *
 * @author dfferber
 */
public class InputStreamListenerToOutputStream implements IInputStreamListener {

	boolean enabled = true;
	OutputStream outputStream;

	public InputStreamListenerToOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void newBytes(byte[] bytes, int length) {
		try {
			if (isEnabled()) outputStream.write(bytes, 0, length);
		} catch (IOException e) {
			disable();
			log(e);
		}
	}

	public void streamClosed() {
		try {
			disable();
			outputStream.close();
		} catch (IOException e) {
			log(e);
		}
	}

	public void streamError(Exception e) {
		disable();
		log(e);
	}

	protected void log(Exception e) {
		PTPCorePlugin.log(e);
	}
	
	public synchronized void disable() {
		enabled = false;
	}
	
	public synchronized boolean isEnabled() {
		return enabled;
	}
}
