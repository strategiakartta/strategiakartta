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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.TextField;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils.AbstractCommentCallback;

abstract public class Datatype extends Base {

	private static final long serialVersionUID = 3032947469375442364L;

	protected Datatype(Database database, String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static Datatype find(Database database, String id) {
		for(Datatype dt : enumerate(database)) 
			if(id.equals(dt.getId(database)))
				return dt;
		return null;
	}
	
	public static List<Datatype> enumerate(Database database) {
		ArrayList<Datatype> result = new ArrayList<Datatype>();
		for(Base b : database.objects.values()) {
			if(b instanceof Datatype) result.add((Datatype)b);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public static interface ValueChanged {
		
		public void run(Object newValue);
		
	}
	
	public abstract TrafficValuation getDefaultTrafficValuation();
	
	public AbstractField<?> getEditor(final Main main, final Base base, final Indicator indicator) {
		
		Object value = indicator.getValue();
		final String formatted = indicator.getDatatype(main.getDatabase()).format(value);

		final TextField tf = new TextField();
		tf.setValue(formatted);
		
		if(main.canWrite(base)) {
		
			tf.addValidator(new Validator() {
	
				private static final long serialVersionUID = 9043601075831736114L;
	
				@Override
				public void validate(Object value) throws InvalidValueException {
					
					try {
						BigDecimal.valueOf(Double.parseDouble((String)value));
					} catch (NumberFormatException e) {
						throw new InvalidValueException("Arvon tulee olla numero");
					}
					
				}
				
			});
			tf.addValueChangeListener(new ValueChangeListener() {
				
				private static final long serialVersionUID = 3547126051252580446L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
					
					try {
						final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(tf.getValue()));
						indicator.modifyValueWithComment(main, base, number, new AbstractCommentCallback() {
							
							public void canceled() {
								tf.setValue(formatted);
							}
							
						});
					} catch (NumberFormatException e) {
						tf.setComponentError(new UserError("Arvon tulee olla numero"));
					}
					
				}
				
			});
			
		} else {
			
			tf.setReadOnly(true);
			
		}
		
		return tf;

	}
	
	abstract public Object getDefaultValue();
	
	abstract public String format(Object value);
	
}
