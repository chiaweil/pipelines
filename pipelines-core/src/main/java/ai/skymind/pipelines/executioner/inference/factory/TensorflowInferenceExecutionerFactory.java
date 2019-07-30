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
import ai.skymind.pipelines.executioner.inference.TensorflowInferenceExecutioner;
import ai.skymind.pipelines.model.TensorFlowConfig;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowGraphHolder;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.threadpool.tensorflow.conversion.graphrunner.GraphRunner;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.base.Preconditions;

import java.util.List;

@Slf4j
public class TensorflowInferenceExecutionerFactory implements InferenceExecutionerFactory {

    @Override
    public InitializedInferenceExecutionerConfig create(ModelPipelineStep modelPipelineStepConfig) throws Exception {
        TensorFlowConfig tensorFlowConfig = null;
        try {
            tensorFlowConfig = (TensorFlowConfig) modelPipelineStepConfig.getModelConfig();
        } catch (Exception e) {
            log.error("Could not extract TensorFlowConfig. Did you provide one to your verticle?");
        }

        log.debug("Loading model loader from configuration " + tensorFlowConfig);
        TensorflowModelLoader tensorflowModelLoader = TensorflowModelLoader.createFromConfig(modelPipelineStepConfig);
        TensorflowInferenceExecutioner inferenceExecutioner = new TensorflowInferenceExecutioner();
        Preconditions.checkNotNull(modelPipelineStepConfig.getServingConfig(),"No serving config found on model pipeline step!");
        inferenceExecutioner.initialize(tensorflowModelLoader, modelPipelineStepConfig.getServingConfig().getParallelInferenceConfig());

        /**
         * Automatically infer from model
         */
        TensorflowGraphHolder computationGraph = tensorflowModelLoader.loadModel();
        log.debug("Created model loader with inputs " + computationGraph.getInputNames() + " and output names " + computationGraph.getOutputNames());

        GraphRunner graphRunner = computationGraph.createRunner();
        List<String> inputNames = graphRunner.getInputOrder();
        List<String> outputNames = graphRunner.getOutputOrder();
        if (inputNames == null) {
            throw new IllegalStateException("No input names found from configuration!");
        }


        if (outputNames == null) {
            throw new IllegalStateException("No output names found from configuration!");
        }


        graphRunner.close();
        return new InitializedInferenceExecutionerConfig(inferenceExecutioner,inputNames,outputNames);
    }
}
