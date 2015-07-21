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

package de.braintags.io.vertx.pojomapper.mapping.impl;

import de.braintags.io.vertx.pojomapper.annotation.field.Referenced;

/**
 * An object reference is used to store a reference to an instance, which is annotated with {@link Referenced}
 * 
 * @author Michael Remme
 * 
 */

public class ObjectReference {
  private Object reference;

  /**
   * 
   */
  public ObjectReference() {
  }

  /**
   * 
   */
  public ObjectReference(Object reference) {
    this.reference = reference;
  }

  /**
   * @return the reference
   */
  public final Object getReference() {
    return reference;
  }

  /**
   * @param reference
   *          the reference to set
   */
  public final void setReference(Object reference) {
    this.reference = reference;
  }

}