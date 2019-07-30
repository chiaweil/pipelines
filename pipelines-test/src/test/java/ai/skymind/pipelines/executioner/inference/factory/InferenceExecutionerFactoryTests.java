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

package ai.skymind.pipelines.executioner.inference.factory;

import ai.skymind.pipelines.executioner.inference.*;
import ai.skymind.pipelines.executioner.inference.factory.ComputationGraphInferenceExecutionerFactory;
import ai.skymind.pipelines.executioner.inference.factory.MultiLayerNetworkInferenceExecutionerFactory;
import ai.skymind.pipelines.executioner.inference.factory.SameDiffInferenceExecutionerFactory;
import ai.skymind.pipelines.executioner.inference.factory.TensorflowInferenceExecutionerFactory;
import ai.skymind.pipelines.model.*;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowGraphHolder;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.ServingConfig;
import ai.skymind.pipelines.threadpool.tensorflow.conversion.graphrunner.GraphRunner;
import ai.skymind.pipelines.train.TrainUtils;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.Test;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.primitives.Pair;

import java.io.File;

import static org.junit.Assert.*;

public class InferenceExecutionerFactoryTests {
    
    @Test
    public void testTensorflow() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("inference/tensorflow/frozen_model.pb");
        TensorflowInferenceExecutionerFactory tensorflowInferenceExecutionerFactory = new TensorflowInferenceExecutionerFactory();
        TensorFlowConfig tensorFlowConfig = TensorFlowConfig.builder()
                .modelConfigType(ModelConfigType.tensorFlow(classPathResource.getFile().getAbsolutePath()))
                .tensorDataTypesConfig(TensorDataTypesConfig.builder()
                        .inputDataType("default", TensorDataType.INT32).build())
                .configProtoPath(classPathResource.getFile().getAbsolutePath()).build();
        
        ServingConfig servingConfig = ServingConfig.builder()
                .httpPort(1139)
                .build();
        
        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("default")
                .outputName("output")
                .servingConfig(servingConfig)
                .modelConfig(tensorFlowConfig)
                .build();
        
        TensorflowModelLoader tensorflowModelLoader = TensorflowModelLoader.createFromConfig(modelPipelineStep);
        assertFalse(tensorflowModelLoader.getInputNames().isEmpty());
        assertFalse(tensorflowModelLoader.getOutputNames().isEmpty());
        assertFalse(tensorflowModelLoader.getCastingInputTypes().isEmpty());
        assertTrue(tensorflowModelLoader.getCastingOutputTypes().isEmpty());
        
        InitializedInferenceExecutionerConfig initializedInferenceExecutionerConfig = tensorflowInferenceExecutionerFactory.create(modelPipelineStep);
        InferenceExecutioner inferenceExecutioner = initializedInferenceExecutionerConfig.getInferenceExecutioner();
        assertNotNull(inferenceExecutioner);
        
        TensorflowInferenceExecutioner tensorflowInferenceExecutioner = (TensorflowInferenceExecutioner) inferenceExecutioner;
        assertNotNull(tensorflowInferenceExecutioner.model());
        assertNotNull(tensorflowInferenceExecutioner.modelLoader());
        assertNotNull(tensorflowInferenceExecutioner.getTensorflowThreadPool());
        
        TensorflowGraphHolder tensorflowGraphHolder =  tensorflowInferenceExecutioner.model();
        assertNotNull(tensorflowGraphHolder.getCastingInputTypes());
        assertFalse(tensorflowGraphHolder.getCastingInputTypes().isEmpty());
        assertNotNull(tensorflowGraphHolder.getTfGraph());
        
