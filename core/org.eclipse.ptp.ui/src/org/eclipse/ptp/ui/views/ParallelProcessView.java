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
package org.eclipse.ptp.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.events.IProcessChangeEvent;
import org.eclipse.ptp.core.elements.listeners.IProcessListener;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * View for displaying details about an individual process
 */
public class ParallelProcessView extends AbstractTextEditor implements IProcessListener {
	private Label pidLabel = null;
	private Label statusLabel = null;
	private Text outputText = null;
	private FormToolkit toolkit = null;
	private ScrolledForm myForm = null;
	private IPProcess process = null;

	public ParallelProcessView() {
		super();
		setDocumentProvider(new StorageDocumentProvider());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#dispose()
	 */
	public void dispose() {
		process.removeElementListener(this);
		myForm.dispose();
		toolkit.dispose();
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setFocus()
	 */
	public void setFocus() {
		myForm.setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSaveAs()
	 */
	public void doSaveAs() {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setPartName(input.getName());
		setInput(input);
		Object obj = getEditorInput().getAdapter(IPProcess.class);
		if (obj instanceof IPProcess) {
			process = (IPProcess) obj;
			process.addElementListener(this);
		}
	}
	
	/**
	 * 
	 */
	public void close() {
		if (process != null) {
			process.removeElementListener(this);
		}
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (toolkit != null) {
					getSite().getPage().closeEditor(ParallelProcessView.this, false);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		myForm = toolkit.createScrolledForm(parent);
		myForm.getBody().setLayout(createGridLayout(1, false, 5, 5));
		myForm.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		detailsSection();
		outputSection();
		initialText();
	}
	
	/**
	 * Add the process details to the view
	 */
	protected void detailsSection() {
		Section detailsSection = toolkit.createSection(myForm.getBody(), Section.TITLE_BAR);
		detailsSection.setText("Process details");
		detailsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite detailsContainer = createClientContainer(detailsSection, toolkit, 3, true, 2, 2);
		pidLabel = toolkit.createLabel(detailsContainer, null);
		pidLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite statusContainer = createClientContainer(detailsContainer, toolkit, 3, true, 0, 0);
		statusContainer.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 3));
		statusLabel = toolkit.createLabel(statusContainer, null);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		detailsSection.setClient(detailsContainer);
	}
	
	/**
	 * Add the process output section to the view
	 */
	protected void outputSection() {
		Section outputSection = toolkit.createSection(myForm.getBody(), Section.TITLE_BAR | Section.TWISTIE);
		outputSection.setText("Program output");
		outputSection.setLayoutData(new GridData(GridData.FILL_BOTH));
		outputSection.setExpanded(true);
		Composite ouputContainer = createClientContainer(outputSection, toolkit, 1, true, 2, 2);
		outputText = toolkit.createText(ouputContainer, null, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		outputText.setLayoutData(gd);
		outputSection.setClient(ouputContainer);
		toolkit.paintBordersFor(ouputContainer);
	}
	
	/**
	 * Convenience method to create a container
	 * 
	 * @param parent
	 * @param toolkit
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected Composite createClientContainer(Composite parent, FormToolkit toolkit, int columns, 
			boolean isEqual, int mh, int mw) {
		Composite container = toolkit.createComposite(parent);
		container.setLayout(createGridLayout(columns, isEqual, mh, mw));
		return container;
	}
	
	/**
	 * Initialize the view
	 */
	public void initialText() {
		pidLabel.setText("PID: N/A");
		statusLabel.setText("Status: N/A");
		outputText.setText("N/A");
		
		if (process != null) {
			pidLabel.setText("PID: " + process.getPid());
			statusLabel.setText("Status: " + process.getState());

			/*
			 * Set initial output text
			 */
			outputText.setText(process.getSavedOutput(ProcessAttributes.getStdoutAttributeDefinition()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.IProcessChangedEvent)
	 */
	public void handleEvent(final IProcessChangeEvent e) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				EnumeratedAttribute<ProcessAttributes.State> stateAttr = e.getAttributes().getAttribute(ProcessAttributes.getStateAttributeDefinition());
				if (stateAttr != null) {
					ProcessAttributes.State state = stateAttr.getValue();
					statusLabel.setText("Status: " + state.toString());
				} 
				
				StringAttribute stdoutAttr = e.getAttributes().getAttribute(ProcessAttributes.getStdoutAttributeDefinition());
				if (stdoutAttr != null) {
					outputText.append(stdoutAttr.getValue() + "\n");
				}
			}
		});
	}
	
	/**
	 * Convenience method to create a grid layout
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}
	
	/**
	 * Convenience method to create a GridData object
	 * 
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updateStatusField(java.lang.String)
	 */
	protected void updateStatusField(String category) {
		if (category == null) {
			return;
		}
		IStatusField field = getStatusField(category);
		if (field != null) {
			String text = null;
			if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION.equals(category)) {
				text = pidLabel.getText();
			} else if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE.equals(category)) {
				if (process != null) {
					text = process.getState().toString();
				}
			}
			field.setText(text == null ? fErrorLabel : text);
		}
	}
}
