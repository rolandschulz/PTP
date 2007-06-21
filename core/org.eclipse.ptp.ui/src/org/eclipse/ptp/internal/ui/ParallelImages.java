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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.graphics.Image;

public class ParallelImages {

	// ==== URLs for Icon Folders ==== 
	
	public final static URL ICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/");
	public final static URL TOOLICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/tool/");
	public final static URL PROCESSICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/process/");
	public final static URL NODEICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/node/");
	public final static URL MACHINEICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/machine/");
	public final static URL RMICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/rm/");
	
	// ===== Icon Files =====

	// CHANGE/DELETE SET
	public static final String ICON_CHANGESET_NORMAL = "changeset_normal.gif";
	public static final String ICON_CREATESET_NORMAL = "createset_normal.gif";
	public static final String ICON_DELETESET_NORMAL = "deleteset_normal.gif";
	public static final String ICON_DELETEELEMENT_NORMAL = "deleteelement_normal.gif";

	//ZOOM
	public static final String ICON_ZOOMIN_NORMAL = "zoomin_normal.gif";
	public static final String ICON_ZOOMOUT_NORMAL = "zoomout_normal.gif";
	
	// TOOLBAR
	public static final String ICON_MACHINE_NORMAL = "machine_normal.gif";
	public static final String ICON_JOB_NORMAL = "job_normal.gif";
	public static final String ICON_TERMINATE_ALL_DISABLE = "terminate_all_disable.gif";
	public static final String ICON_TERMINATE_ALL_NORMAL = "terminate_all_normal.gif";
	public static final String ICON_RUNMODE_NORMAL = "runmode_normal.gif";
	public static final String ICON_DEBUGMODE_NORMAL = "debugmode_normal.gif";
	public static final String ICON_REMOVEALLTERMINATED_NORMAL = "remove_all_terminated_normal.gif";

	// SHOW LEGEND
	public static final String ICON_SHOWLEGEND_ACTION_NORMAL =  "legend.gif";
	
	// NODE
	public static final String IMG_NODE_USER_ALLOC_EXCL = "node_user_excl.gif";
	public static final String IMG_NODE_USER_ALLOC_EXCL_SEL = "node_user_excl_sel.gif";
	public static final String IMG_NODE_USER_ALLOC_SHARED = "node_user_shared.gif";
	public static final String IMG_NODE_USER_ALLOC_SHARED_SEL = "node_user_shared_sel.gif";
	public static final String IMG_NODE_OTHER_ALLOC_EXCL = "node_other_excl.gif";
	public static final String IMG_NODE_OTHER_ALLOC_EXCL_SEL = "node_other_excl_sel.gif";
	public static final String IMG_NODE_OTHER_ALLOC_SHARED = "node_other_shared.gif";
	public static final String IMG_NODE_OTHER_ALLOC_SHARED_SEL = "node_other_shared_sel.gif";
	public static final String IMG_NODE_DOWN = "node_down.gif";
	public static final String IMG_NODE_DOWN_SEL = "node_down_sel.gif";
	public static final String IMG_NODE_ERROR = "node_error.gif";
	public static final String IMG_NODE_ERROR_SEL = "node_error_sel.gif";
	public static final String IMG_NODE_EXITED = "node_exited.gif";
	public static final String IMG_NODE_EXITED_SEL = "node_exited_sel.gif";
	public static final String IMG_NODE_RUNNING = "node_running.gif";
	public static final String IMG_NODE_RUNNING_SEL = "node_running_sel.gif";
	public static final String IMG_NODE_UNKNOWN = "node_unknown.gif";
	public static final String IMG_NODE_UNKNOWN_SEL = "node_unknown_sel.gif";
	public static final String IMG_NODE_UP = "node_up.gif";
	public static final String IMG_NODE_UP_SEL = "node_up_sel.gif";
	
