# Claude Code 新增特性汇总

> 本文档整理了 Claude Code 各版本新增的 "Added" 特性，按版本号倒序排列。

---

## 2.1.74

| 特性 | 说明 |
|------|------|
| `/context` 命令改进 | 添加了可操作的建议 — 识别上下文繁重的工具、内存膨胀和容量警告，并提供具体优化提示 |
| `autoMemoryDirectory` 设置 | 可配置自定义目录用于自动内存存储 |

---

## 2.1.73

| 特性 | 说明 |
|------|------|
| `modelOverrides` 设置 | 将模型选择器条目映射到自定义提供者模型 ID（如 Bedrock 推理配置文件 ARN） |
| OAuth/SSL 错误指导 | 当 OAuth 登录或连接检查因 SSL 证书错误失败时提供可操作指导（企业代理、`NODE_EXTRA_CA_CERTS`） |

---

## 2.1.72

| 特性 | 说明 |
|------|------|
| `/copy` 命令 `w` 键 | 在 `/copy` 中添加 `w` 键，将选中的内容直接写入文件，绕过剪贴板（SSH 下有用） |
| `/plan` 描述参数 | 为 `/plan` 添加可选描述参数（如 `/plan fix the auth bug`），进入计划模式并立即开始 |
| `ExitWorktree` 工具 | 用于离开 `EnterWorktree` 会话 |
| `CLAUDE_CODE_DISABLE_CRON` | 环境变量，立即停止会话中计划的 cron 作业 |
| Bash 自动批准列表 | 添加 `lsof`、`pgrep`、`tput`、`ss`、`fd`、`fdfind` 到 bash 自动批准允许列表，减少常见只读操作的权限提示 |
| Git URL 支持 | 支持不带 `.git` 后缀的 marketplace git URL（Azure DevOps、AWS CodeCommit） |

---

## 2.1.71

| 特性 | 说明 |
|------|------|
| `/loop` 命令 | 按固定间隔运行提示或斜杠命令（如 `/loop 5m check the deploy`） |
| Cron 调度工具 | 用于会话内重复提示的 cron 调度工具 |
| `voice:pushToTalk` 键绑定 | 使语音激活键可重新绑定（默认：空格）— 修饰符 + 字母组合如 `meta+k` 零输入干扰 |
| Bash 自动批准列表 | 添加 `fmt`、`comm`、`cmp`、`numfmt`、`expr`、`test`、`printf`、`getconf`、`seq`、`tsort`、`pr` |

---

## 2.1.70

> 此版本无新增特性记录

---

## 2.1.69

