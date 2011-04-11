/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * Update model for the entire viewer (CheckboxTable or CheckboxTree). <br>
 * <br>
 * The viewer needs to be acted on as a whole on updates when check state
 * changes, and it needs to be refreshed after updates.
 * 
 * @author arossi
 * 
 */
public class ViewerUpdateModel extends AbstractUpdateModel implements ICheckStateListener, IDoubleClickListener, SelectionListener {
	private final StringBuffer checked;
	private final StringBuffer templatedValue;
	private final String pattern;
	private final String separator;
	private final ICheckable viewer;
	private final ColumnViewer columnViewer;
	private Button showOnlySelected;

	/**
	 * Used to filter out unchecked items.
	 */
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

	/**
	 * @param name
	 *            An arbitrary name given to the viewer; the viewer string
	 *            output (produced from the pattern template) will be stored in
	 *            the environment as the value of this name, and thus can be
	 *            referenced by other widget models.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param viewer
	 *            the checkable viewer the object models
	 * @param template
	 *            JAXB data element defining a pattern by which to process the
	 *            name-value pairs associated with the items of the viewer into
	 *            a single output string
	 */
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

	/*
	 * Model serves as CheckStateListener for the viewer. When check state
	 * changes, the checked field of the model is set to the new value, stored,
	 * and the update handler notified. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse
	 * .jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object target = event.getElement();
		boolean checked = viewer.getChecked(target);
		if (target instanceof ICellEditorUpdateModel) {
			ICellEditorUpdateModel model = (ICellEditorUpdateModel) target;
			model.setChecked(checked);
		} else {
			viewer.setChecked(target, false);
		}
		storeValue();
		handleUpdate(null);
	}

	/*
	 * Model serves as DoubleClickListener for the viewer. Multiple rows can be
	 * selected, and their checked state toggled using a double-click. Each
	 * toggle generates a CheckStateChangedEvent passed directly to the {@link
	 * #checkStateChanged(CheckStateChangedEvent)} method.(non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse
	 * .jface.viewers.DoubleClickEvent)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#getControl()
	 */
	@Override
	public Object getControl() {
		return viewer;
	}

	/*
	 * Returns the mapValue in this case, since there is not actually a single
	 * value associated with the viewer. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return mapValue;
	}

	/**
	 * Set up the initial state of the viewer from the initial state of the
	 * items; set the button state controlling the filter from the initial map
	 * value.
	 * 
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public void initializeChecked() throws CoreException {
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			boolean selected = false;
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				selected = model.isChecked();
				viewer.setChecked(model, selected);
			} else {
				viewer.setChecked(o, false);
			}
		}

		Boolean b = (Boolean) lcMap.get(name + SHOW_ONLY_CHECKED);
		if (b == null) {
			b = false;
		}
		showOnlySelected.setSelection(b);
		if (b) {
			columnViewer.addFilter(filter);
		}
	}

	/*
	 * Retrieves the last value of the pattern string.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		mapValue = lcMap.get(name);
	}

	/**
	 * @param showOnlySelected
	 *            whether to apply filter to viewer
	 */
	public void setShowAll(Button showOnlySelected) {
		this.showOnlySelected = showOnlySelected;
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
			lcMap.put(CHECKED_ATTRIBUTES, c);
		} else {
			lcMap.remove(CHECKED_ATTRIBUTES);
		}
	}

	/*
	 * Model serves as widget selected listener for the filter button.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		if (showOnlySelected.getSelection()) {
			columnViewer.addFilter(filter);
		} else {
			columnViewer.removeFilter(filter);
		}
		/*
		 * a memento for between sessions
		 */
		lcMap.put(name + SHOW_ONLY_CHECKED, showOnlySelected.getSelection());
	}
}
