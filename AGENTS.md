
# AGENTS.md

# Minecraft Mod Fabric Migration Agent

## 0. 项目目标

本项目目标：

> 将一个 Minecraft 1.21.1 + Mojmap + Architectury 架构 Mod 的 Fabric 部分迁移到 Minecraft 26.2，同时保持 Architectury 架构。

迁移要求：

- 保留 Architectury common/fabric/neoforge 结构
- 只修改 Fabric 相关实现
- 不将项目转换为纯 Fabric
- 不删除 Architectury API 抽象层
- 保持未来多加载器兼容能力

目标：

```

Minecraft 1.21.1 Mojmap
|
v
Minecraft 26.2 Mojmap

Fabric Loader
Fabric API
Architectury API
Architectury Loom

```

---

# 1. 输入资料

将提供：

## Minecraft源码

### Source

```

minecraft-1.21.1-mojmap "D:\aiminecraftdev\minecraft1.21.1mojmap"

```

用于：

- 查找旧Minecraft API
- 对照类名
- 分析方法变化


### Target

```

minecraft-26.2-mojmap "D:\aiminecraftdev\minecraft26.2"

```

用于：

- 查找新API
- 确认官方实现
- 替换废弃接口


---

# 2. 依赖项目
## fabric loader
通用 "D:\aiminecraftdev\fabric-loader-0.19.3"
## fabric api
fabric api for 1.21.1 "D:\aiminecraftdev\fabric-api1.21.1"
fabric api for 26.2 "D:\aiminecraftdev\fabric-api-0.156.0-26.2"

## architectury
architectury for 1.21.1 "D:\aiminecraftdev\architectury-api-1.21"
architectury for 26.2 "D:\aiminecraftdev\architectury-api26.2"
## ponder
ponder26.2包含在create-fly项目中 "D:\aiminecraftdev\Create-Fly"
## Uranus
Uranus for mc26.2 "D:\aiminecraftdev\Uranus26.2"
Uranus for mc1.21.1 "D:\aiminecraftdev\Uranus1.21.1"
用途：

- API依赖
- Fabric实现依赖

特点：

```

API变化较小

```

处理策略：

优先直接迁移。

---

## Jupiter
Jupiter for 26.2 "D:\aiminecraftdev\Jupiter new"
Jupiter for 1.21.1 "D:\aiminecraftdev\Jupiter old"
用途：

核心API依赖。

特点：

```

API变化较大

```

处理策略：

必须：

1. 对比两个版本源码
2. 建立API变化表
3. 分析替代方案
4. 修改调用代码

禁止：

- 猜测API
- 简单删除调用
- 用空实现绕过


---

## EMI
emi for 1.21.1 "D:\aiminecraftdev\emi1.21.1"
emi for 26.2 "D:\aiminecraftdev\emi26.2"
处理：

- Recipe API
- Plugin API
- Client integration


检查：

- emi api package变化
- plugin入口变化
- registry变化


---

## Trinkets
trinkets for 1.21.1 "D:\aiminecraftdev\trinkets1.21.1"
trinkets for 26.2 "D:\aiminecraftdev\trinkets26.2"
处理：

- Slot API
- Component API
- Entity integration


检查：

- TrinketComponent变化
- SlotGroup变化
- Callback变化


---

## Jade
jade for 1.21.1 "D:\aiminecraftdev\Jade1.21.1"
jade for 26.2 "D:\aiminecraftdev\Jade26.2"
处理：

- Client plugin
- Block entity data
- Tooltip provider


检查：

- plugin注册方式
- API package变化


---

## Integration
通用 "D:\aiminecraftdev\Integration"

特殊说明：

Integration 基于 Loader API。

特点：

```

不依赖Minecraft版本API

```

迁移原则：

优先保持：

- loader entrypoint
- Fabric API调用

避免：

- 修改核心逻辑


---

# 3. 项目结构要求


必须保持：

```

project
|
|-- common
|
|-- fabric
|
|-- neoforge
|
|-- gradle

```

禁止：

- 删除common
- 移动common代码到fabric
- 删除neoforge模块
- 改成单loader项目


