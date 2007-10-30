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
package org.eclipse.ptp.remotetools.environment.launcher.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ptp.remotetools.environment.launcher.data.DownloadRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.UploadRule;


public class RuleActionFactory {
	
	private ILaunchProcessCallback process;

	public RuleActionFactory(ILaunchProcessCallback process) {
		super();
		this.process = process;
	}

	public IRuleAction getAction(ISynchronizationRule rule) {
		if (rule instanceof DownloadRule) {
			DownloadRule downloadRule = (DownloadRule) rule;
			DownloadRuleAction action = new DownloadRuleAction(process, downloadRule);
			return action;
		} else if (rule instanceof UploadRule) {
			UploadRule uploadRule = (UploadRule) rule;
			UploadRuleAction action = new UploadRuleAction(process, uploadRule);
			return action;
		} else if (rule instanceof DownloadBackRule) {
			DownloadBackRule uploadRule = (DownloadBackRule) rule;
			DownloadBackAction action = new DownloadBackAction(process, uploadRule);
			return action;
		} else {
			Assert.isLegal(false);
			return null;
		}
	}

}
