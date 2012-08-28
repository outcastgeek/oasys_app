package com.outcastgeek.web;

import com.outcastgeek.domain.Employees;
import com.outcastgeek.model.security.Authority;
import com.outcastgeek.model.security.AuthorityPrincipalAssignment;
import com.outcastgeek.model.security.Principal;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

@Configurable
/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}

	public Converter<Employees, String> getEmployeesToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.outcastgeek.domain.Employees, java.lang.String>() {
            public String convert(Employees employees) {
                return new StringBuilder().append(employees.getFirstName()).append(' ').append(employees.getLastName()).append(' ').append(employees.getUsername()).append(' ').append(employees.getEmail()).toString();
            }
        };
    }

	public Converter<Integer, Employees> getIdToEmployeesConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Integer, com.outcastgeek.domain.Employees>() {
            public com.outcastgeek.domain.Employees convert(java.lang.Integer id) {
                return Employees.findEmployees(id);
            }
        };
    }

	public Converter<String, Employees> getStringToEmployeesConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.outcastgeek.domain.Employees>() {
            public com.outcastgeek.domain.Employees convert(String id) {
                return getObject().convert(getObject().convert(id, Integer.class), Employees.class);
            }
        };
    }

	public Converter<Authority, String> getAuthorityToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.outcastgeek.model.security.Authority, java.lang.String>() {
            public String convert(Authority authority) {
                return new StringBuilder().append(authority.getRoleId()).append(' ').append(authority.getAuthority()).toString();
            }
        };
    }

	public Converter<Long, Authority> getIdToAuthorityConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.outcastgeek.model.security.Authority>() {
            public com.outcastgeek.model.security.Authority convert(java.lang.Long id) {
                return Authority.findAuthority(id);
            }
        };
    }

	public Converter<String, Authority> getStringToAuthorityConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.outcastgeek.model.security.Authority>() {
            public com.outcastgeek.model.security.Authority convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Authority.class);
            }
        };
    }

	public Converter<AuthorityPrincipalAssignment, String> getAuthorityPrincipalAssignmentToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.outcastgeek.model.security.AuthorityPrincipalAssignment, java.lang.String>() {
            public String convert(AuthorityPrincipalAssignment authorityPrincipalAssignment) {
                return "(no displayable fields)";
            }
        };
    }

	public Converter<Long, AuthorityPrincipalAssignment> getIdToAuthorityPrincipalAssignmentConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.outcastgeek.model.security.AuthorityPrincipalAssignment>() {
            public com.outcastgeek.model.security.AuthorityPrincipalAssignment convert(java.lang.Long id) {
                return AuthorityPrincipalAssignment.findAuthorityPrincipalAssignment(id);
            }
        };
    }

	public Converter<String, AuthorityPrincipalAssignment> getStringToAuthorityPrincipalAssignmentConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.outcastgeek.model.security.AuthorityPrincipalAssignment>() {
            public com.outcastgeek.model.security.AuthorityPrincipalAssignment convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), AuthorityPrincipalAssignment.class);
            }
        };
    }

	public Converter<Principal, String> getPrincipalToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.outcastgeek.model.security.Principal, java.lang.String>() {
            public String convert(Principal principal) {
                return new StringBuilder().append(principal.getUsername()).append(' ').append(principal.getPassword()).toString();
            }
        };
    }

	public Converter<Long, Principal> getIdToPrincipalConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.outcastgeek.model.security.Principal>() {
            public com.outcastgeek.model.security.Principal convert(java.lang.Long id) {
                return Principal.findPrincipal(id);
            }
        };
    }

	public Converter<String, Principal> getStringToPrincipalConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.outcastgeek.model.security.Principal>() {
            public com.outcastgeek.model.security.Principal convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Principal.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getEmployeesToStringConverter());
        registry.addConverter(getIdToEmployeesConverter());
        registry.addConverter(getStringToEmployeesConverter());
        registry.addConverter(getAuthorityToStringConverter());
        registry.addConverter(getIdToAuthorityConverter());
        registry.addConverter(getStringToAuthorityConverter());
        registry.addConverter(getAuthorityPrincipalAssignmentToStringConverter());
        registry.addConverter(getIdToAuthorityPrincipalAssignmentConverter());
        registry.addConverter(getStringToAuthorityPrincipalAssignmentConverter());
        registry.addConverter(getPrincipalToStringConverter());
        registry.addConverter(getIdToPrincipalConverter());
        registry.addConverter(getStringToPrincipalConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
