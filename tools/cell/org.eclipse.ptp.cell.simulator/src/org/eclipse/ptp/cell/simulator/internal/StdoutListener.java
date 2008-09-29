/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.simulator.internal;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.utils.stream.ILineStreamListener;


/**
 * Receives text lines from the simulator stdout that runs the TCL command console. Notifies the Controller about
 * important events. Lines that match events are filtered out. Other lines that do not match are forwarded to the
 * controller.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
class StdoutListener implements ILineStreamListener {
	SimulatorControl control;

	/**
	 * Constructor. 
	 * <p>
	 * Do not forget to call run() to observer the simulator.
	 * 
	 * @param control
	 *            controller class that receives events
	 */
	public StdoutListener(SimulatorControl control) {
		this.control = control;
	}

	public void newLine(String line) {
		if (line.startsWith("||| ") && line.endsWith(" |||")) { //$NON-NLS-1$ //$NON-NLS-2$
			String command = ""; //$NON-NLS-1$
			String message = ""; //$NON-NLS-1$
			int div = line.indexOf(":"); //$NON-NLS-1$
			if (div >= 0) {
				command = line.substring(4, div).trim();
				message = line.substring(div + 1, line.length() - 4).trim();
			}
			
			if (command.equals("SIMULATOR")) { //$NON-NLS-1$
				if (message.equalsIgnoreCase("Start")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.SIMULATOR_START);
				} else if (message.equalsIgnoreCase("Stop")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.SIMULATOR_STOP);
				} else {
					handleUnknownEvent(command, message);
				}
				
			} else if (command.equals("INIT")) { //$NON-NLS-1$
				if (message.equalsIgnoreCase("Parse")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_PARSE);
				} else if (message.equalsIgnoreCase("Check")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_CHECK);
				} else if (message.equalsIgnoreCase("Configure")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_CONFIGURE);
				} else if (message.equalsIgnoreCase("Bogusnet")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_BOGUSNET);
				} else if (message.equalsIgnoreCase("Console")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_CONSOLE);
				} else if (message.equalsIgnoreCase("Configured")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.INIT_CONFIGURED);
				} else {
					handleUnknownEvent(command, message);
				}
				
			} else if (command.equals("BOOT")) { //$NON-NLS-1$
				if (message.equalsIgnoreCase("Bios")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.BOOT_BIOS);
				} else if (message.equalsIgnoreCase("Linux")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.BOOT_LINUX);
				} else if (message.equalsIgnoreCase("System")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.BOOT_SYSTEM);
				} else if (message.equalsIgnoreCase("Configure")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.BOOT_CONFIGURE);
				} else if (message.equalsIgnoreCase("Complete")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.BOOT_COMPLETE);
				} else {
					handleUnknownEvent(command, message);
				}
				
			} else if (command.equals("SHUTDOWN")) { //$NON-NLS-1$
				if (message.equalsIgnoreCase("Prepared")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.SHUTDOWN_PREPARED);
				} else if (message.equalsIgnoreCase("Started")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.SHUTDOWN_START);
				} else if (message.equalsIgnoreCase("Complete")) { //$NON-NLS-1$
					control.notifyEvent(SimulatorControl.SHUTDOWN_COMPLETE);
				} else {
					handleUnknownEvent(command, message);
				}
				
			} else if (command.equals("ERROR")) { //$NON-NLS-1$
				control.notifyError(message);
			} else {
				handleUnknownEvent(command, message);
			}
		} else {
			forwardLine(line);
		}
	}

	private void forwardLine(String line) {
		control.receiveProcessLine(line);
	}

	private void handleUnknownEvent(String command, String message) {
		System.err.println(NLS.bind(Messages.StdoutListener_UnknownEvent, new String[] { command, message}));
	}

	public void streamClosed() {
		control.notifyEvent(SimulatorControl.TCL_STREAM_CLOSED);
	}

	public void streamError(Exception e) {
		control.notifyEvent(SimulatorControl.TCL_STREAM_CLOSED);
	}
}
