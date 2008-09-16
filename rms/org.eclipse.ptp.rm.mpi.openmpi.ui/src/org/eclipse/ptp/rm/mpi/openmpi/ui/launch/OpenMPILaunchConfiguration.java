package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;

public class OpenMPILaunchConfiguration {
	public static final String ATTR_BASE = OpenMPIUIPlugin.PLUGIN_ID + ".launchAttributes";
	public static final String ATTR_NUMPROCS = ATTR_BASE + ".numProcs";
	public static final String ATTR_BYSLOT = ATTR_BASE + ".bySlot";
	public static final String ATTR_NOOVERSUBSCRIBE = ATTR_BASE + ".noOversubscribe";
	public static final String ATTR_NOLOCAL = ATTR_BASE + ".noLocal";
	public static final String ATTR_PREFIX = ATTR_BASE + ".prefix";
	public static final String ATTR_USEPREFIX = ATTR_BASE + ".usePrefix";
	public static final String ATTR_HOSTFILE = ATTR_BASE + ".hostFile";
	public static final String ATTR_USEHOSTFILE = ATTR_BASE + ".useHostFile";
	public static final String ATTR_HOSTLIST = ATTR_BASE + ".hostList";
	public static final String ATTR_USEHOSTLIST = ATTR_BASE + ".useHostList";
	public static final String ATTR_ARGUMENTS = ATTR_BASE + ".arguments";
	public static final String ATTR_USEDEFAULTARGUMENTS = ATTR_BASE + ".useDefaultArguments";
	public static final String ATTR_PARAMETERS = ATTR_BASE + ".parameters";
	public static final String ATTR_USEDEFAULTPARAMETERS = ATTR_BASE + ".useDefaultParameters";

	static String calculateArguments(ILaunchConfiguration configuration) throws CoreException {
		if (configuration.getAttribute(ATTR_USEDEFAULTARGUMENTS, OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS)) {
			String launchArgs = "-np " + Integer.toString(configuration.getAttribute(ATTR_NUMPROCS, OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS));
			if (configuration.getAttribute(ATTR_BYSLOT, OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT)) {
				launchArgs += " -byslot";
			}
			if (configuration.getAttribute(ATTR_NOOVERSUBSCRIBE, OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE)) {
				launchArgs += " -nooversubscribe";
			}
			if (configuration.getAttribute(ATTR_NOLOCAL, OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL)) {
				launchArgs += " -nolocal";
			}
			if (configuration.getAttribute(ATTR_USEPREFIX, OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX)) {
				launchArgs += " --prefix " + fixString(configuration.getAttribute(ATTR_PREFIX, OpenMPILaunchConfigurationDefaults.ATTR_PREFIX));
			}
			if (configuration.getAttribute(ATTR_USEHOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE)) {
				launchArgs += " -hostfile " + fixString(configuration.getAttribute(ATTR_HOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE));
			}
			if (configuration.getAttribute(ATTR_USEHOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST)) {
				launchArgs += " -host " + fixString(configuration.getAttribute(ATTR_HOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST));
			}

			if (! configuration.getAttribute(ATTR_USEDEFAULTPARAMETERS, OpenMPILaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS)) {
				Map<String, String> params = configuration.getAttribute(ATTR_PARAMETERS, OpenMPILaunchConfigurationDefaults.ATTR_PARAMETERS);
				for (Entry<String, String> param : params.entrySet()) {
					launchArgs += " -mca " + param.getKey() + " " + fixString(param.getValue());
				}
			}
			return launchArgs;
		} else {
			String launchArgs = configuration.getAttribute(ATTR_ARGUMENTS, OpenMPILaunchConfigurationDefaults.ATTR_ARGUMENTS);
			return launchArgs;
		}
	}

	/**
	 * Make string suitable for passing as an argument
	 *
	 * @param s
	 * @return
	 */
	static private String fixString(String s) {
		// TODO is that right and escaped correctly?
		if (s == null) {
			return "\"\"";
		}
		return "\"" + s + "\"";
	}
}
