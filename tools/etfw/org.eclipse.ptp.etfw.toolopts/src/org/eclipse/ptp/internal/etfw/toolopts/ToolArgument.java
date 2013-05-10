package org.eclipse.ptp.internal.etfw.toolopts;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.toolopts.IAppInput;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;

public class ToolArgument implements IAppInput {
	// private String argument=null;
	private String flag = null;
	private String value = null;
	private String separator = ""; //$NON-NLS-1$
	private String confVal = null;
	private boolean localFile = false;
	private boolean useConfValue = false;
	private boolean requireValue = false;

	public static int ARG = 0;

	public static int VAR = 1;

	private int type = ARG;

	public ToolArgument(String arg) {
		value = arg;
	}

	public ToolArgument(String flag, String value, String sep, boolean local) {
		this.flag = flag;
		this.value = value;
		if (sep != null) {
			separator = sep;
		}
		this.localFile = local;

	}

	private String getArg() {

		String arg = ""; //$NON-NLS-1$
		if (flag != null) {
			arg += flag;
			arg += separator;
		}
		if (useConfValue) {
			arg += ToolsOptionsConstants.CONF_VALUE;
		} else if (value != null) {
			if (localFile) {
				arg += ToolsOptionsConstants.PROJECT_BUILD + File.separator;
			}
			arg += value;
		}
		// argument=arg;

		return arg;
	}

	/**
	 * Builds and returns the argument from the elements defined in this object
	 */
	public String getArgument(ILaunchConfiguration configuration) {

		if (type != ARG) {
			return null;
		}

		if (isUseConfValue()) {
			String carg = getArg();
			String cval = ""; //$NON-NLS-1$
			try {
				cval = configuration.getAttribute(getConfValue(), ""); //$NON-NLS-1$
			} catch (final CoreException e) {
				e.printStackTrace();
			}
			if (requireValue && cval.trim().length() <= 0) {
				return ""; //$NON-NLS-1$
			}
			carg = carg.replace(ToolsOptionsConstants.CONF_VALUE, cval);
			return carg;
		} else {
			return getArg();
		}
	}

	public String getConfValue() {
		return confVal;
	}

	public Map<String, String> getEnvVars(ILaunchConfiguration configuration) {
		if (type != VAR || flag == null) {
			return null;
		}
		final Map<String, String> map = new LinkedHashMap<String, String>();

		String val = ""; //$NON-NLS-1$
		if (value != null) {
			if (localFile) {
				val += ToolsOptionsConstants.PROJECT_BUILD + File.separator;
			}
			val += value;
		}
		boolean ok = true;
		if (isUseConfValue()) {
			String cval = ""; //$NON-NLS-1$
			try {
				cval = configuration.getAttribute(getConfValue(), ""); //$NON-NLS-1$
			} catch (final CoreException e) {
				e.printStackTrace();
			}

			val = val.replace(ToolsOptionsConstants.CONF_VALUE, cval);
			if (requireValue && cval.trim().length() <= 0) {
				ok = false;
			}
		}
		if (ok) {
			map.put(flag, val);
		}
		return map;
	}

	/**
	 * @since 1.1
	 */
	public boolean isRequireValue() {
		return requireValue;
	}

	/**
	 * If true the value string is a key for the actual value to be used from
	 * the launch configuration object
	 * 
	 * @return
	 */
	public boolean isUseConfValue() {
		return useConfValue;
	}

	public void setConfValue(String cval) {
		confVal = cval;

	}

	// private String getArg(String buildDir, String rootDir){
	// String arg=getArg();
	// arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_BUILD, buildDir);
	// arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_ROOT, rootDir);
	// return arg;
	// }

	/**
	 * @since 1.1
	 */
	public void setRequireValue(boolean requireValue) {
		this.requireValue = requireValue;
	}

	public void setType(int t) {
		type = t;
	}

	public void setUseConfValue(boolean useConfValue) {
		this.useConfValue = useConfValue;
	}

}
