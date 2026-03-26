# 城市噪音投诉管理系统 - API接口契约文档

## 1. 基本信息

### 1.1 服务信息
- **服务名称**: 城市噪音投诉管理系统
- **服务地址**: `http://localhost:8080`
- **API基础路径**: `/api`
- **认证方式**: JWT Token
- **Token格式**: `Bearer {token}`
- **Token过期时间**: 24小时

### 1.2 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 响应数据
  }
}
```

### 1.3 错误响应格式
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 2. 认证模块

### 2.1 用户登录
- **URL**: `/api/auth/login`
- **方法**: POST
- **请求体**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | username | string | 是 | 用户名 |
  | password | string | 是 | 密码 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "...",
      "userInfo": {
        "id": 1,
        "username": "admin",
        "realName": "管理员",
        "role": "ADMIN",
        "phone": "",
        "avatar": "",
        "createTime": "2026-02-25 10:00:00"
      }
    }
  }
  ```

### 2.2 用户注册
- **URL**: `/api/auth/register`
- **方法**: POST
- **请求体**:
  ```json
  {
    "username": "newuser",
    "password": "password123",
    "realName": "新用户",
    "phone": "13800138000"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | username | string | 是 | 用户名 |
  | password | string | 是 | 密码 |
  | realName | string | 是 | 真实姓名 |
  | phone | string | 否 | 电话号码 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "token": "...",
      "userInfo": {
        "id": 4,
        "username": "newuser",
        "realName": "新用户",
        "role": "RESIDENT",
        "phone": "13800138000",
        "avatar": "",
        "createTime": "2026-02-25 11:00:00"
      }
    }
  }
  ```

### 2.3 获取当前用户信息
- **URL**: `/api/auth/me`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "role": "ADMIN",
      "phone": "",
      "avatar": "",
      "createTime": "2026-02-25 10:00:00"
    }
  }
  ```

### 2.4 更新用户信息
- **URL**: `/api/auth/me`
- **方法**: PUT
- **认证**: 需要JWT Token
- **请求体**:
  ```json
  {
    "realName": "新姓名",
    "phone": "13900139000",
    "avatar": "http://example.com/avatar.jpg"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | realName | string | 是 | 真实姓名 |
  | phone | string | 否 | 电话号码 |
  | avatar | string | 否 | 头像URL |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户信息更新成功",
    "data": null
  }
  ```

### 2.5 用户登出
- **URL**: `/api/auth/logout`
- **方法**: POST
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "登出成功",
    "data": null
  }
  ```

## 3. 管理员模块

### 3.1 获取用户列表
- **URL**: `/api/admin/users`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | roleFilter | string | 否 | - | 角色过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "username": "admin",
          "realName": "管理员",
          "role": "ADMIN",
          "phone": "",
          "avatar": "",
          "createTime": "2026-02-25 10:00:00",
          "deleted": false
        }
      ],
      "total": 3,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 3.2 创建新用户
- **URL**: `/api/admin/users`
- **方法**: POST
- **认证**: 需要JWT Token (ADMIN角色)
- **请求体**:
  ```json
  {
    "username": "newworker",
    "password": "password123",
    "realName": "新处理人员",
    "role": "WORKER",
    "phone": "13800138000"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | username | string | 是 | 用户名 |
  | password | string | 是 | 密码 |
  | realName | string | 是 | 真实姓名 |
  | role | string | 是 | 角色 (ADMIN/WORKER/RESIDENT) |
  | phone | string | 否 | 电话号码 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户创建成功",
    "data": {
      "id": 4,
      "username": "newworker",
      "realName": "新处理人员",
      "role": "WORKER",
      "phone": "13800138000",
      "avatar": "/uploads/default-avatar.png",
      "createTime": "2026-02-25 12:00:00"
    }
  }
  ```

### 3.3 更新用户信息
- **URL**: `/api/admin/users/{id}`
- **方法**: PUT
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 用户ID |
- **请求体**:
  ```json
  {
    "realName": "管理员",
    "role": "ADMIN",
    "phone": "13900139000"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | realName | string | 是 | 真实姓名 |
  | role | string | 是 | 角色 |
  | phone | string | 否 | 电话号码 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户更新成功",
    "data": null
  }
  ```

