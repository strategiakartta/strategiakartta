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

var painopisteColumns = 2;

var a4ratio = 0.4;
var mainMargin = a4ratio*37.0;
var chartWidth = a4ratio*1920-2*mainMargin;
var chartHeight = a4ratio*1500;
var textPad = a4ratio*6;
var boxPad = a4ratio*10;
var visioX = a4ratio*300;
var visioWidth = chartWidth - 2*visioX;
var visioHeight = a4ratio*100;
var tavoiteX = a4ratio*20 
var tavoiteSpacing = a4ratio*10;
var tavoiteWidth = 0.5* (chartWidth - 2*tavoiteX - tavoiteSpacing);
var painopisteMeterHeight = a4ratio*30;
var painopisteTagHeight = a4ratio*20;
var tavoiteMeterHeight = a4ratio*35;
var tavoiteTagHeight = a4ratio*25;
var tavoiteHeight = a4ratio*265 + painopisteMeterHeight;
var visioBaseY = a4ratio*70;
var tavoiteBaseline = visioBaseY + visioHeight + a4ratio*30;
var alikarttaBaseline = tavoiteBaseline + a4ratio*35;
var painopisteWidth = (tavoiteWidth-(painopisteColumns+1)*boxPad)/painopisteColumns;
var painopisteHeight = a4ratio*120 + painopisteMeterHeight;
var painopisteY = tavoiteHeight - painopisteHeight - boxPad;
var meterHeight = a4ratio*50;

fi_semantum_strategia_widget_D3 = function() {	

	var self = this;

	create(this);
	
	this.onStateChange = function() {
		refresh(self);
	};

	this.onStateChange();

};

function create(rootFn) {

	var thisElement = rootFn.getElement();

	var root = d3.select(thisElement);
	
	var svg = root.append("svg")
	.on("click", function(d) { 
		rootFn.select(d3.event.pageX, d3.event.pageY); 
	})
	.attr("id", "map");

	var fillPattern = svg.append("defs").append("pattern")
	.attr("id", "tavoitePattern")
	.attr("patternUnits", "userSpaceOnUse")
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*50)
	.attr("patternTransform", "rotate(65)");
	var fillPatternRectangle1 = fillPattern.append("rect")
	.attr("id", "fillRect1")
	.attr("x", 0)
	.attr("y", 0)
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*26)
	.style("stroke-width", 0)
	.style("fill", "green");
	var fillPatternRectangle2 = fillPattern.append("rect")
	.attr("id", "fillRect2")
	.attr("x", 0)
	.attr("y", a4ratio*25)
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*25)
	.style("stroke-width", 0)
	.style("fill", "red");
	
	var scaleGroup = svg.append("g").classed("scalegroup", true);

	var group = scaleGroup.append("g").classed("maingroup", true);

	var naviGroup = scaleGroup.append("g").classed("navigroup", true);

	group.append('text').classed("header edit", true);

	group.append('rect').classed("visio", true)
	.attr("width", visioWidth)
	.attr("height", visioHeight)
	.attr("transform", "translate(" + visioX + "," + visioBaseY + ")")
	.attr("rx", "2")
	.attr("ry", "2");

	group.append('rect').classed("goalLegend", true)
	.attr("width", a4ratio*32)
	.attr("height", a4ratio*32)
	.attr("transform", "translate(" + (visioX+visioWidth+a4ratio*16) + "," + (visioBaseY+a4ratio*8) + ")")
	.attr("rx", "2")
	.attr("ry", "2");

	group.append('text').classed("goalLegend", true)
	.attr("transform", "translate(" + (visioX+visioWidth+a4ratio*55) + "," + (visioBaseY+a4ratio*28) + ")")
	.style("font-size", "7px")
	.text("Goal");

	group.append('rect').classed("focusLegend", true)
	.attr("width", a4ratio*32)
	.attr("height", a4ratio*32)
	.attr("transform", "translate(" + (visioX+visioWidth+a4ratio*16) + "," + (visioBaseY+a4ratio*48) + ")")
	.attr("rx", "2")
	.attr("ry", "2");

	group.append('text').classed("focusLegend", true)
	.attr("transform", "translate(" + (visioX+visioWidth+a4ratio*55) + "," + (visioBaseY+a4ratio*68) + ")")
	.style("font-size", "7px")
	.text("Focus");

	group.append('text').classed("visio edit", true)
	.attr("transform", "translate(" + (visioX + 0.5*visioWidth) + "," + (visioBaseY+a4ratio*40) + ")");

	naviGroup.append("text").classed("alas", true)
	.attr("y", "20")
	.text("Aliorganisaation strategiamääritykset:");

}

function shadeColor(color, percent) {

    var R = parseInt(color.substring(1,3),16);
    var G = parseInt(color.substring(3,5),16);
    var B = parseInt(color.substring(5,7),16);

    R = parseInt(R * (100 + percent) / 100);
    G = parseInt(G * (100 + percent) / 100);
    B = parseInt(B * (100 + percent) / 100);

    R = (R<255)?R:255;  
    G = (G<255)?G:255;  
    B = (B<255)?B:255;  

    var RR = ((R.toString(16).length==1)?"0"+R.toString(16):R.toString(16));
    var GG = ((G.toString(16).length==1)?"0"+G.toString(16):G.toString(16));
    var BB = ((B.toString(16).length==1)?"0"+B.toString(16):B.toString(16));

    return "#"+RR+GG+BB;
}

function refresh(rootFn) {

	var thisElement = rootFn.getElement();
	var state = rootFn.getState();
	var strategiaKartta = state.model;
	var root = d3.select(thisElement).datum(strategiaKartta);

	var ratio = strategiaKartta.width / chartWidth;

	root.select("#fillRect1").style("fill", strategiaKartta.tavoiteColor);
	root.select("#fillRect2").style("fill", shadeColor(strategiaKartta.tavoiteColor, 10));
	
	var svg = root.select("svg");
	
	svg.attr("width", chartWidth*ratio);

	var scaleGroup = root.select("g.scalegroup")
	.attr("transform", function(d) { return "scale(" + ratio + ")"; });

	var mainGroup = root.select("g.maingroup");

	mainGroup.select("text.uusiTavoite")
	.attr("visibility", function(d) { return state.logged ? "visible" : "hidden"; });
	
	mainGroup.select("text.header")
	.attr("transform", function(d) { return "translate(" + 0.5*chartWidth + "," + (a4ratio*45) + ")"; });

	mainGroup.select("text.header")
	.on("click", function(d) { 
		if(state.logged) {
			d3.event.stopPropagation();
			rootFn.editHeader();
		}
	})
	.style("cursor", function(d) { return state.logged ? "text" : "default"; })
	.on("mouseover", function(d,i) { if(state.logged && !d.copy) d3.select(this).attr("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).attr("fill", "#000000"); })
	.attr("fill", "#000000")
	.text(function(d) { return d.text.length > 0 ? d.text : "<ei nimeä>"; });
	
	mainGroup.select("text.visio.edit")
	.text(function(d) { return d.visio.length > 0 ? d.visio : "<ei visiota>"; })
	.style("cursor", function(d) { return state.logged ? "text" : "default"; })
	.on("click", function(d) { 
		if(state.logged)  {
			d3.event.stopPropagation();
			rootFn.editVisio();
		}
	})
	.on("mouseover", function(d,i) { if(state.logged && !d.copy) d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", "#fff"); })
	.attr("fill", "#000000")
	.call(wrapVisio, visioWidth-2*textPad);

	mainGroup.select("text.goalLegend")
	.text(function(d) { return strategiaKartta.tavoiteDescription; });

	mainGroup.select("rect.goalLegend")
	.style("fill", function(d) { return strategiaKartta.tavoiteColor; });

	mainGroup.select("text.focusLegend")
	.text(function(d) { return strategiaKartta.painopisteDescription; });

	mainGroup.select("rect.focusLegend")
	.style("fill", function(d) { return strategiaKartta.painopisteColor; });
	
	tavoitteet(rootFn, mainGroup, strategiaKartta);

	// layout after text wrapping
	var pps = thisElement.querySelectorAll('g.painopiste');
	for (var i=0;i<pps.length;i++) {
		
		var pp = pps[i];
		
		var ppRect = pp.childNodes[0];
		var content = pp.childNodes[3];
		var text = content.childNodes[1];

		content.setAttribute('transform', 'translate(0,0)');

		var tags = pp.querySelectorAll('g.painopisteTag');
		var meters = pp.querySelectorAll('g.painopisteMeter');
		
		var tagHeight = ((tags.length > 0) ? (painopisteTagHeight+boxPad) : 0.0);
		var meterHeight = ((meters.length > 0) ? (painopisteMeterHeight+boxPad) : 0.0);

		var lines = parseInt(text.getAttribute('lineCount'),10);
		var contentHeight = (20 + lines  * 9.0);
		
		var currentY = 0;
		var currentX = boxPad;
		for (var j=0;j<tags.length;j++) {
			
			var g = tags[j];
			var fillRect = g.childNodes[0];
			var rect = g.childNodes[1];
			var text = g.childNodes[2];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));
			fillRect.setAttribute('width', '' + g.__data__.fillRatio*(wordLength+4));

			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (painopisteWidth-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += painopisteTagHeight+boxPad;
				tagHeight += painopisteTagHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}
		
		currentY = 0;
		currentX = boxPad;
		
		for (var j=0;j<meters.length;j++) {
			
			var g = meters[j];
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (painopisteWidth-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += painopisteMeterHeight+boxPad;
				meterHeight += painopisteMeterHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;
			
		}

		// Font size is 9px
		var pxHeight = contentHeight + meterHeight + tagHeight;
		
		ppRect.setAttribute('height', pxHeight);

		var tagsG = pp.querySelectorAll('g.painopisteTags');
		tagsG[0].setAttribute('transform', 'translate(0, ' + (pxHeight-meterHeight-tagHeight) + ')');
		var meterG = pp.querySelectorAll('g.painopisteMeters');
		meterG[0].setAttribute('transform', 'translate(0, ' + (pxHeight-meterHeight) + ')');
		
	}

	var ts = thisElement.querySelectorAll('g.tavoite');
	for (var i=0;i<ts.length;i++) {
		
		var t = ts[i];
		
		var text = t.childNodes[2];
		
		var tags = t.querySelectorAll('g.tavoiteTag');
		var meters = t.querySelectorAll('g.tavoiteMeter');
		
		var tagHeight = ((tags.length > 0) ? (tavoiteTagHeight+boxPad) : 0.0);
		var meterHeight = ((meters.length > 0) ? (tavoiteMeterHeight+boxPad) : 0.0);

		var lines = parseInt(text.getAttribute('lineCount'),10);
		var contentHeight = (25 + lines  * 11.0);
		
		var currentY = contentHeight;
		var currentX = boxPad;
		for (var j=0;j<tags.length;j++) {
			
			var g = tags[j];
			var fillRect = g.childNodes[0];
			var rect = g.childNodes[1];
			var text = g.childNodes[2];
			var wordLength = text.getComputedTextLength();
			
			fillRect.setAttribute('width', '' + g.__data__.fillRatio*(wordLength+4));
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (tavoiteWidth-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += tavoiteTagHeight+boxPad;
				tagHeight += tavoiteTagHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}
		
		currentY = 0;
		currentX = boxPad;
		for (var j=0;j<meters.length;j++) {
			
			var g = meters[j];
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (tavoiteWidth-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += tavoiteMeterHeight+boxPad;
				meterHeight += tavoiteMeterHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}

		
		var ps = t.querySelectorAll('g.painopiste');
		var currentY = contentHeight + tagHeight;
		var rowHeight = 0.0;
		for (var j=0;j<ps.length;j++) {
			var g = ps[j];
			var rect = g.childNodes[0];
			var height = parseInt(rect.getAttribute('height'),10);
			if(height > rowHeight) rowHeight = height;
			g.setAttribute('transform', 'translate(' + (boxPad + (boxPad + painopisteWidth)*(j&1)) + ',' + currentY + ')');
			if((j&1) == 1) {
				currentY += rowHeight + boxPad;
				rowHeight = 0.0;
			}
		}

		currentY += meterHeight;

		var trect = t.childNodes[0];
		var h = currentY+rowHeight+boxPad;
		trect.setAttribute('height', h);
		
		var meterG = t.querySelectorAll('g.tavoiteMeters');
		meterG[0].setAttribute('transform', 'translate(0, ' + (h-meterHeight) + ')');
		
	}

	var maxY = 0;
	var currentY = tavoiteBaseline;
	var rowHeight = 0.0;
	var rowStart = 0;
	for (var i=0;i<ts.length;i++) {
		
		var g = ts[i];
		var rect = g.childNodes[0];
		
		if(g.__data__.startNewRow) {
			currentY += rowHeight + boxPad;
			rowHeight = 0.0;
			rowStart = i;
		}
		
		var height = parseInt(rect.getAttribute('height'),10);
		if(height > rowHeight) rowHeight = height;
		
		var xOffset = tavoiteWidth*g.__data__.xOffset;
		var yOffset = g.__data__.yOffset;
		rowHeight += yOffset;

		g.setAttribute('transform', 'translate(' + (tavoiteX+xOffset+(tavoiteWidth+tavoiteSpacing)*(i-rowStart)) + ',' + (currentY+yOffset) + ')');
		
		if((currentY+yOffset+height) > maxY) maxY = (currentY+yOffset+height); 
		
	}

	var naviGroup = root.select("g.navigroup")
	.attr("transform", function(d) { return "translate(0," + (currentY+rowHeight) + ")"; });

	naviGroup.select("text.alas")
	.attr("transform", function(d) { return "translate(" + 0.5*chartWidth + ",0)"; });

	naviGroup.select("text.uusiAliorganisaatio")
	.attr("visibility", function(d) { return state.logged ? "visible" : "hidden"; })
	.attr("transform", function(d) { return "translate(" + (a4ratio*25) + ",0)"; });

	var naviHeight = navigointiLinkit(rootFn, naviGroup, strategiaKartta);
	
	var parents = mainGroup.selectAll("text.parents")
	.data(function(d) { 
		return d.parents; 
		});
	
	creates = parents.enter().append("text").classed("parents link", true)
	.attr("transform", "translate(20,42)");
	
	parents.on("click", function(d,i) { 
		d3.event.stopPropagation();
		rootFn.navigate(d.uuid); 
	});
	parents.text(function(d,i) { return strategiaKartta.showNavigation ? "Ylös: " + d.text : ""; });

	parents.exit().remove();
	
	if(strategiaKartta.showNavigation) {
		svg.attr("height", (maxY+naviHeight)*ratio);
	} else {
		svg.attr("height", maxY*ratio);
	}
	
}

function tavoitteet(rootFn, svgs, map) {

	var state = rootFn.getState();

	var tavoitteet = svgs.selectAll("g.tavoite")
	.data(function(d) { return d.tavoitteet; });
	
	creates = tavoitteet.enter().append("g").classed("tavoite", true);

	creates.append("rect").classed("tavoite", true)
	.attr("width", "" + tavoiteWidth+"px")
	.attr("rx", "2")
	.attr("ry", "2")
	.style("cursor", function(d) { return "pointer"; })
	.on("click", function(d,i) { 
		d3.event.stopPropagation();
		rootFn.navi(d3.event.pageX, d3.event.pageY, d.realIndex); 
	});
	
	creates.append("text").classed("tavoite idElement", true)
	.attr("transform", "translate(" + (0.5*tavoiteWidth) + ", " + (a4ratio*26) + ")");

	creates.append("text").classed("tavoite textElement", true)
	.attr("transform", "translate(" + (0.5*tavoiteWidth) + ", " + (a4ratio*60) + ")");
	
	creates.append("g").classed("tavoiteMeters", true);
	creates.append("g").classed("tavoiteTags", true);

	svgs.selectAll("text.tavoite.idElement")
	.data(function(d) { return d.tavoitteet; })
	.style("cursor", function(d) { return (state.logged && !d.copy) ? "text" : "default"; })
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.on("click", function(d,i) { 
		if(state.logged && !d.copy) {
			d3.event.stopPropagation();
			rootFn.editTavoite(d.realIndex); 
		}
	}
	)
	.text(function(d) { return d.id; });

	svgs.selectAll("text.tavoite.textElement")
	.data(function(d) { return d.tavoitteet; })
	.style("cursor", function(d) { return (state.logged && !d.copy) ? "text" : "default"; })
	.text(function(d) { return d.text.length > 0 ? d.text : "<ei nimeä>"; })
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.on("click", function(d,i) { 
		if(state.logged && !d.copy) { 
			d3.event.stopPropagation();
			rootFn.editTavoite(d.realIndex); 
		}
	})
	.on("mouseover", function(d,i) { if(state.logged && !d.copy) d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", map.tavoiteTextColor); })
	.call(wrapFocus, tavoiteWidth-2*textPad);

	svgs.selectAll("rect.tavoite")
	.data(function(d) { return d.tavoitteet; })
	.style("fill", function(d) { return d.stripes ? "url(#tavoitePattern)" : d.color; });

	tavoitteet.exit().remove();

	// Tavoitteiden aihetunnisteet
	{

		var tagG = svgs.selectAll("g.tavoiteTags")
		.data(function(d) { 
			return d.tavoitteet; 
		});

		var tavoiteTags = tagG.selectAll("g.tavoiteTag")
		.data(function(d) {
			return d.tags;
		});
		
		creates = tavoiteTags.enter().append("g").classed("tavoiteTag", true);
		
		creates.append("rect").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("height", "" + (tavoiteTagHeight))
		.style("cursor", "pointer")
		.style("stroke-width", "0")
		.style("fill", function(d) { return d.color;});

		creates.append("rect").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("rx", "" + a4ratio*2)
		.attr("height", "" + tavoiteTagHeight)
		.style("cursor", "pointer")
		.style("stroke-width", "1")
		.style("stroke", function(d) { return d.color;})
		.style("fill", "none");

		creates.append("text").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("x", "2")
		.attr("y", "7")
		.text(function(d,i) { return d.text; });
		
		tavoiteTags.exit().remove();
		
	}
	
	// Tavoitteiden mittarit
	{

		var meterG = svgs.selectAll("g.tavoiteMeters")
		.data(function(d,i) { 
			return d.tavoitteet; 
		});

		var tavoiteMeters = meterG.selectAll("g.tavoiteMeter")
		.data(function(d,i) {
			return d.meters;
		});

		creates = tavoiteMeters.enter().append("g").classed("tavoiteMeter", true);
		
		creates.append("rect").classed("tavoiteMeter", true)
		.attr("height", "" + tavoiteMeterHeight)
		.attr("rx", "1")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex); 
		})
		.style("stroke", "#111")
		.style("stroke-width", "0")
		.style("fill", function(d) { return d.color;})
		.call(wrapFocus, painopisteWidth-2*textPad);

		creates.append("text").classed("tavoiteMeter", true)
		.attr("x", "2")
		.attr("y", "11")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex); 
		})
		.text(function(d,i) { return d.text; });

		tavoiteMeters.exit().remove();

		meterG.selectAll("rect.tavoiteMeter")
		.data(function(d,i) { return d.meters; })
		.style("fill", function(d) { return d.color;});
		
	}
	
	{

		// Painopisteet
		var painopisteet = tavoitteet.selectAll("g.painopiste")
		.data(function(d,i) {
			return d.painopisteet;
		});

		creates = painopisteet.enter().append("g").classed("painopiste", true);

		creates.append("rect").classed("painopiste", true)
		.attr("width", painopisteWidth)
		.attr("height", painopisteHeight)
		.attr("rx", "2")
		.attr("ry", "2")
		.style("cursor", function(d) { return "pointer"; })
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.navi2(d3.event.pageX, d3.event.pageY, d.tavoite, d.realIndex);
		});
		
		creates.append("g").classed("painopisteMeters", true);
		creates.append("g").classed("painopisteTags", true);

		creates = creates.append("g").classed("painopisteContent", true);

		creates.append("text").classed("painopiste idElement", true)
		.attr("transform", "translate(" + (0.5*painopisteWidth) + ", " + (a4ratio*21) + ")");

		creates.append("text").classed("painopiste textElement", true)
		.attr("transform", "translate(" + (0.5*painopisteWidth) + ", " + (textPad+a4ratio*45) + ")");

		tavoitteet.selectAll("text.painopiste.idElement")
		.data(function(d,i) {
			return d.painopisteet;
		})
		.text(function(d) { return d.id; });

		tavoitteet.selectAll("text.painopiste.textElement")
		.data(function(d,i) {
			return d.painopisteet;
		})
		.style("cursor", function(d) { return (state.logged && !d.copy) ? "text" : "default"; })
		.text(function(d) { return d.text.length > 0 ? d.text : "<ei tekstiä>";})
		.on("click", function(d,i) { 
			if(state.logged && !d.copy) {
				d3.event.stopPropagation();
				rootFn.editPainopiste(d.tavoite, d.realIndex); 
			}
		})
		.on("mouseover", function(d,i) { if(state.logged && !d.copy) d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", "#000000"); })
		.call(wrapFocus, painopisteWidth-2*textPad);

		tavoitteet.selectAll("rect.painopiste")
		.data(function(d,i) {
			return d.painopisteet;
		})
		.style("fill", function(d) { return d.color; });

		painopisteet.exit().remove();

	}

	// Painopisteiden aihetunnisteet
	{

		var tagG = tavoitteet.selectAll("g.painopisteTags")
		.data(function(d) { 
			return d.painopisteet; 
		});

		var painopisteTags = tagG.selectAll("g.painopisteTag")
		.data(function(d) {
			return d.tags;
		});
		
		creates = painopisteTags.enter().append("g").classed("painopisteTag", true);

		creates.append("rect").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("height", "" + (painopisteTagHeight))
		.style("cursor", "pointer")
		.style("stroke-width", "0")
		.style("fill", function(d) { return d.color;});

		creates.append("rect").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("rx", "" + a4ratio*2)
		.attr("height", "" + painopisteTagHeight)
		.style("cursor", "pointer")
		.style("stroke-width", "1")
		.style("stroke", function(d) { return d.color;})
		.style("fill", "none");

		creates.append("text").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("x", "2")
		.attr("y", "6")
		.text(function(d,i) { return d.text; });

		painopisteTags.exit().remove();
		
	}
	
	// Painopisteiden mittarit
	{

		var meterG = tavoitteet.selectAll("g.painopisteMeters")
		.data(function(d,i) { 
			return d.painopisteet;
		});

		var painopisteMeters = meterG.selectAll("g.painopisteMeter")
		.data(function(d,i) {
			return d.meters;
		});
		
		creates = painopisteMeters.enter().append("g").classed("painopisteMeter", true);
		
		creates.append("rect").classed("painopisteMeter", true)
		.attr("width", "" + a4ratio*30)
		.attr("height", "" + painopisteMeterHeight)
		.attr("rx", "1")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex); 
		})
		.style("stroke", "#111")
		.style("stroke-width", "0")
		.style("fill", function(d) { return d.color;})
		.call(wrapFocus, painopisteWidth-2*textPad);

		creates.append("text").classed("painopisteMeter", true)
		.attr("x", "2")
		.attr("y", "9")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex); 
		})
		.text(function(d,i) { return d.text; });

		painopisteMeters.exit().remove();

		meterG.selectAll("rect.painopisteMeter")
		.data(function(d,i) { return d.meters; })
		.style("fill", function(d) { return d.color;});

	}
	
	
}

function navigointiLinkit(rootFn, svgs, map) {

	var alikartat = svgs.selectAll("g.alikartta")
	.data(function(d,i) {
		return d.alikartat;
	});

	creates = alikartat.enter().append("g").classed("alikartta", true);

	creates.append("text").classed("alikartta link", true)
	.attr("transform", "translate(0,5)")
	.on("click", function(d,i) { 
		d3.event.stopPropagation();
		rootFn.navigate(d.uuid); 
	});

	svgs.selectAll("g.alikartta")
	.data(function(d,i) {
		return d.alikartat;
	})
	.attr("transform", function(d, i){ 
		return "translate("+ 0.5*chartWidth + "," + (30 + i*a4ratio*37) + ")";
		});

	svgs.selectAll("text.alikartta.link")
	.data(function(d,i) {
		return d.alikartat;
	})
	.text(function(d,i) { return d.text; });

	alikartat.exit().remove();
	
	return 30 + map.alikartat.length * a4ratio*37; 
	
};

function wrapImpl(element, width, allowedNumberOfLines) {
	var text = d3.select(element),
	words = text.text().split(/\s+/).reverse(),
	word,
	line = [],
	y = text.attr("y"),
	tspan = null,
	firstSpan = null;
	text.text(null);
	//allowedNumberOfLines = 3;
	wordLength = 0;
	lineLength = 0;
	lineNumber = 0;
	allowedWidth = width;
	while (word = words.pop()) {
		tspan = text.append("tspan").attr("y", y).text(word+"\u00A0");
		if(firstSpan == null) firstSpan = tspan;
		wordLength = tspan.node().getComputedTextLength();
		lineLength += wordLength; 
		if(word.length > 0 && word.charAt(0) == "#") {
			if(word.indexOf("@") > -1)
				tspan.attr("fill", "#3000ff");
			else if (word.indexOf("%") > -1)
				tspan.attr("fill", "#30ff00");
			else
				tspan.attr("fill", "#ff0003");
		}
		if(lineLength > allowedWidth) {
			lineNumber++;
			if(lineNumber < allowedNumberOfLines) {
				tspan.attr("dy", "1.1em").attr("x", 0);
				lineLength = wordLength;
			} else {
				tspan.text("...");
				break;
			}
			if(lineNumber == allowedNumberOfLines) allowedWidth = 2*width / 3;
		}
	}
	text.attr("lineCount", lineNumber+1);
	return { ln : lineNumber, span : firstSpan };
}

