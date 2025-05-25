USE BankCustomerDB;
GO

-- 1. 角色表
CREATE TABLE role (
    role_id INT PRIMARY KEY,
    role_name NVARCHAR(20) NOT NULL UNIQUE
);
GO

-- 2. 用户表
CREATE TABLE [user] (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    address NVARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    last_login DATETIME,
    status NVARCHAR(10) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive'))
);
GO

-- 3. 用户角色关联表
CREATE TABLE user_role (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES [user](user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);
GO

-- 4. 账户表
CREATE TABLE account (
    account_id NVARCHAR(20) PRIMARY KEY,
    user_id INT NOT NULL,
    account_type NVARCHAR(10) NOT NULL CHECK (account_type IN ('savings', 'checking')),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status NVARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES [user](user_id)
);
GO

-- 5. 交易记录表
CREATE TABLE [transaction] (
    transaction_id INT PRIMARY KEY IDENTITY(1,1),
    from_account_id NVARCHAR(20),
    to_account_id NVARCHAR(20),
    amount DECIMAL(15,2) NOT NULL,
    transaction_type NVARCHAR(10) NOT NULL CHECK (transaction_type IN ('deposit', 'withdraw', 'transfer')),
    transaction_time DATETIME NOT NULL DEFAULT GETDATE(),
    description NVARCHAR(255),
    FOREIGN KEY (from_account_id) REFERENCES account(account_id),
    FOREIGN KEY (to_account_id) REFERENCES account(account_id)
);
GO

-- 6. 贷款申请表
CREATE TABLE loan_application (
    loan_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    account_id NVARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    term INT NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'disbursed')),
    application_date DATETIME NOT NULL DEFAULT GETDATE(),
    approval_date DATETIME,
    approved_by INT,
    FOREIGN KEY (user_id) REFERENCES [user](user_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (approved_by) REFERENCES [user](user_id)
);
GO

-- 7. 数据导入表
CREATE TABLE data_import (
    import_id INT PRIMARY KEY IDENTITY(1,1),
    admin_id INT NOT NULL,
    filename NVARCHAR(255) NOT NULL,
    import_time DATETIME NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(20) NOT NULL CHECK (status IN ('success', 'partial_failure', 'failed')),
    FOREIGN KEY (admin_id) REFERENCES [user](user_id)
);
GO

-- 8. 数据导入记录表
CREATE TABLE import_record (
    record_id INT PRIMARY KEY IDENTITY(1,1),
    import_id INT NOT NULL,
    data_content NVARCHAR(MAX) NOT NULL,
    status NVARCHAR(10) NOT NULL CHECK (status IN ('success', 'failed')),
    error_message NVARCHAR(255),
    FOREIGN KEY (import_id) REFERENCES data_import(import_id)
);
GO