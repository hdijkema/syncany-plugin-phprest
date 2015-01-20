/*
 * Syncany, www.syncany.org
 * Copyright (C) 2015 Hans Dijkema <hans@dykema.nl>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.plugins.php;

import java.util.logging.Logger;

import org.simpleframework.xml.Element;
import org.syncany.plugins.transfer.Setup;
import org.syncany.plugins.transfer.TransferSettings;

public class PhpTransferSettings extends TransferSettings {
	private static final Logger logger = Logger.getLogger(PhpTransferSettings.class.getSimpleName());

	@Element(name = "url", required = true)
	@Setup(order = 1, description = "URL for the REST site")
	public String url;

	@Element(name = "userid", required = true)
	@Setup(order = 2, description = "User id")
	public String userid;
	
	@Element(name = "passwd", required = true)
	@Setup(order = 3, description = "Password")
	public String passwd;

	@Element(name = "context", required = true)
	@Setup(order = 4, description = "Context Identifier for this storage")
	public String context;
	
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String u) {
		this.url = u;
	}
	
	public String getUserId() {
		return userid;
	}
	
	public void setUserId(String u) {
		this.userid = u;
	}
	
	public String getPasswd() {
		return passwd;
	}
	
	public void setPasswd(String p ) {
		this.passwd = p;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String c) {
		this.context = c;
	}
	
	
}