	// PROCESS
	public static final String IMG_PROC_ERROR = "proc_error.gif";
	public static final String IMG_PROC_ERROR_SEL = "proc_error_sel.gif"; 
	public static final String IMG_PROC_EXITED = "proc_exited.gif";
	public static final String IMG_PROC_EXITED_SEL = "proc_exited_sel.gif";
	public static final String IMG_PROC_EXITED_SIGNAL = "proc_exited_signal.gif";
	public static final String IMG_PROC_EXITED_SIGNAL_SEL = "proc_exited_signal_sel.gif";
	public static final String IMG_PROC_RUNNING = "proc_running.gif";
	public static final String IMG_PROC_RUNNING_SEL = "proc_running_sel.gif";
	public static final String IMG_PROC_STARTING = "proc_starting.gif";
	public static final String IMG_PROC_STARTING_SEL = "proc_starting_sel.gif";
	public static final String IMG_PROC_STOPPED = "proc_stopped.gif";
	public static final String IMG_PROC_STOPPED_SEL = "proc_stopped_sel.gif";
	
	// MACHINE
	public static final String IMG_MACHINE_UNKNOWN = "machine_unknown.gif";
	public static final String IMG_MACHINE_UP = "machine_up.gif";
	public static final String IMG_MACHINE_DOWN = "machine_down.gif";
	public static final String IMG_MACHINE_ALERT = "machine_alert.gif";

	// RESOURCE MANAGERS
	public static final String IMG_RM_STOPPED = "rm_stopped.gif";
	public static final String IMG_RM_STARTED = "rm_started.gif";
	public static final String IMG_RM_STARTING = "rm_starting.gif";
	public static final String IMG_RM_STOPPING = "rm_stopping.gif";
	public static final String IMG_RM_ERROR = "rm_error.gif";
	public static final String IMG_RM_START = "start.gif";
	public static final String IMG_RM_STOP = "stop.gif";
	
	// ==== Image Descriptors ====
	
