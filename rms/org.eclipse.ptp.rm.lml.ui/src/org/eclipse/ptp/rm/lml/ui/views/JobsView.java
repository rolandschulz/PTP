package org.eclipse.ptp.rm.lml.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.providers.LMLListLabelProvider;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class JobsView extends LMLViewPart {

	public final class JobListener implements ILMLListener {

		public void handleEvent(IJobListSortedEvent e) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(ITableColumnChangeEvent e) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(ISelectedObjectChangeEvent event) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(IMarkObjectEvent event) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(IUnmarkObjectEvent event) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(IUnselectedObjectEvent event) {
			// TODO Auto-generated method stub

		}
	}

	public final class ListSelectionListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			int selectedItem = list.getSelectionIndex();
			lmlManager.selectLgui(selectedItem);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			lmlManager.selectLgui(0);
		}

	}

	/**
	 * 
	 */
	public ListViewer viewer;
	private ILguiItem fSelected = null;
	private final ILMLListener jobListener = new JobListener();
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
	private List list = null;
	private final ListSelectionListener listListener = new ListSelectionListener();

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ListViewer(parent, SWT.SINGLE);
		viewer.setLabelProvider(new LMLListLabelProvider());
		viewer.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object parent) {
				return null;
			}
		});

		fSelected = lmlManager.getSelectedLguiItem();
		createList();
	}

	private void createList() {
		list = viewer.getList();
		list.removeAll();
		list.removeSelectionListener(listListener);

		if (fSelected != null) {
			for (String lgui : lmlManager.getLguis()) {
				viewer.add(lgui);
			}
			list.setSelection(lmlManager.getSelectedLguiIndex(fSelected.toString()));
		}
		list.addSelectionListener(listListener);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void prepareDispose() {
		// TODO Auto-generated method stub

	}

}
