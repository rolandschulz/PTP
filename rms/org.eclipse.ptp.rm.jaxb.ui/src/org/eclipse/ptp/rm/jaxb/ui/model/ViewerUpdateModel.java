package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.rm.jaxb.core.data.Template;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

public class ViewerUpdateModel extends AbstractUpdateModel implements ICheckStateListener, IDoubleClickListener, SelectionListener {
	private final StringBuffer checked;
	private final StringBuffer templatedValue;
	private final String pattern;
	private final String separator;
	private final ICheckable viewer;
	private final ColumnViewer columnViewer;
	private Button showAll;

	private final ViewerFilter filter = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof ICellEditorUpdateModel) {
				return ((ICheckable) viewer).getChecked(element);
			} else if (element instanceof InfoTreeNodeModel) {
				Object parent = ((InfoTreeNodeModel) element).getParent();
				return ((ICheckable) viewer).getChecked(parent);
			}
			return false;
		}
	};

	public ViewerUpdateModel(String name, ValueUpdateHandler handler, ICheckable viewer, Template template) {
		super(name, handler);
		this.viewer = viewer;
		this.columnViewer = (ColumnViewer) viewer;
		pattern = template.getPattern();
		String s = template.getSeparator();
		separator = s == null ? ZEROSTR : s;
		checked = new StringBuffer();
		templatedValue = new StringBuffer();
		viewer.addCheckStateListener(this);
		columnViewer.addDoubleClickListener(this);
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		Object target = event.getElement();
		boolean checked = viewer.getChecked(target);
		if (target instanceof ICellEditorUpdateModel) {
			ICellEditorUpdateModel model = (ICellEditorUpdateModel) target;
			model.setSelected(checked);
		}
		WidgetActionUtils.refreshViewer(columnViewer);
		storeValue();
		handleUpdate(null);
	}

	public void doubleClick(DoubleClickEvent event) {
		List<CheckStateChangedEvent> csEvents = new ArrayList<CheckStateChangedEvent>();
		try {
			IStructuredSelection selection = (IStructuredSelection) ((ColumnViewer) viewer).getSelection();
			List<?> selected = selection.toList();
			if (!selected.isEmpty()) {
				for (Object o : selected) {
					boolean checked = viewer.getChecked(o);
					if (o instanceof ICellEditorUpdateModel) {
						ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
						viewer.setChecked(model, !checked);
					} else {
						viewer.setChecked(o, false);
					}
					csEvents.add(new CheckStateChangedEvent(viewer, o, !checked));
				}
			}
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
		}
		for (CheckStateChangedEvent e : csEvents) {
			checkStateChanged(e);
		}
	}

	@Override
	public Object getControl() {
		return viewer;
	}

	public Object getValueFromControl() {
		return mapValue;
	}

	@SuppressWarnings("unchecked")
	public void initializeSelected() throws CoreException {
		Map<String, String> checked = lcMap.getSelected();
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		boolean selected = false;
		for (Object o : input) {
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				selected = checked.containsKey(model.getName());
				model.setSelected(selected);
				viewer.setChecked(model, selected);
			}
		}

		Boolean b = (Boolean) lcMap.get(name + SHOW_ALL);
		if (b == null) {
			b = true;
		}
		showAll.setSelection(b);
	}

	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		refreshing = false;
	}

	public void setShowAll(Button showAll) {
		this.showAll = showAll;
	}

	/*
	 * store the checked string and the template setting (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void storeValue() {
		templatedValue.setLength(0);
		checked.setLength(0);
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				/*
				 * model will return ZEROSTR if the entry is not selected
				 */
				String replaced = model.getReplacedValue(pattern);
				if (!ZEROSTR.equals(replaced)) {
					templatedValue.append(separator).append(replaced);
					checked.append(model.getName()).append(SP);
				}
			}
		}
		templatedValue.delete(0, separator.length());
		String t = templatedValue.toString().trim();
		if (!ZEROSTR.equals(t)) {
			lcMap.put(name, t);
		} else {
			lcMap.remove(name);
		}
		String c = checked.toString().trim();
		if (!ZEROSTR.equals(c)) {
			lcMap.put(SELECTED_ATTRIBUTES, c);
		} else {
			lcMap.remove(SELECTED_ATTRIBUTES);
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		if (!showAll.getSelection()) {
			columnViewer.addFilter(filter);
		} else {
			columnViewer.removeFilter(filter);
		}
		/*
		 * a memento for between sessions
		 */
		lcMap.put(name + SHOW_ALL, showAll.getSelection());
	}
}
