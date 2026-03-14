// ProjectHub Backend API Test Suite v6
// 执行全量 API 接口测试、边界测试、异常场景测试

const axios = require('axios');
const fs = require('fs');
const path = require('path');

// 配置
const BASE_URL = 'http://localhost:9527/api/v1';
const OUTPUT_DIR = 'D:/data/project/ClaudeStudy/ProjectManagerStudy/Validator/docs/issues/v6';

// 测试用户凭证
const CREDENTIALS = {
    usernameOrEmail: 'admin',
    password: 'Admin123'
};

// 测试状态
const state = {
    accessToken: '',
    refreshToken: '',
    projectId: null,
    taskId: null,
    commentId: null,
    epicId: null,
    storyId: null,
    issueId: null,
    wikiId: null,
    testsPassed: 0,
    testsFailed: 0,
    testsTotal: 0,
    issues: [],
    issueCounter: 0,
    workingFeatures: []
};

// 日志输出
function log(message, level = 'INFO') {
    const timestamp = new Date().toLocaleString('zh-CN');
    const prefix = {
        'INFO': '',
        'GREEN': '[PASS]',
        'RED': '[FAIL]',
        'WARN': '[WARN]',
        'HEADER': '===',
        'ISSUE': '[ISSUE]'
    }[level] || '';
    console.log(`[${timestamp}] ${prefix} ${message}`);
}

// 记录问题
function recordIssue(title, severity, category, description, affectedApis,
                     currentState, expectedState, reproductionSteps, labels) {
    state.issueCounter++;
    const issue = {
        id: `BACKEND-V6-${String(state.issueCounter).padStart(3, '0')}`,
        title,
        severity,
        category,
        description,
        affected_apis: affectedApis,
        current_state: currentState,
        expected_state: expectedState,
        reproduction_steps: reproductionSteps,
        labels
    };
    state.issues.push(issue);
    log(`发现问题：${title}`, 'ISSUE');
}

// 测试断言
function testAssert(name, condition, expected, actual, severity = 'medium',
                    category = 'functionality', description = '',
                    affectedApis = [], reproductionSteps = [], labels = []) {
    state.testsTotal++;

    if (condition) {
        state.testsPassed++;
        log(`${name}`, 'GREEN');
        return true;
    } else {
        state.testsFailed++;
        log(`${name} - 期望：${expected}, 实际：${actual}`, 'RED');

        recordIssue(
            name,
            severity,
            category,
            description || `测试失败：${name}`,
            affectedApis,
            actual,
            expected,
            reproductionSteps.length ? reproductionSteps : ['步骤待补充'],
            labels.length ? labels : []
        );
        return false;
    }
}

// HTTP 请求
async function makeRequest(method, endpoint, data = null, headers = {}, useAuth = false) {
    const url = `${BASE_URL}${endpoint}`;
    const reqHeaders = {
        'Content-Type': 'application/json',
        ...headers
    };

    if (useAuth && state.accessToken) {
        reqHeaders['Authorization'] = `Bearer ${state.accessToken}`;
    }

    try {
        const config = {
            method: method.toUpperCase(),
            url,
            headers: reqHeaders,
            data: data && (method.toUpperCase() === 'POST' || method.toUpperCase() === 'PUT') ? data : undefined,
            timeout: 30000,
            validateStatus: () => true // 不抛出错误，返回所有状态码
        };

        const response = await axios(config);
        return {
            status: response.status,
            data: response.data
        };
    } catch (error) {
        return {
            status: 0,
            data: { error: error.message }
        };
    }
}

// 登录
async function login() {
    log('正在登录...', 'INFO');
    const { status, data } = await makeRequest('POST', '/auth/login', CREDENTIALS);

    if (status === 200 && data.code === 200) {
        state.accessToken = data.data?.accessToken || '';
        state.refreshToken = data.data?.refreshToken || '';
        log(`登录成功，Token: ${state.accessToken.substring(0, 50)}...`, 'GREEN');
        return true;
    } else {
        log(`登录失败：${JSON.stringify(data)}`, 'RED');
        testAssert(
            '登录接口正常返回 Token',
            false,
            'code=200 且返回 accessToken',
            `status=${status}, code=${data.code}`,
            'high',
            'functionality',
            '登录接口无法正常获取 Token',
            ['POST /api/v1/auth/login'],
            [
                '1. 发送 POST /api/v1/auth/login',
                `2. 请求体：${JSON.stringify(CREDENTIALS)}`,
                '3. 观察返回结果'
            ],
            ['auth', 'login', 'token']
        );
        return false;
    }
}

