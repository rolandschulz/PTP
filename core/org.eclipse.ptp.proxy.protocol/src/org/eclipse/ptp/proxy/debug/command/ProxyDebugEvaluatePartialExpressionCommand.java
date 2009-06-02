package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugEvaluatePartialExpressionCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugEvaluatePartialExpressionCommand(String bits, String expr, String exprId, boolean listChildren, boolean express) {
		super(EVALUATEPARTIALEXPRESSION, bits);
		addArgument(expr);
		addArgument(exprId);
		addArgument(listChildren);
		addArgument(express);
	}
	
	public ProxyDebugEvaluatePartialExpressionCommand(int transID, String[] args) {
		super(EVALUATEPARTIALEXPRESSION, transID, args);
	}
}
