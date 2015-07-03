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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import de.braintags.io.vertx.pojomapper.exception.PropertyAccessException;
import de.braintags.io.vertx.pojomapper.mapping.IPropertyAccessor;

/**
 * An accessor which is used on BeanMethods of an object
 * 
 * @author Michael Remme
 * 
 */

public class JavaBeanAccessor implements IPropertyAccessor {
  private String name;
  private Method readMethod = null;
  private Method writeMethod = null;

  /**
   * 
   */
  public JavaBeanAccessor(PropertyDescriptor beanDescriptor) {
    this.name = beanDescriptor.getName();
    this.setReadMethod(beanDescriptor.getReadMethod());
    this.setWriteMethod(beanDescriptor.getWriteMethod());
  }

  @Override
  public Object readData(Object record) {
    try {
      return this.getReadMethod().invoke(record, new Object[0]);
    } catch (Exception e) {
      throw new PropertyAccessException("Cannot read data from property " + this.name, e);
    }
  }

  @Override
  public void writeData(Object record, Object data) {
    try {
      this.getWriteMethod().invoke(record, new Object[] { data });
    } catch (Exception e) {
      throw new PropertyAccessException("Cannot write data from property " + this.name, e.getCause() == null ? e
          : e.getCause());
    }
  }

  /**
   * Sets the method to be used for reading content
   */
  private void setWriteMethod(Method method) {
    this.writeMethod = method;
  }

  /**
   * Gets the method to be used for writing content
   */
  protected Method getWriteMethod() {
    return this.writeMethod;
  }

  /**
   * Gets the method to be used for reading content
   */
  protected Method getReadMethod() {
    return this.readMethod;
  }

  /**
   * Sets the method to be used for writing content
   */
  private void setReadMethod(Method method) {
    this.readMethod = method;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IPropertyAccessor#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}