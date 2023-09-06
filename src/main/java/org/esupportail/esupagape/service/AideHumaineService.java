package org.esupportail.esupagape.service;

import org.esupportail.esupagape.entity.AideHumaine;
import org.esupportail.esupagape.entity.Document;
import org.esupportail.esupagape.entity.Dossier;
import org.esupportail.esupagape.entity.enums.StatusDossier;
import org.esupportail.esupagape.entity.enums.TypeDocument;
import org.esupportail.esupagape.exception.AgapeException;
import org.esupportail.esupagape.exception.AgapeIOException;
import org.esupportail.esupagape.exception.AgapeRuntimeException;
import org.esupportail.esupagape.exception.AgapeYearException;
import org.esupportail.esupagape.repository.AideHumaineRepository;
import org.esupportail.esupagape.repository.DocumentRepository;
import org.esupportail.esupagape.service.interfaces.importindividu.IndividuInfos;
import org.esupportail.esupagape.service.utils.UtilsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AideHumaineService {

    private final DocumentRepository documentRepository;

    private final AideHumaineRepository aideHumaineRepository;

    private final DocumentService documentService;

    private final UtilsService utilsService;

    private final DossierService dossierService;

    private final SyncService syncService;

    public AideHumaineService(DocumentRepository documentRepository, AideHumaineRepository aideHumaineRepository, DocumentService documentService, UtilsService utilsService, DossierService dossierService, SyncService syncService) {
        this.documentRepository = documentRepository;
        this.aideHumaineRepository = aideHumaineRepository;
        this.documentService = documentService;
        this.utilsService = utilsService;
        this.dossierService = dossierService;
        this.syncService = syncService;
    }

    @Transactional
    public AideHumaine create(AideHumaine aideHumaine, Long dossierId) {
        Dossier dossier = dossierService.getById(dossierId);
        if(dossier.getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        aideHumaine.setDossier(dossier);
        if (dossier.getStatusDossier().equals(StatusDossier.IMPORTE)
                || dossier.getStatusDossier().equals(StatusDossier.AJOUT_MANUEL)
                || dossier.getStatusDossier().equals(StatusDossier.ACCUEILLI)) {
            dossier.setStatusDossier(StatusDossier.SUIVI);
        }
        recupAidantWithNumEtu(aideHumaine.getNumEtuAidant(), aideHumaine);
        return aideHumaineRepository.save(aideHumaine);
    }

    public Page<AideHumaine> findByDossier(Long dossierId) {
        return aideHumaineRepository.findByDossierId(dossierId, Pageable.unpaged());
    }

    public AideHumaine getById(Long aideHumaineId) {
        return aideHumaineRepository.findById(aideHumaineId).orElseThrow();
    }

    @Transactional
    public void delete(Long aideHumaineId) {
        AideHumaine aideHumaineToUpdate = getById(aideHumaineId);
        if(aideHumaineToUpdate.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        if(aideHumaineToUpdate.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeRuntimeException("Impossible de modifier un dossier d'une année précédente");
        }
        aideHumaineRepository.deleteById(aideHumaineId);
    }

    @Transactional
    public void save(Long aideHumaineId, AideHumaine aideHumaine) throws AgapeException {
        AideHumaine aideHumaineToUpdate = getById(aideHumaineId);
        if(aideHumaineToUpdate.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        aideHumaineToUpdate.setStatusAideHumaine(aideHumaine.getStatusAideHumaine());
        aideHumaineToUpdate.setFonctionAidants(aideHumaine.getFonctionAidants());
        if (StringUtils.hasText(aideHumaine.getNumEtuAidant())) {
            if (!aideHumaine.getNumEtuAidant().equals(aideHumaineToUpdate.getNumEtuAidant())) {
                recupAidantWithNumEtu(aideHumaine.getNumEtuAidant(), aideHumaineToUpdate);
            }
        } else {
            aideHumaineToUpdate.setNumEtuAidant("");
            if (StringUtils.hasText(aideHumaine.getNameAidant())) {
                aideHumaineToUpdate.setNameAidant(aideHumaine.getNameAidant());
            }
            if (StringUtils.hasText(aideHumaine.getFirstNameAidant())) {
                aideHumaineToUpdate.setFirstNameAidant(aideHumaine.getFirstNameAidant());
            }
            if (StringUtils.hasText(aideHumaine.getPhoneAidant())) {
                aideHumaineToUpdate.setPhoneAidant(aideHumaine.getPhoneAidant());
            }
            if (StringUtils.hasText(aideHumaine.getEmailAidant())) {
                aideHumaineToUpdate.setEmailAidant(aideHumaine.getEmailAidant());
            }
        }
    }

    private void recupAidantWithNumEtu(String numEtu, AideHumaine aideHumaineToUpdate) {
        IndividuInfos individuInfos = syncService.getIndividuInfosByNumEtu(numEtu);
        if (StringUtils.hasText(individuInfos.getName())) {
            aideHumaineToUpdate.setNameAidant(individuInfos.getName());
        }
        if (StringUtils.hasText(individuInfos.getFirstName())) {
            aideHumaineToUpdate.setFirstNameAidant(individuInfos.getFirstName());
        }
        if (StringUtils.hasText(individuInfos.getEmailEtu())) {
            aideHumaineToUpdate.setEmailAidant(individuInfos.getEmailEtu());
        }
        if (individuInfos.getDateOfBirth() != null) {
            aideHumaineToUpdate.setDateOfBirthAidant(individuInfos.getDateOfBirth());
        }
        if (StringUtils.hasText(individuInfos.getFixPhone())) {
            aideHumaineToUpdate.setPhoneAidant(individuInfos.getFixPhone());
        }
        if (StringUtils.hasText(individuInfos.getContactPhone())) {
            aideHumaineToUpdate.setPhoneAidant(individuInfos.getContactPhone());
        }
        aideHumaineToUpdate.setNumEtuAidant(numEtu);
    }

    @Transactional
    public void addDocument(Long aideHumaineId, MultipartFile[] multipartFiles, TypeDocument type) throws AgapeIOException {
        AideHumaine aideHumaine = getById(aideHumaineId);
        if(aideHumaine.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                Document document = documentService.createDocument(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), aideHumaine.getId(), AideHumaine.class.getSimpleName(), aideHumaine.getDossier());
                document.setTypeDocument(type);
                aideHumaine.getPiecesJointes().add(document);
            }
        } catch (IOException e) {
            throw new AgapeIOException(e.getMessage());
        }
    }

    @Transactional
    public void deleteDocument(Long aideHumaineId, Long documentId) {
        AideHumaine aideHumaine = getById(aideHumaineId);
        if(aideHumaine.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        if(aideHumaine.getPiecesJointes().stream().anyMatch(document -> Objects.equals(document.getId(), documentId))) {
            Document document = documentService.getById(documentId);
            aideHumaine.getPiecesJointes().remove(document);
            documentService.delete(document);
        }
    }

    @Transactional
    public void getDocumentHttpResponse(Long aideHumaineId, HttpServletResponse httpServletResponse, TypeDocument type) throws AgapeIOException {
        try {
            Document document = getDocumentByType(aideHumaineId, type);
            utilsService.copyFileStreamToHttpResponse(document.getFileName(), document.getContentType(), document.getInputStream(), httpServletResponse);
        } catch (IOException e) {
            throw new AgapeIOException(e.getMessage());
        }
    }

    private Document getDocumentByType(Long aideHumaineId, TypeDocument type) {
        return documentRepository.findByParentIdAndTypeDocument(aideHumaineId, type);
    }

    @Transactional
    public List<TypeDocument> getPiecesJointesTypes(Long aideHumaineId) {
        AideHumaine aideHumaine = getById(aideHumaineId);
        return aideHumaine.getPiecesJointes().stream().map(Document::getTypeDocument).collect(Collectors.toList());
    }

    @Transactional
    public List<Document> getPiecesJointes(Long aideHumaineId) {
        AideHumaine aideHumaine = getById(aideHumaineId);
        return new ArrayList<>(aideHumaine.getPiecesJointes());
    }
}
