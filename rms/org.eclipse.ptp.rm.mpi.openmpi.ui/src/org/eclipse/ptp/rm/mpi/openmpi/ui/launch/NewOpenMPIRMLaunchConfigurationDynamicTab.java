package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;


import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;

public class NewOpenMPIRMLaunchConfigurationDynamicTab extends
ExtendableRMLaunchConfigurationDynamicTab {

	public NewOpenMPIRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		super();
		addDynamicTab(new BasicOpenMpiRMLaunchConfigurationDynamicTab());
		addDynamicTab(new AdvancedOpenMpiRMLaunchConfigurationDynamicTab(rm));
	}
}
