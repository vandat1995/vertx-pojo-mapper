/*
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

package de.braintags.io.vertx.pojomapper.dataaccess.impl;

import java.util.ArrayList;
import java.util.List;

import de.braintags.io.vertx.pojomapper.IDataStore;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWrite;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */

public abstract class AbstractWrite<T> extends AbstractDataAccessObject<T> implements IWrite<T> {
  private List<T> objectsToSave = new ArrayList<T>();

  /**
   * @param mapperClass
   * @param datastore
   */
  public AbstractWrite(final Class<T> mapperClass, IDataStore datastore) {
    super(mapperClass, datastore);
  }

  /**
   * Get the objects that shall be saved
   * 
   * @return the objectsToSave
   */
  protected final List<T> getObjectsToSave() {
    return objectsToSave;
  }

  @Override
  public final void add(T mapper) {
    objectsToSave.add(mapper);
  }

}