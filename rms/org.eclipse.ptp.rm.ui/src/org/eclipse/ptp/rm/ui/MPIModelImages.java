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
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.utils.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

public class MPIModelImages {

	// ==== URLs for Icon Folders ==== 
	
	public final static URL PROCESS_URL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/process/"); //$NON-NLS-1$
	public final static URL JOB_URL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/job/"); //$NON-NLS-1$

	// ===== Icon Files =====

	public static final String IMG_PROC_ERROR = "proc_error.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_ERROR_SEL = "proc_error_sel.gif";  //$NON-NLS-1$
	public static final String IMG_PROC_EXITED = "proc_exited.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_EXITED_SEL = "proc_exited_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_EXITED_SIGNAL = "proc_exited_signal.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_EXITED_SIGNAL_SEL = "proc_exited_signal_sel.gif"; //$NON-NLS-1$
	
	public static final String IMG_JOB_ERROR = "job_error.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_ERROR = "debug_job_error.gif"; //$NON-NLS-1$

	public static final Map<String, Image> procImages= new HashMap<String, Image>();	
	public static final Map<String, Image> procSelImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobDebugImages= new HashMap<String, Image>();	

	static {
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_ERROR, IMG_PROC_ERROR);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_ERROR_SEL, IMG_PROC_ERROR_SEL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED, IMG_PROC_EXITED);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED_SEL, IMG_PROC_EXITED_SEL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED_SIGNAL, IMG_PROC_EXITED_SIGNAL);
		ImageManager.createImageDescriptor(PROCESS_URL, IMG_PROC_EXITED_SIGNAL_SEL, IMG_PROC_EXITED_SIGNAL_SEL);
	
		ImageManager.createImageDescriptor(JOB_URL, IMG_JOB_ERROR, IMG_JOB_ERROR);
		ImageManager.createImageDescriptor(JOB_URL, IMG_DEBUG_JOB_ERROR, IMG_DEBUG_JOB_ERROR);

		procImages.put(MPIProcessAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_PROC_ERROR));
		procImages.put(MPIProcessAttributes.Status.EXITED.toString(), ImageManager.getImage(IMG_PROC_EXITED));
		procImages.put(MPIProcessAttributes.Status.EXITED_SIGNAL.toString(), ImageManager.getImage(IMG_PROC_EXITED_SIGNAL));
		
		procSelImages.put(MPIProcessAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_PROC_ERROR_SEL));
		procSelImages.put(MPIProcessAttributes.Status.EXITED.toString(), ImageManager.getImage(IMG_PROC_EXITED_SEL));
		procSelImages.put(MPIProcessAttributes.Status.EXITED_SIGNAL.toString(), ImageManager.getImage(IMG_PROC_EXITED_SIGNAL_SEL));
		
		jobImages.put(MPIJobAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_JOB_ERROR));
		jobDebugImages.put(MPIJobAttributes.Status.ERROR.toString(), ImageManager.getImage(IMG_DEBUG_JOB_ERROR));
	}
}
