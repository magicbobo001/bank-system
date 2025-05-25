USE BankCustomerDB;
GO

-- 删除旧存储过程（如果存在）
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_transfer')
    DROP PROCEDURE sp_transfer;
GO

-- 创建新存储过程（适配新表名）
CREATE PROCEDURE sp_transfer
    @p_from_account_id NVARCHAR(20),
    @p_to_account_id NVARCHAR(20),
    @p_amount DECIMAL(15,2)
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        -- 扣除转出账户余额
        UPDATE account SET balance = balance - @p_amount 
        WHERE account_id = @p_from_account_id;
        -- 增加转入账户余额
        UPDATE account SET balance = balance + @p_amount 
        WHERE account_id = @p_to_account_id;
        -- 记录交易（使用新表名 account_transaction）
        INSERT INTO account_transaction (
            from_account_id, to_account_id, amount, transaction_type, transaction_time
        ) VALUES (
            @p_from_account_id, @p_to_account_id, @p_amount, 'transfer', GETDATE()
        );
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO