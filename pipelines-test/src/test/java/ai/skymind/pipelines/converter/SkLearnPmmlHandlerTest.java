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

package ai.skymind.pipelines.converter;

public class SkLearnPmmlHandlerTest {




    //Currently doesn't work. See: https://github.com/jpmml/jpmml-evaluator/issues/96
    @org.junit.Test
    @org.junit.Ignore
    public void testPreocessingOnlyPmmlHandler() throws Exception {
        ai.skymind.pipelines.input.converter.pmml.SkLearnPmmlHandler skLearnPmmlHandler = new ai.skymind.pipelines.input.converter.pmml.SkLearnPmmlHandler();
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource("/tfidf/python/pipeline.pkl.z");
        // TODO file does not exist
        java.io.File f = new java.io.File("../model-server-pmml/src/test/resources/tfidf/python/pipeline.pkl.z");
        io.vertx.core.buffer.Buffer pmmlBuffer = skLearnPmmlHandler.getPmmlBuffer(null, f);
        System.out.println(pmmlBuffer);
    }

}
