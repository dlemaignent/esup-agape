package org.esupportail.esupagape.dtos;

import org.esupportail.esupagape.entity.enums.Gender;
import org.esupportail.esupagape.entity.enums.StatusDossier;
import org.esupportail.esupagape.entity.enums.StatusDossierAmenagement;
import org.esupportail.esupagape.entity.enums.TypeIndividu;

import java.time.LocalDate;


public class DossierIndividuClassDto implements DossierIndividuDto {

    private Long id;

    private String numEtu;

    private String codeIne;

    private String firstName;

    private String name;

    private LocalDate dateOfBirth;

    private TypeIndividu type;

    private StatusDossier statusDossier;

    private StatusDossierAmenagement statusDossierAmenagement;
    private Integer year;

    private Long individuId;

    private Gender gender;

    private String emailEtu;

    private Boolean desinscrit;


    public DossierIndividuClassDto(Long id, String numEtu, String codeIne, String firstName, String name, LocalDate dateOfBirth, TypeIndividu type, StatusDossier statusDossier, StatusDossierAmenagement statusDossierAmenagement, Integer year, Long individuId, Gender gender, String emailEtu, Boolean desinscript) {
        this.id = id;
        this.numEtu = numEtu;
        this.codeIne = codeIne;
        this.firstName = firstName;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.type = type;
        this.statusDossier = statusDossier;
        this.statusDossierAmenagement = statusDossierAmenagement;
        this.year = year;
        this.individuId = individuId;
        this.gender = gender;
        this.emailEtu = emailEtu;
        this.desinscrit = desinscript;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumEtu() {
        return numEtu;
    }

    public void setNumEtu(String numEtu) {
        this.numEtu = numEtu;
    }

    public String getCodeIne() {
        return codeIne;
    }

    public void setCodeIne(String codeIne) {
        this.codeIne = codeIne;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public TypeIndividu getType() {
        return type;
    }

    public void setType(TypeIndividu type) {
        this.type = type;
    }

    public StatusDossier getStatusDossier() {
        return statusDossier;
    }

    public void setStatusDossier(StatusDossier statusDossier) {
        this.statusDossier = statusDossier;
    }

    public StatusDossierAmenagement getStatusDossierAmenagement() {
        return statusDossierAmenagement;
    }

    public void setStatusDossierAmenagement(StatusDossierAmenagement statusDossierAmenagement) {
        this.statusDossierAmenagement = statusDossierAmenagement;
    }

    public Long getIndividuId() {
        return individuId;
    }

    public void setIndividuId(Long individuId) {
        this.individuId = individuId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getEmailEtu() {
        return emailEtu;
    }

    public void setEmailEtu(String emailEtu) {
        this.emailEtu = emailEtu;
    }

    public Boolean getDesinscrit() {
        return desinscrit;
    }

    public void setDesinscrit(Boolean desinscrit) {
        this.desinscrit = desinscrit;
    }

    @Override
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
