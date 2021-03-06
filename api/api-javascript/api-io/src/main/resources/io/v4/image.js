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

/**
 * API v4 Image
 * 
 * Note: This module is supported only with the Mozilla Rhino engine
 */

var streams = require("io/v4/streams");

exports.resize = function(original, type, width, height) {
	var native = org.eclipse.dirigible.api.v3.io.ImageFacade(original, type, width, height);
	var inputStream = new streams.InputStream();
	inputStream.native = native;
	return inputStream;
};
