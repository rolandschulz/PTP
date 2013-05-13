package org.eclipse.ptp.internal.debug.sdm.core.utils;


import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.debug.sdm.core.SDMDebugCorePlugin;

public class DebugUtil {
	private static final String SDM_MASTER_TRACING_OPTION = "org.eclipse.ptp.internal.debug.sdm.core/debug/SDMmaster"; //$NON-NLS-1$
	private static final String SDM_MASTER_TRACING_OPTION_MORE = "org.eclipse.ptp.internal.debug.sdm.core/debug/SDMmaster/more"; //$NON-NLS-1$
	private static final String SDM_MASTER_OUTPUT_TRACING_OPTION = "org.eclipse.ptp.internal.debug.sdm.core/debug/SDMmaster/output"; //$NON-NLS-1$

	public static boolean SDM_MASTER_TRACING = false;
	public static boolean SDM_MASTER_TRACING_MORE = false;
	public static boolean SDM_MASTER_OUTPUT_TRACING = false;

	public static void configurePluginDebugOptions() {
		if (SDMDebugCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(SDM_MASTER_TRACING_OPTION);
			if (option != null) {
				SDM_MASTER_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(SDM_MASTER_TRACING_OPTION_MORE);
			if (option != null) {
				SDM_MASTER_TRACING_MORE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(SDM_MASTER_OUTPUT_TRACING_OPTION);
			if (option != null) {
				SDM_MASTER_OUTPUT_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}

	public static void trace(boolean option, String pattern, Object ... arguments) {
		trace(option, NLS.bind(pattern, arguments));
	}

	public static void trace(boolean option, String message) {
		if (option) {
			System.out.println(message);
			System.out.flush();
		}
	}

	public static void error(boolean option, String pattern, Object ... arguments) {
		error(option, NLS.bind(pattern, arguments));
	}

	public static void error(boolean option, String message) {
		if (option) {
			System.err.println(message);
		}
	}
}
