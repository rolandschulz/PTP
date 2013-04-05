/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.core.server;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * @since 7.0
 */
public class RemoteVariableManager {
	private static RemoteVariableManager fInstance = null;

	public static RemoteVariableManager getInstance() {
		if (fInstance == null) {
			fInstance = new RemoteVariableManager();
		}
		return fInstance;
	}

	private final IStringVariableManager fVarMgr;

	public RemoteVariableManager() {
		fVarMgr = VariablesPlugin.getDefault().getStringVariableManager();
	}

	/**
	 * Perform a string substitution on the expression using the variables known
	 * by the platform.
	 * 
	 * @param expression
	 *            expression to substitute
	 * @return
	 */
	public String performStringSubstitution(String expression) {
		try {
			return fVarMgr.performStringSubstitution(expression, false);
		} catch (CoreException e) {
			return expression;
		}
	}

	/**
	 * Initialize variables with values from the map. Variable values should be
	 * stored externally as platform variables are shared across all plugins.
	 * This method should be called prior to calling {@link performStringSubstitution}
	 * 
	 * @param vars
	 * @since 5.0
	 */
	public void setVars(Map<String, String> vars) {
		for (String name : vars.keySet()) {
			String value = vars.get(name);
			IValueVariable var = fVarMgr.getValueVariable(name);
			if (var == null) {
				var = fVarMgr.newValueVariable(name, name, false, value);
			} else {
				var.setValue(value);
			}
		}
	}
}
