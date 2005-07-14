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
package org.eclipse.ptp.debug.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author clement chu
 *
 */
public class ImageUtil {
	public final static URL ICONURL = UIPlugin.getDefault().getBundle().getEntry("icons/");
	public final static URL TOOLICONURL = UIPlugin.getDefault().getBundle().getEntry("icons/tool/");
	public final static URL PROCESSICONURL = UIPlugin.getDefault().getBundle().getEntry("icons/process/");
	
    private static ImageRegistry imageRegistry = null;
    
	public static final String ICON_RESUME_DISABLE = "resume_disable.gif";
	public static final String ICON_RESUME_NORMAL = "resume_normal.gif";
	public static final String ICON_SUSPEND_DISABLE = "suspend_disable.gif";
	public static final String ICON_SUSPEND_NORMAL = "suspend_normal.gif";
	public static final String ICON_TERMINATE_DISABLE = "terminate_disable.gif";
	public static final String ICON_TERMINATE_NORMAL = "terminate_normal.gif";
	public static final String ICON_STEPINTO_DISABLE = "stepinto_disable.gif";
	public static final String ICON_STEPINTO_NORMAL = "stepinto_normal.gif";
	public static final String ICON_STEPOVER_DISABLE = "stepover_disable.gif";
	public static final String ICON_STEPOVER_NORMAL = "stepover_normal.gif";
	public static final String ICON_STEPRETURN_DISABLE = "stepreturn_disable.gif";
	public static final String ICON_STEPRETURN_NORMAL = "stepreturn_normal.gif";
	public static final String ICON_REGISTER_DISABLE = "register_disable.gif";
	public static final String ICON_REGISTER_NORMAL = "register_normal.gif";
	public static final String ICON_CREATEGROUP_DISABLE = "creategroup_disable.gif";
	public static final String ICON_CREATEGROUP_NORMAL = "creategroup_normal.gif";
	public static final String ICON_DELETEGROUP_DISABLE = "deletegroup_disable.gif";
	public static final String ICON_DELETEGROUP_NORMAL = "deletegroup_normal.gif";
	
	public static final String IMG_PRO_ERROR_SEL = "error_sel.gif";
	public static final String IMG_PRO_ERROR = "error.gif";
	public static final String IMG_PRO_RUNNING_SEL = "running_sel.gif";
	public static final String IMG_PRO_RUNNING = "running.gif";
	public static final String IMG_PRO_STARTED_SEL = "started_sel.gif";
	public static final String IMG_PRO_STARTED = "started.gif";
	public static final String IMG_PRO_STOPPED_SEL = "stopped_sel.gif";
	public static final String IMG_PRO_STOPPED = "stopped.gif";
	public static final String IMG_PRO_SUSPENDED_SEL = "suspended_sel.gif";
	public static final String IMG_PRO_SUSPENDED = "suspended.gif";
	
