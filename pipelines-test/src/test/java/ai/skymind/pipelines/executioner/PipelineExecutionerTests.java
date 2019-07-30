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

package ai.skymind.pipelines.executioner;

import ai.skymind.pipelines.InferenceConfiguration;
import ai.skymind.pipelines.executioner.PipelineExecutioner;
import ai.skymind.pipelines.model.*;
import ai.skymind.pipelines.pipeline.ImageLoading;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.pipeline.ObjectDetectionConfig;
import ai.skymind.pipelines.serving.Input;
import ai.skymind.pipelines.serving.Output;
import ai.skymind.pipelines.serving.ParallelInferenceConfig;
import ai.skymind.pipelines.serving.ServingConfig;
import org.junit.Test;
import org.nd4j.linalg.io.ClassPathResource;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class PipelineExecutionerTests {
    
    
    @Test
    public void testInitPipeline() throws Exception {
        ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();
        int port = 1111;
        System.out.println("Started on port " + port);
        String path = new ClassPathResource("inference/tensorflow/mnist/lenet_frozen.pb").getFile().getAbsolutePath();

        TensorDataTypesConfig tensorDataTypesConfig = TensorDataTypesConfig.builder()
                .inputDataType("image_tensor",TensorDataType.INT64)
                .build();
        
        
        TensorFlowConfig modelConfig = TensorFlowConfig.builder()
                .tensorDataTypesConfig(tensorDataTypesConfig)
                .modelConfigType(
                        ModelConfigType.builder()
                                .modelLoadingPath(path)
                                .modelType(ModelConfig.ModelType.TENSORFLOW)
                                .build()
                )                .build();
        
        
        ServingConfig servingConfig = ServingConfig.builder()
                .parallelInferenceConfig(parallelInferenceConfig)
                .predictionType(Output.PredictionType.RAW)
                .inputDataType(Input.DataType.IMAGE)
                .httpPort(port)
                .build();
        
        ModelPipelineStep modelStepConfig = ModelPipelineStep.builder()
                .inputNames(Arrays.asList(new String[]{"image_tensor"}))
                .outputNames(Arrays.asList(new String[]{"detection_classes"}))
                .modelConfig(modelConfig)
                .servingConfig(servingConfig)
                .build();
        
        
        
        InferenceConfiguration configuration = InferenceConfiguration.builder()
                .pipelineStep(modelStepConfig)
                .servingConfig(servingConfig)
                .build();
        
        PipelineExecutioner pipelineExecutioner = new PipelineExecutioner(configuration);
        pipelineExecutioner.init();
        assertNotNull("Input names should not be null.",pipelineExecutioner.inputNames());
        assertNotNull("Output names should not be null.",pipelineExecutioner.outputNames());
        assertNotNull(pipelineExecutioner.inputDataTypes);
        assertFalse(pipelineExecutioner.inputDataTypes.isEmpty());
        assertEquals(TensorDataType.INT64,pipelineExecutioner.inputDataTypes.get("image_tensor"));
    }
    
    @Test
    public void testInitPipelineYolo() throws Exception {
        ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();
        int port = 1111;
        System.out.println("Started on port " + port);
        String path = new ClassPathResource("inference/tensorflow/mnist/lenet_frozen.pb").getFile().getAbsolutePath();
        
        TensorDataTypesConfig tensorDataTypesConfig = TensorDataTypesConfig.builder()
                .inputDataType("image_tensor",TensorDataType.INT64)
                .build();
        
        ObjectDetectionConfig objectRecognitionConfig = ObjectDetectionConfig
                .builder()
                .numLabels(80)
                .build();
        ImageLoading imageLoadingConfig = ImageLoading.builder()
                .inputNames(Arrays.asList(new String[]{"image_tensor"}))
                .outputNames(Arrays.asList(new String[]{"detection_classes"}))
                .objectDetectionConfig(objectRecognitionConfig)
                .build();
        
        ServingConfig servingConfig = ServingConfig.builder()
                .parallelInferenceConfig(parallelInferenceConfig)
                .inputDataType(Input.DataType.IMAGE)
                .predictionType(Output.PredictionType.YOLO)
                .outputDataType(Output.DataType.JSON)
                .httpPort(port)
                .build();
        
        TensorFlowConfig modelConfig = TensorFlowConfig.builder()
                .tensorDataTypesConfig(tensorDataTypesConfig)
                 .modelConfigType(ModelConfigType.tensorFlow(path))
                .build();
        
        ModelPipelineStep modelStepConfig = ModelPipelineStep.builder()
                 .modelConfig(modelConfig)
                  .inputNames(Arrays.asList(new String[]{"image_tensor"}))
                .outputNames(Arrays.asList(new String[]{"detection_classes"}))
             
                .servingConfig(servingConfig)
                .build();
        
        
        
        
        InferenceConfiguration configuration = InferenceConfiguration.builder()
                .servingConfig(servingConfig)
                .pipelineStep(imageLoadingConfig)
                .pipelineStep(modelStepConfig)
                .build();
        
        PipelineExecutioner pipelineExecutioner = new PipelineExecutioner(configuration);
        pipelineExecutioner.init();
        assertNotNull("Input names should not be null.",pipelineExecutioner.inputNames());
        assertNotNull("Output names should not be null.",pipelineExecutioner.outputNames());
        assertEquals(TensorDataType.INT64,pipelineExecutioner.inputDataTypes.get("image_tensor"));
        assertNotNull(pipelineExecutioner.getMultiOutputAdapter());
    }
    
    
}
