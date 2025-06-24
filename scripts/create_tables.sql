CREATE DATABASE BankCustomerDB;
GO
USE BankCustomerDB;
GO
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
	[has_overdue] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[user] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[user] ADD  DEFAULT ('active') FOR [status]
GO
ALTER TABLE [dbo].[user] ADD  DEFAULT ((0)) FOR [has_overdue]
GO
ALTER TABLE [dbo].[user]  WITH CHECK ADD CHECK  (([status]='inactive' OR [status]='active'))
GO

CREATE TABLE [dbo].[role](
	[role_id] [int] NOT NULL,
	[role_name] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[role_name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[user_role](
	[user_id] [int] NOT NULL,
	[role_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC,
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[user_role]  WITH CHECK ADD FOREIGN KEY([role_id])
REFERENCES [dbo].[role] ([role_id])
GO
ALTER TABLE [dbo].[user_role]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO

CREATE TABLE [dbo].[account](
	[account_id] [nvarchar](20) NOT NULL,
	[user_id] [int] NOT NULL,
	[account_type] [nvarchar](10) NOT NULL,
	[balance] [decimal](15, 2) NOT NULL,
	[status] [nvarchar](10) NOT NULL,
	[created_at] [datetime] NOT NULL,
	[closed_at] [datetime] NULL,
	[has_overdue] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[account_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT ((0.00)) FOR [balance]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT ('ACTIVE') FOR [status]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT (getdate()) FOR [created_at]
GO
ALTER TABLE [dbo].[account] ADD  DEFAULT ((0)) FOR [has_overdue]
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD CHECK  (([account_type]='checking' OR [account_type]='savings'))
GO
ALTER TABLE [dbo].[account]  WITH CHECK ADD CHECK  (([status]='CLOSED' OR [status]='FROZEN' OR [status]='ACTIVE'))
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
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[account_transaction] ADD  DEFAULT (getdate()) FOR [transaction_time]
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD FOREIGN KEY([from_account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD FOREIGN KEY([to_account_id])
REFERENCES [dbo].[account] ([account_id])
GO
ALTER TABLE [dbo].[account_transaction]  WITH CHECK ADD CHECK  (([transaction_type]='transfer' OR [transaction_type]='withdraw' OR [transaction_type]='deposit'))
GO
CREATE TABLE [dbo].[audit_log](
	[log_id] [bigint] IDENTITY(1,1) NOT NULL,
	[operation_type] [varchar](20) NOT NULL,
	[account_id] [nvarchar](20) NOT NULL,
	[operator_id] [int] NOT NULL,
	[operation_time] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[log_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[audit_log]  WITH CHECK ADD  CONSTRAINT [FK_1] FOREIGN KEY([account_id])
REFERENCES [dbo].[account] ([account_id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[audit_log] CHECK CONSTRAINT [FK_1]
GO
ALTER TABLE [dbo].[audit_log]  WITH CHECK ADD  CONSTRAINT [FK_2] FOREIGN KEY([operator_id])
REFERENCES [dbo].[user] ([user_id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[audit_log] CHECK CONSTRAINT [FK_2]
GO

CREATE TABLE [dbo].[loan_application](
	[user_id] [int] NOT NULL,
	[account_id] [nvarchar](20) NOT NULL,
	[amount] [decimal](15, 2) NOT NULL,
	[term] [int] NOT NULL,
	[interest_rate] [decimal](6, 4) NOT NULL,
	[status] [nvarchar](20) NOT NULL,
	[application_date] [datetime] NOT NULL,
	[approval_date] [datetime] NULL,
	[approved_by] [int] NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NOT NULL,
	[monthly_payment] [decimal](15, 2) NULL,
	[remaining_principal] [decimal](15, 2) NULL,
	[loan_id] [bigint] IDENTITY(1,1) NOT NULL,
	[disbursement_date] [date] NULL,
 CONSTRAINT [PK_loan_application] PRIMARY KEY CLUSTERED 
(
	[loan_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[loan_application] ADD  DEFAULT ('pending') FOR [status]
GO
ALTER TABLE [dbo].[loan_application] ADD  DEFAULT (getdate()) FOR [application_date]
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
ALTER TABLE [dbo].[loan_application]  WITH CHECK ADD  CONSTRAINT [CK_loan_status] CHECK  (([status]='DEFAULT' OR [status]='CLOSED' OR [status]='DISBURSED' OR [status]='REJECTED' OR [status]='APPROVED' OR [status]='PENDING'))
GO
ALTER TABLE [dbo].[loan_application] CHECK CONSTRAINT [CK_loan_status]
GO

CREATE TABLE [dbo].[loan_repayment](
	[loan_id] [bigint] NOT NULL,
	[repayment_date] [date] NOT NULL,
	[amount] [decimal](15, 2) NOT NULL,
	[principal] [decimal](15, 2) NOT NULL,
	[interest] [decimal](15, 2) NOT NULL,
	[status] [nvarchar](20) NOT NULL,
	[repayment_id] [bigint] IDENTITY(1,1) NOT NULL,
	[late_fee] [decimal](15, 2) NOT NULL,
	[actual_repayment_date] [date] NULL,
 CONSTRAINT [PK_loan_repayment] PRIMARY KEY CLUSTERED 
(
	[repayment_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[loan_repayment] ADD  DEFAULT ((0.00)) FOR [late_fee]
GO
ALTER TABLE [dbo].[loan_repayment]  WITH CHECK ADD  CONSTRAINT [fk_repayment_loan] FOREIGN KEY([loan_id])
REFERENCES [dbo].[loan_application] ([loan_id])
GO
ALTER TABLE [dbo].[loan_repayment] CHECK CONSTRAINT [fk_repayment_loan]
GO
