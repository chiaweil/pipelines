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

package ai.skymind.pipelines.inference;

import ai.skymind.pipelines.executioner.Pipeline;
import ai.skymind.pipelines.model.PythonConfig;
import ai.skymind.pipelines.pipeline.PythonPipelineStep;
import ai.skymind.pipelines.pipeline.TransformProcessPipelineStep;
import ai.skymind.pipelines.pipeline.steps.PythonPipelineStepRunner;
import ai.skymind.pipelines.pipeline.steps.TransformProcessPipelineStepRunner;
import ai.skymind.pipelines.serving.SchemaType;
import ai.skymind.pipelines.util.python.PythonVariables.Type;
import org.datavec.api.records.Record;
import org.datavec.api.transform.MathOp;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.NDArrayWritable;
import org.datavec.api.writable.Writable;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@NotThreadSafe
@org.junit.Ignore
public class PythonPipelineTests {

    
    @Test
    public void testPipeline() throws Exception {
        Schema schema = new Schema.Builder()
                .addColumnNDArray("first",new long[]{1,1})
                .build();
        
        
        
        PythonConfig pythonConfig = PythonConfig.builder()
                .pythonCode("first += 2")
                .pythonInput("first", Type.NDARRAY.name())
                .pythonOutput("first", Type.NDARRAY.name())
                .returnAllInputs(false)
                .build();
        
        PythonPipelineStep config = PythonPipelineStep.builder()
                .inputName("default")
                .inputColumnName("default", Arrays.asList(new String[]{"first"}))
                .inputSchema("default", new SchemaType[]{SchemaType.NDArray})
                .pythonConfig("default",pythonConfig)
                .build();
        
        PythonPipelineStepRunner pythonPipelineStep = new PythonPipelineStepRunner(config);
        
        
        TransformProcess transformProcess = new TransformProcess.Builder(schema)
                .ndArrayScalarOpTransform("first", MathOp.Add,1.0)
                .build();
        
        
        TransformProcessPipelineStep transformProcessPipelineStepConfig =
                TransformProcessPipelineStep.builder()
                        .inputName("default")
                        .inputColumnName("default",Arrays.asList(new String[]{"first"}))
                        .inputSchema("default", new SchemaType[]{SchemaType.NDArray})
                        .outputSchema("default",new SchemaType[]{SchemaType.NDArray})
                        .transformProcess("default", transformProcess)
                        .build();
        
        TransformProcessPipelineStepRunner transformProcessPipelineStep = new TransformProcessPipelineStepRunner(transformProcessPipelineStepConfig);
        
        
        List<Writable> record = new ArrayList<>();
        
        record.add(new NDArrayWritable(Nd4j.scalar(1.0)));
        org.datavec.api.records.impl.Record record1 = new org.datavec.api.records.impl.Record(record,null);
        Pipeline pipeline = Pipeline.builder()
                .steps(Arrays.asList(pythonPipelineStep,transformProcessPipelineStep))
                .build();
        
        INDArray[] indArrays = pipeline.doPipelineArrays(new Record[]{record1});
        assertEquals(1,indArrays.length);
        assertEquals(Nd4j.scalar(4.0),indArrays[0]);
        
        Pipeline pipeline1 = Pipeline.getPipeline(Arrays.asList(config, transformProcessPipelineStepConfig));
        INDArray[] indArrays2 = pipeline1.doPipelineArrays(new Record[]{record1});
        assertEquals(1,indArrays2.length);
        assertEquals(Nd4j.scalar(4.0),indArrays2[0]);
        
    }
    
}
