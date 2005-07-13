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
package org.eclipse.ptp.core;

public interface PreferenceConstants {
	public static final String MONITORING_SYSTEM_SELECTION = "MONITORING_SYSTEM_SELECTION";
	
	public static final String OUTPUT_DIR = "OUTPUT_DIR";

	public static final String STORE_LINE = "STORE_LINE";

	public int DEF_STORE_LINE = 100;

	public String DEF_OUTPUT_DIR_NAME = "output";
	
	public static final String ORTE_ORTED_PATH = "ORTE_ORTED_PATH";
	
	public static final String ORTE_ORTED_ARGS = "ORTE_ORTED_ARGS";
}
