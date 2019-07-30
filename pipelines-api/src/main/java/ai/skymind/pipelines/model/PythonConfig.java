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

package ai.skymind.pipelines.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@NoArgsConstructor
@Slf4j
@SuperBuilder
public class PythonConfig extends ModelConfig {

    private String  pythonCode,pythonCodePath;
    @Singular
    private Map<String,String>  pythonInputs;
    @Singular
    private Map<String,String> pythonOutputs;
    @Singular
    private Map<String,String> extraInputs;

    private String pythonPath;

    private static String defaultPythonPath;

    private boolean returnAllInputs;
    
    @Builder
    private PythonConfig(String pythonCode, Map<String,String>  pythonInputs,
                         Map<String,String>  pythonOutputs, Map<String,String>  extraInputs,
                         String pythonPath,String pythonCodePath){
        super();
        this.pythonCode = pythonCode;
        this.pythonInputs = pythonInputs;
        this.pythonOutputs = pythonOutputs;
        this.extraInputs = extraInputs;
        this.pythonPath = pythonPath;
        this.pythonCodePath = pythonCodePath;
    }



}
