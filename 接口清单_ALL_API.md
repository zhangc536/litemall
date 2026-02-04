# 全量接口清单（当前仓库）

> 说明：本清单按模块分组，列出控制器声明的 HTTP 接口（方法 + 路径），仅统计当前代码状态。返回结构以各模块实现为准（admin/wx 为 { errno, errmsg, data }；V1 适配层为 { code, message, data }）。

## 管理后台（/admin/**）

- 认证与账号
  - GET /admin/auth/kaptcha
  - POST /admin/auth/login
  - POST /admin/auth/logout
  - GET /admin/auth/info
- 首页与权限测试
  - GET /admin/index/index
  - GET /admin/index/read
  - POST /admin/index/write
- 管理员管理
  - GET /admin/admin/list
  - POST /admin/admin/create
  - GET /admin/admin/read
  - POST /admin/admin/update
  - POST /admin/admin/delete
- 角色管理
  - GET /admin/role/list
  - GET /admin/role/options
  - GET /admin/role/read
  - POST /admin/role/create
  - POST /admin/role/update
  - POST /admin/role/delete
  - GET /admin/role/permissions
  - POST /admin/role/permissions
- 个人中心通知
  - POST /admin/profile/password
  - GET /admin/profile/nnotice
  - GET /admin/profile/lsnotice
  - POST /admin/profile/catnotice
  - POST /admin/profile/bcatnotice
  - POST /admin/profile/rmnotice
  - POST /admin/profile/brmnotice
- 系统通知
  - GET /admin/notice/list
  - POST /admin/notice/create
  - GET /admin/notice/read
  - POST /admin/notice/update
  - POST /admin/notice/delete
  - POST /admin/notice/batch-delete
- 操作日志
  - GET /admin/log/list
- 历史记录（搜索等）
  - GET /admin/history/list
- 商品管理
  - GET /admin/goods/list
  - GET /admin/goods/catAndBrand
  - POST /admin/goods/update
  - POST /admin/goods/delete
  - POST /admin/goods/create
  - GET /admin/goods/detail
- 统计
  - GET /admin/stat/user
  - GET /admin/stat/order
  - GET /admin/stat/goods
- 系统配置
  - GET /admin/config/mall
  - POST /admin/config/mall
  - GET /admin/config/express
  - POST /admin/config/express
  - GET /admin/config/order
  - POST /admin/config/order
  - GET /admin/config/wx
  - POST /admin/config/wx
- 对象存储
  - GET /admin/storage/list
  - POST /admin/storage/create
  - POST /admin/storage/read
  - POST /admin/storage/update
  - POST /admin/storage/delete
- 凭证审核与发货（当前流程）
  - GET /admin/order/voucher/list
  - POST /admin/order/audit
  - POST /admin/order/ship

> 说明：旧的评论管理接口仍存在但前端未使用  
> - GET /admin/comment/list  
> - POST /admin/comment/delete  
> - POST /admin/comment/batch-delete

## 微信小程序（/wx/**）

- 订单凭证
  - GET /wx/order/voucher/list
  - POST /wx/order/voucher/upload
- 订单
  - GET /wx/order/list
  - GET /wx/order/detail
  - POST /wx/order/submit
  - POST /wx/order/cancel
  - POST /wx/order/prepay
  - POST /wx/order/h5pay
  - POST /wx/order/pay-notify
  - POST /wx/order/refund
  - POST /wx/order/confirm
  - POST /wx/order/delete
  - GET /wx/order/goods
  - POST /wx/order/comment
- 对象存储（用户通用上传/下载）
  - POST /wx/storage/upload
  - GET /wx/storage/fetch/{key}
  - GET /wx/storage/download/{key}
- 用户
  - GET /wx/user/index
- 商品
  - GET /wx/goods/detail
  - GET /wx/goods/category
  - GET /wx/goods/list
  - GET /wx/goods/related
  - GET /wx/goods/count
- 优惠券
  - GET /wx/coupon/list
  - GET /wx/coupon/mylist
  - GET /wx/coupon/selectlist
  - POST /wx/coupon/receive
  - POST /wx/coupon/exchange
- 反馈
  - POST /wx/feedback/submit
- 评论
  - POST /wx/comment/post
  - GET /wx/comment/count
  - GET /wx/comment/list
- 收藏
  - GET /wx/collect/list
  - POST /wx/collect/addordelete
- 购物车
  - GET /wx/cart/index
  - POST /wx/cart/add
  - POST /wx/cart/fastadd
  - POST /wx/cart/update
  - POST /wx/cart/checked
  - POST /wx/cart/delete
  - GET /wx/cart/goodscount
  - GET /wx/cart/checkout
- 类目
  - GET /wx/catalog/getfirstcategory
  - GET /wx/catalog/getsecondcategory
  - GET /wx/catalog/index
  - GET /wx/catalog/all
  - GET /wx/catalog/current
- 品牌
  - GET /wx/brand/list
  - GET /wx/brand/detail
- 售后
  - GET /wx/aftersale/list
  - GET /wx/aftersale/detail
  - POST /wx/aftersale/submit
  - POST /wx/aftersale/cancel
- 搜索
  - GET /wx/search/index
  - GET /wx/search/helper
  - POST /wx/search/clearhistory
- 专题
  - GET /wx/topic/list
  - GET /wx/topic/detail
  - GET /wx/topic/related
- 首页与公共
  - GET /wx/home/cache
  - GET /wx/home/index
  - GET /wx/home/about
  - GET /wx/index/index
- 微信消息配置
  - GET /wx/msg/config（text/plain）
  - POST /wx/msg/config（application/xml）
- 认证
  - POST /wx/auth/login
  - POST /wx/auth/login_by_weixin
  - POST /wx/auth/regCaptcha
  - POST /wx/auth/register
  - POST /wx/auth/captcha
  - POST /wx/auth/reset

## V1 适配层（/api/v1/**）

- 前台产品
  - GET /api/v1/product/list
  - GET /api/v1/product/{id}
- 管理端产品
  - GET /api/v1/admin/product
  - POST /api/v1/admin/product
  - PUT /api/v1/admin/product/{id}
  - DELETE /api/v1/admin/product/{id}
- 管理端订单
  - GET /api/v1/admin/order/list
  - POST /api/v1/admin/order/{orderId}/ship
  - POST /api/v1/admin/order/{orderId}/audit
  - DELETE /api/v1/admin/order/{orderNo}
- 前台地址
  - GET /api/v1/address/list

---

> 备注  
> - 管理端 /admin/** 接口基于 Shiro 会话鉴权；V1 层可按需迁移至 JWT。  
> - 如需导出为 OpenAPI/Swagger，请告知我进一步生成标准规范文件。 
