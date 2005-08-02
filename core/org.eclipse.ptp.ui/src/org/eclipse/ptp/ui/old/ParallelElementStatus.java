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
package org.eclipse.ptp.ui.old;

/**
 *
 */
public interface ParallelElementStatus {
    public final static int NODE_USER_ALLOC_EXCL = 0;
    public final static int NODE_USER_ALLOC_SHARED = 1;
    public final static int NODE_OTHER_ALLOC_EXCL = 2;
    public final static int NODE_OTHER_ALLOC_SHARED = 3;
    public final static int NODE_DOWN = 4;
    public final static int NODE_ERROR = 5;
    public final static int NODE_EXITED = 6;
    public final static int NODE_RUNNING = 7;
    public final static int NODE_UNKNOWN = 8;
    public final static int NODE_UP = 9;
    public final static int PROC_ERROR = 10;
    public final static int PROC_EXITED = 11;
    public final static int PROC_EXITED_SIGNAL = 12;
    public final static int PROC_RUNNING = 13;
    public final static int PROC_STARTING = 14;
    public final static int PROC_STOPPED = 15;
}
