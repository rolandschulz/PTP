package org.eclipse.ptp.services.test.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ptp.services.core.ServiceModelManager;


public class PrintServiceModelHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ServiceModelManager.getInstance().printServiceModel();
		return null;
	}
}
