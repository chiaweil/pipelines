/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2018 Skymind, Inc.
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

import ai.skymind.pipelines.executioner.inference.InitializedInferenceExecutionerConfig;
import ai.skymind.pipelines.executioner.inference.MultiComputationGraphInferenceExecutioner;
import ai.skymind.pipelines.executioner.inference.MultiLayerNetworkInferenceExecutioner;
import ai.skymind.pipelines.model.ModelConfig;
import ai.skymind.pipelines.model.loader.ModelGuesser;
import ai.skymind.pipelines.model.loader.ModelLoader;
import ai.skymind.pipelines.model.loader.dl4j.cg.ComputationGraphModelLoader;
import ai.skymind.pipelines.model.loader.dl4j.mln.MultiLayerNetworkModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.ServingConfig;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.graph.ComputationGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class KerasInferenceExecutionerFactory implements InferenceExecutionerFactory {

    @Override
    public InitializedInferenceExecutionerConfig create(ModelPipelineStep modelPipelineStepConfig) throws Exception {
        ModelLoader kerasLoader;
        ModelConfig inferenceConfiguration = modelPipelineStepConfig.getModelConfig();
        ServingConfig servingConfig = modelPipelineStepConfig.getServingConfig();
        if (ModelGuesser.isKerasComputationGraphFile(new File(inferenceConfiguration.getModelConfigType().getModelLoadingPath()))) {
            MultiComputationGraphInferenceExecutioner inferenceExecutioner = new MultiComputationGraphInferenceExecutioner();
            kerasLoader = new ComputationGraphModelLoader(new File(inferenceConfiguration.getModelConfigType().getModelLoadingPath()));
            ComputationGraph model = (ComputationGraph) kerasLoader.loadModel();
            int numInputs = model.getNumInputArrays();
            int numOutputs = model.getNumOutputArrays();
            List<String> inputNames = new ArrayList<>();
            for (int i = 0; i < numInputs; inputNames.add(String.valueOf(i++))) ;
            List<String> outputNames = new ArrayList<>();
            for (int i = 0; i < numOutputs; outputNames.add(String.valueOf(i++))) ;
            inferenceExecutioner.initialize(kerasLoader, servingConfig.getParallelInferenceConfig());

            log.info("Keras model loaded with inputs " + inputNames + " and output names " + outputNames);
            return new InitializedInferenceExecutionerConfig(inferenceExecutioner,inputNames,outputNames);
        } else {
            MultiLayerNetworkInferenceExecutioner inferenceExecutioner = new MultiLayerNetworkInferenceExecutioner();
            kerasLoader = new MultiLayerNetworkModelLoader(new File(inferenceConfiguration.getModelConfigType().getModelLoadingPath()));
            List<String> inputNames = Collections.singletonList("default");
            List<String> outputNames = Collections.singletonList("default");
            inferenceExecutioner.initialize(kerasLoader, servingConfig.getParallelInferenceConfig());
            return new InitializedInferenceExecutionerConfig(inferenceExecutioner,inputNames,outputNames);

        }
    }
}
