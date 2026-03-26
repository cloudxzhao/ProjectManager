@echo off
REM ProjectHub 微服务本地开发启动脚本 (Windows)
REM 使用本地 Docker 容器作为数据库和缓存

echo ========================================
echo   ProjectHub 微服务启动脚本 (本地开发)
echo ========================================
echo.

REM 加载环境变量
if exist .env.local (
    for /f "delims=" %%a in (.env.local) do set %%a
) else if exist .env (
    for /f "delims=" %%a in (.env) do set %%a
) else (
    echo X 错误：找不到 .env.local 或 .env 文件
    exit /b 1
)

echo √ 环境变量已加载
echo.

REM 检查 Docker 容器状态
echo 正在检查 Docker 容器状态...

docker ps --format "{{.Names}}" | findstr project-manager-postgres >nul
if errorlevel 1 (
    echo X PostgreSQL 容器未运行
    exit /b 1
)

docker ps --format "{{.Names}}" | findstr project-manager-redis >nul
if errorlevel 1 (
    echo X Redis 容器未运行
    exit /b 1
)

echo √ Docker 容器运行正常
echo.

REM 显示配置
echo 当前配置:
echo    PostgreSQL: %DB_HOST%:%DB_PORT% / %DB_NAME%
echo    Redis: %REDIS_HOST%:%REDIS_PORT%
echo    Nacos: %NACOS_SERVER%
echo.

REM 进入项目目录
cd /d "%~dp0"

REM 使用 Maven 启动（开发模式）
echo 正在启动微服务...
echo.

REM 启动认证服务（先启动）
start "Auth Service" cmd /k "mvn spring-boot:run -pl services/auth-service -am -Dspring-boot.run.profiles=dev"
timeout /t 10 /nobreak >nul

REM 启动其他服务
start "User Service" cmd /k "mvn spring-boot:run -pl services/user-service -am -Dspring-boot.run.profiles=dev"
start "Project Service" cmd /k "mvn spring-boot:run -pl services/project-service -am -Dspring-boot.run.profiles=dev"
start "Task Service" cmd /k "mvn spring-boot:run -pl services/task-service -am -Dspring-boot.run.profiles=dev"
start "Story Service" cmd /k "mvn spring-boot:run -pl services/story-service -am -Dspring-boot.run.profiles=dev"
start "Issue Service" cmd /k "mvn spring-boot:run -pl services/issue-service -am -Dspring-boot.run.profiles=dev"
start "Wiki Service" cmd /k "mvn spring-boot:run -pl services/wiki-service -am -Dspring-boot.run.profiles=dev"
start "Notification Service" cmd /k "mvn spring-boot:run -pl services/notification-service -am -Dspring-boot.run.profiles=dev"

echo.
echo √ 所有服务已启动
echo 提示：关闭所有窗口可停止服务
echo.

pause
