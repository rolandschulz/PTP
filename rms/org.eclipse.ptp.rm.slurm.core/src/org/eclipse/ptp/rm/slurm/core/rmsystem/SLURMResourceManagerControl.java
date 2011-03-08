/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 *  
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.slurm.core.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 5.0
 */
public class SLURMResourceManagerControl extends AbstractRuntimeResourceManagerControl {

	/**
	 * @since 5.0
	 */
	public SLURMResourceManagerControl(IResourceManagerConfiguration config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;)
	 */
	@Override
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		final String title = Messages.SLURMResourceManager_0;
		final String msg = e.getErrorMessage();

		// System.out.println("Job submit error!");
		// System.out.println(msg);
		/*
		 * see showErrorDialog(title, msg, status) in UIUtils.java;
		 */
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final Shell shell = Display.getDefault().getActiveShell();
				MessageDialog.openError(shell, title, msg);
			}
		});
	}
}