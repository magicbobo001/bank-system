USE [master]
GO
/****** Object:  Database [BankCustomerDB]    Script Date: 2025/5/25 18:42:47 ******/
CREATE DATABASE [BankCustomerDB]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'BankCustomerDB', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.SQLEXPRESS\MSSQL\DATA\BankCustomerDB.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'BankCustomerDB_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.SQLEXPRESS\MSSQL\DATA\BankCustomerDB_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [BankCustomerDB].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [BankCustomerDB] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [BankCustomerDB] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [BankCustomerDB] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [BankCustomerDB] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [BankCustomerDB] SET ARITHABORT OFF 
GO
ALTER DATABASE [BankCustomerDB] SET AUTO_CLOSE ON 
GO
ALTER DATABASE [BankCustomerDB] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [BankCustomerDB] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [BankCustomerDB] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [BankCustomerDB] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [BankCustomerDB] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [BankCustomerDB] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [BankCustomerDB] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [BankCustomerDB] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [BankCustomerDB] SET  ENABLE_BROKER 
GO
ALTER DATABASE [BankCustomerDB] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [BankCustomerDB] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [BankCustomerDB] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [BankCustomerDB] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [BankCustomerDB] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [BankCustomerDB] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [BankCustomerDB] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [BankCustomerDB] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [BankCustomerDB] SET  MULTI_USER 
GO
ALTER DATABASE [BankCustomerDB] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [BankCustomerDB] SET DB_CHAINING OFF 
GO
ALTER DATABASE [BankCustomerDB] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [BankCustomerDB] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [BankCustomerDB] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [BankCustomerDB] SET QUERY_STORE = OFF
GO
USE [BankCustomerDB]
GO
ALTER DATABASE SCOPED CONFIGURATION SET ACCELERATED_PLAN_FORCING = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET BATCH_MODE_ADAPTIVE_JOINS = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET BATCH_MODE_MEMORY_GRANT_FEEDBACK = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET BATCH_MODE_ON_ROWSTORE = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET DEFERRED_COMPILATION_TV = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET ELEVATE_ONLINE = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET ELEVATE_RESUMABLE = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET GLOBAL_TEMPORARY_TABLE_AUTO_DROP = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET IDENTITY_CACHE = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET INTERLEAVED_EXECUTION_TVF = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET ISOLATE_SECURITY_POLICY_CARDINALITY = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET LAST_QUERY_PLAN_STATS = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET LEGACY_CARDINALITY_ESTIMATION = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION FOR SECONDARY SET LEGACY_CARDINALITY_ESTIMATION = PRIMARY;
GO
ALTER DATABASE SCOPED CONFIGURATION SET LIGHTWEIGHT_QUERY_PROFILING = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET MAXDOP = 0;
GO
ALTER DATABASE SCOPED CONFIGURATION FOR SECONDARY SET MAXDOP = PRIMARY;
GO
ALTER DATABASE SCOPED CONFIGURATION SET OPTIMIZE_FOR_AD_HOC_WORKLOADS = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET PARAMETER_SNIFFING = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION FOR SECONDARY SET PARAMETER_SNIFFING = PRIMARY;
GO
ALTER DATABASE SCOPED CONFIGURATION SET QUERY_OPTIMIZER_HOTFIXES = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION FOR SECONDARY SET QUERY_OPTIMIZER_HOTFIXES = PRIMARY;
GO
ALTER DATABASE SCOPED CONFIGURATION SET ROW_MODE_MEMORY_GRANT_FEEDBACK = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET TSQL_SCALAR_UDF_INLINING = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET VERBOSE_TRUNCATION_WARNINGS = ON;
GO
ALTER DATABASE SCOPED CONFIGURATION SET XTP_PROCEDURE_EXECUTION_STATISTICS = OFF;
GO
ALTER DATABASE SCOPED CONFIGURATION SET XTP_QUERY_EXECUTION_STATISTICS = OFF;
GO
USE [BankCustomerDB]
GO
/****** Object:  Table [dbo].[user]    Script Date: 2025/5/25 18:42:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[username] [nvarchar](50) NOT NULL,
	[password_hash] [nvarchar](255) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[email] [nvarchar](100) NOT NULL,
	[phone] [nvarchar](20) NOT NULL,
	[address] [nvarchar](255) NULL,
	[created_at] [datetime] NOT NULL,
	[last_login] [datetime] NULL,
	[status] [nvarchar](10) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[account]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[account](
	[account_id] [nvarchar](20) NOT NULL,
	[user_id] [int] NOT NULL,
	[account_type] [nvarchar](10) NOT NULL,
	[balance] [decimal](15, 2) NOT NULL,
	[status] [nvarchar](10) NOT NULL,
	[created_at] [datetime] NOT NULL,
	[closed_at] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[v_user_account_summary]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- 用户账户视图
CREATE VIEW [dbo].[v_user_account_summary]
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
/****** Object:  Table [dbo].[account_transaction]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[account_transaction](
	[transaction_id] [int] IDENTITY(1,1) NOT NULL,
	[from_account_id] [nvarchar](20) NULL,
	[to_account_id] [nvarchar](20) NULL,
	[amount] [decimal](15, 2) NOT NULL,
	[transaction_type] [nvarchar](10) NOT NULL,
	[transaction_time] [datetime] NOT NULL,
	[description] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[transaction_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[audit_log]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[audit_log](
	[log_id] [bigint] IDENTITY(1,1) NOT NULL,
	[operation_type] [varchar](20) NOT NULL,
	[account_id] [varchar](20) NOT NULL,
	[operator_id] [int] NOT NULL,
	[operation_time] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[log_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[data_import]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[data_import](
	[import_id] [int] IDENTITY(1,1) NOT NULL,
	[admin_id] [int] NOT NULL,
	[filename] [nvarchar](255) NOT NULL,
	[import_time] [datetime] NOT NULL,
	[status] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[import_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[import_record]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[import_record](
	[record_id] [int] IDENTITY(1,1) NOT NULL,
	[import_id] [int] NOT NULL,
	[data_content] [nvarchar](max) NOT NULL,
	[status] [nvarchar](10) NOT NULL,
	[error_message] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[record_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[loan_application]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[loan_application](
	[user_id] [int] NOT NULL,
	[account_id] [nvarchar](20) NOT NULL,
	[amount] [decimal](15, 2) NOT NULL,
	[term] [int] NOT NULL,
	[interest_rate] [decimal](5, 2) NOT NULL,
	[status] [nvarchar](20) NOT NULL,
	[application_date] [datetime] NOT NULL,
	[approval_date] [datetime] NULL,
	[approved_by] [int] NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NOT NULL,
	[monthly_payment] [decimal](15, 2) NULL,
	[remaining_principal] [decimal](15, 2) NULL,
	[loan_id] [bigint] IDENTITY(1,1) NOT NULL,
 CONSTRAINT [PK_loan_application] PRIMARY KEY CLUSTERED 
(
	[loan_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[loan_repayment]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[loan_repayment](
	[loan_id] [bigint] NOT NULL,
	[repayment_date] [date] NOT NULL,
	[amount] [decimal](15, 2) NOT NULL,
	[principal] [decimal](15, 2) NOT NULL,
	[interest] [decimal](15, 2) NOT NULL,
	[status] [nvarchar](20) NOT NULL,
	[repayment_id] [bigint] IDENTITY(1,1) NOT NULL,
 CONSTRAINT [PK_loan_repayment] PRIMARY KEY CLUSTERED 
(
	[repayment_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[role]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[role](
	[role_id] [int] NOT NULL,
	[role_name] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[user_role]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user_role](
	[user_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
INSERT [dbo].[account] ([account_id], [user_id], [account_type], [balance], [status], [created_at], [closed_at]) VALUES (N'62258827962235', 1, N'savings', CAST(300.00 AS Decimal(15, 2)), N'ACTIVE', CAST(N'2025-05-24T15:51:53.237' AS DateTime), NULL)
INSERT [dbo].[account] ([account_id], [user_id], [account_type], [balance], [status], [created_at], [closed_at]) VALUES (N'62258868079609', 1, N'savings', CAST(0.00 AS Decimal(15, 2)), N'ACTIVE', CAST(N'2025-05-24T15:01:47.057' AS DateTime), NULL)
SET IDENTITY_INSERT [dbo].[account_transaction] ON 

INSERT [dbo].[account_transaction] ([transaction_id], [from_account_id], [to_account_id], [amount], [transaction_type], [transaction_time], [description]) VALUES (1, NULL, N'62258827962235', CAST(500.00 AS Decimal(15, 2)), N'deposit', CAST(N'2025-05-24T17:07:51.693' AS DateTime), NULL)
INSERT [dbo].[account_transaction] ([transaction_id], [from_account_id], [to_account_id], [amount], [transaction_type], [transaction_time], [description]) VALUES (2, N'62258827962235', N'62258868079609', CAST(200.00 AS Decimal(15, 2)), N'transfer', CAST(N'2025-05-24T17:21:34.193' AS DateTime), NULL)
INSERT [dbo].[account_transaction] ([transaction_id], [from_account_id], [to_account_id], [amount], [transaction_type], [transaction_time], [description]) VALUES (3, N'62258868079609', NULL, CAST(100.00 AS Decimal(15, 2)), N'withdraw', CAST(N'2025-05-24T17:27:29.863' AS DateTime), NULL)
INSERT [dbo].[account_transaction] ([transaction_id], [from_account_id], [to_account_id], [amount], [transaction_type], [transaction_time], [description]) VALUES (4, N'62258868079609', NULL, CAST(100.00 AS Decimal(15, 2)), N'withdraw', CAST(N'2025-05-24T17:38:48.733' AS DateTime), NULL)
INSERT [dbo].[account_transaction] ([transaction_id], [from_account_id], [to_account_id], [amount], [transaction_type], [transaction_time], [description]) VALUES (5, NULL, N'62258868079609', CAST(100.00 AS Decimal(15, 2)), N'deposit', CAST(N'2025-05-24T17:40:59.990' AS DateTime), NULL)
SET IDENTITY_INSERT [dbo].[account_transaction] OFF
SET IDENTITY_INSERT [dbo].[audit_log] ON 

INSERT [dbo].[audit_log] ([log_id], [operation_type], [account_id], [operator_id], [operation_time]) VALUES (1, N'FREEZE', N'62258868079609', 1, CAST(N'2025-05-24T17:38:39.663' AS DateTime))
INSERT [dbo].[audit_log] ([log_id], [operation_type], [account_id], [operator_id], [operation_time]) VALUES (2, N'UNFREEZE', N'62258868079609', 1, CAST(N'2025-05-24T17:40:26.730' AS DateTime))
INSERT [dbo].[audit_log] ([log_id], [operation_type], [account_id], [operator_id], [operation_time]) VALUES (3, N'CLOSE', N'62258868079609', 1, CAST(N'2025-05-24T17:43:22.893' AS DateTime))
INSERT [dbo].[audit_log] ([log_id], [operation_type], [account_id], [operator_id], [operation_time]) VALUES (5, N'RESTORE', N'62258868079609', 1, CAST(N'2025-05-24T17:52:25.503' AS DateTime))
SET IDENTITY_INSERT [dbo].[audit_log] OFF
SET IDENTITY_INSERT [dbo].[loan_application] ON 

INSERT [dbo].[loan_application] ([user_id], [account_id], [amount], [term], [interest_rate], [status], [application_date], [approval_date], [approved_by], [start_date], [end_date], [monthly_payment], [remaining_principal], [loan_id]) VALUES (1, N'62258827962235', CAST(10000.00 AS Decimal(15, 2)), 12, CAST(0.05 AS Decimal(5, 2)), N'APPROVED', CAST(N'2025-05-25T16:26:40.187' AS DateTime), NULL, NULL, CAST(N'2025-06-01' AS Date), CAST(N'2026-06-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(9185.60 AS Decimal(15, 2)), 1)
SET IDENTITY_INSERT [dbo].[loan_application] OFF
SET IDENTITY_INSERT [dbo].[loan_repayment] ON 

INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-07-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(814.40 AS Decimal(15, 2)), CAST(41.67 AS Decimal(15, 2)), N'PAID', 37)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-08-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(817.80 AS Decimal(15, 2)), CAST(38.27 AS Decimal(15, 2)), N'PENDING', 38)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-09-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(821.20 AS Decimal(15, 2)), CAST(34.87 AS Decimal(15, 2)), N'PENDING', 39)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-10-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(824.63 AS Decimal(15, 2)), CAST(31.44 AS Decimal(15, 2)), N'PENDING', 40)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-11-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(828.06 AS Decimal(15, 2)), CAST(28.01 AS Decimal(15, 2)), N'PENDING', 41)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2025-12-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(831.51 AS Decimal(15, 2)), CAST(24.56 AS Decimal(15, 2)), N'PENDING', 42)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-01-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(834.98 AS Decimal(15, 2)), CAST(21.09 AS Decimal(15, 2)), N'PENDING', 43)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-02-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(838.46 AS Decimal(15, 2)), CAST(17.61 AS Decimal(15, 2)), N'PENDING', 44)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-03-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(841.95 AS Decimal(15, 2)), CAST(14.12 AS Decimal(15, 2)), N'PENDING', 45)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-04-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(845.46 AS Decimal(15, 2)), CAST(10.61 AS Decimal(15, 2)), N'PENDING', 46)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-05-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(848.98 AS Decimal(15, 2)), CAST(7.09 AS Decimal(15, 2)), N'PENDING', 47)
INSERT [dbo].[loan_repayment] ([loan_id], [repayment_date], [amount], [principal], [interest], [status], [repayment_id]) VALUES (1, CAST(N'2026-06-01' AS Date), CAST(856.07 AS Decimal(15, 2)), CAST(852.52 AS Decimal(15, 2)), CAST(3.55 AS Decimal(15, 2)), N'PENDING', 48)
SET IDENTITY_INSERT [dbo].[loan_repayment] OFF
INSERT [dbo].[role] ([role_id], [role_name]) VALUES (2, N'管理员')
INSERT [dbo].[role] ([role_id], [role_name]) VALUES (1, N'普通用户')
SET IDENTITY_INSERT [dbo].[user] ON 

INSERT [dbo].[user] ([user_id], [username], [password_hash], [full_name], [email], [phone], [address], [created_at], [last_login], [status]) VALUES (1, N'admin', N'hashed_password', N'系统管理员', N'admin@bank.com', N'13800138000', NULL, CAST(N'2025-05-24T14:09:23.013' AS DateTime), NULL, N'active')
INSERT [dbo].[user] ([user_id], [username], [password_hash], [full_name], [email], [phone], [address], [created_at], [last_login], [status]) VALUES (2, N'alice', N'hashed_password', N'Alice Smith', N'alice@example.com', N'13800138000', NULL, CAST(N'2025-05-24T14:32:24.233' AS DateTime), NULL, N'ACTIVE')
SET IDENTITY_INSERT [dbo].[user] OFF
INSERT [dbo].[user_role] ([user_id], [role_id]) VALUES (1, 2)
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__role__783254B1D39B8260]    Script Date: 2025/5/25 18:42:48 ******/
ALTER TABLE [dbo].[role] ADD UNIQUE NONCLUSTERED 
(
	[role_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__user__F3DBC5726CC09737]    Script Date: 2025/5/25 18:42:48 ******/
ALTER TABLE [dbo].[user] ADD UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT ((0.00)) FOR [balance]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT ('ACTIVE') FOR [status]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[account_transaction] ADD  DEFAULT (getdate()) FOR [transaction_time]
GO
ALTER TABLE [dbo].[data_import] ADD  DEFAULT (getdate()) FOR [import_time]
GO
ALTER TABLE [dbo].[loan_application] ADD  DEFAULT ('pending') FOR [status]
GO
ALTER TABLE [dbo].[loan_application] ADD  DEFAULT (getdate()) FOR [application_date]
GO
ALTER TABLE [dbo].[user] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[user] ADD  DEFAULT ('active') FOR [status]
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD FOREIGN KEY([from_account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD FOREIGN KEY([to_account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[data_import]  WITH CHECK ADD FOREIGN KEY([admin_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[import_record]  WITH CHECK ADD FOREIGN KEY([import_id])
REFERENCES [dbo].[data_import] ([import_id])
GO
ALTER TABLE [dbo].[loan_application]  WITH CHECK ADD FOREIGN KEY([account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[loan_application]  WITH CHECK ADD FOREIGN KEY([approved_by])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[loan_application]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[loan_repayment]  WITH CHECK ADD  CONSTRAINT [fk_repayment_loan] FOREIGN KEY([loan_id])
REFERENCES [dbo].[loan_application] ([loan_id])
GO
ALTER TABLE [dbo].[loan_repayment] CHECK CONSTRAINT [fk_repayment_loan]
GO
ALTER TABLE [dbo].[user_role]  WITH CHECK ADD FOREIGN KEY([role_id])
REFERENCES [dbo].[role] ([role_id])
GO
ALTER TABLE [dbo].[user_role]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD CHECK  (([account_type]='checking' OR [account_type]='savings'))
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD CHECK  (([status]='CLOSED' OR [status]='FROZEN' OR [status]='ACTIVE'))
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD CHECK  (([transaction_type]='transfer' OR [transaction_type]='withdraw' OR [transaction_type]='deposit'))
GO
ALTER TABLE [dbo].[data_import]  WITH CHECK ADD CHECK  (([status]='failed' OR [status]='partial_failure' OR [status]='success'))
GO
ALTER TABLE [dbo].[import_record]  WITH CHECK ADD CHECK  (([status]='failed' OR [status]='success'))
GO
ALTER TABLE [dbo].[loan_application]  WITH CHECK ADD  CONSTRAINT [CK_loan_status] CHECK  (([status]='CLOSED' OR [status]='DISBURSED' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[loan_application] CHECK CONSTRAINT [CK_loan_status]
GO
ALTER TABLE [dbo].[user]  WITH CHECK ADD CHECK  (([status]='inactive' OR [status]='active'))
GO
/****** Object:  StoredProcedure [dbo].[sp_create_account]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
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
        INSERT INTO account (account_id, user_id, account_type, created_at)
        VALUES (@p_account_id, @p_user_id, @p_account_type, GETDATE());
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END;
GO
/****** Object:  StoredProcedure [dbo].[sp_transfer]    Script Date: 2025/5/25 18:42:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- 创建新存储过程（适配新表名）
CREATE PROCEDURE [dbo].[sp_transfer]
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
USE [master]
GO
ALTER DATABASE [BankCustomerDB] SET  READ_WRITE 
GO
