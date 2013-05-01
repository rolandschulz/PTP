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
package org.eclipse.ptp.internal.debug.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * @author clement chu
 *
 */
public class PDebugImage {
	public final static String ICONPATH = "icons/"; //$NON-NLS-1$
	public final static String TOOLICONPATH = ICONPATH + "tool/"; //$NON-NLS-1$
	public final static String PROCESSICONPATH = ICONPATH + "process/"; //$NON-NLS-1$
	public final static String DEBUGICONPATH = ICONPATH + "debug/"; //$NON-NLS-1$
	public final static String OBJICONPATH = ICONPATH + "obj16/"; //$NON-NLS-1$
	public final static String WIZBANICONPATH = ICONPATH + "wizban/"; //$NON-NLS-1$
	
    //array view
	public static final String ICON_ADD_VAR_NORMAL = "add_variable_normal.gif"; //$NON-NLS-1$
	
	//expression view
	public static final String ICON_VAR_ADD_NORMAL = "var_add_normal.gif"; //$NON-NLS-1$
	public static final String ICON_VAR_DELETE_NORMAL = "var_delete_normal.gif"; //$NON-NLS-1$
	public static final String ICON_VAR_EDIT_NORMAL = "var_edit_normal.gif"; //$NON-NLS-1$
	public static final String ICON_VAR_REFRESH_NORMAL = "var_refresh_normal.gif"; //$NON-NLS-1$
	public static final String ICON_VAR_COMPARE_NORMAL = "var_compare_normal.gif"; //$NON-NLS-1$

	public static final String ICON_RESUME_GROUP_DISABLE = "resume_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_RESUME_GROUP_NORMAL = "resume_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_SUSPEND_GROUP_DISABLE = "suspend_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_SUSPEND_GROUP_NORMAL = "suspend_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_TERMINATE_GROUP_DISABLE = "terminate_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_TERMINATE_GROUP_NORMAL = "terminate_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_STEPINTO_GROUP_DISABLE = "stepinto_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_STEPINTO_GROUP_NORMAL = "stepinto_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_STEPOVER_GROUP_DISABLE = "stepover_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_STEPOVER_GROUP_NORMAL = "stepover_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_STEPRETURN_GROUP_DISABLE = "stepreturn_group_disable.png"; //$NON-NLS-1$
	public static final String ICON_STEPRETURN_GROUP_NORMAL = "stepreturn_group_normal.png"; //$NON-NLS-1$
	public static final String ICON_REGISTER_NORMAL = "register_normal.gif"; //$NON-NLS-1$
	public static final String ICON_UNREGISTER_NORMAL = "unregister_normal.gif"; //$NON-NLS-1$
	
	public static final String IMG_PRO_ERROR_SEL = "error_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_ERROR = "error.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_RUNNING_SEL = "running_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_RUNNING = "running.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_STARTED_SEL = "started_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_STARTED = "started.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_STOPPED_SEL = "stopped_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_STOPPED = "stopped.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_SUSPENDED_SEL = "suspended_sel.gif"; //$NON-NLS-1$
	public static final String IMG_PRO_SUSPENDED = "suspended.gif"; //$NON-NLS-1$

	public static final String IMG_DEBUG_PTPBPTSET = "ptp_bpt_set.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTCURSET_EN = "bpt_curr_set_enable.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTMULTISET_EN = "bpt_multi_set_enable.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTNOSET_EN = "bpt_no_set_enable.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTCURSET_DI = "bpt_curr_set_disable.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTMULTISET_DI = "bpt_multi_set_disable.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_BPTNOSET_DI = "bpt_no_set_disable.gif"; //$NON-NLS-1$
	
	//TODO
	public static final String IMG_DEBUG_OVRS_WARNING = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVRS_ERROR = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVRS_ARGUMENT = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVRS_GLOBAL = "no.gif"; //$NON-NLS-1$
	
	public static final String IMG_DEBUG_SIGNAL = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_REGISTER_GROUP_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_REGISTER_GROUP = "no.gif"; //$NON-NLS-1$

	public static final String IMG_DEBUG_READ_WATCHPOINT_ENABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_WRITE_WATCHPOINT_ENABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_READ_WATCHPOINT_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_WRITE_WATCHPOINT_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_WATCHPOINT_ENABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_WATCHPOINT_DISABLED = "no.gif"; //$NON-NLS-1$

