#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import hashlib
import base64
import urllib.parse
import urllib.request
import json

APP_ID = "1912788"
APP_KEY = "3e1ce226-8749-4266-8cef-bb3e6234ec4c"
EXP_NO = "73596830956390"

REQ_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx"

def test_request(request_data, desc):
    print(f"\n{'='*50}")
    print(f"测试: {desc}")
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
                print(f"State: {json_result.get('State')}")
                if json_result.get('Traces'):
                    print(f"轨迹数量: {len(json_result.get('Traces', []))}")
                    for trace in json_result.get('Traces', [])[:3]:
                        print(f"  - {trace.get('AcceptTime')}: {trace.get('AcceptStation')}")
            except:
                print(result)
                
    except Exception as e:
        print(f"请求失败: {e}")

def main():
    print("=" * 50)
    print("快递查询API测试 (请求指令: 8002)")
    print(f"快递单号: {EXP_NO}")
    print("=" * 50)

    # 测试1: 只传单号（自动识别快递公司）
    test_request({
        "LogisticCode": EXP_NO
    }, "只传单号（自动识别）")

    # 测试2: 传单号+快递公司
    test_request({
        "ShipperCode": "ZTO",
        "LogisticCode": EXP_NO
    }, "传单号+快递公司")

    # 测试3: 传单号+快递公司+OrderCode
    test_request({
        "OrderCode": "",
        "ShipperCode": "ZTO",
        "LogisticCode": EXP_NO
    }, "传单号+快递公司+OrderCode")

if __name__ == '__main__':
    main()
