/*
 * Created on Oct 21, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.fdt.launch.internal.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.fdt.debug.core.IFDTLaunchConfigurationConstants;


/**
 * @deprecated - temporary class for while configs are migrated to new EnvironmentTab
 */
public class MigratingCEnvironmentTab extends EnvironmentTab {

	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.launch.ui.CEnvironmentTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			ILaunchConfigurationWorkingCopy wc = (ILaunchConfigurationWorkingCopy) config;
			try {
				Map map = wc.getAttribute(IFDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
				if (map != null) {
					wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
					wc.setAttribute(IFDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
				}
			} catch (CoreException e) {
			}
		}
		super.initializeFrom(config);
	}
}
