package org.esupportail.esupagape.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.esupportail.esupagape.config.ApplicationProperties;
import org.esupportail.esupagape.entity.AideHumaine;
import org.esupportail.esupagape.entity.Dossier;
import org.esupportail.esupagape.entity.ExcludeIndividu;
import org.esupportail.esupagape.entity.Individu;
import org.esupportail.esupagape.entity.enums.StatusDossier;
import org.esupportail.esupagape.entity.enums.TypeIndividu;
import org.esupportail.esupagape.exception.AgapeException;
import org.esupportail.esupagape.exception.AgapeJpaException;
import org.esupportail.esupagape.exception.AgapeRuntimeException;
import org.esupportail.esupagape.repository.ExcludeIndividuRepository;
import org.esupportail.esupagape.repository.IndividuRepository;
import org.esupportail.esupagape.service.interfaces.importindividu.IndividuSourceService;
import org.esupportail.esupagape.service.utils.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@EnableConfigurationProperties(ApplicationProperties.class)
public class IndividuService {

    private static final Logger logger = LoggerFactory.getLogger(IndividuService.class);

    private final List<IndividuSourceService> individuSourceServices;

    private final ApplicationProperties applicationProperties;

    private final IndividuRepository individuRepository;

    private final UtilsService utilsService;

    private final ExcludeIndividuRepository excludeIndividuRepository;

    private final EnqueteService enqueteService;

    private final DossierService dossierService;

    private final SyncService syncService;

    public IndividuService(List<IndividuSourceService> individuSourceServices, ApplicationProperties applicationProperties, IndividuRepository individuRepository, UtilsService utilsService, ExcludeIndividuRepository excludeIndividuRepository, DossierService dossierService, EnqueteService enqueteService, SyncService syncService) {
        this.individuSourceServices = individuSourceServices;
        this.applicationProperties = applicationProperties;
        this.individuRepository = individuRepository;
        this.utilsService = utilsService;
        this.excludeIndividuRepository = excludeIndividuRepository;
        this.dossierService = dossierService;
        this.enqueteService = enqueteService;
        this.syncService = syncService;
    }

    public Individu getIndividu(String numEtu) {
        return individuRepository.findByNumEtu(numEtu);
    }

    public Individu getIndividu(String name, String firstName, LocalDate dateOfBirth) {
        return individuRepository.findByNameIgnoreCaseAndFirstNameIgnoreCaseAndDateOfBirth(name, firstName, dateOfBirth);
    }

    public List<Individu> getAllIndividus() {
        return individuRepository.findAll();
    }

    @Transactional
    public void importIndividus() throws AgapeException {
        logger.info("Import individus started");
        List<Individu> individus = individuRepository.findAll();
        List<ExcludeIndividu> excludeIndividus = excludeIndividuRepository.findAll();
        for (IndividuSourceService individuSourceService : individuSourceServices) {
            List<Individu> individusFromSource = individuSourceService.getAllIndividuNums();
            List<Individu> individusToCreate = individusFromSource.stream().filter(individuToCreate -> individus.stream().noneMatch(individuInDataBase -> individuInDataBase.getNumEtu() != null && individuInDataBase.getNumEtu().equals(individuToCreate.getNumEtu())) && excludeIndividus.stream().noneMatch(excludeIndividu -> excludeIndividu.getNumEtuHash().equals(new DigestUtils("SHA3-256").digestAsHex(individuToCreate.getNumEtu())))).toList();
            individuRepository.saveAll(individusToCreate);
            List<Dossier> dossiers = new ArrayList<>();
            for (Individu individu : individusToCreate) {
                logger.info("Importing : " + individu.getNumEtu() + " " + individu.getFirstName() + " " + individu.getName());
                Dossier dossier = dossierService.create(individu, null, StatusDossier.IMPORTE);
                dossiers.add(dossier);
                individu.getDossiers().add(dossier);
            }
            dossierService.saveAll(dossiers);
        }
        logger.info("Import individus done");
    }

    public void syncAllIndividus() {
        logger.info("Sync individus started");
        List<Long> individusIds = individuRepository.findIdsAll();
        int count = 0;
        for (Long individuId : individusIds) {
            count++;
            try {
                syncService.syncIndividu(individuId);
            } catch (AgapeRuntimeException e) {
                logger.debug(e.getMessage() + " for individu " + individuId);
            }
        }
        logger.info("Sync individus done : " + count);
    }