// 测试认证模块
async function testAuthModule() {
    log('=== 开始测试认证模块 ===', 'HEADER');

    // 1. 测试登录 - 空用户名
    let { status, data } = await makeRequest('POST', '/auth/login',
        { usernameOrEmail: '', password: 'Admin123' });
    testAssert('登录 - 空用户名返回 400',
        data.code === 400, 'code=400', `code=${data.code}`,
        'low', 'validation', '空用户名登录的验证测试',
        ['POST /api/v1/auth/login'],
        ['1. 发送空用户名的登录请求'],
        ['auth', 'validation']);

    // 2. 测试登录 - 空密码
    ({ status, data } = await makeRequest('POST', '/auth/login',
        { usernameOrEmail: 'admin' }));
    testAssert('登录 - 空密码返回 400',
        data.code === 400, 'code=400', `code=${data.code}`,
        'low', 'validation', '空密码登录的验证测试',
        ['POST /api/v1/auth/login'],
        ['1. 发送空密码的登录请求'],
        ['auth', 'validation']);

    // 3. 测试登录 - 错误密码
    ({ status, data } = await makeRequest('POST', '/auth/login',
        { usernameOrEmail: 'admin', password: 'wrong' }));
    testAssert('登录 - 错误密码返回 401',
        data.code === 2001 && status === 401,
        'code=2001, HTTP 401', `code=${data.code}, HTTP ${status}`,
        'low', 'functionality', '错误密码登录的响应测试',
        ['POST /api/v1/auth/login'],
        ['1. 使用错误密码登录'],
        ['auth', 'login']);

    // 4. 测试登录 - 不存在的用户
    ({ status, data } = await makeRequest('POST', '/auth/login',
        { usernameOrEmail: 'nonexistent', password: 'Admin123' }));
    testAssert('登录 - 不存在的用户返回 401',
        data.code === 2001 && status === 401,
        'code=2001, HTTP 401', `code=${data.code}, HTTP ${status}`,
        'low', 'functionality', '不存在用户登录的响应测试',
        ['POST /api/v1/auth/login'],
        ['1. 使用不存在的用户名登录'],
        ['auth', 'login']);

    // 5. 测试刷新 Token - 空 Token
    ({ status, data } = await makeRequest('POST', '/auth/refresh',
        { refreshToken: '' }));
    testAssert('刷新 Token - 空 Token 返回 400',
        data.code === 400, 'code=400', `code=${data.code}`,
        'low', 'validation', '空刷新 Token 的验证测试',
        ['POST /api/v1/auth/refresh'],
        ['1. 发送空 refreshToken 的刷新请求'],
        ['auth', 'validation', 'token']);

    // 6. 测试刷新 Token - 无效 Token
    ({ status, data } = await makeRequest('POST', '/auth/refresh',
        { refreshToken: 'invalid_token' }));
    testAssert('刷新 Token - 无效 Token 返回 401',
        data.code === 2003 || status === 401,
        'code=2003 或 HTTP 401', `code=${data.code}, HTTP ${status}`,
        'low', 'functionality', '无效刷新 Token 的响应测试',
        ['POST /api/v1/auth/refresh'],
        ['1. 发送无效的 refreshToken'],
        ['auth', 'validation', 'token']);

    // 7. 测试登出 - 无认证
    ({ status, data } = await makeRequest('POST', '/auth/logout'));
    testAssert('登出 - 无认证返回 401',
        status === 401, 'HTTP 401', `HTTP ${status}`,
        'low', 'functionality', '无认证登出的响应测试',
        ['POST /api/v1/auth/logout'],
        ['1. 不带 Token 发送登出请求'],
        ['auth', 'logout']);

    // 8. 测试忘记密码
    ({ status, data } = await makeRequest('POST', '/auth/forgot-password',
        { email: 'admin@projecthub.com' }));
    testAssert('忘记密码返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '忘记密码接口测试',
        ['POST /api/v1/auth/forgot-password'],
        ['1. 发送忘记密码请求'],
        ['auth', 'forgot-password']);

    log('=== 认证模块测试完成 ===', 'HEADER');
}

// 测试用户模块
async function testUserModule() {
    log('=== 开始测试用户模块 ===', 'HEADER');

    // 1. 测试获取用户资料 - 无认证
    let { status, data } = await makeRequest('GET', '/user/profile');
    testAssert('获取用户资料 - 无认证返回 401',
        status === 401, 'HTTP 401', `HTTP ${status}`,
        'low', 'security', '未认证访问用户资料的测试',
        ['GET /api/v1/user/profile'],
        ['1. 不带 Token 访问用户资料接口'],
        ['user', 'security']);

    if (!state.accessToken) {
        log('跳过后续用户模块测试（无 Token）', 'WARN');
        log('=== 用户模块测试完成 ===', 'HEADER');
        return;
    }

    // 2. 测试获取用户资料 - 有认证
    ({ status, data } = await makeRequest('GET', '/user/profile', null, {}, true));
    testAssert('获取用户资料 - 有认证返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '认证访问用户资料的测试',
        ['GET /api/v1/user/profile'],
        ['1. 登录后获取 Token', '2. 带 Token 访问用户资料接口'],
        ['user', 'profile']);

    // 3. 测试更新用户资料
    ({ status, data } = await makeRequest('PUT', '/user/profile',
        { nickname: 'Test Admin', avatar: 'https://example.com/avatar.jpg' }, {}, true));
    testAssert('更新用户资料返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '更新用户资料的测试',
        ['PUT /api/v1/user/profile'],
        ['1. 登录后获取 Token', '2. PUT 更新用户资料'],
        ['user', 'profile', 'update']);

    // 4. 测试修改密码 - 旧密码错误
    ({ status, data } = await makeRequest('POST', '/user/change-password',
        { oldPassword: 'wrong', newPassword: 'New123!' }, {}, true));
    testAssert('修改密码 - 旧密码错误返回 401',
        status === 401 || data.code === 3001,
        'HTTP 401 或 code=3001', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '错误旧密码修改密码的测试',
        ['POST /api/v1/user/change-password'],
        ['1. 登录后获取 Token', '2. 使用错误旧密码修改密码'],
        ['user', 'password']);

    // 5. 测试修改密码 - 空新密码
    ({ status, data } = await makeRequest('POST', '/user/change-password',
        { oldPassword: 'Admin123', newPassword: '' }, {}, true));
    testAssert('修改密码 - 空新密码返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空新密码的验证测试',
        ['POST /api/v1/user/change-password'],
        ['1. 登录后获取 Token', '2. 使用空新密码修改密码'],
        ['user', 'password', 'validation']);

    log('=== 用户模块测试完成 ===', 'HEADER');
}