| 特性 | 说明 |
|------|------|
| `/claude-api` 技能 | 使用 Claude API 和 Anthropic SDK 构建应用程序的技能 |
| Ctrl+U 退出 bash 模式 | 在空 bash 提示符（`!`）上按 Ctrl+U 退出 bash 模式，匹配 `escape` 和 `backspace` |
| 数字键盘支持 | 支持数字小键盘选择 Claude 访谈问题中的选项（之前只有 QWERTY 上方的数字行有效） |
| `/remote-control` 名称参数 | 为 `/remote-control` 添加可选名称参数（如 `/remote-control My Project` 或 `--name "My Project"`）设置自定义会话标题，在 claude.ai/code 可见 |
| Voice STT 多语言支持 | 语音识别新增 10 种语言（共 20 种）— 俄语、波兰语、土耳其语、荷兰语、乌克兰语、希腊语、捷克语、丹麦语、瑞典语、挪威语 |
| 努力程度显示 | 在 logo 和 spinner 上显示努力程度（如"with low effort"），更容易看清哪个努力设置处于活动状态 |
| Agent 名称显示 | 使用 `claude --agent` 时在终端标题中显示 agent 名称 |
| `sandbox.enableWeakerNetworkIsolation` | 设置（仅 macOS）允许 Go 程序如 `gh`、`gcloud`、`terraform` 在使用自定义 MITM 代理配合 `httpProxyPort` 时验证 TLS 证书 |
| `includeGitInstructions` | 设置（和 `CLAUDE_CODE_DISABLE_GIT_INSTRUCTIONS` 环境变量）从 Claude 系统提示中移除内置的提交和 PR 工作流指令 |
| `/reload-plugins` 命令 | 无需重启即可激活待处理的插件更改 |
| Claude Code Desktop 提示 | 一次性启动提示，建议 macOS 和 Windows 用户使用 Claude Code Desktop（最多显示 3 次，可关闭） |
| `${CLAUDE_SKILL_DIR}` 变量 | 技能可引用自己的目录在 SKILL.md 内容中 |
| `InstructionsLoaded` 钩子事件 | 当 CLAUDE.md 或 `.claude/rules/*.md` 文件加载到上下文时触发 |
| `agent_id` 和 `agent_type` | 添加到钩子事件（用于子 agent 和 `--agent`） |
| `worktree` 字段 | 添加到状态行钩子命令，包含名称、路径、分支和在 `--worktree` 会话中的原始仓库目录 |
| `pluginTrustMessage` | 在管理设置中添加到插件信任警告中的组织特定上下文，在安装前显示 |
| 策略限制获取 | 为 Team plan OAuth 用户添加策略限制获取（如远程控制限制），不仅限于 Enterprise |
| `pathPattern` | 添加到 `strictKnownMarketplaces` 用于正则匹配文件/目录 marketplace 源 alongside `hostPattern` 限制 |
| 插件源类型 `git-subdir` | 指向 git 仓库中的子目录 |
| `oauth.authServerMetadataUrl` | MCP 服务器的 OAuth 元数据发现 URL 配置选项，当标准发现失败时使用 |

---

## 2.1.68

> 此版本无新增特性记录

---

## 2.1.66

> 此版本无新增特性记录

---

## 2.1.63

| 特性 | 说明 |
|------|------|
| `/simplify` 和 `/batch` | 捆绑的斜杠命令 |
| `ENABLE_CLAUDEAI_MCP_SERVERS` | 环境变量设为 false 可选择退出使 claude.ai MCP 服务器可用 |
| HTTP 钩子 | 可 POST JSON 到 URL 并接收 JSON，而不是运行 shell 命令 |
| MCP OAuth 手动 URL 粘贴 | MCP OAuth 认证期间的手动 URL 粘贴回退。如果自动 localhost 重定向不起作用，可粘贴回调 URL 完成认证 |
| `/copy` 始终复制完整响应 | `/copy` 选择器中添加"始终复制完整响应"选项。选中后，未来的 `/copy` 命令将跳过代码块选择器并直接复制完整响应 |

---

## 2.1.62 / 2.1.61 / 2.1.59

| 特性 | 说明 |
|------|------|
| `/copy` 命令 | 当存在代码块时显示交互式选择器，允许选择单个代码块或完整响应 |

---

## 2.1.58 / 2.1.56 / 2.1.55 / 2.1.53 / 2.1.52

> 这些版本无新增特性记录

---

## 2.1.51

| 特性 | 说明 |
|------|------|
| `claude remote-control` | 为外部构建添加子命令，启用本地环境服务供所有用户使用 |
| 自定义 npm 注册表 | 从 npm 源安装插件时支持自定义 npm 注册表和特定版本固定 |
| 账户信息环境变量 | 添加 `CLAUDE_CODE_ACCOUNT_UUID`、`CLAUDE_CODE_USER_EMAIL`、`CLAUDE_CODE_ORGANIZATION_UUID` 环境变量，为 SDK 调用者同步提供账户信息，消除早期遥测事件缺少账户元数据的竞争条件 |

---

## 2.1.50

| 特性 | 说明 |
|------|------|
| `startupTimeout` 配置 | 支持 LSP 服务器的启动超时配置 |
| `WorktreeCreate`/`WorktreeRemove` | 钩子事件，当 agent worktree 隔离创建或移除 worktrees 时启用自定义 VCS 设置和拆卸 |
| `isolation: worktree` | 支持 agent 定义中的 `isolation: worktree`，允许 agent 声明式地在隔离的 git worktrees 中运行 |
| `claude agents` CLI | 列出所有配置的 agent 的 CLI 命令 |
| `CLAUDE_CODE_DISABLE_1M_CONTEXT` | 环境变量禁用 1M 上下文窗口支持 |

