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

package ai.skymind.pipelines.executioner;

import ai.skymind.pipelines.util.python.NumpyArray;
import ai.skymind.pipelines.util.python.PythonTransform;
import ai.skymind.pipelines.util.python.PythonVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bytedeco.cpython.PyObject;
import org.bytedeco.cpython.PyThreadState;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.numpy.global.numpy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ai.skymind.pipelines.util.python.PythonUtils.*;
import static org.bytedeco.cpython.global.python.*;

/**
 *  Python executioner
 *
 *  @author Fariz Rahman
 */
@Slf4j
public class PythonExecutioner {

    private static PyObject module;
    private static PyObject globals;
    private static JSONParser parser = new JSONParser();
    private final static String fileVarName = "_f" + Nd4j.getRandom().nextInt();
    private static boolean init;
    public final static String DEFAULT_PYTHON_PATH_PROPERTY = "ai.skymind.python.path";
    private static Map<String, PyThreadState> interpreters = new java.util.concurrent.ConcurrentHashMap<>();
    private static String currentInterpreter = "main";
    private static PyThreadState currentThreadState;
    private static PyThreadState mainThreadState;
    public final static String ALL_VARIABLES_KEY = "allVariables";


    /**
     * Set the python path.
     * Generally you can just use the PYTHONPATH environment variable,
     * but if you need to set it from code, this can work as well.
     */
    public static synchronized void setPythonPath() {
        if(!init) {
            try {
                String path = System.getProperty(DEFAULT_PYTHON_PATH_PROPERTY);
                if(path == null) {
                    log.info("Setting python default path");
                    File[] packages = numpy.cachePackages();
                    Py_SetPath(packages);
                }
                else {
                    log.info("Setting python path " + path);
                    Py_SetPath(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            throw new IllegalStateException("Unable to reset python path. Already initialized.");
        }
    }
    public static synchronized  void init() {
        if(init) {
            return;
        }

        log.info("CPython: PyEval_InitThreads()");
        PyEval_InitThreads();
        log.info("CPython: Py_InitializeEx()");
        Py_InitializeEx(0);
        log.info("CPython: PyGILState_Release()");
        init = true;
        interpreters.put("main", PyThreadState_Get());
        if(System.getProperty(DEFAULT_PYTHON_PATH_PROPERTY) == null) {
            numpy._import_array();
            applyPatches();
        }
    }

    public static PythonVariables defaultPythonVariableOutput() {
        PythonVariables ret = new PythonVariables();
        ret.add(ALL_VARIABLES_KEY, PythonVariables.Type.DICT);
        return ret;
    }

    /**
     * Return the python path being used.
     * @return
     */
    public static String getPythonPath() {
        return new BytePointer(Py_GetPath()).getString();
    }


    static {
        setPythonPath();
        init();
    }


    /* ---------sub-interpreter and gil management-----------*/
    public static void setInterpreter(String interpreterName) {
        if (!hasInterpreter(interpreterName)){
            PyThreadState main = PyThreadState_Get();
            PyThreadState ts = Py_NewInterpreter();

            interpreters.put(interpreterName, ts);
            PyThreadState_Swap(main);
        }

        currentInterpreter = interpreterName;
    }

    public static String getInterpreter(){
        return currentInterpreter;
    }


    public static boolean hasInterpreter(String interpreterName){
        return interpreters.containsKey(interpreterName);
    }

    public static void deleteInterpreter(String interpreterName){
        if (interpreterName.equals("main")){
            throw new IllegalArgumentException("Can not delete main interpreter");
        }

        Py_EndInterpreter(interpreters.remove(interpreterName));
    }

    private static synchronized void acquireGIL() {
        log.info("acquireGIL()");
        log.info("CPython: PyEval_SaveThread()");
        mainThreadState = PyEval_SaveThread();
        log.info("CPython: PyThreadState_New()");
        currentThreadState = PyThreadState_New(interpreters.get(currentInterpreter).interp());
        log.info("CPython: PyEval_RestoreThread()");
        PyEval_RestoreThread(currentThreadState);
        log.info("CPython: PyThreadState_Swap()");
        PyThreadState_Swap(currentThreadState);

    }

    private static synchronized void releaseGIL() {
        log.info("CPython: PyEval_SaveThread()");
        PyEval_SaveThread();
        log.info("CPython: PyEval_RestoreThread()");
        PyEval_RestoreThread(mainThreadState);
    }

    /* -------------------*/
    /**
     * Print the python version to standard out.
     */
    public static void printPythonVersion() {
        exec("import sys; print(sys.version) sys.stdout.flush();");
    }



    private static String inputCode(PythonVariables pyInputs)throws Exception {
        String inputCode = "";
        if (pyInputs == null){
            return inputCode;
        }

        Map<String, String> strInputs = pyInputs.getStrVariables();
        Map<String, Long> intInputs = pyInputs.getIntVariables();
        Map<String, Double> floatInputs = pyInputs.getFloatVariables();
        Map<String, NumpyArray> ndInputs = pyInputs.getNdVars();
        Map<String, Object[]> listInputs = pyInputs.getListVariables();
        Map<String, String> fileInputs = pyInputs.getFileVariables();
        Map<String, Map<?,?>> dictInputs = pyInputs.getDictVariables();

        String[] varNames;


        varNames = strInputs.keySet().toArray(new String[strInputs.size()]);
        for(String varName: varNames) {
            Preconditions.checkNotNull(varName,"Var name is null!");
            Preconditions.checkNotNull(varName.isEmpty(),"Var name can not be empty!");
            String varValue = strInputs.get(varName);
            //inputCode += varName + "= {}\n";
            if(varValue != null)
                inputCode += varName + " = \"\"\"" + escapeStr(varValue) + "\"\"\"\n";
            else {
                inputCode += varName + " = ''\n";
            }
        }

        varNames = intInputs.keySet().toArray(new String[intInputs.size()]);
        for(String varName: varNames) {
            Long varValue = intInputs.get(varName);
            if(varValue != null)
                inputCode += varName + " = " + varValue.toString() + "\n";
            else {
                inputCode += " = 0\n";
            }
        }

        varNames = dictInputs.keySet().toArray(new String[dictInputs.size()]);
        for(String varName: varNames) {
            Map<?,?> varValue = dictInputs.get(varName);
            if(varValue != null) {
                throw new IllegalArgumentException("Unable to generate input code for dictionaries.");
            }
            else {
                inputCode += " = {}\n";
            }
        }

        varNames = floatInputs.keySet().toArray(new String[floatInputs.size()]);
        for(String varName: varNames){
            Double varValue = floatInputs.get(varName);
            if(varValue != null)
                inputCode += varName + " = " + varValue.toString() + "\n";
            else {
                inputCode += varName + " = 0.0\n";
            }
        }

        varNames = listInputs.keySet().toArray(new String[listInputs.size()]);
        for (String varName: varNames) {
            Object[] varValue = listInputs.get(varName);
            if(varValue != null) {
                String listStr = jArrayToPyString(varValue);
                inputCode += varName + " = " + listStr + "\n";
            }
            else {
                inputCode += varName + " + []\n";
            }

        }

        varNames = fileInputs.keySet().toArray(new String[fileInputs.size()]);
        for(String varName: varNames) {
            String varValue = fileInputs.get(varName);
            if(varValue != null)
                inputCode += varName + " = \"\"\"" + escapeStr(varValue) + "\"\"\"\n";
            else {
                inputCode += varName + " = ''\n";
            }
        }

        if (!ndInputs.isEmpty()) {
            inputCode += "import ctypes\n\nimport sys\nimport numpy as np\n";
            varNames = ndInputs.keySet().toArray(new String[ndInputs.size()]);

            String converter = "__arr_converter = lambda addr, shape, type: np.ctypeslib.as_array(ctypes.cast(addr, ctypes.POINTER(type)), shape)\n";
            inputCode += converter;
            for(String varName: varNames) {
                NumpyArray npArr = ndInputs.get(varName);
                if(npArr == null)
                    continue;

                npArr = npArr.copy();
                String shapeStr = "(";
                for (long d: npArr.getShape()){
                    shapeStr += d + ",";
                }
                shapeStr += ")";
                String code;
                String ctype;
                if (npArr.getDtype() == DataType.FLOAT){

                    ctype = "ctypes.c_float";
                }
                else if (npArr.getDtype() == DataType.DOUBLE){
                    ctype = "ctypes.c_double";
                }
                else if (npArr.getDtype() == DataType.SHORT){
                    ctype = "ctypes.c_int16";
                }
                else if (npArr.getDtype() == DataType.INT){
                    ctype = "ctypes.c_int32";
                }
                else if (npArr.getDtype() == DataType.LONG){
                    ctype = "ctypes.c_int64";
                }
                else{
                    throw new Exception("Unsupported data type: " + npArr.getDtype().toString() + ".");
                }

                code = "__arr_converter(" + npArr.getAddress() + "," + shapeStr + "," + ctype + ")";
                code =  varName + "=" + code + "\n";
                inputCode += code;
            }

        }
        return inputCode;
    }


    private static synchronized  void _readOutputs(PythonVariables pyOutputs) throws IOException {
        File f = new File(getTempFile());
        Preconditions.checkState(f.exists(),"File " + f.getAbsolutePath() + " failed to get written for reading outputs!");
        String json = FileUtils.readFileToString(f, Charset.defaultCharset());
        log.info("Executioner output: ");
        log.info(json);
        f.delete();

        if(json.isEmpty()) {
            log.warn("No json found fore reading outputs. Returning.");
            return;
        }


        JSONParser p = new JSONParser();
        try{
            JSONObject jobj = (JSONObject) p.parse(json);
            for (String varName: pyOutputs.getVariables()){
                PythonVariables.Type type = pyOutputs.getType(varName);
                if (type == PythonVariables.Type.NDARRAY){
                    JSONObject varValue = (JSONObject)jobj.get(varName);
                    long address = (Long)varValue.get("address");
                    JSONArray shapeJson = (JSONArray)varValue.get("shape");
                    JSONArray stridesJson = (JSONArray)varValue.get("strides");
                    long[] shape = jsonArrayToLongArray(shapeJson);
                    long[] strides = jsonArrayToLongArray(stridesJson);
                    String dtypeName = (String)varValue.get("dtype");
                    DataType dtype;
                    if (dtypeName.equals("float64")){
                        dtype = DataType.DOUBLE;
                    }
                    else if (dtypeName.equals("float32")){
                        dtype = DataType.FLOAT;
                    }
                    else if (dtypeName.equals("int16")){
                        dtype = DataType.SHORT;
                    }
                    else if (dtypeName.equals("int32")){
                        dtype = DataType.INT;
                    }
                    else if (dtypeName.equals("int64")){
                        dtype = DataType.LONG;
                    }
                    else{
                        throw new Exception("Unsupported array type " + dtypeName + ".");
                    }

                    pyOutputs.setValue(varName, new NumpyArray(address, shape, strides, dtype, true));

                    //pyOutputs.setValue(varName, evalNDARRAY(varName));

                }
                else if (type == PythonVariables.Type.LIST) {
                    JSONArray varValue = (JSONArray)jobj.get(varName);
                    pyOutputs.setValue(varName, varValue);
                    //pyOutputs.setValue(varName, evalLIST(varName));
                }
                else if (type == PythonVariables.Type.DICT) {
                    Map map = toMap((JSONObject) jobj.get(varName));
                    pyOutputs.setValue(varName, map);
                    //pyOutputs.setValue(varName, evalDICT(varName));

                }
                else{
                    pyOutputs.setValue(varName, jobj.get(varName));
                }
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }




    private static synchronized void _exec(String code){
        log.info(code);
        log.info("CPython: PyRun_SimpleStringFlag()");

        int result = PyRun_SimpleStringFlags(code, null);
        if (result != 0) {
            log.info("CPython: PyErr_Print");
            PyErr_Print();
            throw new RuntimeException("exec failed");
        }
    }
    /**
     * Executes python code. Also manages python thread state.
     * @param code
     */

    public static void exec(String code) {
        code = getWrappedCode(code);
        if(code.contains("import numpy") && !getInterpreter().equals("main")) {
            throw new IllegalArgumentException("Unable to execute numpy on sub interpreter. See https://mail.python.org/pipermail/python-dev/2019-January/156095.html for the reasons.");
        }

        acquireGIL();
        _exec(code);
        log.info("Exec done");
        releaseGIL();
    }

    public static void exec(String code, PythonVariables pyOutputs) {

        exec(code + '\n'  + outputCode(pyOutputs));
        try {

            _readOutputs(pyOutputs);

        } catch (IOException e) {
            e.printStackTrace();
        }

        releaseGIL();
    }





    public static void exec(String code, PythonVariables pyInputs, PythonVariables pyOutputs) throws Exception {
        String inputCode = inputCode(pyInputs);
        exec(inputCode + code, pyOutputs);
    }

    private static String interpreterNameFromTransform(PythonTransform transform){
        return transform.getName().replace("-", "_");
    }

    private static String[] splitSetupAndExecCode(String code){
        String startToken = "#<SETUP>";
        String endToken = "#</SETUP>";
        if (code.contains(startToken) && code.contains(endToken)){
            return code.split(endToken);
        }
        else{
            return new String[]{null, code};
        }
    }

    public static PythonVariables exec(PythonTransform transform, PythonVariables inputs)throws Exception{
        String split[] = splitSetupAndExecCode(transform.getCode());
        String name = interpreterNameFromTransform(transform);
        if (!hasInterpreter(name) && split[0] != null){
            setInterpreter(name);
            exec(split[0]);

        }
        setInterpreter(name);
        Preconditions.checkNotNull(transform.getOutputs(),"Transform outputs were null!");
        exec(split[1], inputs, transform.getOutputs());
        return transform.getOutputs();
    }

    public static PythonVariables execAndReturnAllVariables(String code) {
        exec(code + '\n' + outputCodeForAllVariables());
        PythonVariables allVars = new PythonVariables();
        allVars.addDict(ALL_VARIABLES_KEY);
        try {
            _readOutputs(allVars);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return expandInnerDict(allVars, ALL_VARIABLES_KEY);
    }

    public  static PythonVariables execAndReturnAllVariables(String code, PythonVariables pyInputs) throws Exception{
        String inputCode = inputCode(pyInputs);
        return execAndReturnAllVariables(inputCode + code);
    }


    public static String evalString(String varName) {
        PythonVariables vars = new PythonVariables();
        vars.addStr(varName);
        exec("print('')", vars);
        return vars.getStrValue(varName);
    }


    public static long evalInteger(String varName) {
        PythonVariables vars = new PythonVariables();
        vars.addInt(varName);
        exec("print('')", vars);
        return vars.getIntValue(varName);
    }


    public static Double evalFLOAT(String varName){
        PythonVariables vars = new PythonVariables();
        vars.addFloat(varName);
        exec("print('')", vars);
        return vars.getFloatValue(varName);
    }
    public static Object[] evalLIST(String varName){
        PythonVariables vars = new PythonVariables();
        vars.addList(varName);
        exec("pass", vars);
        return vars.getListValue(varName);
    }
    public static Map evalDict(String varName){
        PythonVariables vars = new PythonVariables();
        vars.addDict(varName);
        exec("pass", vars);
        return vars.getDictValue(varName);
    }

    public static NumpyArray evalNDARRAY(String varName){
        PythonVariables vars = new PythonVariables();
        vars.addNDArray(varName);
        exec("pass", vars);
        return vars.getNDArrayValue(varName);
    }



    private static String getOutputCheckCode(PythonVariables pyOutputs){
        // make sure all outputs exist and are of expected types
        // helps avoid JVM crashes (most of the time)
        String code= "__error_message=''\n";
        String checkVarExists = "if '%s' not in locals(): __error_message += '%s not found.'\n";
        String checkVarType = "if not isinstance(%s, %s): __error_message += '%s is not of required type.'\n";
        for (String varName: pyOutputs.getVariables()){
            PythonVariables.Type type = pyOutputs.getType(varName);
            code += String.format(checkVarExists, varName, varName);
            switch(type){
                case INT:
                    code += String.format(checkVarType, varName, "int", varName);
                    break;
                case STR:
                    code += String.format(checkVarType, varName, "str", varName);
                    break;
                case FLOAT:
                    code += String.format(checkVarType, varName, "float", varName);
                    break;
                case BOOL:
                    code += String.format(checkVarType, varName, "bool", varName);
                    break;
                case NDARRAY:
                    code += String.format(checkVarType, varName, "np.ndarray", varName);
                    break;
                case LIST:
                    code += String.format(checkVarType, varName, "list", varName);
                    break;
            }
        }
        return code;
    }

    private static String outputVarName() {
        return "_" + Thread.currentThread().getId() + "_" + currentInterpreter + "_out";
    }
    private static  String outputCode(PythonVariables pyOutputs) {
        if (pyOutputs == null){
            return "";
        }

        String outputCode = "import json\n";
        String outputFunctions;
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new ClassPathResource("pythonexec/serialize_array.py").getInputStream())) {
            outputFunctions= IOUtils.toString(bufferedInputStream,Charset.defaultCharset());
            outputCode += outputFunctions;
            outputCode += "\n";
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read python file pythonexec/serialize_arrays.py from classpath");
        }

        outputCode += outputVarName() + " = __serialize_dict({";
        String[] varNames = pyOutputs.getVariables();
        for (String varName: varNames) {
            outputCode += "\"" + varName + "\": " + varName + ",";
        }


        if (varNames.length > 0)
            outputCode = outputCode.substring(0, outputCode.length() - 1);
        outputCode += "})";
        outputCode += "\nwith open('" + getTempFile() + "', 'w') as " + fileVarName + ":" + fileVarName + ".write(" + outputVarName() + ")";


        return outputCode;

    }

    private static String jArrayToPyString(Object[] array) {
        String str = "[";
        for (int i = 0; i < array.length; i++){
            Object obj = array[i];
            if (obj instanceof Object[]){
                str += jArrayToPyString((Object[])obj);
            }
            else if (obj instanceof String){
                str += "\"" + obj + "\"";
            }
            else{
                str += obj.toString().replace("\"", "\\\"");
            }
            if (i < array.length - 1){
                str += ",";
            }

        }
        str += "]";
        return str;
    }

    private static String escapeStr(String str) {
        if(str == null)
            return null;
        str = str.replace("\\", "\\\\");
        str = str.replace("\"\"\"", "\\\"\\\"\\\"");
        return str;
    }

    private static String getWrappedCode(String code) {
        try(InputStream is = new ClassPathResource("pythonexec/pythonexec.py").getInputStream()) {
            String base = IOUtils.toString(is, Charset.defaultCharset());
            StringBuffer indentedCode = new StringBuffer();
            for(String split : code.split("\n")) {
                indentedCode.append("    " + split + "\n");

            }
            String out = base.replace("    pass",indentedCode);
            return out;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read python code!",e);
        }

    }



    private static String getTempFile(){
        String ret =  "temp_" + Thread.currentThread().getId() + "_" + currentInterpreter +  ".json";
        log.info(ret);
        return ret;
    }


    private static String outputCodeForAllVariables(){
        String outputCode = "";
        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new ClassPathResource("pythonexec/outputcode.py").getInputStream())) {
            outputCode  += IOUtils.toString(bufferedInputStream,Charset.defaultCharset()).replace("f2",fileVarName);
            outputCode += "\n";
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read python file pythonexec/outputcode.py from classpath");
        }

        outputCode += String.format("vars =  {key:value for (key,value) in locals().items() if not key.startswith('_') and key is not '%s' and key is not 'loc' and type(value) in (list, dict, str, int, float, bool) or value is None}\n",fileVarName);
        outputCode += String.format("with open('" + getTempFile() + "', 'w') as %s:json.dump({",fileVarName);
        outputCode +=String.format( "\"" + ALL_VARIABLES_KEY + "\"" + ": vars}, %s)\n",fileVarName);
        return outputCode;
    }


    /*-----monkey patch for numpy-----*/
    private static List<String[]> _getPatches() {
        exec("import numpy as np");
        exec( "__overrides_path = np.core.overrides.__file__");
        exec("__random_path = np.random.__file__");

        List<String[]> patches = new ArrayList<>();

        patches.add(new String[]{
                "pythonexec/patch0.py",
                evalString("__overrides_path")
        });
        patches.add(new String[]{
                "pythonexec/patch1.py",
                evalString("__random_path")
        });
        return patches;
    }

    private static void _applyPatch(String src, String dest){
        try(InputStream is = new ClassPathResource(src).getInputStream()) {
            String patch = IOUtils.toString(is, Charset.defaultCharset());
            FileUtils.write(new File(dest), patch, "utf-8");
        }
        catch(IOException e){
            throw new RuntimeException("Error reading resource.");
        }
    }

    private static boolean _checkPatchApplied(String dest){
        try {
            return FileUtils.readFileToString(new File(dest), "utf-8").startsWith("#patch");
        } catch (IOException e) {
            throw new RuntimeException("Error patching numpy");

        }
    }

    private static void applyPatches(){
        for (String[] patch : _getPatches()){
            if (!_checkPatchApplied(patch[1])){
                _applyPatch(patch[0], patch[1]);
            }
        }
       // exec("print('Reloading numpy'); sys.stdout.flush(); sys.stderr.flush(); import importlib; print('Imported importlib'); sys.stdout.flush();  importlib.reload(np); print('Reloaded lib'); sys.stdout.flush(); sys.stderr.flush();");
        for (String[] patch: _getPatches()){
            if (!_checkPatchApplied(patch[1])){
                throw new RuntimeException("Error patching numpy");
            }
        }
    }
}