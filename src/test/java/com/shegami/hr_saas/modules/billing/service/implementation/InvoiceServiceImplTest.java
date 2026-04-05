package com.shegami.hr_saas.modules.billing.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.billing.dto.CreateInvoiceRequest;
import com.shegami.hr_saas.modules.billing.dto.InvoiceDto;
import com.shegami.hr_saas.modules.billing.dto.PaymentRequest;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.Payment;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.enums.PaymentMethod;
import com.shegami.hr_saas.modules.billing.mapper.BillingMapper;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.billing.repository.PaymentRepository;
import com.shegami.hr_saas.modules.billing.service.PdfGeneratorService;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.shared.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private PdfGeneratorService pdfGeneratorService;
    @Mock
    private UploadService uploadService;
    @Mock
    private BillingMapper billingMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private TenantService tenantService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private final String TENANT_ID = "test-tenant";
    private final String USER_ID = "user-123";
    private final String INVOICE_ID = "inv-123";

    @BeforeEach
    void setUp() {
        UserContext userContext = new UserContext(USER_ID, TENANT_ID, "test@test.com", "token");
        UserContextHolder.setCurrentUserContext(userContext);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearCurrentUserContext();
    }

    @Test
    @DisplayName("Create Invoice - Should build and save invoice correctly")
    void testCreateInvoice() {
        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                TransactionSynchronizationManager.class)) {
            // Arrange
            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setClientId("client-1");
            request.setIssueDate(LocalDate.now());
            request.setDueDate(LocalDate.now().plusDays(30));
            request.setLines(Collections.emptyList());

            Tenant tenant = new Tenant();
            when(tenantService.getTenant(TENANT_ID)).thenReturn(tenant);

            Client client = new Client();
            client.setFullName("Test Client");
            when(clientRepository.findByClientIdAndTenantTenantId("client-1", TENANT_ID))
                    .thenReturn(Optional.of(client));

            Invoice savedInvoice = new Invoice();
            savedInvoice.setInvoiceId(INVOICE_ID);
            savedInvoice.setTotalAmount(BigDecimal.ZERO);
            when(invoiceRepository.save(any())).thenReturn(savedInvoice);

            InvoiceDto dto = mock(InvoiceDto.class);
            when(billingMapper.toDto(any(), any())).thenReturn(dto);

            // Act
            InvoiceDto result = invoiceService.createInvoice(request);

            // Assert
            assertNotNull(result);
            verify(invoiceRepository).save(any());
            mockedStatic.verify(() -> TransactionSynchronizationManager.registerSynchronization(any()));
        }
    }

    @Test
    @DisplayName("Record Payment - Should update status to PAID when fully paid")
    void testRecordPayment_FullyPaid() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(INVOICE_ID);
        invoice.setTotalAmount(new BigDecimal("1000.00"));
        invoice.setStatus(InvoiceStatus.SENT);

        PaymentRequest request = new PaymentRequest();
        request.setAmount(new BigDecimal("1000.00"));
        request.setPaymentDate(LocalDate.now());
        request.setTransactionReference("REF-1");
        request.setMethod(PaymentMethod.BANK_TRANSFER);

        when(invoiceRepository.findByInvoiceIdAndTenantTenantId(INVOICE_ID, TENANT_ID))
                .thenReturn(Optional.of(invoice));

        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("1000.00"));
        when(paymentRepository.findByInvoiceInvoiceIdAndTenantTenantId(INVOICE_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(payment));

        // Act
        invoiceService.recordPayment(INVOICE_ID, request);

        // Assert
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        verify(paymentRepository).save(any());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("Get Invoice By ID - Should return DTO with payments")
    void testGetInvoiceById() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(INVOICE_ID);

        when(invoiceRepository.findByInvoiceIdAndTenantTenantId(INVOICE_ID, TENANT_ID))
                .thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceInvoiceIdAndTenantTenantId(INVOICE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());

        InvoiceDto dto = mock(InvoiceDto.class);
        when(billingMapper.toDto(invoice, Collections.emptyList())).thenReturn(dto);

        // Act
        InvoiceDto result = invoiceService.getInvoiceById(INVOICE_ID);

        // Assert
        assertNotNull(result);
        verify(invoiceRepository).findByInvoiceIdAndTenantTenantId(INVOICE_ID, TENANT_ID);
    }
}
