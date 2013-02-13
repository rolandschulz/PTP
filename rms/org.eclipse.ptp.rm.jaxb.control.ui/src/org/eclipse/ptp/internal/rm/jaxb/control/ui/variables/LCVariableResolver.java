/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.ems.core.EnvManagerConfigString;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Resolver for the LCVariableMap (tag: ${ptp_lc:). <br>
 * <br>
 * * In order to guarantee consistency, any implicit call to resolve should be synchronized statically and should be preceded by a
 * call to {@link #setActive(LCVariableMap)}.<br>
 * <br>
 * See further {@link #resolveValue(IDynamicVariable, String)} <br>
 * <br>
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class LCVariableResolver implements IDynamicVariableResolver {

	private static LCVariableMap active;

	/**
	 * Only looks the the first element of the reference. This is because ${ptp_rm:name#field} gets replaced to become
	 * ${ptp_lc:name#field}, but the LC map itself is flattened from the RC name=Attribute or name=Property to name=primitive value.
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (active != null && argument != null) {
			String[] split = argument.split(JAXBCoreConstants.PDRX);
			if (split.length > 1) {
				if (split[1].equals(JAXBCoreConstants.VALUE)) {
					argument = split[0];
				}
			}
			Object value = active.getValue(argument);
			if (value != null) {
				if (value instanceof String && !((String) value).equals("")
						&& EnvManagerConfigString.isEnvMgmtConfigString((String) value)) {
					IEnvManager envMgr = active.getEnvManager();
					if (envMgr != null) {
						return envMgr.getBashConcatenation("\n", false, new EnvManagerConfigString((String) value), //$NON-NLS-1$
								null);
					} else {
						return ""; //$NON-NLS-1$
					}
				} else {
					return String.valueOf(value);
				}
			}
		}
		return JAXBCoreConstants.ZEROSTR;
	}

	/**
	 * @param active
	 *            current instance of the map
	 */
	public static void setActive(LCVariableMap active) {
		LCVariableResolver.active = active;
	}
}