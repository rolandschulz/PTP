/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science,
 * National University of Defense Technology, P.R.China.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jie Jiang,National University of Defense Technology
 ******************************************************************************/

package org.eclipse.ptp.rm.slurm.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.slurm.core.SLURMMPIJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMMPINodeAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMMPIProcessAttributes;
import org.eclipse.ptp.utils.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

public class SLURMModelImages {
	// ==== URLs for Icon Folders ==== 
	
	public final static URL PROCESS_URL = Activator.getDefault().getBundle().getEntry("icons/process/"); //$NON-NLS-1$
	public final static URL JOB_URL = Activator.getDefault().getBundle().getEntry("icons/job/"); //$NON-NLS-1$
	public final static URL NODE_URL = Activator.getDefault().getBundle().getEntry("icons/node/"); //$NON-NLS-1$
	
	// ===== Icon Files =====

	//status images for SLURM
	public static final String SLURM_IMG_PROC_PENDING = "proc_pending.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_RUNNING = "proc_running.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_SUSPENDED = "proc_suspended.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_COMPLETED = "proc_completed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_CANCELLED = "proc_cancelled.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_FAILED = "proc_failed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_TIMEOUT = "proc_timeout.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_PROC_NODEFAIL = "proc_nodefail.gif"; //$NON-NLS-1$

	public static final String SLURM_IMG_JOB_PENDING = "job_pending.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_PENDING = "debug_job_pending.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_RUNNING = "job_running.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_RUNNING = "debug_job_running.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_SUSPENDED = "job_suspended.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_SUSPENDED = "debug_job_suspended.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_COMPLETED = "job_completed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_COMPLETED = "debug_job_completed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_CANCELLED = "job_cancelled.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_CANCELLED = "debug_job_cancelled.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_FAILED = "job_failed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_FAILED = "debug_job_failed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_TIMEOUT = "job_timeout.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_TIMEOUT = "debug_job_timeout.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_JOB_NODEFAIL = "job_nodefail.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_DEBUG_JOB_NODEFAIL = "debug_job_nodefail.gif"; //$NON-NLS-1$
	
	public static final String SLURM_IMG_NODE_IDLE = "node_up.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_ALLOCATED = "node_alloc.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_DOWN = "node_down.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_UNKNOWN = "node_unknown.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_MIXED = "node_mixed.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_FUTURE = "node_future.gif"; //$NON-NLS-1$
	public static final String SLURM_IMG_NODE_ERROR = "node_error.gif"; //$NON-NLS-1$

	public static final Map<String, Image> procImages= new HashMap<String, Image>();	
	public static final Map<String, Image> procSelImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobImages= new HashMap<String, Image>();	
	public static final Map<String, Image> jobDebugImages= new HashMap<String, Image>();
	public static final Map<String, Image> nodeImages= new HashMap<String, Image>();		

