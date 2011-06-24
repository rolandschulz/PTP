/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.TemplateType;
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
public class ViewerUpdateModel extends AbstractUpdateModel implements ICheckStateListener, SelectionListener {
	private final StringBuffer checked;
	private final StringBuffer templatedValue;
	private final String pattern;
	private final String separator;
	private final ICheckable viewer;
	private final ColumnViewer columnViewer;
	private final Map<String, Object> deselected;
	private final boolean initialAllChecked;
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
	 * @param initialAllChecked
	 *            default setting for all is checked; else all unchecked
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
	public ViewerUpdateModel(String name, boolean initialAllChecked, ValueUpdateHandler handler, ICheckable viewer,
			TemplateType template) {
		super(name, handler);
		this.viewer = viewer;
		this.columnViewer = (ColumnViewer) viewer;
		this.initialAllChecked = initialAllChecked;
		pattern = template.getPattern();
		String s = template.getSeparator();
		separator = s == null ? JAXBControlUIConstants.ZEROSTR : s;
		checked = new StringBuffer();
		templatedValue = new StringBuffer();
		viewer.addCheckStateListener(this);
		deselected = new HashMap<String, Object>();
	}

	/*
	 * Model serves as CheckStateListener for the viewer. When check state
	 * changes to checked, the checked values are stored, and the update handler
	 * notified. Unchecked values get removed from the current environment and
	 * placed in a temporary map; when rechecked, the current value in the
	 * deselected map is removed and replaced into the environment. Multiple
	 * rows can be selected, in which case they will all receive the value of
	 * the clicked row. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse
	 * .jface.viewers.CheckStateChangedEvent)
	 */
	@SuppressWarnings("unchecked")
	public void checkStateChanged(CheckStateChangedEvent event) {
		try {
			Object element = event.getElement();
			if (!(element instanceof ICellEditorUpdateModel)) {
				viewer.setChecked(element, false);
				return;
			}
			boolean checked = viewer.getChecked(element);
			IStructuredSelection selection = (IStructuredSelection) ((ColumnViewer) viewer).getSelection();
			List<Object> selected = new ArrayList<Object>();
			selected.add(element);
			selected.addAll(selection.toList());
			if (!selected.isEmpty()) {
				for (Object o : selected) {
					if (o instanceof ICellEditorUpdateModel) {
						ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
						model.setChecked(checked);
						viewer.setChecked(model, checked);
						String name = model.getName();
						if (!checked) {
							deselected.put(name, lcMap.remove(name));
						} else if (lcMap.get(name) == null) {
							lcMap.put(name, deselected.remove(name));
							model.refreshValueFromMap();
						}
					} else {
						viewer.setChecked(o, false);
					}
				}
			}
		} catch (Throwable t) {
			JAXBControlUIPlugin.log(t);
		}
		storeValue();
		handleUpdate(null);
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
		Map<String, String> allChecked = lcMap.getChecked(name);
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			boolean checked = false;
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				if (allChecked.isEmpty()) {
					checked = initialAllChecked;
				} else {
					checked = allChecked.containsKey(model.getName());
				}
				model.setChecked(checked);
				viewer.setChecked(model, checked);
			} else {
				viewer.setChecked(o, false);
			}
		}

		Boolean b = (Boolean) lcMap.get(JAXBControlUIConstants.SHOW_ONLY_CHECKED + name);
		if (b == null) {
			b = false;
		}
		showOnlySelected.setSelection(b);
		if (b) {
			columnViewer.addFilter(filter);
		}
		storeValue();
		handleUpdate(null);
	}

	/*
	 * Store the checked string and the show-only-checked setting (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@SuppressWarnings("unchecked")
	public void putCheckedSettings(Map<String, Object> localMap) {
		checked.setLength(0);
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				if (model.isChecked()) {
					checked.append(model.getName()).append(JAXBControlUIConstants.SP);
				}
			}
		}
		localMap.put(JAXBControlUIConstants.CHECKED_ATTRIBUTES + name, checked.toString().trim());
		localMap.put(JAXBControlUIConstants.SHOW_ONLY_CHECKED + name, showOnlySelected.getSelection());
	}

	/*
	 * NOP for this type; easier to keep in sync at the storeValue
	 * call.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		// does nothing
	}

	/**
	 * @param showOnlySelected
	 *            whether to apply filter to viewer
	 */
	public void setShowAll(Button showOnlySelected) {
		this.showOnlySelected = showOnlySelected;
	}

	/*
	 * Store the template setting (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#storeValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object storeValue() {
		templatedValue.setLength(0);
		Collection<Object> input = (Collection<Object>) ((Viewer) viewer).getInput();
		for (Object o : input) {
			if (o instanceof ICellEditorUpdateModel) {
				ICellEditorUpdateModel model = (ICellEditorUpdateModel) o;
				/*
				 * model will return ZEROSTR if the entry is not selected
				 */
				String replaced = model.getReplacedValue(pattern);
				if (!JAXBControlUIConstants.ZEROSTR.equals(replaced)) {
					templatedValue.append(separator).append(replaced);
				}
			}
		}
		templatedValue.delete(0, separator.length());
		String t = templatedValue.toString().trim();
		if (!JAXBControlUIConstants.ZEROSTR.equals(t)) {
			t = lcMap.getString(t);
			lcMap.put(name, t);
		} else {
			lcMap.remove(name);
		}
		mapValue = t;
		return t;
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

	/*
	 * Model serves as widget selected listener for the filter button.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if (showOnlySelected.getSelection()) {
			columnViewer.addFilter(filter);
		} else {
			columnViewer.removeFilter(filter);
		}
		storeValue();
		handleUpdate(null);
	}
}
