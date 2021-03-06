/**
 * Copyright (c) 2010-2018 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 */
package org.eclipse.dirigible.repository.database;

import static org.junit.Assert.fail;

import javax.sql.DataSource;

import org.eclipse.dirigible.repository.db.DatabaseRepository;
import org.eclipse.dirigible.repository.generic.RepositoryGenericModifiedTest;
import org.junit.Before;

/**
 * The Class DatabaseRepositoryModifiedTest.
 */
public class DatabaseRepositoryModifiedTest extends RepositoryGenericModifiedTest {

	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {
		try {
			DataSource dataSource = DatabaseTestHelper.createDataSource("target/tests/derby");
			repository1 = new DatabaseRepository(dataSource);
			repository2 = new DatabaseRepository(dataSource);
			repository3 = new DatabaseRepository(dataSource);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
