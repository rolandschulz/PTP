package org.eclipse.ptp.launch.ui;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @author clement
 *
 */
public class PCommonTab extends CommonTab {
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        super.setDefaults(configuration);
        //set defualt running launchInForeground
        configuration.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);        
    }
}
