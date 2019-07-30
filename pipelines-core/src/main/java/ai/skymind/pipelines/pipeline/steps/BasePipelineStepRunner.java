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

package ai.skymind.pipelines.pipeline.steps;

import ai.skymind.pipelines.pipeline.PipelineStep;
import ai.skymind.pipelines.pipeline.PipelineStepRunner;
import ai.skymind.pipelines.serving.SchemaType;
import org.datavec.api.records.Record;
import org.datavec.api.writable.Writable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract  class BasePipelineStepRunner implements PipelineStepRunner {

    protected PipelineStep pipelineStep;

    public BasePipelineStepRunner(PipelineStep pipelineStep) {
        this.pipelineStep = pipelineStep;
    }


    @Override
    public Record[] transform(Record[] input) {
        int batchSize = input.length;
        Record[] ret = new Record[input.length];
        for(int example = 0; example < batchSize; example++) {
            for(int name = 0; name < pipelineStep.getInputNames().size(); name++) {
                String inputName = pipelineStep.inputNameAt(name);
                if(pipelineStep.inputNameIsValidForStep(pipelineStep.inputNameAtIndex(name))) {
                    List<Writable> currRecord;
                    if(ret[example] == null) {
                        currRecord = new ArrayList<>();
                        ret[example] = new org.datavec.api.records.impl.Record(currRecord,null);

                    }
                    else {
                        currRecord = ret[example].getRecord();
                    }

                    Writable currWritable = input[example].getRecord().get(name);
                    //Add filtering for column size equal to 1, to reduce boilerplate
                    if(pipelineStep.processColumn(inputName,name)) {
                        processValidWritable(currWritable,currRecord,name);
                    }
                    else {
                        currRecord.add(input[example].getRecord().get(name));
                    }


                }
                else {
                    ret[example] = input[example];
                }
            }

        }

        return ret;
    }


    @Override
    public Map<String, SchemaType[]> inputTypes() {
        return pipelineStep.getInputSchemas();
    }

    @Override
    public Map<String, SchemaType[]> outputTypes() {
        return pipelineStep.getOutputSchemas();
    }


    public abstract void processValidWritable(Writable writable, List<Writable> record, int inputIndex, Object... extraArgs);

}
