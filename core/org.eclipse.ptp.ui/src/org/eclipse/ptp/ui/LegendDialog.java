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

package org.eclipse.ptp.ui;

import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class LegendDialog extends Dialog 
{
	private String rmStateText[] = {
		"STARTING",
		"STARTED",
		"STOPPING",
		"STOPPED",
		"SUSPENDED",
		"ERROR"
	};
	
	private String machineStateText[] = {
		"UP",
		"DOWN",
		"ALERT",
		"ERROR",
		"UNKNOWN"
	};

	private String jobStateText[] = {
		"PENDING",
		"STARTED",
		"RUNNING",
		"TERMINATED",
		"SUSPENDED",
		"ERROR",
		"UNKNOWN"		
	};
	
	private String nodeStateText[] = {
		"UP",
		"DOWN",
		"ERROR",
		"UNKNOWN",
		"ALLOCATED TO USER EXCLUSIVELY",
		"ALLOCATED TO USER SHARED",
		"ALLOCATED TO OTHER EXCLUSIVELY",
		"ALLOCATED TO OTHER SHARED",
		"PROCESS RUNNING ON NODE",
		"TERMINATED PROCESS ON NODE",
	};
	
	private String processStateText[] = {
		"STARTING",
		"RUNNING",
		"EXITED NORMALLY",
		"EXITED WITH SIGNAL",
		"SUSPENDED",
		"ERROR",
		"UNKNOWN"
	};
	
	/**
	 * @param parent
	 */
	public LegendDialog(Shell parent) {
		/* we DON'T want this to be modal */
		this(parent, SWT.DIALOG_TRIM | SWT.NONE);
	}
	
	public LegendDialog(Shell parent, int style) {
		super(parent, style);
		setText("Legend");
	}
	
	public String open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(1, true));
		
		/* the RM box */
		Group box = new Group(shell, SWT.BORDER);
		box.setText("Resource Manager Icons");
		FillLayout fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		FormData fd ;
		GridData data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < rmStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
	        FormLayout layout = new FormLayout();     
	        c.setLayout(layout);
	        /* the contents of the horizontal box */
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.rmImages[i]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(rmStateText[i]);
			/* formdata stuff so they take up the right amount of space */
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(20);
			fd.left = new FormAttachment(0);
			b.setLayoutData(fd);
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(100);
			fd.left = new FormAttachment(b);
			l.setLayoutData(fd);
		}
		
		/* the machines box */
		box = new Group(shell, SWT.BORDER);
		box.setText("Machine Icons");
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < machineStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
	        FormLayout layout = new FormLayout();     
	        c.setLayout(layout);
	        /* the contents of the horizontal box */
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.machineImages[i]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(machineStateText[i]);
			/* formdata stuff so they take up the right amount of space */
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(20);
			fd.left = new FormAttachment(0);
			b.setLayoutData(fd);
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(100);
			fd.left = new FormAttachment(b);
			l.setLayoutData(fd);
		}
		
		/* the node box */
		box = new Group(shell, SWT.BORDER);
		box.setText("Node Icons");
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < nodeStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
	        FormLayout layout = new FormLayout();     
	        c.setLayout(layout);
	        /* the contents of the horizontal box */
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.nodeImages[i][0]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(nodeStateText[i]);
			/* formdata stuff so they take up the right amount of space */
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(20);
			fd.left = new FormAttachment(0);
			b.setLayoutData(fd);
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(100);
			fd.left = new FormAttachment(b);
			l.setLayoutData(fd);
		}
		
		/* the job box */
		box = new Group(shell, SWT.BORDER);
		box.setText("Job Icons");
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < jobStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
	        FormLayout layout = new FormLayout();     
	        c.setLayout(layout);
	        /* the contents of the horizontal box */
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.jobImages[i][0]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(jobStateText[i]);
			/* formdata stuff so they take up the right amount of space */
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(20);
			fd.left = new FormAttachment(0);
			b.setLayoutData(fd);
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(100);
			fd.left = new FormAttachment(b);
			l.setLayoutData(fd);
		}
				
		/* the process box */
		box = new Group(shell, SWT.BORDER);
		box.setText("Process Icons");
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);
		
		for (int i = 0; i < processStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
			FormLayout layout = new FormLayout();     
	        c.setLayout(layout);
	        /* the contents of the horizontal box */
	        CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.procImages[i][0]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(processStateText[i]);
			/* formdata stuff so they take up the right amount of space */
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(20);
			fd.left = new FormAttachment(0);
			b.setLayoutData(fd);
			fd = new FormData();
			fd.top = new FormAttachment(0);
			fd.bottom = new FormAttachment(100);
			fd.right = new FormAttachment(100);
			fd.left = new FormAttachment(b);
			l.setLayoutData(fd);
		}
		
        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		close.setLayoutData(data);

        close.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		shell.close();
        	}
        });
                    
        shell.setDefaultButton(close);
	}
}
