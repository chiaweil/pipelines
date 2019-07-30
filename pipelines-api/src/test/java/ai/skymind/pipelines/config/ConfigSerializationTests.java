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

package ai.skymind.pipelines.config;

import ai.skymind.pipelines.model.PythonConfig;
import org.junit.Test;
import org.nd4j.jackson.objectmapper.holder.ObjectMapperHolder;

public class ConfigSerializationTests {

    @Test
    public void testSerializationPython() throws Exception {
        PythonConfig pythonConfig = PythonConfig.builder()
                .build();
        tryDeSerialize(trySerialize(pythonConfig), PythonConfig.class);


        tryDeSerialize(trySerialize(pythonConfig), PythonConfig.class);

    }




    private String trySerialize(Object o) throws Exception {
        return ObjectMapperHolder.getJsonMapper().writeValueAsString(o);
    }

    private <T> void tryDeSerialize(String input, Class<T> clazz) throws Exception {
        ObjectMapperHolder.getJsonMapper().readValue(input, clazz);
    }

}