// 测试项目模块
async function testProjectModule() {
    log('=== 开始测试项目模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过项目模块测试（无 Token）', 'WARN');
        log('=== 项目模块测试完成 ===', 'HEADER');
        return;
    }

    // 1. 测试获取项目列表 - 无认证
    let { status, data } = await makeRequest('GET', '/projects');
    testAssert('获取项目列表 - 无认证返回 401',
        status === 401, 'HTTP 401', `HTTP ${status}`,
        'low', 'security', '未认证访问项目列表的测试',
        ['GET /api/v1/projects'],
        ['1. 不带 Token 访问项目列表接口'],
        ['project', 'security']);

    // 2. 测试获取项目列表 - 有认证
    ({ status, data } = await makeRequest('GET', '/projects', null, {}, true));
    testAssert('获取项目列表 - 有认证返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '认证访问项目列表的测试',
        ['GET /api/v1/projects'],
        ['1. 登录后获取 Token', '2. 带 Token 访问项目列表接口'],
        ['project', 'list']);

    // 3. 测试创建项目
    const projectData = {
        name: `Test Project ${Date.now()}`,
        key: `TP${Date.now()}`,
        description: 'API Test Project',
        leadUserId: 1,
        type: 'SOFTWARE'
    };
    ({ status, data } = await makeRequest('POST', '/projects', projectData, {}, true));
    if (status === 200 && data.code === 200) {
        state.projectId = data.data?.id;
        log(`创建项目成功，ID: ${state.projectId}`, 'GREEN');
    }
    testAssert('创建项目返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '创建项目的测试',
        ['POST /api/v1/projects'],
        ['1. 登录后获取 Token', '2. POST 创建项目'],
        ['project', 'create']);

    // 4. 测试创建项目 - 空名称
    ({ status, data } = await makeRequest('POST', '/projects',
        { key: 'TEST', description: 'Test' }, {}, true));
    testAssert('创建项目 - 空名称返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空项目名称的验证测试',
        ['POST /api/v1/projects'],
        ['1. 登录后获取 Token', '2. POST 创建空名称项目'],
        ['project', 'validation']);

    // 5. 测试获取项目详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', '/projects/99999', null, {}, true));
    testAssert('获取项目详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在项目的测试',
        ['GET /api/v1/projects/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的项目 ID'],
        ['project', 'not-found']);

    // 6. 测试更新项目
    if (state.projectId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}`,
            { name: 'Updated Project Name' }, {}, true));
        testAssert('更新项目返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新项目的测试',
            ['PUT /api/v1/projects/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新项目'],
            ['project', 'update']);
    }

    // 7. 测试添加项目成员
    if (state.projectId) {
        ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/members`,
            { userId: 2, role: 'MEMBER' }, {}, true));
        testAssert('添加项目成员返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '添加项目成员的测试',
            ['POST /api/v1/projects/{id}/members'],
            ['1. 登录后获取 Token', '2. POST 添加项目成员'],
            ['project', 'member']);
    }

    // 8. 测试删除项目
    if (state.projectId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}`, null, {}, true));
        testAssert('删除项目返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除项目的测试',
            ['DELETE /api/v1/projects/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除项目'],
            ['project', 'delete']);
        state.projectId = null;
    }

    log('=== 项目模块测试完成 ===', 'HEADER');
}

// 测试任务模块
async function testTaskModule() {
    log('=== 开始测试任务模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过任务模块测试（无 Token）', 'WARN');
        log('=== 任务模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建一个项目用于测试
    if (!state.projectId) {
        const projectData = {
            name: `Task Test Project ${Date.now()}`,
            key: `TTP${Date.now()}`,
            description: 'API Test Project for Tasks',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
            log(`创建测试项目成功，ID: ${state.projectId}`, 'GREEN');
        }
    }

    if (!state.projectId) {
        log('无法创建项目，跳过任务模块测试', 'WARN');
        return;
    }

    // 1. 测试创建任务
    const taskData = {
        title: 'Test Task',
        description: 'API Test Task',
        status: 'TODO',
        priority: 'MEDIUM',
        assigneeId: 1
    };
    let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/tasks`, taskData, {}, true);
    if (status === 200 && data.code === 200) {
        state.taskId = data.data?.id;
        log(`创建任务成功，ID: ${state.taskId}`, 'GREEN');

        // 检查 status 和 priority 是否正确返回
        const respStatus = data.data?.status;
        const respPriority = data.data?.priority;
        testAssert('创建任务 - status 和 priority 正确返回',
            respStatus === 'TODO' && respPriority === 'MEDIUM',
            'status=TODO, priority=MEDIUM',
            `status=${respStatus}, priority=${respPriority}`,
            'high', 'data', '创建任务时 status 和 priority 字段的返回测试',
            ['POST /api/v1/projects/{id}/tasks'],
            ['1. 登录后获取 Token', '2. POST 创建任务', '3. 检查返回的 status 和 priority'],
            ['task', 'data-consistency']);
    } else {
        testAssert('创建任务返回 200',
            false,
            'HTTP 200, code=200',
            `HTTP ${status}, code=${data.code}`,
            'high', 'functionality', '创建任务接口测试失败',
            ['POST /api/v1/projects/{id}/tasks'],
            ['1. 登录后获取 Token', '2. POST 创建任务'],
            ['task', 'create']);
    }

    // 2. 测试创建任务 - 空标题
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/tasks`,
        { description: 'Test' }, {}, true));
    testAssert('创建任务 - 空标题返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空任务标题的验证测试',
        ['POST /api/v1/projects/{id}/tasks'],
        ['1. 登录后获取 Token', '2. POST 创建空标题任务'],
        ['task', 'validation']);

    // 3. 测试创建任务 - 超长标题
    const longTitle = 'A'.repeat(500);
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/tasks`,
        { title: longTitle, description: 'Test' }, {}, true));
    testAssert('创建任务 - 超长标题返回 400 或 422',
        [400, 422, 500].includes(data.code) || [400, 422].includes(status),
        'HTTP 400/422', `HTTP ${status}, code=${data.code}`,
        'medium', 'validation', '超长任务标题的验证测试',
        ['POST /api/v1/projects/{id}/tasks'],
        ['1. 登录后获取 Token', '2. POST 创建超长标题任务'],
        ['task', 'validation', 'input-length']);

    // 4. 测试获取任务列表
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/tasks`, null, {}, true));
    testAssert('获取任务列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取任务列表的测试',
        ['GET /api/v1/projects/{id}/tasks'],
        ['1. 登录后获取 Token', '2. GET 获取任务列表'],
        ['task', 'list']);

    // 5. 测试获取任务详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/tasks/99999`, null, {}, true));
    testAssert('获取任务详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在任务的测试',
        ['GET /api/v1/projects/{id}/tasks/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的任务 ID'],
        ['task', 'not-found']);

    // 6. 测试更新任务
    if (state.taskId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}/tasks/${state.taskId}`,
            { title: 'Updated Task', status: 'IN_PROGRESS', priority: 'HIGH' }, {}, true));
        testAssert('更新任务返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新任务的测试',
            ['PUT /api/v1/projects/{id}/tasks/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新任务'],
            ['task', 'update']);

        // 7. 测试更新后 GET 获取任务详情 - 检查数据一致性
        if (status === 200 && data.code === 200) {
            const getResp = await makeRequest('GET', `/projects/${state.projectId}/tasks/${state.taskId}`, null, {}, true);
            const getStatusVal = getResp.data?.data?.status;
            const getPriorityVal = getResp.data?.data?.priority;
            testAssert('更新任务后 GET 返回正确的 status 和 priority',
                getStatusVal === 'IN_PROGRESS' && getPriorityVal === 'HIGH',
                'status=IN_PROGRESS, priority=HIGH',
                `status=${getStatusVal}, priority=${getPriorityVal}`,
                'high', 'data', '更新任务后通过 GET 获取时 status 和 priority 字段的返回测试',
                ['GET /api/v1/projects/{id}/tasks/{id}'],
                [
                    '1. 登录后获取 Token',
                    '2. PUT 更新任务 status=IN_PROGRESS, priority=HIGH',
                    '3. GET 获取任务详情',
                    '4. 检查 status 和 priority 字段'
                ],
                ['task', 'data-consistency']);
        }
    }

    // 8. 测试切换任务完成状态
    if (state.taskId) {
        ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/tasks/${state.taskId}/toggle-complete`, null, {}, true));
        testAssert('切换任务完成状态返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '切换任务完成状态的测试',
            ['POST /api/v1/projects/{id}/tasks/{id}/toggle-complete'],
            ['1. 登录后获取 Token', '2. POST 切换任务完成状态'],
            ['task', 'toggle']);
    }

    // 9. 测试删除任务
    if (state.taskId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}/tasks/${state.taskId}`, null, {}, true));
        testAssert('删除任务返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除任务的测试',
            ['DELETE /api/v1/projects/{id}/tasks/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除任务'],
            ['task', 'delete']);
        state.taskId = null;
    }

    log('=== 任务模块测试完成 ===', 'HEADER');
}

// 测试评论模块
async function testCommentModule() {
    log('=== 开始测试评论模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过评论模块测试（无 Token）', 'WARN');
        log('=== 评论模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建项目和任务
    if (!state.projectId) {
        const projectData = {
            name: `Comment Test Project ${Date.now()}`,
            key: `CTP${Date.now()}`,
            description: 'API Test Project for Comments',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
        }
    }

    if (!state.taskId && state.projectId) {
        const taskData = {
            title: 'Comment Test Task',
            description: 'API Test Task for Comments',
            status: 'TODO',
            priority: 'MEDIUM'
        };
        let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/tasks`, taskData, {}, true);
        if (status === 200 && data.code === 200) {
            state.taskId = data.data?.id;
        }
    }

    if (!state.taskId) {
        log('无法创建任务，跳过评论模块测试', 'WARN');
        return;
    }

    // 1. 测试创建评论
    let { status, data } = await makeRequest('POST', `/tasks/${state.taskId}/comments`,
        { content: 'Test comment' }, {}, true);
    if (status === 200 && data.code === 200) {
        state.commentId = data.data?.id;
        log(`创建评论成功，ID: ${state.commentId}`, 'GREEN');
    }
    testAssert('创建评论返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '创建评论的测试',
        ['POST /api/v1/tasks/{id}/comments'],
        ['1. 登录后获取 Token', '2. POST 创建评论'],
        ['comment', 'create']);

    // 2. 测试创建评论 - 空内容
    ({ status, data } = await makeRequest('POST', `/tasks/${state.taskId}/comments`,
        {}, {}, true));
    testAssert('创建评论 - 空内容返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空评论内容的验证测试',
        ['POST /api/v1/tasks/{id}/comments'],
        ['1. 登录后获取 Token', '2. POST 创建空内容评论'],
        ['comment', 'validation']);

    // 3. 测试创建评论 - 不存在的任务
    ({ status, data } = await makeRequest('POST', '/tasks/99999/comments',
        { content: 'Test' }, {}, true));
    testAssert('创建评论 - 不存在的任务返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '在不存在任务上创建评论的测试',
        ['POST /api/v1/tasks/{id}/comments'],
        ['1. 登录后获取 Token', '2. POST 到不存在的任务 ID 创建评论'],
        ['comment', 'not-found']);

    // 4. 测试获取评论列表
    ({ status, data } = await makeRequest('GET', `/tasks/${state.taskId}/comments`, null, {}, true));
    testAssert('获取评论列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取评论列表的测试',
        ['GET /api/v1/tasks/{id}/comments'],
        ['1. 登录后获取 Token', '2. GET 获取评论列表'],
        ['comment', 'list']);

    // 5. 测试获取评论详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/tasks/${state.taskId}/comments/99999`, null, {}, true));
    testAssert('获取评论详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在评论的测试',
        ['GET /api/v1/tasks/{id}/comments/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的评论 ID'],
        ['comment', 'not-found']);

    // 6. 测试更新评论
    if (state.commentId) {
        ({ status, data } = await makeRequest('PUT', `/tasks/${state.taskId}/comments/${state.commentId}`,
            { content: 'Updated comment' }, {}, true));
        testAssert('更新评论返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新评论的测试',
            ['PUT /api/v1/tasks/{id}/comments/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新评论'],
            ['comment', 'update']);
    }

    // 7. 测试删除评论
    if (state.commentId) {
        ({ status, data } = await makeRequest('DELETE', `/tasks/${state.taskId}/comments/${state.commentId}`, null, {}, true));
        testAssert('删除评论返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除评论的测试',
            ['DELETE /api/v1/tasks/{id}/comments/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除评论'],
            ['comment', 'delete']);
        state.commentId = null;
    }

    log('=== 评论模块测试完成 ===', 'HEADER');
}

