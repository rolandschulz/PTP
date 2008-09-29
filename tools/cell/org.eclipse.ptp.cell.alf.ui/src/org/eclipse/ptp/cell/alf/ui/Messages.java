/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.alf.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.alf.ui.messages"; //$NON-NLS-1$	
	
	/* messages for ALF wizard */
	public static String ALFWizard_title;
	public static String ALFWizard_projectNamePageName;	
	public static String ALFWizard_projectNamePageTitle;	
	public static String ALFWizard_projectNamePageDescription;
	public static String ALFWizard_yes;
	public static String ALFWizard_no;
	public static String ALFWizard_ok;
	public static String ALFWizard_errorDirDoesNotExist;
	public static String ALFWizard_errorHelpDocTitle;
	public static String ALFWizard_errorHelpDocMessage;
	public static String ALFWizard_errorCannotFinishMessage;
	public static String ALFWizard_errorWizardCanceled;
	public static String ALFWizard_errorWizardFinish;
	public static String ALFWizard_errorUnknown;
	public static String ALFWizard_errorAlfNotInstalled;
	public static String ALFWizard_errorALFTemplateNotInstalled;
	public static String ALFWizard_warningNoBuffersExist;
	public static String ALFWizard_warningBufferErrorsExist;
	public static String ALFWizard_warningUnequalNumDT;
	public static String ALFWizard_warningNoRemainingMemory;
	public static String ALFWizard_whatsThisText;

	/* messages for ALF wizard new project creation page */
	public static String ALFWizardNewProjectCreationPage_projectNameEmpty;
	public static String ALFWizardNewProjectCreationPage_projectExistsMessagePart1;
	public static String ALFWizardNewProjectCreationPage_projectExistsMessagePart2;
	
	/* messages for ALF wizard page A */
	public static String ALFWizardPageA_pageName;
	public static String ALFWizardPageA_pageTitle;
	public static String ALFWizardPageA_pageDescription;
	public static String ALFWizardPageA_stackSizeLabelMessage;
	public static String ALFWizardPageA_numAcceleratorsLabelMessage;
	public static String ALFWizardPageA_allAvailableAccelerators;
	public static String ALFWizardPageA_partitionMethodLabelMessage;
	public static String ALFWizardPageA_partitionMethodHost;
	public static String ALFWizardPageA_partitionMethodAccelerator;
	public static String ALFWizardPageA_errorMsgExpectedStackSize;
	public static String ALFWizardPageA_configuration32bit;
	public static String ALFWizardPageA_configuration64bit;
	public static String ALFWizardPageA_configurationLabelMessage;
	
	/* messages for ALF wizard page B */
	public static String ALFWizardPageB_pageName;
	public static String ALFWizardPageB_pageTitle;
	public static String ALFWizardPageB_pageDescription;
	public static String ALFWizardPageB_bufferListTitle;
	public static String ALFWizardPageB_columnOneName;
	public static String ALFWizardPageB_columnTwoName;
	public static String ALFWizardPageB_addBufferButtonText;
	public static String ALFWizardPageB_editBufferButtonText;
	public static String ALFWizardPageB_removeBufferButtonText;
	public static String ALFWizardPageB_createAlfBufferDialogTitle;
	public static String ALFWizardPageB_editAlfBufferDialogTitle;
	public static String ALFWizardPageB_inputBufferType;
	public static String ALFWizardPageB_outputBufferType;
	public static String ALFWizardPageB_notAvailableTxt;
	public static String ALFWizardPageB_noBufferSelectedForEdit;
	public static String ALFWizardPageB_numDTEntryGroupText;
	public static String ALFWizardPageB_numDTEntryLabelText;
	public static String ALFWizardPageB_numDTEntryStatusEqual;
	public static String ALFWizardPageB_numDTEntryStatusUnequal;
	public static String ALFWizardPageB_numDTEntryStatusNA;
	public static String ALFWizardPageB_localMemoryGroupText;
	public static String ALFWizardPageB_localMemoryTotalMemoryLabelText;
	public static String ALFWizardPageB_localMemoryRemainingMemoryLabelText;
	public static String ALFWizardPageB_localMemoryRemainingMemoryInitialText;
	// error messages for ALFWizardPageB
	public static String ALFWizardPageB_previousPageIsNullErrorMsg;
	public static String ALFWizardPageB_errorMsgInvalidBuffer;
	
	/* messages for ALFBufferDialog */
	public static String ALFBufferDialog_variableNameLabelMessage;
	public static String ALFBufferDialog_elementTypeLabelMessage;
	public static String ALFBufferDialog_elementUnitLabelMessage;
	public static String ALFBufferDialog_elementUnitByte;
	public static String ALFBufferDialog_elementUnitInt16;
	public static String ALFBufferDialog_elementUnitInt32;
	public static String ALFBufferDialog_elementUnitInt64;
	public static String ALFBufferDialog_elementUnitFloat;
	public static String ALFBufferDialog_elementUnitDouble;
	public static String ALFBufferDialog_elementUnitAddr32;
	public static String ALFBufferDialog_elementUnitAddr64;
	public static String ALFBufferDialog_elementUnitType;
	public static String ALFBufferDialog_bufferTypeLabelMessage;
	public static String ALFBufferDialog_bufferTypeInput;
	public static String ALFBufferDialog_bufferTypeOutput;
	public static String ALFBufferDialog_dimensionSizeLabelMessage;
	public static String ALFBufferDialog_dimensionSizeGroupMessage;
	public static String ALFBufferDialog_distributionModelGroupMessage;
	public static String ALFBufferDialog_distributionSizeGroupMessage;
	public static String ALFBufferDialog_XLabelMessage;
	public static String ALFBufferDialog_YLabelMessage;
	public static String ALFBufferDialog_ZLabelMessage;
	public static String ALFBufferDialog_oneDimensionMessage;
	public static String ALFBufferDialog_twoDimensionMessage;
	public static String ALFBufferDialog_threeDimensionMessage;
	public static String ALFBufferDialog_distributionModelStar;
	public static String ALFBufferDialog_distributionModelBlock;
	public static String ALFBufferDialog_distributionModelCyclic;
	public static String ALFBufferDialog_errorMsgBufferName;
	public static String ALFBufferDialog_errorMsgElementType;
	
	/* messages for ALFBufferValidator */
	public static String ALFBufferValidator_errorMsg;
	public static String ALFBufferValidator_errorMsgInvalidName1;
	public static String ALFBufferValidator_errorMsgInvalidName2;
	public static String ALFBufferValidator_errorMsgInvalidName3;
	public static String ALFBufferValidator_errorMsgInvalidType1;
	public static String ALFBufferValidator_errorMsgInvalidType2;
	public static String ALFBufferValidator_errorMsgSameNameExists;
	public static String ALFBufferValidator_errorMsgBlock;
	public static String ALFBufferValidator_errorMsgCyclic;
	public static String ALFBufferValidator_errorMsgDimensionSizeX;
	public static String ALFBufferValidator_errorMsgDimensionSizeY;
	public static String ALFBufferValidator_errorMsgDimensionSizeZ;
	public static String ALFBufferValidator_errorMsgDistributionSizeX1;
	public static String ALFBufferValidator_errorMsgDistributionSizeX2;
	public static String ALFBufferValidator_errorMsgDistributionSizeX3;
	public static String ALFBufferValidator_errorMsgDistributionSizeY1;
	public static String ALFBufferValidator_errorMsgDistributionSizeY2;
	public static String ALFBufferValidator_errorMsgDistributionSizeY3;
	public static String ALFBufferValidator_errorMsgDistributionSizeZ1;
	public static String ALFBufferValidator_errorMsgDistributionSizeZ2;
	public static String ALFBufferValidator_errorMsgNumDTEntries;
	// Warning messages for ALFBufferValidator
	public static String ALFBufferValidator_warningMsg;
	public static String ALFBufferValidator_warningMsgIncompatibleUnit;
	public static String ALFBufferValidator_warningMsgVoidStar;
	public static String ALFBufferValidator_warningMsgNot16ByteAligned;
    // sizeof() messages
	public static String ALFBufferValidator_sizeOfChar;
	public static String ALFBufferValidator_sizeOfShort;
	public static String ALFBufferValidator_sizeOfInt;
	public static String ALFBufferValidator_sizeOfLong32bit;
	public static String ALFBufferValidator_sizeOfLong64bit;
	public static String ALFBufferValidator_sizeOfLongSPU;
	public static String ALFBufferValidator_sizeOfLongLong;
	public static String ALFBufferValidator_sizeOfFloat;
	public static String ALFBufferValidator_sizeOfDouble;
	public static String ALFBufferValidator_sizeOfLongDouble32bit;
	public static String ALFBufferValidator_sizeOfLongDouble64bit;
	public static String ALFBufferValidator_sizeOfLongDoubleSPU;
	public static String ALFBufferValidator_sizeOfBool;
	public static String ALFBufferValidator_sizeOfVoid32bit;
	public static String ALFBufferValidator_sizeOfVoid64bit;
	public static String ALFBufferValidator_sizeOfVoidSPU;
	
	
	/* messages for ALFWizardCreationAction */
	public static String ALFWizardCreationAction_codeGeneratorCommand;
	public static String ALFWizardCreationAction_codeGeneratorOptions;
	public static String ALFWizardCreationAction_alfVersion;
	public static String ALFWizardCreationAction_templateDir;
	public static String ALFWizardCreationAction_tmpDirLocation;
	public static String ALFWizardCreationAction_xmlParamFileName;
	public static String ALFWizardCreationAction_timeZone;
	public static String ALFWizardCreationAction_tag_alf;
	public static String ALFWizardCreationAction_tag_global;
	public static String ALFWizardCreationAction_tag_ALF_VERSION;
	public static String ALFWizardCreationAction_tag_DATE_TIME;
	public static String ALFWizardCreationAction_tag_TEMPLATE_DIR;
	public static String ALFWizardCreationAction_tag_TARGET_DIR;
	public static String ALFWizardCreationAction_tag_PROJECT_NAME;
	public static String ALFWizardCreationAction_tag_STACK_SIZE;
	public static String ALFWizardCreationAction_tag_PARTITION_METHOD;
	public static String ALFWizardCreationAction_tag_EXP_ACCEL_NUM;
	public static String ALFWizardCreationAction_tag_BUFFER_NUMBER;
	public static String ALFWizardCreationAction_tag_buffer;
	public static String ALFWizardCreationAction_tag_VARIABLE_NAME;
	public static String ALFWizardCreationAction_tag_ELEMENT_TYPE;
	public static String ALFWizardCreationAction_tag_ELEMENT_UNIT;
	public static String ALFWizardCreationAction_tag_BUFFER_TYPE;
	public static String ALFWizardCreationAction_tag_NUM_DIMENSION;
	public static String ALFWizardCreationAction_tag_DIMENSION_SIZE_X;
	public static String ALFWizardCreationAction_tag_DIMENSION_SIZE_Y;
	public static String ALFWizardCreationAction_tag_DIMENSION_SIZE_Z;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_X;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_Y;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_Z;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_X;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_Y;
	public static String ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_Z;
	public static String ALFWizardCreationAction_elementUnitByte;
	public static String ALFWizardCreationAction_elementUnitInt16;
	public static String ALFWizardCreationAction_elementUnitInt32;
	public static String ALFWizardCreationAction_elementUnitInt64;
	public static String ALFWizardCreationAction_elementUnitFloat;
	public static String ALFWizardCreationAction_elementUnitDouble;
	public static String ALFWizardCreationAction_elementUnitAddr32;
	public static String ALFWizardCreationAction_elementUnitAddr64;
	public static String ALFWizardCreationAction_bufferTypeInput;
	public static String ALFWizardCreationAction_bufferTypeOutput;
	public static String ALFWizardCreationAction_distributionModelStar;
	public static String ALFWizardCreationAction_distributionModelBlock;
	public static String ALFWizardCreationAction_distributionModelCyclic;
	public static String ALFWizardCreationAction_mainTask;
	public static String ALFWizardCreationAction_taskName;
	public static String ALFWizardCreationAction_subTask1;
	public static String ALFWizardCreationAction_subTask2;
	public static String ALFWizardCreationAction_subTask3;
	public static String ALFWizardCreationAction_subTask4;
	public static String ALFWizardCreationAction_subTask5;
	public static String ALFWizardCreationAction_subTask6;
	public static String ALFWizardCreationAction_subTask7;
	public static String ALFWizardCreationAction_subTask8;
	public static String ALFWizardCreationAction_subTask9;
	public static String ALFWizardCreationAction_canceled;
	public static String ALFWizardCreationAction_queryTitle;
	public static String ALFWizardCreationAction_queryTemplateDirMessage;
	public static String ALFWizardCreationAction_queryCodeGenPath;
	public static String ALFWizardCreationAction_errorCreatingParamFileMsg;
	public static String ALFWizardCreationAction_errorCallingCodeGenMsg;
	public static String ALFWizardCreationAction_errorTemplateDir;
	public static String ALFWizardCreationAction_errorSourceFilesNotFound;
	public static String ALFWizardCreationAction_errorCreatingProject;
	public static String ALFWizardCreationAction_errorConfiguringPpuProject;
	public static String ALFWizardCreationAction_errorConfiguringSharedLibraryProject;
	public static String ALFWizardCreationAction_errorConfiguringSpuProject;
	public static String ALFWizardCreationAction_errorHeaderFileNotFound;
	public static String ALFWizardCreationAction_errorCopyingFiles;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Private constructor to prevent instances of this class.
	}
}
