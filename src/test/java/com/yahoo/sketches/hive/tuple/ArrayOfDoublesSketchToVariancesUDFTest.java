/*
 * Copyright 2017, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hive.tuple;

import java.util.List;
import java.util.Random;

import org.apache.hadoop.io.BytesWritable;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketchBuilder;

public class ArrayOfDoublesSketchToVariancesUDFTest {

  @Test
  public void nullSketch() {
    List<Double> result = new ArrayOfDoublesSketchToVariancesUDF().evaluate(null);
    Assert.assertNull(result);
  }

  @Test
  public void emptySketch() {
    ArrayOfDoublesUpdatableSketch sketch = new ArrayOfDoublesUpdatableSketchBuilder().build();
    List<Double> result = new ArrayOfDoublesSketchToVariancesUDF().evaluate(new BytesWritable(sketch.compact().toByteArray()));
    Assert.assertNull(result);
  }

  @Test
  public void oneEntrySketch() {
    ArrayOfDoublesUpdatableSketch sketch = new ArrayOfDoublesUpdatableSketchBuilder().build();
    sketch.update(1, new double[] {1});
    List<Double> result = new ArrayOfDoublesSketchToVariancesUDF().evaluate(new BytesWritable(sketch.compact().toByteArray()));
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 1);
    Assert.assertEquals(result.get(0), 0.0);
  }

  @Test
  public void manyEntriesTwoValuesSketch() {
    ArrayOfDoublesUpdatableSketch sketch = new ArrayOfDoublesUpdatableSketchBuilder().setNumberOfValues(2).build();
    Random rand = new Random(0);
    int numKeys = 10000; // to saturate the sketch with default number of nominal entries (4K)
    for (int i = 0; i < numKeys; i++ ) {
      // two random values normally distributed with standard deviations of 1 and 10
      sketch.update(i, new double[] {rand.nextGaussian(), rand.nextGaussian() * 10.0});
    }
    Assert.assertTrue(sketch.getRetainedEntries() >= 4096);
    List<Double> result = new ArrayOfDoublesSketchToVariancesUDF().evaluate(new BytesWritable(sketch.compact().toByteArray()));
    Assert.assertNotNull(result);
    Assert.assertEquals(result.size(), 2);
    Assert.assertEquals(result.get(0), 1.0, 0.04);
    Assert.assertEquals(result.get(1), 100.0, 100.0 * 0.04); // squared standard deviation within 4%
  }

}
