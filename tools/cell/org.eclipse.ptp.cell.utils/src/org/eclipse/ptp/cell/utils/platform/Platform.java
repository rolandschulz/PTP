/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * This class is a convinience class to ease the the procedure to check if the
 * current platform is PPC64 or Cell.
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public final class Platform {

	protected static final String COMPATIBLE = "/proc/device-tree/compatible"; //$NON-NLS-1$

	protected static final String CELL_COMPATIBLE_ID = "CBEA"; //$NON-NLS-1$

	/**
	 * Constant string (value "cell") indicating the platform is running on an
	 * Cell BE-based architecture.
	 */
	public static final String ARCH_CELL = "cell"; //$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown Linux distro.
	 */
	public static final String LINUX_DISTRO_UNKNOWN = "unknown"; //$NON-NLS-1$

	/**
	 * Constant string (value "Fedora Core 6") indicating the platform is running on a machine running Fedora Core 6 Linux distro.
	 */
	public static final String LINUX_DISTRO_FEDORA_CORE_6 = "Fedora Core 6"; //$NON-NLS-1$

	/**
	 * Constant string (value "Fedora 7") indicating the platform is running on a machine running Fedora 7 Linux distro.
	 */
	public static final String LINUX_DISTRO_FEDORA_7 = "Fedora 7"; //$NON-NLS-1$
	
	/**
	 * Constant string (value "Fedora 8") indicating the platform is running on a machine running Fedora 8 Linux distro.
	 */
	public static final String LINUX_DISTRO_FEDORA_8 = "Fedora 8"; //$NON-NLS-1$
	
	/**
	 * Constant string (value "Fedora 9") indicating the platform is running on a machine running Fedora 9 Linux distro.
	 */
	public static final String LINUX_DISTRO_FEDORA_9 = "Fedora 9"; //$NON-NLS-1$
	
	/**
	 * Constant string (value "Red Hat Enterprise Linux 5") indicating the platform is running on a machine running Red Hat Enterprise Linux 5 Linux distro.
	 */
	public static final String LINUX_DISTRO_RHEL5 = "Red Hat Enterprise Linux 5"; //$NON-NLS-1$

	/**
	 * Constant string (value "Red Hat Enterprise Linux 5 Update 1") indicating the platform is running on a machine running Red Hat Enterprise Linux 5 Update 1 Linux distro.
	 */
	public static final String LINUX_DISTRO_RHEL5U1 = "Red Hat Enterprise Linux 5 Update 1"; //$NON-NLS-1$
	
	/**
	 * Constant string (value "Red Hat Enterprise Linux 5 Update 2") indicating the platform is running on a machine running Red Hat Enteprise Linux 5 Update 2 Linux distro.
	 */
	public static final String LINUX_DISTRO_RHEL5U2 = "Red Hat Enterprise Linux 5 Update 2"; //$NON-NLS-1$

	private Platform() {
		// Private constructor to prevent instances of this class.
	}
	
	/**
	 * Cell boxes currenlty are identified as PPC architectures by Eclipse's
	 * org.eclipse.core.runtime.Platform. This method tries to find if we are
	 * currently running on a real Cell box based on the fact that Linux is the
	 * only supported OS currently available for Cell and Cell IDE.
	 * 
	 * @return architecture
	 */
	public static String getOSArch() {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_PLATFORM);

		String architecture = org.eclipse.core.runtime.Platform.getOSArch();
		String compatibilityLine;

		if (org.eclipse.core.runtime.Platform.getOS().equals(
				org.eclipse.core.runtime.Platform.OS_LINUX)) {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Detected Linux."); //$NON-NLS-1$

			if (architecture.equals(org.eclipse.core.runtime.Platform.ARCH_PPC)) {
				Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Detected Linux on PPC."); //$NON-NLS-1$
	
				File compatible = new File(COMPATIBLE);
				try {
					BufferedReader reader = new BufferedReader(new FileReader(
							compatible));
					Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Check file: {0}", COMPATIBLE); //$NON-NLS-1$
					
					compatibilityLine = reader.readLine();
					Debug.POLICY.trace(Debug.DEBUG_PLATFORM_MORE, "Content: {0}", compatibilityLine); //$NON-NLS-1$
					if ((compatibilityLine != null)
							&& (compatibilityLine.indexOf(CELL_COMPATIBLE_ID) != -1)) {
						architecture = ARCH_CELL;
					}
				} catch (FileNotFoundException e) {
					Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Assuming not {1} because file does not exist: {0}", e.getMessage(), ARCH_CELL); //$NON-NLS-1$
				} catch (IOException ioe) {
					Debug.POLICY.error(ioe.getMessage());
					Debug.POLICY.logError(ioe, Messages.Platform_FailedReadFile, compatible.toString());
				}
			}
		}

		Debug.POLICY.exit(Debug.DEBUG_PLATFORM, architecture);
		return architecture;
	}

	/**
	 * Returns the string name of the current Linux Distro. <code>LINUX_DISTRO_UNKNOWN</code> is
	 * returned if the operating system cannot be determined.
	 * 
	 * @return the string name of the current Linux Distro
	 */
	public static String getOSLinuxDistro() {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_PLATFORM);

		if (!org.eclipse.core.runtime.Platform.getOS().equals(
				org.eclipse.core.runtime.Platform.OS_LINUX)) {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Not a Linux distro"); //$NON-NLS-1$
			return LINUX_DISTRO_UNKNOWN;
		}

		File distroIdFile;

		// try Fedora
		distroIdFile = new File(IdentityInformation.Fedora);
		try {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Check file: {0}", IdentityInformation.Fedora); //$NON-NLS-1$
			String release = (new BufferedReader(new FileReader(distroIdFile)))
					.readLine();
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM_MORE, "Content: {0}", release); //$NON-NLS-1$
			// try Fedora Core 6
			if (release.equals(IdentityInformation.FedoraCore6)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_FEDORA_CORE_6);
				return LINUX_DISTRO_FEDORA_CORE_6;
			}
			// try Fedora 7
			if (release.equals(IdentityInformation.Fedora7)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_FEDORA_7);
				return LINUX_DISTRO_FEDORA_7;
			}
			// try Fedora 8
			if (release.equals(IdentityInformation.Fedora8)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_FEDORA_8);
				return LINUX_DISTRO_FEDORA_8;
			}
			// try Fedora 9
			if (release.equals(IdentityInformation.Fedora9)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_FEDORA_9);
				return LINUX_DISTRO_FEDORA_9;
			}
		} catch (FileNotFoundException e) {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Assuming not Fedora because files does not exist: {0}", distroIdFile.toString()); //$NON-NLS-1$
			// No fedora-release file. This is not Fedora
		} catch (IOException e) {
			Debug.POLICY.error(e);
			// Problems reading the fedora-release file. Assuming this is not Fedora.
			Debug.POLICY.logError(e, Messages.Platform_FailedReadFile, distroIdFile.toString());
		}

		// Try Red Hat Enterprise Linux
		distroIdFile = new File(IdentityInformation.RHEL);
		try {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Check file: {0}", IdentityInformation.RHEL); //$NON-NLS-1$
			String release = (new BufferedReader(new FileReader(distroIdFile)))
					.readLine();
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM_MORE, "Content: {0}", release); //$NON-NLS-1$
			//try Red Hat Enterprise Linux 5
			if (release.equals(IdentityInformation.RedHatEnterpriseLinux5)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_RHEL5U1);
				return LINUX_DISTRO_RHEL5;
			}
			// try Red Hat Enterprise Linux 5 Update 1
			if (release.equals(IdentityInformation.RedHatEnterpriseLinux5U1)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_RHEL5U1);
				return LINUX_DISTRO_RHEL5U1;
			}
			// try Red Hat Enterprise Linux 5 Update 2
			if (release.equals(IdentityInformation.RedHatEnterpriseLinux5U2)) {
				Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_RHEL5U2);
				return LINUX_DISTRO_RHEL5U2;
			}
		} catch (FileNotFoundException e) {
			Debug.POLICY.trace(Debug.DEBUG_PLATFORM, "Assuming not RHEL, because file does not exist: {0}", distroIdFile); //$NON-NLS-1$
			// No redhat-release file. This is not Red Hat Enterprise Linux
		} catch (IOException e) {
			Debug.POLICY.error(e);
			Debug.POLICY.logError(e, Messages.Platform_FailedReadFile, distroIdFile.toString());
		}

		Debug.POLICY.exit(Debug.DEBUG_PLATFORM, LINUX_DISTRO_UNKNOWN);
		return LINUX_DISTRO_UNKNOWN;		
	}

}
