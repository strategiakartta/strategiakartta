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

import java.util.ArrayList;

import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;

@JavaScript(value = {
	"app://VAADIN/js/d3.nocache.js",
	"app://VAADIN/js/map.nocache.js"
})
public class D3 extends AbstractJavaScriptComponent {

	private static final long serialVersionUID = -7783740979205083211L;

	public interface D3Listener {
		void navigate(String kartta);
		void select(double x, double y);
		void selectMeter(int tavoite, int painopiste, int index, String link);
		void drill(int tavoite);
		void navi(double x, double y, int tavoite);
		void navi2(double x, double y, int tavoite, int painopiste);
		void editHeader();
		void editVisio();
		void editTavoite(int index);
		void editPainopiste(int tavoite, int painopiste);
		void removeTavoite(int index);
		void displayInfo(int tavoite, int painopiste);
		void displayMeter(int tavoite, int painopiste);
	}
	
	private ArrayList<D3Listener> listeners = new ArrayList<D3Listener>();
	
	private static Integer asInteger(JsonArray arguments, int index) {
		return Integer.parseInt(arguments.get(index).asString());
	}

	private static Double asDouble(JsonArray arguments, int index) {
		return Double.parseDouble(arguments.get(index).asString());
	}

	public D3() {
		addFunction("navigate", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners) listener.navigate(arguments.get(0).asString());
			}
			
		});
		addFunction("select", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners) listener.select(asDouble(arguments, 0), asDouble(arguments, 1));
			}
			
		});
		addFunction("drill", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.drill(asInteger(arguments, 0));
			}
			
		});
		addFunction("navi", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi(asDouble(arguments, 0), asDouble(arguments, 1), asInteger(arguments, 2));
			}
			
		});
		addFunction("navi2", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi2(asDouble(arguments, 0), asDouble(arguments, 1), asInteger(arguments, 2), asInteger(arguments, 3));
			}
			
		});
		addFunction("editHeader", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editHeader();
			}
			
		});
		addFunction("editVisio", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editVisio();
			}
			
		});
		addFunction("selectMeter", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.selectMeter(asInteger(arguments, 0),asInteger(arguments, 1),asInteger(arguments, 2),arguments.get(3).asString());
			}
			
		});
		addFunction("editTavoite", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editTavoite(asInteger(arguments, 0));
			}
			
		});
		addFunction("removeTavoite", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.removeTavoite(asInteger(arguments, 0));
			}
			
		});
		addFunction("editPainopiste", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editPainopiste(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		addFunction("displayInfo", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.displayInfo(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		addFunction("displayMeter", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8685767075489512422L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.displayMeter(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
	}
	
	public void addListener(D3Listener listener) {
		listeners.add(listener);
	}
	
	@Override
	public D3State getState() {
		return (D3State) super.getState();
	}
	
	public void update(final MapVis model, int width, boolean logged, boolean edit) {
		getState().setModel(model);
		if(model != null) {
			model.width = width-30;
			getState().setLogged(logged);
			getState().setEdit(edit);
		}
	}
	
	public MapVis getModel() {
		return getState().model;
	}

}
