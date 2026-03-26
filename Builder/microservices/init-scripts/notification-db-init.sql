-- Notification Service 数据库初始化脚本
-- 数据库：notification_db

-- 创建通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    channel VARCHAR(20) NOT NULL COMMENT '通知渠道：IN_APP/EMAIL',
    recipient VARCHAR(100) COMMENT '接收者邮箱',
    status VARCHAR(20) DEFAULT 'UNREAD' COMMENT '状态：UNREAD/READ/SENT/FAILED/PENDING',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    sent_at TIMESTAMP COMMENT '发送时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 创建索引
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- 插入默认数据（测试用）
INSERT INTO notifications (user_id, type, title, content, channel, status)
VALUES (1, 'SYSTEM', '欢迎使用 ProjectHub', '您已成功注册 ProjectHub 项目管理平台', 'IN_APP', 'READ')
ON DUPLICATE KEY UPDATE id=id;

COMMENT ON TABLE notifications IS '通知表';
COMMENT ON COLUMN notifications.user_id IS '用户 ID';
COMMENT ON COLUMN notifications.type IS '通知类型';
COMMENT ON COLUMN notifications.title IS '通知标题';
COMMENT ON COLUMN notifications.content IS '通知内容';
COMMENT ON COLUMN notifications.channel IS '通知渠道：IN_APP/EMAIL';
COMMENT ON COLUMN notifications.status IS '状态：UNREAD/READ/SENT/FAILED/PENDING';
