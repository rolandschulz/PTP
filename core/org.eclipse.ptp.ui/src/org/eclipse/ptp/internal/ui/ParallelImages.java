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
package org.eclipse.ptp.internal.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.utils.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

public class ParallelImages {

	// ==== URLs for Icon Folders ==== 
	
	public final static URL ICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/"); //$NON-NLS-1$
	public final static URL TOOLICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/tool/"); //$NON-NLS-1$
	public final static URL JOBICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/job/"); //$NON-NLS-1$
	public final static URL PROCESSICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/process/"); //$NON-NLS-1$
	public final static URL NODEICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/node/"); //$NON-NLS-1$
	public final static URL MACHINEICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/machine/"); //$NON-NLS-1$
	public final static URL RMICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/rm/"); //$NON-NLS-1$
	
	// ===== Icon Files =====

	// CHANGE/DELETE SET
	public static final String ICON_CHANGESET_NORMAL = "changeset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_CREATESET_NORMAL = "createset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_DELETESET_NORMAL = "deleteset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_DELETEELEMENT_NORMAL = "deleteelement_normal.gif"; //$NON-NLS-1$

	//ZOOM
	public static final String ICON_ZOOMIN_NORMAL = "zoomin_normal.gif"; //$NON-NLS-1$
	public static final String ICON_ZOOMOUT_NORMAL = "zoomout_normal.gif"; //$NON-NLS-1$
	
	// TOOLBAR
	public static final String ICON_TERMINATE_JOB_DISABLE = "terminate_job_disable.gif"; //$NON-NLS-1$
	public static final String ICON_TERMINATE_JOB_NORMAL = "terminate_job_normal.gif"; //$NON-NLS-1$
	public static final String ICON_REMOVEALLTERMINATED_NORMAL = "remove_all_terminated_normal.gif"; //$NON-NLS-1$
	public static final String ICON_REMOVETERMINATED_NORMAL = "remove_terminated_normal.gif"; //$NON-NLS-1$
	public static final String ICON_JOB_FOCUS_DISABLE = "job_focus_disable.gif"; //$NON-NLS-1$
	public static final String ICON_JOB_FOCUS_ENABLE = "job_focus_enable.gif"; //$NON-NLS-1$
	
	// SHOW LEGEND
	public static final String ICON_SHOWLEGEND_ACTION_NORMAL =  "legend.gif"; //$NON-NLS-1$

	// JOB
	public static final String IMG_JOB_STARTING = "job_pending.gif"; //$NON-NLS-1$
//	public static final String IMG_JOB_STARTED = "job_started.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_RUNNING = "job_running.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_COMPLETED = "job_terminated.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_SUSPENDED = "job_suspended.gif"; //$NON-NLS-1$
//	public static final String IMG_JOB_ERROR = "job_error.gif"; //$NON-NLS-1$
//	public static final String IMG_JOB_UNKNOWN = "job_unknown.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_STARTING = "debug_job_pending.gif"; //$NON-NLS-1$
//	public static final String IMG_DEBUG_JOB_STARTED = "debug_job_started.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_RUNNING = "debug_job_running.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_COMPLETED = "debug_job_terminated.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_SUSPENDED = "debug_job_suspended.gif"; //$NON-NLS-1$
//	public static final String IMG_DEBUG_JOB_ERROR = "debug_job_error.gif"; //$NON-NLS-1$
//	public static final String IMG_DEBUG_JOB_UNKNOWN = "debug_job_unknown.gif"; //$NON-NLS-1$
	
	// NODE
//	public static final String IMG_NODE_USER_ALLOC_EXCL = "node_user_excl.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_USER_ALLOC_EXCL_SEL = "node_user_excl_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_USER_ALLOC_SHARED = "node_user_shared.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_USER_ALLOC_SHARED_SEL = "node_user_shared_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_OTHER_ALLOC_EXCL = "node_other_excl.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_OTHER_ALLOC_EXCL_SEL = "node_other_excl_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_OTHER_ALLOC_SHARED = "node_other_shared.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_OTHER_ALLOC_SHARED_SEL = "node_other_shared_sel.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_DOWN = "node_down.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_DOWN_SEL = "node_down_sel.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_ERROR = "node_error.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_ERROR_SEL = "node_error_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_EXITED_PROCESS = "node_exited.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_EXITED_PROCESS_SEL = "node_exited_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_RUNNING_PROCESS = "node_running.gif"; //$NON-NLS-1$
//	public static final String IMG_NODE_RUNNING_PROCESS_SEL = "node_running_sel.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_UNKNOWN = "node_unknown.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_UNKNOWN_SEL = "node_unknown_sel.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_UP = "node_up.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_UP_SEL = "node_up_sel.gif"; //$NON-NLS-1$
	
