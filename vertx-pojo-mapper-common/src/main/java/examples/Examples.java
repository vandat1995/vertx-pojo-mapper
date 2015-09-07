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
package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.docgen.Source;
import de.braintags.io.vertx.pojomapper.IDataStore;
import de.braintags.io.vertx.pojomapper.dataaccess.delete.IDelete;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryResult;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWrite;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWriteEntry;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWriteResult;
import examples.mapper.SimpleMapper;

/**
 * Simple example to write and read Pojos
 * 
 * @author Michael Remme
 * 
 */

@Source(translate = false)
public class Examples {
  private static final Logger logger = LoggerFactory.getLogger(Examples.class);

  /**
   * Init a MongoClient onto a locally running Mongo and the {@link dataStore}
   * 
   * @param vertx
   */
  public void example1(Vertx vertx) {
    JsonObject config = new JsonObject();
    config.put("connection_string", "mongodb://localhost:27017");
    config.put("db_name", "PojongoTestDatabase");
    // MongoClient mongoClient = MongoClient.createNonShared(vertx, config);
    // dataStore dataStore = new dataStore(mongoClient);
  }

  /**
   * Create the object to be saved into the datastore
   */
  public void example2() {
    SimpleMapper dm = new SimpleMapper();
    dm.setName("SimpleMapper");
    dm.number = 20;
  }

  /**
   * Saving an instance intp the Datastore
   * 
   * @param dataStore
   * @param dm
   */
  public void example3(IDataStore dataStore, SimpleMapper dm) {
    IWrite<SimpleMapper> write = dataStore.createWrite(SimpleMapper.class);
    write.add(dm);
    write.save(result -> {
      if (result.failed()) {
        logger.error(result.cause());
      } else {
        IWriteResult wr = result.result();
        IWriteEntry entry = wr.iterator().next();
        logger.info("written with id " + entry.getId());
        logger.info("written action: " + entry.getAction());
        logger.info("written as " + entry.getStoreObject());
      }
    });
  }

  /**
   * Searching for objects
   * 
   * @param dataStore
   */
  public void example4(IDataStore dataStore) {
    IQuery<SimpleMapper> query = dataStore.createQuery(SimpleMapper.class);
    query.field("name").is("SimpleMapper");
    query.execute(rResult -> {
      if (rResult.failed()) {
        logger.error(rResult.cause());
      } else {
        IQueryResult<SimpleMapper> qr = rResult.result();
        qr.iterator().next(itResult -> {
          if (itResult.failed()) {
            logger.error(itResult.cause());
          } else {
            SimpleMapper readMapper = itResult.result();
            logger.info("Query found id " + readMapper.id);
          }
        });
      }
    });
  }

  /**
   * Delete an instance from the Datastore
   * 
   * @param dataStore
   * @param mapper
   */
  public void example5(IDataStore dataStore, SimpleMapper mapper) {
    IDelete<SimpleMapper> delete = dataStore.createDelete(SimpleMapper.class);
    delete.add(mapper);
    delete.delete(deleteResult -> {
      if (deleteResult.failed()) {
        logger.error("", deleteResult.cause());
      } else {
        logger.info(deleteResult.result().getOriginalCommand());
      }
    });
  }

  public void example6(IDataStore dataStore) {
    IQuery<SimpleMapper> query = dataStore.createQuery(SimpleMapper.class);
    query.field("name").is("test");
    IDelete<SimpleMapper> delete = dataStore.createDelete(SimpleMapper.class);
    delete.setQuery(query);
    delete.delete(deleteResult -> {
      if (deleteResult.failed()) {
        logger.error("", deleteResult.cause());
      } else {
        logger.info(deleteResult.result().getOriginalCommand());
      }
    });

  }

}
