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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.Main.TimeInterval;

public class TimeConfiguration extends Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = 713732830288790423L;
	
	private String timeRange;
	private Set<Integer> frozen = new HashSet<Integer>();
	
	public String getRange() {
		return timeRange;
	}
	
	public void setRange(String range) {
		timeRange = range;
	}
	
	public boolean isFrozen(int year) {
		return frozen.contains(year);
	}
	
	public void freeze(int year) {
		frozen.add(year);
	}

	public void unfreeze(int year) {
		frozen.remove(year);
	}

	public static TimeConfiguration create(Database database, String timeRange) {
		TimeConfiguration p = new TimeConfiguration(timeRange);
		database.register(p);
		return p;
	}
	
	private TimeConfiguration(String timeRange) {
		super(UUID.randomUUID().toString(), "TimeConfiguration", "TimeConfiguration");
		this.timeRange = timeRange;
	}
	
	public static TimeConfiguration getInstance(Database database) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof TimeConfiguration) {
				return (TimeConfiguration)b;
			}
		}
		return null;

	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public boolean canWrite(Database database, Base b) {
		Property aika = Property.find(database, Property.AIKAVALI);
		String a = aika.getPropertyValue(b);
		if(a == null) return true;
		return canWrite(a);
	}

	public boolean canWrite(String a) {
		if(a == null) return true;
		if(Property.AIKAVALI_KAIKKI.equals(a)) return true;
		TimeInterval ti = TimeInterval.parse(a);
		for(int year : ti.years()) {
			if(isFrozen(year)) return false;
		}
		return true;
	}
	
	@Override
	public boolean migrate(Main main) {
		if(frozen == null) {
			frozen = new HashSet<Integer>();
			super.migrate(main);
			return true;
		}
		return super.migrate(main);
	}
	
}
