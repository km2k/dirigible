/*******************************************************************************
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.ide.template.ui.tc.wizard;

import org.eclipse.dirigible.ide.template.ui.common.GenerationModel;
import org.eclipse.dirigible.ide.template.ui.common.TemplateGenerator;
import org.eclipse.dirigible.repository.api.ICommonConstants;

/**
 * TestCase Template Generator
 */
public class TestCaseTemplateGenerator extends TemplateGenerator {

	private static final String LOG_TAG = "TEST_CASE_GENERATOR"; //$NON-NLS-1$

	private TestCaseTemplateModel model;

	/**
	 * Constructor
	 *
	 * @param model
	 */
	public TestCaseTemplateGenerator(TestCaseTemplateModel model) {
		this.model = model;
	}

	@Override
	protected GenerationModel getModel() {
		return model;
	}

	@Override
	protected String getLogTag() {
		return LOG_TAG;
	}

	@Override
	protected byte[] afterGeneration(byte[] bytes) {
		return model.normalizeEscapes(bytes);
	}

	@Override
	protected String getDefaultRootFolder() {
		return ICommonConstants.ARTIFACT_TYPE.TEST_CASES;
	}

}