### 3.4 删除用户
- **URL**: `/api/admin/users/{id}`
- **方法**: DELETE
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 用户ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "用户删除成功",
    "data": null
  }
  ```

### 3.5 获取投诉列表
- **URL**: `/api/admin/complaints`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | status | string | 否 | - | 状态过滤 |
  | district | string | 否 | - | 区域过滤 |
  | noiseType | string | 否 | - | 噪音类型过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "工地噪音",
          "description": "夜间施工噪音",
          "noiseType": "CONSTRUCTION",
          "status": "PENDING",
          "createTime": "2026-02-25 10:00:00",
          "userId": 3,
          "userName": "test1"
        }
      ],
      "total": 5,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 3.6 分配投诉任务
- **URL**: `/api/admin/complaint/{id}/assign`
- **方法**: POST
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | workerId | long | 是 | 处理人员ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "任务分配成功",
    "data": null
  }
  ```

### 3.7 关闭投诉
- **URL**: `/api/admin/complaint/{id}/close`
- **方法**: POST
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | remark | string | 否 | 关闭备注 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "投诉已关闭",
    "data": null
  }
  ```

### 3.8 获取系统统计数据
- **URL**: `/api/admin/statistics`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "totalUsers": 3,
      "totalComplaints": 5,
      "pendingComplaints": 2,
      "processingComplaints": 1,
      "resolvedComplaints": 2
    }
  }
  ```

### 3.9 任务监控接口
- **URL**: `/api/admin/tasks/monitor`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | status | string | 否 | - | 状态过滤 |
  | workerId | long | 否 | - | 处理人员ID过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "工地噪音",
          "description": "夜间施工噪音",
          "noiseType": "CONSTRUCTION",
          "status": "ASSIGNED",
          "createTime": "2026-02-25 10:00:00",
          "userId": 3,
          "userName": "test1"
        }
      ],
      "total": 2,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 3.10 修改任务进度
- **URL**: `/api/admin/task/{id}/progress`
- **方法**: PUT
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体**:
  ```json
  {
    "status": "PROCESSING",
    "remark": "正在处理中，已联系施工方",
    "workerId": 2
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | status | string | 是 | 状态 |
  | remark | string | 是 | 修改原因 |
  | workerId | long | 否 | 处理人员ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "任务进度修改成功",
    "data": null
  }
  ```

### 3.11 查询任务修改历史
- **URL**: `/api/admin/task/{id}/history`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "complaintId": 1,
        "handlerId": 2,
        "handlerName": "worker1",
        "action": "ASSIGNED",
        "remark": "分配给处理人员",
        "createTime": "2026-02-25 11:00:00"
      }
    ]
  }
  ```

### 3.12 按行政区统计投诉数量
- **URL**: `/api/admin/complaints/district-stats`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "海淀区": 10,
      "朝阳区": 5
    }
  }
  ```

### 3.13 按噪音类型统计投诉数量
- **URL**: `/api/admin/complaints/noise-type-stats`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "CONSTRUCTION": 8,
      "TRAFFIC": 5
    }
  }
  ```

### 3.14 获取所有处理人员列表
- **URL**: `/api/admin/workers`
- **方法**: GET
- **认证**: 需要JWT Token (ADMIN角色)
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 2,
        "username": "worker1",
        "realName": "处理人员1",
        "role": "WORKER",
        "phone": "13800138000",
        "avatar": "",
        "createTime": "2026-02-25 10:00:00",
        "deleted": false
      }
    ]
  }
  ```

## 4. 处理人员模块

### 4.1 获取我的任务列表
- **URL**: `/api/worker/tasks`
- **方法**: GET
- **认证**: 需要JWT Token (WORKER角色)
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | status | string | 否 | - | 状态过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "工地噪音",
          "description": "夜间施工噪音",
          "noiseType": "CONSTRUCTION",
          "status": "ASSIGNED",
          "createTime": "2026-02-25 10:00:00",
          "userId": 3,
          "userName": "test1"
        }
      ],
      "total": 2,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 4.2 开始处理投诉
- **URL**: `/api/worker/task/{id}/start`
- **方法**: POST
- **认证**: 需要JWT Token (WORKER角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | remark | string | 否 | 处理备注 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "已开始处理投诉",
    "data": null
  }
  ```

### 4.3 更新处理进展
- **URL**: `/api/worker/task/{id}/update`
- **方法**: POST
- **认证**: 需要JWT Token (WORKER角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体**:
  ```json
  {
    "remark": "正在处理中，已联系施工方"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | remark | string | 是 | 处理备注 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "处理进展已更新",
    "data": null
  }
  ```

### 4.4 完成投诉处理
- **URL**: `/api/worker/task/{id}/resolve`
- **方法**: POST
- **认证**: 需要JWT Token (WORKER角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | remark | string | 否 | 处理备注 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "投诉处理完成",
    "data": null
  }
  ```

