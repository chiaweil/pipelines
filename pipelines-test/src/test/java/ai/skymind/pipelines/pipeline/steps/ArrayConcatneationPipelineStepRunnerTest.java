/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2019 Skymind, Inc.
 *  *  *
 *  *  * This program and the accompanying materials are made available under the
 *  *  * terms of the Apache License, Version 2.0 which is available at
 *  *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  *  * License for the specific language governing permissions and limitations
 *  *  * under the License.
 *  *  *
 *  *  * SPDX-License-Identifier: Apache-2.0
 *  *  *****************************************************************************
 *
 *
 */

package ai.skymind.pipelines.pipeline.steps;

import ai.skymind.pipelines.pipeline.ArrayConcatneationStep;
import ai.skymind.pipelines.pipeline.steps.ArrayConcatneationStepRunner;
import org.datavec.api.records.Record;
import org.datavec.api.writable.NDArrayWritable;
import org.datavec.api.writable.Writable;
import org.junit.Test;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArrayConcatneationPipelineStepRunnerTest {

    @Test
    public void testArrayConcatStep() {
        ArrayConcatneationStep config = ArrayConcatneationStep.builder()
                .inputName("default")
                .inputName("input1")
                .concatDimension(0,0)
                .concatDimension(1,0)
                .build();

        ArrayConcatneationStepRunner step = new ArrayConcatneationStepRunner(config);
        Record[] input = new Record[2];
        for(int i = 0; i < input.length; i++)
            input[i] = new org.datavec.api.records.impl.Record(Arrays.asList(
                    new NDArrayWritable(Nd4j.scalar(1.0 + i))
                    ,new NDArrayWritable(Nd4j.scalar(1.0 + i))),
                    null);

        Record[] transform = step.transform(input);
        assertEquals(1,transform.length);

        List<Writable> recordContent = transform[0].getRecord();
        for(int i = 0; i < recordContent.size(); i++) {
            assertTrue(recordContent.get(i) instanceof NDArrayWritable);

            NDArrayWritable ndArrayWritable = (NDArrayWritable) recordContent.get(i);
            assertEquals(2,ndArrayWritable.get().length());
        }




    }

}
