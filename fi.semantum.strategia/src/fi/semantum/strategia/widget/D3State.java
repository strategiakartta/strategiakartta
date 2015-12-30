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

import com.vaadin.shared.ui.JavaScriptComponentState;

public class D3State extends JavaScriptComponentState {

	private static final long serialVersionUID = 4416227271940056425L;
	
	public MapVis model;
	public boolean logged;

	public void setModel(final MapVis model) {
		this.model = model;
	}

	public void setLogged(boolean value) {
		this.logged = value;
	}

}