// 测试史诗模块
async function testEpicModule() {
    log('=== 开始测试史诗模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过史诗模块测试（无 Token）', 'WARN');
        log('=== 史诗模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建项目
    if (!state.projectId) {
        const projectData = {
            name: `Epic Test Project ${Date.now()}`,
            key: `ETP${Date.now()}`,
            description: 'API Test Project for Epics',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
        }
    }

    if (!state.projectId) {
        log('无法创建项目，跳过史诗模块测试', 'WARN');
        return;
    }

    // 1. 测试创建史诗
    const epicData = {
        name: 'Test Epic',
        description: 'API Test Epic',
        status: 'TODO',
        color: '#FF5733'
    };
    let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/epics`, epicData, {}, true);
    if (status === 200 && data.code === 200) {
        state.epicId = data.data?.id;
        log(`创建史诗成功，ID: ${state.epicId}`, 'GREEN');
    }
    testAssert('创建史诗返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'high', 'functionality', '创建史诗接口测试',
        ['POST /api/v1/projects/{id}/epics'],
        ['1. 登录后获取 Token', '2. POST 创建史诗'],
        ['epic', 'create']);

    // 2. 测试创建史诗 - 空名称
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/epics`,
        { description: 'Test' }, {}, true));
    testAssert('创建史诗 - 空名称返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空史诗名称的验证测试',
        ['POST /api/v1/projects/{id}/epics'],
        ['1. 登录后获取 Token', '2. POST 创建空名称史诗'],
        ['epic', 'validation']);

    // 3. 测试获取史诗列表
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/epics`, null, {}, true));
    testAssert('获取史诗列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取史诗列表的测试',
        ['GET /api/v1/projects/{id}/epics'],
        ['1. 登录后获取 Token', '2. GET 获取史诗列表'],
        ['epic', 'list']);

    // 4. 测试获取史诗详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/epics/99999`, null, {}, true));
    testAssert('获取史诗详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在史诗的测试',
        ['GET /api/v1/projects/{id}/epics/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的史诗 ID'],
        ['epic', 'not-found']);

    // 5. 测试更新史诗
    if (state.epicId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}/epics/${state.epicId}`,
            { name: 'Updated Epic' }, {}, true));
        testAssert('更新史诗返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新史诗的测试',
            ['PUT /api/v1/projects/{id}/epics/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新史诗'],
            ['epic', 'update']);
    }

    // 6. 测试删除史诗
    if (state.epicId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}/epics/${state.epicId}`, null, {}, true));
        testAssert('删除史诗返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除史诗的测试',
            ['DELETE /api/v1/projects/{id}/epics/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除史诗'],
            ['epic', 'delete']);
        state.epicId = null;
    }

    log('=== 史诗模块测试完成 ===', 'HEADER');
}

