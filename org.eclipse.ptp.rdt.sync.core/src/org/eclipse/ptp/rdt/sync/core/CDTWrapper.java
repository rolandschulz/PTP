package org.eclipse.ptp.rdt.sync.core;

import java.util.HashMap;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.services.core.BuildScenario;

/**
 * Static class to wrap CDT operations, specifically writing and reading remote information to the build configuration stored in
 * the .cproject file.
 *
 *
 */
public class CDTWrapper {
	private static HashMap<String, String> configToId = new HashMap<String, String>();

	// Static class - prevent instantiation and subclassing
	private CDTWrapper() {
		throw new AssertionError();
	}
	
	public static BuildScenario getRemoteInformationForConfiguration(IConfiguration config) {
		ITool remoteSyncTool = config.getToolChain().getTool(configToId.get(config.getName()));

//		for (ITool tool : allTools) {
//			if (tool.getName() == "Remote Sync Tool") { //$NON-NLS-1$
//				remoteSyncTool = tool;
//				break;
//			}
//		}
		if (remoteSyncTool == null) {
			throw new RuntimeException("Unable to find remote sync tool for configuration"); //$NON-NLS-1$
		}
				
		BuildScenario newBuildScenario = new BuildScenario(
				(String) remoteSyncTool.getOptionById("remoteSyncProviderOption").getValue(), //$NON-NLS-1$
				(String) remoteSyncTool.getOptionById("remoteConnectionOption").getValue(), //$NON-NLS-1$
				(String) remoteSyncTool.getOptionById("remoteLocationOption").getValue()); //$NON-NLS-1$
		
		return newBuildScenario;
	}
	
	public static void setRemoteInformationForConfiguration(BuildScenario bs, IConfiguration config) throws BuildException {
		String configId = ManagedBuildManager.calculateChildId("org.eclipse.ptp.rdt.sync.tool", null); //$NON-NLS-1$
		configToId.put(config.getName(), configId);
		ITool remoteSyncTool = config.getToolChain().createTool(null, configId, "Remote Sync Tool", true); //$NON-NLS-1$
		IOption rspOption = remoteSyncTool.createOption(null, "remoteSyncProviderOption", "remoteSyncProvider", false); //$NON-NLS-1$ //$NON-NLS-2$
		IOption rcOption = remoteSyncTool.createOption(null, "remoteConnectionOption", "remoteConnection", false); //$NON-NLS-1$ //$NON-NLS-2$
		IOption rlOption = remoteSyncTool.createOption(null, "remoteLocationOption", "remoteLocation", false); //$NON-NLS-1$ //$NON-NLS-2$

		int STRING_TYPE = org.eclipse.cdt.managedbuilder.core.IOption.STRING;
		rspOption.setValueType(STRING_TYPE);
		rcOption.setValueType(STRING_TYPE);
		rlOption.setValueType(STRING_TYPE);

		try {
			remoteSyncTool.getOptionToSet(rspOption, false).setValue(bs.getSyncProvider());
			remoteSyncTool.getOptionToSet(rcOption, false).setValue(bs.getRemoteConnectionName());
			remoteSyncTool.getOptionToSet(rlOption, false).setValue(bs.getLocation());
		} catch (BuildException e) {
			throw e;
		}
	}
	
	public static void saveRemoteInformation(IProject project) {
		ManagedBuildManager.saveBuildInfo(project, true);
	}
}
