# Mantle 1.21.1 移植版

本项目是 **Mantle** 面向 **Minecraft 1.21.1 + NeoForge** 的社区移植版本，对照官方 `v1.11.117` 并保留本分支的平台适配和本地修复。

该项目主要用于为 Tinkers' Construct 1.21.1 移植版提供前置依赖，不属于 Mantle 官方发布版本。

## 项目信息

- Minecraft：`1.21.1`
- Mod Loader：`NeoForge`
- 移植版本：`1.12.4`
- NeoForge：`21.1.241`
- Java：`21`
- 对应 Tinkers' Construct：`3.12.5`
- 状态：开发与适配中，部分内容可能尚未完成

这是开发版，不保证与旧存档 / 旧版本互通。上游同步的当前运行矩阵仍有 15 项 `not_run`，不代表全部客户端和专服场景已经验收。

## 项目关系

Mantle 与 Tinkers' Construct 是两个独立模组，需要分别构建和安装：

```text
Mantle-1.21.1/build/libs/Mantle-*.jar
TinkersConstruct-1.21.1/build/libs/TinkersConstruct-*.jar
```

运行 Tinkers' Construct 时，必须同时安装对应版本的 Mantle。

## 构建

构建需要 JDK 21。Mantle 可在自身项目目录中独立构建，也可由相邻的 Tinkers' Construct 项目作为复合构建子项目构建。

独立构建时，在 Mantle 项目目录中执行：

```bash
gradlew build
```

## 上游项目

- Mantle：https://github.com/SlimeKnights/Mantle
- Tinkers' Construct：https://github.com/SlimeKnights/TinkersConstruct

## 许可

本项目沿用上游 Mantle 的 MIT License。
