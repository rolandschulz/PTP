/*******************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science, 
 *National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMLaunchConfiguration;
import org.eclipse.ptp.rm.slurm.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SLURMRMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab {

	// ntasks
	private Text nProcsText;
	private String nProcsString = "1"; //$NON-NLS-1$ 
	private static final String JOB_NUMBER_OF_PROCESSES_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_nprocs;

	// nnodes
	private Text nNodesText;
	private String nNodesString = "1"; //$NON-NLS-1$ 
	private static final String JOB_NUMBER_OF_NODES_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_nnodes;

	// tlimit
	private Text tLimitText;
	private String tLimitString = "5"; //$NON-NLS-1$
	private static final String JOB_TIME_LIMIT_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_tlimit;

	// partition
	private Text partText;
	private String partString = ""; //$NON-NLS-1$
	private static final String JOB_PARTITION_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_partition;

	// nodeList requested
	private Text reqNodeListText;
	private String reqNodeListString = ""; //$NON-NLS-1$
	private static final String JOB_REQUESTED_NODELIST_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_reqlist;

	// nodeList excluded
	private Text excNodeListText;
	private String excNodeListString = ""; //$NON-NLS-1$
	private static final String JOB_EXCLUDED_NODELIST_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_exclist;

	private static final RMLaunchValidation success = new RMLaunchValidation(true, ""); //$NON-NLS-1$

	private Composite control;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	public RMLaunchValidation canSave(Control control) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef();
			@SuppressWarnings("unused")
			IntegerAttribute iattr0 = new IntegerAttribute(numProcsAttrDef, nProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef();
			@SuppressWarnings("unused")
			IntegerAttribute iattr1 = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef();
			@SuppressWarnings("unused")
			IntegerAttribute iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		// Not necessary to check following StringAttribute

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr3 = new StringAttribute(jobPartitionAttrDef, partString);

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr4 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr5 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite,
	 * java.lang.String)
	 */
	public void createControl(Composite parent, String id) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, true));
		GridData gd;
		final int numColumns = 2;

		final Composite comp = new Composite(control, SWT.BORDER);
		GridLayout launchConfigLayout = new GridLayout(numColumns, true);
		launchConfigLayout.marginHeight = 0;
		launchConfigLayout.marginWidth = 0;
		launchConfigLayout.numColumns = numColumns;
		comp.setLayout(launchConfigLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = numColumns;
		comp.setLayoutData(gd);

		/* ntasks label */
		final Label nProcsLabel = new Label(comp, SWT.NONE);
		nProcsLabel.setText(JOB_NUMBER_OF_PROCESSES_LABEL);
		final GridData gd_nProcsLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_nProcsLabel.minimumWidth = nProcsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		nProcsLabel.setLayoutData(gd_nProcsLabel);
		/* ntasks text */
		nProcsText = new Text(comp, SWT.BORDER | SWT.WRAP);
		nProcsText.setTextLimit(20);
		nProcsText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_nprocs_tip);
		final GridData gd_nProcsText = new GridData(SWT.FILL, SWT.FILL, true, false, numColumns - 1, 1);
		nProcsText.setLayoutData(gd_nProcsText);
		// Tell the client of this dynamic tab that the
		// contents of this tab are affected by the contents
		// of this widget.
		nProcsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				nProcsString = nProcsText.getText();
				fireContentsChanged();
			}
		});

		/* nnodes label */
		final Label nNodesLabel = new Label(comp, SWT.NONE);
		nNodesLabel.setText(JOB_NUMBER_OF_NODES_LABEL);
		final GridData gd_nNodesLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_nNodesLabel.minimumWidth = nNodesLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		nNodesLabel.setLayoutData(gd_nNodesLabel);
		/* nnodes text */
		nNodesText = new Text(comp, SWT.BORDER | SWT.WRAP);
		nNodesText.setTextLimit(20);
		nNodesText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_nnodes_tip);
		final GridData gd_nNodesText = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		nNodesText.setLayoutData(gd_nNodesText);
		nNodesText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				nNodesString = nNodesText.getText();
				fireContentsChanged();
			}
		});

		/* timelimit label */
		final Label tLimitLabel = new Label(comp, SWT.NONE);
		tLimitLabel.setText(JOB_TIME_LIMIT_LABEL);
		final GridData gd_tLimitLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_tLimitLabel.minimumWidth = tLimitLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		tLimitLabel.setLayoutData(gd_tLimitLabel);
		/* timelimit text */
		tLimitText = new Text(comp, SWT.BORDER | SWT.WRAP);
		tLimitText.setTextLimit(10);
		tLimitText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_tlimit_tip);
		final GridData gd_tLimitText = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		tLimitText.setLayoutData(gd_tLimitText);
		tLimitText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tLimitString = tLimitText.getText();
				fireContentsChanged();
			}
		});

		/* partition label */
		final Label partLabel = new Label(comp, SWT.NONE);
		partLabel.setText(JOB_PARTITION_LABEL);
		final GridData gd_partLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_partLabel.minimumWidth = partLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		partLabel.setLayoutData(gd_partLabel);
		/* partition text */
		partText = new Text(comp, SWT.BORDER | SWT.WRAP);
		partText.setTextLimit(20);
		partText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_partition_tip);
		final GridData gd_partText = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		partText.setLayoutData(gd_partText);
		partText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				partString = partText.getText();
				fireContentsChanged();
			}
		});

		/* requested nodeList label */
		final Label reqNodeListLabel = new Label(comp, SWT.NONE);
		reqNodeListLabel.setText(JOB_REQUESTED_NODELIST_LABEL);
		final GridData gd_reqNodeListLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_reqNodeListLabel.minimumWidth = reqNodeListLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		reqNodeListLabel.setLayoutData(gd_reqNodeListLabel);
		/* requested nodeList text */
		reqNodeListText = new Text(comp, SWT.BORDER | SWT.WRAP);
		reqNodeListText.setTextLimit(200);
		reqNodeListText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_reqlist_tip);
		final GridData gd_reqNodeListText = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		reqNodeListText.setLayoutData(gd_reqNodeListText);
		reqNodeListText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				reqNodeListString = reqNodeListText.getText();
				fireContentsChanged();
			}
		});

		/* excluded nodeList label */
		final Label excNodeListLabel = new Label(comp, SWT.NONE);
		excNodeListLabel.setText(JOB_EXCLUDED_NODELIST_LABEL);
		final GridData gd_excNodeListLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_excNodeListLabel.minimumWidth = excNodeListLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		excNodeListLabel.setLayoutData(gd_excNodeListLabel);
		/* excluded nodeList text */
		excNodeListText = new Text(comp, SWT.BORDER | SWT.WRAP);
		excNodeListText.setTextLimit(200);
		excNodeListText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_exclist_tip);
		final GridData gd_excNodeListText = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		excNodeListText.setLayoutData(gd_excNodeListText);
		excNodeListText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				excNodeListString = excNodeListText.getText();
				fireContentsChanged();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {

		int jobnumProcs;
		try {
			jobnumProcs = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_NUMPROCS, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition nProcsAttrDef = getJobNumProcsAttrDef();
				nProcsText.setText(nProcsAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		nProcsText.setText(Integer.toString(jobnumProcs));

		int jobNumNodes;
		try {
			jobNumNodes = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_NUMNODES, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef();
				nNodesText.setText(jobNumNodesAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage() + " : " + e1.getMessage());//$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		nNodesText.setText(Integer.toString(jobNumNodes));

		int jobTimeLimit;
		try {
			jobTimeLimit = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_TIMELIMIT, 5);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef();
				tLimitText.setText(jobTimeLimitAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		tLimitText.setText(Integer.toString(jobTimeLimit));

		String jobPartition;
		try {
			jobPartition = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBPARTITION, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef();
			partText.setText(jobPartitionAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_PARTITION_LABEL + e.getMessage());
		}
		partText.setText(jobPartition);

		String jobReqNodeList;
		try {
			jobReqNodeList = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBREQNODELIST, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef();
			reqNodeListText.setText(jobReqNodeListAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_REQUESTED_NODELIST_LABEL + e.getMessage());
		}
		reqNodeListText.setText(jobReqNodeList);

		String jobExcNodeList;
		try {
			jobExcNodeList = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBEXCNODELIST, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef();
			excNodeListText.setText(jobExcNodeListAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_EXCLUDED_NODELIST_LABEL + e.getMessage());
		}
		excNodeListText.setText(jobExcNodeList);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */

	public RMLaunchValidation isValid(ILaunchConfiguration configuration) {

		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef();
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, nProcsString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_numProcsInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef();
			IntegerAttribute iattr = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_numNodesInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef();
			IntegerAttribute iattr = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_timeLimitInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		// Not necessary to check following StringAttribute

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr0 = new StringAttribute(jobPartitionAttrDef, partString);

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr1 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef();
		@SuppressWarnings("unused")
		StringAttribute sattr2 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {

		IntegerAttribute iattr0 = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef();
			iattr0 = new IntegerAttribute(numProcsAttrDef, nProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_NUMPROCS, iattr0.getValue());

		IntegerAttribute iattr1 = null;
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef();
			iattr1 = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_NUMNODES, iattr1.getValue());

		IntegerAttribute iattr2 = null;
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef();
			iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_TIMELIMIT, iattr2.getValue());

		StringAttribute sattr0 = null;
		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef();
		sattr0 = new StringAttribute(jobPartitionAttrDef, partString);
		if (sattr0.getValue().length() > 0) {
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBPARTITION, sattr0.getValue());
		} else {
			configuration.removeAttribute(SLURMLaunchConfiguration.ATTR_JOBPARTITION);
		}

		StringAttribute sattr1 = null;
		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef();
		sattr1 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);
		if (sattr1.getValue().length() > 0) {
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBREQNODELIST, sattr1.getValue());
		} else {
			configuration.removeAttribute(SLURMLaunchConfiguration.ATTR_JOBREQNODELIST);
		}

		StringAttribute sattr2 = null;
		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef();
		sattr2 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);
		if (sattr2.getValue().length() > 0) {
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBEXCNODELIST, sattr2.getValue());
		} else {
			configuration.removeAttribute(SLURMLaunchConfiguration.ATTR_JOBEXCNODELIST);
		}

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef();
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_NUMPROCS, numProcsAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef();
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_NUMNODES, jobNumNodesAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef();
			configuration.setAttribute(SLURMLaunchConfiguration.ATTR_TIMELIMIT, jobTimeLimitAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef();
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBPARTITION, jobPartitionAttrDef.create().getValue());

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef();
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBREQNODELIST, jobReqNodeListAttrDef.create().getValue());

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef();
		configuration.setAttribute(SLURMLaunchConfiguration.ATTR_JOBEXCNODELIST, jobExcNodeListAttrDef.create().getValue());

		return success;
	}

	/**
	 * Get the attribute definition for the number of processes job launch attribute
	 * 
	 * @param rm
	 * @param queue
	 * @return
	 */
	private IntegerAttributeDefinition getJobNumProcsAttrDef() {
		return SLURMJobAttributes.getJobNumberOfProcsAttributeDefinition();
	}

	private IntegerAttributeDefinition getJobNumNodesAttrDef() {
		return SLURMJobAttributes.getJobNumberOfNodesAttributeDefinition();
	}

	private IntegerAttributeDefinition getJobTimeLimitAttrDef() {
		return SLURMJobAttributes.getJobTimelimitAttributeDefinition();
	}

	private StringAttributeDefinition getJobPartitionAttrDef() {
		return SLURMJobAttributes.getJobPartitionAttributeDefinition();
	}

	private StringAttributeDefinition getJobReqNodeListAttrDef() {
		return SLURMJobAttributes.getJobReqNodeListAttributeDefinition();
	}

	private StringAttributeDefinition getJobExcNodeListAttrDef() {
		return SLURMJobAttributes.getJobExcNodeListAttributeDefinition();
	}

}