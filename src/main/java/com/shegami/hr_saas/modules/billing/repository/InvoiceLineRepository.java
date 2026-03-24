package com.shegami.hr_saas.modules.billing.repository;

import com.shegami.hr_saas.modules.billing.entity.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, String> {
}