---

## 2.1.49

| 特性 | 说明 |
|------|------|
| `--worktree` / `-w` 标志 | 在隔离的 git worktree 中启动 Claude |
| Ctrl+F 键绑定 | 终止后台 agent（两次按键确认） |
| `ConfigChange` 钩子事件 | 配置文件在会话期间更改时触发，启用企业安全审计和可选的设置更改阻止 |

---

## 2.1.47

| 特性 | 说明 |
|------|------|
| `last_assistant_message` | 添加到 Stop 和 SubagentStop 钩子输入，提供最终助手响应文本，使钩子可访问而无需解析转录文件 |
| `chat:newline` 键绑定 | 可配置的多行输入（anthropics/claude-code#26075） |
| `added_dirs` | 添加到状态行 JSON `workspace` 部分，通过 `/add-dir` 添加的目录暴露给外部脚本（anthropics/claude-code#26096） |

---

## 2.1.46

| 特性 | 说明 |
|------|------|
| claude.ai MCP 连接器 | 支持在 Claude Code 中使用 claude.ai MCP 连接器 |

---

## 2.1.45

| 特性 | 说明 |
|------|------|
| Claude Sonnet 4.6 | 支持 Claude Sonnet 4.6 |
| `enabledPlugins` 和 `extraKnownMarketplaces` | 支持从 `--add-dir` 目录加载 |
| `spinnerTipsOverride` | 自定义 spinner 提示 — 用 `tips` 配置自定义提示字符串数组，可选设置 `excludeDefault: true` 仅显示自定义提示而非内置提示 |
| SDKRateLimitInfo/Event | SDK 中的类型，使消费者能够接收限制状态更新，包括利用率、重置时间和超额信息 |

---

## 2.1.44 / 2.1.43

> 这些版本无新增特性记录

---

## 2.1.42

| 特性 | 说明 |
|------|------|
| Opus 4.6 努力呼吁 | 为符合条件的用户一次性 Opus 4.6 努力呼吁 |

---

## 2.1.41

| 特性 | 说明 |
|------|------|
| 防止嵌套会话 | 防止在另一个 Claude Code 会话内启动 Claude Code |
| OTel 速度属性 | OTel 事件和跟踪跨度中的 `speed` 属性，用于快速模式可见性 |
| `claude auth` 子命令 | `claude auth login`、`claude auth status`、`claude auth logout` CLI 子命令 |
| Windows ARM64 | Windows ARM64 (win32-arm64) 原生二进制支持 |

---

## 2.1.39 / 2.1.38 / 2.1.37 / 2.1.36 / 2.1.34

> 这些版本无新增特性记录

---

## 2.1.33

| 特性 | 说明 |
|------|------|
| `TeammateIdle`/`TaskCompleted` | 多 agent 工作流的钩子事件 |
| 子 agent 限制 | 支持通过 agent 的"tools" frontmatter 中的 `Task(agent_type)` 语法限制可生成的子 agent |
| `memory` frontmatter | agent 的 frontmatter 字段支持，启用持久内存，作用域为 `user`、`project` 或 `local` |
| 插件名称显示 | 技能描述和 `/skills` 菜单中添加插件名称，提高可发现性 |

---

## 2.1.32

| 特性 | 说明 |
|------|------|
| Agent Teams | 研究预览 agent 团队功能，用于多 agent 协作（令牌密集型功能，需要设置 `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1`） |
| "从这里总结" | 消息选择器中添加"从这里总结"，允许部分会话总结 |

---

## 2.1.31

| 特性 | 说明 |
|------|------|
| 会话恢复提示 | 退出时会话恢复提示，显示如何稍后继续对话 |
| 全角空格输入 | 支持日语 IME 的全角（zenkaku）空格输入在复选框选择中 |