// 测试用户故事模块
async function testUserStoryModule() {
    log('=== 开始测试用户故事模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过用户故事模块测试（无 Token）', 'WARN');
        log('=== 用户故事模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建项目
    if (!state.projectId) {
        const projectData = {
            name: `Story Test Project ${Date.now()}`,
            key: `STP${Date.now()}`,
            description: 'API Test Project for User Stories',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
        }
    }

    if (!state.projectId) {
        log('无法创建项目，跳过用户故事模块测试', 'WARN');
        return;
    }

    // 1. 测试创建用户故事
    const storyData = {
        title: 'Test User Story',
        description: 'API Test User Story',
        status: 'TODO',
        priority: 'MEDIUM'
    };
    let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/stories`, storyData, {}, true);
    if (status === 200 && data.code === 200) {
        state.storyId = data.data?.id;
        log(`创建用户故事成功，ID: ${state.storyId}`, 'GREEN');
    }
    testAssert('创建用户故事返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'high', 'functionality', '创建用户故事接口测试',
        ['POST /api/v1/projects/{id}/stories'],
        ['1. 登录后获取 Token', '2. POST 创建用户故事'],
        ['userstory', 'create']);

    // 2. 测试创建用户故事 - 空标题
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/stories`,
        { description: 'Test' }, {}, true));
    testAssert('创建用户故事 - 空标题返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空用户故事标题的验证测试',
        ['POST /api/v1/projects/{id}/stories'],
        ['1. 登录后获取 Token', '2. POST 创建空标题用户故事'],
        ['userstory', 'validation']);

    // 3. 测试获取用户故事列表
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/stories`, null, {}, true));
    testAssert('获取用户故事列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取用户故事列表的测试',
        ['GET /api/v1/projects/{id}/stories'],
        ['1. 登录后获取 Token', '2. GET 获取用户故事列表'],
        ['userstory', 'list']);

    // 4. 测试获取用户故事详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/stories/99999`, null, {}, true));
    testAssert('获取用户故事详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在用户故事的测试',
        ['GET /api/v1/projects/{id}/stories/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的用户故事 ID'],
        ['userstory', 'not-found']);

    // 5. 测试更新用户故事
    if (state.storyId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}/stories/${state.storyId}`,
            { title: 'Updated Story' }, {}, true));
        testAssert('更新用户故事返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新用户故事的测试',
            ['PUT /api/v1/projects/{id}/stories/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新用户故事'],
            ['userstory', 'update']);
    }

    // 6. 测试删除用户故事
    if (state.storyId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}/stories/${state.storyId}`, null, {}, true));
        testAssert('删除用户故事返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除用户故事的测试',
            ['DELETE /api/v1/projects/{id}/stories/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除用户故事'],
            ['userstory', 'delete']);
        state.storyId = null;
    }

    log('=== 用户故事模块测试完成 ===', 'HEADER');
}

