package org.eclipse.ptp.rm.ui;
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


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.core.MPIJobAttributes;
import org.eclipse.ptp.rm.core.MPIProcessAttributes;
import org.eclipse.ptp.utils.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

public class RMModelImages {

	// ==== URLs for Icon Folders ==== 
	
	public final static URL PROCESS_URL = RMUIPlugin.getDefault().getBundle().getEntry("icons/process/"); //$NON-NLS-1$
	public final static URL JOB_URL = RMUIPlugin.getDefault().getBundle().getEntry("icons/job/"); //$NON-NLS-1$
	public final static URL NODE_URL = RMUIPlugin.getDefault().getBundle().getEntry("icons/node/"); //$NON-NLS-1$

	// ===== Icon Files =====

	public static final String IMG_PROC_ERROR = "proc_error.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_ERROR_SEL = "proc_error_sel.gif";  //$NON-NLS-1$
	public static final String IMG_PROC_EXITED_SIGNAL = "proc_exited_signal.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_EXITED_SIGNAL_SEL = "proc_exited_signal_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_UNKNOWN = "proc_unknown.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_UNKNOWN_SEL = "proc_unknown_sel.gif"; //$NON-NLS-1$
	
	public static final String IMG_JOB_ERROR = "job_error.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_ERROR = "debug_job_error.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_STARTED = "job_started.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_STARTED = "debug_job_started.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_UNKNOWN = "job_unknown.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_UNKNOWN = "debug_job_unknown.gif"; //$NON-NLS-1$

	public static final String IMG_NODE_ALLOC_OTHER = "node_alloc_other.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_ALLOC_OTHER_SEL = "node_alloc_other_sel.gif";  //$NON-NLS-1$
	public static final String IMG_NODE_ALLOC_USER = "node_alloc_user.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_ALLOC_USER_SEL = "node_alloc_user_sel.gif";  //$NON-NLS-1$
	public static final String IMG_NODE_OTHER_EXCLUSIVE = "node_other_excl.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_OTHER_EXCLUSIVE_SEL = "node_other_excl_sel.gif";  //$NON-NLS-1$
	public static final String IMG_NODE_OTHER_SHARED = "node_other_shared.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_OTHER_SHARED_SEL = "node_other_shared_sel.gif";  //$NON-NLS-1$
	public static final String IMG_NODE_USER_EXCLUSIVE = "node_user_excl.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_USER_EXCLUSIVE_SEL = "node_user_excl_sel.gif";  //$NON-NLS-1$
	public static final String IMG_NODE_USER_SHARED = "node_user_shared.gif"; //$NON-NLS-1$
	public static final String IMG_NODE_USER_SHARED_SEL = "node_user_shared_sel.gif";  //$NON-NLS-1$

	public static final Map<String, Image> procImages= new HashMap<String, Image>();	
	public static final Map<String, Image> procSelImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobDebugImages= new HashMap<String, Image>();	

	static {
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_ERROR, IMG_PROC_ERROR);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_ERROR_SEL, IMG_PROC_ERROR_SEL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED_SIGNAL, IMG_PROC_EXITED_SIGNAL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED_SIGNAL_SEL, IMG_PROC_EXITED_SIGNAL_SEL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_UNKNOWN, IMG_PROC_UNKNOWN);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_UNKNOWN_SEL, IMG_PROC_UNKNOWN_SEL);
	
		ImageManager.createImageDescriptor(JOB_URL, IMG_JOB_ERROR, IMG_JOB_ERROR);
		ImageManager.createImageDescriptor(JOB_URL, IMG_DEBUG_JOB_ERROR, IMG_DEBUG_JOB_ERROR);
		ImageManager.createImageDescriptor(JOB_URL, IMG_JOB_STARTED, IMG_JOB_STARTED);
		ImageManager.createImageDescriptor(JOB_URL, IMG_DEBUG_JOB_STARTED, IMG_DEBUG_JOB_STARTED);
		ImageManager.createImageDescriptor(JOB_URL, IMG_JOB_UNKNOWN, IMG_JOB_UNKNOWN);
		ImageManager.createImageDescriptor(JOB_URL, IMG_DEBUG_JOB_UNKNOWN, IMG_DEBUG_JOB_UNKNOWN);

		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_ALLOC_OTHER, IMG_NODE_ALLOC_OTHER);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_ALLOC_OTHER_SEL, IMG_NODE_ALLOC_OTHER_SEL);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_ALLOC_USER, IMG_NODE_ALLOC_USER);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_ALLOC_USER_SEL, IMG_NODE_ALLOC_USER_SEL);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_OTHER_EXCLUSIVE, IMG_NODE_OTHER_EXCLUSIVE);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_OTHER_EXCLUSIVE_SEL, IMG_NODE_OTHER_EXCLUSIVE_SEL);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_OTHER_SHARED, IMG_NODE_OTHER_SHARED);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_OTHER_SHARED_SEL, IMG_NODE_OTHER_SHARED_SEL);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_USER_EXCLUSIVE, IMG_NODE_USER_EXCLUSIVE);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_USER_EXCLUSIVE_SEL, IMG_NODE_USER_EXCLUSIVE_SEL);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_USER_SHARED, IMG_NODE_USER_SHARED);
		ImageManager.createImageDescriptor(JOB_URL, IMG_NODE_USER_SHARED_SEL, IMG_NODE_USER_SHARED_SEL);

		procImages.put(MPIProcessAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_PROC_ERROR));
		procImages.put(MPIProcessAttributes.Status.EXITED_SIGNAL.toString(), ImageManager.getImage(IMG_PROC_EXITED_SIGNAL));
		
		procSelImages.put(MPIProcessAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_PROC_ERROR_SEL));
		procSelImages.put(MPIProcessAttributes.Status.EXITED_SIGNAL.toString(), ImageManager.getImage(IMG_PROC_EXITED_SIGNAL_SEL));
		
		jobImages.put(MPIJobAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_JOB_ERROR));
		jobDebugImages.put(MPIJobAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_DEBUG_JOB_ERROR));
	}
}
