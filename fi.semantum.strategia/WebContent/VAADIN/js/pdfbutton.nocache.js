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

var $wnd = $wnd || window.parent;
$wnd.extractSVG = function extractSVG () {
  var v = document.getElementById('map');
  var s = new XMLSerializer();
  return s.serializeToString(v);
};
