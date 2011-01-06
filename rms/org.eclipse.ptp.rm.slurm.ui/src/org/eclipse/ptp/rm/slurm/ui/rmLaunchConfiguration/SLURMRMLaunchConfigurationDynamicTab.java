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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.ui.SLURMUIPlugin;
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

	private static final String ATTR_PREFIX = SLURMUIPlugin.getUniqueIdentifier() + ".launchAttributes"; //$NON-NLS-1$
	private static final String ATTR_NUMPROCS = ATTR_PREFIX + ".numProcs"; //$NON-NLS-1$
	private static final String ATTR_NUMNODES = ATTR_PREFIX + ".numNodes"; //$NON-NLS-1$
	private static final String ATTR_TIMELIMIT = ATTR_PREFIX + ".timeLimit"; //$NON-NLS-1$
	private static final String ATTR_JOBPARTITION = ATTR_PREFIX + ".jobPartition"; //$NON-NLS-1$
	private static final String ATTR_JOBREQNODELIST = ATTR_PREFIX + ".jobReqNodeList"; //$NON-NLS-1$
	private static final String ATTR_JOBEXCNODELIST = ATTR_PREFIX + ".jobExcNodeList"; //$NON-NLS-1$
	private static final RMLaunchValidation success = new RMLaunchValidation(true, ""); //$NON-NLS-1$

	private Composite control;

	public SLURMRMLaunchConfigurationDynamicTab(IPResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IPResourceManager rm, IPQueue queue) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr0 = new IntegerAttribute(numProcsAttrDef, nProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr1 = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		// Not necessary to check following StringAttribute

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr3 = new StringAttribute(jobPartitionAttrDef, partString);

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr4 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr5 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public void createControl(Composite parent, IPResourceManager rm, IPQueue queue) {
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
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getAttributes(org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute<?, ?, ?>[] getAttributes(IPResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {

		int jobnumProcs = configuration.getAttribute(ATTR_NUMPROCS, -1);
		IntegerAttribute iattr0 = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
			iattr0 = new IntegerAttribute(numProcsAttrDef, jobnumProcs);
		} catch (IllegalValueException e) {
			return new IAttribute[] { iattr0 };
		}

		int jobNumNodes = configuration.getAttribute(ATTR_NUMNODES, -1);
		IntegerAttribute iattr1 = null;
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			iattr1 = new IntegerAttribute(jobNumNodesAttrDef, jobNumNodes);
		} catch (IllegalValueException e) {
			return new IAttribute[1];
		}

		int jobTimeLimit = configuration.getAttribute(ATTR_TIMELIMIT, -1);
		IntegerAttribute iattr2 = null;
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, jobTimeLimit);
		} catch (IllegalValueException e) {
			return new IAttribute[2];
		}

		String jobPartition = configuration.getAttribute(ATTR_JOBPARTITION, "");//$NON-NLS-1$
		StringAttribute sattr3 = null;
		if (jobPartition.length() > 0) {
			StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
			sattr3 = new StringAttribute(jobPartitionAttrDef, jobPartition);
		}

		String jobReqNodeList = configuration.getAttribute(ATTR_JOBREQNODELIST, "");//$NON-NLS-1$
		StringAttribute sattr4 = null;
		if (jobReqNodeList.length() > 0) {
			StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
			sattr4 = new StringAttribute(jobReqNodeListAttrDef, jobReqNodeList);
		}

		String jobExcNodeList = configuration.getAttribute(ATTR_JOBEXCNODELIST, "");//$NON-NLS-1$
		StringAttribute sattr5 = null;
		if (jobExcNodeList.length() > 0) {
			StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
			sattr5 = new StringAttribute(jobExcNodeListAttrDef, jobExcNodeList);
		}

		ArrayList<IAttribute<?, ?, ?>> int_al = new ArrayList<IAttribute<?, ?, ?>>();
		if (iattr0 != null)
			int_al.add(iattr0);
		if (iattr1 != null)
			int_al.add(iattr1);
		if (iattr2 != null)
			int_al.add(iattr2);

		ArrayList<IAttribute<?, ?, ?>> str_al = new ArrayList<IAttribute<?, ?, ?>>();
		if (sattr3 != null)
			str_al.add(sattr3);
		if (sattr4 != null)
			str_al.add(sattr4);
		if (sattr5 != null)
			str_al.add(sattr5);

		int size1 = int_al.size();
		int size2 = str_al.size();
		int size = size1 + size2;
		IAttribute<?, ?, ?>[] attr = new IAttribute<?, ?, ?>[size];
		int i;
		for (i = 0; i < size1; i++) {
			attr[i] = int_al.get(i);
		}
		for (i = 0; i < size2; i++) {
			attr[size1 + i] = str_al.get(i);
		}

		return attr;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IPResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {

		int jobnumProcs;
		try {
			jobnumProcs = configuration.getAttribute(ATTR_NUMPROCS, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition nProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
				nProcsText.setText(nProcsAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		nProcsText.setText(Integer.toString(jobnumProcs));

		int jobNumNodes;
		try {
			jobNumNodes = configuration.getAttribute(ATTR_NUMNODES, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
				nNodesText.setText(jobNumNodesAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage() + " : " + e1.getMessage());//$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		nNodesText.setText(Integer.toString(jobNumNodes));

		int jobTimeLimit;
		try {
			jobTimeLimit = configuration.getAttribute(ATTR_TIMELIMIT, 5);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
				tLimitText.setText(jobTimeLimitAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		tLimitText.setText(Integer.toString(jobTimeLimit));

		String jobPartition;
		try {
			jobPartition = configuration.getAttribute(ATTR_JOBPARTITION, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
			partText.setText(jobPartitionAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_PARTITION_LABEL + e.getMessage());
		}
		partText.setText(jobPartition);

		String jobReqNodeList;
		try {
			jobReqNodeList = configuration.getAttribute(ATTR_JOBREQNODELIST, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
			reqNodeListText.setText(jobReqNodeListAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_REQUESTED_NODELIST_LABEL + e.getMessage());
		}
		reqNodeListText.setText(jobReqNodeList);

		String jobExcNodeList;
		try {
			jobExcNodeList = configuration.getAttribute(ATTR_JOBEXCNODELIST, "");//$NON-NLS-1$
		} catch (CoreException e) {
			StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
			excNodeListText.setText(jobExcNodeListAttrDef.create().getValueAsString());
			return new RMLaunchValidation(false, JOB_EXCLUDED_NODELIST_LABEL + e.getMessage());
		}
		excNodeListText.setText(jobExcNodeList);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */

	public RMLaunchValidation isValid(ILaunchConfiguration configuration, IPResourceManager rm, IPQueue queue) {

		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, nProcsString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_numProcsInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			IntegerAttribute iattr = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_numNodesInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			IntegerAttribute iattr = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_timeLimitInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		// Not necessary to check following StringAttribute

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr0 = new StringAttribute(jobPartitionAttrDef, partString);

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr1 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
		@SuppressWarnings("unused")
		StringAttribute sattr2 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IPResourceManager rm, IPQueue queue) {

		IntegerAttribute iattr0 = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
			iattr0 = new IntegerAttribute(numProcsAttrDef, nProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_NUMPROCS, iattr0.getValue());

		IntegerAttribute iattr1 = null;
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			iattr1 = new IntegerAttribute(jobNumNodesAttrDef, nNodesString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_NUMNODES, iattr1.getValue());

		IntegerAttribute iattr2 = null;
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, tLimitString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_TIMELIMIT, iattr2.getValue());

		StringAttribute sattr0 = null;
		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
		sattr0 = new StringAttribute(jobPartitionAttrDef, partString);
		if (sattr0.getValue().length() > 0)
			configuration.setAttribute(ATTR_JOBPARTITION, sattr0.getValue());
		else
			configuration.removeAttribute(ATTR_JOBPARTITION);

		StringAttribute sattr1 = null;
		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
		sattr1 = new StringAttribute(jobReqNodeListAttrDef, reqNodeListString);
		if (sattr1.getValue().length() > 0)
			configuration.setAttribute(ATTR_JOBREQNODELIST, sattr1.getValue());
		else
			configuration.removeAttribute(ATTR_JOBREQNODELIST);

		StringAttribute sattr2 = null;
		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
		sattr2 = new StringAttribute(jobExcNodeListAttrDef, excNodeListString);
		if (sattr2.getValue().length() > 0)
			configuration.setAttribute(ATTR_JOBEXCNODELIST, sattr2.getValue());
		else
			configuration.removeAttribute(ATTR_JOBEXCNODELIST);

		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IPResourceManager rm, IPQueue queue) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getJobNumProcsAttrDef(rm, queue);
			configuration.setAttribute(ATTR_NUMPROCS, numProcsAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			configuration.setAttribute(ATTR_NUMNODES, jobNumNodesAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}

		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			configuration.setAttribute(ATTR_TIMELIMIT, jobTimeLimitAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}

		StringAttributeDefinition jobPartitionAttrDef = getJobPartitionAttrDef(rm, queue);
		configuration.setAttribute(ATTR_JOBPARTITION, jobPartitionAttrDef.create().getValue());

		StringAttributeDefinition jobReqNodeListAttrDef = getJobReqNodeListAttrDef(rm, queue);
		configuration.setAttribute(ATTR_JOBREQNODELIST, jobReqNodeListAttrDef.create().getValue());

		StringAttributeDefinition jobExcNodeListAttrDef = getJobExcNodeListAttrDef(rm, queue);
		configuration.setAttribute(ATTR_JOBEXCNODELIST, jobExcNodeListAttrDef.create().getValue());

		return success;
	}

	/**
	 * Get the attribute definition for the number of processes job launch
	 * attribute
	 * 
	 * @param rm
	 * @param queue
	 * @return
	 */
	private IntegerAttributeDefinition getJobNumProcsAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobNumberOfProcsAttributeDefinition();
	}

	private IntegerAttributeDefinition getJobNumNodesAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobNumberOfNodesAttributeDefinition();
	}

	private IntegerAttributeDefinition getJobTimeLimitAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobTimelimitAttributeDefinition();
	}

	private StringAttributeDefinition getJobPartitionAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobPartitionAttributeDefinition();
	}

	private StringAttributeDefinition getJobReqNodeListAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobReqNodeListAttributeDefinition();
	}

	private StringAttributeDefinition getJobExcNodeListAttrDef(IPResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getJobExcNodeListAttributeDefinition();
	}

}