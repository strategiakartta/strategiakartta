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

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;

public class TagVis {

	public String text;
	public String color;
	public double fillRatio;

	public TagVis(Tag tag, double coverage) {
		int pct = (int)(100.0*coverage);
		text = tag.text;
		if(pct < 100) text += " " + pct;
		color = tag.color;
		fillRatio = coverage;
	}

	public static double computeCoverage(Main main, Tag monitorTag, Base b) {
		Database database = main.getDatabase();
		double value = monitorTag.getCoverage(main, b);
		if(value > 0) {
			return 1.0;
		} else {
			for(Base b2 : Utils.getImplementationSet(database, b)) {
				if(b2.getRelatedTags(database).contains(monitorTag)) {
					return 1.0;
				}
				if(b2 instanceof Painopiste) {
					Tavoite t2 = ((Painopiste)b2).getGoal(database);
					if(t2.getRelatedTags(database).contains(monitorTag)) {
						return 1.0;
					}
				}
			}
		}
		return 0.0;
	}
	
}