	// PROCESS
//	public static final String IMG_PROC_ERROR = "proc_error.gif"; //$NON-NLS-1$
//	public static final String IMG_PROC_ERROR_SEL = "proc_error_sel.gif";  //$NON-NLS-1$
	public static final String IMG_PROC_COMPLETED = "proc_exited.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_COMPLETED_SEL = "proc_exited_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_PROC_EXITED_SIGNAL = "proc_exited_signal.gif"; //$NON-NLS-1$
//	public static final String IMG_PROC_EXITED_SIGNAL_SEL = "proc_exited_signal_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_RUNNING = "proc_running.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_RUNNING_SEL = "proc_running_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_STARTING = "proc_starting.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_STARTING_SEL = "proc_starting_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_SUSPENDED = "proc_suspended.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_SUSPENDED_SEL = "proc_suspended_sel.gif"; //$NON-NLS-1$
//	public static final String IMG_PROC_UNKNOWN = "proc_unknown.gif"; //$NON-NLS-1$
//	public static final String IMG_PROC_UNKNOWN_SEL = "proc_unknown_sel.gif"; //$NON-NLS-1$
	
	// MACHINE
	public static final String IMG_MACHINE_UNKNOWN = "machine_unknown.gif"; //$NON-NLS-1$
	public static final String IMG_MACHINE_UP = "machine_up.gif"; //$NON-NLS-1$
	public static final String IMG_MACHINE_DOWN = "machine_down.gif"; //$NON-NLS-1$
	public static final String IMG_MACHINE_ALERT = "machine_alert.gif"; //$NON-NLS-1$
	public static final String IMG_MACHINE_ERROR = "machine_error.gif"; //$NON-NLS-1$

	// RESOURCE MANAGERS
	public static final String IMG_RM_STOPPED = "rm_stopped.gif"; //$NON-NLS-1$
	public static final String IMG_RM_STARTED = "rm_started.gif"; //$NON-NLS-1$
	public static final String IMG_RM_STARTING = "rm_starting.gif"; //$NON-NLS-1$
	public static final String IMG_RM_STOPPING = "rm_stopping.gif"; //$NON-NLS-1$
	public static final String IMG_RM_SUSPENDED = "rm_suspended.gif"; //$NON-NLS-1$
	public static final String IMG_RM_ERROR = "rm_error.gif"; //$NON-NLS-1$
	public static final String IMG_RM_START = "start.gif"; //$NON-NLS-1$
	public static final String IMG_RM_STOP = "stop.gif"; //$NON-NLS-1$
	
	// ==== Image Descriptors ====
	
	// CHANGE/DELETE SET
	public static final ImageDescriptor ID_ICON_CHANGESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_CHANGESET_NORMAL, ICON_CHANGESET_NORMAL);
	public static final ImageDescriptor ID_ICON_CREATESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_CREATESET_NORMAL, ICON_CREATESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_DELETESET_NORMAL, ICON_DELETESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETEELEMENT_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_DELETEELEMENT_NORMAL, ICON_DELETEELEMENT_NORMAL);

	//ZOOM
	public static final ImageDescriptor ID_ICON_ZOOMIN_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_ZOOMIN_NORMAL, ICON_ZOOMIN_NORMAL);
	public static final ImageDescriptor ID_ICON_ZOOMOUT_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_ZOOMOUT_NORMAL, ICON_ZOOMOUT_NORMAL);

	// TOOLBAR
	public static final ImageDescriptor ID_ICON_TERMINATE_JOB_DISABLE = ImageManager.createImageDescriptor(TOOLICONURL, ICON_TERMINATE_JOB_DISABLE, ICON_TERMINATE_JOB_DISABLE);
	public static final ImageDescriptor ID_ICON_TERMINATE_JOB_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_TERMINATE_JOB_NORMAL, ICON_TERMINATE_JOB_NORMAL);
	public static final ImageDescriptor ID_ICON_REMOVEALLTERMINATED_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_REMOVEALLTERMINATED_NORMAL, ICON_REMOVEALLTERMINATED_NORMAL);
	public static final ImageDescriptor ID_ICON_REMOVETERMINATED_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_REMOVETERMINATED_NORMAL, ICON_REMOVETERMINATED_NORMAL);
	public static final ImageDescriptor ID_ICON_JOB_FOCUS_DISABLE = ImageManager.createImageDescriptor(TOOLICONURL, ICON_JOB_FOCUS_DISABLE, ICON_JOB_FOCUS_DISABLE);
	public static final ImageDescriptor ID_ICON_JOB_FOCUS_ENABLE = ImageManager.createImageDescriptor(TOOLICONURL, ICON_JOB_FOCUS_ENABLE, ICON_JOB_FOCUS_ENABLE);
	
	// SHOW LEGEND
	public static final ImageDescriptor ID_ICON_SHOWLEGEND_ACTION_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_SHOWLEGEND_ACTION_NORMAL, ICON_SHOWLEGEND_ACTION_NORMAL);

	// JOB
	public static final ImageDescriptor ID_IMG_JOB_STARTING = ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_STARTING, IMG_JOB_STARTING);
