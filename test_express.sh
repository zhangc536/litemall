#!/bin/bash

# 快递鸟API测试脚本
# 使用方法: bash test_express.sh

APP_ID="1912788"
APP_KEY="3e1ce226-8749-4266-8cef-bb3e6234ec4c"
EXP_CODE="ZTO"
EXP_NO="73596830956390"

echo "========== 快递鸟API测试 =========="
echo "快递公司代码: $EXP_CODE"
echo "快递单号: $EXP_NO"
echo "APP ID: $APP_ID"
echo "=================================="

# 构建请求数据
REQUEST_DATA="{'OrderCode':'','ShipperCode':'$EXP_CODE','LogisticCode':'$EXP_NO'}"
echo "请求数据: $REQUEST_DATA"

# 生成签名
CONTENT="${REQUEST_DATA}${APP_KEY}"
MD5_STR=$(echo -n "$CONTENT" | md5sum | awk '{print $1}')
DATA_SIGN=$(echo -n "$MD5_STR" | base64)

echo "MD5: $MD5_STR"
echo "签名: $DATA_SIGN"

# URL编码
REQUEST_DATA_ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$REQUEST_DATA'''))")
DATA_SIGN_ENCODED=$(python3 -c "import urllib.parse; print(urllib.parse.quote('''$DATA_SIGN'''))")

echo ""
echo "发送请求..."

# 发送请求
curl -X POST "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "RequestData=${REQUEST_DATA_ENCODED}" \
  -d "EBusinessID=${APP_ID}" \
  -d "RequestType=1002" \
  -d "DataSign=${DATA_SIGN_ENCODED}" \
  -d "DataType=2"

echo ""
echo "=================================="
