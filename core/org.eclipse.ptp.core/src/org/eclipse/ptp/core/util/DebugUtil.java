package org.eclipse.ptp.core.util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.core.PTPCorePlugin;

public class DebugUtil {
	private static final String PROTOCOL_TRACING_OPTION = "org.eclipse.ptp.core/debug/proxy/protocol/tracing";
	private static final String PROXY_CLIENT_TRACING_OPTION = "org.eclipse.ptp.core/debug/proxy/client/tracing";
	private static final String PROXY_SERVER_DEBUG_LEVEL_OPTION = "org.eclipse.ptp.core/debug/proxy/server/debug_level";
	private static final String RM_TRACING_OPTION = "org.eclipse.ptp.core/debug/rm/tracing";

	public static boolean PROTOCOL_TRACING;
	public static boolean PROXY_CLIENT_TRACING;
	public static int PROXY_SERVER_DEBUG_LEVEL;
	public static boolean RM_TRACING;

	public static void configurePluginDebugOptions() {
		if (PTPCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(PROTOCOL_TRACING_OPTION);
			if (option != null) {
				PROTOCOL_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_CLIENT_TRACING_OPTION);
			if (option != null) {
				PROXY_CLIENT_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_SERVER_DEBUG_LEVEL_OPTION);
			if (option != null) {
				try {
					PROXY_SERVER_DEBUG_LEVEL = Integer.parseInt(option);
				} catch (NumberFormatException e) {
					PROXY_SERVER_DEBUG_LEVEL = 0;
				}
			}
			option = Platform.getDebugOption(RM_TRACING_OPTION);
			if (option != null) {
				RM_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}

}