---

# 4. 迁移原则


## 优先级


按照以下顺序：

```

P0 环境确认

P1 Gradle / Loom / Architectury升级

P2 Fabric Loader / Fabric API适配

P3 外部依赖适配

P4 Minecraft API迁移

P5 Fabric实现迁移

P6 Client迁移

P7 测试验证

```


---

# P0 环境检查


确认：

- Java版本
- Gradle版本
- Architectury Loom版本
- Fabric Loader版本
- Fabric API版本


执行：

```

./gradlew tasks
./gradlew buildEnvironment

```


记录：

- 当前依赖版本
- mappings版本


---

# P1 Architectury迁移


检查：

```

architectury {
minecraft = ...
}

```


确认：

- common模块正常
- fabric模块正常
- neoforge模块正常


禁止：

直接替换Architectury为Fabric Loom。


---

# P2 Fabric API迁移


检查：

## Registry

旧：

```

Registry.register

```

新版本：

确认：

- BuiltInRegistries变化
- ResourceLocation变化


---

## Events

检查：

- ClientTickEvents
- ServerTickEvents
- LifecycleEvents
- EntityEvents


---

## Networking

检查：

- ServerPlayNetworking
- ClientPlayNetworking
- Payload API变化


---

# P3 Minecraft API迁移


所有Minecraft API变化必须：

1. 查找1.21.1实现
2. 查找26.2实现
3. 对比调用方式
4. 修改


重点检查：

## ResourceLocation

旧：

```

new ResourceLocation()

```

新：

```

ResourceLocation.fromNamespaceAndPath()

```


---

## Components

旧：

```

Text

```

新：

```

Component

```


---

## World

旧：

```

Level

```

确认：

- ServerLevel
- ClientLevel


---

## Registry

检查：

- Holder
- HolderSet
- RegistryAccess


---

# P4 Jupiter适配


必须创建：

```

docs/jupiter-migration.md

```


记录：

格式：

```

旧API:

xxx()

新API:

yyy()

修改位置:

file.java

原因:

xxx

```


---

# P5 Fabric代码迁移


重点目录：

```

fabric/src/main/java

```


检查：

- FabricInitializer
- ClientInitializer
- Mixins
- AccessWidener
- Fabric Events
- Networking
- Rendering


---

# P6 Client迁移


重点：

```

fabric/client

```


检查：

- Renderer
- Model
- Screen
- Keybind
- Particle
- Shader
- HUD


---

# 5. Mixin规则


所有Mixin：

必须检查：

- target class变化
- method signature变化
- injection point变化


禁止：

直接删除Mixin。


如果Mixin失效：

记录：

```

docs/mixin-migration.md

```


格式：

```

Mixin:

xxxMixin

旧target:

xxx

新target:

xxx

解决方案:

```


---

# 6. 编译策略


不要一次修所有错误。


使用阶段编译：


## 第一阶段

```

./gradlew compileJava

```


解决：

- 类不存在
- package变化


---

## 第二阶段

解决：

- 方法签名变化


---

## 第三阶段

解决：

- 泛型
- Holder
- Registry


---

## 第四阶段

运行：

```

./gradlew build

```


---

# 7. AI工作规则


Claude Code必须：

## 修改前

说明：

```

文件:
原因:
API变化:
修改方案:

```


---

## 修改后

报告：

```

修改文件:
修改内容:
测试结果:

```


---

## 禁止行为


禁止：

- 删除功能
- 添加空实现
- TODO代替实现
- 注释掉代码解决错误
- 猜测API


---

# 8. Git要求


每完成一个阶段：

创建commit：

格式：

```

migration: migrate xxx to MC26.2

```


例如：

```

migration: update Fabric networking API
migration: migrate Jupiter integration

```


---

# 9. 最终验收


必须满足：


Build:

```

./gradlew build

```


运行：

Fabric 26.2客户端启动成功


检查：

- Mod加载成功
- Uranus正常
- Jupiter功能正常
- EMI显示正常
- Trinkets正常
- Jade提示正常
- Integration正常


---

# End
```

---

