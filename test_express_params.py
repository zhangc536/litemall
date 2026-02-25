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

def test_params(params_name, request_data):
    print(f"\n{'='*50}")
    print(f"测试参数名: {params_name}")
    print("="*50)
    
    request_data_str = json.dumps(request_data, ensure_ascii=False)
    print(f"请求数据: {request_data_str}")

    content = request_data_str + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')

    params = {
        'RequestData': request_data_str,
        'EBusinessID': APP_ID,
        'RequestType': '8002',
        'DataSign': data_sign,
        'DataType': '2'
    }

    encoded_params = urllib.parse.urlencode(params)

    try:
        req = urllib.request.Request(REQ_URL, data=encoded_params.encode('utf-8'), method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        
        with urllib.request.urlopen(req, timeout=30) as response:
            result = response.read().decode('utf-8')
            try:
                json_result = json.loads(result)
                print(f"Success: {json_result.get('Success')}")
                print(f"Reason: {json_result.get('Reason')}")
                if json_result.get('Traces'):
                    print(f"轨迹数量: {len(json_result.get('Traces', []))}")
            except:
                print(result)
                
    except Exception as e:
        print(f"请求失败: {e}")

def main():
    print("=" * 50)
    print("快递查询API多参数测试")
    print(f"快递公司: {EXP_CODE}, 单号: {EXP_NO}, 手机尾号: {PHONE_TAIL}")
    print("=" * 50)

    # 测试1: Phone参数
    test_params("Phone", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "Phone": PHONE_TAIL
    })

    # 测试2: PhoneNum参数
    test_params("PhoneNum", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "PhoneNum": PHONE_TAIL
    })

    # 测试3: Mobile参数
    test_params("Mobile", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "Mobile": PHONE_TAIL
    })

    # 测试4: Tel参数
    test_params("Tel", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "Tel": PHONE_TAIL
    })

    # 测试5: ReceiverPhone参数
    test_params("ReceiverPhone", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "ReceiverPhone": PHONE_TAIL
    })

    # 测试6: 完整手机号格式
    test_params("完整手机号", {
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "Phone": "13800009605"
    })

if __name__ == '__main__':
    main()
