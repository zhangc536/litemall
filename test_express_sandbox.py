#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import hashlib
import base64
import urllib.parse
import urllib.request
import json

# 测试环境配置
APP_ID = "test1617571"
APP_KEY = "554343b2-7252-439b-b4eb-1af42c8f2175"
EXP_CODE = "ZTO"
EXP_NO = "73596830956390"

# 测试环境URL
REQ_URL = "http://sandboxapi.kdniao.com:8080/kdniaosandbox/gateway/exterfaceInvoke.json"

def main():
    print("=" * 50)
    print("快递鸟API测试（测试环境）")
    print("=" * 50)
    print(f"快递公司代码: {EXP_CODE}")
    print(f"快递单号: {EXP_NO}")
    print(f"APP ID: {APP_ID}")
    print(f"APP Key: {APP_KEY}")
    print("=" * 50)

    # 构建请求数据
    request_data = f"{{'OrderCode':'','ShipperCode':'{EXP_CODE}','LogisticCode':'{EXP_NO}'}}"
    print(f"请求数据: {request_data}")

    # 生成签名
    content = request_data + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')

    print(f"MD5结果: {md5_str}")
    print(f"签名结果: {data_sign}")

    # 构建请求参数
    params = {
        'RequestData': request_data,
        'EBusinessID': APP_ID,
        'RequestType': '1002',
        'DataSign': data_sign,
        'DataType': '2'
    }

    # URL编码
    encoded_params = urllib.parse.urlencode(params)

    # 发送请求
    print("\n发送请求到测试环境...")
    try:
        req = urllib.request.Request(REQ_URL, data=encoded_params.encode('utf-8'), method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        
        with urllib.request.urlopen(req, timeout=30) as response:
            result = response.read().decode('utf-8')
            print("\n" + "=" * 50)
            print("响应结果:")
            print("=" * 50)
            
            try:
                json_result = json.loads(result)
                print(json.dumps(json_result, indent=2, ensure_ascii=False))
            except:
                print(result)
                
            print("=" * 50)
            
    except Exception as e:
        print(f"\n请求失败: {e}")

if __name__ == '__main__':
    main()
