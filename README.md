# 城市噪音系统项目概述

## 项目简介

城市噪音系统是一个基于前后端分离架构的现代化Web应用，旨在帮助城市居民提交噪音投诉、管理部门处理投诉、监测噪音数据，以及提供噪音分布可视化。

## 技术架构

### 前端技术栈

- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **地图服务**: 百度地图API
- **状态管理**: Vue 3 Composition API
- **路由**: Vue Router
- **构建工具**: Vite
- **代码规范**: ESLint + Prettier

### 后端技术栈

- **框架**: Spring Boot 2.x
- **ORM框架**: MyBatis Plus
- **数据库**: MySQL
- **认证**: JWT Token
- **API设计**: RESTful风格
- **文件上传**: 本地文件存储
- **跨域处理**: CORS配置

## 系统功能模块

### 1. 用户认证模块

- **登录**: 支持用户名密码登录，返回JWT Token
- **注册**: 新用户注册，自动分配居民角色
- **个人信息管理**: 更新用户信息、头像等
- **权限控制**: 基于JWT Token的权限验证

### 2. 投诉管理模块

- **提交投诉**: 支持地图选择位置、上传图片、选择噪音类型
- **投诉列表**: 查看个人投诉历史，支持状态筛选
- **投诉详情**: 查看投诉处理进度、处理记录
- **投诉评价**: 对已处理的投诉进行评分和评价
- **未解决标记**: 居民可标记未解决的投诉
- **投诉删除**: 居民和管理员可删除投诉

### 3. 工人管理模块

- **任务列表**: 查看分配的投诉任务
- **任务处理**: 处理投诉，添加处理记录
- **处理历史**: 查看历史处理记录

### 4. 管理员模块

- **用户管理**: 查看和管理系统用户
- **投诉管理**: 查看所有投诉，分配任务给工人
- **统计分析**: 投诉数据统计，噪音类型分布分析
- **任务监控**: 监控任务处理状态和进度

### 5. 噪音地图模块

- **投诉点展示**: 在地图上展示投诉位置
- **附近投诉**: 根据当前位置查看附近的投诉点
- **噪音类型筛选**: 按噪音类型筛选投诉点

### 6. 声环境质量模块

- **噪音监测数据**: 查看噪音监测站点数据
- **质量分析**: 声环境质量评估和分析

### 7. 通知模块

- **消息通知**: 系统通知、任务分配通知等
- **通知列表**: 查看历史通知

## 核心业务流程

### 投诉提交流程

1. 居民登录系统
2. 进入投诉提交页面
3. 在地图上选择位置或输入坐标
4. 选择噪音类型
5. 填写详细描述
6. 上传相关图片（最多5张）
7. 提交投诉
8. 系统自动分配任务给相关工人

### 投诉处理流程

1. 工人登录系统
2. 查看分配的任务列表
3. 处理投诉（现场核实、沟通等）
4. 更新处理状态
5. 添加处理记录
6. 标记为已处理
7. 居民收到处理结果通知
8. 居民对处理结果进行评价

### 管理员流程

1. 管理员登录系统
2. 查看投诉统计数据
3. 监控任务处理状态
4. 分配任务给工人
5. 管理用户账号
6. 分析噪音分布情况

## 系统特点

### 1. 地图可视化

- 集成百度地图API，支持地图选点
- 实时地理编码，自动获取地址信息
- 投诉点在地图上直观展示

### 2. 多角色权限管理

- 居民：提交投诉、查看个人投诉、评价处理结果
- 工人：处理分配的任务、添加处理记录
- 管理员：用户管理、任务分配、数据统计

### 3. 响应式设计

- 适配PC端、平板和移动端
- 统一的设计风格和用户体验

### 4. 完整的投诉生命周期管理

- 从提交到处理到评价的完整流程
- 支持投诉状态跟踪和历史记录

### 5. 数据统计分析

- 投诉数据统计和可视化
- 噪音类型分布分析
- 处理效率分析

## 项目结构

### 前端项目结构

```
city_noise_system_frontend/
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 组件
│   │   ├── business/    # 业务组件
│   │   ├── common/      # 通用组件
│   │   └── layout/      # 布局组件
│   ├── services/        # API服务
│   │   └── api/         # API接口
│   ├── views/           # 页面视图
│   │   ├── admin/       # 管理员页面
│   │   ├── auth/        # 认证页面
│   │   ├── complaint/   # 投诉相关页面
│   │   ├── dashboard/   # 仪表盘
│   │   ├── environment/ # 环境数据页面
│   │   ├── home/        # 首页
│   │   ├── notification/# 通知页面
│   │   └── worker/      # 工人页面
│   ├── router/          # 路由配置
│   ├── utils/           # 工具函数
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── public/              # 公共资源
├── index.html           # HTML模板
├── vite.config.js       # Vite配置
└── package.json         # 项目依赖
```

### 后端项目结构

```
city_noise_system/
├── src/main/java/com/example/city_noise_system/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── filter/          # 过滤器
│   ├── handler/         # 异常处理器
│   ├── mapper/          # 数据访问层
│   ├── service/         # 业务逻辑层
│   │   └── impl/        # 实现类
│   ├── utils/           # 工具类
│   ├── vo/              # 视图对象
│   └── CityNoiseSystemApplication.java # 应用入口
├── src/main/resources/  # 资源文件
│   └── application.yml  # 应用配置
├── sql/                 # SQL脚本
├── uploads/             # 文件上传目录
└── pom.xml              # Maven配置
```

## 核心API接口

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息
- `PUT /api/auth/me` - 更新用户信息