### 4.5 获取处理记录
- **URL**: `/api/worker/task/{id}/records`
- **方法**: GET
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "complaintId": 1,
        "handlerId": 2,
        "handlerName": "worker1",
        "action": "ASSIGNED",
        "remark": "分配给处理人员",
        "createTime": "2026-02-25 11:00:00"
      }
    ]
  }
  ```

### 4.6 获取处理历史
- **URL**: `/api/worker/history`
- **方法**: POST
- **认证**: 需要JWT Token (WORKER角色)
- **请求体**:
  ```json
  {
    "status": "COMPLETED",
    "startDate": "2026-02-01",
    "endDate": "2026-02-29",
    "page": 1,
    "pageSize": 10
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | status | string | 否 | 状态过滤 (COMPLETED/PROCESSED) |
  | startDate | string | 否 | 开始日期 |
  | endDate | string | 否 | 结束日期 |
  | page | integer | 是 | 页码 |
  | pageSize | integer | 是 | 每页大小 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "complaintId": 1,
          "complaintTitle": "工地噪音",
          "district": "海淀区",
          "noiseType": "CONSTRUCTION",
          "processTime": "2026-02-25 11:00:00",
          "status": "COMPLETED",
          "result": "处理完成"
        }
      ],
      "total": 5,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

## 5. 投诉模块

### 5.1 提交投诉
- **URL**: `/api/complaint`
- **方法**: POST
- **认证**: 需要JWT Token (RESIDENT或ADMIN角色)
- **请求体** (multipart/form-data):
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | title | string | 是 | 投诉标题 |
  | description | string | 是 | 投诉描述 |
  | noiseType | string | 是 | 噪音类型 |
  | longitude | number | 是 | 经度 |
  | latitude | number | 是 | 纬度 |
  | district | string | 是 | 区域 |
  | detailAddress | string | 是 | 详细地址 |
  | images | file | 否 | 图片文件（最多5个，每个不超过5MB） |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "投诉提交成功",
    "data": {
      "id": 1,
      "images": ["/uploads/20260225_123456_abcdef.png"]
    }
  }
  ```

### 5.2 提交投诉（JSON格式）
- **URL**: `/api/complaint`
- **方法**: POST
- **认证**: 需要JWT Token (RESIDENT或ADMIN角色)
- **请求体**:
  ```json
  {
    "title": "工地噪音",
    "description": "夜间施工噪音严重",
    "noiseType": "CONSTRUCTION",
    "longitude": 116.3974,
    "latitude": 39.9093,
    "district": "海淀区",
    "detailAddress": "中关村大街1号"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | title | string | 是 | 投诉标题 |
  | description | string | 是 | 投诉描述 |
  | noiseType | string | 是 | 噪音类型 |
  | longitude | number | 是 | 经度 |
  | latitude | number | 是 | 纬度 |
  | district | string | 是 | 区域 |
  | detailAddress | string | 是 | 详细地址 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "投诉提交成功",
    "data": {
      "id": 1
    }
  }
  ```

### 5.3 获取我的投诉列表
- **URL**: `/api/complaint/my`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | status | string | 否 | - | 状态过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "工地噪音",
          "description": "夜间施工噪音",
          "noiseType": "CONSTRUCTION",
          "status": "PENDING",
          "createTime": "2026-02-25 10:00:00"
        }
      ],
      "total": 2,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 5.4 获取投诉详情
- **URL**: `/api/complaint/{id}`
- **方法**: GET
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "title": "工地噪音",
      "description": "夜间施工噪音",
      "noiseType": "CONSTRUCTION",
      "status": "PENDING",
      "createTime": "2026-02-25 10:00:00",
      "updateTime": "2026-02-25 10:00:00",
      "userId": 3,
      "userName": "test1",
      "longitude": 116.3974,
      "latitude": 39.9093,
      "district": "海淀区",
      "detailAddress": "中关村大街1号",
      "images": [],
      "handlingRecords": []
    }
  }
  ```

### 5.5 获取地图投诉点
- **URL**: `/api/complaint/map-points`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "longitude": 116.3974,
        "latitude": 39.9093,
        "title": "工地噪音",
        "noiseType": "CONSTRUCTION",
        "status": "PENDING"
      }
    ]
  }
  ```

### 5.6 获取附近投诉
- **URL**: `/api/complaint/nearby`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | longitude | number | 是 | 经度 |
  | latitude | number | 是 | 纬度 |
  | radius | integer | 是 | 半径(米) |
  | noiseType | string | 否 | 噪音类型 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "longitude": 116.3974,
        "latitude": 39.9093,
        "title": "工地噪音",
        "noiseType": "CONSTRUCTION",
        "status": "PENDING"
      }
    ]
  }
  ```

