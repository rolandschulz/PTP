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

	public String getConfigID();

	public String getConfigVarID();

	public String getName();

	public String getOptionString();

	public String getToolName();

	public Map<String, String> getVarMap();

	public void initializePane(ILaunchConfiguration configuration) throws CoreException;

	public boolean isEmbedded();

	public boolean isVirtual();

	public void makeToolPane(Composite comp);

	public void makeToolPane(Composite comp, ToolPaneListener paneListener);

	public void OptUpdate();

	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	abstract void updateOptDisplay();

	public boolean updateOptField(Object source);
}
