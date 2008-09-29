/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.packagemanager;

import java.util.List;

import org.eclipse.ptp.cell.utils.linux.packagemanager.rpm.RPMPackageManager;
import org.eclipse.ptp.cell.utils.platform.Platform;


/**
 * This class checks which Package Management System should be used on a
 * specific platform supported by Cell IDE. Notice that the Package Management
 * Systems that implement PackageManager interface should work also in other
 * platforms not supported by Cell IDE and hence someone can use its classes to
 * access the features they provide outside the context defined in this class.
 * 
 * @author laggarcia
 * @since 1.3.1
 */
public class PackageManagementSystemManager {

	public static PackageManager getPackageManager() {

		String arch = org.eclipse.core.runtime.Platform.getOSArch();
		String linuxDistro = Platform.getOSLinuxDistro();

		// Check whether we are in an x86, x86-64 or ppc platform
		if (arch.equals(org.eclipse.core.runtime.Platform.ARCH_X86)
				|| arch.equals(org.eclipse.core.runtime.Platform.ARCH_X86_64)
				|| arch.equals(org.eclipse.core.runtime.Platform.ARCH_PPC)) {
			// Check for Linux and Linux distros
			if (linuxDistro.equals(Platform.LINUX_DISTRO_FEDORA_CORE_6)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_FEDORA_7)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_FEDORA_8)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_FEDORA_9)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_RHEL5)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_RHEL5U1)
					|| linuxDistro.equals(Platform.LINUX_DISTRO_RHEL5U2)) {
				return RPMPackageManager.getDefault();
			}
		}

		// this system is not supported
		return new PackageManager() {

			public boolean query(String pack) {
				return false;
			}

			public boolean queryAll(String[] packs) {
				return false;
			}

			public boolean queryAll(String packs, String delimiter) {
				return false;
			}

			public List <String> queryList(String pack) {
				return null;
			}

			public String searchFileInQueryListAndReturnInitialPathSegments(
					String pack, String pathEnd) {
				return null;
			}

			public String searchFileInQueryListAndReturnFullPath(String pack,
					String pathEnd) {
				return null;
			}

		};

	}
}
