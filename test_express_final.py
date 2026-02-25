#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import hashlib
import base64
import urllib.parse
import urllib.request
import json

APP_ID = "1912788"
APP_KEY = "3e1ce226-8749-4266-8cef-bb3e6234ec4c"
EXP_CODE = "ZTO"
EXP_NO = "73596830956390"
PHONE_TAIL = "9605"

REQ_URL = "https://api.kdniao.com/api/dist"

def main():
    print("=" * 50)
    print("快递查询API测试 (请求指令: 8002)")
    print("=" * 50)
    print(f"快递公司代码: {EXP_CODE}")
    print(f"快递单号: {EXP_NO}")
    print(f"手机尾号: {PHONE_TAIL}")
    print(f"APP ID: {APP_ID}")
    print(f"接口地址: {REQ_URL}")
    print("=" * 50)

    request_data = {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "Phone": PHONE_TAIL
    }
    request_data_str = json.dumps(request_data, ensure_ascii=False)
    print(f"请求数据: {request_data_str}")

    content = request_data_str + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')
    print(f"MD5结果: {md5_str}")
    print(f"签名结果: {data_sign}")

    params = {
        'RequestData': request_data_str,
        'EBusinessID': APP_ID,
        'RequestType': '8002',
        'DataSign': data_sign,
        'DataType': '2'
    }

    encoded_params = urllib.parse.urlencode(params)
    print(f"\n发送请求到: {REQ_URL}")

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
