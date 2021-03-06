/*-
 * #%L
 * vertx-pojo-mapper-common-test
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.braintags.vertx.jomnigate.testdatastore.mapper;

import de.braintags.vertx.jomnigate.annotation.Entity;
import de.braintags.vertx.jomnigate.annotation.field.Id;
import de.braintags.vertx.jomnigate.dataaccess.query.IIndexedField;
import de.braintags.vertx.jomnigate.dataaccess.query.impl.IndexedField;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */

@Entity
public class MiniMapper {
  public static final IIndexedField NAME = new IndexedField("name");

  @Id
  public String id = null;
  public String name = "testName";

  public transient String transientString;

  public MiniMapper() {
  }

  public MiniMapper(String name) {
    this.name = name;
  }

}
