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
@Table(schema = "public",name = "projects")
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(versionField = "", table = "projects", schema = "public")
@RooDbManaged(automaticallyDelete = true)
public class Projects {

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new Projects().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countProjectses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Projects o", Long.class).getSingleResult();
    }

	public static List<Projects> findAllProjectses() {
        return entityManager().createQuery("SELECT o FROM Projects o", Projects.class).getResultList();
    }

	public static Projects findProjects(Integer id) {
        if (id == null) return null;
        return entityManager().find(Projects.class, id);
    }

	public static List<Projects> findProjectsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Projects o", Projects.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Projects attached = Projects.findProjects(this.id);
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
    public Projects merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Projects merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

	@Column(name = "name", length = 255)
    private String name;

	@Column(name = "client", length = 255)
    private String client;

	@Column(name = "description")
    private String description;

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

	public String getName() {
        return name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getClient() {
        return client;
    }

	public void setClient(String client) {
        this.client = client;
    }

	public String getDescription() {
        return description;
    }

	public void setDescription(String description) {
        this.description = description;
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
}
