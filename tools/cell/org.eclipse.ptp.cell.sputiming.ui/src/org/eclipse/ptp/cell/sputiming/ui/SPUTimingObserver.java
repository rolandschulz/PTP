/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.cell.sputiming.extension.ISPUTimingObserver;
import org.eclipse.ptp.cell.sputiming.ui.debug.Debug;
import org.eclipse.ptp.cell.sputiming.ui.message.Messages;
import org.eclipse.ptp.cell.sputiming.ui.parse.ParsedTimingFile;
import org.eclipse.ptp.cell.sputiming.ui.parse.TimingFileParser;


/**
 * Listener to SPUTiming information generator plugin
 * 
 * @author Richard Maciel
 *
 */
public class SPUTimingObserver implements ISPUTimingObserver {
	public SPUTimingObserver() {
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.launch.sputiming.extension.ISPUTimingObserver#afterFileGeneration(org.eclipse.core.runtime.IPath)
	 */
	public void afterFileGeneration(IPath filename) {
		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_NOTIFICATIONS, "Received notification: ''{0}''", filename.toString()); //$NON-NLS-1$

		TimingFileParser timingFileParser = new TimingFileParser(filename.toOSString());
		ParsedTimingFile parsedTiming = null;
		try {
			parsedTiming = timingFileParser.parseTimingFile();
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_NOTIFICATIONS, "Failed to parse file: ''{0}''", filename.toOSString()); //$NON-NLS-1$
			Debug.POLICY.error(e);
			Debug.POLICY.logError(e, Messages.SPUTimingObserver_FailedParseFile, filename.toOSString());
			parsedTiming = null;
			return;
		}
		Debug.POLICY.trace(Debug.DEBUG_NOTIFICATIONS, "sputiming file parsed sucessfully", filename.toString()); //$NON-NLS-1$
		
		Activator.getDefault().setTiming(parsedTiming);
		Debug.POLICY.trace(Debug.DEBUG_NOTIFICATIONS, "Requested to update GUI", filename.toString()); //$NON-NLS-1$
	}
	
}