---

## 2.1.30

| 特性 | 说明 |
|------|------|
| PDF `pages` 参数 | Read 工具中为 PDF 添加 `pages` 参数，允许读取特定页面范围（如 `pages: "1-5"`）。大 PDF（>10 页）在 `@` 提及时返回轻量级引用而非内联到上下文 |
| MCP OAuth 预配置凭证 | 为不支持动态客户端注册（DCR）的 MCP 服务器（如 Slack）添加预配置的 OAuth 客户端凭证。使用 `--client-id` 和 `--client-secret` 配合 `claude mcp add` |
| `/debug` 命令 | 帮助 Claude 排查当前会话 |
| Git 只读标志 | 支持只读模式下更多 `git log` 和 `git show` 标志（如 `--topo-order`、`--cherry-pick`、`--format`、`--raw`） |
| Task 指标 | Task 工具结果中添加令牌计数、工具使用和持续时间指标 |
| 减少动画模式 | 配置中支持减少动画模式 |

---

## 2.1.29 / 2.1.27

| 特性 | 说明 |
|------|------|
| 调试日志 | 工具调用失败和拒绝添加到调试日志 |
| `--from-pr` 标志 | 恢复链接到特定 GitHub PR 编号或 URL 的会话 |

---

## 2.1.25 / 2.1.23

| 特性 | 说明 |
|------|------|
| 自定义 spinner | 自定义 spinner 动词设置（`spinnerVerbs`） |

---

## 2.1.22 / 2.1.21

| 特性 | 说明 |
|------|------|
| 全角数字输入 | 支持日语 IME 的全角（zenkaku）数字输入在选项选择提示中 |

---

## 2.1.20

| 特性 | 说明 |
|------|------|
| 箭头键历史导航 | vim 普通模式下箭头键历史导航，当光标无法进一步移动时 |
| 外部编辑器快捷键 | 帮助菜单中添加外部编辑器快捷键（Ctrl+G）提高可发现性 |
| PR 审查状态指示器 | 提示页脚添加 PR 审查状态指示器，显示当前分支的 PR 状态（已批准、要求更改、待处理或草稿）为彩色圆点和可点击链接 |
| `--add-dir` 支持 `CLAUDE.md` | 支持从 `--add-dir` 标志指定的额外目录加载 `CLAUDE.md` 文件（需要设置 `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1`） |
| 任务删除 | 通过 `TaskUpdate` 工具删除任务的能力 |

---

## 2.1.19

| 特性 | 说明 |
|------|------|
| `CLAUDE_CODE_ENABLE_TASKS` | 环境变量设为 `false` 暂时保留旧系统 |
| 自定义命令参数 | 自定义命令中支持简写 `$0`、`$1` 等访问单个参数 |

---

## 2.1.18

| 特性 | 说明 |
|------|------|
| 自定义键盘快捷键 | 可自定义键盘快捷键。按上下文配置键绑定，创建和弦序列，个性化工作流。运行 `/keybindings` 开始 |

---

## 2.1.17

> 此版本无新增特性记录

---

## 2.1.16

| 特性 | 说明 |
|------|------|
| 任务管理系统 | 新任务管理系统，包括依赖跟踪等新功能 |

---

## 2.1.15

| 特性 | 说明 |
|------|------|
| npm 安装弃用通知 | npm 安装的弃用通知 — 运行 `claude install` 或查看 https://docs.anthropic.com/en/docs/claude-code/getting-started |

---

## 2.1.14

| 特性 | 说明 |
|------|------|
| Bash 模式历史自动补全 | 在 bash 模式（`!`）中基于历史的自动补全 — 输入部分命令并按 Tab 从 bash 命令历史中补全 |
| 插件列表搜索 | 已安装插件列表支持搜索 — 输入按名称或描述过滤 |
| Git commit SHA 固定 | 支持将插件固定到特定 git commit SHA，允许 marketplace 条目安装确切版本 |

---

## 2.1.12 / 2.1.11

