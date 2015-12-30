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

public class TimeSeriesEntry implements Serializable {

	private static final long serialVersionUID = -2255599915523496925L;
	
	private Object value;
	private Account account;
	private String shortComment;
	private String comment;
	
	public TimeSeriesEntry(Object value, Account account, String shortComment, String comment) {
		this.value = value;
		this.account = account;
		this.shortComment = shortComment;
		this.comment = comment;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public String getShortComment() {
		return shortComment;
	}

	public String getComment() {
		return comment;
	}
	
	public Object getValue() {
		return value;
	}
	
}
