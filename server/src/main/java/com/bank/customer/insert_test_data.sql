USE BankCustomerDB;
GO

-- 插入角色
INSERT INTO role (role_id, role_name) VALUES
(1, '普通用户'),
(2, '管理员');
GO

-- 插入管理员用户
INSERT INTO [user] (username, password_hash, full_name, email, phone)
VALUES ('admin', 'hashed_password', '系统管理员', 'admin@bank.com', '13800138000');
GO

-- 关联管理员角色
INSERT INTO user_role (user_id, role_id)
SELECT user_id, 2 FROM [user] WHERE username = 'admin';
GO