> 这些版本无新增特性记录

---

## 2.1.10

| 特性 | 说明 |
|------|------|
| `Setup` 钩子事件 | 新 `Setup` 钩子事件，可通过 `--init`、`--init-only` 或 `--maintenance` CLI 标志触发，用于仓库设置和维护操作 |
| OAuth URL 复制 | 登录期间浏览器未自动打开时，按键盘快捷键 'c' 复制 OAuth URL |

---

## 2.1.9

| 特性 | 说明 |
|------|------|
| `auto:N` 语法 | 配置 MCP 工具搜索自动启用阈值，N 为上下文窗口百分比（0-100） |
| `plansDirectory` | 自定义计划文件存储位置的设置 |
| 外部编辑器支持 | AskUserQuestion"Other"输入字段中的外部编辑器支持（Ctrl+G） |
| PR/提交会话 URL | 从 web 会话创建的提交和 PR 添加会话 URL 归属 |
| `PreToolUse` 钩子 | 支持 `PreToolUse` 钩子返回 `additionalContext` 给模型 |
| `${CLAUDE_SESSION_ID}` | 技能可访问当前会话 ID 的字符串替换 |

---

## 2.1.7

| 特性 | 说明 |
|------|------|
| `showTurnDuration` | 隐藏轮次持续时间消息（如"Cooked for 1m 6s"） |
| 权限提示反馈 | 接受权限提示时提供反馈的能力 |
| Agent 内联响应 | Agent 最终响应内联显示在任务通知中，更容易看清结果而无需阅读完整转录文件 |

---

## 2.1.6

| 特性 | 说明 |
|------|------|
| `/config` 搜索 | `/config` 命令的搜索功能，快速过滤设置 |
| `/doctor` 更新部分 | `/doctor` 中的更新部分，显示自动更新频道和可用 npm 版本（stable/latest） |
| `/stats` 日期范围 | `/stats` 命令的日期范围过滤 — 按 `r` 循环选择过去 7 天、过去 30 天和全部时间 |
| 嵌套技能发现 | 在子目录中工作时从嵌套的 `.claude/skills` 目录自动发现技能 |
| 上下文窗口百分比 | 状态行输入中添加 `context_window.used_percentage` 和 `context_window.remaining_percentage` 字段，更容易显示上下文窗口 |
| 编辑器错误显示 | 当 Ctrl+G 编辑器失败时的错误显示 |

---

## 2.1.5

| 特性 | 说明 |
|------|------|
| `CLAUDE_CODE_TMPDIR` | 环境变量覆盖内部临时文件使用的临时目录，适用于自定义临时目录要求的环境 |

---

## 2.1.4

| 特性 | 说明 |
|------|------|
| `CLAUDE_CODE_DISABLE_BACKGROUND_TASKS` | 环境变量禁用所有后台任务功能，包括自动后台和 Ctrl+B 快捷键 |

---

## 2.1.3

| 特性 | 说明 |
|------|------|
| 发布频道切换 | `/config` 中的发布频道（`stable` 或`latest`）切换 |
| 不可达权限规则警告 | 检测不可达权限规则的警告，`/doctor` 中警告并在保存规则后显示，包括每个规则的来源和可操作的修复指导 |

---

## 2.1.2

| 特性 | 说明 |
|------|------|
| 拖拽图片源路径 | 拖拽到终端的图片添加源路径元数据，帮助 Claude 了解图片来源 |
| 文件路径超链接 | 支持 OSC 8 的终端中工具输出中的文件路径可点击超链接（如 iTerm） |
| Windows winget 支持 | 支持 Windows Package Manager (winget) 安装，自动检测和更新指令 |
| Shift+Tab 计划模式 | 计划模式中 Shift+Tab 快捷键快速选择"自动接受编辑"选项 |
| `FORCE_AUTOUPDATE_PLUGINS` | 环境变量允许插件自动更新，即使主自动更新器禁用 |
| `agent_type` | 添加到 SessionStart 钩子输入，如果指定 `--agent` 则填充 |

