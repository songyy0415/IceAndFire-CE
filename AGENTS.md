
# AGENTS.md

# Minecraft Mod Fabric Migration Agent

## 0. 项目目标

本项目目标：

> 按用户要求修改一个 已经经过Minecraft 1.21.1 + yarn + Architectury 架构 Mod 的 Fabric 部分迁移到 Minecraft 26.2，同时保持 Architectury 架构的项目

要求：

- 保留 Architectury common/fabric/neoforge 结构
- 只修改 Fabric 相关实现
- 不将项目转换为纯 Fabric
- 不删除 Architectury API 抽象层
- 保持未来多加载器兼容能力

目标：



```

---

# 1. 输入资料


将提供：

## Minecraft源码

### Source

```

minecraft-1.21.1-mojmap "D:\aimoddev\minecraft\1.21.1"

```

用于：

- 查找旧Minecraft API
- 对照类名
- 分析方法变化


### Target

```

minecraft-26.2-mojmap "D:\aimoddev\minecraft\26.2"

```

用于：

- 查找新API
- 确认官方实现
- 替换废弃接口

neoforge 1.21.1 "D:\aimoddev\neoforge\NeoForge1.21.1"
用于：
进行同步上游任务时的参考

---

# 2. 依赖项目
## fabric loader
通用 "D:\aimoddev\fabric_lodaer\fabric-loader"
## fabric api
fabric api for 1.21.1 "D:\aimoddev\fabric_api\fabric-api-1.21.1"
fabric api for 26.2 "D:\aimoddev\fabric_api\fabric-api26.2"

## architectury
architectury api for 1.21.1 "D:\aimoddev\architectury\architectury-api-1.21"
architectury for 26.2 "D:\aimoddev\architectury\architectury-api-26.2"
## ponder
ponder26.2包含在create-fly项目中 "D:\aimoddev\createfly\Create-Fly"
## Uranus
Uranus for mc26.2 "D:\aimoddev\Uranus\Uranus26.2"
Uranus for mc1.21.1 "D:\aimoddev\Uranus\Uranus1.21.1"
## Jupiter
Jupiter for 26.2 "D:\aimoddev\jupiter\Jupiter"
用途：

- API依赖
- Fabric实现依赖








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

bugfix/feat/chore/等其他表示这个commit性质的词语: xxx

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

