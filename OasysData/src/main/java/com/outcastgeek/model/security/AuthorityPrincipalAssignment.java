package com.outcastgeek.model.security;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name = "security_role_assignments")
@Configurable
@RooJavaBean
@RooToString
@RooJpaActiveRecord(table = "security_role_assignments")
public class AuthorityPrincipalAssignment {

    @ManyToOne
    private Principal username;

    @ManyToOne
    private Authority roleId;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new AuthorityPrincipalAssignment().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAuthorityPrincipalAssignments() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AuthorityPrincipalAssignment o", Long.class).getSingleResult();
    }

	public static List<AuthorityPrincipalAssignment> findAllAuthorityPrincipalAssignments() {
        return entityManager().createQuery("SELECT o FROM AuthorityPrincipalAssignment o", AuthorityPrincipalAssignment.class).getResultList();
    }

	public static AuthorityPrincipalAssignment findAuthorityPrincipalAssignment(Long id) {
        if (id == null) return null;
        return entityManager().find(AuthorityPrincipalAssignment.class, id);
    }

	public static List<AuthorityPrincipalAssignment> findAuthorityPrincipalAssignmentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AuthorityPrincipalAssignment o", AuthorityPrincipalAssignment.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            AuthorityPrincipalAssignment attached = AuthorityPrincipalAssignment.findAuthorityPrincipalAssignment(this.id);
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
    public AuthorityPrincipalAssignment merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AuthorityPrincipalAssignment merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public Principal getUsername() {
        return this.username;
    }

	public void setUsername(Principal username) {
        this.username = username;
    }

	public Authority getRoleId() {
        return this.roleId;
    }

	public void setRoleId(Authority roleId) {
        this.roleId = roleId;
    }
}
