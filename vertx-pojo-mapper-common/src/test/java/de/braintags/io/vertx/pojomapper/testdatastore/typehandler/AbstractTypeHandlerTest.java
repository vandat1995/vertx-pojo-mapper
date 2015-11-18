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
package de.braintags.io.vertx.pojomapper.testdatastore.typehandler;

import java.util.Iterator;

import org.junit.Test;

import de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWriteEntry;
import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.mapping.IMapper;
import de.braintags.io.vertx.pojomapper.mapping.IStoreObject;
import de.braintags.io.vertx.pojomapper.testdatastore.DatastoreBaseTest;
import de.braintags.io.vertx.pojomapper.testdatastore.ResultContainer;
import de.braintags.io.vertx.pojomapper.testdatastore.mapper.typehandler.BaseRecord;
import de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public abstract class AbstractTypeHandlerTest extends DatastoreBaseTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(AbstractTypeHandlerTest.class);

  @Test
  public final void testTypeHandler(TestContext context) {
    BaseRecord record = createInstance(null);
    IMapper mapper = getDataStore().getMapperFactory().getMapper(record.getClass());
    IField field = mapper.getField(getTestFieldName());
    ITypeHandler th = field.getTypeHandler();
    context.assertNotNull(th);
    String typeHandlerName = datastoreContainer.getExpectedTypehandlerName(getClass(),
        getExpectedTypeHandlerClassName());
    context.assertEquals(typeHandlerName, th.getClass().getName());
  }

  /**
   * Get the name of the field, which for the {@link ITypeHandler} shall be checked
   * 
   * @return the name of the field
   */
  protected abstract String getTestFieldName();

  /**
   * Get the classname of the expected {@link ITypeHandler}
   * 
   * @return the classname
   */
  protected abstract String getExpectedTypeHandlerClassName();

  @Test
  public void testSaveAndReadRecord(TestContext context) {
    BaseRecord record = createInstance(context);
    dropTables(context, record);

    ResultContainer resultContainer = saveRecord(context, record);
    if (resultContainer.assertionError != null)
      throw resultContainer.assertionError;
    Iterator<IWriteEntry> it = resultContainer.writeResult.iterator();
    while (it.hasNext()) {
      IStoreObject<?> entry = it.next().getStoreObject();
      LOGGER.info("written entry: " + entry.toString());
    }

    // SimpleQuery for all records
    IQuery<? extends BaseRecord> query = getDataStore().createQuery(record.getClass());
    resultContainer = find(context, query, 1);
    if (resultContainer.assertionError != null)
      throw resultContainer.assertionError;

    resultContainer.queryResult.iterator().next(result -> {
      if (result.failed()) {
        result.cause().printStackTrace();
      } else {
        context.assertTrue(record.equals(result.result()));
        LOGGER.info("finished!");
      }
    });

  }

  protected void dropTables(TestContext context, BaseRecord record) {
    dropTable(context, record.getClass().getSimpleName());
    dropTable(context, "SimpleMapper");
  }

  /**
   * Create the instance of
   * 
   * @param context
   *          TODO
   * 
   * @return
   */
  public abstract BaseRecord createInstance(TestContext context);
}
