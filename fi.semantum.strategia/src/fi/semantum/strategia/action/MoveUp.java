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
package fi.semantum.strategia.action;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Moveable;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;
import fi.semantum.strategia.widget.Tavoite;

public class MoveUp extends ActionBase<Moveable> {
	
	public MoveUp(Main main, Moveable m) {
		super("Siirrä ylemmäs", main, m);
	}
	
	@Override
	public void run() {
		base.moveUp(main);
	}
	
	public boolean accept(Tavoite goal) {

		Database database = main.getDatabase();
		Strategiakartta map = database.getMap(goal);
		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if(goal.hasRelatedTag(database, voimavarat)) return false;
		
		int pos = map.findTavoite(goal);
		return pos > 0;

	}

}
