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
import org.eclipse.ptp.utils.ui.ImageManager;
import org.eclipse.swt.graphics.Image;

public class ParallelImages {

	// ==== URLs for Icon Folders ====

	public final static URL TOOLICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/tool/"); //$NON-NLS-1$
	public final static URL JOBICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/job/"); //$NON-NLS-1$
	public final static URL PROCESSICONURL = PTPUIPlugin.getDefault().getBundle().getEntry("icons/process/"); //$NON-NLS-1$

	// ===== Icon Files =====

	// CHANGE/DELETE SET
	public static final String ICON_CHANGESET_NORMAL = "changeset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_CREATESET_NORMAL = "createset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_DELETESET_NORMAL = "deleteset_normal.gif"; //$NON-NLS-1$
	public static final String ICON_DELETEELEMENT_NORMAL = "deleteelement_normal.gif"; //$NON-NLS-1$

	// ZOOM
	public static final String ICON_ZOOMIN_NORMAL = "zoomin_normal.gif"; //$NON-NLS-1$
	public static final String ICON_ZOOMOUT_NORMAL = "zoomout_normal.gif"; //$NON-NLS-1$

	// TOOLBAR
	public static final String ICON_REMOVEALLTERMINATED_NORMAL = "remove_all_terminated_normal.gif"; //$NON-NLS-1$

	// JOB
	public static final String IMG_JOB_STARTING = "job_starting.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_RUNNING = "job_running.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_COMPLETED = "job_completed.gif"; //$NON-NLS-1$
	public static final String IMG_JOB_SUSPENDED = "job_suspended.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_STARTING = "debug_job_starting.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_RUNNING = "debug_job_running.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_COMPLETED = "debug_job_completed.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_JOB_SUSPENDED = "debug_job_suspended.gif"; //$NON-NLS-1$

	// PROCESS
	public static final String IMG_PROC_COMPLETED = "proc_completed.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_COMPLETED_SEL = "proc_completed_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_RUNNING = "proc_running.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_RUNNING_SEL = "proc_running_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_STARTING = "proc_starting.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_STARTING_SEL = "proc_starting_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_SUSPENDED = "proc_suspended.gif"; //$NON-NLS-1$
	public static final String IMG_PROC_SUSPENDED_SEL = "proc_suspended_sel.gif"; //$NON-NLS-1$

	// ==== Image Descriptors ====

	// CHANGE/DELETE SET
	public static final ImageDescriptor ID_ICON_CHANGESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_CHANGESET_NORMAL, ICON_CHANGESET_NORMAL);
	public static final ImageDescriptor ID_ICON_CREATESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_CREATESET_NORMAL, ICON_CREATESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETESET_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_DELETESET_NORMAL, ICON_DELETESET_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETEELEMENT_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_DELETEELEMENT_NORMAL, ICON_DELETEELEMENT_NORMAL);

	// ZOOM
	public static final ImageDescriptor ID_ICON_ZOOMIN_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL, ICON_ZOOMIN_NORMAL,
			ICON_ZOOMIN_NORMAL);
	public static final ImageDescriptor ID_ICON_ZOOMOUT_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_ZOOMOUT_NORMAL, ICON_ZOOMOUT_NORMAL);

	// TOOLBAR
	public static final ImageDescriptor ID_ICON_REMOVEALLTERMINATED_NORMAL = ImageManager.createImageDescriptor(TOOLICONURL,
			ICON_REMOVEALLTERMINATED_NORMAL, ICON_REMOVEALLTERMINATED_NORMAL);

	static {
		ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_STARTING, IMG_JOB_STARTING);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_RUNNING, IMG_JOB_RUNNING);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_COMPLETED, IMG_JOB_COMPLETED);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_JOB_SUSPENDED, IMG_JOB_SUSPENDED);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_STARTING, IMG_DEBUG_JOB_STARTING);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_RUNNING, IMG_DEBUG_JOB_RUNNING);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_COMPLETED, IMG_DEBUG_JOB_COMPLETED);
		ImageManager.createImageDescriptor(JOBICONURL, IMG_DEBUG_JOB_SUSPENDED, IMG_DEBUG_JOB_SUSPENDED);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_COMPLETED, IMG_PROC_COMPLETED);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_COMPLETED_SEL, IMG_PROC_COMPLETED_SEL);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING, IMG_PROC_RUNNING);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_RUNNING_SEL, IMG_PROC_RUNNING_SEL);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING, IMG_PROC_STARTING);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_STARTING_SEL, IMG_PROC_STARTING_SEL);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_SUSPENDED, IMG_PROC_SUSPENDED);
		ImageManager.createImageDescriptor(PROCESSICONURL, IMG_PROC_SUSPENDED_SEL, IMG_PROC_SUSPENDED_SEL);
	}
	// ==== Image Arrays ====

	// NOTE: The order of images in these arrays must correspond to the ordinal
	// values of the element state attributes.

	public static Image[][] jobImages = {
			{ ImageManager.getImage(ParallelImages.IMG_JOB_STARTING), ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_STARTING) },
			{ ImageManager.getImage(ParallelImages.IMG_JOB_RUNNING), ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_RUNNING) },
			{ ImageManager.getImage(ParallelImages.IMG_JOB_SUSPENDED),
					ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_SUSPENDED) },
			{ ImageManager.getImage(ParallelImages.IMG_JOB_COMPLETED),
					ImageManager.getImage(ParallelImages.IMG_DEBUG_JOB_COMPLETED) } };

	public static final Image[][] procImages = {
			{ ImageManager.getImage(ParallelImages.IMG_PROC_STARTING), ImageManager.getImage(ParallelImages.IMG_PROC_STARTING_SEL) },
			{ ImageManager.getImage(ParallelImages.IMG_PROC_RUNNING), ImageManager.getImage(ParallelImages.IMG_PROC_RUNNING_SEL) },
			{ ImageManager.getImage(ParallelImages.IMG_PROC_SUSPENDED),
					ImageManager.getImage(ParallelImages.IMG_PROC_SUSPENDED_SEL) },
			{ ImageManager.getImage(ParallelImages.IMG_PROC_COMPLETED), ImageManager.getImage(ParallelImages.IMG_PROC_COMPLETED) } };
}
