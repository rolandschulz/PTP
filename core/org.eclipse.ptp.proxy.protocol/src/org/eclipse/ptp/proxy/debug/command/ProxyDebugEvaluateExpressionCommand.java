package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugEvaluateExpressionCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugEvaluateExpressionCommand(int transID, String[] args) {
		super(EVALUATEEXPRESSION, transID, args);
	}

	public ProxyDebugEvaluateExpressionCommand(String bits, String expr) {
		super(EVALUATEEXPRESSION, bits);
		addArgument(expr);
	}
}
