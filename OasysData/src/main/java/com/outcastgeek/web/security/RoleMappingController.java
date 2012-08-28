package com.outcastgeek.web.security;

import com.outcastgeek.model.security.Authority;
import com.outcastgeek.model.security.AuthorityPrincipalAssignment;
import com.outcastgeek.model.security.Principal;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/security/assignments")
@Controller
@RooWebScaffold(path = "security/assignments", formBackingObject = AuthorityPrincipalAssignment.class)
public class RoleMappingController {

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid AuthorityPrincipalAssignment authorityPrincipalAssignment, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, authorityPrincipalAssignment);
            return "security/assignments/create";
        }
        uiModel.asMap().clear();
        authorityPrincipalAssignment.persist();
        return "redirect:/security/assignments/" + encodeUrlPathSegment(authorityPrincipalAssignment.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new AuthorityPrincipalAssignment());
        return "security/assignments/create";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("authorityprincipalassignment", AuthorityPrincipalAssignment.findAuthorityPrincipalAssignment(id));
        uiModel.addAttribute("itemId", id);
        return "security/assignments/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("authorityprincipalassignments", AuthorityPrincipalAssignment.findAuthorityPrincipalAssignmentEntries(firstResult, sizeNo));
            float nrOfPages = (float) AuthorityPrincipalAssignment.countAuthorityPrincipalAssignments() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("authorityprincipalassignments", AuthorityPrincipalAssignment.findAllAuthorityPrincipalAssignments());
        }
        return "security/assignments/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid AuthorityPrincipalAssignment authorityPrincipalAssignment, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, authorityPrincipalAssignment);
            return "security/assignments/update";
        }
        uiModel.asMap().clear();
        authorityPrincipalAssignment.merge();
        return "redirect:/security/assignments/" + encodeUrlPathSegment(authorityPrincipalAssignment.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, AuthorityPrincipalAssignment.findAuthorityPrincipalAssignment(id));
        return "security/assignments/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        AuthorityPrincipalAssignment authorityPrincipalAssignment = AuthorityPrincipalAssignment.findAuthorityPrincipalAssignment(id);
        authorityPrincipalAssignment.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/security/assignments";
    }

	void populateEditForm(Model uiModel, AuthorityPrincipalAssignment authorityPrincipalAssignment) {
        uiModel.addAttribute("authorityPrincipalAssignment", authorityPrincipalAssignment);
        uiModel.addAttribute("authoritys", Authority.findAllAuthoritys());
        uiModel.addAttribute("principals", Principal.findAllPrincipals());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
}
