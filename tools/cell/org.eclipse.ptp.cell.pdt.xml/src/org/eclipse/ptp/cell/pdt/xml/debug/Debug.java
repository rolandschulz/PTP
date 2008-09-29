package org.eclipse.ptp.cell.pdt.xml.debug;

import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	/* The utility class. */
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	/* Debug options. */
	public static boolean DEBUG = false;
	
	public static boolean DEBUG_XML_GENERATOR = false;
	
	public static boolean DEBUG_EVENT_FOREST_GENERATOR = false;
	
	public static boolean DEBUG_XML_WIZARD = false;
	
	

	

	/* Static initialization. */
	static {
		read();
	}

	/* Get debug options. */
	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			
			DEBUG_XML_GENERATOR = DEBUG && POLICY.getBooleanOption("debug/xmlgenerator"); //$NON-NLS-1$
			
			DEBUG_EVENT_FOREST_GENERATOR = DEBUG && POLICY.getBooleanOption("debug/eventforestgenerator"); //$NON-NLS-1$
			
			DEBUG_XML_WIZARD = DEBUG && POLICY.getBooleanOption("debug/xmlwizard"); //$NON-NLS-1$
		}
	}
}