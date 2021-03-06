/*
 * Copyright 2016, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hive.frequencies;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;

import com.yahoo.sketches.ArrayOfStringsSerDe;

@Description(name = "Union", value = "_FUNC_(sketch) - "
    + "Returns an ItemsSketch<String> in a serialized form as a binary blob."
    + " Input values must also be serialized sketches.")
public class UnionStringsSketchUDAF extends UnionItemsSketchUDAF<String> {

  @Override
  GenericUDAFEvaluator createEvaluator() {
    return new UnionStringsSketchEvaluator();
  }

  static class UnionStringsSketchEvaluator extends UnionItemsSketchEvaluator<String> {

    UnionStringsSketchEvaluator() {
      super(new ArrayOfStringsSerDe());
    }

  }

}
