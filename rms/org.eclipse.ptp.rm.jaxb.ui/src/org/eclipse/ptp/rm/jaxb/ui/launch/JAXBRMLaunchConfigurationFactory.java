package org.eclipse.ptp.rm.jaxb.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #getResourceManagerClass()
	 */
	@Override
	public Class<? extends IResourceManagerControl> getResourceManagerClass() {
		return JAXBResourceManager.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #doCreate(org.eclipse.ptp.rmsystem.IResourceManagerControl,
	 * org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManagerControl rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		if (!(rm instanceof IJAXBResourceManagerControl)) {
			throw CoreExceptionUtils.newException(Messages.JAXBRMLaunchConfigurationFactory_doCreateError + rm, null);
		}
		return new JAXBRMLaunchConfigurationDynamicTab((IJAXBResourceManagerControl) rm, dialog);
	}
}
