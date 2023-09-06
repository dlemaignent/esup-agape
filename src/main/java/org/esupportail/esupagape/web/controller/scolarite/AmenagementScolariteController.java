package org.esupportail.esupagape.web.controller.scolarite;

import org.esupportail.esupagape.entity.Amenagement;
import org.esupportail.esupagape.entity.Dossier;
import org.esupportail.esupagape.entity.enums.*;
import org.esupportail.esupagape.repository.AmenagementRepository;
import org.esupportail.esupagape.service.AmenagementService;
import org.esupportail.esupagape.service.DossierService;
import org.esupportail.esupagape.service.ScolariteService;
import org.esupportail.esupagape.service.ldap.LdapPersonService;
import org.esupportail.esupagape.service.ldap.OrganizationalUnitLdap;
import org.esupportail.esupagape.service.ldap.PersonLdap;
import org.esupportail.esupagape.service.utils.UtilsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/scolarite/amenagements")
public class AmenagementScolariteController {

    private final AmenagementService amenagementService;
    private final UtilsService utilsService;
    private final DossierService dossierService;

    private final ScolariteService scolariteService;

    //TOTO création d'un service OUService
    private final LdapPersonService ldapPersonService;

    public AmenagementScolariteController(AmenagementService amenagementService, UtilsService utilsService, ScolariteService scolariteService, DossierService dossierService, AmenagementRepository amenagementRepository, ScolariteService scolariteService1, LdapPersonService ldapPersonService) {
        this.amenagementService = amenagementService;
        this.utilsService = utilsService;
        this.dossierService = dossierService;
        this.scolariteService = scolariteService;
        this.ldapPersonService = ldapPersonService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer yearFilter,
                       @RequestParam(required = false) String fullTextSearch,
                       @RequestParam(required = false) StatusAmenagement statusAmenagement,
                       @PageableDefault(size = 10,
                               sort = "createDate",
                               direction = Sort.Direction.DESC) Pageable pageable, PersonLdap personLdap, Model model) {
        if (yearFilter == null) {
            yearFilter = utilsService.getCurrentYear();
        }
        //TOTO Champ de recherche + prefix supannref id configurable

        OrganizationalUnitLdap organizationalUnitLdap = ldapPersonService.getOrganizationalUnitLdap(personLdap.getSupannEntiteAffectationPrincipale());
        String codComposante = organizationalUnitLdap.getSupannRefId().stream().filter(s -> s.toUpperCase().startsWith("{APOGEE}")).toList().get(0).split("}")[1];

        Page<Amenagement> amenagements;
        amenagements = scolariteService.getFullTextSearchScol(statusAmenagement, codComposante, utilsService.getCurrentYear(), pageable);
       // model.addAttribute("amenagements", amenagementService.getFullTextSearchScol(statusAmenagement, codComposante, utilsService.getCurrentYear(), pageable));
        model.addAttribute("amenagements", amenagements);
        model.addAttribute("codComposante", codComposante);
        model.addAttribute("yearFilter", yearFilter);
        model.addAttribute("composantes", dossierService.getAllComposantes());
        model.addAttribute("statusAmenagement", StatusAmenagement.values());
        setModel(model);
        return "scolarite/amenagements/list";
    }

    private void setModel(Model model) {
        model.addAttribute("typeAmenagements", TypeAmenagement.values());
        model.addAttribute("tempsMajores", TempsMajore.values());
        model.addAttribute("typeEpreuves", TypeEpreuve.values());
        model.addAttribute("classifications", Classification.values());
        model.addAttribute("autorisations", Autorisation.values());
        model.addAttribute("years", utilsService.getYears());
    }

    @GetMapping("/{amenagementId}")
    public String show(Amenagement amenagement, Dossier dossier, Model model) {
        setModel(model);
        model.addAttribute("dossiers", dossier);
        model.addAttribute("amenagement", amenagement);
        return "scolarite/amenagements/show";
    }

}
