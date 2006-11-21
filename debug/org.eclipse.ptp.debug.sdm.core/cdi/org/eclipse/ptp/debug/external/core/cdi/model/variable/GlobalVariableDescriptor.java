package org.eclipse.ptp.debug.external.core.cdi.model.variable;

import org.eclipse.ptp.debug.core.cdi.model.IPCDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;

public class GlobalVariableDescriptor extends VariableDescriptor implements IPCDIGlobalVariableDescriptor {
	public GlobalVariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth) {
		super(target, thread, stack, n, fn, pos, depth);
	}
}
