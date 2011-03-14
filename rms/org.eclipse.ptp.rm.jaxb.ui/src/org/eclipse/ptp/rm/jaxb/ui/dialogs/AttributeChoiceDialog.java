package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class AttributeChoiceDialog extends Dialog implements IJAXBUINonNLSConstants {
	private class AttributeOrPropertyContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				List<CheckedProperty> cpList = new ArrayList<CheckedProperty>();
				List<?> in = (List<?>) inputElement;
				for (Object o : in) {
					if (o instanceof CheckedProperty) {
						cpList.add((CheckedProperty) o);
					} else {
						cpList.add(new CheckedProperty(o));
					}
				}
				return cpList.toArray();
			}
			return new Object[] { new CheckedProperty(inputElement) };
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class AttributeOrPropertyLabelProvider implements ITableLabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			CheckedProperty p = (CheckedProperty) element;
			switch (columnIndex) {
			case 0:
				return p.name;
			case 1:
				return p.description;
			}
			return ZEROSTR;
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	private class CheckedProperty {
		private String name;
		private String description;
		private boolean visible;
		private boolean checked;

		private CheckedProperty(Object o) {
			if (o instanceof JobAttribute) {
				JobAttribute ja = (JobAttribute) o;
				name = ja.getName();
				visible = ja.isVisible();
				description = ja.getDescription();
				checked = true && visible;
			} else if (o instanceof Property) {
				visible = true;
				name = ((Property) o).getName();
				description = ZEROSTR;
				checked = true;
			} else if (o instanceof String) {
				visible = true;
				name = o.toString();
				description = ZEROSTR;
				checked = true;
			}
		}

		@Override
		public String toString() {
			return name + CM + SP + description + CM + SP + checked;
		}

		private void setChecked(boolean checked) {
			this.checked = (checked && visible);
		}
	}

	private CheckboxTableViewer preferences;
	private final List<CheckedProperty> allProps;
	private boolean toggle;

	public AttributeChoiceDialog(Shell parentShell) {
		super(parentShell);
		this.allProps = new ArrayList<CheckedProperty>();
		toggle = false;
	}

	public void clearChecked() {
		allProps.clear();
	}

	public Map<String, Boolean> getChecked() {
		Map<String, Boolean> map = new TreeMap<String, Boolean>();
		for (CheckedProperty p : allProps) {
			map.put(p.name, p.checked);
		}
		return map;
	}

	public void setCurrentlyVisible(String current) {
		Map<?, ?>[] vars = new Map<?, ?>[] { RMVariableMap.getActiveInstance().getVariables(),
				RMVariableMap.getActiveInstance().getDiscovered() };
		Map<String, String> currentlyVisible = new TreeMap<String, String>();
		if (current != null) {
			String[] selected = current.split(CM);
			for (String s : selected) {
				currentlyVisible.put(s, s);
			}
		}
		for (Map<?, ?> m : vars) {
			for (Object s : m.keySet()) {
				Object o = m.get(s);
				if (o == null) {
					o = s;
				}
				CheckedProperty p = new CheckedProperty(o);
				if (currentlyVisible.containsKey(s)) {
					// override default settings
					p.checked = true;
				}
				allProps.add(p);
			}
		}
	}

	public void toggleCheckboxes() throws Throwable {
		IStructuredSelection selection = (IStructuredSelection) preferences.getSelection();
		List<?> selected = selection.toList();
		toggle = true;
		for (Object o : selected) {
			CheckedProperty p = (CheckedProperty) o;
			boolean checked = p.checked;
			preferences.setChecked(p, !checked);
			p.setChecked(!checked);
		}
		toggle = false;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.ConfigureLaunchSettings);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = WidgetBuilderUtils.createGridLayout(2, true);
		GridData gdfill = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, true, 600, 300, 2);
		Group grp = WidgetBuilderUtils.createGroup(composite, SWT.BORDER, layout, gdfill);
		int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK | SWT.MULTI | SWT.WRAP;
		Table t = WidgetBuilderUtils.createTable(grp, style, 2, 600, gdfill);
		preferences = new CheckboxTableViewer(t);
		WidgetBuilderUtils.addTableColumn(preferences, Messages.AttributeName, SWT.LEFT, null);
		WidgetBuilderUtils.addTableColumn(preferences, Messages.AttributeDescription, SWT.LEFT, null);
		preferences.setContentProvider(new AttributeOrPropertyContentProvider());
		preferences.setLabelProvider(new AttributeOrPropertyLabelProvider());
		preferences.setInput(allProps);
		preferences.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					toggleCheckboxes();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
		preferences.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (toggle) {
					return;
				}
				CheckedProperty p = (CheckedProperty) event.getElement();
				p.setChecked(event.getChecked());
			}
		});
		preferences.getTable().setHeaderVisible(true);
		applyDialogFont(parent);
		initialize();
		return composite;
	}

	private void initialize() {
		for (CheckedProperty p : allProps) {
			preferences.setChecked(p, p.checked);
		}
	}
}
