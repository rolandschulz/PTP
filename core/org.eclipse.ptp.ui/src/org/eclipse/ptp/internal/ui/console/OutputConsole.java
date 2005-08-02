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
package org.eclipse.ptp.internal.ui.console;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.ui.old.ParallelImages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;

/**
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
	    newMessageStream().print(text);
		//appendToDocument(text, newMessageStream());
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
