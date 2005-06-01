/*******************************************************************************
 *
 * (c) 2002 Copyright RedHat Inc
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     RedHat Inc - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ptp.debug.mi.core.gdb.command;

/**
 * 
 *    -target-download
 *
 *  Load the executable to the remote target.  This command takes no args.
 *
 *
 *   Loads the executable onto the remote target. It prints out an
 *   update message every half second, which includes the fields:
 * 
 *  +download,{section=".text",section-size="6668",total-size="9880"}
 *  +download,{section=".text",section-sent="512",section-size="6668",
 *  total-sent="512",total-size="9880"}
 * 
 */
public class MITargetDownload extends MICommand 
{
	public MITargetDownload() {
		super("-target-download"); //$NON-NLS-1$
	}
}
