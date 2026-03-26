@echo off
REM ProjectHub 数据迁移验证脚本 (Windows)

echo ========================================
echo   ProjectHub 数据迁移验证
echo ========================================
echo.

REM 1. 检查 Docker 容器状态
echo 1. 检查 Docker 容器状态
echo ------------------------

docker ps --format "{{.Names}}" | findstr project-manager-postgres >nul
if errorlevel 1 (
    echo X PostgreSQL 容器未运行
    exit /b 1
) else (
    echo √ PostgreSQL 容器运行中
)

docker ps --format "{{.Names}}" | findstr project-manager-redis >nul
if errorlevel 1 (
    echo X Redis 容器未运行
    exit /b 1
) else (
    echo √ Redis 容器运行中
)

echo.

REM 2. 测试数据库连接
echo 2. 测试数据库连接
echo ------------------------

docker exec project-manager-postgres psql -U postgres -d projecthub_dev -c "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo X PostgreSQL 连接失败
    exit /b 1
) else (
    echo √ PostgreSQL 连接成功
)

docker exec project-manager-redis redis-cli -a "Admin_2026" ping 2>nul | findstr PONG >nul
if errorlevel 1 (
    echo X Redis 连接失败
    exit /b 1
) else (
    echo √ Redis 连接成功
)

echo.

REM 3. 检查核心表和数据
echo 3. 检查核心表和数据
echo ------------------------

for %%t in (sys_user project task epic user_story issue wiki_document) do (
    docker exec project-manager-postgres psql -U postgres -d projecthub_dev -c "SELECT 'exists' FROM %%t LIMIT 1;" >nul 2>&1
    if errorlevel 1 (
        echo X %%t 表不存在或无法访问
    ) else (
        echo √ %%t 表可访问
    )
)

echo.

REM 4. 显示数据统计
echo 4. 数据统计
echo ------------------------

echo 正在查询数据统计...
docker exec project-manager-postgres psql -U postgres -d projecthub_dev -c "SELECT 'sys_user' as table_name, count(*) FROM sys_user UNION ALL SELECT 'project', count(*) FROM project UNION ALL SELECT 'task', count(*) FROM task;" 2>nul

echo.
echo ========================================
echo   验证完成
echo ========================================

pause
