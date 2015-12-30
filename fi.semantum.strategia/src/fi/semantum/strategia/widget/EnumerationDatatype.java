/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia.widget;

import java.util.List;
import java.util.TreeMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils.AbstractCommentCallback;

public class EnumerationDatatype extends Datatype {

	private static final long serialVersionUID = -9212610888611727972L;
	
	private List<String> enumeration;
	private EnumeratedTrafficValuation defaultValuation;

	protected EnumerationDatatype(Database database, String id, List<String> enumeration, String traffic) {
		super(database, id, id);
		this.enumeration = enumeration;
		TreeMap<Object,String> values = new TreeMap<Object,String>();
		assert(enumeration.size() == traffic.length());
		for(int i=0;i<enumeration.size();i++) {
			String value = enumeration.get(i);
			char c = traffic.charAt(i);
			if(c == 'p') values.put(value, TrafficValuation.RED);
			else if(c == 'k') values.put(value, TrafficValuation.YELLOW);
			else if(c == 'v') values.put(value, TrafficValuation.GREEN);
			else throw new IllegalArgumentException("traffic=" + traffic);
		}
		defaultValuation = new EnumeratedTrafficValuation(values);
	}
	
	public List<String> getValues() {
		return enumeration;
	}
	
	public void replace(EnumerationDatatype other) {
		this.enumeration = other.enumeration;
		this.defaultValuation = other.defaultValuation;
	}
	
	@Override
	public AbstractField<?> getEditor(final Main main, final Base base, final Indicator indicator) {

		final Object value = indicator.getValue();
		
		final ComboBox combo = new ComboBox();
		for(String s : enumeration) {
			combo.addItem(s);
		}
		
		combo.select(value);
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");

		if(main.canWrite(base)) {
			combo.addValueChangeListener(new ValueChangeListener() {
				
				private static final long serialVersionUID = 3547126051252580446L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
					indicator.modifyValueWithComment(main, base, combo.getValue(), new AbstractCommentCallback() {
						
						@Override
						public void canceled() {
							combo.select(value);
						}
						
					});
				}
				
			});
		} else {
			combo.setReadOnly(true);
		}
		
		return combo;

	}

	@Override
	public TrafficValuation getDefaultTrafficValuation() {
		return defaultValuation;
	}
	
	@Override
	public Object getDefaultValue() {
		return enumeration.get(0);
	}
	
	@Override
	public String format(Object value) {
		if(value == null) return "<arvoa ei ole asetettu>";
		return value.toString();
	}
	
	
}
