/*
 * #%L
 * vertx-pojo-mapper-json
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.mapper;

import java.util.Map;

import de.braintags.vertx.jomnigate.annotation.Entity;
import de.braintags.vertx.jomnigate.annotation.field.Id;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
@Entity
public class Mapper_MapWithReferenced {
  @Id
  public String idField;
  public Map<String, MapperWithDeepReferenced> refMap;

}
