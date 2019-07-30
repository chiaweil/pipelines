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

package ai.skymind.pipelines.pipeline;

import ai.skymind.pipelines.InferenceConfiguration;
import ai.skymind.pipelines.serving.ServingConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PipelineSerializationTests {

    @Test
    public void testFromYaml() throws Exception {
        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .pipelineStep(new PythonPipelineStep())
                .pipelineStep(new PythonPipelineStep())
                .servingConfig(ServingConfig.builder().build())
                .build();
        assertEquals(inferenceConfiguration,InferenceConfiguration.fromYaml(inferenceConfiguration.toYaml()));
    }

    @Test
    public void testPipelineSerialization() throws  Exception {
        ImageLoading imageLoadingConfig = ImageLoading.builder()
                .dimensionsConfig("default",new Long[]{426L,426L,3L})
                .dimensionsConfig("1",new Long[]{426L,426L,3L})
                .build();

        InferenceConfiguration config = InferenceConfiguration.builder()
                .pipelineStep(imageLoadingConfig)
                .pipelineStep(imageLoadingConfig)
                .build();
        System.out.println(config.toJson());

    }

}
