/*
 * #%L
 * vertx-pojo-mapper-common
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.pojomapper.dataaccess.query;

import de.braintags.io.vertx.pojomapper.IDataStore;
import de.braintags.io.vertx.pojomapper.mapping.IMapper;
import de.braintags.io.vertx.util.CollectionAsync;

/**
 * The result of an executed {@link IQuery}. Acts as an unmodifyable {@link CollectionAsync}, so that implementations
 * can decide to perform a lazy load of found results
 * 
 * @author Michael Remme
 * 
 */

public interface IQueryResult<E> extends CollectionAsync<E> {

  /**
   * Get the {@link IDataStore} by which the current instance was created
   * 
   * @return
   */
  public IDataStore getDataStore();

  /**
   * Get the underlaying {@link IMapper}
   * 
   * @return
   */
  public IMapper getMapper();

  /**
   * Get the original query, which was executed in the datastore
   * 
   * @return the query
   */
  public Object getOriginalQuery();

}
