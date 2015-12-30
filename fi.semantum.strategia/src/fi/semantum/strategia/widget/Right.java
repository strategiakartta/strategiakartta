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

import java.io.Serializable;

public class Right implements Serializable {

	private static final long serialVersionUID = 3246509646431571477L;
	
	final public Strategiakartta map;
	final public boolean write;
	final public boolean recurse;
	
	public Right(Strategiakartta map, boolean write, boolean recurse) {
		this.map = map;
		this.write = write;
		this.recurse = recurse;
	}
	
}
