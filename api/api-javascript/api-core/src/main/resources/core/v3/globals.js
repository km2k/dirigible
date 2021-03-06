/*
 * Copyright (c) 2010-2019 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 */
var java = require('core/v3/java');

exports.get = function(name) {
	if (__engine === 'rhino') {
		return org.eclipse.dirigible.api.v3.core.GlobalsFacade.get(name);
	}
	if (__engine === 'nashorn') {
		return Packages.org.eclipse.dirigible.api.v3.core.GlobalsFacade.get(name);
	}
	var value = java.call('org.eclipse.dirigible.api.v3.core.GlobalsFacade', 'get', [name]);
	return value;
};

exports.set = function(name, value) {
	if (__engine === 'rhino') {
		org.eclipse.dirigible.api.v3.core.GlobalsFacade.set(name, value);
	} else if (__engine === 'nashorn') {
		Packages.org.eclipse.dirigible.api.v3.core.GlobalsFacade.set(name, value);
	} else {
		java.call('org.eclipse.dirigible.api.v3.core.GlobalsFacade', 'set', [name, value]);
	}
};

exports.list = function() {
	if (__engine === 'rhino') {
		return org.eclipse.dirigible.api.v3.core.GlobalsFacade.list();
	}
	if (__engine === 'nashorn') {
		return Packages.org.eclipse.dirigible.api.v3.core.GlobalsFacade.list();
	}
	var value = java.call('org.eclipse.dirigible.api.v3.core.GlobalsFacade', 'list', []);
	return value;
};