    public void save(Individu individuToAdd, TypeIndividu typeIndividu, String force) throws AgapeJpaException {
        ExcludeIndividu excludeIndividu = null;
        if (StringUtils.hasText(individuToAdd.getNumEtu())) {
            excludeIndividu = excludeIndividuRepository.findByNumEtuHash(new DigestUtils("SHA3-256").digestAsHex(individuToAdd.getNumEtu()));
            if (force == null && excludeIndividu != null) {
                throw new AgapeJpaException("L'étudiant est dans la liste d'exclusion");
            }
        }
        Individu foundIndividu = null;
        if (StringUtils.hasText(individuToAdd.getNumEtu())) {
            foundIndividu = individuRepository.findByNumEtu(individuToAdd.getNumEtu());
        } else {
            //numEtu à null pour éviter la contrainte d’unicité
            individuToAdd.setNumEtu(null);
        }
        if (foundIndividu != null) {
            dossierService.create(foundIndividu, null, StatusDossier.AJOUT_MANUEL);
        } else {
            if (excludeIndividu != null) {
                // suppression de l'exclusion si l’insertion est forcée
                excludeIndividuRepository.delete(excludeIndividu);
            }
            individuRepository.save(individuToAdd);
            dossierService.create(individuToAdd, typeIndividu, StatusDossier.AJOUT_MANUEL);
        }
    }

    public Individu findById(Long id) throws AgapeException {
        Optional<Individu> optionalIndividu = individuRepository.findById(id);
        if (optionalIndividu.isPresent()) {
            return optionalIndividu.get();
        } else {
            throw new AgapeException("Je n'ai pas trouvé cet individu");
        }
    }

    public Page<Individu> searchByName(String name, Pageable pageable) {
        return individuRepository.findAllByNameContainsIgnoreCase(name, pageable);
    }

    @Transactional
    public Individu create(Individu individu, TypeIndividu typeIndividu, String force) throws AgapeJpaException {
        Individu individuTestIsExist = null;
        if (StringUtils.hasText(individu.getNumEtu())) {
            individuTestIsExist = getIndividu(individu.getNumEtu());
            if (individuTestIsExist == null) {
                individuTestIsExist = createFromSources(individu.getNumEtu(), force);
            }
        } else if (StringUtils.hasText(individu.getCodeIne())) {
            individuTestIsExist = getIndividu(individu.getCodeIne());
            if (individuTestIsExist == null) {
                individuTestIsExist = createFromSources(individu.getCodeIne(), force);
            }
        } else if (StringUtils.hasText(individu.getName()) && StringUtils.hasText(individu.getFirstName()) && individu.getDateOfBirth() != null) {
            individu.setCodeIne(null);
            individuTestIsExist = getIndividu(individu.getName(), individu.getFirstName(), individu.getDateOfBirth());
            if (individuTestIsExist == null) {
                Individu newIndividu = createFromSources(individu.getName(), individu.getFirstName(), individu.getDateOfBirth(), force);
                if (newIndividu != null) {
                    return newIndividu;
                } else {
                    save(individu, typeIndividu, force);
                    return individu;
                }
            }
        }
        if (individuTestIsExist != null) {
            if (individuTestIsExist.getDossiers().stream().noneMatch(dossier -> dossier.getYear().equals(utilsService.getCurrentYear()))) {
                Dossier dossier = dossierService.create(individuTestIsExist, null, StatusDossier.AJOUT_MANUEL);
                syncService.syncDossier(dossier.getId());
            }
            try {
                syncService.syncIndividu(individuTestIsExist.getId());
            } catch (AgapeJpaException e) {
                throw new RuntimeException(e);
            }
            return individuTestIsExist;
        } else if (StringUtils.hasText(individu.getCodeIne()) && StringUtils.hasText(individu.getName()) && StringUtils.hasText(individu.getFirstName()) && individu.getDateOfBirth() != null && StringUtils.hasText(individu.getSex())) {
            save(individu, typeIndividu, force);
        }
        return individu;
    }

    public Individu createFromSources(String code, String force) throws AgapeJpaException {
        Individu individuFromSources = null;
        for (IndividuSourceService individuSourceService : individuSourceServices) {
            individuFromSources = individuSourceService.getIndividuByNumEtu(code);
            if (individuFromSources != null) {
                break;
            }
        }
        if (individuFromSources == null) {
            for (IndividuSourceService individuSourceService : individuSourceServices) {
                individuFromSources = individuSourceService.getIndividuByCodeIne(code);
                if (individuFromSources != null) {
                    break;
                }
            }
        }
        if (individuFromSources != null) {
            Individu individuTestIsExist = getIndividu(individuFromSources.getNumEtu());
            if(individuTestIsExist != null) {
                return individuTestIsExist;
            }
            save(individuFromSources, null, force);
        }
        return individuFromSources;
    }

