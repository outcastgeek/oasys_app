package com.outcastgeek.domain;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(schema = "public",name = "payroll_cycles")
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(versionField = "", table = "payroll_cycles", schema = "public")
@RooDbManaged(automaticallyDelete = true)
public class PayrollCycles {

	@Column(name = "payroll_cycle_year")
    private Integer payrollCycleYear;

	@Column(name = "payroll_cycle_number")
    private Integer payrollCycleNumber;

	@Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date startDate;

	@Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date endDate;

	@Column(name = "direct_deposit_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date directDepositDate;

	@Column(name = "check_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date checkDate;

	@Column(name = "created_at")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date createdAt;

	@Column(name = "updated_at")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date updatedAt;

	public Integer getPayrollCycleYear() {
        return payrollCycleYear;
    }

	public void setPayrollCycleYear(Integer payrollCycleYear) {
        this.payrollCycleYear = payrollCycleYear;
    }

	public Integer getPayrollCycleNumber() {
        return payrollCycleNumber;
    }

	public void setPayrollCycleNumber(Integer payrollCycleNumber) {
        this.payrollCycleNumber = payrollCycleNumber;
    }

	public Date getStartDate() {
        return startDate;
    }

	public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

	public Date getEndDate() {
        return endDate;
    }

	public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

	public Date getDirectDepositDate() {
        return directDepositDate;
    }

	public void setDirectDepositDate(Date directDepositDate) {
        this.directDepositDate = directDepositDate;
    }

	public Date getCheckDate() {
        return checkDate;
    }

	public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

	public Date getCreatedAt() {
        return createdAt;
    }

	public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

	public Date getUpdatedAt() {
        return updatedAt;
    }

	public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new PayrollCycles().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPayrollCycleses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PayrollCycles o", Long.class).getSingleResult();
    }

	public static List<PayrollCycles> findAllPayrollCycleses() {
        return entityManager().createQuery("SELECT o FROM PayrollCycles o", PayrollCycles.class).getResultList();
    }

	public static PayrollCycles findPayrollCycles(Integer id) {
        if (id == null) return null;
        return entityManager().find(PayrollCycles.class, id);
    }

	public static List<PayrollCycles> findPayrollCyclesEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PayrollCycles o", PayrollCycles.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PayrollCycles attached = PayrollCycles.findPayrollCycles(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public PayrollCycles merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PayrollCycles merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

	public Integer getId() {
        return this.id;
    }

	public void setId(Integer id) {
        this.id = id;
    }
}