---

## 2.1.0

| 特性 | 说明 |
|------|------|
| 技能自动热重载 | `~/.claude/skills` 或 `.claude/skills` 中创建或修改的技能立即可用，无需重启会话 |
| 技能分叉子 agent | 支持使用 `context: fork` 在技能 frontmatter 中在分叉的子 agent 上下文中运行技能和斜杠命令 |
| 技能 agent 字段 | 支持技能中的 `agent` 字段指定执行的 agent 类型 |
| `language` 设置 | 配置 Claude 响应语言（如 `language: "japanese"`） |
| `respectGitignore` | `settings.json` 中支持 `respectGitignore`，按项目控制 `@` 提及文件选择器行为 |
| `IS_DEMO` | 环境变量从 UI 隐藏邮箱和组织，适用于流式传输或录制会话 |
| Bash 通配符权限 | 使用 `*` 在规则中任何位置进行 Bash 工具权限的通配符模式匹配（如 `Bash(npm *)`、`Bash(* install)`、`Bash(git * main)`） |
| Ctrl+B 统一后台 | 统一 bash 命令和 agent 的 Ctrl+B 后台 — 按 Ctrl+B 现在同时后台化所有运行的前台任务 |
| MCP `list_changed` | 支持 MCP `list_changed` 通知，允许 MCP 服务器动态更新可用工具、提示和资源，无需重新连接 |
| `/teleport`/`/remote-env` | claude.ai 订阅者的斜杠命令，允许恢复和配置远程会话 |
| 禁用特定 agent | 支持使用 `Task(AgentName)` 语法在 settings.json 权限或 `--disallowedTools` CLI 标志中禁用特定 agent |
| Agent hooks | hooks 支持 agent frontmatter，允许 agent 定义 PreToolUse、PostToolUse 和 Stop hooks 限定于 agent 生命周期 |
| 技能/命令 hooks | hooks 支持技能和斜杠命令 frontmatter |
| Vim 动作 | 新 Vim 动作：`;`和`,`重复 f/F/t/T 动作，`y` 操作符用于复制（`yy`/`Y`），`p`/`P` 粘贴，文本对象（`iw`、`aw`、`iW`、`aW`、`i"`、`a"`、`i'`、`a'`、`i(`、`a(`、`i[`、`a[`、`i{`、`a{`），`>>`和`<<` 缩进/取消缩进，`J` 连接行 |
| `/plan` 命令 | 直接从提示启用计划模式的 `/plan` 命令快捷方式 |
| `/` 自动补全 | 支持 `/` 出现在输入中任何位置时的斜杠命令自动补全，不仅限于开头 |
| `--tools` 标志 | 交互模式下 `--tools` 标志支持，限制 Claude 在交互式会话期间可使用的内置工具 |
| `CLAUDE_CODE_FILE_READ_MAX_OUTPUT_TOKENS` | 环境变量覆盖默认文件读取令牌限制 |
| `once: true` | 支持钩子配置的`once: true` |
| YAML 风格列表 | frontmatter `allowed-tools` 字段支持 YAML 风格列表，技能声明更简洁 |
| 插件 hooks | 支持插件的提示和 agent 钩子类型（之前仅支持命令钩子） |
| iTerm2 图片粘贴 | iTerm2 中支持 Cmd+V 图片粘贴（映射到 Ctrl+V） |
| 对话框箭头导航 | 对话框中使用左右箭头键循环选项卡 |
| 实时思维块 | Ctrl+O 转录模式中实时思维块显示 |
| 后台 bash 完整输出 | 后台 bash 任务详细信息对话框中的文件路径完整输出 |
| 技能上下文可视化 | 技能作为上下文可视化中的单独类别 |

---

## 2.0.76 / 2.0.75

> 这些版本无新增特性记录

---

## 2.0.74

