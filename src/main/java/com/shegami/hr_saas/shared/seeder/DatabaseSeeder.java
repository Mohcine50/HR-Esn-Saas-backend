package com.shegami.hr_saas.shared.seeder;

import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.SubscriptionPlan;
import com.shegami.hr_saas.modules.auth.enums.TenantStatus;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.auth.repository.TenantRepository;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.repository.UserRoleRepository;
import com.shegami.hr_saas.modules.billing.entity.Invoice;
import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import com.shegami.hr_saas.modules.billing.enums.InvoiceStatus;
import com.shegami.hr_saas.modules.billing.repository.InvoiceLineRepository;
import com.shegami.hr_saas.modules.billing.repository.InvoiceRepository;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import com.shegami.hr_saas.modules.hr.repository.EmployeeRepository;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.enums.*;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.modules.timesheet.entity.Timesheet;
import com.shegami.hr_saas.modules.timesheet.entity.TimesheetEntry;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetEntryRepository;
import com.shegami.hr_saas.modules.timesheet.repository.TimesheetRepository;
import com.shegami.hr_saas.modules.notifications.entity.Notification;
import com.shegami.hr_saas.modules.notifications.enums.*;
import com.shegami.hr_saas.modules.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ConsultantRepository consultantRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final MissionRepository missionRepository;
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (tenantRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("Starting database seeding...");

        // 1. Roles
        Map<UserRoles, UserRole> roles = seedRoles();

        // 2. Tenant
        Tenant tenant = new Tenant();
        tenant.setName("Shegami Corp");
        tenant.setDomain("shegami.com");
        tenant.setPlan(SubscriptionPlan.ENTERPRISE);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);

        // 3. Users & Employees
        User adminUser = createUser("Admin", "User", "admin@shegami.com", "password123", tenant,
                roles.get(UserRoles.ADMIN));
        createEmployee(adminUser, tenant);

        User managerUser = createUser("Manager", "User", "manager@shegami.com", "password123", tenant,
                roles.get(UserRoles.MANAGER));
        Employee managerEmployee = createEmployee(managerUser, tenant);

        User financialUser = createUser("Financial", "User", "finance@shegami.com", "password123", tenant,
                roles.get(UserRoles.FINANCIAL));
        createEmployee(financialUser, tenant);

        // 4. Consultants
        Consultant consultant1 = createConsultant("Alice", "Smith", "alice@consultant.com", tenant,
                roles.get(UserRoles.CONSULTANT), ConsultantLevel.SENIOR);
        Consultant consultant2 = createConsultant("Bob", "Jones", "bob@consultant.com", tenant,
                roles.get(UserRoles.CONSULTANT), ConsultantLevel.CONFIRMED);
        Consultant consultant3 = createConsultant("Charlie", "Brown", "charlie@consultant.com", tenant,
                roles.get(UserRoles.CONSULTANT), ConsultantLevel.JUNIOR);

        // 5. Clients
        Client client1 = createClient("TechGiant SA", "Paris, France", "FR123456789", "billing@techgiant.com", tenant);
        Client client2 = createClient("EcoSolution SRL", "Lyon, France", "FR987654321", "contact@ecosolution.com",
                tenant);

        // 6. Projects
        Project project1 = createProject("Cloud Migration", "Migrate legacy apps to AWS", Priority.HIGH, client1,
                tenant);
        Project project2 = createProject("Mobile App Dev", "New React Native app", Priority.MEDIUM, client1, tenant);
        Project project3 = createProject("ERP Cleanup", "Data cleansing for ERP", Priority.LOW, client2, tenant);

        // 7. Missions
        Mission mission1 = createMission("Cloud Architecture Design", project1, client1, tenant, consultant1,
                managerEmployee);
        Mission mission2 = createMission("UI/UX Design Mobile", project2, client1, tenant, consultant2,
                managerEmployee);
        Mission mission3 = createMission("Backend Integration", project2, client1, tenant, consultant1,
                managerEmployee);
        Mission mission4 = createMission("Data Mapping", project3, client2, tenant, consultant3, managerEmployee);

        // 8. Timesheets
        Timesheet ts1 = seedTimesheets(tenant, consultant1, mission1);
        seedTimesheets(tenant, consultant2, mission2);

        // 9. Invoices
        Invoice inv1 = seedInvoices(tenant, client1, InvoiceStatus.PAID, "INV-2026-001", new BigDecimal("12000.00"));
        Invoice inv2 = seedInvoices(tenant, client1, InvoiceStatus.OVERDUE, "INV-2026-002", new BigDecimal("8500.00"));
        Invoice inv3 = seedInvoices(tenant, client2, InvoiceStatus.SENT, "INV-2026-003", new BigDecimal("4200.00"));

        // 10. Notifications
        seedNotifications(tenant, adminUser, managerUser, consultant1, mission1, ts1, inv1, inv2);

        log.info("Database seeding completed successfully!");
    }

    private Map<UserRoles, UserRole> seedRoles() {
        Map<UserRoles, UserRole> rolesMap = new HashMap<>();
        for (UserRoles roleName : UserRoles.values()) {
            UserRole role = new UserRole();
            role.setName(roleName);
            rolesMap.put(roleName, roleRepository.save(role));
        }
        return rolesMap;
    }

    private User createUser(String first, String last, String email, String pwd, Tenant tenant, UserRole role) {
        User user = new User();
        user.setFirstName(first);
        user.setLastName(last);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(pwd));
        user.setTenant(tenant);
        user.setStatus(UserStatus.ACTIVE);
        user.setIsEmailVerified(true);
        user.setRoles(new ArrayList<>(Collections.singletonList(role)));
        return userRepository.save(user);
    }

    private Employee createEmployee(User user, Tenant tenant) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setTenant(tenant);
        employee.setPosition("Employee");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setSalary(new BigDecimal(800 + Math.random() * 200));
        employee.setCurrency("EUR");
        employee.setHireDate(LocalDateTime.now());
        return employeeRepository.save(employee);
    }

    private Consultant createConsultant(String first, String last, String email, Tenant tenant, UserRole role,
            ConsultantLevel level) {
        User user = createUser(first, last, email, "password123", tenant, role);
        Consultant consultant = new Consultant();
        consultant.setFirstName(first);
        consultant.setLastName(last);
        consultant.setEmail(email);
        consultant.setType(ConsultantType.CDI);
        consultant.setStatus(ConsultantStatus.ON_MISSION);
        consultant.setInternalDailyCost(new BigDecimal("450.00"));
        consultant.setSeniority(level);
        consultant.setUser(user);
        consultant.setTenant(tenant);
        consultant.setProjects(new HashSet<>());
        return consultantRepository.save(consultant);
    }

    private Client createClient(String name, String address, String vat, String contactEmail, Tenant tenant) {
        Client client = new Client();
        client.setFullName(name);
        client.setAddress(address);
        client.setVatNumber(vat);
        client.setEmail(contactEmail);
        client.setTenant(tenant);
        return clientRepository.save(client);
    }

    private Project createProject(String name, String desc, Priority priority, Client client, Tenant tenant) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(desc);
        project.setPriority(priority);
        project.setProjectStatus(ProjectStatus.IN_PROGRESS);
        project.setClient(client);
        project.setTenant(tenant);
        project.setConsultants(new HashSet<>());
        return projectRepository.save(project);
    }

    private Mission createMission(String title, Project project, Client client, Tenant tenant, Consultant consultant,
            Employee manager) {
        Mission mission = new Mission();
        mission.setTitle(title);
        mission.setProject(project);
        mission.setClient(client);
        mission.setTenant(tenant);
        mission.setConsultants(new HashSet<>(Collections.singletonList(consultant)));
        mission.setAccountManager(manager);
        mission.setStatus(MissionStatus.ACTIVE);
        mission.setPriority(Priority.HIGH);
        mission.setStartDate(LocalDate.now().minusMonths(1));
        mission.setEndDate(LocalDate.now().plusMonths(5));

        // Update bidirectionals
        if (project.getConsultants() == null)
            project.setConsultants(new HashSet<>());
        project.getConsultants().add(consultant);
        if (consultant.getProjects() == null)
            consultant.setProjects(new HashSet<>());
        consultant.getProjects().add(project);

        projectRepository.save(project);
        consultantRepository.save(consultant);

        return missionRepository.save(mission);
    }

    private Timesheet seedTimesheets(Tenant tenant, Consultant consultant, Mission mission) {
        Timesheet ts = new Timesheet();
        ts.setTenant(tenant);
        ts.setConsultant(consultant);
        ts.setMission(mission);
        ts.setMonth(LocalDate.now().getMonthValue());
        ts.setYear(LocalDate.now().getYear());
        ts.setStatus(TimesheetStatus.APPROVED);
        ts.setEntries(new HashSet<>());
        ts = timesheetRepository.save(ts);

        for (int i = 1; i <= 20; i++) {
            TimesheetEntry entry = new TimesheetEntry();
            entry.setTimesheet(ts);
            entry.setDate(LocalDate.of(ts.getYear(), ts.getMonth(), i));
            entry.setQuantity(1.0);
            entry.setComment("Worked on " + mission.getTitle());
            ts.getEntries().add(entry);
            timesheetEntryRepository.save(entry);
        }
        return timesheetRepository.save(ts);
    }

    private Invoice seedInvoices(Tenant tenant, Client client, InvoiceStatus status, String number, BigDecimal total) {
        Invoice invoice = new Invoice();
        invoice.setTenant(tenant);
        invoice.setClient(client);
        invoice.setInvoiceNumber(number);
        invoice.setIssueDate(LocalDate.now().minusDays(10));
        invoice.setDueDate(LocalDate.now().plusDays(status == InvoiceStatus.OVERDUE ? -1 : 20));
        invoice.setStatus(status);
        invoice.setClientNameAtBilling(client.getFullName());
        invoice.setClientAddressAtBilling(client.getAddress());
        invoice.setVatNumberAtBilling(client.getVatNumber());
        invoice.setSubTotal(total.divide(new BigDecimal("1.2"), 2, java.math.RoundingMode.HALF_UP));
        invoice.setVatAmount(total.subtract(invoice.getSubTotal()));
        invoice.setTotalAmount(total);
        invoice.setInvoiceLines(new HashSet<>());
        invoice = invoiceRepository.save(invoice);

        InvoiceLine line = new InvoiceLine();
        line.setTenant(tenant);
        line.setInvoice(invoice);
        line.setDescription("Professional Services - " + number);
        line.setQuantity(new BigDecimal("1"));
        line.setUnitPrice(invoice.getSubTotal());
        line.setTotalLineAmount(invoice.getSubTotal());
        invoice.getInvoiceLines().add(line);
        invoiceLineRepository.save(line);
        return invoiceRepository.save(invoice);
    }

    private void seedNotifications(Tenant tenant, User admin, User manager, Consultant consultant, Mission mission,
            Timesheet ts, Invoice invoice, Invoice overdueInvoice) {
        // Mission notifications
        createNotification(tenant, consultant.getUser(), admin, NotificationType.CONSULTANT_ASSIGNED,
                EntityType.MISSION, mission.getMissionId(), "Mission Assigned",
                "You have been assigned to: " + mission.getTitle());
        createNotification(tenant, manager, admin, NotificationType.MISSION_STATUS_CHANGED, EntityType.MISSION,
                mission.getMissionId(), "Mission Update", mission.getTitle() + " status has been updated to ACTIVE");

        // Timesheet notifications
        createNotification(tenant, manager, consultant.getUser(), NotificationType.TIMESHEET_SUBMITTED,
                EntityType.TIMESHEET, ts.getTimesheetId(), "Timesheet for Approval",
                consultant.getFirstName() + " submitted their monthly timesheet.");
        createNotification(tenant, consultant.getUser(), manager, NotificationType.TIMESHEET_APPROVED,
                EntityType.TIMESHEET, ts.getTimesheetId(), "Timesheet Approved",
                "Your timesheet for " + ts.getMonth() + "/" + ts.getYear() + " has been approved.");
        createNotification(tenant, consultant.getUser(), manager, NotificationType.TIMESHEET_REJECTED,
                EntityType.TIMESHEET, ts.getTimesheetId(), "Timesheet Rejected",
                "Please check your timesheet for some missing entries.");

        // Invoice notifications
        createNotification(tenant, admin, null, NotificationType.INVOICE_GENERATED, EntityType.INVOICE,
                invoice.getInvoiceId(), "New Invoice",
                "Invoice " + invoice.getInvoiceNumber() + " has been generated.");
        createNotification(tenant, admin, null, NotificationType.INVOICE_OVERDUE, EntityType.INVOICE,
                invoice.getInvoiceId(), "Payment Overdue",
                "Invoice " + invoice.getInvoiceNumber() + " is past its due date!");
        createNotification(tenant, admin, manager, NotificationType.PAYMENT_RECORDED, EntityType.INVOICE,
                invoice.getInvoiceId(), "Payment Received",
                "Payment for " + invoice.getInvoiceNumber() + " has been confirmed.");

        // Comment notifications (dummy)
        createNotification(tenant, admin, manager, NotificationType.MISSION_COMMENT_ADDED, EntityType.MISSION,
                mission.getMissionId(), "New Comment",
                manager.getFirstName() + " left a comment on " + mission.getTitle());

        // System notifications
        createNotification(tenant, admin, null, NotificationType.SYSTEM_ANNOUNCEMENT, EntityType.SYSTEM, null,
                "Server Maintenance", "Planned maintenance on Sunday at 2 AM.");
        createNotification(tenant, consultant.getUser(), null, NotificationType.SYSTEM_UPDATE, EntityType.SYSTEM, null,
                "New Feature", "You can now export your missions as PDF!");

        // HR & Invitation notifications
        createNotification(tenant, consultant.getUser(), admin, NotificationType.INVITATION_SENT, EntityType.ACCOUNT,
                null, "Invitation Sent", "Your invitation to join Shegami Corp has been sent.");
        createNotification(tenant, admin, manager, NotificationType.EMPLOYEE_ONBOARDED, EntityType.ACCOUNT,
                manager.getUserId(), "New Employee", manager.getFirstName() + " has completed onboarding.");

        // Mission & Project remaining
        createNotification(tenant, consultant.getUser(), admin, NotificationType.CONSULTANT_REMOVED_FROM_MISSION,
                EntityType.MISSION, mission.getMissionId(), "Removed from Mission",
                "You have been removed from " + mission.getTitle());
        createNotification(tenant, consultant.getUser(), null, NotificationType.MISSION_DEADLINE_APPROACHING,
                EntityType.MISSION, mission.getMissionId(), "Deadline Warning",
                "Mission " + mission.getTitle() + " ends in 3 days.");
        createNotification(tenant, consultant.getUser(), admin, NotificationType.PROJECT_CONSULTANT_ASSIGNED,
                EntityType.PROJECT, mission.getProject().getProjectId(), "Project Assigned",
                "You've been assigned to project: " + mission.getProject().getName());
        createNotification(tenant, manager, admin, NotificationType.PROJECT_STATUS_CHANGED, EntityType.PROJECT,
                mission.getProject().getProjectId(), "Project Update",
                "Project " + mission.getProject().getName() + " is now IN_PROGRESS");
    }

    private void createNotification(Tenant tenant, User recipient, User actor, NotificationType type, EntityType eType,
            String eId, String title, String msg) {
        Notification n = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .actorName(actor != null ? actor.getFirstName() + " " + actor.getLastName() : "System")
                .notificationType(type)
                .entityType(eType)
                .entityId(eId)
                .title(title)
                .message(msg)
                .status(NotificationStatus.UNREAD)
                .sentInApp(true)
                .build();
        n.setTenant(tenant);
        notificationRepository.save(n);
    }
}
