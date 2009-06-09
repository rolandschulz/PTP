/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.ui.Activator;
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

public class SLURMRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {
	
	//jobNumProces
	private Text numProcsText;
	private String numProcsString = "1"; //$NON-NLS-1$
	private static final String NUMBER_OF_PROCESSES_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_0;
	//jobNumNodes
	/*
	private Text NumNodesText;
	private String numbernodesString = "2";
	private static final String JOB_NUMBER_OF_NODES_LABEL = "Number of Nodes(-N): ";
	*/
	//jobTimeLimit
	private Text TimeLimitText;
	private String timeLimitString = "2"; //$NON-NLS-1$
	private static final String JOB_TIME_LIMIT_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_1;
	//jobPartition
	private String partationRequestedString = "3"; //$NON-NLS-1$
	private static final String JOB_PARTATION_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_2;
	//jobType
	private String jobTypeComboString = "mpi"; //$NON-NLS-1$
	private static final String JOB_TYPE_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_3;
	//jobIoLabel
	private String jobIoString = Messages.SLURMRMLaunchConfigurationDynamicTab_4;
	private static final String JOB_IO_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_5;
	//jobVerbose
	private String jobVerboseModString = Messages.SLURMRMLaunchConfigurationDynamicTab_6;
	private static final String JOB_VERBOSE_MODE_LABEL = Messages.SLURMRMLaunchConfigurationDynamicTab_7;
	//jobNodeList
	private String jobNodeListString = "node0,node1"; //$NON-NLS-1$
	private static final String JOB_NODE_LIST_LABEL =Messages.SLURMRMLaunchConfigurationDynamicTab_8;
	
	private static final String ATTR_PREFIX = Activator.PLUGIN_ID + ".launchAttributes"; //$NON-NLS-1$
	private static final String ATTR_NUMPROCS = ATTR_PREFIX + ".numProcs";	 //$NON-NLS-1$
	private static final String ATTR_NUMNODES = ATTR_PREFIX + ".numNodes"; //$NON-NLS-1$
	private static final String ATTR_TIMELIMIT = ATTR_PREFIX + ".timeLimit"; //$NON-NLS-1$
	private static final String ATTR_JOBPARTATION = ATTR_PREFIX + ".jobpartation"; //$NON-NLS-1$
	private static final String ATTR_JOBTYPE = ATTR_PREFIX + ".jobtype"; //$NON-NLS-1$
	private static final String ATTR_JOBIO = ATTR_PREFIX + ".jobio"; //$NON-NLS-1$
	private static final String ATTR_JOBVERBOSEMODE = ATTR_PREFIX + ".jobverbosemode";	 //$NON-NLS-1$
	private static final RMLaunchValidation success = new RMLaunchValidation(true, ""); //$NON-NLS-1$
	
	private Composite control;