| 特性 | 说明 |
|------|------|
| LSP 工具 | 语言服务器协议工具，用于代码智能功能，如转到定义、查找引用和悬停文档 |
| `/terminal-setup` | 支持 Kitty、Alacritty、Zed 和 Warp 终端 |
| `ctrl+t` 主题切换 | `/theme` 中 ctrl+t 快捷键切换语法高亮开/关 |
| 主题选择器语法高亮 | 主题选择器中添加语法高亮信息 |
| macOS Alt 快捷键指导 | 当 Alt 快捷键因终端配置失败时为 macOS 用户提供指导 |

---

## 2.0.73

| 特性 | 说明 |
|------|------|
| 图片链接 | 可点击的 `[Image #N]` 链接，在默认查看器中打开附加图片 |
| alt-y yank-pop | alt-y 循环杀死环历史在 ctrl-y yank 之后 |
| 插件发现搜索 | 插件发现屏幕的搜索过滤（输入按名称、描述或 marketplace 过滤） |
| 自定义会话 ID | 使用 `--session-id` 结合 `--resume` 或 `--continue` 和 `--fork-session` 时分叉会话时支持自定义会话 ID |

---

## 2.0.72

| 特性 | 说明 |
|------|------|
| Claude in Chrome | Claude in Chrome (Beta) 功能，与 Chrome 扩展配合使用（https://claude.ai/chrome），允许直接从 Claude Code 控制浏览器 |
| 移动应用二维码 | 移动应用提示中可扫描的 QR 码，用于快速应用下载 |
| 恢复加载指示器 | 恢复对话时的加载指示器，更好的反馈 |

---

## 2.0.71

| 特性 | 说明 |
|------|------|
| `/config` 提示建议 | /config 切换启用/禁用提示建议 |
| `/settings` 别名 | `/settings` 作为 `/config` 命令的别名 |

---

## 2.0.70

| 特性 | 说明 |
|------|------|
| Enter 键建议 | Enter 键立即接受并提交提示建议（tab 仍接受编辑） |
| MCP 通配符语法 | MCP 工具权限的通配符语法 `mcp__server__*`，允许或拒绝服务器的所有工具 |
| 插件 marketplace 自动更新 | 插件 marketplace 的自动更新切换，允许每个 marketplace 控制自动更新 |
| `current_usage` | 状态行输入中的 `current_usage` 字段，启用准确的上下文窗口百分比计算 |

---

## 2.0.69 / 2.0.68

> 这些版本无新增特性记录

---

## 2.0.67

| 特性 | 说明 |
|------|------|
| `/permissions` 搜索 | `/permissions` 命令的搜索功能，带 `/` 快捷键按工具名称过滤规则 |

---

## 2.0.65

| 特性 | 说明 |
|------|------|
| 切换模型 | 编写提示时使用 alt+p（linux、windows）、option+p（macos）切换模型的能力 |
| 上下文窗口信息 | 状态行中的上下文窗口信息 |
| `fileSuggestion` | `@` 文件搜索命令的自定义 `fileSuggestion` 设置 |
| `CLAUDE_CODE_SHELL` | 环境变量覆盖自动 shell 检测（当登录 shell 与实际工作 shell 不同时很有用） |

---

## 2.0.64

| 特性 | 说明 |
|------|------|
| 命名会话 | 命名会话支持：使用 `/rename` 命名会话，在 REPL 中 `/resume <name>` 或从终端`claude --resume <name>`恢复 |
| `.claude/rules/` | 支持 `.claude/rules/`。详见 https://code.claude.com/docs/en/memory |
| 图片维度元数据 | 图片调整大小时添加维度元数据，启用大图片的准确坐标映射 |

---

## 2.0.62

| 特性 | 说明 |
|------|------|
| "(Recommended)" 指示 | 多项选择题中的"(Recommended)"指示，推荐选项移至列表顶部 |
| `attribution` 设置 | 自定义提交和 PR 署名行的 `attribution` 设置（弃用 `includeCoAuthoredBy`） |

---

## 2.0.61

> 此版本无新增特性记录

---

## 2.0.60

