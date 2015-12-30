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

public class MeterVis {

	public String text;
	public String color;
	public int tavoite;
	public int painopiste;
	public int realIndex;

	public MeterVis(String text, String color, int tavoite, int painopiste, int realIndex) {
		this.text = text;
		this.color = color;
		this.tavoite = tavoite;
		this.painopiste = painopiste;
		this.realIndex = realIndex;
	}

}
