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

/**
 *
 */
public interface IPTPLaunchConfigurationConstants {
    public static final String PTP_LAUNCHCONFIGURETYPE_ID = "org.eclipse.ptp.launch.parallelLaunch";
	public static final String PTP_LAUNCH_ID = "org.eclipse.ptp.launch";
	
    public static final String P_TYPE = "P";
    public static final String G_TYPE = "G";	

    public static final String ATTR_PROJECT_NAME = PTP_LAUNCH_ID + ".PROJECT_ATTR";
	public static final String ATTR_APPLICATION_NAME = PTP_LAUNCH_ID + ".APPLICATION_NAME";
    public static final String ATTR_STOP_IN_MAIN = PTP_LAUNCH_ID + ".STOP_IN_MAIN";

    public static final String NUMBER_OF_PROCESSES = PTP_LAUNCH_ID + ".NUMBER_OF_PROCESSES";
    //public static final String NETWORK_TYPE = PTP_LAUNCH_ID + ".COMMUNICATION";
    public static final String PROCESSES_PER_NODE = PTP_LAUNCH_ID + ".NUMBER_OF_PROCESSES_START";
    public static final String FIRST_NODE_NUMBER = PTP_LAUNCH_ID + ".NODE_NUMBER";

    public static final String ATTR_ARGUMENT = PTP_LAUNCH_ID + ".ARGUMENT_ATTR";
    public static final String ATTR_WORK_DIRECTORY = PTP_LAUNCH_ID + ".WORK_DIRECTORY_ATTR";

    public static final String DEF_NUMBER_OF_PROCESSES = "0";
    public static final String DEF_NETWORK_TYPE = P_TYPE;
    public static final String DEF_PROCESSES_PER_NODE = "1";
    public static final String DEF_FIRST_NODE_NUMBER = "0";	
}
