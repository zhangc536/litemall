#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
快递鸟API完整测试脚本
测试快递查询API(8002)是否正常工作
"""

import hashlib
import base64
import urllib.parse
import urllib.request
import json
import sys

# 配置信息
APP_ID = "1912788"
APP_KEY = "3e1ce226-8749-4266-8cef-bb3e6234ec4c"
REQ_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx"

def md5_hash(content):
    """计算MD5"""
    return hashlib.md5(content.encode('utf-8')).hexdigest().lower()

def generate_sign(request_data):
    """生成签名"""
    content = request_data + APP_KEY
    md5_str = md5_hash(content)
    return base64.b64encode(md5_str.encode('utf-8')).decode('utf-8')

def query_express(exp_code, exp_no, phone_tail=None):
    """查询快递"""
    request_data = {
        "ShipperCode": exp_code,
        "LogisticCode": exp_no
    }
    if phone_tail:
        request_data["CustomerName"] = phone_tail
    
    request_data_str = json.dumps(request_data, ensure_ascii=False)
    data_sign = generate_sign(request_data_str)
    
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
            return json.loads(result)
    except Exception as e:
        return {"Success": False, "Reason": str(e)}

def main():
    print("=" * 60)
    print("快递鸟API测试 - 快递查询API(8002)")
    print("=" * 60)
    print(f"APP ID: {APP_ID}")
    print(f"APP Key: {APP_KEY}")
    print(f"API URL: {REQ_URL}")
    print("=" * 60)
    
    # 测试用例
    test_cases = [
        {
            "name": "中通快递 - 有手机尾号",
            "exp_code": "ZTO",
            "exp_no": "73596830956390",
            "phone_tail": "9605"
        },
        {
            "name": "中通快递 - 无手机尾号",
            "exp_code": "ZTO",
            "exp_no": "73596830956390",
            "phone_tail": None
        }
    ]
    
    for case in test_cases:
        print(f"\n{'='*60}")
        print(f"测试: {case['name']}")
        print("=" * 60)
        
        result = query_express(case['exp_code'], case['exp_no'], case['phone_tail'])
        
        print(f"Success: {result.get('Success')}")
        print(f"Reason: {result.get('Reason')}")
        
        if result.get('Traces'):
            traces = result.get('Traces', [])
            print(f"轨迹数量: {len(traces)}")
            print("\n最新轨迹:")
            for trace in traces[:3]:
                print(f"  - {trace.get('AcceptTime')}: {trace.get('AcceptStation')}")
        
        if result.get('Success'):
            print("\n✅ 测试通过")
        else:
            print("\n❌ 测试失败")

if __name__ == '__main__':
    main()
