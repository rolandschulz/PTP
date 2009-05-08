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
package org.eclipse.ptp.launch.rulesengine;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.launch.data.DownloadBackRule;
import org.eclipse.ptp.launch.data.DownloadRule;
import org.eclipse.ptp.launch.data.ISynchronizationRule;
import org.eclipse.ptp.launch.data.UploadRule;

/**
 * TODO NEEDS TO BE DOCUMENTED
 */
public class RuleActionFactory {
	
	private ILaunchProcessCallback process;
	private ILaunchConfiguration configuration;
	private IProgressMonitor monitor;

	public RuleActionFactory(ILaunchConfiguration configuration, ILaunchProcessCallback process, IProgressMonitor monitor) {
		super();
		this.process = process;
		this.configuration = configuration;
		this.monitor = monitor;
	}

	public IRuleAction getAction(ISynchronizationRule rule) throws CoreException {
		if (rule instanceof DownloadRule) {
			DownloadRule downloadRule = (DownloadRule) rule;
			DownloadRuleAction action = new DownloadRuleAction(process, configuration, downloadRule, monitor);
			return action;
		} else if (rule instanceof UploadRule) {
			UploadRule uploadRule = (UploadRule) rule;
			UploadRuleAction action = new UploadRuleAction(process, configuration, uploadRule, monitor);
			return action;
		} else if (rule instanceof DownloadBackRule) {
			DownloadBackRule uploadRule = (DownloadBackRule) rule;
			DownloadBackAction action = new DownloadBackAction(process, configuration, uploadRule, monitor);
			return action;
		} else {
			Assert.isLegal(false);
			return null;
		}
	}

}
