/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.front.controller.mina;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.base.controller.BaseController;
import com.java110.core.context.IPageData;
import com.java110.core.context.PageData;
import com.java110.core.factory.WechatFactory;
import com.java110.front.smo.login.IWxLoginSMO;
import com.java110.utils.cache.CommonCache;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信小程序登录处理类
 */
@RestController
@RequestMapping(path = "/app")
public class WxLoginController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(WxLoginController.class);

    @Autowired
    private IWxLoginSMO wxLoginSMOImpl;


    /**
     * 微信登录接口
     *
     * @param postInfo
     * @param request
     */
    @RequestMapping(path = "/loginWx", method = RequestMethod.POST)
    public ResponseEntity<String> loginWx(@RequestBody String postInfo, HttpServletRequest request) {
        ResponseEntity<String> responseEntity = null;
        JSONObject postObj = JSONObject.parseObject(postInfo);
        String code = JSONObject.parseObject(postInfo).getString("code");
        JSONObject userInfo = postObj.getJSONObject("userInfo");
        if (code == null || userInfo == null) {
            logger.error("code is null");
            responseEntity = new ResponseEntity<>("code is null", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }

        /*IPageData pd = (IPageData) request.getAttribute(CommonConstant.CONTEXT_PAGE_DATA);*/
        String appId = request.getHeader("APP_ID");
        if (StringUtil.isEmpty(appId)) {
            appId = request.getHeader("APP-ID");
        }
        IPageData pd = PageData.newInstance().builder("", "", "", postInfo,
                "", "", "", "",
                appId);

        return wxLoginSMOImpl.doLogin(pd);
    }


    @RequestMapping(path = "/getWxPhoto", method = RequestMethod.POST)
    public ResponseEntity<String> getWxPhoto(@RequestBody String postInfo) {
        JSONObject postObj = JSONObject.parseObject(postInfo);


        String photoInfo = WechatFactory.getPhoneNumberBeanS5(postObj.getString("decryptData"),
                postObj.getString("key"), postObj.getString("iv"));
        JSONObject photoObj = JSONObject.parseObject(photoInfo);
        CommonCache.setValue(postObj.getString("key"), photoObj.getString("phoneNumber"), CommonCache.defaultExpireTime);
        return ResultVo.createResponseEntity(photoObj);

    }
}
