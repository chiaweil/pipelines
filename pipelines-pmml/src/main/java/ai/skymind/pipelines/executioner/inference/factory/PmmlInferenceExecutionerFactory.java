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

import ai.skymind.pipelines.executioner.inference.InitializedInferenceExecutionerConfig;
import ai.skymind.pipelines.executioner.inference.PmmlInferenceExecutioner;
import ai.skymind.pipelines.model.ModelConfig;
import ai.skymind.pipelines.model.PmmlConfig;
import ai.skymind.pipelines.model.loader.pmml.PmmlModelLoader;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.ServingConfig;
import org.jpmml.evaluator.ModelEvaluatorFactory;

import java.io.File;

public class PmmlInferenceExecutionerFactory implements InferenceExecutionerFactory {

    @Override
    public InitializedInferenceExecutionerConfig create(ModelPipelineStep modelPipelineStepConfig) throws Exception {
        ModelConfig inferenceConfiguration = modelPipelineStepConfig.getModelConfig();
        ServingConfig servingConfig = modelPipelineStepConfig.getServingConfig();

        PmmlConfig pmmlConfig = (PmmlConfig) inferenceConfiguration;
        String evaluationModelFactory = pmmlConfig.evaluatorFactoryName();
        String pmmlConfigPath = inferenceConfiguration.getModelConfigType().getModelLoadingPath();
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        PmmlInferenceExecutioner inferenceExecutioner = new PmmlInferenceExecutioner();
        PmmlModelLoader modelLoader1 = new PmmlModelLoader(modelEvaluatorFactory, new File(pmmlConfigPath));
        inferenceExecutioner.initialize(modelLoader1, servingConfig.getParallelInferenceConfig());
        return new InitializedInferenceExecutionerConfig(inferenceExecutioner,null,null);
    }
}
