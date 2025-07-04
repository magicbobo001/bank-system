USE BankCustomerDB;
GO

-- 开户存储过程
CREATE PROCEDURE sp_create_account
    @p_user_id INT,
    @p_account_type NVARCHAR(10),
    @p_account_id NVARCHAR(20) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        -- 生成账户号（示例逻辑）
        SET @p_account_id = '622588' + RIGHT('00000000' + CAST(ABS(CHECKSUM(NEWID())) AS NVARCHAR(8)), 8);
        -- 插入账户表
        INSERT INTO account (account_id, user_id, account_type)
        VALUES (@p_account_id, @p_user_id, @p_account_type);
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO

-- 用户账户视图
CREATE VIEW v_user_account_summary
AS
SELECT 
    u.user_id,
    u.full_name,
    a.account_id,
    a.account_type,
    a.balance,
    a.status
FROM [user] u
JOIN account a ON u.user_id = a.user_id;
GO

-- 添加交易视图
CREATE VIEW v_account_transaction_history AS
SELECT t.*, a1.user_id as from_user, a2.user_id as to_user
FROM account_transaction t
LEFT JOIN account a1 ON t.from_account_id = a1.account_id
LEFT JOIN account a2 ON t.to_account_id = a2.account_id;