function wrapVisio(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 2);
		if(result.ln == 0 && result.span != null)
			result.span.attr("dy", "0.2em");
	});
}

function wrapBrowseNode(text) {
	text.each(function() {
		var result = wrapImpl(this, this.__data__.w-40, 2);
		if(result.ln == 0 && result.span != null)
			result.span.attr("dy", "0.3em");
	});
}

function wrapFocus(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 10);
		if(result.ln < 2 && result.span != null)
			result.span.attr("dy", "0.3em");
	});
}

var singletonBrowserRootFn;

fi_semantum_strategia_widget_Browser = function() {	

	var self = this;

	singletonBrowserRootFn = this;

	createBrowser(this);

	this.onStateChange = function() {
		refreshBrowser(self);
	};

	this.onStateChange();

};

var force;
var lastTranslate = "0,0";
var lastScale = "1";

var zoom = d3.behavior.zoom()
.scaleExtent([0.1, 10])
.on("zoom", zoomed);

var linkVis;
var nodeVis;

function zoomed() {
	lastScale = d3.event.scale;
	lastTranslate = d3.event.translate;
	linkVis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
	nodeVis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

function createBrowser(rootFn) {

	var thisElement = rootFn.getElement();

	thisElement.style.backgroundColor = '#FFF';

	var root = d3.select(thisElement);

	force = d3.layout.force()
	.gravity(0.0)
	.charge(function(d) { 
		return d.charge; 
	})
	.chargeDistance(1600.0)
	.linkDistance(function(d) { 
		return d.linkDistance; 
	})
	.linkStrength(function(d) { 
		return d.linkStrength; 
	});
	
	var svg = root
	.append("svg:svg")
	.attr("id", "browser")
	.attr("class", "browser")
	.attr("width", 20)
	.attr("height", 20)
	.call(zoom);

	linkVis = svg.append("svg:g").classed("links", true);
	nodeVis = svg.append("svg:g").classed("nodes", true);

	linkVis.attr("transform", "translate(" + lastTranslate + ")scale(" + lastScale + ")");
	nodeVis.attr("transform", "translate(" + lastTranslate + ")scale(" + lastScale + ")");

}

var drag = d3.behavior.drag()
.origin(function(d) { return d; })
.on("dragstart", dragstarted)
.on("dragend", dragended);	

function dragstarted(d) {
	d3.event.sourceEvent.stopPropagation();
	force.stop();
}

function dragged(self, data, d) {
	
	d.x = d.px = d3.event.x;
	d.y = d.py = d3.event.y;
	d.fixed = true;
	
	d3.select(self).select("rect.fixed").attr("display", null);

	nodeMap[d.uuid] = {'x': d.x, 'y': d.y, 'px' : d.px, 'py:' : d.py, 'fixed' : d.fixed };
	d3.select(self).attr("transform", "translate(" + d.x+ "," + d.y + ")");
	tick(data, node, link);
	force.start();
	d3.event.sourceEvent.stopPropagation();
	
}

function dragended(d) {
	force.friction(0.9);
	d3.event.sourceEvent.stopPropagation();
}

function tick(data, node, link) {
	
	node.attr("transform", function(d, i) {
		nodeMap[d.uuid] = {'x': d.x, 'y': d.y, 'px' : d.px, 'py:' : d.py, 'fixed' : d.fixed };
		return "translate(" + d.x + "," + d.y + ")"; 
	});

	link.attr("x1", function(d)   { return d.source.x + 0.5*d.source.w; })
	.attr("y1", function(d)   { return d.source.y + 0.5*d.source.h; })
	.attr("x2", function(d)   { return d.target.x + 0.5*d.target.w; })
	.attr("y2", function(d)   { return d.target.y + 0.5*d.target.h; });

}

var nodeMap = new Object();
var node;
var link;

function doSaveBrowserState(name) {
	
	if (typeof singletonBrowserRootFn === 'undefined') return;
	singletonBrowserRootFn.save(name, nodeMap);
	
}

function refreshBrowser(rootFn) {
	
	var data = rootFn.getState();

	drag.on("drag", function(d) { 
		dragged(this, data, d); 
	});
	
	var thisElement = rootFn.getElement();
	var root = d3.select(thisElement).datum(data);

	var svg = root.select("svg");

	svg.attr("width", data.w);
	svg.attr("height", data.h);
	svg.attr("cursor", "move");

	var links = document.getElementsByClassName('links')[0];
	var nodes = document.getElementsByClassName('nodes')[0];

	if(!data.setPositions) {
		for (var i=0;i<data.nodes.length;i++) {
			var n = data.nodes[i];
			var cache = nodeMap[n.uuid];
			if(cache != null) {
				n.x = cache.x;
				n.y = cache.y;
				n.px = cache.px;
				n.py = cache.py;
				n.fixed = cache.fixed;
			}
		}
	}
	
	if(force != null) force.stop();
	
	force
	.nodes(data.nodes)
	.links(data.links)
	.size([data.w, data.h]);

	link = d3.select(links).selectAll(".browseLink").data(data.links);
	
	link.enter().append("line")
	.attr("class", "browseLink")
	.attr("fill", "none");

	link = d3.select(links).selectAll(".browseLink").data(data.links);

	link
	.attr("cursor", "default")
	.style("stroke", function(d) { 
		return d.color; 
	})
	.style("stroke-dasharray", function(d) { 
		return d.dash;
	})
	.style("stroke-width", function(d) { 
		return 0.25*d.weight;
	});
	
	link.exit().remove();

	node = d3.select(nodes).selectAll("g.node").data(data.nodes);
	
	var enterNode = node.enter().append("g")
	.attr("class", "node")
	.attr("id", function(d) { return d.uuid; })
	.call(drag);
	
	enterNode.append("svg:rect").classed("outline", true);
	enterNode.append("svg:rect").classed("status", true);
	enterNode.append("svg:rect").classed("main", true);
	enterNode.append("svg:rect").classed("fixed", true);
	enterNode.append("svg:text").classed("browseName", true);
	enterNode.append("svg:text").classed("description", true);

	uses = d3.select(nodes).selectAll("use").data([1]);
	var enterUses = uses.enter().append("use")
	.attr("xlink:href", "");
	
	svg.on("mouseover", function() {

		var target = d3.event.target;
		if(target.parentNode.parentNode == nodes) {
			
			var d = target.__data__;
			d3.select(target.parentNode).select("rect.outline").attr("display", null);
			
			var links = document.getElementsByClassName('browseLink');
			for(var i=0;i<links.length;i++) {
				var link = links[i];
				var linkData = link.__data__;
				if(linkData.source == d || linkData.target == d) {
					link.style.stroke = "#ea2";
					link.style.strokeWidth = 0.75 * linkData.weight;
				}
			}
			
		}
		
	});

	svg.on("mouseout", function() {

		var target = d3.event.target;
		if(target.parentNode.parentNode == nodes) {
			var d = target.__data__;
			var links = document.getElementsByClassName('browseLink');
			for(var i=0;i<links.length;i++) {
				var link = links[i];
				var linkData = link.__data__;
				if(linkData.source == d || linkData.target == d) {
					link.style.stroke = linkData.color;
					link.style.strokeWidth = 0.25 * linkData.weight;

				}
			}
			
			if(d.emph) return;
			d3.select(target.parentNode).select("rect.outline").attr("display", "none");
			
		}
		
	});

	node.select("rect.outline")
	.attr("width", function(d) { return  d.w+20; } )
	.attr("height", function(d) { return  d.h+20; } )
	.attr("x", -10)
	.attr("y", -10)
	.attr("rx", 10)
	.attr("ry", 10)
	.attr("display", function(d) { return  d.emph ? null : "none"; } )
	.style("stroke",function(d,i) { 
		return d.color; 
	})
	.style("stroke-opacity","0.7")
	.style("fill","none")
	.style("stroke-width", 20);

	node.select("rect.main")
	.attr("width", function(d) { return  d.w; } )
	.attr("height", function(d) { return  d.h; } )
	.attr("rx", 10)
	.attr("ry", 10)
	.style("stroke",function(d,i) { 
		return d.color; 
	})
	.style("stroke-width", 6)
	.attr("fill", "none")
	.attr("cursor", "pointer")
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});

	node.select("rect.status")
	.attr("width", function(d) { return  d.w-16; } )
	.attr("height", function(d) { return  d.h-16; } )
	.attr("x", 8)
	.attr("y", 8)
	.attr("rx", 3)
	.attr("ry", 3)
	.attr("cursor", "pointer")
	.style("stroke", function(d) { return  d.circleFill; })
	.style("stroke-width", 10)
	.attr("fill", "#FFF" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});

	node.select("rect.fixed")
	.attr("x", function(d) { return  d.w-20; } )
	.attr("y", function(d) { return  d.h-20; } )
	.attr("width", 40)
	.attr("height", 40)
	.attr("rx", 2)
	.attr("ry", 2)
	.attr("cursor", "pointer")
	.style("stroke", function(d) { return  d.color; })
	.style("stroke-width", 6)
	.attr("fill", "#B22" )
	.attr("display", function(d) { return d.fixed ? null : "none"; })
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		d.fixed = false;
		d3.select(this).attr("display", "none");
		force.start();
	});

	node.select("text.browseName")
	.text(function(d, i) { return d.name; })
	.attr("transform", function(d) { return "translate(" + (d.w/2) + "," + (d.nameSize + 0.05*d.h) + ")"; })
	.attr("cursor", "pointer")
	.style("text-decoration", "underline")
	.attr("fill", "#111" )
	.attr("font-size", function(d) { return d.nameSize + "px"; })
	.attr("text-anchor", "middle" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});
	//.call(wrapBrowseNode, 200);
	
	node.select("text.description")
	.text(function(d, i) { return d.description; })
	.attr("transform", function(d) { return "translate(" + (d.w/2) + "," + (d.nameSize + 0.05*d.h + d.descriptionSize + 0.05*d.h) + ")"; })
//	.attr("x", function(d) { return  d.w/2; } )
//	.attr("y", function(d) { return (d.nameSize + 10 + d.descriptionSize + 10); })
	.attr("cursor", "pointer")
	.attr("fill", "#111" )
	.attr("font-size", function(d) { return d.descriptionSize + "px"; })
	.attr("text-anchor", "middle" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	})
	.call(wrapBrowseNode);

	node.exit().remove();

	force.on("tick", function(e) {
		tick(data, node, link);
	});

	force.start();

};



