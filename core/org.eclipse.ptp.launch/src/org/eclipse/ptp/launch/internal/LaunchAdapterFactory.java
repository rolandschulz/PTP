/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.launch.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.debug.core.launch.IPLaunch;

/**
 * Interim class to obtain an IPLaunch from its corresponding ILaunchConfiguration. This class will be removed in later versions of
 * PTP.
 * 
 * @deprecated
 */
@Deprecated
public class LaunchAdapterFactory implements IAdapterFactory {

	private static Map<ILaunchConfiguration, IPLaunch> fMap = Collections
			.synchronizedMap(new HashMap<ILaunchConfiguration, IPLaunch>());

	public static void addLaunch(ILaunchConfiguration config, IPLaunch launch) {
		fMap.put(config, launch);
	}

	public static IPLaunch getLaunch(ILaunchConfiguration config) {
		return fMap.get(config);
	}

	public static IPLaunch removeLaunch(ILaunchConfiguration config) {
		return fMap.remove(config);
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IPLaunch.class) {
			if (adaptableObject instanceof ILaunchConfiguration) {
				return getLaunch((ILaunchConfiguration) adaptableObject);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IPLaunch.class };
	}

}
