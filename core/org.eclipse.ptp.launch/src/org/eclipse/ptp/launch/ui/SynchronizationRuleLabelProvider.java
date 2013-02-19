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
package org.eclipse.ptp.launch.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.internal.rulesengine.DownloadRule;
import org.eclipse.ptp.launch.internal.rulesengine.UploadRule;
import org.eclipse.swt.graphics.Image;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 * 
 * @since 7.0
 */
public class SynchronizationRuleLabelProvider implements ILabelProvider {
	Image uploadRuleImage = null;
	Image downloadRuleImage = null;

	String remoteWorkingDir;

	public SynchronizationRuleLabelProvider() {
		super();
	}

	public String getRemoteWorkingDir() {
		return remoteWorkingDir;
	}

	public void setRemoteWorkingDir(String remoteWorkingDir) {
		this.remoteWorkingDir = remoteWorkingDir;
	}

	public Image getImage(Object element) {
		if (element instanceof DownloadRule) {
			return downloadRuleImage;
		} else if (element instanceof DownloadRule) {
			return uploadRuleImage;
		} else {
			return null;
		}
	}

	public String getText(Object element) {
		if (element instanceof DownloadRule) {
			DownloadRule rule = (DownloadRule) element;
			String result = Messages.EnhancedSynchronizeTab_DownloadLabel_Type;
			if (rule.getRemoteFileCount() == 0) {
				result += Messages.EnhancedSynchronizeTab_DownloadLabel_NoFiles;
			} else if (rule.getRemoteFileCount() == 1) {
				result += Messages.EnhancedSynchronizeTab_DownloadLabel_OneFile;
			} else {
				result += NLS.bind(Messages.EnhancedSynchronizeTab_DownloadLabel_MultipleFiles,
						new Integer(rule.getRemoteFileCount()));
			}
			if (rule.getRemoteFileCount() >= 1) {
				result += '\n' + Messages.EnhancedSynchronizeTab_DownloadLabel_FromLabel;
				String files[] = rule.getRemoteFilesAsStringArray();
				for (int i = 0; i < files.length; i++) {
					String file = files[i];
					if (i != 0) {
						file += Messages.EnhancedSynchronizeTab_DownloadLabel_FileListSeparator;
					}
					result += file;
				}
				result += '\n';
				if (rule.getLocalDirectory() == null) {
					result += Messages.EnhancedSynchronizeTab_DownloadLabel_DestinationMissing;
				} else {
					result += Messages.EnhancedSynchronizeTab_DownloadLabel_DestinationLabel + rule.getLocalDirectory();
				}
				if (rule.isAsExecutable() || rule.isAsReadOnly() || rule.isPreserveTimeStamp()) {
					result += '\n' + Messages.EnhancedSynchronizeTab_DownloadLabel_OptionsLabel;
					boolean comma = false;
					if (rule.isAsExecutable()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_DownloadLabel_OptionsSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_DownloadLabel_Options_Executable;
					}
					if (rule.isAsReadOnly()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_DownloadLabel_OptionsSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_DownloadLabel_Options_Readonly;
					}
					if (rule.isPreserveTimeStamp()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_DownloadLabel_OptionsSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_DownloadLabel_Options_PreserveTimeStamp;
					}
				}
			}
			return result;
		} else if (element instanceof UploadRule) {
			UploadRule rule = (UploadRule) element;
			String result = Messages.EnhancedSynchronizeTab_UploadLabel_Type;
			if (rule.getRemoteFileCount() == 0) {
				result += Messages.EnhancedSynchronizeTab_UploadLabel_NoFiles;
			} else if (rule.getRemoteFileCount() == 1) {
				result += Messages.EnhancedSynchronizeTab_UploadLabel_OneFile;
			} else {
				result += NLS.bind(Messages.EnhancedSynchronizeTab_UploadLabel_MultipleFiles,
						new Integer(rule.getRemoteFileCount()));
			}
			if (rule.getRemoteFileCount() >= 1) {
				result += '\n' + Messages.EnhancedSynchronizeTab_UploadLabel_FromLabel;
				String files[] = rule.getLocalFilesAsStringArray();
				for (int i = 0; i < files.length; i++) {
					String file = files[i];
					if (i != 0) {
						file += Messages.EnhancedSynchronizeTab_UploadLabel_FileListSeparator;
					}
					result += file;
				}
				result += '\n';
				if ((rule.getRemoteDirectory() == null) || rule.isDefaultRemoteDirectory()) {
					result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationLabel + remoteWorkingDir;
				} else {
					IPath remoteWorkingPath = new Path(remoteWorkingDir);
					IPath remotePath = new Path(rule.getRemoteDirectory());
					if (!remotePath.isAbsolute()) {
						remotePath = remoteWorkingPath.append(remotePath);
					}
					result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationLabel + remotePath.toOSString();
				}
				if (rule.isAsExecutable() || rule.isAsReadOnly() || rule.isPreserveTimeStamp() || rule.isDownloadBack()) {
					result += '\n' + Messages.EnhancedSynchronizeTab_UploadLabel_OptionsLabel;
					boolean comma = false;
					if (rule.isAsExecutable()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_UploadLabel_Options_Executable;
					}
					if (rule.isAsReadOnly()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_UploadLabel_Options_Readonly;
					}
					if (rule.isPreserveTimeStamp()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_UploadLabel_Options_PreserveTimeStamp;
					}
					if (rule.isDownloadBack()) {
						if (comma) {
							result += Messages.EnhancedSynchronizeTab_UploadLabel_DestinationSeparator;
						} else {
							comma = true;
						}
						result += Messages.EnhancedSynchronizeTab_UploadLabel_Options_DownloadBack;
					}
				}
			}
			return result;
		} else {
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// Do nothing
	}

	public void dispose() {
		if (uploadRuleImage != null) {
			uploadRuleImage.dispose();
		}
		if (downloadRuleImage != null) {
			downloadRuleImage.dispose();
		}
		uploadRuleImage = null;
		downloadRuleImage = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}
}
