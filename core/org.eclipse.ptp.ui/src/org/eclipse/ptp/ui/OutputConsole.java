/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author clement
 *
 */
public class OutputConsole extends MessageConsole {
	private InputStream inputStream = null;
	private MessageConsoleStream messageOutputStream = null;
	private Thread thread = null;
	private static final int BUFFER_SIZE = 8192;
	private boolean isKilled = false;

	/** Constructor
	 * @param name name of console window
	 * @param inputStream
	 */
	public OutputConsole(String name, InputStream inputStream) {
		this(name, null, inputStream);
	}
	/** Constructor
	 * @param name name of console window
	 * @param imageDescriptor image of console window
	 * @param inputStream
	 */
	public OutputConsole(String name, ImageDescriptor imageDescriptor, InputStream inputStream) {
		super(name, imageDescriptor);
		this.inputStream = inputStream;
		messageOutputStream = newMessageStream();
		startMonitorOutput();
	}
	/** Start monitoring output
	 * 
	 */
	protected void startMonitorOutput() {
		if (thread == null) {
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this });
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						read();
					} catch (IOException e) {
						kill();
					}
				}
			};
			thread = new Thread(runnable, Messages.OutputConsole_0);
			thread.start();
		}
	}
	/** Close console window
	 * @throws IOException
	 */
	private void close() throws IOException {
		if (thread != null) {
			try {
				closeStream();
				messageOutputStream.close();
				thread.interrupt();
			} finally {
				thread = null;
				isKilled = true;
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { this });
			}
		}
	}
	/** Append text into console
	 * @param text
	 */
	private void appendText(String text) {
		messageOutputStream.print(text);
	}
	/** Kill console window
	 * 
	 */
	public void kill() {
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** Close console input stream
	 * @throws IOException
	 */
	private void closeStream() throws IOException {
		try {
			if (inputStream != null)
				inputStream.close();
		} finally {
			inputStream = null;
			isKilled = true;
		}
	}
	/** Read from input stream
	 * @throws IOException
	 */
	private void read() throws IOException {
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
			} catch (NullPointerException e) {
				break;
			}
		}
		closeStream();
	}
}