### 5.7 上传投诉图片
- **URL**: `/api/complaint/{id}/upload-image`
- **方法**: POST
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体** (multipart/form-data):
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | file | file | 是 | 图片文件（不超过5MB，支持JPG、PNG、GIF、WEBP格式） |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "图片上传成功",
    "data": {
      "imageUrl": "/uploads/20260225_123456_abcdef.png"
    }
  }
  ```

### 5.8 评价投诉
- **URL**: `/api/complaint/{id}/rating`
- **方法**: POST
- **认证**: 需要JWT Token (RESIDENT角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体**:
  ```json
  {
    "score": 5,
    "comment": "处理及时，效果很好"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | score | integer | 是 | 评分(1-5) |
  | comment | string | 是 | 评价内容 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "评价成功",
    "data": null
  }
  ```

### 5.9 标记投诉为未解决
- **URL**: `/api/complaint/{id}/unsolved`
- **方法**: POST
- **认证**: 需要JWT Token (RESIDENT角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体**:
  ```json
  {
    "reason": "问题依然存在"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | reason | string | 是 | 未解决原因 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "已标记为未解决，处理人员将重新处理",
    "data": null
  }
  ```

### 5.10 删除投诉
- **URL**: `/api/complaint/{id}/delete`
- **方法**: POST
- **认证**: 需要JWT Token (RESIDENT或ADMIN角色)
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 投诉ID |
- **请求体**:
  ```json
  {
    "reason": "误提交"
  }
  ```
- **参数说明**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | reason | string | 是 | 删除原因 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "投诉已成功删除",
    "data": null
  }
  ```

## 6. 监控模块

### 6.1 获取所有监测点
- **URL**: `/api/monitor/stations`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "stationName": "监测点1",
        "longitude": 116.3974,
        "latitude": 39.9093,
        "district": "海淀区",
        "functionalZone": "1类区",
        "address": "中关村大街1号"
      }
    ]
  }
  ```

### 6.2 获取监测点地图点
- **URL**: `/api/monitor/map-points`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "stationName": "监测点1",
        "longitude": 116.3974,
        "latitude": 39.9093,
        "district": "海淀区",
        "functionalZone": "1类区",
        "address": "中关村大街1号",
        "noiseLevel": 55.5,
        "monitorTime": "2026-02-25 10:00:00",
        "periodType": "DAY"
      }
    ]
  }
  ```

### 6.3 获取声标准数据
- **URL**: `/api/monitor/standards`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "functionalZone": "1类区",
        "dayLimit": 55,
        "nightLimit": 45
      }
    ]
  }
  ```

### 6.4 获取超标监测点
- **URL**: `/api/monitor/over-standard`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "stationId": 1,
        "stationName": "监测点1",
        "address": "中关村大街1号",
        "noiseLevel": 60.5,
        "limitValue": 55,
        "periodType": "DAY",
        "monitorTime": "2026-02-25 10:00:00",
        "functionalZone": "1类区"
      }
    ]
  }
  ```

### 6.5 获取监测点最新数据
- **URL**: `/api/monitor/data`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | stationId | long | 是 | 监测点ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "stationId": 1,
        "noiseLevel": 55.5,
        "monitorTime": "2026-02-25 10:00:00",
        "periodType": "DAY"
      }
    ]
  }
  ```

### 6.6 获取监测点24小时数据
- **URL**: `/api/monitor/hourly-data`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | stationId | long | 是 | 监测点ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "hours": ["00:00", "01:00", ..., "23:00"],
      "values": [45.5, 46.2, ..., 50.8]
    }
  }
  ```

### 6.7 生成模拟数据
- **URL**: `/api/monitor/generate-data`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "模拟数据生成成功",
    "data": null
  }
  ```

## 7. 公共模块

### 7.1 获取我的通知列表
- **URL**: `/api/common/notifications`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
  | isRead | boolean | 否 | - | 是否已读 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "投诉处理通知",
          "content": "您的投诉已分配给处理人员",
          "isRead": false,
          "createTime": "2026-02-25 11:00:00"
        }
      ],
      "total": 5,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 7.2 获取未读通知数量
- **URL**: `/api/common/notifications/unread-count`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": 3
  }
  ```

### 7.3 标记通知为已读
- **URL**: `/api/common/notifications/{id}/read`
- **方法**: PUT
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 通知ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "通知已标记为已读",
    "data": null
  }
  ```

### 7.4 标记所有通知为已读
- **URL**: `/api/common/notifications/read-all`
- **方法**: PUT
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "所有通知已标记为已读",
    "data": null
  }
  ```