//	public static final ImageDescriptor ID_IMG_JOB_STARTED = createImageDescriptor(JOBICONURL, IMG_JOB_STARTED, IMG_JOB_STARTED);
	public static final ImageDescriptor ID_IMG_JOB_RUNNING = ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_RUNNING, IMG_JOB_RUNNING);
	public static final ImageDescriptor ID_IMG_JOB_COMPLETED = ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_COMPLETED, IMG_JOB_COMPLETED);
	public static final ImageDescriptor ID_IMG_JOB_SUSPENDED = ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_SUSPENDED, IMG_JOB_SUSPENDED);
//	public static final ImageDescriptor ID_IMG_JOB_ERROR = createImageDescriptor(JOBICONURL, IMG_JOB_ERROR, IMG_JOB_ERROR);
//	public static final ImageDescriptor ID_IMG_JOB_UNKNOWN = createImageDescriptor(JOBICONURL, IMG_JOB_UNKNOWN, IMG_JOB_UNKNOWN);
	public static final ImageDescriptor ID_IMG_DEBUG_JOB_STARTING = ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_STARTING, IMG_DEBUG_JOB_STARTING);
//	public static final ImageDescriptor ID_IMG_DEBUG_JOB_STARTED = createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_STARTED, IMG_DEBUG_JOB_STARTED);
	public static final ImageDescriptor ID_IMG_DEBUG_JOB_RUNNING = ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_RUNNING, IMG_DEBUG_JOB_RUNNING);
	public static final ImageDescriptor ID_IMG_DEBUG_JOB_COMPLETED = ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_COMPLETED, IMG_DEBUG_JOB_COMPLETED);
	public static final ImageDescriptor ID_IMG_DEBUG_JOB_SUSPENDED = ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_SUSPENDED, IMG_DEBUG_JOB_SUSPENDED);
//	public static final ImageDescriptor ID_IMG_DEBUG_JOB_ERROR = createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_ERROR, IMG_DEBUG_JOB_ERROR);
//	public static final ImageDescriptor ID_IMG_DEBUG_JOB_UNKNOWN = createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_UNKNOWN, IMG_DEBUG_JOB_UNKNOWN);

	// NODE
//	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_EXCL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_EXCL, IMG_NODE_USER_ALLOC_EXCL);
//	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_EXCL_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_EXCL_SEL, IMG_NODE_USER_ALLOC_EXCL_SEL);
//	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_SHARED = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_SHARED, IMG_NODE_USER_ALLOC_SHARED);
//	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_SHARED_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_SHARED_SEL, IMG_NODE_USER_ALLOC_SHARED_SEL);
//	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_EXCL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_EXCL, IMG_NODE_OTHER_ALLOC_EXCL);
//	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_EXCL_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_EXCL_SEL, IMG_NODE_OTHER_ALLOC_EXCL_SEL);
//	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_SHARED = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_SHARED, IMG_NODE_OTHER_ALLOC_SHARED);
//	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_SHARED_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_SHARED_SEL, IMG_NODE_OTHER_ALLOC_SHARED_SEL);
	public static final ImageDescriptor ID_IMG_NODE_DOWN = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_DOWN, IMG_NODE_DOWN);
	public static final ImageDescriptor ID_IMG_NODE_DOWN_SEL = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_DOWN_SEL, IMG_NODE_DOWN_SEL);
	public static final ImageDescriptor ID_IMG_NODE_ERROR = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_ERROR, IMG_NODE_ERROR);
	public static final ImageDescriptor ID_IMG_NODE_ERROR_SEL = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_ERROR_SEL, IMG_NODE_ERROR_SEL);