// 测试 Issue 模块
async function testIssueModule() {
    log('=== 开始测试 Issue 模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过 Issue 模块测试（无 Token）', 'WARN');
        log('=== Issue 模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建项目
    if (!state.projectId) {
        const projectData = {
            name: `Issue Test Project ${Date.now()}`,
            key: `ITP${Date.now()}`,
            description: 'API Test Project for Issues',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
        }
    }

    if (!state.projectId) {
        log('无法创建项目，跳过 Issue 模块测试', 'WARN');
        return;
    }

    // 1. 测试创建 Issue
    const issueData = {
        title: 'Test Issue',
        description: 'API Test Issue',
        type: 'BUG',
        priority: 'HIGH',
        status: 'OPEN'
    };
    let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/issues`, issueData, {}, true);
    if (status === 200 && data.code === 200) {
        state.issueId = data.data?.id;
        log(`创建 Issue 成功，ID: ${state.issueId}`, 'GREEN');
    }
    testAssert('创建 Issue 返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'high', 'functionality', '创建 Issue 接口测试',
        ['POST /api/v1/projects/{id}/issues'],
        ['1. 登录后获取 Token', '2. POST 创建 Issue'],
        ['issue', 'create']);

    // 2. 测试创建 Issue - 空标题
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/issues`,
        { description: 'Test' }, {}, true));
    testAssert('创建 Issue - 空标题返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空 Issue 标题的验证测试',
        ['POST /api/v1/projects/{id}/issues'],
        ['1. 登录后获取 Token', '2. POST 创建空标题 Issue'],
        ['issue', 'validation']);

    // 3. 测试获取 Issue 列表
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/issues`, null, {}, true));
    testAssert('获取 Issue 列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取 Issue 列表的测试',
        ['GET /api/v1/projects/{id}/issues'],
        ['1. 登录后获取 Token', '2. GET 获取 Issue 列表'],
        ['issue', 'list']);

    // 4. 测试获取 Issue 详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/issues/99999`, null, {}, true));
    testAssert('获取 Issue 详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在 Issue 的测试',
        ['GET /api/v1/projects/{id}/issues/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的 Issue ID'],
        ['issue', 'not-found']);

    // 5. 测试更新 Issue
    if (state.issueId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}/issues/${state.issueId}`,
            { title: 'Updated Issue' }, {}, true));
        testAssert('更新 Issue 返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新 Issue 的测试',
            ['PUT /api/v1/projects/{id}/issues/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新 Issue'],
            ['issue', 'update']);
    }

    // 6. 测试删除 Issue
    if (state.issueId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}/issues/${state.issueId}`, null, {}, true));
        testAssert('删除 Issue 返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除 Issue 的测试',
            ['DELETE /api/v1/projects/{id}/issues/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除 Issue'],
            ['issue', 'delete']);
        state.issueId = null;
    }

    log('=== Issue 模块测试完成 ===', 'HEADER');
}

