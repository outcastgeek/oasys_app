package com.outcastgeek.model.security;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@Table(name = "security_authorities")
@RooJavaBean
@RooToString
@RooJpaActiveRecord(table = "security_authorities")
public class Authority {

    @NotNull
    @Size(min = 8, max = 10)
    private String roleId;

    @NotNull
    @Size(min = 8, max = 50)
    @Pattern(regexp = "^ROLE_[A-Z]*")
    private String authority;

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new Authority().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAuthoritys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Authority o", Long.class).getSingleResult();
    }

	public static List<Authority> findAllAuthoritys() {
        return entityManager().createQuery("SELECT o FROM Authority o", Authority.class).getResultList();
    }

	public static Authority findAuthority(Long id) {
        if (id == null) return null;
        return entityManager().find(Authority.class, id);
    }

	public static List<Authority> findAuthorityEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Authority o", Authority.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Authority attached = Authority.findAuthority(this.id);
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
    public Authority merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Authority merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public String getRoleId() {
        return this.roleId;
    }

	public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

	public String getAuthority() {
        return this.authority;
    }

	public void setAuthority(String authority) {
        this.authority = authority;
    }

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

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
