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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.widget.Base;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategia.widget.BrowserNodeState;
import fi.semantum.strategia.widget.Database;
import fi.semantum.strategia.widget.Meter;
import fi.semantum.strategia.widget.Painopiste;
import fi.semantum.strategia.widget.Strategiakartta;
import fi.semantum.strategia.widget.Tavoite;

public class FilterState {

	public static class ReportCell {
		
		public static ReportCell EMPTY = new ReportCell("-", "-");
		
		String id;
		String caption;
		
		public ReportCell(String id, String caption) {
			this.id = id;
			this.caption = caption;
		}
		
		public ReportCell(String text) {
			this(text, text);
		}
		
	}
	
	private Map<Base, Integer> indexMap = new HashMap<Base, Integer>();

	List<BrowserNode> nodes = new ArrayList<BrowserNode>();
	List<BrowserLink> links = new ArrayList<BrowserLink>();

	public LinkedList<LinkSpec> linkSpecs = new LinkedList<LinkSpec>();
	public Collection<Map<String,ReportCell>> report = new ArrayList<Map<String,ReportCell>>();
	public List<String> reportColumns = new ArrayList<String>();
	private Set<List<Base>> acceptSet = new HashSet<List<Base>>();
	private List<List<Base>> accepted = new ArrayList<List<Base>>();
	
	final private int windowWidth;
	final private int windowHeight;
	
	final private Main main;

	public FilterState(Main main, int windowWidth, int windowHeight) {
		this.main = main;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}
	
	public int countAccepted() {
		return accepted.size();
	}
	
	public void accept(NodeFilter filter, List<Base> path) {
		if(acceptSet.add(path)) {
			accepted.add(new ArrayList<Base>(path));
		}
	}
	
	public boolean isPrefix(List<Base> path) {
		int pathLen = path.size();
		loop: for(List<Base> p : accepted) {
			if(p.size() > pathLen) {
				for(int i=0;i<pathLen;i++) {
					if(!path.get(i).equals(p.get(i))) continue loop;
				}
				return true;
			}
		}
		return false;
	}
	
	public Collection<List<Base>> getAcceptedNodes() {
		return accepted;
	}
	
	BrowserNode addNode(Base b) {

		if(indexMap.containsKey(b)) {
			return nodes.get(indexMap.get(b));
		}
		
		BrowserNode node = makeNode(b);
		if(node != null) {
			indexMap.put(b, nodes.size());
			nodes.add(node);
		}

		// Default parents
		Base parent = main.getDatabase().getDefaultParent(b);
		if(parent != null) {
			BrowserNode parentNode = addNode(parent);
			if(node != null)
				parentNode.addChild(b);
			addLink(b, parent, true);
		}
		
		return node;

	}
	
	private Point2D randomPos(int x, int y) {
		return new Point2D.Double(x + (Math.random() - 0.5) * 100, y
				+ (Math.random() - 0.5) * 100);
	}

	public void linkIfAbsent(Base source, Base target, String color, String dash,
			double weight, double linkDistance, double linkStrength) {
		Integer sId = indexMap.get(source);
		if (sId == null)
			return;
		Integer tId = indexMap.get(target);
		if (tId == null) {
			List<Base> path = main.getDatabase().getDefaultPath(target);
			if(path == null) return;
			addPath(path);
			tId = indexMap.get(target);
			if (tId == null) return;
		}
		for(BrowserLink l : links)
			if((sId == l.source && tId == l.target) || (sId == l.target && tId == l.source))
				return;
		links.add(new BrowserLink(color, dash, sId, tId, weight, linkDistance, linkStrength));
	}
	
	private Map<Integer,List<BrowserNode>> maps = new HashMap<Integer,List<BrowserNode>>();
	
	private int computeSize(Base b) {
		if(b instanceof Strategiakartta) {
			return 500;
		} else if(b instanceof Tavoite) {
			return 325;
		} else if(b instanceof Painopiste) {
			return 325;
		} else if(b instanceof Meter) {
			return 325;
		} else {
			return 325;
		}
	}

	private int computeSize(VisuSpec spec, Base b) {
		
		double pathFactor = (8.0 / (spec.pathLength+5.0));
		pathFactor = Math.max(Math.min(pathFactor, 1.0), 0.2);
		int objectSize = computeSize(b);
		return (int)(pathFactor*objectSize);
		
	}
	