//	public static final ImageDescriptor ID_IMG_NODE_EXITED_PROCESS = createImageDescriptor(NODEICONURL, IMG_NODE_EXITED_PROCESS, IMG_NODE_EXITED_PROCESS);
//	public static final ImageDescriptor ID_IMG_NODE_EXITED_PROCESS_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_EXITED_PROCESS_SEL, IMG_NODE_EXITED_PROCESS_SEL);
//	public static final ImageDescriptor ID_IMG_NODE_RUNNING_PROCESS = createImageDescriptor(NODEICONURL, IMG_NODE_RUNNING_PROCESS, IMG_NODE_RUNNING_PROCESS);
//	public static final ImageDescriptor ID_IMG_NODE_RUNNING_PROCESS_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_RUNNING_PROCESS_SEL, IMG_NODE_RUNNING_PROCESS_SEL);
	public static final ImageDescriptor ID_IMG_NODE_UNKNOWN = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_UNKNOWN, IMG_NODE_UNKNOWN);
	public static final ImageDescriptor ID_IMG_NODE_UNKNOWN_SEL = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_UNKNOWN_SEL, IMG_NODE_UNKNOWN_SEL);
	public static final ImageDescriptor ID_IMG_NODE_UP = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_UP, IMG_NODE_UP);
	public static final ImageDescriptor ID_IMG_NODE_UP_SEL = ImageManager.createImageDescriptor(NODEICONURL, IMG_NODE_UP_SEL, IMG_NODE_UP_SEL);

	// PROCESS
//	public static final ImageDescriptor ID_IMG_PROC_ERROR = createImageDescriptor(PROCESSICONURL, IMG_PROC_ERROR, IMG_PROC_ERROR);
//	public static final ImageDescriptor ID_IMG_PROC_ERROR_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_ERROR_SEL, IMG_PROC_ERROR_SEL);
	public static final ImageDescriptor ID_IMG_PROC_COMPLETED = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_COMPLETED, IMG_PROC_COMPLETED);
	public static final ImageDescriptor ID_IMG_PROC_COMPLETED_SEL = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_COMPLETED_SEL, IMG_PROC_COMPLETED_SEL);
//	public static final ImageDescriptor ID_IMG_PROC_EXITED_SIGNAL = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED_SIGNAL, IMG_PROC_EXITED_SIGNAL);
//	public static final ImageDescriptor ID_IMG_PROC_EXITED_SIGNAL_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED_SIGNAL_SEL, IMG_PROC_EXITED_SIGNAL_SEL);
	public static final ImageDescriptor ID_IMG_PROC_RUNNING = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING, IMG_PROC_RUNNING);
	public static final ImageDescriptor ID_IMG_PROC_RUNNING_SEL = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING_SEL, IMG_PROC_RUNNING_SEL);
	public static final ImageDescriptor ID_IMG_PROC_STARTING = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING, IMG_PROC_STARTING);
	public static final ImageDescriptor ID_IMG_PROC_STARTING_SEL = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING_SEL, IMG_PROC_STARTING_SEL);
	public static final ImageDescriptor ID_IMG_PROC_SUSPENDED = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_SUSPENDED, IMG_PROC_SUSPENDED);
	public static final ImageDescriptor ID_IMG_PROC_SUSPENDED_SEL = ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_SUSPENDED_SEL, IMG_PROC_SUSPENDED_SEL);
