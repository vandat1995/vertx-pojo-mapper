/*
 * #%L
 * vertx-pojo-mapper-common
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.dataaccess.query;

import de.braintags.vertx.jomnigate.IDataStore;
import de.braintags.vertx.jomnigate.dataaccess.IAccessResult;
import de.braintags.vertx.jomnigate.dataaccess.query.impl.IQueryExpression;
import de.braintags.vertx.jomnigate.mapping.IMapper;
import de.braintags.vertx.util.CollectionAsync;

/**
 * The result of an executed {@link IQuery}. Acts as an unmodifyable {@link CollectionAsync}, so that implementations
 * can decide to perform a lazy load of found results
 * 
 * @author Michael Remme
 * @param <E>
 *          the underlaying mapper class
 * 
 */

public interface IQueryResult<E> extends CollectionAsync<E>, IAccessResult {

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
  public IQueryExpression getOriginalQuery();

  /**
   * If the {@link IQuery#setReturnCompleteCount(boolean)} is set to true and {@link IQuery#setLimit(int)} is set with a
   * value > 0, then here will be returned the complete number of fitting records
   * Otherwise the length of the current selection is returned
   * 
   * @return the complete number of records
   */
  public long getCompleteResult();

}
