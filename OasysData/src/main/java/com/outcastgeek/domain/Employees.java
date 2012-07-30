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
@Table(schema = "public",name = "employees")
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(versionField = "", table = "employees", schema = "public")
@RooDbManaged(automaticallyDelete = true)
public class Employees {

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@Column(name = "first_name", length = 255)
    private String firstName;

	@Column(name = "last_name", length = 255)
    private String lastName;

	@Column(name = "username", length = 255)
    private String username;

	@Column(name = "email", length = 255)
    private String email;

	@Column(name = "active")
    private Boolean active;

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

	@Column(name = "unique", length = 255)
    private String unique;

	@Column(name = "provider", length = 255)
    private String provider;

	public String getFirstName() {
        return firstName;
    }

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getLastName() {
        return lastName;
    }

	public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public String getUsername() {
        return username;
    }

	public void setUsername(String username) {
        this.username = username;
    }

	public String getEmail() {
        return email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public Boolean getActive() {
        return active;
    }

	public void setActive(Boolean active) {
        this.active = active;
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

	public String getUnique() {
        return unique;
    }

	public void setUnique(String unique) {
        this.unique = unique;
    }

	public String getProvider() {
        return provider;
    }

	public void setProvider(String provider) {
        this.provider = provider;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new Employees().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countEmployeeses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Employees o", Long.class).getSingleResult();
    }

	public static List<Employees> findAllEmployeeses() {
        return entityManager().createQuery("SELECT o FROM Employees o", Employees.class).getResultList();
    }

	public static Employees findEmployees(Integer id) {
        if (id == null) return null;
        return entityManager().find(Employees.class, id);
    }

	public static List<Employees> findEmployeesEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Employees o", Employees.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Employees attached = Employees.findEmployees(this.id);
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
    public Employees merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Employees merged = this.entityManager.merge(this);
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
