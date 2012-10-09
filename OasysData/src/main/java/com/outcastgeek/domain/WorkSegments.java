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
@Table(schema = "public", name = "work_segments")
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(versionField = "", table = "work_segments", schema = "public")
@RooDbManaged(automaticallyDelete = true)
public class WorkSegments {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @PersistenceContext
    transient EntityManager entityManager;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "timesheet_id")
    private Integer timesheetId;

    @Column(name = "payroll_cycle_id")
    private Integer payrollCycleId;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date date;

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

    @Column(name = "hours", precision = 17, scale = 17)
    private Double hours;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static final EntityManager entityManager() {
        EntityManager em = new WorkSegments().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

    public static long countWorkSegmentses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM WorkSegments o", Long.class).getSingleResult();
    }

    public static List<com.outcastgeek.domain.WorkSegments> findAllWorkSegmentses() {
        return entityManager().createQuery("SELECT o FROM WorkSegments o", WorkSegments.class).getResultList();
    }

    public static com.outcastgeek.domain.WorkSegments findWorkSegments(Integer id) {
        if (id == null) return null;
        return entityManager().find(WorkSegments.class, id);
    }

    public static List<com.outcastgeek.domain.WorkSegments> findWorkSegmentsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM WorkSegments o", WorkSegments.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            WorkSegments attached = WorkSegments.findWorkSegments(this.id);
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
    public com.outcastgeek.domain.WorkSegments merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        WorkSegments merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getTimesheetId() {
        return timesheetId;
    }

    public void setTimesheetId(Integer timesheetId) {
        this.timesheetId = timesheetId;
    }

    public Integer getPayrollCycleId() {
        return payrollCycleId;
    }

    public void setPayrollCycleId(Integer payrollCycleId) {
        this.payrollCycleId = payrollCycleId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