	static {		
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_PENDING, SLURM_IMG_PROC_PENDING);//$NON-NLS-1$	
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_RUNNING, SLURM_IMG_PROC_RUNNING);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_SUSPENDED, SLURM_IMG_PROC_SUSPENDED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_COMPLETED, SLURM_IMG_PROC_COMPLETED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_CANCELLED, SLURM_IMG_PROC_CANCELLED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_FAILED, SLURM_IMG_PROC_FAILED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_TIMEOUT, SLURM_IMG_PROC_TIMEOUT);//$NON-NLS-1$
		ImageManager.createImageDescriptor(PROCESS_URL, "SLURM_" + SLURM_IMG_PROC_NODEFAIL, SLURM_IMG_PROC_NODEFAIL);//$NON-NLS-1$

		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_PENDING, SLURM_IMG_JOB_PENDING);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" +  SLURM_IMG_DEBUG_JOB_PENDING, SLURM_IMG_DEBUG_JOB_PENDING);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_RUNNING, SLURM_IMG_JOB_RUNNING);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_RUNNING, SLURM_IMG_JOB_RUNNING);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_SUSPENDED, SLURM_IMG_JOB_SUSPENDED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_SUSPENDED, SLURM_IMG_DEBUG_JOB_SUSPENDED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_COMPLETED, SLURM_IMG_JOB_COMPLETED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_COMPLETED, SLURM_IMG_DEBUG_JOB_COMPLETED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_CANCELLED, SLURM_IMG_JOB_CANCELLED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_CANCELLED, SLURM_IMG_DEBUG_JOB_CANCELLED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_FAILED, SLURM_IMG_JOB_FAILED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_FAILED, SLURM_IMG_DEBUG_JOB_FAILED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_TIMEOUT, SLURM_IMG_JOB_TIMEOUT);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_TIMEOUT, SLURM_IMG_DEBUG_JOB_TIMEOUT);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_JOB_NODEFAIL, SLURM_IMG_JOB_NODEFAIL);//$NON-NLS-1$
		ImageManager.createImageDescriptor(JOB_URL, "SLURM_" + SLURM_IMG_DEBUG_JOB_NODEFAIL, SLURM_IMG_DEBUG_JOB_NODEFAIL);//$NON-NLS-1$
		
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_IDLE, SLURM_IMG_NODE_IDLE);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_ALLOCATED, SLURM_IMG_NODE_ALLOCATED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_DOWN, SLURM_IMG_NODE_DOWN);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_UNKNOWN, SLURM_IMG_NODE_UNKNOWN);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_MIXED, SLURM_IMG_NODE_MIXED);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_FUTURE, SLURM_IMG_NODE_FUTURE);//$NON-NLS-1$
		ImageManager.createImageDescriptor(NODE_URL, "SLURM_" + SLURM_IMG_NODE_ERROR, SLURM_IMG_NODE_ERROR);//$NON-NLS-1$
		
		procImages.put(SLURMMPIProcessAttributes.Status.PENDING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_PENDING));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.RUNNING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_RUNNING));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.SUSPENDED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_SUSPENDED));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.COMPLETED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_COMPLETED));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.CANCELLED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_CANCELLED));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.FAILED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_FAILED));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.TIMEOUT.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_TIMEOUT));//$NON-NLS-1$
		procImages.put(SLURMMPIProcessAttributes.Status.NODEFAIL.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_PROC_NODEFAIL));//$NON-NLS-1$
		
		jobImages.put(SLURMMPIJobAttributes.Status.PENDING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_PENDING));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.RUNNING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_RUNNING));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.SUSPENDED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_SUSPENDED));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.COMPLETED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_COMPLETED));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.CANCELLED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_CANCELLED));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.FAILED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_FAILED));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.TIMEOUT.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_TIMEOUT));//$NON-NLS-1$
		jobImages.put(SLURMMPIJobAttributes.Status.NODEFAIL.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_JOB_NODEFAIL));//$NON-NLS-1$
		
		jobDebugImages.put(SLURMMPIJobAttributes.Status.PENDING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_PENDING));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.RUNNING.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_RUNNING));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.SUSPENDED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_SUSPENDED));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.COMPLETED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_COMPLETED));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.CANCELLED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_CANCELLED));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.FAILED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_FAILED));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.TIMEOUT.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_TIMEOUT));//$NON-NLS-1$
		jobDebugImages.put(SLURMMPIJobAttributes.Status.NODEFAIL.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_DEBUG_JOB_NODEFAIL));//$NON-NLS-1$
		
		nodeImages.put(SLURMMPINodeAttributes.Status.IDLE.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_IDLE));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.ALLOCATED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_ALLOCATED));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.DOWN.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_DOWN));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.UNKNOWN.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_UNKNOWN));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.MIXED.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_MIXED));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.FUTURE.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_FUTURE));//$NON-NLS-1$
		nodeImages.put(SLURMMPINodeAttributes.Status.ERROR.toString(), ImageManager.getImage("SLURM_" + SLURM_IMG_NODE_ERROR));//$NON-NLS-1$
	}

}