| 特性 | 说明 |
|------|------|
| 后台 agent | 后台 agent 支持。Agent 在后台运行，同时您可以工作 |
| `--disable-slash-commands` | CLI 标志禁用所有斜杠命令 |
| 模型名称提交 | "Co-Authored-By"提交消息中的模型名称 |

---

## 2.0.59

| 特性 | 说明 |
|------|------|
| `--agent` CLI 标志 | 为当前会话覆盖 agent 设置的 `--agent` CLI 标志 |
| `agent` 设置 | 使用特定 agent 的系统提示、工具限制和模型配置主线程 |

---

## 2.0.58

> 此版本无新增特性记录

---

## 2.0.57

| 特性 | 说明 |
|------|------|
| 计划反馈 | 拒绝计划时的反馈输入，允许用户告诉 Claude 需要更改什么 |

---

## 2.0.56

| 特性 | 说明 |
|------|------|
| 终端进度条设置 | 启用/禁用终端进度条（OSC 9;4）的设置 |

---

## 2.0.55 / 2.0.54 / 2.0.52

> 这些版本无新增特性记录

---

## 2.0.51

| 特性 | 说明 |
|------|------|
| Opus 4.5 | Opus 4.5！详见 https://www.anthropic.com/news/claude-opus-4-5 |

---

## 2.0.50

> 此版本无新增特性记录

---

## 2.0.49

| 特性 | 说明 |
|------|------|
| ctrl-y 粘贴 | readline 风格的 ctrl-y 粘贴删除的文本 |

---

## 2.0.47 / 2.0.46

> 这些版本无新增特性记录

---

## 2.0.45

| 特性 | 说明 |
|------|------|
| Microsoft Foundry | 支持 Microsoft Foundry！详见 https://code.claude.com/docs/en/azure-ai-foundry |
| `PermissionRequest` 钩子 | 自动批准或拒绝工具权限请求的自定义逻辑钩子 |

---

## 2.0.43

| 特性 | 说明 |
|------|------|
| `permissionMode` | 自定义 agent 的 `permissionMode` 字段 |
| `tool_use_id` | `PreToolUseHookInput` 和 `PostToolUseHookInput` 类型中的 `tool_use_id` 字段 |
| 技能 frontmatter | 声明子 agent 自动加载技能的技能 frontmatter 字段 |
| `SubagentStart` | `SubagentStart` 钩子事件 |

---

## 2.0.42

| 特性 | 说明 |
|------|------|
| `agent_id`/`agent_transcript_path` | `SubagentStop` 钩子中的 `agent_id` 和 `agent_transcript_path` 字段 |

---

## 2.0.41

| 特性 | 说明 |
|------|------|
| 钩子模型参数 | 提示驱动的停止钩子的 `model` 参数，允许用户为钩子评估指定自定义模型 |

---

## 2.0.37 / 2.0.36 / 2.0.35

| 特性 | 说明 |
|------|------|
| `CLAUDE_CODE_EXIT_AFTER_STOP_DELAY` | 环境变量在指定的空闲持续时间后自动退出 SDK 模式，适用于自动化工作流和脚本 |

---

## 2.0.34 / 2.0.33 / 2.0.32

| 特性 | 说明 |
|------|------|
| `companyAnnouncements` | 启动时显示公告的 `companyAnnouncements` 设置 |

---

## 2.0.31

> 此版本无新增特性记录

---

## 2.0.30

| 特性 | 说明 |
|------|------|
| macOS keychain 提示 | 当 macOS 上遇到 API 密钥错误时，提示运行 `security unlock-keychain` |
| `allowUnsandboxedCommands` | 沙箱设置，在策略级别禁用 `dangerouslyDisableSandbox` 逃逸舱口 |
| `disallowedTools` | 自定义 agent 定义中的 `disallowedTools` 字段，用于显式工具阻止 |
| 提示驱动停止钩子 | 提示驱动的停止钩子 |

---

## 2.0.28 / 2.0.27

> 这些版本无新增特性记录

---

**文档生成时间：** 2026-03-13
**来源：** https://github.com/anthropics/claude-code/blob/main/CHANGELOG.md