### 投诉接口

- `POST /api/complaint` - 提交投诉
- `GET /api/complaint/my` - 获取个人投诉列表
- `GET /api/complaint/{id}` - 获取投诉详情
- `GET /api/complaint/map-points` - 获取地图投诉点
- `POST /api/complaint/{id}/rating` - 评价投诉
- `GET /api/complaint/nearby` - 获取附近投诉点
- `POST /api/complaint/{id}/unsolved` - 标记为未解决

### 工人接口

- `GET /api/worker/tasks` - 获取工人任务列表
- `GET /api/worker/history` - 获取处理历史
- `PUT /api/worker/task/{id}` - 更新任务状态

### 管理员接口

- `GET /api/admin/users` - 获取用户列表
- `GET /api/admin/complaints` - 获取所有投诉
- `GET /api/admin/statistics` - 获取统计数据
- `GET /api/admin/task-monitor` - 获取任务监控数据

### 环境数据接口

- `GET /api/monitor/data` - 获取噪音监测数据
- `GET /api/monitor/stations` - 获取监测站点

## 数据库设计

### 主要表结构

#### users表

- `id` - 用户ID
- `username` - 用户名
- `password` - 密码（加密存储）
- `real_name` - 真实姓名
- `phone` - 手机号
- `role` - 角色（RESIDENT, WORKER, ADMIN）
- `avatar` - 头像
- `create_time` - 创建时间
- `update_time` - 更新时间

#### complaints表

- `id` - 投诉ID
- `user_id` - 提交用户ID
- `title` - 投诉标题
- `description` - 详细描述
- `noise_type` - 噪音类型
- `status` - 状态（PENDING, ASSIGNED, PROCESSING, RESOLVED, CLOSED）
- `create_time` - 创建时间
- `update_time` - 更新时间

#### complaint\_locations表

- `id` - 位置ID
- `complaint_id` - 投诉ID
- `longitude` - 经度
- `latitude` - 纬度
- `district` - 行政区
- `detail_address` - 详细地址

#### complaint\_images表

- `id` - 图片ID
- `complaint_id` - 投诉ID
- `image_url` - 图片路径
- `create_time` - 创建时间

#### handling\_records表

- `id` - 记录ID
- `complaint_id` - 投诉ID
- `worker_id` - 处理工人ID
- `content` - 处理内容
- `create_time` - 创建时间

#### ratings表

- `id` - 评价ID
- `complaint_id` - 投诉ID
- `user_id` - 评价用户ID
- `score` - 评分（1-5）
- `comment` - 评价内容
- `create_time` - 创建时间

#### monitor\_stations表

- `id` - 站点ID
- `name` - 站点名称
- `longitude` - 经度
- `latitude` - 纬度
- `address` - 地址

#### monitor\_data表

- `id` - 数据ID
- `station_id` - 站点ID
- `noise_value` - 噪音值
- `measure_time` - 测量时间

## 部署指南

### 前端部署

1. 安装依赖：`pnpm install`
2. 构建生产版本：`pnpm build`
3. 将构建产物部署到Web服务器（如Nginx）

### 后端部署

1. 编译打包：`mvn clean package`
2. 配置数据库连接（修改application.yml）
3. 运行jar包：`java -jar city-noise-system.jar`

### 环境要求

- **前端**: Node.js 14+，pnpm 6+
- **后端**: JDK 8+，MySQL 5.7+
- **服务器**: 支持Java和静态文件服务

## 项目亮点

1. **完整的前后端分离架构**：采用Vue 3 + Spring Boot的现代技术栈，实现了清晰的前后端分离
2. **地图集成**：集成百度地图API，实现了地图选点、地理编码等功能，提升用户体验
3. **多角色权限管理**：基于JWT的权限控制，实现了居民、工人、管理员三种角色的权限管理
4. **响应式设计**：适配不同设备屏幕，提供一致的用户体验
5. **完整的业务流程**：从投诉提交到处理到评价的完整流程，满足实际业务需求
6. **数据可视化**：提供投诉统计、噪音分布等数据可视化功能，帮助管理部门分析问题
7. **文件上传功能**：支持图片上传，方便居民提供证据
8. **实时通知系统**：及时通知用户投诉处理状态和系统消息

## 未来发展方向

1. **移动应用开发**：开发iOS和Android移动应用，方便用户随时提交投诉
2. **智能噪音监测**：接入实际的噪音监测设备，实现实时噪音数据采集和分析
3. **AI智能分配**：基于机器学习算法，智能分配投诉任务给最合适的工人
4. **社区共治**：增加社区志愿者角色，参与噪音治理
5. **数据分析平台**：构建更强大的数据分析平台，为城市规划提供参考
6. **多语言支持**：增加多语言支持，服务国际化城市
7. **区块链存证**：使用区块链技术对投诉数据进行存证，确保数据不可篡改
8. **智能预测**：基于历史数据，预测噪音热点区域，提前进行干预

## 总结

城市噪音系统是一个功能完整、技术先进的现代化Web应用，旨在解决城市噪音污染问题。通过居民、工人、管理员的协同工作，实现了噪音投诉的高效处理和管理。系统采用了现代的前后端技术栈，提供了良好的用户体验和管理功能。

该项目不仅满足了实际业务需求，也展示了现代Web应用开发的最佳实践，包括前后端分离架构、RESTful API设计、JWT认证、地图集成、响应式设计等技术点。

未来，随着技术的发展和业务需求的变化，系统可以不断扩展和优化，为城市噪音治理提供更加智能、高效的解决方案。
