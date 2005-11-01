package org.eclipse.ptp.debug.external.cdi.model.variable;

import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;

public class GlobalVariableDescriptor extends VariableDescriptor implements ICDIGlobalVariableDescriptor {
	public GlobalVariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth, IAIF aif) {
		super(target, thread, stack, n, fn, pos, depth, aif);
	}
}