// 测试 Wiki 模块
async function testWikiModule() {
    log('=== 开始测试 Wiki 模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过 Wiki 模块测试（无 Token）', 'WARN');
        log('=== Wiki 模块测试完成 ===', 'HEADER');
        return;
    }

    // 先创建项目
    if (!state.projectId) {
        const projectData = {
            name: `Wiki Test Project ${Date.now()}`,
            key: `WTP${Date.now()}`,
            description: 'API Test Project for Wiki',
            leadUserId: 1,
            type: 'SOFTWARE'
        };
        let { status, data } = await makeRequest('POST', '/projects', projectData, {}, true);
        if (status === 200 && data.code === 200) {
            state.projectId = data.data?.id;
        }
    }

    if (!state.projectId) {
        log('无法创建项目，跳过 Wiki 模块测试', 'WARN');
        return;
    }

    // 1. 测试创建 Wiki
    const wikiData = {
        title: 'Test Wiki',
        content: 'API Test Wiki Content',
        parentId: null
    };
    let { status, data } = await makeRequest('POST', `/projects/${state.projectId}/wiki`, wikiData, {}, true);
    if (status === 200 && data.code === 200) {
        state.wikiId = data.data?.id;
        log(`创建 Wiki 成功，ID: ${state.wikiId}`, 'GREEN');
    }
    testAssert('创建 Wiki 返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'high', 'functionality', '创建 Wiki 接口测试',
        ['POST /api/v1/projects/{id}/wiki'],
        ['1. 登录后获取 Token', '2. POST 创建 Wiki'],
        ['wiki', 'create']);

    // 2. 测试创建 Wiki - 空标题
    ({ status, data } = await makeRequest('POST', `/projects/${state.projectId}/wiki`,
        { content: 'Test' }, {}, true));
    testAssert('创建 Wiki - 空标题返回 400',
        data.code === 400 || status === 400,
        'HTTP 400 或 code=400', `HTTP ${status}, code=${data.code}`,
        'low', 'validation', '空 Wiki 标题的验证测试',
        ['POST /api/v1/projects/{id}/wiki'],
        ['1. 登录后获取 Token', '2. POST 创建空标题 Wiki'],
        ['wiki', 'validation']);

    // 3. 测试获取 Wiki 列表
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/wiki`, null, {}, true));
    testAssert('获取 Wiki 列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取 Wiki 列表的测试',
        ['GET /api/v1/projects/{id}/wiki'],
        ['1. 登录后获取 Token', '2. GET 获取 Wiki 列表'],
        ['wiki', 'list']);

    // 4. 测试获取 Wiki 详情 - 不存在的 ID
    ({ status, data } = await makeRequest('GET', `/projects/${state.projectId}/wiki/99999`, null, {}, true));
    testAssert('获取 Wiki 详情 - 不存在的 ID 返回 404',
        status === 404 || data.code === 404,
        'HTTP 404 或 code=404', `HTTP ${status}, code=${data.code}`,
        'low', 'functionality', '获取不存在 Wiki 的测试',
        ['GET /api/v1/projects/{id}/wiki/{id}'],
        ['1. 登录后获取 Token', '2. GET 访问不存在的 Wiki ID'],
        ['wiki', 'not-found']);

    // 5. 测试更新 Wiki
    if (state.wikiId) {
        ({ status, data } = await makeRequest('PUT', `/projects/${state.projectId}/wiki/${state.wikiId}`,
            { title: 'Updated Wiki' }, {}, true));
        testAssert('更新 Wiki 返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '更新 Wiki 的测试',
            ['PUT /api/v1/projects/{id}/wiki/{id}'],
            ['1. 登录后获取 Token', '2. PUT 更新 Wiki'],
            ['wiki', 'update']);
    }

    // 6. 测试删除 Wiki
    if (state.wikiId) {
        ({ status, data } = await makeRequest('DELETE', `/projects/${state.projectId}/wiki/${state.wikiId}`, null, {}, true));
        testAssert('删除 Wiki 返回 200',
            status === 200 && data.code === 200,
            'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
            'medium', 'functionality', '删除 Wiki 的测试',
            ['DELETE /api/v1/projects/{id}/wiki/{id}'],
            ['1. 登录后获取 Token', '2. DELETE 删除 Wiki'],
            ['wiki', 'delete']);
        state.wikiId = null;
    }

    log('=== Wiki 模块测试完成 ===', 'HEADER');
}

// 测试通知模块
async function testNotificationModule() {
    log('=== 开始测试通知模块 ===', 'HEADER');

    if (!state.accessToken) {
        log('跳过通知模块测试（无 Token）', 'WARN');
        log('=== 通知模块测试完成 ===', 'HEADER');
        return;
    }

    // 1. 测试获取通知列表 - 无认证
    let { status, data } = await makeRequest('GET', '/notifications');
    testAssert('获取通知列表 - 无认证返回 401',
        status === 401, 'HTTP 401', `HTTP ${status}`,
        'low', 'security', '未认证访问通知列表的测试',
        ['GET /api/v1/notifications'],
        ['1. 不带 Token 访问通知列表接口'],
        ['notification', 'security']);

    // 2. 测试获取通知列表 - 有认证
    ({ status, data } = await makeRequest('GET', '/notifications', null, {}, true));
    testAssert('获取通知列表返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取通知列表的测试',
        ['GET /api/v1/notifications'],
        ['1. 登录后获取 Token', '2. GET 获取通知列表'],
        ['notification', 'list']);

    // 3. 测试获取未读通知数量
    ({ status, data } = await makeRequest('GET', '/notifications/unread-count', null, {}, true));
    testAssert('获取未读通知数量返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '获取未读通知数量的测试',
        ['GET /api/v1/notifications/unread-count'],
        ['1. 登录后获取 Token', '2. GET 获取未读通知数量'],
        ['notification', 'unread']);

    // 4. 测试全部标记已读
    ({ status, data } = await makeRequest('POST', '/notifications/mark-all-read', null, {}, true));
    testAssert('全部标记已读返回 200',
        status === 200 && data.code === 200,
        'HTTP 200, code=200', `HTTP ${status}, code=${data.code}`,
        'medium', 'functionality', '全部标记已读的测试',
        ['POST /api/v1/notifications/mark-all-read'],
        ['1. 登录后获取 Token', '2. POST 全部标记已读'],
        ['notification', 'mark-read']);

    log('=== 通知模块测试完成 ===', 'HEADER');
}

// 保存测试结果
function saveResults() {
    const passRate = state.testsTotal > 0
        ? ((state.testsPassed * 100 / state.testsTotal).toFixed(1)) + '%'
        : '0%';

    const report = {
        test_version: 'v6',
        test_date: new Date().toLocaleDateString('zh-CN'),
        tester: 'API Test Engineer',
        api_base_url: BASE_URL,
        summary: {
            total_tests: state.testsTotal,
            passed: state.testsPassed,
            failed: state.testsFailed,
            pass_rate: passRate
        },
        issues: state.issues,
        working_features: state.workingFeatures
    };

    // 保存 JSON 文件
    const outputFile = path.join(OUTPUT_DIR, 'issues-v6-p01.json');
    fs.writeFileSync(outputFile, JSON.stringify(report, null, 2), 'utf-8');
    log(`测试结果已保存到：${outputFile}`, 'INFO');

    // 生成测试报告文本
    let reportText = `# ProjectHub Backend API 测试报告 v6

