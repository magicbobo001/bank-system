package com.bank.customer.service;

import com.bank.customer.entity.Account;
import com.bank.customer.entity.AccountStatus;
import com.bank.customer.entity.User;
import com.bank.customer.exception.AccountNotFoundException;
import com.bank.customer.repository.AccountRepository;
import com.bank.customer.repository.AuditLogRepository;
import com.bank.customer.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.hibernate.Session;
import org.hibernate.Filter;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AccountService accountService;
    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 将 mock 的 entityManager 注入到 service 中
        ReflectionTestUtils.setField(accountService, "entityManager", entityManager);
    }

    // 测试创建账户成功场景
    @Test
    void createAccount_Success() {
        // Arrange
        Integer userId = 1;
        String accountType = "SAVINGS";
        String accountId = "ACC123456";
        User mockUser = new User();
        mockUser.setUserId(userId);
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(accountRepository.createAccount(userId, accountType)).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        Account result = accountService.createAccount(userId, accountType);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(userRepository).findById(userId);
        verify(accountRepository).createAccount(userId, accountType);
        verify(accountRepository).findById(accountId);
    }

    // 测试创建账户时用户不存在场景
    @Test
    void createAccount_UserNotFound() {
        // Arrange
        Integer userId = 999;
        String accountType = "SAVINGS";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.createAccount(userId, accountType);
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(accountRepository, never()).createAccount(anyInt(), anyString());
    }

    // 测试获取所有账户带状态筛选
    @Test
    void getAllAccounts_WithStatusFilter() {
        // Arrange
        AccountStatus status = AccountStatus.ACTIVE;
        int page = 0;
        int size = 10;
        PageRequest pageable = PageRequest.of(page, size);

        // 构造混合状态的模拟数据
        Account activeAccount = new Account();
        activeAccount.setStatus(AccountStatus.ACTIVE);
        Account frozenAccount = new Account();
        frozenAccount.setStatus(AccountStatus.FROZEN);
        @SuppressWarnings("unused")
        List<Account> allAccounts = Arrays.asList(activeAccount, frozenAccount);

        // 模拟repository返回筛选后的结果（仅1条ACTIVE）
        Page<Account> filteredPage = new PageImpl<>(Arrays.asList(activeAccount), pageable, 1);
        when(accountRepository.findByStatus(status, pageable)).thenReturn(filteredPage);

        // Act
        Page<Account> result = accountService.getAllAccounts(status, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements()); // 验证筛选后数量
        assertEquals(AccountStatus.ACTIVE, result.getContent().get(0).getStatus()); // 验证状态正确性
        verify(accountRepository).findByStatus(status, pageable); // 验证参数传递
    }

    // 测试获取所有账户不带状态筛选
    @Test
    void getAllAccounts_WithoutStatusFilter() {
        // Arrange
        int page = 0;
        int size = 10;
        PageRequest pageable = PageRequest.of(page, size);
        List<Account> mockAccounts = Arrays.asList(new Account(), new Account());
        Page<Account> mockPage = new PageImpl<>(mockAccounts, pageable, mockAccounts.size());

        when(accountRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<Account> result = accountService.getAllAccounts(null, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(accountRepository).findAll(pageable);
    }

    // 测试获取用户账户列表
    @Test
    void getUserAccounts_Success() {
        // Arrange
        Integer userId = 1;
        List<Account> mockAccounts = Arrays.asList(new Account(), new Account());
        Session mockSession = mock(Session.class);
        Filter mockFilter = mock(Filter.class);

        when(entityManager.unwrap(Session.class)).thenReturn(mockSession);
        when(mockSession.enableFilter(anyString())).thenReturn(mockFilter);
        when(mockFilter.setParameter(anyString(), anyString())).thenReturn(mockFilter);
        when(accountRepository.findByUserUserId(userId)).thenReturn(mockAccounts);

        // Act
        List<Account> result = accountService.getUserAccounts(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(entityManager).unwrap(Session.class);
        verify(mockSession).enableFilter("activeAccountFilter");
        verify(mockFilter).setParameter("status", "ACTIVE");
        verify(accountRepository).findByUserUserId(userId);
    }

    // 测试关闭账户功能
    @Test
    void closeAccount_Success() {
        // Arrange
        String accountId = "ACC123456";
        Integer operatorId = 1;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);
        mockAccount.setBalance(new BigDecimal(1000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        accountService.closeAccount(accountId, operatorId);

        // Assert
        assertEquals(AccountStatus.CLOSED, mockAccount.getStatus());
        assertEquals(BigDecimal.ZERO, mockAccount.getBalance());
        assertNotNull(mockAccount.getClosedAt());
        verify(accountRepository).save(mockAccount);
        verify(auditLogRepository).save(any());
    }

    @Test
    void testFreezeAccount_Success() {
        // Arrange
        String accountId = "ACC123456";
        Integer operatorId = 1;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.ACTIVE);
        mockAccount.setBalance(new BigDecimal(1000));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        accountService.freezeAccount(accountId, operatorId);

        // Assert
        assertEquals(AccountStatus.FROZEN, mockAccount.getStatus());
        verify(accountRepository).save(mockAccount);
    }

    // 测试冻结账户-账户不存在
    @Test
    void freezeAccount_AccountNotFound() {
        // Arrange
        String accountId = "INVALID_ACCOUNT";
        Integer operatorId = 1;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.freezeAccount(accountId, operatorId);
        });
        verify(accountRepository).findById(accountId);
        verify(accountRepository, never()).save(any());
    }

    // 测试解冻账户功能
    @Test
    void unfreezeAccount_Success() {
        // Arrange
        String accountId = "ACC123456";
        Integer operatorId = 1;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.FROZEN);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        accountService.unfreezeAccount(accountId, operatorId);

        // Assert
        assertEquals(AccountStatus.ACTIVE, mockAccount.getStatus());
        verify(accountRepository).save(mockAccount);
        verify(auditLogRepository).save(any());
    }

    // 测试恢复账户功能
    @Test
    void restoreAccount_Success() {
        // Arrange
        String accountId = "ACC123456";
        Integer operatorId = 1;
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setStatus(AccountStatus.CLOSED);
        mockAccount.setClosedAt(LocalDateTime.now().minusDays(1));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        Account result = accountService.restoreAccount(accountId, operatorId);

        // Assert
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertNull(result.getClosedAt());
        verify(accountRepository).save(mockAccount);
        verify(auditLogRepository).save(any());
    }

    // 测试标记账户逾期
    @Test
    void markAccountWithOverdue_Success() {
        // Arrange
        String accountId = "ACC123456";
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setHasOverdue(false);
        User mockUser = new User();
        mockUser.setHasOverdue(false);
        mockAccount.setUser(mockUser);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Act
        accountService.markAccountWithOverdue(accountId);

        // Assert
        assertTrue(mockAccount.isHasOverdue());
        assertTrue(mockUser.isHasOverdue());
        verify(accountRepository).save(mockAccount);
        verify(userRepository).save(mockUser);
    }
}