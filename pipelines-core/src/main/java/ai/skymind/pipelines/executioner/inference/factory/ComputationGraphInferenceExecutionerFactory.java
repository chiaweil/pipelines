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
import ai.skymind.pipelines.model.ModelConfig;
import ai.skymind.pipelines.model.loader.dl4j.cg.ComputationGraphModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.ServingConfig;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.graph.ComputationGraph;

import java.io.File;
import java.util.List;

@Slf4j
public class ComputationGraphInferenceExecutionerFactory implements InferenceExecutionerFactory {

    @Override
    public InitializedInferenceExecutionerConfig create(ModelPipelineStep modelPipelineStepConfig) throws Exception {
        ModelConfig inferenceConfiguration = modelPipelineStepConfig.getModelConfig();
        ServingConfig servingConfig = modelPipelineStepConfig.getServingConfig();

        ComputationGraphModelLoader computationGraphModelLoader = new ComputationGraphModelLoader(new File(inferenceConfiguration.getModelConfigType().getModelLoadingPath()));
        MultiComputationGraphInferenceExecutioner inferenceExecutioner = new MultiComputationGraphInferenceExecutioner();
        inferenceExecutioner.initialize(computationGraphModelLoader, servingConfig.getParallelInferenceConfig());

        ComputationGraph computationGraph2 = computationGraphModelLoader.loadModel();
        List<String> inputNames = computationGraph2.getConfiguration().getNetworkInputs();
        List<String> outputNames = computationGraph2.getConfiguration().getNetworkOutputs();
        log.info("Loaded computation graph with input names " + inputNames + " and output names " + outputNames);

        return InitializedInferenceExecutionerConfig.builder()
                .inferenceExecutioner(inferenceExecutioner)
                .inputNames(inputNames).outputNames(outputNames)
                .build();
    }
}
