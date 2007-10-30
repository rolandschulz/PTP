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
package org.eclipse.ptp.remotetools.environment.launcher.data;


public class RuleFactory {
	
	/**
	 * Returns a new synchronization rule object for the serialized string.
	 * @param string The serialized string
	 * @return The rule or null if no rule is known for the string.
	 */
	public static ISynchronizationRule createRuleFromString(String string) {
		String list[] = string.split("\n"); //$NON-NLS-1$
		String first = list[0];
		if (first.equalsIgnoreCase(SerializationKeys.TYPE_UPLOAD)) {
			return new UploadRule(string);
		} else if (first.equalsIgnoreCase(SerializationKeys.TYPE_DOWNLOAD)) {
			return new DownloadRule(string);
		} else {
			return null;
		}
	}
	
	public static ISynchronizationRule duplicateRule(ISynchronizationRule rule) {
		if (rule instanceof DownloadRule) {
			DownloadRule downloadRule = (DownloadRule) rule;
			return new DownloadRule(downloadRule);
		} if (rule instanceof UploadRule) {
			UploadRule uploadRule = (UploadRule) rule;
			return new UploadRule(uploadRule);
		} else {
			return null;
		}
	}
}
