/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;

public class RemoteVariableManager {
	private static RemoteVariableManager fInstance = null;
	private final IStringVariableManager fVarMgr;

	public static RemoteVariableManager getInstance() {
		if (fInstance == null) {
			fInstance = new RemoteVariableManager();
		}
		return fInstance;
	}

	public RemoteVariableManager() {
		fVarMgr = VariablesPlugin.getDefault().getStringVariableManager();
	}

	public void setVariable(String name, String value) {
		IValueVariable var = fVarMgr.getValueVariable(name);
		if (var != null) {
			var.setValue(value);
		}
	}

	public String getVariable(String name) {
		IValueVariable var = fVarMgr.getValueVariable(name);
		if (var != null) {
			return var.getValue();
		}
		return null;
	}

	public String performStringSubstitution(String expression) {
		try {
			return fVarMgr.performStringSubstitution(expression, false);
		} catch (CoreException e) {
			return expression;
		}
	}
}
