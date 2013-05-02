package org.eclipse.ptp.etfw.toolopts;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 5.0
 */
public interface IToolUITab extends IAppInput {

	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	public void makeToolPane(Composite comp, ToolPaneListener paneListener);

	public void makeToolPane(Composite comp);

	public void OptUpdate();

	abstract void updateOptDisplay();

	public boolean updateOptField(Object source);

	public String getName();

	public String getToolName();

	public Map<String, String> getVarMap();

	public String getOptionString();

	public String getConfigID();

	public String getConfigVarID();

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	public void initializePane(ILaunchConfiguration configuration) throws CoreException;

	public boolean isVirtual();

	public boolean isEmbedded();
}