    public Individu createFromSources(String name, String firstName, LocalDate dateOfBirth, String force) throws AgapeJpaException {
        Individu individuFromSources = null;
        for (IndividuSourceService individuSourceService : individuSourceServices) {
            individuFromSources = individuSourceService.getIndividuByProperties(name, firstName, dateOfBirth);
            if (individuFromSources != null) {
                break;
            }
        }
        if (individuFromSources != null) {
            save(individuFromSources, null, force);
        }
        return individuFromSources;
    }

    public Individu getById(Long id) {
        return individuRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteIndividu(long id) {
        Individu individu = getById(id);
        enqueteService.detachAllByDossiers(id);
        if (StringUtils.hasText(individu.getNumEtu())) {
            ExcludeIndividu excludeIndividu = excludeIndividuRepository.findByNumEtuHash(new DigestUtils("SHA3-256").digestAsHex(individu.getNumEtu()));
            if (excludeIndividu == null) {
                excludeIndividu = new ExcludeIndividu();
                excludeIndividu.setNumEtuHash(new DigestUtils("SHA3-256").digestAsHex(individu.getNumEtu()));
                excludeIndividuRepository.save(excludeIndividu);
            }
        }
        this.individuRepository.delete(individu);
    }

    public ResponseEntity<byte[]> getPhoto(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
        ResponseEntity<byte[]> httpResponse = setNoPhoto();
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(1000);
        clientHttpRequestFactory.setReadTimeout(1000);
        RestTemplate template = new RestTemplate(clientHttpRequestFactory);
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<>();
        HttpEntity<Object> request = new HttpEntity<>(multipartMap, headers);
        Individu individu = getById(id);
        if (StringUtils.hasText(applicationProperties.getDisplayPhotoUriPattern()) && individu != null && individu.getPhotoId() != null && !individu.getPhotoId().isEmpty()) {
            String uri = MessageFormat.format(applicationProperties.getDisplayPhotoUriPattern(), individu.getPhotoId());
            try {
                httpResponse = template.exchange(uri, HttpMethod.GET, request, byte[].class);
            } catch (RestClientException e) {
                logger.debug("photo not found for " + id);
            }
        }
        return httpResponse;
    }

    private ResponseEntity<byte[]> setNoPhoto() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
            ClassPathResource noImg = new ClassPathResource("/static/images/NoPhoto.jpg");
            return new ResponseEntity<>(noImg.getInputStream().readAllBytes(), headers, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("NoPhoto.jpg not found", e);
        }
        return null;
    }

    public int computeAge(Individu individu) {
        Period agePeriod = Period.between(individu.getDateOfBirth(), LocalDate.now());
        return agePeriod.getYears();
    }

    public List<String> getAllFixCP() {
        return individuRepository.findAllFixCP();
    }

    public List<Integer> getAllDateOfBirth() {
        return individuRepository.findAllDateOfBirthDistinct();
    }


   /* @Transactional
    public void anonymiseIndividu(Long individuId) {
        Individu individu = individuRepository.findById(individuId).orElse(null);{
            if (individu != null) {
                int yearOfBirth = individu.getDateOfBirth().getYear();
                individu.setNumEtu("Anonyme" + individu.getId());
                individu.setCodeIne("Anonyme" + individu.getId());
                individu.setName("Anonyme");
                individu.setFirstName("Anonyme");
                individu.setDateOfBirth(LocalDate.of(yearOfBirth, Month.JANUARY, 1));
                individu.setEppn("example@example.org");
                individu.setEmailEtu("exampleetu@example.org");
                individu.setContactPhone("0000000000");
                individu.setFixAddress("");
                individu.setFixCity("");
                individu.setFixCP(individu.getFixCP().substring(0,2));
                List<Dossier> dossiers = individu.getDossiers();
                for (Dossier dossier : dossiers) {
                    List<AideHumaine> aidesHumaines = dossier.getAidesHumaines();
                    for (AideHumaine aideHumaine : aidesHumaines) {
                        int aidantYearOfBirth = aideHumaine.getDateOfBirthAidant().getYear();
                        aideHumaine.setNumEtuAidant("AnonymeAidant" + aideHumaine.getId());
                        aideHumaine.setNameAidant("AnonymeAidant");
                        aideHumaine.setFirstNameAidant("AnonymeAidant");
                        aideHumaine.setDateOfBirthAidant(LocalDate.of(aidantYearOfBirth, Month.FEBRUARY, 1));
                        aideHumaine.setEmailAidant("exampleAidant@example.org");
                        aideHumaine.setPhoneAidant("0000000000");
                    }
                }
                dossierService.anonymiseDossiers(individu);
            }
        }
    }*/