	// CHANGE/DELETE SET
	public static final ImageDescriptor ID_ICON_CHANGESET_NORMAL = createImageDescriptor(TOOLICONURL, ICON_CHANGESET_NORMAL, ICON_CHANGESET_NORMAL);
	public static final ImageDescriptor ID_ICON_CREATESET_NORMAL = createImageDescriptor(TOOLICONURL, ICON_CREATESET_NORMAL, ICON_CREATESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETESET_NORMAL = createImageDescriptor(TOOLICONURL, ICON_DELETESET_NORMAL, ICON_DELETESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETEELEMENT_NORMAL = createImageDescriptor(TOOLICONURL, ICON_DELETEELEMENT_NORMAL, ICON_DELETEELEMENT_NORMAL);

	//ZOOM
	public static final ImageDescriptor ID_ICON_ZOOMIN_NORMAL = createImageDescriptor(TOOLICONURL, ICON_ZOOMIN_NORMAL, ICON_ZOOMIN_NORMAL);
	public static final ImageDescriptor ID_ICON_ZOOMOUT_NORMAL = createImageDescriptor(TOOLICONURL, ICON_ZOOMOUT_NORMAL, ICON_ZOOMOUT_NORMAL);

	// TOOLBAR
	public static final ImageDescriptor ID_ICON_MACHINE_NORMAL = createImageDescriptor(TOOLICONURL, ICON_MACHINE_NORMAL, ICON_MACHINE_NORMAL);
	public static final ImageDescriptor ID_ICON_JOB_NORMAL = createImageDescriptor(TOOLICONURL, ICON_JOB_NORMAL, ICON_JOB_NORMAL);
	public static final ImageDescriptor ID_ICON_TERMINATE_ALL_DISABLE = createImageDescriptor(TOOLICONURL, ICON_TERMINATE_ALL_DISABLE, ICON_TERMINATE_ALL_DISABLE);
	public static final ImageDescriptor ID_ICON_TERMINATE_ALL_NORMAL = createImageDescriptor(TOOLICONURL, ICON_TERMINATE_ALL_NORMAL, ICON_TERMINATE_ALL_NORMAL);
	public static final ImageDescriptor ID_ICON_RUNMODE_NORMAL = createImageDescriptor(TOOLICONURL, ICON_RUNMODE_NORMAL, ICON_RUNMODE_NORMAL);
	public static final ImageDescriptor ID_ICON_DEBUGMODE_NORMAL = createImageDescriptor(TOOLICONURL, ICON_DEBUGMODE_NORMAL, ICON_DEBUGMODE_NORMAL);
	public static final ImageDescriptor ID_ICON_REMOVEALLTERMINATED_NORMAL = createImageDescriptor(TOOLICONURL, ICON_REMOVEALLTERMINATED_NORMAL, ICON_REMOVEALLTERMINATED_NORMAL);

	// SHOW LEGEND
	public static final ImageDescriptor ID_ICON_SHOWLEGEND_ACTION_NORMAL = createImageDescriptor(TOOLICONURL, ICON_SHOWLEGEND_ACTION_NORMAL, ICON_SHOWLEGEND_ACTION_NORMAL);

	// NODE
	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_EXCL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_EXCL, IMG_NODE_USER_ALLOC_EXCL);
	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_EXCL_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_EXCL_SEL, IMG_NODE_USER_ALLOC_EXCL_SEL);
	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_SHARED = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_SHARED, IMG_NODE_USER_ALLOC_SHARED);
	public static final ImageDescriptor ID_IMG_NODE_USER_ALLOC_SHARED_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_USER_ALLOC_SHARED_SEL, IMG_NODE_USER_ALLOC_SHARED_SEL);
	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_EXCL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_EXCL, IMG_NODE_OTHER_ALLOC_EXCL);
	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_EXCL_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_EXCL_SEL, IMG_NODE_OTHER_ALLOC_EXCL_SEL);
	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_SHARED = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_SHARED, IMG_NODE_OTHER_ALLOC_SHARED);
	public static final ImageDescriptor ID_IMG_NODE_OTHER_ALLOC_SHARED_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_OTHER_ALLOC_SHARED_SEL, IMG_NODE_OTHER_ALLOC_SHARED_SEL);
	public static final ImageDescriptor ID_IMG_NODE_DOWN = createImageDescriptor(NODEICONURL, IMG_NODE_DOWN, IMG_NODE_DOWN);
	public static final ImageDescriptor ID_IMG_NODE_DOWN_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_DOWN_SEL, IMG_NODE_DOWN_SEL);
	public static final ImageDescriptor ID_IMG_NODE_ERROR = createImageDescriptor(NODEICONURL, IMG_NODE_ERROR, IMG_NODE_ERROR);
	public static final ImageDescriptor ID_IMG_NODE_ERROR_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_ERROR_SEL, IMG_NODE_ERROR_SEL);
	public static final ImageDescriptor ID_IMG_NODE_EXITED = createImageDescriptor(NODEICONURL, IMG_NODE_EXITED, IMG_NODE_EXITED);
	public static final ImageDescriptor ID_IMG_NODE_EXITED_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_EXITED_SEL, IMG_NODE_EXITED_SEL);
	public static final ImageDescriptor ID_IMG_NODE_RUNNING = createImageDescriptor(NODEICONURL, IMG_NODE_RUNNING, IMG_NODE_RUNNING);
	public static final ImageDescriptor ID_IMG_NODE_RUNNING_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_RUNNING_SEL, IMG_NODE_RUNNING_SEL);
	public static final ImageDescriptor ID_IMG_NODE_UNKNOWN = createImageDescriptor(NODEICONURL, IMG_NODE_UNKNOWN, IMG_NODE_UNKNOWN);
	public static final ImageDescriptor ID_IMG_NODE_UNKNOWN_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_UNKNOWN_SEL, IMG_NODE_UNKNOWN_SEL);
	public static final ImageDescriptor ID_IMG_NODE_UP = createImageDescriptor(NODEICONURL, IMG_NODE_UP, IMG_NODE_UP);
	public static final ImageDescriptor ID_IMG_NODE_UP_SEL = createImageDescriptor(NODEICONURL, IMG_NODE_UP_SEL, IMG_NODE_UP_SEL);

	// PROCESS
	public static final ImageDescriptor ID_IMG_PROC_ERROR = createImageDescriptor(PROCESSICONURL, IMG_PROC_ERROR, IMG_PROC_ERROR);
	public static final ImageDescriptor ID_IMG_PROC_ERROR_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_ERROR_SEL, IMG_PROC_ERROR_SEL);
	public static final ImageDescriptor ID_IMG_PROC_EXITED = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED, IMG_PROC_EXITED);
	public static final ImageDescriptor ID_IMG_PROC_EXITED_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED_SEL, IMG_PROC_EXITED_SEL);
	public static final ImageDescriptor ID_IMG_PROC_EXITED_SIGNAL = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED_SIGNAL, IMG_PROC_EXITED_SIGNAL);
	public static final ImageDescriptor ID_IMG_PROC_EXITED_SIGNAL_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_EXITED_SIGNAL_SEL, IMG_PROC_EXITED_SIGNAL_SEL);
	public static final ImageDescriptor ID_IMG_PROC_RUNNING = createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING, IMG_PROC_RUNNING);
	public static final ImageDescriptor ID_IMG_PROC_RUNNING_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING_SEL, IMG_PROC_RUNNING_SEL);
	public static final ImageDescriptor ID_IMG_PROC_STARTING = createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING, IMG_PROC_STARTING);
	public static final ImageDescriptor ID_IMG_PROC_STARTING_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING_SEL, IMG_PROC_STARTING_SEL);
	public static final ImageDescriptor ID_IMG_PROC_STOPPED = createImageDescriptor(PROCESSICONURL, IMG_PROC_STOPPED, IMG_PROC_STOPPED);
	public static final ImageDescriptor ID_IMG_PROC_STOPPED_SEL = createImageDescriptor(PROCESSICONURL, IMG_PROC_STOPPED_SEL, IMG_PROC_STOPPED_SEL);
	
	// MACHINE
	public static final ImageDescriptor ID_IMG_MACHINE_UNKNOWN = createImageDescriptor(MACHINEICONURL, IMG_MACHINE_UNKNOWN, IMG_MACHINE_UNKNOWN);
	public static final ImageDescriptor ID_IMG_MACHINE_UP = createImageDescriptor(MACHINEICONURL, IMG_MACHINE_UP, IMG_MACHINE_UP);
	public static final ImageDescriptor ID_IMG_MACHINE_DOWN = createImageDescriptor(MACHINEICONURL, IMG_MACHINE_DOWN, IMG_MACHINE_DOWN);
	public static final ImageDescriptor ID_IMG_MACHINE_ALERT = createImageDescriptor(MACHINEICONURL, IMG_MACHINE_ALERT, IMG_MACHINE_ALERT);

	// RESOURCE MANAGERS
	public static final ImageDescriptor ID_IMG_RM_STOPPED = createImageDescriptor(RMICONURL, IMG_RM_STOPPED, IMG_RM_STOPPED);
	public static final ImageDescriptor ID_IMG_RM_STARTED = createImageDescriptor(RMICONURL, IMG_RM_STARTED, IMG_RM_STARTED);
	public static final ImageDescriptor ID_IMG_RM_STARTING = createImageDescriptor(RMICONURL, IMG_RM_STARTING, IMG_RM_STARTING);
	public static final ImageDescriptor ID_IMG_RM_STOPPING = createImageDescriptor(RMICONURL, IMG_RM_STOPPING, IMG_RM_STOPPING);
	public static final ImageDescriptor ID_IMG_RM_ERROR = createImageDescriptor(RMICONURL, IMG_RM_ERROR, IMG_RM_ERROR);
	public static final ImageDescriptor ID_IMG_RM_START= createImageDescriptor(RMICONURL, IMG_RM_START, IMG_RM_START);
	public static final ImageDescriptor ID_IMG_RM_STOP = createImageDescriptor(RMICONURL, IMG_RM_STOP, IMG_RM_STOP);
	
	// ==== Image Arrays ====
	
	// NODE
	public static final Image[][] nodeImages = {
		{ getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL),	getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL_SEL)		},
		{ getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED),	getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED_SEL)		},
		{ getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL),	getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL_SEL)		},
		{ getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED),	getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED_SEL)	},
		{ getImage(ParallelImages.IMG_NODE_DOWN),				getImage(ParallelImages.IMG_NODE_DOWN_SEL)					},
		{ getImage(ParallelImages.IMG_NODE_ERROR),				getImage(ParallelImages.IMG_NODE_ERROR_SEL)					},
		{ getImage(ParallelImages.IMG_NODE_EXITED),				getImage(ParallelImages.IMG_NODE_EXITED_SEL)				},
		{ getImage(ParallelImages.IMG_NODE_RUNNING),			getImage(ParallelImages.IMG_NODE_RUNNING_SEL)				},
		{ getImage(ParallelImages.IMG_NODE_UNKNOWN),			getImage(ParallelImages.IMG_NODE_UNKNOWN_SEL)				},
		{ getImage(ParallelImages.IMG_NODE_UP),					getImage(ParallelImages.IMG_NODE_UP_SEL)					}              
	};

	// JOB
	public static Image[] jobImages = {
		getImage(ParallelImages.ICON_RUNMODE_NORMAL),
		getImage(ParallelImages.ICON_DEBUGMODE_NORMAL),
		getImage(ParallelImages.ICON_TERMINATE_ALL_NORMAL)
	};
	
	// PROCESS
	public static final Image[][] procImages = {
		{ getImage(ParallelImages.IMG_PROC_ERROR),			getImage(ParallelImages.IMG_PROC_ERROR_SEL)				},
		{ getImage(ParallelImages.IMG_PROC_EXITED),			getImage(ParallelImages.IMG_PROC_EXITED_SEL)			},
		{ getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL),	getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL_SEL)		},
		{ getImage(ParallelImages.IMG_PROC_RUNNING),		getImage(ParallelImages.IMG_PROC_RUNNING_SEL)			},
		{ getImage(ParallelImages.IMG_PROC_STARTING),		getImage(ParallelImages.IMG_PROC_STARTING_SEL)			},
		{ getImage(ParallelImages.IMG_PROC_STOPPED),		getImage(ParallelImages.IMG_PROC_STOPPED_SEL)			}
	};

	// MACHINE
	public static Image[] machineImages = {
		getImage(ParallelImages.IMG_MACHINE_UNKNOWN),
		getImage(ParallelImages.IMG_MACHINE_UP),
		getImage(ParallelImages.IMG_MACHINE_DOWN),
		getImage(ParallelImages.IMG_MACHINE_ALERT)
	};
	
	// RESOURCE MANAGERS
	public static Image[] rmImages = {
		getImage(ParallelImages.IMG_RM_STOPPED),
		getImage(ParallelImages.IMG_RM_STARTED),
		getImage(ParallelImages.IMG_RM_STARTING),
		getImage(ParallelImages.IMG_RM_STOPPING),
		getImage(ParallelImages.IMG_RM_ERROR)
	};
	
    private static ImageRegistry imageRegistry = null;

	/** Get image
	 * @param key
	 * @return
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	/** Get image descriptor
	 * @param key
	 * @return
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	/** Get image registry
	 * @return
	 */
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}
	
	/** add image 
	 * @param key
	 * @param image
	 */
	public static void addImage(String key, Image image) {
		getImageRegistry().put(key, image);
	}
	/** add image descriptor
	 * @param key
	 * @param imageDescriptor
	 */
	public static void addImageDescriptor(String key, ImageDescriptor imageDescriptor) {
		getImageRegistry().put(key, imageDescriptor);
	}
		
	/** Create image descriptor
	 * @param iconURL
	 * @param key
	 * @param name
	 * @return
	 */
	public static ImageDescriptor createImageDescriptor(URL iconURL, String key, String name) {
		try {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(getIconURL(iconURL, name));
			addImageDescriptor(key, imageDescriptor);
			return imageDescriptor;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	/** Get icon url
	 * @param iconURL
	 * @param name
	 * @return
	 * @throws MalformedURLException
	 */
	private static URL getIconURL(URL iconURL, String name) throws MalformedURLException {
		if (iconURL == null)
			throw new MalformedURLException();
			
		return new URL(iconURL, name);
	}
}
