USE BankCustomerDB;
GO

-- 创建新存储过程（修复版）
CREATE PROCEDURE [dbo].[sp_create_account]
    @p_user_id INT,
    @p_account_type NVARCHAR(10),
    @p_account_id NVARCHAR(20) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        -- 生成 8 位随机数（确保范围在 0-99999999 之间）
        DECLARE @random_number INT = ABS(CHECKSUM(NEWID())) % 100000000;
        -- 格式化为 8 位字符串（不足补零）
        DECLARE @random_part NVARCHAR(8) = FORMAT(@random_number, '00000000');
        -- 拼接账户号
        SET @p_account_id = '622588' + @random_part;

        -- 插入账户表
        INSERT INTO account (account_id, user_id, account_type, created_at,[status],has_overdue)
        VALUES (@p_account_id, @p_user_id, @p_account_type, GETDATE(),'ACTIVE',0);
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO
