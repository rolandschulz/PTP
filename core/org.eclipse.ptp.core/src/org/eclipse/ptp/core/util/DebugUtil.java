package org.eclipse.ptp.core.util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.core.PTPCorePlugin;

public class DebugUtil {
	private static final String PROTOCOL_TRACING_OPTION = "org.eclipse.ptp.core/debug/proxy/protocol_tracing";
	private static final String PROXY_CLIENT_MESSAGES_OPTION = "org.eclipse.ptp.core/debug/proxy/client_messages";
	private static final String PROXY_SERVER_MESSAGES_OPTION = "org.eclipse.ptp.core/debug/proxy/server_messages";
	private static final String RM_MESSAGES_OPTION = "org.eclipse.ptp.core/debug/rm/messages";

	public static boolean PROTOCOL_TRACING;
	public static boolean PROXY_CLIENT_MESSAGES;
	public static boolean PROXY_SERVER_MESSAGES;
	public static boolean RM_MESSAGES;

	public static void configurePluginDebugOptions() {
		if (PTPCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(PROTOCOL_TRACING_OPTION);
			if (option != null) {
				PROTOCOL_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_CLIENT_MESSAGES_OPTION);
			if (option != null) {
				PROXY_CLIENT_MESSAGES = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_SERVER_MESSAGES_OPTION);
			if (option != null) {
				PROXY_SERVER_MESSAGES = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(RM_MESSAGES_OPTION);
			if (option != null) {
				RM_MESSAGES = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}

}