### 7.5 获取监测点列表
- **URL**: `/api/common/monitor-stations`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | district | string | 否 | 区域过滤 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "stationName": "监测点1",
        "longitude": 116.3974,
        "latitude": 39.9093,
        "district": "海淀区",
        "functionalZone": "1类区",
        "address": "中关村大街1号"
      }
    ]
  }
  ```

### 7.6 生成模拟监测数据
- **URL**: `/api/common/monitor-data/simulate`
- **方法**: POST
- **认证**: 需要JWT Token (ADMIN角色)
- **响应**:
  ```json
  {
    "code": 200,
    "message": "模拟数据生成成功",
    "data": null
  }
  ```

### 7.7 获取首页概览数据
- **URL**: `/api/common/dashboard/overview`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "userRole": "admin",
      "message": "管理员概览数据待实现"
    }
  }
  ```

### 7.8 获取所有行政区列表
- **URL**: `/api/common/districts`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "name": "江岸区",
        "code": "001"
      },
      {
        "name": "江汉区",
        "code": "002"
      }
    ]
  }
  ```

## 8. 通知模块

### 8.1 获取用户的消息列表
- **URL**: `/api/notification/list`
- **方法**: GET
- **认证**: 需要JWT Token
- **查询参数**:
  | 参数名 | 类型 | 必填 | 默认值 | 描述 |
  |--------|------|------|--------|------|
  | page | integer | 否 | 1 | 页码 |
  | size | integer | 否 | 10 | 每页大小 |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "投诉处理通知",
          "content": "您的投诉已分配给处理人员",
          "isRead": false,
          "createTime": "2026-02-25 11:00:00"
        }
      ],
      "total": 5,
      "size": 10,
      "current": 1,
      "pages": 1
    }
  }
  ```

### 8.2 标记消息为已读
- **URL**: `/api/notification/{id}/read`
- **方法**: POST
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 通知ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": null
  }
  ```

### 8.3 获取未读消息数量
- **URL**: `/api/notification/unread-count`
- **方法**: GET
- **认证**: 需要JWT Token
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": 3
  }
  ```

### 8.4 删除通知
- **URL**: `/api/notification/{id}`
- **方法**: DELETE
- **认证**: 需要JWT Token
- **路径参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | long | 是 | 通知ID |
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": null
  }
  ```

## 9. 数据类型定义

### 9.1 用户角色
- `ADMIN`: 管理员
- `WORKER`: 处理人员
- `RESIDENT`: 居民

### 9.2 投诉状态
- `PENDING`: 待处理
- `ASSIGNED`: 已分配
- `PROCESSING`: 处理中
- `RESOLVED`: 已解决
- `CLOSED`: 已关闭

### 9.3 噪音类型
- `CONSTRUCTION`: 施工噪音
- `TRAFFIC`: 交通噪音
- `LIVING`: 生活噪音
- `COMMERCIAL`: 商业噪音
- `INDUSTRIAL`: 工业噪音

## 10. 错误码定义

| 错误码 | 含义 | 描述 |
|--------|------|------|
| 400 | 参数错误 | 请求参数错误或无效 |
| 401 | 未认证 | 未提供Token或Token无效 |
| 403 | 无权限 | 用户角色无权限访问该接口 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器内部错误 | 服务器处理请求时发生错误 |

## 11. 接口调用示例

### 11.1 登录示例
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 11.2 获取用户信息示例
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {token}"
```

### 11.3 提交投诉示例
```bash
curl -X POST http://localhost:8080/api/complaint \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "工地噪音",
    "description": "夜间施工噪音",
    "noiseType": "CONSTRUCTION",
    "longitude": 116.3974,
    "latitude": 39.9093,
    "district": "海淀区",
    "detailAddress": "中关村大街1号"
  }'
```

## 12. 安全注意事项

1. **Token安全**: Token应妥善保管，避免泄露
2. **权限控制**: 严格按照用户角色分配权限
3. **参数验证**: 所有输入参数必须进行严格验证
4. **文件上传**: 限制上传文件类型和大小
5. **SQL注入**: 使用参数化查询，避免SQL注入攻击
6. **跨域安全**: 正确配置CORS策略

## 13. 版本控制

| 版本 | 日期 | 描述 |
|------|------|------|
| 1.0 | 2026-02-25 | 初始版本 |
| 1.1 | 2026-03-18 | 更新API契约文档，与实际实现保持一致 |