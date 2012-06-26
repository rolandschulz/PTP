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
 * 
 * Modified by:
 * 		Claudia Konbloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.ui;

import org.eclipse.ptp.rm.lml.core.ILMLCoreConstants;

public interface ILMLUIConstants extends ILMLCoreConstants {
	public static final String ID_SYSTEM_MONITORING_PERSPECTIVE = "org.eclipse.ptp.rm.lml.ui.SystemMonitoringPerspective"; //$NON-NLS-1$
	public static final String ID_SYSTEM_MONITOR_VIEW = "org.eclipse.ptp.rm.lml.ui.SystemMonitorView"; //$NON-NLS-1$
	public static final String ID_INFO_VIEW = "org.eclipse.ptp.rm.lml.ui.InfoView"; //$NON-NLS-1$

	public static final int INTERNAL_ERROR = 150;
	public static final String COLUMN_STATUS = "status";//$NON-NLS-1$

	public static final String INFO_JOB = "job"; //$NON-NLS-1$
	public static final String INFO_NODE = "node"; //$NON-NLS-1$
	public static final String INFO_MOTD = "motd"; //$NON-NLS-1$
	public static final String INFO_ERROR = "error"; //$NON-NLS-1$
	public static final String INFO_KEY = "Key"; //$NON-NLS-1$
	public static final String INFO_VALUES = "Value"; //$NON-NLS-1$

	public static final String VERIFY_TEXT_NUMERIC = "0|([1-9][0-9]*)"; //$NON-NLS-1$
	public static final String VERIFY_TEXT_ALPHA = ".+"; //$NON-NLS-1$
	public static final String TEXT_SEPARATOR = " - "; //$NON-NLS-1$
}