	BrowserNode makeNode(Base b) {
		
		final Database database = main.getDatabase();

		UIState state = main.getUIState();
		VisuSpec spec = main.getUIState().getCurrentFilter().acceptNode(Collections.singletonList(b), this);
		Point2D random = randomPos(windowWidth / 2, windowHeight / 2);
		Strategiakartta containerMap = database.getMap(b);
		if(state.browserStates == null) state.browserStates = new HashMap<String, BrowserNodeState>();
		BrowserNodeState ns = state.browserStates.get(b.uuid); 
		
		int size = computeSize(spec, b);
		int height = (int)(0.4*size);
		int nameSize = (int)(0.3*height);
		int descriptionSize = (int)(0.2*height);
		int repel = -30*size;
		
		if(b instanceof Strategiakartta) {
			Strategiakartta map = (Strategiakartta)b;
			int level = map.getLevel(database);
			BrowserNode node = new BrowserNode(map.getId(database), map.uuid, ns, true, map.characterColor, nameSize, descriptionSize, 0, 0,
					size, height, repel, spec);
			List<BrowserNode> list = maps.get(level);
			if(list == null) {
				list = new ArrayList<BrowserNode>();
				maps.put(level, list);
			}
			list.add(node);
			return node;
		} else if(b instanceof Tavoite) {
			Tavoite goal = (Tavoite)b;
			return new BrowserNode(goal.getId(database), goal.uuid, ns, false, containerMap.tavoiteColor, nameSize, descriptionSize,
					(int) random.getX(), (int) random.getY(), size, height, repel, spec);
		} else if(b instanceof Painopiste) {
			Painopiste focus = (Painopiste)b;
			return new BrowserNode(focus.getId(database), focus.uuid, ns, false, containerMap.painopisteColor, nameSize, descriptionSize,
			(int) random.getX(), (int) random.getY(), size, height, repel, spec);
		} else if(b instanceof Meter) {
			Meter m = (Meter)b;
			return new BrowserNode(m.getCaption(database), m.uuid, ns, false, "#222", nameSize, descriptionSize,
			(int) random.getX(), (int) random.getY(), size, height, repel, spec);
		}
		
		return null;
		
	}
	
	void addLink(Base b, Base parent, boolean useDefaultColoring) {
		
		Strategiakartta containerMap = main.getDatabase().getMap(b);
		
		if(!useDefaultColoring) {
			linkIfAbsent(parent, b, "#777", "15, 15", 20, 200, 0.5);
			return;
		}
		
		if(b instanceof Strategiakartta) {
			Strategiakartta map = (Strategiakartta)b;
			linkIfAbsent(parent, b, map.characterColor, "35, 15", 60, 1750, 0.2);
		} else if(b instanceof Tavoite) {
			linkIfAbsent(parent, b, containerMap.tavoiteColor, "45, 15", 80, 550, 0.2);
		} else if(b instanceof Painopiste) {
			linkIfAbsent(parent, b, containerMap.painopisteColor, "55, 15", 80, 200, 0.2);
		} else {
			linkIfAbsent(parent, b, "#777", "15, 15", 20, 200, 0.2);
		}
		
	}

	void addPath(List<Base> path) {
		for(Base b : path) {
			addNode(b);
		}
		for(int i=1;i<path.size();i++) {
			addLink(path.get(i), path.get(i-1), false);
			BrowserNode n = addNode(path.get(i-1));
			n.addChild(path.get(i));
		}
	}
	
	void process(NodeFilter filter, boolean setPositions) {
		
		filter.report(accepted, this);
		
		// Create nodes and links
		for(List<Base> path : new ArrayList<List<Base>>(accepted)) {
			addPath(path);
		}
		
		for(LinkSpec spec : linkSpecs) {
			linkIfAbsent(spec.source, spec.target, spec.stroke, "15, 15", 15, 500, 0.1);
		}
		
		for(int level : maps.keySet()) {
			
			List<BrowserNode> ns = maps.get(level);
			Collections.sort(ns, new Comparator<BrowserNode>() {

				@Override
				public int compare(BrowserNode arg0, BrowserNode arg1) {
					return arg0.uuid.compareTo(arg1.uuid);
				}
				
			});
			
			if(!setPositions) {
				int span = 1500 * level;
				for(int i=0;i<ns.size();i++) {
					BrowserNode node = ns.get(i);
					node.x = windowWidth/3 + 2*span;
					node.y = windowHeight/2 + (int)((Math.random() - 0.5) * span);
				}
			}
			
		}
		
	}

}
