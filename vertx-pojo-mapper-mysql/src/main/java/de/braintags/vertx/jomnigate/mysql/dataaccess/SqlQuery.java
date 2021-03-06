/*
 * #%L
 * vertx-pojo-mapper-mysql
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.braintags.vertx.jomnigate.mysql.dataaccess;

import de.braintags.vertx.jomnigate.IDataStore;
import de.braintags.vertx.jomnigate.dataaccess.query.IQuery;
import de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult;
import de.braintags.vertx.jomnigate.dataaccess.query.IQueryResult;
import de.braintags.vertx.jomnigate.dataaccess.query.impl.IQueryExpression;
import de.braintags.vertx.jomnigate.dataaccess.query.impl.Query;
import de.braintags.vertx.jomnigate.mysql.MySqlDataStore;
import de.braintags.vertx.jomnigate.mysql.SqlUtil;
import de.braintags.vertx.jomnigate.mysql.exception.SqlException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;

/**
 *
 * An implementation of {@link IQuery} for sql databases
 *
 * @param <T>
 *          the type of the mapper, which is handled here
 * @author Michael Remme
 *
 */

public class SqlQuery<T> extends Query<T> {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(SqlQuery.class);

  /**
   * @param mapperClass
   * @param datastore
   */
  public SqlQuery(Class<T> mapperClass, IDataStore datastore) {
    super(mapperClass, datastore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQuery#execute(io.vertx.core.Handler)
   */
  @Override
  public void internalExecute(IQueryExpression queryExpression, Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    doFind((SqlExpression) queryExpression, resultHandler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQuery#executeCount(io.vertx.core.Handler)
   */
  @Override
  public void internalExecuteCount(IQueryExpression queryExpression,
      Handler<AsyncResult<IQueryCountResult>> resultHandler) {
    executeCount((SqlExpression) queryExpression, resultHandler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQuery#executeExplain(io.vertx.core.Handler)
   */
  @Override
  public void executeExplain(Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    resultHandler.handle(Future.failedFuture(new UnsupportedOperationException("Not implemented yet")));
  }

  private void executeCount(SqlExpression statement, Handler<AsyncResult<IQueryCountResult>> resultHandler) {
    if (statement.hasQueryParameters()) {
      SqlUtil.queryWithParams((MySqlDataStore) getDataStore(), statement.getCountExpression(),
          statement.getParameters(), qRes -> handleCountResult(qRes, statement, resultHandler));
    } else {
      SqlUtil.query((MySqlDataStore) getDataStore(), statement.getCountExpression(),
          qRes -> handleCountResult(qRes, statement, resultHandler));
    }
  }

  private void handleCountResult(AsyncResult<ResultSet> qRes, SqlExpression statement,
      Handler<AsyncResult<IQueryCountResult>> resultHandler) {
    if (qRes.failed()) {
      String message = "Executed count: " + statement.toString();
      resultHandler.handle(Future.failedFuture(new SqlException(message, qRes.cause())));
      return;
    }
    SqlQueryCountResult cr = new SqlQueryCountResult(getMapper(), getDataStore(), qRes.result(), statement);
    resultHandler.handle(Future.succeededFuture(cr));
  }

  private void doFind(SqlExpression statement, Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    LOGGER.debug("start doFind");
    if (statement.hasQueryParameters()) {
      SqlUtil.queryWithParams((MySqlDataStore) getDataStore(), statement.getSelectExpression(),
          statement.getParameters(), qRes -> handleQueryResult(qRes, statement, resultHandler));
    } else {
      SqlUtil.query((MySqlDataStore) getDataStore(), statement.getSelectExpression(),
          qRes -> handleQueryResult(qRes, statement, resultHandler));
    }
  }

  private void handleQueryResult(AsyncResult<ResultSet> qRes, SqlExpression statement,
      Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    if (qRes.failed()) {
      String message = "Executed query: " + statement.toString();
      resultHandler.handle(Future.failedFuture(new SqlException(message, qRes.cause())));
      return;
    }
    createQueryResult(qRes.result(), statement, resultHandler);
  }

  private void createQueryResult(ResultSet resultSet, SqlExpression statement,
      Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    SqlQueryResult<T> qR = new SqlQueryResult<>(resultSet, (MySqlDataStore) getDataStore(), getMapper(), statement);
    if (isReturnCompleteCount()) {
      if (statement.getOffset() == 0 && statement.getLimit() > 0 && qR.size() < statement.getLimit()) {
        qR.setCompleteResult(qR.size());
        resultHandler.handle(Future.succeededFuture(qR));
      } else {
        fetchCompleteCount(qR, resultHandler);
      }
    } else {
      qR.setCompleteResult(-1);
      resultHandler.handle(Future.succeededFuture(qR));
    }
  }

  private void fetchCompleteCount(SqlQueryResult<T> qR, Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    executeCount(cr -> {
      if (cr.failed()) {
        resultHandler.handle(Future.failedFuture(cr.cause()));
      } else {
        long count = cr.result().getCount();
        qR.setCompleteResult(count);
        resultHandler.handle(Future.succeededFuture(qR));
      }
    });
  }

  /* (non-Javadoc)
   * @see de.braintags.vertx.jomnigate.dataaccess.query.impl.Query#getQueryExpressionClass()
   */
  @Override
  protected Class<? extends IQueryExpression> getQueryExpressionClass() {
    return SqlExpression.class;
  }

}
