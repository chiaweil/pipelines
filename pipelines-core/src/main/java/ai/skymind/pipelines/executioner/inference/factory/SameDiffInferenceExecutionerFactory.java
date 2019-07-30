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
import ai.skymind.pipelines.executioner.inference.SameDiffInferenceExecutioner;
import ai.skymind.pipelines.model.ModelConfig;
import ai.skymind.pipelines.model.ModelConfigType;
import ai.skymind.pipelines.model.SameDiffConfig;
import ai.skymind.pipelines.model.loader.samediff.SameDiffModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.ServingConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class SameDiffInferenceExecutionerFactory implements InferenceExecutionerFactory {
    @Override
    public InitializedInferenceExecutionerConfig create(ModelPipelineStep modelPipelineStepConfig) throws Exception {
        SameDiffConfig sameDiffconfig = null;
        ModelConfig inferenceConfiguration = modelPipelineStepConfig.getModelConfig();
        ServingConfig servingConfig = modelPipelineStepConfig.getServingConfig();

        try {
            sameDiffconfig = (SameDiffConfig) inferenceConfiguration;
        } catch (Exception e) {
            log.error("Could not extract SameDiffConfig. Did you provide one to your verticle?");
        }

        ModelConfigType modelLoadingConfig2 = inferenceConfiguration.getModelConfigType();

        SameDiffModelLoader modelLoader;
        String modelPath = null;
        if (modelLoadingConfig2 != null) {
            if (modelLoadingConfig2.getModelLoadingPath() != null) {
                modelPath = modelLoadingConfig2.getModelLoadingPath();
            }
        }

        modelLoader = new SameDiffModelLoader(new File(modelPath), modelPipelineStepConfig.getInputNames(), modelPipelineStepConfig.getOutputNames());
        SameDiffInferenceExecutioner inferenceExecutioner = new SameDiffInferenceExecutioner();
        inferenceExecutioner.initialize(modelLoader, servingConfig.getParallelInferenceConfig());
        List<String> inputNames = modelPipelineStepConfig.getInputNames();
        List<String> outputNames = modelPipelineStepConfig.getOutputNames();
        if (inputNames.isEmpty() || outputNames.isEmpty()) {
            throw new IllegalStateException("Input and output names are empty!");
        }



        return new InitializedInferenceExecutionerConfig(inferenceExecutioner,inputNames,outputNames);
    }
}
