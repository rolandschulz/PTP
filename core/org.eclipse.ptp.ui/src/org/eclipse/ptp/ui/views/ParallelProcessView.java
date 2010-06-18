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

import java.util.BitSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.messages.Messages;
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
public class ParallelProcessView extends AbstractTextEditor implements IJobChildListener {
	private Label pidLabel = null;
	private Label statusLabel = null;
	private Text outputText = null;
	private FormToolkit toolkit = null;
	private ScrolledForm myForm = null;
	// FIXME PProcessUI goes away when we address UI scalability. See Bug 311057
	private PProcessUI process = null;

	public ParallelProcessView() {
		super();
		setDocumentProvider(new StorageDocumentProvider());
	}

	/**
	 * 
	 */
	public void close() {
		if (process != null) {
			process.getJob().removeChildListener(this);
		}
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (toolkit != null) {
					getSite().getPage().closeEditor(ParallelProcessView.this, false);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.AbstractTextEditor#createPartControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		myForm = toolkit.createScrolledForm(parent);
		myForm.getBody().setLayout(createGridLayout(1, false, 5, 5));
		myForm.getBody().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		detailsSection();
		outputSection();
		initialText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#dispose()
	 */
	@Override
	public void dispose() {
		process.getJob().removeChildListener(this);
		myForm.dispose();
		toolkit.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.
	 * runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * @since 4.0
	 */
	public void handleEvent(final IChangedProcessEvent e) {
		UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
			public void run() {
				final BitSet procRanks = e.getProcesses();
				if (!procRanks.get(process.getJobRank())) {
					return;
				}
				EnumeratedAttribute<State> statusAttr = e.getAttributes().getAttribute(
						ProcessAttributes.getStateAttributeDefinition());
				if (statusAttr != null) {
					statusLabel.setText(Messages.ParallelProcessView_6 + statusAttr.getValue());
				}

				StringAttribute stdoutAttr = e.getAttributes().getAttribute(ProcessAttributes.getStdoutAttributeDefinition());
				if (stdoutAttr != null) {
					outputText.append(stdoutAttr.getValue());
				}
			}
		});
	}

	/**
	 * @since 4.0
	 */
	public void handleEvent(INewProcessEvent e) {
		// no-op
	}

	/**
	 * @since 4.0
	 */
	public void handleEvent(IRemoveProcessEvent e) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite
	 * , org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setPartName(input.getName());
		setInput(input);
		// FIXME PProcessUI goes away when we address UI scalability. See Bug
		// 311057
		Object obj = getEditorInput().getAdapter(PProcessUI.class);
		if (obj instanceof PProcessUI) {
			process = (PProcessUI) obj;
			process.getJob().addChildListener(this);
		}
	}

	/**
	 * Initialize the view
	 */
	public void initialText() {
		pidLabel.setText(Messages.ParallelProcessView_2);
		statusLabel.setText(Messages.ParallelProcessView_3);
		outputText.setText(Messages.ParallelProcessView_4);

		if (process != null) {
			if (process.getPid() == 0) {
				pidLabel.setText(Messages.ParallelProcessView_5 + "N/A"); //$NON-NLS-1$
			} else {
				pidLabel.setText(Messages.ParallelProcessView_5 + process.getPid());
			}

			statusLabel.setText(Messages.ParallelProcessView_6 + process.getState());

			/*
			 * Set initial output text
			 */
			outputText.setText(process.getSavedOutput());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setFocus()
	 */
	@Override
	public void setFocus() {
		myForm.setFocus();
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
	protected Composite createClientContainer(Composite parent, FormToolkit toolkit, int columns, boolean isEqual, int mh, int mw) {
		Composite container = toolkit.createComposite(parent);
		container.setLayout(createGridLayout(columns, isEqual, mh, mw));
		return container;
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
	 * Add the process details to the view
	 */
	protected void detailsSection() {
		Section detailsSection = toolkit.createSection(myForm.getBody(), Section.TITLE_BAR);
		detailsSection.setText(Messages.ParallelProcessView_0);
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
		outputSection.setText(Messages.ParallelProcessView_1);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.AbstractTextEditor#updateStatusField(java.lang
	 * .String)
	 */
	@Override
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