	public static final ImageDescriptor ID_ICON_RESUME_DISABLE = createImageDescriptor(TOOLICONURL, ICON_RESUME_DISABLE, ICON_RESUME_DISABLE);
	public static final ImageDescriptor ID_ICON_RESUME_NORMAL = createImageDescriptor(TOOLICONURL, ICON_RESUME_NORMAL, ICON_RESUME_NORMAL);
	public static final ImageDescriptor ID_ICON_SUSPEND_DISABLE = createImageDescriptor(TOOLICONURL, ICON_SUSPEND_DISABLE, ICON_SUSPEND_DISABLE);
	public static final ImageDescriptor ID_ICON_SUSPEND_NORMAL = createImageDescriptor(TOOLICONURL, ICON_SUSPEND_NORMAL, ICON_SUSPEND_NORMAL);
	public static final ImageDescriptor ID_ICON_TERMINATE_DISABLE = createImageDescriptor(TOOLICONURL, ICON_TERMINATE_DISABLE, ICON_TERMINATE_DISABLE);
	public static final ImageDescriptor ID_ICON_TERMINATE_NORMAL = createImageDescriptor(TOOLICONURL, ICON_TERMINATE_NORMAL, ICON_TERMINATE_NORMAL);
	public static final ImageDescriptor ID_ICON_STEPINTO_DISABLE = createImageDescriptor(TOOLICONURL, ICON_STEPINTO_DISABLE, ICON_STEPINTO_DISABLE);
	public static final ImageDescriptor ID_ICON_STEPINTO_NORMAL = createImageDescriptor(TOOLICONURL, ICON_STEPINTO_NORMAL, ICON_STEPINTO_NORMAL);
	public static final ImageDescriptor ID_ICON_STEPOVER_DISABLE = createImageDescriptor(TOOLICONURL, ICON_STEPOVER_DISABLE, ICON_STEPOVER_DISABLE);
	public static final ImageDescriptor ID_ICON_STEPOVER_NORMAL = createImageDescriptor(TOOLICONURL, ICON_STEPOVER_NORMAL, ICON_STEPOVER_NORMAL);
	public static final ImageDescriptor ID_ICON_STEPRETURN_DISABLE = createImageDescriptor(TOOLICONURL, ICON_STEPRETURN_DISABLE, ICON_STEPRETURN_DISABLE);
	public static final ImageDescriptor ID_ICON_STEPRETURN_NORMAL = createImageDescriptor(TOOLICONURL, ICON_STEPRETURN_NORMAL, ICON_STEPRETURN_NORMAL);
	public static final ImageDescriptor ID_ICON_REGISTER_DISABLE = createImageDescriptor(TOOLICONURL, ICON_REGISTER_DISABLE, ICON_REGISTER_DISABLE);
	public static final ImageDescriptor ID_ICON_REGISTER_NORMAL = createImageDescriptor(TOOLICONURL, ICON_REGISTER_NORMAL, ICON_REGISTER_NORMAL);
	public static final ImageDescriptor ID_ICON_CREATEGROUP_DISABLE = createImageDescriptor(TOOLICONURL, ICON_CREATEGROUP_DISABLE, ICON_CREATEGROUP_DISABLE);
	public static final ImageDescriptor ID_ICON_CREATEGROUP_NORMAL = createImageDescriptor(TOOLICONURL, ICON_CREATEGROUP_NORMAL, ICON_CREATEGROUP_NORMAL);
	public static final ImageDescriptor ID_ICON_DELETEGROUP_DISABLE = createImageDescriptor(TOOLICONURL, ICON_DELETEGROUP_DISABLE, ICON_DELETEGROUP_DISABLE);
	public static final ImageDescriptor ID_ICON_DELETEGROUP_NORMAL = createImageDescriptor(TOOLICONURL, ICON_DELETEGROUP_NORMAL, ICON_DELETEGROUP_NORMAL);

	public static final ImageDescriptor ID_IMG_PRO_ERROR_SEL = createImageDescriptor(PROCESSICONURL, IMG_PRO_ERROR_SEL, IMG_PRO_ERROR_SEL);
	public static final ImageDescriptor ID_IMG_PRO_ERROR = createImageDescriptor(PROCESSICONURL, IMG_PRO_ERROR, IMG_PRO_ERROR);
	public static final ImageDescriptor ID_IMG_PRO_RUNNING_SEL = createImageDescriptor(PROCESSICONURL, IMG_PRO_RUNNING_SEL, IMG_PRO_RUNNING_SEL);
	public static final ImageDescriptor ID_IMG_PRO_RUNNING = createImageDescriptor(PROCESSICONURL, IMG_PRO_RUNNING, IMG_PRO_RUNNING);
	public static final ImageDescriptor ID_IMG_PRO_STARTED_SEL = createImageDescriptor(PROCESSICONURL, IMG_PRO_STARTED_SEL, IMG_PRO_STARTED_SEL);
	public static final ImageDescriptor ID_IMG_PRO_STARTED = createImageDescriptor(PROCESSICONURL, IMG_PRO_STARTED, IMG_PRO_STARTED);
	public static final ImageDescriptor ID_IMG_PRO_STOPPED_SEL = createImageDescriptor(PROCESSICONURL, IMG_PRO_STOPPED_SEL, IMG_PRO_STOPPED_SEL);
	public static final ImageDescriptor ID_IMG_PRO_STOPPED = createImageDescriptor(PROCESSICONURL, IMG_PRO_STOPPED, IMG_PRO_STOPPED);
	public static final ImageDescriptor ID_IMG_PRO_SUSPENDED_SEL = createImageDescriptor(PROCESSICONURL, IMG_PRO_SUSPENDED_SEL, IMG_PRO_SUSPENDED_SEL);
	public static final ImageDescriptor ID_IMG_PRO_SUSPENDED = createImageDescriptor(PROCESSICONURL, IMG_PRO_SUSPENDED, IMG_PRO_SUSPENDED);
	
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}
	
	public static void addImage(String key, Image image) {
		getImageRegistry().put(key, image);
	}
	public static void addImageDescriptor(String key, ImageDescriptor imageDescriptor) {
		getImageRegistry().put(key, imageDescriptor);
	}
		
	public static ImageDescriptor createImageDescriptor(URL iconURL, String key, String name) {
		try {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(getIconURL(iconURL, name));
			addImageDescriptor(key, imageDescriptor);
			return imageDescriptor;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL getIconURL(URL iconURL, String name) throws MalformedURLException {
		if (iconURL == null)
			throw new MalformedURLException();
			
		return new URL(iconURL, name);
	}	
}
