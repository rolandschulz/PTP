package org.eclipse.ptp.ui;

/**
 * @author clement
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
