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

package ai.skymind.pipelines.configprovider.args;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.converters.EnumConverter;

public class InputDataTypeConverter extends EnumConverter<InputDataType> implements IStringConverter<InputDataType> {
    /**
     * Constructs a new converter.
     *
     * @param optionName the option name for error reporting
     * @param clazz      the enum class
     */
    public InputDataTypeConverter(String optionName, Class<InputDataType> clazz) {
        super(optionName, clazz);
    }
}