    @Transactional
    public void anonymiseIndividu(Long individuId) {
        Individu individu = individuRepository.findById(individuId).orElse(null);
        if (individu != null && !individu.getNumEtu().startsWith("Anonyme")) {
            logger.info("anonymise " + individu.getNumEtu());
            individu.setNumEtu("Anonyme" + individu.getId());
            individu.setCodeIne("Anonyme" + individu.getId());
            individu.setName("Anonyme");
            individu.setFirstName("Anonyme");
            if(individu.getDateOfBirth() != null) {
                int yearOfBirth = individu.getDateOfBirth().getYear();
                individu.setDateOfBirth(LocalDate.of(yearOfBirth, Month.JANUARY, 1));
            }
            individu.setEppn("example@example.org");
            individu.setEmailEtu("exampleetu@example.org");
            individu.setContactPhone("0000000000");
            individu.setFixAddress("");
            individu.setFixCity("");
            if(StringUtils.hasText(individu.getFixCP())) {
                individu.setFixCP(individu.getFixCP().substring(0, 2));
            }

            List<Dossier> dossiers = individu.getDossiers();
            LocalDate currentDate = LocalDate.now();
            LocalDate anonymisationDateLimit = individu.getDateAnonymisation();

            boolean hasHelpPreviousYear = false;

            if (anonymisationDateLimit != null) {
                for (Dossier dossier : dossiers) {
                    List<AideHumaine> aidesHumaines = dossier.getAidesHumaines();
                    for (AideHumaine aideHumaine : aidesHumaines) {
                        LocalDate startDate = LocalDate.from(aideHumaine.getStartDate());
                        if (startDate.getYear() == anonymisationDateLimit.getYear() - 1) {
                            hasHelpPreviousYear = true;

                        }
                    }
                }
            }
            if (!hasHelpPreviousYear && dossiers.size() > 1) {
                for (Dossier dossier : dossiers) {
                    List<AideHumaine> aidesHumaines = dossier.getAidesHumaines();
                    for (AideHumaine aideHumaine : aidesHumaines) {
                        if(aideHumaine.getDateOfBirthAidant() != null) {
                            int aidantYearOfBirth = aideHumaine.getDateOfBirthAidant().getYear();
                            aideHumaine.setDateOfBirthAidant(LocalDate.of(aidantYearOfBirth, Month.FEBRUARY, 1));
                        }
                        aideHumaine.setNumEtuAidant("AnonymeAidant" + aideHumaine.getId());
                        aideHumaine.setNameAidant("AnonymeAidant");
                        aideHumaine.setFirstNameAidant("AnonymeAidant");
                        aideHumaine.setEmailAidant("exampleAidant@example.org");
                        aideHumaine.setPhoneAidant("0000000000");
                    }
                }
            }
            individu.setDateAnonymisation(currentDate);
//            dossierService.anonymiseDossiers(individu);
        }
    }

    @Transactional
    public void anonymiseAll() {
        List<Individu> individus = individuRepository.findAll();
        for (Individu individu : individus) {
            if (individu.getDossiers().stream().sorted(Comparator.comparingInt(Dossier::getYear).reversed()).toList().get(0).getYear() <= utilsService.getCurrentYear() - applicationProperties.getAnonymiseDelay()) {
                anonymiseIndividu(individu.getId());
            }
        }
    }

    @Transactional
    public void anonymiseOldDossiers() {
        if(applicationProperties.getNbDossierNullBeforeAnonymise() > -1) {
            List<Individu> individus = getAllIndividus();
            for (Individu individu : individus) {
                long countDossiers = individu.getDossiers().stream()
                        .filter(d -> d.getYear() >= utilsService.getCurrentYear() - applicationProperties.getNbDossierNullBeforeAnonymise())
                        .count();
                if (countDossiers == 0) {
                    anonymiseIndividu(individu.getId());
                }
            }
        }
    }

    @Transactional
    public void fusion(List<Long> ids) throws AgapeException {
        ids = ids.stream().sorted(Comparator.comparingLong(Long::longValue).reversed()).toList();
        Individu individu1 = findById(ids.get(0));
        Individu individu2 = findById(ids.get(1));
        if(individu1.getDateOfBirth().equals(individu2.getDateOfBirth())) {
            for (Dossier dossier : individu2.getDossiers()) {
                individu1.getDossiers().add(dossier);
                dossier.setIndividu(individu1);
            }
            anonymiseIndividu(individu2.getId());
        } else {
            throw new AgapeRuntimeException("la date de naissance ne correspond pas");
        }
    }
}
