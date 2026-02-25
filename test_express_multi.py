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

def test_api(request_type, type_name):
    print(f"\n{'='*50}")
    print(f"测试 {type_name} (请求类型: {request_type})")
    print("="*50)
    
    request_data = f"{{'OrderCode':'','ShipperCode':'{EXP_CODE}','LogisticCode':'{EXP_NO}'}}"
    
    content = request_data + APP_KEY
    md5_str = hashlib.md5(content.encode('utf-8')).hexdigest().lower()
    data_sign = base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')
    
    params = {
        'RequestData': request_data,
        'EBusinessID': APP_ID,
        'RequestType': request_type,
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
                print(f"Success: {json_result.get('Success', False)}")
                print(f"Reason: {json_result.get('Reason', 'N/A')}")
                if json_result.get('Traces'):
                    print(f"轨迹数量: {len(json_result.get('Traces', []))}")
            except:
                print(result)
                
    except Exception as e:
        print(f"请求失败: {e}")

def main():
    print("="*50)
    print("快递鸟API多类型测试")
    print(f"快递公司: {EXP_CODE}")
    print(f"快递单号: {EXP_NO}")
    print("="*50)
    
    # 测试不同的API类型
    test_api("1002", "即时查询API")
    test_api("1001", "物流跟踪API")
    test_api("8001", "物流轨迹地图")

if __name__ == '__main__':
    main()
