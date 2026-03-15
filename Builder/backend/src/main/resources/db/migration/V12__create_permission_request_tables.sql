-- ProjectHub 数据库 Schema - V12
-- 创建权限申请和审批表
-- 数据库：PostgreSQL 15+
-- Flyway 版本：V12

-- ============================================
-- 权限申请模块
-- ============================================

-- 权限申请表
CREATE TABLE sys_permission_request (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请人 ID',
    permission_id BIGINT NOT NULL COMMENT '申请的权限 ID',
    project_id BIGINT COMMENT '关联项目 ID（可选，项目级权限申请）',
    reason VARCHAR(500) NOT NULL COMMENT '申请理由',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_permission_request_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_permission_request_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id),
    CONSTRAINT fk_permission_request_project FOREIGN KEY (project_id) REFERENCES project(id)
);

-- 权限申请表注释
COMMENT ON TABLE sys_permission_request IS '权限申请表';
COMMENT ON COLUMN sys_permission_request.id IS '主键 ID';
COMMENT ON COLUMN sys_permission_request.user_id IS '申请人 ID';
COMMENT ON COLUMN sys_permission_request.permission_id IS '申请的权限 ID';
COMMENT ON COLUMN sys_permission_request.project_id IS '关联项目 ID（可选，项目级权限申请）';
COMMENT ON COLUMN sys_permission_request.reason IS '申请理由';
COMMENT ON COLUMN sys_permission_request.status IS '状态：PENDING/APPROVED/REJECTED';
COMMENT ON COLUMN sys_permission_request.created_at IS '创建时间';
COMMENT ON COLUMN sys_permission_request.updated_at IS '更新时间';

-- 权限申请表索引
CREATE INDEX idx_permission_request_user ON sys_permission_request(user_id);
CREATE INDEX idx_permission_request_permission ON sys_permission_request(permission_id);
CREATE INDEX idx_permission_request_project ON sys_permission_request(project_id);
CREATE INDEX idx_permission_request_status ON sys_permission_request(status);
CREATE INDEX idx_permission_request_created_at ON sys_permission_request(created_at);

-- 权限审批记录表
CREATE TABLE sys_permission_approval (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT '申请 ID',
    approver_id BIGINT NOT NULL COMMENT '审批人 ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：APPROVE/REJECT',
    comment VARCHAR(500) COMMENT '审批意见',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_approval_request FOREIGN KEY (request_id) REFERENCES sys_permission_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_approver FOREIGN KEY (approver_id) REFERENCES sys_user(id)
);

-- 权限审批记录表注释
COMMENT ON TABLE sys_permission_approval IS '权限审批记录表';
COMMENT ON COLUMN sys_permission_approval.id IS '主键 ID';
COMMENT ON COLUMN sys_permission_approval.request_id IS '申请 ID';
COMMENT ON COLUMN sys_permission_approval.approver_id IS '审批人 ID';
COMMENT ON COLUMN sys_permission_approval.action IS '操作：APPROVE/REJECT';
COMMENT ON COLUMN sys_permission_approval.comment IS '审批意见';
COMMENT ON COLUMN sys_permission_approval.created_at IS '创建时间';

-- 权限审批记录表索引
CREATE INDEX idx_approval_request ON sys_permission_approval(request_id);
CREATE INDEX idx_approval_approver ON sys_permission_approval(approver_id);

-- 更新时自动更新 updated_at 字段的触发器
CREATE TRIGGER update_sys_permission_request_updated_at
    BEFORE UPDATE ON sys_permission_request
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
