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
package org.eclipse.ptp.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class ParallelImages {
	private static final String NAME_PREFIX = "org.eclipse.pdt.ui.";
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
    
	private static URL iconBaseURL = null;
	
	static {
		String pathSuffix = "icons/";
		iconBaseURL = ParallelPlugin.getDefault().getBundle().getEntry(pathSuffix);
	}
	
	// The plugin registry
	private static ImageRegistry imageRegistry = null;
	private static HashMap avoidSWTErrorMap = null;

	public static final String IMG_PARALLEL = NAME_PREFIX + "parallel.gif";
	
	public static final String IMG_PARALLEL_TAB = NAME_PREFIX + "parallel_tab.gif";
	public static final String IMG_ARGUMENT_TAB = NAME_PREFIX + "arguments_tab.gif";
	public static final String IMG_MAIN_TAB = NAME_PREFIX + "main_tab.gif";

	public static final String IMG_TERMINATE_ACTION_NORMAL = NAME_PREFIX + "icon_terminate_all.gif";
	public static final String IMG_TERMINATE_ACTION_DISABLE = NAME_PREFIX + "icon_terminate_all.gif";
	public static final String IMG_TERMINATE_ACTION_HOVER = NAME_PREFIX + "icon_terminate_all.gif";	
	
	public static final String IMG_SEARCH_ACTION_NORMAL = NAME_PREFIX + "search.gif";
	public static final String IMG_SEARCH_ACTION_DISABLE = NAME_PREFIX + "search_dim.gif";
	public static final String IMG_SEARCH_ACTION_HOVER = NAME_PREFIX + "search.gif";	
	
	public static final String IMG_SHOWALLNODES_ACTION_NORMAL = NAME_PREFIX + "all_nodes.gif";
	public static final String IMG_SHOWALLNODES_ACTION_DISABLE = NAME_PREFIX + "all_nodes.gif";
	public static final String IMG_SHOWALLNODES_ACTION_HOVER = NAME_PREFIX + "all_nodes.gif";	
	
	public static final String IMG_SHOWMYALLOCNODES_ACTION_NORMAL = NAME_PREFIX + "alloc_nodes.gif";
	public static final String IMG_SHOWMYALLOCNODES_ACTION_DISABLE = NAME_PREFIX + "alloc_nodes.gif";
	public static final String IMG_SHOWMYALLOCNODES_ACTION_HOVER = NAME_PREFIX + "alloc_nodes.gif";	
	
	public static final String IMG_SHOWMYUSEDNODES_ACTION_NORMAL = NAME_PREFIX + "used_nodes.gif";
	public static final String IMG_SHOWMYUSEDNODES_ACTION_DISABLE = NAME_PREFIX + "used_nodes.gif";
	public static final String IMG_SHOWMYUSEDNODES_ACTION_HOVER = NAME_PREFIX + "used_nodes.gif";	
	
	public static final String IMG_SHOWLEGEND_ACTION_NORMAL = NAME_PREFIX + "legend.gif";
	public static final String IMG_SHOWLEGEND_ACTION_DISABLE = NAME_PREFIX + "legend.gif";
	public static final String IMG_SHOWLEGEND_ACTION_HOVER = NAME_PREFIX + "legend.gif";	
	
	public static final String IMG_SHOWPROCESSES_ACTION_NORMAL = NAME_PREFIX + "processes.gif";
	public static final String IMG_SHOWPROCESSES_ACTION_DISABLE = NAME_PREFIX + "processes.gif";
	public static final String IMG_SHOWPROCESSES_ACTION_HOVER = NAME_PREFIX + "processes.gif";	

	public static final String IMG_NODE_USER_ALLOC_EXCL = NAME_PREFIX + "node_user_excl.gif";
	public static final String IMG_NODE_USER_ALLOC_EXCL_SEL = NAME_PREFIX + "node_user_excl_sel.gif";
	public static final String IMG_NODE_USER_ALLOC_SHARED = NAME_PREFIX + "node_user_shared.gif";
	public static final String IMG_NODE_USER_ALLOC_SHARED_SEL = NAME_PREFIX + "node_user_shared_sel.gif";
	public static final String IMG_NODE_OTHER_ALLOC_EXCL = NAME_PREFIX + "node_other_excl.gif";
	public static final String IMG_NODE_OTHER_ALLOC_EXCL_SEL = NAME_PREFIX + "node_other_excl_sel.gif";
	public static final String IMG_NODE_OTHER_ALLOC_SHARED = NAME_PREFIX + "node_other_shared.gif";
	public static final String IMG_NODE_OTHER_ALLOC_SHARED_SEL = NAME_PREFIX + "node_other_shared_sel.gif";
	public static final String IMG_NODE_DOWN = NAME_PREFIX + "node_down.gif";
	public static final String IMG_NODE_DOWN_SEL = NAME_PREFIX + "node_down_sel.gif";
	public static final String IMG_NODE_ERROR = NAME_PREFIX + "node_error.gif";
	public static final String IMG_NODE_ERROR_SEL = NAME_PREFIX + "node_error_sel.gif";
	public static final String IMG_NODE_EXITED = NAME_PREFIX + "node_exited.gif";
	public static final String IMG_NODE_EXITED_SEL = NAME_PREFIX + "node_exited_sel.gif";
	public static final String IMG_NODE_RUNNING = NAME_PREFIX + "node_running.gif";
	public static final String IMG_NODE_RUNNING_SEL = NAME_PREFIX + "node_running_sel.gif";
	public static final String IMG_NODE_UNKNOWN = NAME_PREFIX + "node_unknown.gif";
	public static final String IMG_NODE_UNKNOWN_SEL = NAME_PREFIX + "node_unknown_sel.gif";
	public static final String IMG_NODE_UP = NAME_PREFIX + "node_up.gif";
	public static final String IMG_NODE_UP_SEL = NAME_PREFIX + "node_up_sel.gif";
	
	public static final String IMG_PROC_ERROR = NAME_PREFIX + "proc_error.gif";
	public static final String IMG_PROC_ERROR_SEL = NAME_PREFIX + "proc_error_sel.gif"; 
	public static final String IMG_PROC_EXITED = NAME_PREFIX + "proc_exited.gif";
	public static final String IMG_PROC_EXITED_SEL = NAME_PREFIX + "proc_exited_sel.gif";
	public static final String IMG_PROC_EXITED_SIGNAL = NAME_PREFIX + "proc_exited_signal.gif";
	public static final String IMG_PROC_EXITED_SIGNAL_SEL = NAME_PREFIX + "proc_exited_signal_sel.gif";
	public static final String IMG_PROC_RUNNING = NAME_PREFIX + "proc_running.gif";
	public static final String IMG_PROC_RUNNING_SEL = NAME_PREFIX + "proc_running_sel.gif";
	public static final String IMG_PROC_STARTING = NAME_PREFIX + "proc_starting.gif";
	public static final String IMG_PROC_STARTING_SEL = NAME_PREFIX + "proc_starting_sel.gif";
	public static final String IMG_PROC_STOPPED = NAME_PREFIX + "proc_stopped.gif";
	public static final String IMG_PROC_STOPPED_SEL = NAME_PREFIX + "proc_stopped_sel.gif";
	
	
	
	
	/*
	public static final String IMG_ABORT_ACTION_NORMAL = NAME_PREFIX + "icon_terminate_all.gif";
	public static final String IMG_ABORT_ACTION_DISABLE = NAME_PREFIX + "icon_terminate_all.gif";
	public static final String IMG_ABORT_ACTION_HOVER = NAME_PREFIX + "icon_terminate_all.gif";	
	public static final String IMG_EXIT_ACTION_NORMAL = NAME_PREFIX + "exit_normal.gif";
	public static final String IMG_EXIT_ACTION_DISABLE = NAME_PREFIX + "exit_disable.gif";
	public static final String IMG_EXIT_ACTION_HOVER = NAME_PREFIX + "exit_hover.gif";
	public static final String IMG_VIEWSTATUS_ACTION_NORMAL = NAME_PREFIX + "viewstatus_normal.gif";
	public static final String IMG_VIEWSTATUS_ACTION_DISABLE = NAME_PREFIX + "viewstatus_disable.gif";
	public static final String IMG_VIEWSTATUS_ACTION_HOVER = NAME_PREFIX + "viewstatus_hover.gif";

	public static final String IMG_NODE = NAME_PREFIX + "node.gif";
	public static final String IMG_PROCESS = NAME_PREFIX + "process.gif";
	*/	
	
	public static final ImageDescriptor DESC_PARALLEL = createManaged(IMG_PARALLEL);
	
	public static final ImageDescriptor DESC_PARALLEL_TAB = createManaged(IMG_PARALLEL_TAB);
	public static final ImageDescriptor DESC_ARGUMENT_TAB = createManaged(IMG_ARGUMENT_TAB);
	public static final ImageDescriptor DESC_MAIN_TAB = createManaged(IMG_MAIN_TAB);

	public static final ImageDescriptor DESC_TERMINATE_ACTION_NORMAL = createManaged(IMG_TERMINATE_ACTION_NORMAL);
	public static final ImageDescriptor DESC_TERMINATE_ACTION_DISABLE = createManaged(IMG_TERMINATE_ACTION_DISABLE);
	public static final ImageDescriptor DESC_TERMINATE_ACTION_HOVER = createManaged(IMG_TERMINATE_ACTION_HOVER);
	
	public static final ImageDescriptor DESC_SEARCH_ACTION_NORMAL = createManaged(IMG_SEARCH_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SEARCH_ACTION_DISABLE = createManaged(IMG_SEARCH_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SEARCH_ACTION_HOVER = createManaged(IMG_SEARCH_ACTION_HOVER);
	
	public static final ImageDescriptor DESC_SHOWALLNODES_ACTION_NORMAL = createManaged(IMG_SHOWALLNODES_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SHOWALLNODES_ACTION_DISABLE = createManaged(IMG_SHOWALLNODES_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SHOWALLNODES_ACTION_HOVER = createManaged(IMG_SHOWALLNODES_ACTION_HOVER);
	
	public static final ImageDescriptor DESC_SHOWMYALLOCNODES_ACTION_NORMAL = createManaged(IMG_SHOWMYALLOCNODES_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SHOWMYALLOCNODES_ACTION_DISABLE = createManaged(IMG_SHOWMYALLOCNODES_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SHOWMYALLOCNODES_ACTION_HOVER = createManaged(IMG_SHOWMYALLOCNODES_ACTION_HOVER);	
	
	public static final ImageDescriptor DESC_SHOWMYUSEDNODES_ACTION_NORMAL = createManaged(IMG_SHOWMYUSEDNODES_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SHOWMYUSEDNODES_ACTION_DISABLE = createManaged(IMG_SHOWMYUSEDNODES_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SHOWMYUSEDNODES_ACTION_HOVER = createManaged(IMG_SHOWMYUSEDNODES_ACTION_HOVER);	
	
	public static final ImageDescriptor DESC_SHOWLEGEND_ACTION_NORMAL = createManaged(IMG_SHOWLEGEND_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SHOWLEGEND_ACTION_DISABLE = createManaged(IMG_SHOWLEGEND_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SHOWLEGEND_ACTION_HOVER = createManaged(IMG_SHOWLEGEND_ACTION_HOVER);

	public static final ImageDescriptor DESC_SHOWPROCESSES_ACTION_NORMAL = createManaged(IMG_SHOWPROCESSES_ACTION_NORMAL);
	public static final ImageDescriptor DESC_SHOWPROCESSES_ACTION_DISABLE = createManaged(IMG_SHOWPROCESSES_ACTION_DISABLE);
	public static final ImageDescriptor DESC_SHOWPROCESSES_ACTION_HOVER = createManaged(IMG_SHOWPROCESSES_ACTION_HOVER);
	
	public static final ImageDescriptor DESC_NODE_USER_ALLOC_EXCL = createManaged(IMG_NODE_USER_ALLOC_EXCL);
	public static final ImageDescriptor DESC_NODE_USER_ALLOC_EXCL_SEL = createManaged(IMG_NODE_USER_ALLOC_EXCL_SEL);
	public static final ImageDescriptor DESC_NODE_USER_ALLOC_SHARED = createManaged(IMG_NODE_USER_ALLOC_SHARED);
	public static final ImageDescriptor DESC_NODE_USER_ALLOC_SHARED_SEL = createManaged(IMG_NODE_USER_ALLOC_SHARED_SEL);
	public static final ImageDescriptor DESC_NODE_OTHER_ALLOC_EXCL = createManaged(IMG_NODE_OTHER_ALLOC_EXCL);
	public static final ImageDescriptor DESC_NODE_OTHER_ALLOC_EXCL_SEL = createManaged(IMG_NODE_OTHER_ALLOC_EXCL_SEL);
	public static final ImageDescriptor DESC_NODE_OTHER_ALLOC_SHARED = createManaged(IMG_NODE_OTHER_ALLOC_SHARED);
	public static final ImageDescriptor DESC_NODE_OTHER_ALLOC_SHARED_SEL = createManaged(IMG_NODE_OTHER_ALLOC_SHARED_SEL);
	public static final ImageDescriptor DESC_NODE_DOWN = createManaged(IMG_NODE_DOWN);
	public static final ImageDescriptor DESC_NODE_DOWN_SEL = createManaged(IMG_NODE_DOWN_SEL);
	public static final ImageDescriptor DESC_NODE_ERROR = createManaged(IMG_NODE_ERROR);
	public static final ImageDescriptor DESC_NODE_ERROR_SEL = createManaged(IMG_NODE_ERROR_SEL);
	public static final ImageDescriptor DESC_NODE_EXITED = createManaged(IMG_NODE_EXITED);
	public static final ImageDescriptor DESC_NODE_EXITED_SEL = createManaged(IMG_NODE_EXITED_SEL);
	public static final ImageDescriptor DESC_NODE_RUNNING = createManaged(IMG_NODE_RUNNING);
	public static final ImageDescriptor DESC_NODE_RUNNING_SEL = createManaged(IMG_NODE_RUNNING_SEL);
	public static final ImageDescriptor DESC_NODE_UNKNOWN = createManaged(IMG_NODE_UNKNOWN);
	public static final ImageDescriptor DESC_NODE_UNKNOWN_SEL = createManaged(IMG_NODE_UNKNOWN_SEL);
	public static final ImageDescriptor DESC_NODE_UP = createManaged(IMG_NODE_UP);
	public static final ImageDescriptor DESC_NODE_UP_SEL = createManaged(IMG_NODE_UP_SEL);
	
	public static final ImageDescriptor DESC_PROC_ERROR = createManaged(IMG_PROC_ERROR);
	public static final ImageDescriptor DESC_PROC_ERROR_SEL = createManaged(IMG_PROC_ERROR_SEL);
	public static final ImageDescriptor DESC_PROC_EXITED = createManaged(IMG_PROC_EXITED);
	public static final ImageDescriptor DESC_PROC_EXITED_SEL = createManaged(IMG_PROC_EXITED_SEL);
	public static final ImageDescriptor DESC_PROC_EXITED_SIGNAL = createManaged(IMG_PROC_EXITED_SIGNAL);
	public static final ImageDescriptor DESC_PROC_EXITED_SIGNAL_SEL = createManaged(IMG_PROC_EXITED_SIGNAL_SEL);
	public static final ImageDescriptor DESC_PROC_RUNNING = createManaged(IMG_PROC_RUNNING);
	public static final ImageDescriptor DESC_PROC_RUNNING_SEL = createManaged(IMG_PROC_RUNNING_SEL);
	public static final ImageDescriptor DESC_PROC_STARTING = createManaged(IMG_PROC_STARTING);
	public static final ImageDescriptor DESC_PROC_STARTING_SEL = createManaged(IMG_PROC_STARTING_SEL);
	public static final ImageDescriptor DESC_PROC_STOPPED = createManaged(IMG_PROC_STOPPED);
	public static final ImageDescriptor DESC_PROC_STOPPED_SEL = createManaged(IMG_PROC_STOPPED_SEL);
	
	

	
	
	
	/*
	public static final ImageDescriptor DESC_EXIT_ACTION_NORMAL = createManaged(IMG_EXIT_ACTION_NORMAL);
	public static final ImageDescriptor DESC_EXIT_ACTION_DISABLE = createManaged(IMG_EXIT_ACTION_DISABLE);
	public static final ImageDescriptor DESC_EXIT_ACTION_HOVER = createManaged(IMG_EXIT_ACTION_HOVER);
	public static final ImageDescriptor DESC_ABORT_ACTION_NORMAL = createManaged(IMG_ABORT_ACTION_NORMAL);
	public static final ImageDescriptor DESC_ABORT_ACTION_DISABLE = createManaged(IMG_ABORT_ACTION_DISABLE);
	public static final ImageDescriptor DESC_ABORT_ACTION_HOVER = createManaged(IMG_ABORT_ACTION_HOVER);
	public static final ImageDescriptor DESC_VIEWSTATUS_ACTION_NORMAL = createManaged(IMG_VIEWSTATUS_ACTION_NORMAL);
	public static final ImageDescriptor DESC_VIEWSTATUS_ACTION_DISABLE = createManaged(IMG_VIEWSTATUS_ACTION_DISABLE);
	public static final ImageDescriptor DESC_VIEWSTATUS_ACTION_HOVER = createManaged(IMG_VIEWSTATUS_ACTION_HOVER);
	
	public static final ImageDescriptor DESC_IMG_NODE = createManaged(IMG_NODE);
	public static final ImageDescriptor DESC_IMG_PROCESS = createManaged(IMG_PROCESS);
	*/
	
	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator iter = avoidSWTErrorMap.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor) avoidSWTErrorMap.get(key));
			}
			avoidSWTErrorMap = null;
		}
		return imageRegistry;
	}
	
	private static ImageDescriptor createManaged(String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(name.substring(NAME_PREFIX_LENGTH)));
			if (avoidSWTErrorMap == null) {
				avoidSWTErrorMap = new HashMap(); 
			}
			avoidSWTErrorMap.put(name, result);
			if (imageRegistry != null) {
			    System.out.println("Internal Error: Image registry already defined");
			}
			return result;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static ImageDescriptor create(String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL makeIconFileURL(String name) throws MalformedURLException {
		if (iconBaseURL == null)
			throw new MalformedURLException();
			
		return new URL(iconBaseURL, name);
	}	
}
