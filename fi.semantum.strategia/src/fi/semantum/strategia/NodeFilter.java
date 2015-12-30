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
package fi.semantum.strategia;

import java.util.Collection;
import java.util.List;

import fi.semantum.strategia.widget.Base;

public interface NodeFilter {

	void accept(List<Base> path, FilterState filterState);
	Collection<Base> traverse(List<Base> path, FilterState filterState);
	VisuSpec acceptNode(List<Base> path, FilterState filterState);
	public void report(List<List<Base>> paths, FilterState state);
	void refresh();
	void reset(FilterState state);
	
}
