/*
 * #%L
 * vertx-pojongo
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.pojomapper.mongo.dataaccess;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import de.braintags.io.vertx.pojomapper.dataaccess.query.IFieldParameter;
import de.braintags.io.vertx.pojomapper.dataaccess.query.ILogicContainer;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery;
import de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler;
import de.braintags.io.vertx.pojomapper.exception.MappingException;
import de.braintags.io.vertx.pojomapper.exception.QueryParameterException;
import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.mapping.datastore.IColumnInfo;
import de.braintags.io.vertx.util.CounterObject;
import de.braintags.io.vertx.util.ErrorObject;
import de.braintags.io.vertx.util.Size;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Implementation fills the contents into a {@link JsonObject} which then can be used as source for
 * {@link MongoClient#find(String, JsonObject, io.vertx.core.Handler)}
 * 
 * @author Michael Remme
 * 
 */

public class MongoQueryRambler implements IQueryRambler {
  private JsonObject qDef = new JsonObject();
  private Object currentObject = qDef;
  private Deque<Object> deque = new ArrayDeque<>();

  /**
   * 
   */
  public MongoQueryRambler() {
  }

  /**
   * @return the jsonObject
   */
  public JsonObject getJsonObject() {
    return qDef;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#start(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.IQuery)
   */
  @Override
  public void start(IQuery<?> query) {
    // no use for Mongo
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#stop(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.IQuery)
   */
  @Override
  public void stop(IQuery<?> query) {
    // no use for Mongo
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#start(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.ILogicContainer)
   */
  @Override
  public void start(ILogicContainer<?> container) {
    String logic = QueryLogicTranslator.translate(container.getLogic());
    JsonArray array = new JsonArray();
    add(logic, array);
    deque.addLast(currentObject);
    currentObject = array;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#stop(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.ILogicContainer)
   */
  @Override
  public void stop(ILogicContainer<?> container) {
    currentObject = deque.pollLast();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#start(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.IFieldParameter)
   */
  @Override
  public void start(IFieldParameter<?> fieldParameter, Handler<AsyncResult<Void>> resultHandler) {

    switch (fieldParameter.getOperator()) {
    case IN:
    case NOT_IN:
      handleMultipleValues(fieldParameter, resultHandler);
      break;

    default:
      handleSingleValue(fieldParameter, resultHandler);
    }
  }

  /**
   * Create the argument for query parts, which define one single argument
   * 
   * @param fieldParameter
   * @param resultHandler
   */
  private void handleMultipleValues(IFieldParameter<?> fieldParameter, Handler<AsyncResult<Void>> resultHandler) {
    IField field = fieldParameter.getField();
    IColumnInfo ci = field.getMapper().getTableInfo().getColumnInfo(field);
    if (ci == null) {
      resultHandler
          .handle(Future.failedFuture(new MappingException("Can't find columninfo for field " + field.getFullName())));
      return;
    }

    String mongoOperator = QueryOperatorTranslator.translate(fieldParameter.getOperator());
    Object valueIterable = fieldParameter.getValue();
    if (!(valueIterable instanceof Iterable)) {
      resultHandler.handle(
          Future.failedFuture(new QueryParameterException("multivalued argument but not an instance of Iterable")));
      return;
    }
    int count = Size.size((Iterable<?>) valueIterable);
    if (count == 0) {
      resultHandler
          .handle(Future.failedFuture(new QueryParameterException("multivalued argument but no values defined")));
      return;
    }

    CounterObject co = new CounterObject(count);
    Iterator<?> values = ((Iterable<?>) valueIterable).iterator();
    ErrorObject<Void> errorObject = new ErrorObject<Void>();
    JsonArray resultArray = new JsonArray();

    // TODO check the loop handling here!! see below
    while (values.hasNext() && !errorObject.isError()) {
      Object value = values.next();
      field.getTypeHandler().intoStore(value, field, result -> {
        if (result.failed()) {
          errorObject.setThrowable(result.cause());
          errorObject.handleError(resultHandler);
          return;
        } else {
          resultArray.add(result.result().getResult());
          if (co.reduce()) {
            JsonObject arg = new JsonObject().put(mongoOperator, resultArray);
            String colName = ci.getName();
            add(colName, arg);
            resultHandler.handle(Future.succeededFuture());
          }
        }
      });
    }
  }

  /**
   * Create the argument for query parts, which define one single argument
   * 
   * @param fieldParameter
   * @param resultHandler
   */
  private void handleSingleValue(IFieldParameter<?> fieldParameter, Handler<AsyncResult<Void>> resultHandler) {
    IField field = fieldParameter.getField();
    IColumnInfo ci = field.getMapper().getTableInfo().getColumnInfo(field);
    if (ci == null) {
      resultHandler
          .handle(Future.failedFuture(new MappingException("Can't find columninfo for field " + field.getFullName())));
      return;
    }
    String mongoOperator = QueryOperatorTranslator.translate(fieldParameter.getOperator());
    Object value = fieldParameter.getValue();

    field.getTypeHandler().intoStore(value, field, result -> {
      if (result.failed()) {
        resultHandler.handle(Future.failedFuture(result.cause()));
      } else {
        Object storeObject = result.result().getResult();
        JsonObject arg = new JsonObject().put(mongoOperator, storeObject);
        add(ci.getName(), arg);
        resultHandler.handle(Future.succeededFuture());
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.impl.IQueryRambler#stop(de.braintags.io.vertx.pojomapper.
   * dataaccess .query.IFieldParameter)
   */
  @Override
  public void stop(IFieldParameter<?> fieldParameter) {
    // no use for Mongo
  }

  private void add(String key, Object objectToAdd) {
    if (currentObject instanceof JsonObject) {
      ((JsonObject) currentObject).put(key, objectToAdd);
    } else if (currentObject instanceof JsonArray) {
      JsonObject ob = new JsonObject().put(key, objectToAdd);
      ((JsonArray) currentObject).add(ob);
    } else
      throw new UnsupportedOperationException("no definition to add for " + currentObject.getClass().getName());
  }

}
