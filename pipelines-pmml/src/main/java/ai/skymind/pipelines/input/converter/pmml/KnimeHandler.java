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

package ai.skymind.pipelines.input.converter.pmml;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

public class KnimeHandler extends BasePmmlHandler {
    @Override
    public Buffer getPmmlBuffer(RoutingContext routingContext, Object... otherInputs) {
        return null;
    }

    @Override
    public Object[] getExtraArgs(RoutingContext req) {
        return new Object[0];
    }
}
