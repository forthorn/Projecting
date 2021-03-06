/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com | 3772304@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.forthorn.projecting.api;

public class ApiConstants {


    public static final String JPUSH_HOST = "http://api.im.jpush.cn";
    public static final String VOM_HOST = "http://advert.chindor.com";

    public static final String DEVICE_ANDROID = "0";
    public static final String DEVICE_IOS = "1";
    public static final String DEVICE_PC = "2";
    public static final String VOM_ROOT = "http://vom.chindor.com/";

    /**
     * 获取对应的host
     *
     * @param hostType host类型
     * @return host
     */
    public static String getHost(int hostType) {
        String host;
        switch (hostType) {
            case HostType.VOM_HOST:
                host = VOM_HOST;
                break;
            case HostType.JPUSH_HOST:
                host = JPUSH_HOST;
                break;
            default:
                host = VOM_HOST;
                break;
        }
        return host;
    }
}
