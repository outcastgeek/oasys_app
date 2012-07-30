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

@Configurable
@Entity
@Table(schema = "public",name = "time_sheets")
@RooJavaBean
@RooToString
@RooJpaActiveRecord(versionField = "", table = "time_sheets", schema = "public")
@RooDbManaged(automaticallyDelete = true)
public class TimeSheets {

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new TimeSheets().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countTimeSheetses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM TimeSheets o", Long.class).getSingleResult();
    }

	public static List<TimeSheets> findAllTimeSheetses() {
        return entityManager().createQuery("SELECT o FROM TimeSheets o", TimeSheets.class).getResultList();
    }

	public static TimeSheets findTimeSheets(Integer id) {
        if (id == null) return null;
        return entityManager().find(TimeSheets.class, id);
    }

	public static List<TimeSheets> findTimeSheetsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM TimeSheets o", TimeSheets.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            TimeSheets attached = TimeSheets.findTimeSheets(this.id);
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
    public TimeSheets merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        TimeSheets merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	@Column(name = "employee_id")
    private Integer employeeId;

	@Column(name = "payroll_cycle_id")
    private Integer payrollCycleId;

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

	@Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date startDate;

	@Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date endDate;

	public Integer getEmployeeId() {
        return employeeId;
    }

	public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

	public Integer getPayrollCycleId() {
        return payrollCycleId;
    }

	public void setPayrollCycleId(Integer payrollCycleId) {
        this.payrollCycleId = payrollCycleId;
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

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
