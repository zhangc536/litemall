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

REQ_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx"

def test_track_api():
    print("="*50)
    print("测试物流跟踪API (1001)")
    print("="*50)
    
    # 物流跟踪API需要货物信息
    request_data = {
        "OrderCode": "",
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO,
        "PayType": 1,
        "ExpType": 1,
        "IsReturnSign": 1,
        "Sender": {
            "Name": "发货人",
            "Tel": "13800138000"
        },
        "Receiver": {
            "Name": "收货人",
            "Tel": "13900139000"
        },
        "Commodity": [
            {
                "GoodsName": "商品",
                "GoodsCode": "001",
                "Goodsquantity": 1
            }
        ]
    }
    
    request_data_str = json.dumps(request_data, ensure_ascii=False)
    print(f"请求数据: {request_data_str}")
    
    content = request_data_str + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')
    
    params = {
        'RequestData': request_data_str,
        'EBusinessID': APP_ID,
        'RequestType': '1001',
        'DataSign': data_sign,
        'DataType': '2'
    }
    
    encoded_params = urllib.parse.urlencode(params)
    
    try:
        req = urllib.request.Request(REQ_URL, data=encoded_params.encode('utf-8'), method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        
        with urllib.request.urlopen(req, timeout=30) as response:
            result = response.read().decode('utf-8')
            print("\n响应结果:")
            try:
                json_result = json.loads(result)
                print(json.dumps(json_result, indent=2, ensure_ascii=False))
            except:
                print(result)
                
    except Exception as e:
        print(f"请求失败: {e}")

def test_simple_track():
    print("\n" + "="*50)
    print("测试简化物流跟踪 (1001)")
    print("="*50)
    
    # 简化版本
    request_data = {
        "OrderCode": "",
        "ShipperCode": EXP_CODE,
        "LogisticCode": EXP_NO
    }
    
    request_data_str = json.dumps(request_data, ensure_ascii=False)
    print(f"请求数据: {request_data_str}")
    
    content = request_data_str + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')
    
    params = {
        'RequestData': request_data_str,
        'EBusinessID': APP_ID,
        'RequestType': '1001',
        'DataSign': data_sign,
        'DataType': '2'
    }
    
    encoded_params = urllib.parse.urlencode(params)
    
    try:
        req = urllib.request.Request(REQ_URL, data=encoded_params.encode('utf-8'), method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        
        with urllib.request.urlopen(req, timeout=30) as response:
            result = response.read().decode('utf-8')
            print("\n响应结果:")
            try:
                json_result = json.loads(result)
                print(json.dumps(json_result, indent=2, ensure_ascii=False))
            except:
                print(result)
                
    except Exception as e:
        print(f"请求失败: {e}")

if __name__ == '__main__':
    test_simple_track()
    test_track_api()