	public SLURMRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		
		/*
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobNumNodesAttrDef, numbernodesString);
			//System.out.println("the iattr value is:"+iattr+"the numprocsString value is:"+numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		*/
		
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobTimeLimitAttrDef, timeLimitString);		
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		/*
		
		try {
			IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobPartationAttrDef, partationRequestedString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_PARTATION_LABEL + e.getMessage());
		}
		
		
		try {
			IntegerAttributeDefinition jobTypeComboAttrDef = getJobTypeComboAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobTypeComboAttrDef, jobTypeComboString);		
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TYPE_LABEL + e.getMessage());
		}
		
		
		try {
			IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobIoAttrDef, jobIoString);		
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_IO_LABEL + e.getMessage());
		}		
		*/
				
		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public void createControl(Composite parent,	IResourceManager rm, IPQueue queue) {
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
		
		
		Label numberOfProcessesnLabel = new Label(comp, SWT.NONE);
		numberOfProcessesnLabel.setText(NUMBER_OF_PROCESSES_LABEL);
		final GridData gd_numberOfProcessesnLabel = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_numberOfProcessesnLabel.minimumWidth = numberOfProcessesnLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		numberOfProcessesnLabel.setLayoutData(gd_numberOfProcessesnLabel);
		
		
		numProcsText = new Text(comp, SWT.BORDER | SWT.WRAP);
		numProcsText.setTextLimit(10);
		numProcsText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_9);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, numColumns-1, 1);
		gd.minimumWidth = numProcsText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		numProcsText.setLayoutData(gd);

		// Tell the client of this dynamic tab that the
		// contents of this tab are affected by the contents
		// of this widget.
		numProcsText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				//System.out.println("The value of numProcsString:"+numProcsString);
				numProcsString = numProcsText.getText();
				//System.out.println("The value of numProcsString1:"+numProcsString);
				fireContentsChanged();
			}});
        /*
		final Label numberOfNodesLabel = new Label(comp, SWT.NONE);
		numberOfNodesLabel.setText(JOB_NUMBER_OF_NODES_LABEL);

		NumNodesText = new Text(comp, SWT.BORDER | SWT.WRAP);
		NumNodesText.setTextLimit(10);
		NumNodesText.setToolTipText("Please input the number of nodes!");
		final GridData gd_numNodesText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		NumNodesText.setLayoutData(gd_numNodesText);
		//NumNodesText   value
		NumNodesText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				//System.out.println("The value of numbernodesString:"+numbernodesString);
				numbernodesString = NumNodesText.getText();
				//System.out.println("The value of numbernodesString1:"+numbernodesString);
				fireContentsChanged();
			}});
		
        */
		final Label timeLimitLabel = new Label(comp, SWT.NONE);
		timeLimitLabel.setText(JOB_TIME_LIMIT_LABEL);

		TimeLimitText = new Text(comp, SWT.BORDER | SWT.WRAP);
		TimeLimitText.setTextLimit(10);
		TimeLimitText.setToolTipText(Messages.SLURMRMLaunchConfigurationDynamicTab_10);
		final GridData gd_timeLimitText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		TimeLimitText.setLayoutData(gd_timeLimitText);
		//TimeLimitText   value
		TimeLimitText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				//System.out.println("The value of timeLimitString:"+timeLimitString);
				timeLimitString = TimeLimitText.getText();
				//System.out.println("The value of timeLimitString1:"+timeLimitString);
				fireContentsChanged();
			}});
		//partationRequestedCombo   value
		//JobIdText   value
		//jobTypeCombo   value
		//JobIOLableText   value
		//JobVerboseModeText  value
		//JobNodelistText  value
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getAttributes(org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute<?,?,?>[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration, String mode) throws CoreException {
		
		int numProcs = configuration.getAttribute(ATTR_NUMPROCS, -1);
		IntegerAttribute iattr = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			iattr = new IntegerAttribute(numProcsAttrDef, numProcs);
		} catch (IllegalValueException e) {
			return new IAttribute[0];
		}
		
		/*				
		int jobNumNodes = configuration.getAttribute(ATTR_NUMNODES, -1);
		//System.out.println("The value of jobNumNodes is: "+jobNumNodes);
	
		IntegerAttribute iattr1 = null;
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			iattr1 = new IntegerAttribute(jobNumNodesAttrDef, jobNumNodes);
			//System.out.println("The value of NumNodes is: "+iattr1);
		} catch (IllegalValueException e) {
			return new IAttribute[1];
		}
		*/			
		int jobTimeLimit = configuration.getAttribute(ATTR_TIMELIMIT, -1);
		IntegerAttribute iattr2 = null;
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, jobTimeLimit);
			//System.out.println("The value of TimeLimit is: "+iattr2);
		} catch (IllegalValueException e) {
			return new IAttribute[2];
		}
		
		/*			
		int jobPartation = configuration.getAttribute(ATTR_JOBPARTATION, -1);
		IntegerAttribute iattr3 = null;
		try {
			IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobPartationAttrDef, jobPartation);
		} catch (IllegalValueException e) {
			return new IAttribute[3];
		}
					
		int jobTypeCombo = configuration.getAttribute(ATTR_JOBTYPE, -1);
		IntegerAttribute iattr3 = null;
		try {
			IntegerAttributeDefinition jobTypeComboAttrDef = getJobTimeLimitAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobTypeComboAttrDef, jobTypeCombo);
		} catch (IllegalValueException e) {
			return new IAttribute[3];
		}			
			
		int jobIo = configuration.getAttribute(ATTR_JOBIO, -1);
		IntegerAttribute iattr3 = null;
		try {
			IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
			iattr3 = new IntegerAttribute(jobIoAttrDef, jobIo);
		} catch (IllegalValueException e) {
			return new IAttribute[3];
		}
		*/
		
		
		
		return new IAttribute[]{iattr,iattr2};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getControl()
	 */
	public Control getControl() {
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#initializeFrom(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm,
			IPQueue queue, ILaunchConfiguration configuration) {
		
		int numProcs;
		try {
			numProcs = configuration.getAttribute(ATTR_NUMPROCS, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
				numProcsText.setText(numProcsAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL +
						e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		numProcsText.setText(Integer.toString(numProcs));
		/*
		int jobNumNodes;
		try {
			jobNumNodes = configuration.getAttribute(ATTR_NUMNODES, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
				NumNodesText.setText(jobNumNodesAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL +
						e.getMessage() + " : " + e1.getMessage());
			}
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		NumNodesText.setText(Integer.toString(jobNumNodes));
		*/		
		int jobTimeLimit;
		try {
			jobTimeLimit = configuration.getAttribute(ATTR_TIMELIMIT, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
				TimeLimitText.setText(jobTimeLimitAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL +
						e.getMessage() + " : " + e1.getMessage()); //$NON-NLS-1$
			}
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		TimeLimitText.setText(Integer.toString(jobTimeLimit));
		/*		
		int jobPartation;
		try {
			jobPartation = configuration.getAttribute(ATTR_JOBPARTATION, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
				partationRequestedCombo.setText(jobPartationAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_PARTATION_LABEL +
						e.getMessage() + " : " + e1.getMessage());
			}
			return new RMLaunchValidation(false, JOB_PARTATION_LABEL + e.getMessage());
		}
		partationRequestedCombo.setText(Integer.toString(jobPartation));
		int jobTypeCom;
		try {
			jobTypeCom = configuration.getAttribute(ATTR_JOBTYPE, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobTypeComboAttrDef = getJobTypeComboAttrDef(rm, queue);
				jobTypeCombo.setText(jobTypeComboAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_TYPE_LABEL +
						e.getMessage() + " : " + e1.getMessage());
			}
			return new RMLaunchValidation(false, JOB_TYPE_LABEL + e.getMessage());
		}
		jobTypeCombo.setText(Integer.toString(jobTypeCom));	
		int jobIo;
		try {
			jobIo = configuration.getAttribute(ATTR_JOBIO, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
				JobIOLableText.setText(jobIoAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, JOB_IO_LABEL +
						e.getMessage() + " : " + e1.getMessage());
			}
			return new RMLaunchValidation(false, JOB_IO_LABEL + e.getMessage());
		}
		JobIOLableText.setText(Integer.toString(jobIo));
		*/
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */

	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_numProcsInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		/*
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobNumNodesAttrDef, numbernodesString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.getString("SLURMConfigurationWizardPage.numbernodesInvalid")); //$NON-NLS-1$
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		*/
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobTimeLimitAttrDef, timeLimitString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.SLURMConfigurationWizardPage_timeLimitInvalid);
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		/*
		try {
			IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobPartationAttrDef, partationRequestedString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.getString("SLURMConfigurationWizardPage.numProcsInvalid")); //$NON-NLS-1$
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_PARTATION_LABEL + e.getMessage());
		}
		try {
			IntegerAttributeDefinition jobTypeComboAttrDef = getJobTypeComboAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobTypeComboAttrDef, jobTypeComboString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.getString("SLURMConfigurationWizardPage.numProcsInvalid")); //$NON-NLS-1$
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TYPE_LABEL + e.getMessage());
		}
		try {
			IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(jobIoAttrDef, jobIoString);
			if (iattr.getValue() < 1) {
				return new RMLaunchValidation(false, Messages.getString("SLURMConfigurationWizardPage.numProcsInvalid")); //$NON-NLS-1$
			}
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_IO_LABEL + e.getMessage());
		}
		*/
		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		IntegerAttribute iattr = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_NUMPROCS, iattr.getValue());
		/*
		IntegerAttribute iattr1 = null;
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			iattr1 = new IntegerAttribute(jobNumNodesAttrDef, numbernodesString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_NUMNODES, iattr1.getValue());
		*/
		IntegerAttribute iattr2 = null;
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			iattr2 = new IntegerAttribute(jobTimeLimitAttrDef, timeLimitString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_TIMELIMIT, iattr2.getValue());
		/*
		try {
			IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
			iattr = new IntegerAttribute(jobPartationAttrDef, partationRequestedString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_PARTATION_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_JOBPARTATION, iattr.getValue());
		try {
			IntegerAttributeDefinition jobTypeComboAttrDef = getJobTypeComboAttrDef(rm, queue);
			iattr = new IntegerAttribute(jobTypeComboAttrDef, jobTypeComboString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TYPE_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_TIMELIMIT, iattr.getValue());
		try {
			IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
			iattr = new IntegerAttribute(jobIoAttrDef, jobIoString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_IO_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_JOBIO, iattr.getValue());
		*/
		//System.out.println(ATTR_NUMNODES);
		
		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			configuration.setAttribute(ATTR_NUMPROCS, numProcsAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		/*
		try {
			IntegerAttributeDefinition jobNumNodesAttrDef = getJobNumNodesAttrDef(rm, queue);
			configuration.setAttribute(ATTR_NUMNODES, jobNumNodesAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_NUMBER_OF_NODES_LABEL + e.getMessage());
		}
		*/
		try {
			IntegerAttributeDefinition jobTimeLimitAttrDef = getJobTimeLimitAttrDef(rm, queue);
			configuration.setAttribute(ATTR_TIMELIMIT, jobTimeLimitAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TIME_LIMIT_LABEL + e.getMessage());
		}
		/*
		try {
			IntegerAttributeDefinition jobPartationAttrDef = getJobPartationAttrDef(rm, queue);
			configuration.setAttribute(ATTR_JOBPARTATION, jobPartationAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_PARTATION_LABEL + e.getMessage());
		}
		try {
			IntegerAttributeDefinition jobTypeComboAttrDef = getJobTypeComboAttrDef(rm, queue);
			configuration.setAttribute(ATTR_JOBTYPE, jobTypeComboAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_TYPE_LABEL + e.getMessage());
		}	
		try {
			IntegerAttributeDefinition jobIoAttrDef = getJobIoAttrDef(rm, queue);
			configuration.setAttribute(ATTR_JOBIO, jobIoAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, JOB_IO_LABEL + e.getMessage());
		}	
		*/
		return success;
	}

	/**
	 * Get the attribute definition for the number of processes job launch attribute
	 * 
	 * @param rm
	 * @param queue
	 * @return
	 */
	private IntegerAttributeDefinition getNumProcsAttrDef(IResourceManager rm, IPQueue queue) {
		return JobAttributes.getNumberOfProcessesAttributeDefinition();
	}
	private IntegerAttributeDefinition getJobNumNodesAttrDef(IResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getNumberOfNodesAttributeDefinition();
	}
	private IntegerAttributeDefinition getJobTimeLimitAttrDef(IResourceManager rm, IPQueue queue) {
		return SLURMJobAttributes.getTimeLimitAttributeDefinition();
	}
	/*
	private IntegerAttributeDefinition getJobPartationAttrDef(IResourceManager rm, IPQueue queue) {
		return JobAttributes.getJobPartationAttributeDefinition();
	}
	private IntegerAttributeDefinition getJobTypeComboAttrDef(IResourceManager rm, IPQueue queue) {
		return JobAttributes.getJobTypeComboAttributeDefinition();
	}
	private IntegerAttributeDefinition getJobIoAttrDef(IResourceManager rm, IPQueue queue) {
		return JobAttributes.getJobIoAttributeDefinition();
	}
	*/
	//need to modify JobAttributes.java
}