        GraphRunner create = tensorflowGraphHolder.createRunner();
        assertNotNull(create.getInputDataTypes());
        assertFalse(create.getInputDataTypes().isEmpty());
        assertEquals(create.getInputDataTypes().keySet().iterator().next(),create.getInputOrder().iterator().next());
    }
    
    @Test
    public void testKerasSequential() throws Exception  {
        ClassPathResource classPathResource = new ClassPathResource("inference/keras/bidirectional_lstm_tensorflow_1.h5");
        ModelConfig modelConfig = ModelConfig.builder()
                .modelConfigType(ModelConfigType.keras(classPathResource.getFile().getAbsolutePath()))
                .build();

        ServingConfig servingConfig = ServingConfig.builder()
                .httpPort(1139)
                .build();

        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("default")
                .outputName("output")
                .servingConfig(servingConfig)
                .modelConfig(modelConfig)
                .build();

        MultiLayerNetworkInferenceExecutionerFactory factory = new MultiLayerNetworkInferenceExecutionerFactory();
        InitializedInferenceExecutionerConfig initializedInferenceExecutionerConfig = factory.create(modelPipelineStep);
        MultiLayerNetworkInferenceExecutioner computationGraphInferenceExecutioner = (MultiLayerNetworkInferenceExecutioner) initializedInferenceExecutionerConfig.getInferenceExecutioner();
        assertNotNull(computationGraphInferenceExecutioner);
        assertNotNull(computationGraphInferenceExecutioner.model());
        assertNotNull(computationGraphInferenceExecutioner.modelLoader());

    }



    @Test
    public void testSameDiff() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("inference/tensorflow/frozen_model.pb");
        SameDiffInferenceExecutionerFactory tensorflowInferenceExecutionerFactory = new SameDiffInferenceExecutionerFactory();
        SameDiffConfig tensorFlowConfig = SameDiffConfig.builder()
                .modelConfigType(ModelConfigType.sameDiff(classPathResource.getFile().getAbsolutePath()))
                .tensorDataTypesConfig(TensorDataTypesConfig.builder()
                        .inputDataType("default", TensorDataType.INT32).build())
                .build();

        ServingConfig servingConfig = ServingConfig.builder()
                .httpPort(1139)
                .build();

        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("default")
                .outputName("output")
                .servingConfig(servingConfig)
                .modelConfig(tensorFlowConfig)
                .build();


        InitializedInferenceExecutionerConfig initializedInferenceExecutionerConfig = tensorflowInferenceExecutionerFactory.create(modelPipelineStep);
        InferenceExecutioner inferenceExecutioner = initializedInferenceExecutionerConfig.getInferenceExecutioner();
        assertNotNull(inferenceExecutioner);

        SameDiffInferenceExecutioner tensorflowInferenceExecutioner = (SameDiffInferenceExecutioner) inferenceExecutioner;
        assertNotNull(tensorflowInferenceExecutioner.model());
        assertNotNull(tensorflowInferenceExecutioner.modelLoader());

    }

    @Test
    public void testMultiLayerNetwork() throws Exception {
        Pair<MultiLayerNetwork, DataNormalization> trainedNetwork = TrainUtils.getTrainedNetwork();
        MultiLayerNetwork save = trainedNetwork.getLeft();
        File tmpZip = new File("tmpmodelmln.zip");
        tmpZip.deleteOnExit();
        ModelSerializer.writeModel(save, tmpZip, true);
        ModelConfig modelConfig = ModelConfig.builder()
                .modelConfigType(ModelConfigType.multiLayerNetwork(tmpZip.getAbsolutePath()))
                .build();

        ServingConfig servingConfig = ServingConfig.builder()
                .httpPort(1139)
                .build();

        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("default")
                .outputName("output")
                .servingConfig(servingConfig)
                .modelConfig(modelConfig)
                .build();

        MultiLayerNetworkInferenceExecutionerFactory factory = new MultiLayerNetworkInferenceExecutionerFactory();
        InitializedInferenceExecutionerConfig initializedInferenceExecutionerConfig = factory.create(modelPipelineStep);
        ai.skymind.pipelines.executioner.inference.MultiLayerNetworkInferenceExecutioner computationGraphInferenceExecutioner = (MultiLayerNetworkInferenceExecutioner) initializedInferenceExecutionerConfig.getInferenceExecutioner();
        assertNotNull(computationGraphInferenceExecutioner);
        assertNotNull(computationGraphInferenceExecutioner.model());
        assertNotNull(computationGraphInferenceExecutioner.modelLoader());

    }
    
    @Test
    public void testComputationGraph() throws Exception {
        Pair<MultiLayerNetwork, DataNormalization> trainedNetwork = TrainUtils.getTrainedNetwork();
        ComputationGraph save = trainedNetwork.getLeft().toComputationGraph();
        File tmpZip = new File("tmpmodel.zip");
        tmpZip.deleteOnExit();
        ModelSerializer.writeModel(save, tmpZip, true);
        ModelConfig modelConfig = ModelConfig.builder()
                .modelConfigType(ModelConfigType.computationGraph(tmpZip.getAbsolutePath()))
                .build();
        
        ServingConfig servingConfig = ServingConfig.builder()
                .httpPort(1139)
                .build();
        
        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("default")
                .outputName("output")
                .servingConfig(servingConfig)
                .modelConfig(modelConfig)
                .build();

        ComputationGraphInferenceExecutionerFactory factory = new ComputationGraphInferenceExecutionerFactory();
        InitializedInferenceExecutionerConfig initializedInferenceExecutionerConfig = factory.create(modelPipelineStep);
        MultiComputationGraphInferenceExecutioner computationGraphInferenceExecutioner = (MultiComputationGraphInferenceExecutioner) initializedInferenceExecutionerConfig.getInferenceExecutioner();
        assertNotNull(computationGraphInferenceExecutioner);
        assertNotNull(computationGraphInferenceExecutioner.model());
        assertNotNull(computationGraphInferenceExecutioner.modelLoader());


    }
    
}
