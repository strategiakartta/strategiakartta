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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.BrowserNodeState;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tag;

public class UIState implements Serializable {

	private static final long serialVersionUID = -5997513543643591132L;
	
	public static final int MAP = 0;
	public static final int BROWSER = 1;
	public static final int WIKI = 2;
	public static final int PROPERTIES = 3;
	
	public String name;
	public String time = "2016";
	public Strategiakartta current;
	public Strategiakartta reference;
	public Base currentItem;
	public Strategiakartta currentPosition;
	public int tabState;
	public boolean showTags = false;
	public List<Tag> shownTags = new ArrayList<Tag>();
	public boolean showMeters = false;
	public boolean showVoimavarat = false;
	public int level = 1;
	public String currentFilterName;
	public Map<String,BrowserNodeState> browserStates = new HashMap<String, BrowserNodeState>();
	
	transient private NodeFilter currentFilter;
	
	public UIState() {
		name = "";
	}

	public NodeFilter getCurrentFilter() {
		return currentFilter;
	}
	
	public Strategiakartta getCurrentMap() {
		return current;
	}
	
	public void setCurrentMap(Strategiakartta map) {
		this.current = map;
	}
	
	
	public void setCurrentFilter(NodeFilter filter) {
		this.currentFilter = filter;
		this.currentFilterName = filter.toString();
	}
	
	public UIState duplicate(Main main) {
		return duplicate("s"+main.stateCounter++);
	}

	public UIState duplicate(String name) {
		UIState result = new UIState();
		result.name = name;
		result.current = current;
		result.reference = reference;
		result.time = time;
		result.currentItem = currentItem;
		result.currentPosition = currentPosition;
		result.tabState = tabState;
		result.level = level;
		result.currentFilterName = currentFilterName;
		result.currentFilter = currentFilter;
		result.showTags = showTags;
		result.shownTags = shownTags;
		result.showMeters = showMeters;
		result.showVoimavarat = showVoimavarat;
		result.browserStates = browserStates;
		return result;
	}
	
}
