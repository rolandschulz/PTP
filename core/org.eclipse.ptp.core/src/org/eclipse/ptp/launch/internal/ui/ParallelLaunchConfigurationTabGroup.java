package org.eclipse.ptp.launch.internal.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.PArgumentTab;
import org.eclipse.ptp.launch.ui.PCommonTab;
import org.eclipse.ptp.launch.ui.PMainTab;
import org.eclipse.ptp.launch.ui.ParallelTab;

/**
 * @author Clement
 * 
 */
public class ParallelLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab tabs[] = {
                new PMainTab(), 
                new PArgumentTab(),
                new ParallelTab(),
                //new CEnvironmentTab(), 
                //new CDebuggerTab(), 
                //new CSourceLookupTab(), 
                new PCommonTab() };
        setTabs(tabs);
    }    
}