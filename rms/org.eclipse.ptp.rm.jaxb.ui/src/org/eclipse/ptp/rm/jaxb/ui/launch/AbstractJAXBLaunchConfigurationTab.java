package org.eclipse.ptp.rm.jaxb.ui.launch;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractJAXBLaunchConfigurationTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IJAXBUINonNLSConstants {

	protected final JAXBControllerLaunchConfigurationTab parentTab;
	protected final Map<String, Object> localMap;
	protected String title;

	protected AbstractJAXBLaunchConfigurationTab(JAXBControllerLaunchConfigurationTab parentTab, ILaunchConfigurationDialog dialog) {
		super(dialog);
		this.parentTab = parentTab;
		this.title = Messages.DefaultDynamicTab_title;
		localMap = new TreeMap<String, Object>();
	}

	public abstract Image getImage();

	public abstract String getText();

	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		Map<String, Object> current = null;
		LCVariableMap lcMap = parentTab.getLCMap();
		try {
			refreshLocal(configuration);
			current = lcMap.swapVariables(localMap);
			lcMap.saveToConfiguration(configuration);
		} catch (CoreException t) {
			JAXBUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		} finally {
			try {
				lcMap.swapVariables(current);
			} catch (CoreException t) {
				JAXBUIPlugin.log(t);
				return new RMLaunchValidation(false, t.getMessage());
			}
		}
		return new RMLaunchValidation(true, null);
	}

	protected abstract void doRefreshLocal();

	/*
	 * Subclasses should call this method, but implement doRefreshLocal()
	 */
	@SuppressWarnings("rawtypes")
	protected void refreshLocal(ILaunchConfiguration config) throws CoreException {
		localMap.clear();
		localMap.put(DIRECTORY, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, ZEROSTR));
		localMap.put(EXEC_PATH, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ZEROSTR));
		localMap.put(PROG_ARGS, config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, ZEROSTR));
		localMap.put(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
				config.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true));
		localMap.put(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
				config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null));
		doRefreshLocal();
	}
}