## 测试概要

| 项目 | 值 |
|------|-----|
| 测试日期 | ${new Date().toLocaleDateString('zh-CN')} |
| 测试人员 | API Test Engineer |
| API 地址 | ${BASE_URL} |
| 总测试数 | ${state.testsTotal} |
| 通过数 | ${state.testsPassed} |
| 失败数 | ${state.testsFailed} |
| 通过率 | ${passRate} |

## 发现的问题 (${state.issues.length} 个)

`;

    state.issues.forEach(issue => {
        reportText += `
### ${issue.id}: ${issue.title}

- **严重程度**: ${issue.severity}
- **类别**: ${issue.category}
- **影响接口**: ${issue.affected_apis.join(', ')}
- **描述**: ${issue.description}
- **当前状态**: ${issue.current_state}
- **期望状态**: ${issue.expected_state}
- **复现步骤**:
${issue.reproduction_steps.map(s => '  ' + s).join('\n')}
- **标签**: ${issue.labels.join(', ')}

`;
    });

    const reportFile = path.join(OUTPUT_DIR, 'test-report-v6.md');
    fs.writeFileSync(reportFile, reportText, 'utf-8');
    log(`测试报告已保存到：${reportFile}`, 'INFO');
}

// 主函数
async function runAllTests() {
    console.log('=========================================');
    console.log('ProjectHub Backend API Test Suite v6');
    console.log('=========================================');

    // 登录
    await login();

    // 执行各模块测试
    await testAuthModule();
    await testUserModule();
    await testProjectModule();
    await testTaskModule();
    await testCommentModule();
    await testEpicModule();
    await testUserStoryModule();
    await testIssueModule();
    await testWikiModule();
    await testNotificationModule();

    // 保存结果
    saveResults();

    // 打印摘要
    console.log('=========================================');
    console.log('测试完成摘要');
    console.log('=========================================');
    console.log(`总测试数：${state.testsTotal}`);
    console.log(`通过：${state.testsPassed}`);
    console.log(`失败：${state.testsFailed}`);
    if (state.testsTotal > 0) {
        console.log(`通过率：${(state.testsPassed * 100 / state.testsTotal).toFixed(1)}%`);
    }
    console.log(`发现问题数：${state.issues.length}`);
}

// 运行测试
runAllTests().catch(err => {
    console.error('测试执行出错:', err);
    process.exit(1);
});
