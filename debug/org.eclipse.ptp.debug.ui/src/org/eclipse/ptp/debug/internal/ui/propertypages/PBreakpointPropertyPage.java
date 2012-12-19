/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.propertypages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * @author clement chu
 * 
 */
public class PBreakpointPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {
		/**
		 * Constructor
		 * 
		 * @param name
		 * @param labelText
		 * @param parent
		 */
		public BreakpointIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			setErrorMessage(Messages.PBreakpointPropertyPage_0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.StringFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
		 */
		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doStore()
		 */
		@Override
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#clearErrorMessage()
		 */
		@Override
		protected void clearErrorMessage() {
			if (getPage() != null) {
				String message = getPage().getErrorMessage();
				if (message != null) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				} else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class BreakpointStringFieldEditor extends StringFieldEditor {
		/**
		 * Constructor
		 * 
		 * @param name
		 * @param labelText
		 * @param parent
		 */
		public BreakpointStringFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.StringFieldEditor#checkState()
		 */
		@Override
		protected boolean checkState() {
			Text control = getTextControl();
			if (!control.isEnabled()) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doStore()
		 */
		@Override
		protected void doStore() {
			Text text = getTextControl();
			if (text.isEnabled()) {
				super.doStore();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
		 */
		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#clearErrorMessage()
		 */
		@Override
		protected void clearErrorMessage() {
			if (getPage() != null) {
				String message = getPage().getErrorMessage();
				if (message != null) {
					if (getErrorMessage().equals(message)) {
						super.clearErrorMessage();
					}
				} else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class LabelFieldEditor extends FieldEditor {
		private Label titleLabel;
		private Label valueLabel;
		private Composite basicComposite;
		private final String value;
		private final String title;

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param title
		 * @param value
		 */
		public LabelFieldEditor(Composite parent, String title, String value) {
			this.value = value;
			this.title = title;
			this.createControl(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
		 */
		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) basicComposite.getLayoutData()).horizontalSpan = numColumns;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse
		 * .swt.widgets.Composite, int)
		 */
		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			basicComposite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 2;
			basicComposite.setLayout(layout);
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			basicComposite.setLayoutData(data);
			titleLabel = new Label(basicComposite, SWT.NONE);
			titleLabel.setText(title);
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			titleLabel.setLayoutData(gd);
			valueLabel = new Label(basicComposite, SWT.WRAP);
			valueLabel.setText(value);
			gd = new GridData();
			valueLabel.setLayoutData(gd);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
		 */
		@Override
		public int getNumberOfControls() {
			return 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
		 */
		@Override
		protected void doLoad() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
		 */
		@Override
		protected void doLoadDefault() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doStore()
		 */
		@Override
		protected void doStore() {
		}
	}

	private BooleanFieldEditor enabled;
	private BreakpointStringFieldEditor condition;
	private Text ignoreCountTextControl;
	private BreakpointIntegerFieldEditor ignoreCount;
	private IAdaptable element;
	private final PBreakpointPreferenceStore pBreakpointPreferenceStore;
	private UIDebugManager uiDebugManager = null;

	/**
	 * Constructor
	 * 
	 */
	public PBreakpointPropertyPage() {
		super(GRID);
		noDefaultAndApplyButton();
		uiDebugManager = PTPDebugUIPlugin.getUIDebugManager();
		pBreakpointPreferenceStore = new PBreakpointPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		IPBreakpoint breakpoint = getBreakpoint();
		try {
			createTypeSpecificLabelFieldEditors(breakpoint);
			createEnabledField(getFieldEditorParent());
			IPreferenceStore store = getPreferenceStore();
			String condition = breakpoint.getCondition();
			if (condition == null) {
				condition = ""; //$NON-NLS-1$
			}
			store.setValue(PBreakpointPreferenceStore.CONDITION, condition);
			createConditionEditor(getFieldEditorParent());
			store.setValue(PBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled());
			int ignoreCount = breakpoint.getIgnoreCount();
			store.setValue(PBreakpointPreferenceStore.IGNORE_COUNT, (ignoreCount >= 0) ? ignoreCount : 0);
			createIgnoreCountEditor(getFieldEditorParent());
		} catch (CoreException ce) {
			PTPDebugUIPlugin.errorDialog(getShell(), Messages.PBreakpointPropertyPage_2, "", ce); //$NON-NLS-1$
		}
	}

	/**
	 * Create type specific lable field editors
	 * 
	 * @param breakpoint
	 * @throws CoreException
	 */
	private void createTypeSpecificLabelFieldEditors(IPBreakpoint breakpoint) throws CoreException {
		if (breakpoint instanceof IPFunctionBreakpoint) {
			IPFunctionBreakpoint funcBpt = (IPFunctionBreakpoint) breakpoint;
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_4,
					Messages.PBreakpointPropertyPage_5));
			String function = funcBpt.getFunction();
			if (function != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_6, function));
			}
		} else if (breakpoint instanceof IPAddressBreakpoint) {
			IPAddressBreakpoint addrBpt = (IPAddressBreakpoint) breakpoint;
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_7,
					Messages.PBreakpointPropertyPage_8));
			String address = addrBpt.getAddress();
			if (address != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_9, address));
			}
		} else if (breakpoint instanceof IPWatchpoint) {
			IPWatchpoint watchpoint = (IPWatchpoint) breakpoint;
			String type = ""; //$NON-NLS-1$
			if (watchpoint.isReadType() && !watchpoint.isWriteType()) {
				type = Messages.PBreakpointPropertyPage_11;
			} else if (!watchpoint.isReadType() && watchpoint.isWriteType()) {
				type = Messages.PBreakpointPropertyPage_12;
			} else {
				type = Messages.PBreakpointPropertyPage_13;
			}

			String expression = watchpoint.getExpression();
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_14, type));
			String projectName = breakpoint.getMarker().getResource().getLocation().toOSString();
			if (projectName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_15, projectName));
			}
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_16, expression));
		} else if (breakpoint instanceof ILineBreakpoint) {
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_17,
					Messages.PBreakpointPropertyPage_18));
			String fileName = breakpoint.getSourceHandle();
			if (fileName != null) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_19, fileName));
			}
			ILineBreakpoint lineBpt = (ILineBreakpoint) breakpoint;
			int lineNumber = lineBpt.getLineNumber();
			if (lineNumber > 0) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_20, String.valueOf(lineNumber)));
			}
		}
		// Set
		String job_id = breakpoint.getJobId();
		String jobName = breakpoint.getJobName();
		addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_21, jobName));
		String set_id = breakpoint.getSetId();
		addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_22, set_id));
		// show total number of processes
		if (!job_id.equals(IPBreakpoint.GLOBAL)) {
			IElementHandler setManager = uiDebugManager.getElementHandler(job_id);
			IElementSet elementSet = setManager.getSet(set_id);
			addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_23,
					String.valueOf(elementSet.size())));
			String[] setNames = elementSet.getMatchSetIDs();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < setNames.length; i++) {
				buffer.append(setNames[i]);
				if (i < setNames.length - 1) {
					buffer.append(","); //$NON-NLS-1$
				}
			}
			if (buffer.length() > 0) {
				addField(createLabelEditor(getFieldEditorParent(), Messages.PBreakpointPropertyPage_25, buffer.toString()));
			}
		}
	}

	/**
	 * Create enabled field
	 * 
	 * @param parent
	 */
	protected void createEnabledField(Composite parent) {
		enabled = new BooleanFieldEditor(PBreakpointPreferenceStore.ENABLED, Messages.PBreakpointPropertyPage_26, parent);
		addField(enabled);
	}

	/**
	 * Create condition field
	 * 
	 * @param parent
	 */
	protected void createConditionEditor(Composite parent) {
		condition = new BreakpointStringFieldEditor(PBreakpointPreferenceStore.CONDITION, Messages.PBreakpointPropertyPage_27,
				parent);
		condition.setEmptyStringAllowed(true);
		condition.setErrorMessage(Messages.PBreakpointPropertyPage_28);
		addField(condition);
	}

	/**
	 * Create ignore count field
	 * 
	 * @param parent
	 */
	protected void createIgnoreCountEditor(Composite parent) {
		ignoreCount = new BreakpointIntegerFieldEditor(PBreakpointPreferenceStore.IGNORE_COUNT,
				Messages.PBreakpointPropertyPage_29, parent);
		ignoreCount.setValidRange(0, Integer.MAX_VALUE);
		ignoreCountTextControl = ignoreCount.getTextControl(parent);
		try {
			ignoreCountTextControl.setEnabled(getBreakpoint().getIgnoreCount() >= 0);
		} catch (CoreException ce) {
			PTPDebugUIPlugin.log(ce);
		}
		addField(ignoreCount);
	}

	/**
	 * Create label field
	 * 
	 * @param parent
	 * @param title
	 * @param value
	 * @return
	 */
	protected FieldEditor createLabelEditor(Composite parent, String title, String value) {
		return new LabelFieldEditor(parent, title, value);
	}

	/**
	 * Get breakpoint
	 * 
	 * @return
	 */
	protected IPBreakpoint getBreakpoint() {
		IAdaptable element = getElement();
		return (element instanceof IPBreakpoint) ? (IPBreakpoint) element : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime
	 * .IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		return pBreakpointPreferenceStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		final List<String> changedProperties = new ArrayList<String>(5);
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				changedProperties.add(event.getProperty());
			}
		});
		boolean result = super.performOk();
		setBreakpointProperties(changedProperties);
		return result;
	}

	/**
	 * Set breakpoint properties
	 * 
	 * @param changedProperties
	 */
	protected void setBreakpointProperties(final List<String> changedProperties) {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IPBreakpoint breakpoint = getBreakpoint();
				Iterator<String> changed = changedProperties.iterator();
				while (changed.hasNext()) {
					String property = changed.next();
					if (property.equals(PBreakpointPreferenceStore.ENABLED)) {
						breakpoint.setEnabled(getPreferenceStore().getBoolean(PBreakpointPreferenceStore.ENABLED));
					} else if (property.equals(PBreakpointPreferenceStore.IGNORE_COUNT)) {
						breakpoint.setIgnoreCount(getPreferenceStore().getInt(PBreakpointPreferenceStore.IGNORE_COUNT));
					} else if (property.equals(PBreakpointPreferenceStore.CONDITION)) {
						breakpoint.setCondition(getPreferenceStore().getString(PBreakpointPreferenceStore.CONDITION));
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException ce) {
			PTPDebugUIPlugin.log(ce);
		}
	}
}
