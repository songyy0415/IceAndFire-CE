
# AGENTS.md

# Minecraft Mod Fabric Migration Agent

## 0. 项目目标

本项目目标：

> 修复一个 已经经过Minecraft 1.21.1 + yarn + Architectury 架构 Mod 的 Fabric 部分迁移到 Minecraft 26.2，同时保持 Architectury 架构的项目的bug

修复要求：

- 保留 Architectury common/fabric/neoforge 结构
- 只修改 Fabric 相关实现
- 不将项目转换为纯 Fabric
- 不删除 Architectury API 抽象层
- 保持未来多加载器兼容能力

目标：



```

---

# 1. 输入资料
原项目地址:"D:\aiminecraftdev\IceAndFire-CE"(移植前)

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


# 6. 编译策略


不要一次修所有错误。


使用阶段编译：



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

bugfix: xxx

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

