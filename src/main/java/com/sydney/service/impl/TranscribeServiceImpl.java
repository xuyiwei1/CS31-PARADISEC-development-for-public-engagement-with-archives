package com.sydney.service.impl;

import cn.hutool.http.HttpUtil;
import com.sydney.pojo.Model;
import com.sydney.pojo.Result;
import com.sydney.service.TranscribeService;
import com.sydney.utils.FileUtil;
import com.sydney.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yiwei Xu
 * @version 1.0
 * @description TODO
 * @date 2023/10/4 9:49
 */
@Service
public class TranscribeServiceImpl implements TranscribeService {

    @Value("${elpis.url}")
    private String url;

    //get trained models' info from elpis
    public Result getModels() {
        String models = HttpClientUtil.getRequestNoParam(url + "/api/model/list");
        if(models.isEmpty()) {
            return Result.fail("The model doesn't exist");
        }
        return Result.ok(models);
    }

    //upload audio to make transcribe
    public Result uploadAudio(String filePath) throws IOException {
        if(filePath == null || filePath.isEmpty()) {
            return Result.fail("audio file cannot be empty");
        }

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("file", cn.hutool.core.io.FileUtil.file(filePath));

        String result= HttpUtil.post(url + "/api/transcription/new", paramMap);
        System.out.println(result);


        //begin trans
        //HttpClientUtil.getRequestNoParam(url+"/api/transcription/transcribe");
        return Result.ok(result);
    }


    /*@Override
    public Result uploadAudioMakeTranscribeByHft(MultipartFile file) throws IOException {
        if(file.isEmpty()) {
            return Result.fail("audio file cannot be empty");
        }

        //上传需要转录的语音文件
        HttpClientUtil.fileUpload(url+"/api/transcription/new", bytes);
        //开始转录
        HttpClientUtil.getRequestNoParam(url+"/api/transcription/transcribe");
        return Result.ok("Please wait for transcribing");
    }*/



    //get transcribe result 获取txt格式的转录结果
    @Override
    public Result getTranscribeResult() {
        String transcribeResult = HttpUtil.get(url + "/api/transcription/text");
        if(transcribeResult.isEmpty()) {
            return Result.fail("The transcription is processing, please wait");
        }
        return Result.ok(transcribeResult);
    }

    //获取当前语音转录的状态，用于判断是否转录完成，以及转录进行到哪个阶段了
    @Override
    public Result getTranscribeStatus() {
        String transStatus = HttpUtil.get(url + "/api/transcription/status");
        if(transStatus.isEmpty()) {
            return Result.fail("The transcription is not begin, please start transcription first");
        }
        return Result.ok(transStatus);
    }

    //获取elan格式的转录结果
    @Override
    public Result getTranscribeResultElan() {
        String transStatus = HttpUtil.get(url + "/api/transcription/elan");
        if(transStatus.isEmpty()) {
            return Result.fail("The transcription is not begin, please start transcription first");
        }
        return Result.ok(transStatus);
    }

    //TODO to implement
    @Override
    public Result uploadModel(MultipartFile model) throws IOException {
        if(model == null) {
            return Result.fail("model can not be null");
        }
        //convert the model to the binary file
        byte[] bytes = model.getBytes();
        //upload model to elpis using httpclient
        String uploadResult = HttpClientUtil.fileUploadWithHeaders(url+"/api/model/upload", bytes);
        if(uploadResult.isEmpty()) {
            return Result.fail("uploading model fail, please try again.");
        }
        return Result.ok(uploadResult);
    }

    @Override
    public Result setModelParamKaldi(Model model) {
        //非空校验
        if(model == null) {
            return Result.fail("the model can not be null");
        }
        if(model.getModelName() == null || StringUtils.isEmpty(model.getModelName())) {
            return Result.fail("model name can not be null or empty");
        }
        if(model.getEngineName() == null || StringUtils.isEmpty(model.getEngineName())) {
            return Result.fail("model name can not be null or empty");
        }
        if(model.getDataSetName() == null || StringUtils.isEmpty(model.getDataSetName())) {
            return Result.fail("model name can not be null or empty");
        }
        if(model.getPronDictMapName() == null || StringUtils.isEmpty(model.getPronDictMapName())) {
            return Result.fail("model name can not be null or empty");
        }

        //设置引擎名称
        Map<String,Object> engineMap = new HashMap<>();
        engineMap.put("engine_name",model.getEngineName());
        String s = HttpClientUtil.postWithJsonParam(url + "/api/config/engine/load", engineMap);
        //设置模型
        Map<String,Object> modelMap = new HashMap<>();
        modelMap.put("name",model.getModelName());
        String s1 = HttpClientUtil.postWithJsonParam(url + "/api/model/load", modelMap);
        //设置数据集
        Map<String,Object> dataSetMap = new HashMap<>();
        dataSetMap.put("name",model.getDataSetName());
        String s2 = HttpClientUtil.postWithJsonParam(url + "/api/dataset/load", dataSetMap);
        //设置字典
        Map<String,Object> pronDictMap = new HashMap<>();
        pronDictMap.put("name",model.getPronDictMapName());
        String s3 = HttpClientUtil.postWithJsonParam(url + "/api/pron-dict/load", pronDictMap);
        return Result.ok();
    }

    @Override
    public Result setModelParamHFT(Model model) {
        //非空校验
        if(model == null) {
            return Result.fail("the model can not be null");
        }
        if(model.getModelName() == null || StringUtils.isEmpty(model.getModelName())) {
            return Result.fail("model name can not be null or empty");
        }
        if(model.getEngineName() == null || StringUtils.isEmpty(model.getEngineName())) {
            return Result.fail("model name can not be null or empty");
        }
        if(model.getDataSetName() == null || StringUtils.isEmpty(model.getDataSetName())) {
            return Result.fail("model name can not be null or empty");
        }

        //设置引擎名称
        Map<String,Object> engineMap = new HashMap<>();
        engineMap.put("engine_name",model.getEngineName());
        String s = HttpClientUtil.postWithJsonParam(url + "/api/config/engine/load", engineMap);
        //设置模型
        Map<String,Object> modelMap = new HashMap<>();
        modelMap.put("name",model.getModelName());
        String s1 = HttpClientUtil.postWithJsonParam(url + "/api/model/load", modelMap);
        //设置数据集
        Map<String,Object> dataSetMap = new HashMap<>();
        dataSetMap.put("name",model.getDataSetName());
        String s2 = HttpClientUtil.postWithJsonParam(url + "/api/dataset/load", dataSetMap);
        return Result.ok();

    }

    //begin to transcribe kaldi
    @Override
    public Result transcribe() {
        String result1= HttpUtil.get(url+"/api/transcription/transcribe");
        System.out.println(result1);
        return Result.ok(result1);
    }


}