	public static final String IMG_DEBUG_VARIABLE_POINTER = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_VARIABLE_POINTER_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_VARIABLE_AGGREGATE = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_VARIABLE_AGGREGATE_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_VARIABLE_SIMPLE = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_VARIABLE_SIMPLE_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_REGISTER = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_REGISTER_DISABLED = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_EXECUTABLE_WITH_SYMBOLS = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_EXECUTABLE = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_SHARED_LIBRARY_WITH_SYMBOLS = "no.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_SHARED_LIBRARY = "no.gif"; //$NON-NLS-1$
	
	public static final String IMG_DEBUG_OVER_BPT_GLOB_EN = "global_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_GLOB_DI = "global_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_COND_EN = "conditional_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_COND_DI = "conditional_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_INST_EN = "installed_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_INST_DI = "installed_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_ADDR_EN = "address_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_ADDR_DI = "address_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_FUNC_EN = "function_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUG_OVER_BPT_FUNC_DI = "function_ovr_disabled.gif"; //$NON-NLS-1$
	
	public static final String IMG_OBJS_PATH_MAPPING = "mapping_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PATH_MAP_ENTRY = "mapentry_obj.gif"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_PATH_MAPPING = "mapping_wiz.gif"; //$NON-NLS-1$
	public static final String IMG_WIZBAN_PATH_MAP_ENTRY = "mapentry_wiz.gif"; //$NON-NLS-1$

	public static void initializeImageRegistry(ImageRegistry reg) {
		Bundle bundle = PTPDebugUIPlugin.getDefault().getBundle();

		//array view
		registerImage(bundle, reg, TOOLICONPATH, ICON_ADD_VAR_NORMAL);
		
		//expression view
		registerImage(bundle, reg, TOOLICONPATH, ICON_VAR_ADD_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_VAR_DELETE_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_VAR_EDIT_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_VAR_REFRESH_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_VAR_COMPARE_NORMAL);

		registerImage(bundle, reg, TOOLICONPATH, ICON_RESUME_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_RESUME_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_SUSPEND_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_SUSPEND_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_TERMINATE_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_TERMINATE_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPINTO_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPINTO_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPOVER_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPOVER_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPRETURN_GROUP_DISABLE);
		registerImage(bundle, reg, TOOLICONPATH, ICON_STEPRETURN_GROUP_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_REGISTER_NORMAL);
		registerImage(bundle, reg, TOOLICONPATH, ICON_UNREGISTER_NORMAL);
		
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_ERROR_SEL);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_ERROR);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_RUNNING_SEL);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_RUNNING);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_STARTED_SEL);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_STARTED);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_STOPPED_SEL);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_STOPPED);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_SUSPENDED_SEL);
		registerImage(bundle, reg, PROCESSICONPATH, IMG_PRO_SUSPENDED);

		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_PTPBPTSET);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTCURSET_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTMULTISET_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTNOSET_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTCURSET_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTMULTISET_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_BPTNOSET_DI);

		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_GLOB_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_GLOB_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_COND_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_COND_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_INST_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_INST_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_ADDR_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_ADDR_DI);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_FUNC_EN);
		registerImage(bundle, reg, DEBUGICONPATH, IMG_DEBUG_OVER_BPT_FUNC_DI);

		registerImage(bundle, reg, OBJICONPATH, IMG_OBJS_PATH_MAPPING);
		registerImage(bundle, reg, OBJICONPATH, IMG_OBJS_PATH_MAP_ENTRY);
		registerImage(bundle, reg, WIZBANICONPATH, IMG_WIZBAN_PATH_MAPPING);
		registerImage(bundle, reg, WIZBANICONPATH, IMG_WIZBAN_PATH_MAP_ENTRY);
	}
	
	private static void registerImage(Bundle bundle, ImageRegistry registry, String basePath, String file) {
		URL url = FileLocator.find(bundle, new Path(basePath + file), null);
		if (url!=null) {
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			if (desc == null)
				desc = ImageDescriptor.getMissingImageDescriptor();
			registry.put(file, desc);
		}
	}
	
	/** Get image
	 * @param key
	 * @return
	 */
	public static Image getImage(String key) {
		return PTPDebugUIPlugin.getDefault().getImageRegistry().get(key);
	}
	
	/** Get image descriptor
	 * @param key
	 * @return
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return PTPDebugUIPlugin.getDefault().getImageRegistry().getDescriptor(key);
	}
	
	public static void dispose() {
		PTPDebugUIPlugin.getDefault().getImageRegistry().dispose();
	}
}