//	public static final ImageDescriptor ID_IMG_PROC_UNKNOWN = createImageDescriptor(PROCESSICONURL, IMG_PROC_UNKNOWN, IMG_PROC_UNKNOWN);
//	public static final ImageDescriptor ID_IMG_PROC_UNKNOWN_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_UNKNOWN_SEL, IMG_PROC_UNKNOWN_SEL);
	
	// MACHINE
	public static final ImageDescriptor ID_IMG_MACHINE_UNKNOWN = ImageManager.createImageDescriptor(MACHINEICONURL, IMG_MACHINE_UNKNOWN, IMG_MACHINE_UNKNOWN);
	public static final ImageDescriptor ID_IMG_MACHINE_UP = ImageManager.createImageDescriptor(MACHINEICONURL, IMG_MACHINE_UP, IMG_MACHINE_UP);
	public static final ImageDescriptor ID_IMG_MACHINE_DOWN = ImageManager.createImageDescriptor(MACHINEICONURL, IMG_MACHINE_DOWN, IMG_MACHINE_DOWN);
	public static final ImageDescriptor ID_IMG_MACHINE_ALERT = ImageManager.createImageDescriptor(MACHINEICONURL, IMG_MACHINE_ALERT, IMG_MACHINE_ALERT);
	public static final ImageDescriptor ID_IMG_MACHINE_ERROR = ImageManager.createImageDescriptor(MACHINEICONURL, IMG_MACHINE_ERROR, IMG_MACHINE_ERROR);

	// RESOURCE MANAGERS
	public static final ImageDescriptor ID_IMG_RM_STOPPED = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_STOPPED, IMG_RM_STOPPED);
	public static final ImageDescriptor ID_IMG_RM_STARTED = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_STARTED, IMG_RM_STARTED);
	public static final ImageDescriptor ID_IMG_RM_STARTING = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_STARTING, IMG_RM_STARTING);
	public static final ImageDescriptor ID_IMG_RM_STOPPING = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_STOPPING, IMG_RM_STOPPING);
	public static final ImageDescriptor ID_IMG_RM_SUSPENDED = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_SUSPENDED, IMG_RM_SUSPENDED);
	public static final ImageDescriptor ID_IMG_RM_ERROR = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_ERROR, IMG_RM_ERROR);
	public static final ImageDescriptor ID_IMG_RM_START= ImageManager.createImageDescriptor(RMICONURL, IMG_RM_START, IMG_RM_START);
	public static final ImageDescriptor ID_IMG_RM_STOP = ImageManager.createImageDescriptor(RMICONURL, IMG_RM_STOP, IMG_RM_STOP);
	
	// ==== Image Arrays ====
	
	// NOTE: The order of images in these arrays must correspond to the ordinal
	// values of the element state attributes.
	
	// NODE
	public static final Image[][] nodeImages = {
		{ ImageManager.getImage(ParallelImages.IMG_NODE_UP),		ImageManager.getImage(ParallelImages.IMG_NODE_UP_SEL)		},             
		{ ImageManager.getImage(ParallelImages.IMG_NODE_DOWN),		ImageManager.getImage(ParallelImages.IMG_NODE_DOWN_SEL)		},
		{ ImageManager.getImage(ParallelImages.IMG_NODE_ERROR),		ImageManager.getImage(ParallelImages.IMG_NODE_ERROR_SEL)		},
		{ ImageManager.getImage(ParallelImages.IMG_NODE_UNKNOWN),	ImageManager.getImage(ParallelImages.IMG_NODE_UNKNOWN_SEL)	},
	};

	// JOB
	public static Image[][] jobImages = {
		{ ImageManager.getImage(ParallelImages.IMG_JOB_STARTING),	ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_STARTING)	},
		{ ImageManager.getImage(ParallelImages.IMG_JOB_RUNNING),	ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_RUNNING)	},
		{ ImageManager.getImage(ParallelImages.IMG_JOB_SUSPENDED),	ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_SUSPENDED)},
		{ ImageManager.getImage(ParallelImages.IMG_JOB_COMPLETED),	ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_COMPLETED)}
	};
	
	// PROCESS
	public static final Image[][] procImages = {
		{ ImageManager.getImage(ParallelImages.IMG_PROC_STARTING),	ImageManager.getImage(ParallelImages.IMG_PROC_STARTING_SEL)	},
		{ ImageManager.getImage(ParallelImages.IMG_PROC_RUNNING),	ImageManager.getImage(ParallelImages.IMG_PROC_RUNNING_SEL)	},
		{ ImageManager.getImage(ParallelImages.IMG_PROC_SUSPENDED),	ImageManager.getImage(ParallelImages.IMG_PROC_SUSPENDED_SEL)	},
		{ ImageManager.getImage(ParallelImages.IMG_PROC_COMPLETED),	ImageManager.getImage(ParallelImages.IMG_PROC_COMPLETED)		}
	};

	// MACHINE
	public static Image[] machineImages = {
		ImageManager.getImage(ParallelImages.IMG_MACHINE_UP),
		ImageManager.getImage(ParallelImages.IMG_MACHINE_DOWN),
		ImageManager.getImage(ParallelImages.IMG_MACHINE_ALERT),
		ImageManager.getImage(ParallelImages.IMG_MACHINE_ERROR),
		ImageManager.getImage(ParallelImages.IMG_MACHINE_UNKNOWN)
	};

	// RESOURCE MANAGERS
	public static Image[] rmImages = {
		ImageManager.getImage(ParallelImages.IMG_RM_STARTING),
		ImageManager.getImage(ParallelImages.IMG_RM_STARTED),
		ImageManager.getImage(ParallelImages.IMG_RM_STOPPING),
		ImageManager.getImage(ParallelImages.IMG_RM_STOPPED),
		ImageManager.getImage(ParallelImages.IMG_RM_SUSPENDED),
		ImageManager.getImage(ParallelImages.IMG_RM_ERROR),
	};
}
