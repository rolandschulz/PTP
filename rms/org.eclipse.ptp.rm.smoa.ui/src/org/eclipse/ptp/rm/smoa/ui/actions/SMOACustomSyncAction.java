/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.actions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAFileStore;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;
import org.eclipse.ptp.rm.smoa.core.util.NotifyShell;
import org.eclipse.ptp.rm.smoa.ui.SMOAUIPlugin;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SMOACustomSyncAction extends AbstractHandler {

	private class FileFormatException extends IOException {
		private static final long serialVersionUID = -1360379446136195743L;

		public FileFormatException(String s) {
			super(s);
		}
	}

	/** Policy if and when the files should be overwritten */
	enum OverwritePolicy {
		Always, IfNewer, Never
	}

	private static final QualifiedName SMOA_SYNC_OVERWRITE = new QualifiedName(
			SMOAUIPlugin.PLUGIN_ID, "smoa.sync.rm.files.overwrite"); //$NON-NLS-1$

	private static final String SMOA_SYNC_FILELIST_NAME = ".smoa_sync_file_list"; //$NON-NLS-1$

	SMOARemoteServices rservices = (SMOARemoteServices) PTPRemoteCorePlugin
			.getDefault().getRemoteServices("org.eclipse.ptp.remote.SMOARemoteServices"); //$NON-NLS-1$
	// GUI components
	private Tree remoteTree;
	private Tree localTree;

	private Combo policy;
	// Data
	/** Remote file store (rendered in remoteTree) */
	private IFileStore sfs;
	/** Local file store (rendered in localTree) */
	private IFileStore lfs;

	private OverwritePolicy overwrite;

	private IProject project;

	int counter;

	private TreeItem addToTree(Tree toRegenerate, TreeItem treeItem,
			IFileStore newStore) {
		TreeItem parent = null;
		if (treeItem.getParentItem() != null) {
			parent = addToTree(toRegenerate, treeItem.getParentItem(), null);
		}

		TreeItem newItem;
		if (parent == null) {
			newItem = findChild(toRegenerate, treeItem.getText());
			if (newItem == null) {
				newItem = new TreeItem(toRegenerate, SWT.NONE);
				newItem.setText(treeItem.getText());
			}
		} else {
			newItem = findChild(parent, treeItem.getText());
			if (newItem == null) {
				newItem = new TreeItem(parent, SWT.NONE);
				newItem.setText(treeItem.getText());
			}
		}

		if (newStore != null) {
			newItem.setData(newStore);
		}

		return newItem;
	}

	/**
	 * Copies a single tree item to store, and updates toRegenerate tree
	 */
	private void copyTreeItem(TreeItem treeItem, IFileStore store,
			Tree toRegenerate) throws CoreException {
		final IFileStore newStore = store
				.getFileStore(new Path(getRelPath(treeItem)));
		final IFileInfo info = ((IFileStore) treeItem.getData()).fetchInfo();
		if (info.isDirectory()) {
			newStore.mkdir(0, null);
			newStore.putInfo(info, 0, null);
		} else {
			switch (overwrite) {
			case Always:
				break;
			case Never:
				if (newStore.fetchInfo().exists()) {
					return;
				}
				break;
			case IfNewer:
				final IFileInfo newInfo = newStore.fetchInfo();
				if (newInfo.exists()
						&& newInfo.getLastModified() > info.getLastModified()) {
					return;
				}
				break;
			default:
				throw new CoreException(new Status(IStatus.ERROR,
						PTPUIPlugin.PLUGIN_ID, Messages.SMOACustomSyncAction_IncorretOverwritePolicy));
			}
			((IFileStore) treeItem.getData()).copy(newStore, EFS.OVERWRITE,
					null);
		}

		addToTree(toRegenerate, treeItem, newStore);
	}

	@Override
	public void dispose() {
		// saveTrees(project);
		super.dispose();
	}

	/**
	 * Called when user triggers this action from a menu (Described in
	 * plugin.properties)
	 */
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		try {

			project = SelectConnetionAndDestDir.getSelectedProject(execEvent);

			final SelectConnetionAndDestDir dialog = new SelectConnetionAndDestDir(
					project);

			if (!dialog.open()) {
				return null;
			}

			sfs = dialog.getRemoteFileStore();
			if (sfs == null) {
				throw new ExecutionException(Messages.SMOACustomSyncAction_ErrorAccessingRemoteFS);
			}

			final Shell topShell = Display.getCurrent().getActiveShell();
			final Shell shell = new Shell(topShell, new Shell().getStyle()
					| SWT.APPLICATION_MODAL);
			shell.setText(Messages.SMOACustomSyncAction_WindowTitle);
			final GridLayout gridLayout = new GridLayout(2, true);
			shell.setLayout(gridLayout);

			new Label(shell, SWT.NONE).setText(Messages.SMOACustomSyncAction_Local);
			new Label(shell, SWT.NONE).setText(Messages.SMOACustomSyncAction_Remote);

			localTree = new Tree(shell, SWT.CHECK | SWT.VIRTUAL | SWT.MULTI);
			localTree.setLayoutData(new GridData(GridData.FILL_BOTH));

			final Listener treeListener = new Listener() {
				public void handleEvent(Event event) {
					assert event.item instanceof TreeItem;
					final TreeItem me = (TreeItem) event.item;
					if (me.getChecked()) {

						TreeItem parentItem = me.getParentItem();
						while (parentItem != null) {
							parentItem.setChecked(true);
							parentItem = parentItem.getParentItem();
						}
						for (final TreeItem item : me.getItems()) {
							item.setChecked(true);
							final Event e = new Event();
							e.item = item;
							this.handleEvent(e);
						}
					} else {
						for (final TreeItem item : me.getItems()) {
							item.setChecked(false);
							final Event e = new Event();
							e.item = item;
							this.handleEvent(e);
						}
					}
				}
			};

			remoteTree = new Tree(shell, SWT.CHECK | SWT.VIRTUAL | SWT.MULTI);
			remoteTree.setLayoutData(new GridData(GridData.FILL_BOTH));

			final Button copyLocalToRemote = new Button(shell, SWT.PUSH
					| SWT.BORDER);
			copyLocalToRemote.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_END));
			copyLocalToRemote.setText(Messages.SMOACustomSyncAction_FromLocal);

			copyLocalToRemote.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					toRemote();
					// saveTrees(project);
				}
			});

			final Button copyRemoteToLoacal = new Button(shell, SWT.PUSH
					| SWT.BORDER);
			copyRemoteToLoacal.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_BEGINNING));
			copyRemoteToLoacal.setText(Messages.SMOACustomSyncAction_FromRemote);

			copyRemoteToLoacal.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					fromRemote();
					// saveTrees(project);
				}
			});

			final Composite options = new Composite(shell, SWT.NONE);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			options.setLayoutData(gridData);
			options.setLayout(new GridLayout(2, false));
			new Label(options, SWT.NONE).setText(Messages.SMOACustomSyncAction_FileOverwritePolicy);

			policy = new Combo(options, SWT.BORDER | SWT.READ_ONLY);
			for (final OverwritePolicy p : OverwritePolicy.values()) {
				policy.add(p.name());
			}

			final String policyVal = project
					.getPersistentProperty(SMOA_SYNC_OVERWRITE);
			if (policyVal != null) {
				policy.select(policy.indexOf(policyVal));
			} else {
				policy.select(policy.indexOf(OverwritePolicy.Always.name()));
			}

			assert OverwritePolicy.valueOf(policy.getText()) != null;
			overwrite = OverwritePolicy.valueOf(policy.getText());

			policy.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					try {
						assert OverwritePolicy.valueOf(policy.getText()) != null;
						overwrite = OverwritePolicy.valueOf(policy.getText());
						project.setPersistentProperty(SMOA_SYNC_OVERWRITE,
								policy.getText());
					} catch (final CoreException e) {
						final MessageBox mb = new MessageBox(localTree.getShell(),
								SWT.ICON_ERROR | SWT.OK);
						mb.setText(Messages.SMOACustomSyncAction_ErrorOverwritePolicyTitle);
						mb.setMessage(e.getLocalizedMessage());
						mb.open();
					}
				}
			});

			final Shell progress = new Shell(topShell);
			final GridLayout layout = new GridLayout();
			progress.setLayout(layout);
			final Label msg = new Label(progress, SWT.NONE);
			msg.setText(Messages.SMOACustomSyncAction_PleaswWait);
			gridData = new GridData(GridData.FILL_BOTH);
			gridData.minimumWidth = 300;
			msg.setLayoutData(gridData);
			progress.pack();
			progress.open();

			lfs = rootFromProject(localTree, project, msg);
			rootFromFileStore(remoteTree, sfs, msg);

			if (!progress.isDisposed()) {
				progress.close();
			}

			restoreTrees(project);

			localTree.addListener(SWT.Selection, treeListener);
			remoteTree.addListener(SWT.Selection, treeListener);

			shell.open();

			shell.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event event) {
					saveTrees(project);
				}
			});

			return null;
		} catch (final CoreException e) {
			NotifyShell.open(Messages.SMOACustomSyncAction_ExceptionBySynchro, e.getLocalizedMessage());
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
	}

	private TreeItem findChild(Tree parent, String text) {
		for (final TreeItem it : parent.getItems()) {
			if (it.getText().equals(text)) {
				return it;
			}
		}
		return null;
	}

	private TreeItem findChild(TreeItem parent, String text) {
		for (final TreeItem it : parent.getItems()) {
			if (it.getText().equals(text)) {
				return it;
			}
		}
		return null;
	}

	/** Calls synchronise in local ← remote direction */
	protected void fromRemote() {
		synchronise(getCheckedItems(remoteTree), lfs, localTree);
	}

	/**
	 * Returns array with relative paths to the checked items
	 */
	private TreeItem[] getCheckedItems(Tree tree) {
		final Vector<TreeItem> v = new Vector<TreeItem>();
		for (final TreeItem it : tree.getItems()) {
			v.addAll(getCheckedItems(it));
		}
		return v.toArray(new TreeItem[v.size()]);
	}

	/**
	 * Returns all checked items under this item
	 */
	private Vector<TreeItem> getCheckedItems(TreeItem treeItem) {
		final Vector<TreeItem> v = new Vector<TreeItem>();
		for (final TreeItem it : treeItem.getItems()) {
			if (it.getChecked()) {
				v.add(it);
			}
			v.addAll(getCheckedItems(it));
		}
		return v;
	}

	/** Recursively builds up relative path representing the tree item */
	String getRelPath(TreeItem item) {
		if (item.getParentItem() != null) {
			return getRelPath(item.getParentItem()) + "/" + item.getText(); //$NON-NLS-1$
		}
		return "."; //$NON-NLS-1$
	}

	/**
	 * Recursively and eagerly fills the tree with store and it's children
	 */
	private void populate(TreeItem tree, IFileStore store, Label msg)
			throws CoreException {
		counter++;
		final TreeItem treeItem = new TreeItem(tree, SWT.NONE);
		treeItem.setText(store.getName());
		treeItem.setData(store);

		if (msg != null && !msg.isDisposed()) {
			msg.setText(Messages.SMOACustomSyncAction_RegeneratingTree_FoundFiles + counter);
			msg.redraw();
			msg.getShell().redraw();
			msg.getDisplay().update();
		}

		if (store instanceof SMOAFileStore || store.fetchInfo().isDirectory()) {
			for (final IFileStore fs : store.childStores(0, null)) {
				populate(treeItem, fs, msg);
			}
		}
	}

	/**
	 * Reads the state of trees from project and applies to the trees
	 */
	private void restoreTrees(IProject project) {
		try {
			final BufferedReader in = new BufferedReader(new FileReader(project
					.getLocation().toString() + "/" + SMOA_SYNC_FILELIST_NAME)); //$NON-NLS-1$

			if (!"[Local]".equals(in.readLine())) { //$NON-NLS-1$
				throw new FileFormatException(
						"First line does not contain " + "[Local]"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			int c;
			while ((c = in.read()) == '\t') {
				final String line = in.readLine();
				if (line == null) {
					return;
				}
				selectFromPath(localTree, line);
			}

			if (c == -1) {
				return;
			}

			if (c != '[') {
				throw new FileFormatException("Illegal line start (" + c + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			final String rem = in.readLine();
			if (!"Remote]".equals(rem)) { //$NON-NLS-1$
				throw new FileFormatException("Invalid line [" + rem); //$NON-NLS-1$
			}

			while ((c = in.read()) == '\t') {
				final String line = in.readLine();
				if (line == null) {
					return;
				}
				selectFromPath(remoteTree, line);
			}

			if (c == -1) {
				return;
			}

			throw new FileFormatException("Illegal line start (" + c + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		} catch (final FileNotFoundException e) {
			// first run
		} catch (final IOException e) {
			final MessageBox mb = new MessageBox(localTree.getShell(), SWT.ICON_ERROR
					| SWT.OK);
			mb.setText(Messages.SMOACustomSyncAction_ExceptionByReadingSettings);
			mb.setMessage(e.getLocalizedMessage());
			mb.open();
		}
	}

	/**
	 * Starts filling the tree basing on the given store. Updates the message
	 * label with progress.
	 */
	private void rootFromFileStore(Tree tree, IFileStore store, Label msg)
			throws CoreException {

		counter = 0;
		final TreeItem treeItem = new TreeItem(tree, SWT.NONE);
		treeItem.setText(store.getName());
		treeItem.setData(store);
		for (final IFileStore fs : store.childStores(0, null)) {
			populate(treeItem, fs, msg);
		}

	}

	/**
	 * Starts filling the tree basing on the project location. Updates the
	 * message label with progress.
	 */
	private IFileStore rootFromProject(Tree tree, IProject project, Label msg)
			throws CoreException {
		final IFileStore store = EFS.getLocalFileSystem().getStore(
				project.getLocation());

		rootFromFileStore(tree, store, msg);
		return store;
	}

	/**
	 * Records the state of trees in Project
	 */
	protected void saveTrees(IProject project) {

		try {
			final DataOutputStream out = new DataOutputStream(new FileOutputStream(
					project.getLocation().toString() + "/" //$NON-NLS-1$
							+ SMOA_SYNC_FILELIST_NAME));

			out.writeBytes("[Local]\n"); //$NON-NLS-1$

			for (final TreeItem item : getCheckedItems(localTree)) {
				out.write('\t');
				out.writeBytes(getRelPath(item));
				out.write('\n');
			}

			out.writeBytes("[Remote]\n"); //$NON-NLS-1$
			for (final TreeItem item : getCheckedItems(remoteTree)) {
				out.write('\t');
				out.writeBytes(getRelPath(item));
				out.write('\n');
			}

		} catch (final IOException e) {
			final MessageBox mb = new MessageBox(localTree.getShell(), SWT.ICON_ERROR
					| SWT.OK);
			mb.setText(Messages.SMOACustomSyncAction_ExceptionByWritingSettings);
			mb.setMessage(e.getLocalizedMessage());
			mb.open();
		}
	}

	/**
	 * Looks for a TreeItem corresponding to given path in given tree and
	 * selects it.
	 */
	private void selectFromPath(Tree tree, String file) {
		TreeItem root = tree.getItems()[0];
		TreeItem next = root;
		root.setChecked(true);
		final String[] path = new Path(file).segments();
		for (int i = 0; i < path.length; ++i) {
			for (final TreeItem item : root.getItems()) {
				if (item.getText().equals(path[i])) {
					next = item;
				}
			}
			if (next == root) {
				return;
			}
			root = next;
		}
		next.setChecked(true);
	}

	/**
	 * Copies files (given as array of {@link TreeItem}s) to another tree, given
	 * as the tree and it's starting store
	 * 
	 * @param items
	 *            - what to copy
	 * @param store
	 *            - where to copy
	 * @param toRegenerate
	 *            - which tree to update
	 */
	protected void synchronise(TreeItem[] items, IFileStore store,
			Tree toRegenerate) {

		final Shell topShell = Display.getCurrent().getActiveShell();
		final Shell shell = new Shell(topShell, new Shell().getStyle()
				| SWT.APPLICATION_MODAL);
		shell.setText(Messages.SMOACustomSyncAction_ProgressWindowTitle);

		shell.setLayout(new GridLayout(2, false));

		new Label(shell, SWT.NONE).setText(Messages.SMOACustomSyncAction_FilesTotal);
		new Label(shell, SWT.NONE).setText(Integer.toString(items.length));
		new Label(shell, SWT.NONE).setText(Messages.SMOACustomSyncAction_FilesCopied);
		final Label done = new Label(shell, SWT.NONE);
		done.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(shell, SWT.NONE).setText(Messages.SMOACustomSyncAction_CurrentFile);
		final Label current = new Label(shell, SWT.NONE);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumWidth = 500;
		current.setLayoutData(gd);

		int doneVal = -1;

		shell.pack(true);
		shell.open();

		try {
			for (final TreeItem treeItem : items) {
				if (!shell.isDisposed()) {
					done.setText(Integer.toString(++doneVal));
					current.setText(getRelPath(treeItem));

					shell.pack(true);
					done.redraw();
					current.redraw();
					shell.redraw();
					shell.getDisplay().update();
				}
				copyTreeItem(treeItem, store, toRegenerate);
			}

			if (!shell.isDisposed()) {
				done.setText(Integer.toString(++doneVal));
				current.setText(Messages.SMOACustomSyncAction_RegeneratingTree);

				shell.pack(true);
				done.redraw();
				current.redraw();
				shell.redraw();
				shell.getDisplay().update();
			}

		} catch (final CoreException e) {
			final MessageBox mb = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText(Messages.SMOACustomSyncAction_ErrorDialogTitle);
			mb.setMessage(e.getMessage());
			mb.open();
		}

		if (!shell.isDisposed()) {
			shell.close();
		}

	}

	/** Calls synchronise in local → remote direction */
	protected void toRemote() {
		synchronise(getCheckedItems(localTree), sfs, remoteTree);
	}
}
