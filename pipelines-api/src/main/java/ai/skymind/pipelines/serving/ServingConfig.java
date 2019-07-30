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

package ai.skymind.pipelines.serving;

import ai.skymind.pipelines.metrics.MetricType;
import lombok.*;

import java.util.Arrays;
import java.util.List;

import static ai.skymind.pipelines.metrics.MetricType.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServingConfig {

    private PubsubConfig pubSubConfig;

    private int httpPort;

    @Builder.Default
    private String listenHost = "localhost";
    @Builder.Default
    private Input.DataType inputDataType = Input.DataType.JSON;
    @Builder.Default
    private Output.DataType outputDataType = Output.DataType.JSON;

    @Builder.Default
    private Output.PredictionType predictionType = Output.PredictionType.CLASSIFICATION;

    @Builder.Default
    private ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();

    @Builder.Default
    private String uploadsDirectory = "file-uploads/";

    @Builder.Default
    private boolean logTimings = false;

    @Builder.Default
    private List<MetricType> metricTypes = Arrays.asList(
            CLASS_LOADER,
            JVM_MEMORY,
            JVM_GC,
            PROCESSOR,
            JVM_THREAD,
            LOGGING_METRICS,
            NATIVE
    );

}
