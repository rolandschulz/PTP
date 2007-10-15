package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugEvaluateExpressionCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugEvaluateExpressionCommand(String bits, String expr) {
		super(bits);
		addArgument(expr);
	}

	public int getCommandID() {
		return EVALUATEEXPRESSION;
	}
}
