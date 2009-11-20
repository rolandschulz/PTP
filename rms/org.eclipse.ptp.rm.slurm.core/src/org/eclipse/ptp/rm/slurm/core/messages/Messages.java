/*******************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science,
 *  National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.slurm.core.messages.messages"; //$NON-NLS-1$

	public static String SLURMJobAttributes_0;
	public static String SLURMJobAttributes_1;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}

