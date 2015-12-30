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
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils.Action;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;
import fi.semantum.strategia.widget.Tavoite;

public class MoveUp extends Action {

	private Main main;
	private Strategiakartta map;
	private Tavoite goal;
	
	public MoveUp(Main main, Tavoite goal) {
		super("Siirrä ylemmäs");
		this.main = main;
		this.goal = goal;
		this.map = main.getDatabase().getMap(goal);
	}
	
	@Override
	public void run() {
		map.moveUp(goal);
		Updates.updateJS(main, true);
	}
	
	public boolean accept() {

		Database database = main.getDatabase();
		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if(goal.hasRelatedTag(database, voimavarat)) return false;
		
		int pos = map.findTavoite(goal);
		return pos > 0;

	}

}
