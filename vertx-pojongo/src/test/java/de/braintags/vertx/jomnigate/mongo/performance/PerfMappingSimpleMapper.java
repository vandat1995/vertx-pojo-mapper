/*
 * #%L
 * vertx-pojongo
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.mongo.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.braintags.vertx.jomnigate.mapping.IMapper;
import de.braintags.vertx.jomnigate.mongo.MongoDataStore;
import de.braintags.vertx.jomnigate.mongo.performance.mapper.SimpleMapper;
import de.braintags.vertx.jomnigate.testdatastore.DatastoreBaseTest;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * perform createStoreObject 50 times 50.000 instances:
 * 
 * with typehandlers active: 100 ms, 102 ms, 99 ms
 * 
 * only objecttypehandler active: 112 ms, 113 ms, 120 ms
 * 
 * removed typehandler, use jackson: 80 ms, 78 ms, 83 ms
 * 
 * 
 * @author Michael Remme
 * 
 */
public class PerfMappingSimpleMapper extends DatastoreBaseTest {
  private static final int LOOP = 50000;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public synchronized static void start(MongoDataStore ds, AtomicLong al) {
    final Long startTime = System.currentTimeMillis();
    IMapper<SimpleMapper> mapper = ds.getMapperFactory().getMapper(SimpleMapper.class);
    List<Future> fl = new ArrayList<>();
    for (int i = 0; i < LOOP; i++) {
      Future f = Future.future();
      fl.add(f);
      ds.getStoreObjectFactory().createStoreObject(mapper, new SimpleMapper(i), f.completer());
    }

    CompositeFuture cf = CompositeFuture.all(fl);
    cf.setHandler(res -> {
      if (res.failed()) {
        res.cause().printStackTrace();
      } else {
        long t = System.currentTimeMillis() - startTime;
        System.out.println(t);
        al.addAndGet(t);
      }
    });
  }

  public static void main(String[] args) {
    int loops = 50;
    Vertx vertx = Vertx.vertx();
    JsonObject config = new JsonObject();
    config.put("connection_string", "mongodb://localhost:27017");
    config.put("db_name", "PojongoTestDatabase");
    AtomicLong allTime = new AtomicLong();
    MongoClient mongoClient = MongoClient.createNonShared(vertx, config);
    MongoDataStore store = new MongoDataStore(vertx, mongoClient, config, null);
    store.getMapperFactory().getMapper(SimpleMapper.class);

    for (int i = 0; i < loops; i++) {
      start(store, allTime);
    }
    System.out.println("average: " + allTime.get() / loops);
  }

}
