package org.eclipse.ptp.internal.console;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

/**
 * @author Clement
 *
 */
public class OutputConsole extends MessageConsole {
	private InputStream inputStream = null;
	private Thread thread = null;
	private static final int BUFFER_SIZE = 8192;
	private boolean isKilled = false;
	
	public OutputConsole(String name, InputStream inputStream) {
		this(name, ParallelImages.DESC_PARALLEL);
		this.inputStream = inputStream;
		startMonitorOutput();
	}
	public OutputConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}
	
	protected void startMonitorOutput() {
		if (thread == null) {
			System.out.println("Create console windows");
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {this} );
			//consoleManager.showConsoleView(this);
			
			Runnable runnable = new Runnable() {
				public void run() {
					read();
				}
			};
			thread = new Thread(runnable, "Output Console Monitor");
			thread.start();
		}
	}
	
	protected void close() {
		if (thread != null) {
			System.out.println("Remove console windows");
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {this} );
			closeStream();
			Thread newThread = thread;
			thread = null;
			try {
				newThread.join();
			} catch (InterruptedException e) {				
			}
		}
	}
	
	private void appendText(String text) {
		newMessageStream().println(text); //3.1M4
		//appendToDocument(text, newMessageStream()); //3.0R
	}
	
	public void kill() {
		isKilled = true;
		close();
	}
	
	private void closeStream() {
		if (inputStream == null)
			return;
		
		try {
			inputStream.close();
		} catch (IOException ioe) {			
		} finally {
			inputStream = null;
			isKilled = true;
		}
	}
	
	private void read() {
		byte[] bytes = new byte[BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (isKilled) {
					break;
				}
				read = inputStream.read(bytes);
				if (read > 0) {
					appendText(new String(bytes, 0, read));
				}
			} catch (IOException ioe) {
				return;
			} catch (NullPointerException e) {
				return;
			}
		}
		closeStream();
	}
}
