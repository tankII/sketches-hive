/*
 * Copyright 2016, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hive.quantiles;

import java.util.Comparator;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFParameterInfo;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;

import com.yahoo.sketches.ArrayOfItemsSerDe;

/**
 * This is a generic implementation to be specialized in subclasses
 * @param <T> type of item
 */
public abstract class DataToItemsSketchUDAF<T> extends AbstractGenericUDAFResolver {

  @Override
  public GenericUDAFEvaluator getEvaluator(final GenericUDAFParameterInfo info) throws SemanticException {
    final ObjectInspector[] inspectors = info.getParameterObjectInspectors();
    if (inspectors.length != 1 && inspectors.length != 2) {
      throw new UDFArgumentException("One or two arguments expected");
    }
    ObjectInspectorValidator.validateCategoryPrimitive(inspectors[0], 0);
    if (inspectors.length == 2) {
      ObjectInspectorValidator.validateGivenPrimitiveCategory(inspectors[1], 1, PrimitiveCategory.INT);
    }
    return createEvaluator();
  }

  public abstract GenericUDAFEvaluator createEvaluator();

  public static abstract class DataToSketchEvaluator<T> extends ItemsEvaluator<T> {

    DataToSketchEvaluator(final Comparator<? super T> comparator, final ArrayOfItemsSerDe<T> serDe) {
      super(comparator, serDe);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void iterate(final AggregationBuffer buf, final Object[] data) throws HiveException {
      if (data[0] == null) { return; }
      @SuppressWarnings("unchecked")
      final ItemsUnionState<T> state = (ItemsUnionState<T>) buf;
      if (!state.isInitialized() && kObjectInspector != null) {
        final int k = PrimitiveObjectInspectorUtils.getInt(data[1], kObjectInspector);
        state.init(k);
      }
      state.update(extractValue(data[0], inputObjectInspector));
    }

    public abstract T extractValue(final Object data, final ObjectInspector objectInspector)
        throws HiveException;

  }

}
