/*
 * Copyright 2014 Red Hat, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * 
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 * 
 * You may elect to redistribute this code under either of these licenses.
 */

package de.braintags.io.vertx.pojomapper.mapping;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import de.braintags.io.vertx.pojomapper.IDataStore;

/**
 * A factory which creates instances of {@link IStoreObject} which are suitable for the current {@link IDataStore}
 * 
 * @author Michael Remme
 * 
 */

public interface IStoreObjectFactory {

  /**
   * Creates a new instance of {@link IStoreObject} by using the information from the given entity. The entity is
   * converted into the format, which can be used to save it into the {@link IDataStore}
   * 
   * @param mapper
   *          the mapper for the entity to be handled
   * @param entity
   *          the entity
   * @param handler
   *          the handler to be recalled
   */
  public void createStoreObject(IMapper mapper, Object entity, Handler<AsyncResult<IStoreObject<?>>> handler);

  /**
   * Creates a new instance of {@link IStoreObject} by using the information from the given stored object from the
   * {@link IDataStore}. The informations from the storedObject are used to create a suitable POJO
   * 
   * @param storedObject
   *          the stored object from the {@link IDataStore}
   * @param mapper
   *          the mapper
   * @param handler
   *          the handler to be recalled
   */
  public void createStoreObject(Object storedObject, IMapper mapper, Handler<AsyncResult<IStoreObject<?>>> handler);

}
