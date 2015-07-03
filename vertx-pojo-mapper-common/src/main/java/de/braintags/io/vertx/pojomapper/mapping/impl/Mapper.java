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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.braintags.io.vertx.pojomapper.annotation.Entity;
import de.braintags.io.vertx.pojomapper.annotation.Indexes;
import de.braintags.io.vertx.pojomapper.annotation.ObjectFactory;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.AfterDelete;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.AfterLoad;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.AfterSave;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.BeforeDelete;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.BeforeLoad;
import de.braintags.io.vertx.pojomapper.annotation.lifecycle.BeforeSave;
import de.braintags.io.vertx.pojomapper.exception.ClassAccessException;
import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.mapping.IMapper;
import de.braintags.io.vertx.pojomapper.mapping.IObjectFactory;
import de.braintags.io.vertx.util.ClassUtil;

/**
 * This implementation of {@link IMapper} is using the bean convention to define fields, which shall be mapped. It is
 * first reading all public, non transient fields, then the bean-methods ( public getter/setter ). The way of mapping
 * can be defined by adding several annotations to the field
 * 
 * @author Michael Remme
 * 
 */

public class Mapper implements IMapper {
  private IObjectFactory objectFactory = new DefaultObjectFactory();
  private Map<String, MappedField> mappedFields = new HashMap<String, MappedField>();
  private MapperFactory mapperFactory;
  private Class<?> mapperClass;
  private Entity entity;
  private Map<Class<? extends Annotation>, IField[]> fieldCache = new HashMap<Class<? extends Annotation>, IField[]>();

  /**
   * all annotations which shall be examined for the mapper class itself
   */
  private static final List<Class<? extends Annotation>> CLASS_ANNOTATIONS = Arrays.asList(Indexes.class);

  /**
   * all annotations which shall be examined for the mapper class itself
   */
  private static final List<Class<? extends Annotation>> LIFECYCLE_ANNOTATIONS = Arrays.asList(AfterDelete.class,
      AfterLoad.class, AfterSave.class, BeforeDelete.class, BeforeLoad.class, BeforeSave.class);

  /**
   * Class annotations which were found inside the current definition
   */
  private final Map<Class<? extends Annotation>, Annotation> existingClassAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();

  /**
   * Methods which are life-cycle events. Per event there can be several methods defined
   */
  private final Map<Class<? extends Annotation>, List<Method>> lifecycleMethods = new HashMap<Class<? extends Annotation>, List<Method>>();

  /**
   * @throws Exception
   * 
   */
  public Mapper(Class<?> mapperClass, MapperFactory mapperFactory) throws Exception {
    this.mapperFactory = mapperFactory;
    this.mapperClass = mapperClass;
    init();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getObjectFactory()
   */
  @Override
  public IObjectFactory getObjectFactory() {
    return objectFactory;
  }

  private void init() throws Exception {
    computePersistentFields();
    computeLifeCycleAnnotations();
    computeClassAnnotations();
    computeEntity();
    computeObjectFactory();
  }

  private void computeEntity() {
    if (mapperClass.isAnnotationPresent(Entity.class))
      entity = mapperClass.getAnnotation(Entity.class);
  }

  private void computeObjectFactory() throws Exception {
    if (mapperClass.isAnnotationPresent(ObjectFactory.class)) {
      ObjectFactory ofAnn = mapperClass.getAnnotation(ObjectFactory.class);
      String className = ofAnn.className();
      Class<?> ofClass = Class.forName(className);
      IObjectFactory of = (IObjectFactory) ofClass.newInstance();
      this.objectFactory = of;
    }
  }

  private void computeClassAnnotations() {
    for (Class<? extends Annotation> annClass : CLASS_ANNOTATIONS) {
      Annotation ann = mapperClass.getAnnotation(annClass);
      if (ann != null)
        existingClassAnnotations.put(annClass, ann);
    }
  }

  /**
   * Computes the methods, which are annotated with the lifecycle annotations like {@link BeforeLoad}
   */
  private void computeLifeCycleAnnotations() {
    List<Method> methods = ClassUtil.getDeclaredAndInheritedMethods(mapperClass);
    for (Method method : methods) {
      for (Class<? extends Annotation> ann : LIFECYCLE_ANNOTATIONS) {
        if (method.isAnnotationPresent(ann)) {
          addLifecycleAnnotationMethod(ann, method);
        }
      }
    }
  }

  private void addLifecycleAnnotationMethod(Class<? extends Annotation> ann, Method method) {
    List<Method> lcMethods = lifecycleMethods.get(ann);
    if (lcMethods == null) {
      lcMethods = new ArrayList<Method>();
      lifecycleMethods.put(ann, lcMethods);
    }
    if (!lcMethods.contains(method))
      lcMethods.add(method);
  }

  /**
   * Compute all fields, which shall be persisted. First the public, non-transient fields are read, then the
   * bean-methods.
   */
  private void computePersistentFields() {
    computeFieldProperties();
    computeBeanProperties();
  }

  /**
   * Computes the properties in JavaBean format. Important: the bean-methods are defining the property to be used and
   * the methods are used to write and read information from an instance. Annotations for further definition of the
   * mapping are read from the underlaying field
   */
  public void computeBeanProperties() {
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(mapperClass);
      PropertyDescriptor[] beanDescriptors = beanInfo.getPropertyDescriptors();
      for (int i = 0; i < beanDescriptors.length; i++) {
        Method readMethod = beanDescriptors[i].getReadMethod();
        Method writeMethod = beanDescriptors[i].getWriteMethod();
        if (readMethod != null && writeMethod != null) {
          JavaBeanAccessor accessor = new JavaBeanAccessor(beanDescriptors[i]);
          String name = accessor.getName();
          Field field = mapperClass.getDeclaredField(name);
          mappedFields.put(name, new MappedField(field, accessor, this));
        }
      }
    } catch (IntrospectionException | NoSuchFieldException e) {
      throw new ClassAccessException("Cannot perform introspection of class", e);
    }
  }

