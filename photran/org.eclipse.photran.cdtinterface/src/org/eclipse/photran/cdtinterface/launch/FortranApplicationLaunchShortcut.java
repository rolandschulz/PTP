package org.eclipse.photran.cdtinterface.launch;

import org.eclipse.cdt.launch.internal.CApplicationLaunchShortcut;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class FortranApplicationLaunchShortcut extends CApplicationLaunchShortcut
{
    /*@Override*/ protected ILaunchConfigurationType getCLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType("org.eclipse.photran.launch.localCLaunch");
    }
}
