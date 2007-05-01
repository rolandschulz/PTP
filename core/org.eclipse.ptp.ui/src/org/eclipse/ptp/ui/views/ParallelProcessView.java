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
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.IProcessEvent;
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
 * 
 */
public class ParallelProcessView extends AbstractTextEditor implements IProcessListener, IModelListener {
	private Label rankLabel = null;
	private Label nodeLabel = null;
	private Label jobLabel = null;
	private Label totalLabel = null;
	private Label pidLabel = null;
	private Label statusLabel = null;
	private Text outputText = null;
	private FormToolkit toolkit = null;
	private ScrolledForm myForm = null;

	public ParallelProcessView() {
		super();
		setDocumentProvider(new StorageDocumentProvider());
		PTPCorePlugin.getDefault().getModelPresentation().addModelListener(this);
		PTPCorePlugin.getDefault().getModelPresentation().addProcessListener(this);
	}
	public void dispose() {
		PTPCorePlugin.getDefault().getModelPresentation().removeModelListener(this);
		PTPCorePlugin.getDefault().getModelPresentation().removeProcessListener(this);
		myForm.dispose();
		toolkit.dispose();
		super.dispose();
	}
	public void setFocus() {
		myForm.setFocus();
	}
	public void doSave(IProgressMonitor monitor) {}
	public void doSaveAs() {}
	public boolean isDirty() {
		return false;
	}
	public boolean isSaveAsAllowed() {
		return false;
	}
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setPartName(input.getName());
		setInput(input);
	}
	public void close() {
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (toolkit != null) {
					getSite().getPage().closeEditor(ParallelProcessView.this, false);
				}
			}
		});
	}
	public IPProcess getProcess() {
		Object obj = getEditorInput().getAdapter(IPProcess.class);
		if (obj instanceof IPProcess)
			return (IPProcess) obj;
		return null;
	}
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		myForm = toolkit.createScrolledForm(parent);
		myForm.getBody().setLayout(createGridLayout(1, false, 5, 5));
		myForm.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		detailsSection();
		outputSection();
		initialText();
	}
	protected void detailsSection() {
		Section detailsSection = toolkit.createSection(myForm.getBody(), Section.TITLE_BAR);
		detailsSection.setText("Process details");
		detailsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite detailsContainer = createClientContainer(detailsSection, toolkit, 3, true, 2, 2);
		rankLabel = toolkit.createLabel(detailsContainer, null);
		rankLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nodeLabel = toolkit.createLabel(detailsContainer, null);
		nodeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		totalLabel = toolkit.createLabel(detailsContainer, null);
		totalLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pidLabel = toolkit.createLabel(detailsContainer, null);
		pidLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		jobLabel = toolkit.createLabel(detailsContainer, null);
		jobLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite statusContainer = createClientContainer(detailsContainer, toolkit, 3, true, 0, 0);
		statusContainer.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 3));
		statusLabel = toolkit.createLabel(statusContainer, null);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*
		dynamicLabel = toolkit.createLabel(statusContainer, null);
		dynamicLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		*/
		
		detailsSection.setClient(detailsContainer);
	}
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
	protected Composite createClientContainer(Composite parent, FormToolkit toolkit, int columns, boolean isEqual, int mh, int mw) {
		Composite container = toolkit.createComposite(parent);
		container.setLayout(createGridLayout(columns, isEqual, mh, mw));
		return container;
	}
	public void initialText() {
		IPProcess process = getProcess();
		
		rankLabel.setText("Number: N/A");
		totalLabel.setText("Total: N/A");
		nodeLabel.setText("Node: N/A");
		pidLabel.setText("PID: N/A");
		statusLabel.setText("Status: N/A");
		outputText.setText("N/A");
		
		if (process != null) {
			rankLabel.setText("Number: " + process.getProcessNumber());
			final IPJob job = process.getJob();
			String jobName = "none";
			if (job != null) {
				jobName = job.getName();
			}
			jobLabel.setText("Job: " + jobName);
			pidLabel.setText("PID: " + process.getPid());
			statusLabel.setText("Status: " + process.getState());
			if (job != null) {
				totalLabel.setText("Total: " + job.size());
			}
			IPNode node = process.getNode();
			if (node != null) {
				nodeLabel.setText("Node: " + node.getName());
			}
			
			outputText.setText(process.getContents());
		}
	}
	
	public void processEvent(final IProcessEvent event) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				safeProcessEvent(event);
			}
		});
	}
	private void safeProcessEvent(final IProcessEvent event) {
		if (event.getProcess() != null && event.getProcess().equals(getProcess())) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					switch (event.getType()) {
					case IProcessEvent.STATUS_CHANGE_TYPE:
						statusLabel.setText("Status: " + event.getState().toString());
						break;
						/*
					case IProcessEvent.STATUS_EXIT_TYPE:
						dynamicLabel.setText(EXITCODE_TEXT + event.getValue());
						break;
					case IProcessEvent.STATUS_SIGNALNAME_TYPE:
						dynamicLabel.setText(SIGNALNAME_TEXT + event.getValue());
						break;
						*/
					case IProcessEvent.ADD_OUTPUT_TYPE:
						outputText.append(event.getInput());
						break;			
					}
				}
			});
		}
	}
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		label.setFont(comp.getFont());
	}
	protected void updateStatusField(String category) {
		if (category == null)
			return;
		IStatusField field = getStatusField(category);
		if (field != null) {
			String text = null;
			if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION.equals(category))
				text = pidLabel.getText();
			else if (ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE.equals(category))
				text = rankLabel.getText();
			else if (ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE.equals(category)) {
				text = getProcess().getState().toString();
			}
			field.setText(text == null ? fErrorLabel : text);
		}
	}
	
	public void modelEvent(final IModelEvent event) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				safeModelEvent(event);
			}
		});
	}
	
	private void safeModelEvent(IModelEvent event) {
		if (event instanceof IModelRuntimeNotifierEvent) {
			IModelRuntimeNotifierEvent runEvent = (IModelRuntimeNotifierEvent)event;
			if (runEvent.getType() == IModelRuntimeNotifierEvent.STOPPED) {
				close();
			}
		}	
	}

}