  /**
   * Computes the properties from the public fields of the class, which are not transient
   */
  public void computeFieldProperties() {
    Field[] fieldArray = mapperClass.getFields();
    for (int i = 0; i < fieldArray.length; i++) {
      Field field = fieldArray[i];
      int fieldModifiers = field.getModifiers();
      if (!Modifier.isTransient(fieldModifiers)
          && (Modifier.isPublic(fieldModifiers) && !Modifier.isStatic(fieldModifiers))) {
        JavaFieldAccessor accessor = new JavaFieldAccessor(field);
        mappedFields.put(accessor.getName(), new MappedField(field, accessor, this));
      }
    }
  }

  /**
   * Get the {@link MapperFactory} which created the current instance
   * 
   * @return
   */
  MapperFactory getMapperFactory() {
    return mapperFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getFieldNames()
   */
  @Override
  public Set<String> getFieldNames() {
    return mappedFields.keySet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getField(java.lang.String)
   */
  @Override
  public IField getField(String name) {
    return mappedFields.get(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getMapperClass()
   */
  @Override
  public Class<?> getMapperClass() {
    return mapperClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getLifecycleMethods(java.lang.Class)
   */
  @Override
  public List<Method> getLifecycleMethods(Class<? extends Annotation> annotation) {
    return lifecycleMethods.get(annotation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getEntity()
   */
  @Override
  public Entity getEntity() {
    return this.entity;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getAnnotation(java.lang.Class)
   */
  @Override
  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) {
    return existingClassAnnotations.get(annotationClass);
  }

  /**
   * Add an Annotation, for which the Mapper shall be checked. Existing annotations of that type can be requested by
   * method {@link #getAnnotation(Class)}
   * 
   * @param annotation
   *          the Annotation class, which we are interested in
   */
  public static void addInterestingAnnotation(final Class<? extends Annotation> annotation) {
    CLASS_ANNOTATIONS.add(annotation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IMapper#getAnnotatedFields(java.lang.Class)
   */
  @Override
  public IField[] getAnnotatedFields(Class<? extends Annotation> annotationClass) {
    if (!fieldCache.containsKey(annotationClass)) {
      IField[] result = new IField[0];
      for (MappedField field : mappedFields.values()) {
        if (field.getAnnotation(annotationClass) != null) {
          IField[] newArray = new IField[result.length + 1];
          System.arraycopy(result, 0, newArray, 0, result.length);
          result = newArray;
          result[result.length - 1] = field;
        }
      }
      fieldCache.put(annotationClass, result);
    }

    IField[] result = fieldCache.get(annotationClass);
    if (result.length == 0)
      return null;
    return result;
  }

}