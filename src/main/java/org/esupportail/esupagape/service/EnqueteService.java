package org.esupportail.esupagape.service;

import org.esupportail.esupagape.dtos.forms.EnqueteForm;
import org.esupportail.esupagape.entity.Amenagement;
import org.esupportail.esupagape.entity.Dossier;
import org.esupportail.esupagape.entity.Enquete;
import org.esupportail.esupagape.entity.EnqueteEnumFilFmtScoLibelle;
import org.esupportail.esupagape.entity.enums.*;
import org.esupportail.esupagape.entity.enums.enquete.*;
import org.esupportail.esupagape.exception.AgapeJpaException;
import org.esupportail.esupagape.exception.AgapeYearException;
import org.esupportail.esupagape.repository.EnqueteEnumFilFmtScoLibelleRepository;
import org.esupportail.esupagape.repository.EnqueteEnumFilFmtScoRepository;
import org.esupportail.esupagape.repository.EnqueteRepository;
import org.esupportail.esupagape.service.utils.UtilsService;
import org.esupportail.esupagape.service.utils.slimselect.SlimSelectData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class EnqueteService {

    private final EnqueteRepository enqueteRepository;

    private final EnqueteEnumFilFmtScoRepository enqueteEnumFilFmtScoRepository;

    private final EnqueteEnumFilFmtScoLibelleRepository enqueteEnumFilFmtScoLibelleRepository;

    private final DossierService dossierService;

    private final AmenagementService amenagementService;

    private final UtilsService utilsService;

    public EnqueteService(
            EnqueteRepository enqueteRepository,
            EnqueteEnumFilFmtScoRepository enqueteEnumFilFmtScoRepositoryRepository,
            EnqueteEnumFilFmtScoLibelleRepository enqueteEnumFilFmtScoLibelleRepository,
            DossierService dossierService,
            AmenagementService amenagementService,
            UtilsService utilsService) {
        this.enqueteRepository = enqueteRepository;
        this.enqueteEnumFilFmtScoRepository = enqueteEnumFilFmtScoRepositoryRepository;
        this.enqueteEnumFilFmtScoLibelleRepository = enqueteEnumFilFmtScoLibelleRepository;
        this.dossierService = dossierService;
        this.amenagementService = amenagementService;
        this.utilsService = utilsService;
    }

    public Enquete getById(Long id) throws AgapeJpaException {
        Optional<Enquete> optionalEnquete = enqueteRepository.findById(id);
        if (optionalEnquete.isPresent()) {
            return optionalEnquete.get();
        } else {
            throw new AgapeJpaException("Je n'ai pas trouvé cette enquête");
        }
    }

    @Transactional
    public void create(Enquete enquete, String eppn) {
        if(enquete.getDossier().getStatusDossier().equals(StatusDossier.AJOUT_MANUEL)
            ||
            enquete.getDossier().getStatusDossier().equals(StatusDossier.IMPORTE)) {
            dossierService.changeStatutDossier(enquete.getDossier().getId(), StatusDossier.SUIVI, eppn);

        }
        enqueteRepository.save(enquete);
    }

    @Transactional
    public void update(Long id, EnqueteForm enqueteForm, Long dossierId) throws AgapeJpaException {
        Enquete enqueteToUpdate = getById(id);
        if (enqueteToUpdate.getDossier().getYear() != utilsService.getCurrentYear()) {
            throw new AgapeYearException();
        }
        enqueteToUpdate.setSexe(enqueteForm.getSexe());
        if (enqueteForm.getCodSco() != null) {
            enqueteToUpdate.setCodSco(enqueteForm.getCodSco());
        }
        if (enqueteForm.getCodFmt() != null) {
            enqueteToUpdate.setCodFmt(enqueteForm.getCodFmt());
        }
        if (enqueteForm.getCodFil() != null) {
            enqueteToUpdate.setCodFil(enqueteForm.getCodFil());
        }
        if (enqueteForm.getCodHd() != null) {
            enqueteToUpdate.setCodHd(enqueteForm.getCodHd());
        }
        enqueteToUpdate.setHdTmp(enqueteForm.getHdTmp());
        enqueteToUpdate.setCom(enqueteForm.getCom());
        if (enqueteForm.getCodPfpp() != null) {
            enqueteToUpdate.setCodPfpp(enqueteForm.getCodPfpp());
        }
        enqueteToUpdate.getCodPfas().clear();
        if (enqueteForm.getCodPfasOn().equals("AS0")) {
            enqueteToUpdate.getCodPfas().add(CodPfas.AS0);
        } else {
            enqueteToUpdate.getCodPfas().add(CodPfas.AS1);
            enqueteToUpdate.getCodPfas().addAll(enqueteForm.getCodPfas());
        }
//        enqueteToUpdate.setCodPfas(enqueteForm.getCodPfas());
        enqueteToUpdate.getCodMeahF().clear();
        if (StringUtils.hasText(enqueteForm.getAHS0())) {
            enqueteToUpdate.getCodMeahF().add(CodMeahF.valueOf(enqueteForm.getAHS0()));
        } else {
            if (enqueteForm.getAHS1().size() > 0 || enqueteForm.getAHS2().size() > 0) {
                for (String AHS1 : enqueteForm.getAHS1()) {
                    enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS1);
                    enqueteToUpdate.getCodMeahF().add(CodMeahF.valueOf(AHS1));
                }
                for (String AHS2 : enqueteForm.getAHS2()) {
                    enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS2);
                    enqueteToUpdate.getCodMeahF().add(CodMeahF.valueOf(AHS2));
                }
                if (StringUtils.hasText(enqueteForm.getAHS3())) {
                    if (enqueteForm.getAHS3().equals("on")) {
                        enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS3);
                    } else {
                        enqueteToUpdate.getCodMeahF().remove(CodMeahF.AHS3);
                    }
                }
                if (StringUtils.hasText(enqueteForm.getAHS4())) {
                    if (enqueteForm.getAHS4().equals("on")) {
                        enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS4);
                    } else {
                        enqueteToUpdate.getCodMeahF().remove(CodMeahF.AHS4);
                    }
                }
                if (StringUtils.hasText(enqueteForm.getAHS5())) {
                    if (enqueteForm.getAHS5().equals("on")) {
                        enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS5);
                    } else {
                        enqueteToUpdate.getCodMeahF().remove(CodMeahF.AHS5);
                    }
                }
            } else {
                enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS0);
            }
//            if (StringUtils.hasText(enqueteForm.getAHS5())) {
//                enqueteToUpdate.getCodMeahF().add(CodMeahF.AHS5);
//                enqueteToUpdate.getCodMeahF().add(CodMeahF.valueOf(enqueteForm.getAHS5()));
//            }
        }

        enqueteToUpdate.getCodAmL().clear();
        if (StringUtils.hasText(enqueteForm.getAM0())) {
            enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM0()));
        } else {
            if (StringUtils.hasText(enqueteForm.getAM1())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM1()));
            }
            if (StringUtils.hasText(enqueteForm.getAM2())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM2()));
            }
            if (StringUtils.hasText(enqueteForm.getAM3())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM3()));
            }
            if (StringUtils.hasText(enqueteForm.getAM4())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM4()));
            }
            if (StringUtils.hasText(enqueteForm.getAM5())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM5()));
            }
            if (StringUtils.hasText(enqueteForm.getAM6())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM6()));
            }
            if (StringUtils.hasText(enqueteForm.getAM7())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM7()));
            }
            if (StringUtils.hasText(enqueteForm.getAM8())) {
                enqueteToUpdate.getCodAmL().add(CodAmL.valueOf(enqueteForm.getAM8()));
            }
        }
        enqueteToUpdate.setAidHNat(enqueteForm.getAidHNat());
        enqueteToUpdate.setCodMeae(enqueteForm.getCodMeae());

        enqueteToUpdate.setAutAE(enqueteForm.getAutAE());
        enqueteToUpdate.getCodMeaa().clear();
        enqueteToUpdate.getCodMeaa().add(enqueteForm.getCodMeaaStructure());
        enqueteToUpdate.getCodMeaa().addAll(enqueteForm.getCodMeaa());
        enqueteToUpdate.setAutAA(enqueteForm.getAutAA());
        enqueteToUpdate.setDossier(dossierService.getById(dossierId));
    }

    private Enquete createByDossierId(Long id, String eppn) {
        Enquete enquete = new Enquete();
        Dossier dossier = dossierService.getById(id);
        enquete.setDossier(dossier);
        if(dossier.getStatusDossier().equals(StatusDossier.AJOUT_MANUEL)
                ||
                dossier.getStatusDossier().equals(StatusDossier.IMPORTE)) {
            dossierService.changeStatutDossier(id, StatusDossier.ACCUEILLI, eppn);
        }
        return enqueteRepository.save(enquete);
    }

    @Transactional
    public Enquete getAndUpdateByDossierId(Long id, String eppn) {
        Dossier dossier = dossierService.getById(id);
        Enquete enquete = enqueteRepository.findByDossierId(id).orElseGet(() -> createByDossierId(id, eppn));
        if (dossier.getYear() == utilsService.getCurrentYear()) {
            enquete.setAn(String.valueOf(dossier.getIndividu().getDateOfBirth().getYear()));
            if (dossier.getIndividu().getGender() != null) {
                if (dossier.getIndividu().getGender().equals(Gender.FEMININ)) {
                    enquete.setSexe("0");
                } else if (dossier.getIndividu().getGender().equals(Gender.MASCULIN)) {
                    enquete.setSexe("1");
                } else if (dossier.getIndividu().getGender().equals(Gender.NE_SAIS_PAS)) {
                    enquete.setSexe("2");
                }
            } else {
                enquete.setSexe("2");
            }

            if (enquete.getCodMeahF().isEmpty() || enquete.getCodMeahF().contains(CodMeahF.AHS0)) {
                enquete.getCodMeahF().clear();
                if (dossier.getAidesHumaines().stream().anyMatch(ah -> ah.getFonctionAidants().contains(FonctionAidant.PRENEUR_NOTES))) {
                    enquete.getCodMeahF().add(CodMeahF.AHS3);
                }
                if (dossier.getAidesHumaines().stream().anyMatch(ah -> ah.getFonctionAidants().contains(FonctionAidant.TUTEUR_PEDAGO) || ah.getFonctionAidants().contains(FonctionAidant.TUTEUR_ACC))) {
                    enquete.getCodMeahF().add(CodMeahF.AHS5);
                }
            }
            if (enquete.getTypFrmn() == null) {
                enquete.setTypFrmn(dossier.getTypeFormation());
            }
            if (dossier.getModeFormation() != null) {
                enquete.getModFrmn().clear();
                enquete.getModFrmn().add(dossier.getModeFormation());
                if (dossier.getAlternance()) {
                    enquete.getModFrmn().add(ModFrmn.A);
                }
            }
            Amenagement amenagement = amenagementService.isAmenagementValid(id);
            if (amenagement != null) {
                enquete.getCodMeae().add(CodMeae.AE4);
                enquete.getCodMeae().remove(CodMeae.AE0);
                if (amenagement.getTempsMajore() != null || StringUtils.hasText(amenagement.getAutresTempsMajores())) {
                    enquete.getCodMeae().add(CodMeae.AE7);
                }
            }
            if (StringUtils.hasText(enquete.getAutAE())) {
                enquete.getCodMeae().add(CodMeae.AEO);
                enquete.getCodMeae().remove(CodMeae.AE0);
            } else {
                enquete.getCodMeae().remove(CodMeae.AEO);
            }
            if (enquete.getCodMeae().isEmpty()) {
                enquete.getCodMeae().add(CodMeae.AE0);
            }
            enquete.setHdTmp(false);
            enquete.setCodHd(null);
            if (dossier.getStatusDossier() != null && dossier.getStatusDossier().equals(StatusDossier.ACCUEILLI)) {
                enquete.setCodMeaa(Collections.singleton(CodMeaa.AA1));
            } else if (dossier.getStatusDossier() != null && (dossier.getStatusDossier().equals(StatusDossier.SUIVI) || dossier.getStatusDossier().equals(StatusDossier.RECU_PAR_LA_MEDECINE_PREVENTIVE) || dossier.getStatusDossier().equals(StatusDossier.RECONDUIT))) {
                enquete.setCodMeaa(Collections.singleton(CodMeaa.AA2));
            }
            if (dossier.getClassifications().size() > 2) {
                enquete.setCodHd(CodHd.PTA);
                if (dossier.getClassifications().contains(Classification.TEMPORAIRE)) {
                    enquete.setHdTmp(true);
                }
            } else if (dossier.getClassifications().size() == 2 && dossier.getClassifications().contains(Classification.TEMPORAIRE)) {
                for (Classification classification : dossier.getClassifications()) {
                    if (classification.equals(Classification.TEMPORAIRE)) {
                        enquete.setHdTmp(true);
                    } else {
                        enquete.setCodHd(getClassificationEnqueteMap().get(classification));
                    }
                }
            } else if (dossier.getClassifications().size() == 2) {
                enquete.setCodHd(CodHd.PTA);
            } else if (dossier.getClassifications().size() == 1) {
                if (dossier.getClassifications().stream().toList().get(0).equals(Classification.TEMPORAIRE)) {
                    enquete.setHdTmp(true);
                } else {
                    enquete.setCodHd(getClassificationEnqueteMap().get(dossier.getClassifications().stream().toList().get(0)));
                }
            }
        }
        clearButKeepExceptions(enquete);
        if (!dossier.getMdphs().isEmpty()) {
            if (dossier.getMdphs().contains(Mdph.PCH_AIDE_HUMAINE) ||
                    dossier.getMdphs().contains(Mdph.PCH_AIDE_TECHNIQUE)) {
                enquete.getCodAmL().add(CodAmL.AM21);
            } else {
                enquete.getCodAmL().add(CodAmL.AM20);
            }

            if (dossier.getMdphs().contains(Mdph.TRANSPORT_INDIVIDUEL_ADAPTE)) {
                enquete.getCodAmL().add(CodAmL.AM31);
            } else {
                enquete.getCodAmL().add(CodAmL.AM30);
            }
            if (dossier.getMdphs().contains(Mdph.RQTH)) {
                enquete.getCodAmL().add(CodAmL.AM41);
            } else {
                enquete.getCodAmL().add(CodAmL.AM40);
            }

            if (dossier.getMdphs().contains(Mdph.AEEH)) {
                enquete.getCodAmL().add(CodAmL.AM51);
            } else {
                enquete.getCodAmL().add(CodAmL.AM50);
            }
            if (dossier.getMdphs().contains(Mdph.AAH) ||
                    dossier.getMdphs().contains(Mdph.CARTE_INVALIDITE) ||
                    dossier.getMdphs().contains(Mdph.CARTE_PRIORITE) ||
                    dossier.getMdphs().contains(Mdph.CARTE_INVALIDITE_PRIORITE)) {
                enquete.getCodAmL().add(CodAmL.AM81);
            } else {
                enquete.getCodAmL().add(CodAmL.AM80);
            }
        }
        checkAllAmlX(enquete);
        return enquete;
    }

    private void clearButKeepExceptions(Enquete enquete) {
        Set<CodAmL> codAmLToKeep = new HashSet<>();
        codAmLToKeep.add(CodAmL.AM10);
        codAmLToKeep.add(CodAmL.AM11);
        codAmLToKeep.add(CodAmL.AM1X);
        codAmLToKeep.add(CodAmL.AM60);
        codAmLToKeep.add(CodAmL.AM61);
        codAmLToKeep.add(CodAmL.AM6X);
        codAmLToKeep.add(CodAmL.AM70);
        codAmLToKeep.add(CodAmL.AM71);
        codAmLToKeep.add(CodAmL.AM7X);
        enquete.getCodAmL().retainAll(codAmLToKeep);
    }

    private void checkAllAmlX(Enquete enquete) {
        Set<CodAmL> codAmLX = new HashSet<>();
        codAmLX.add(CodAmL.AM1X);
        codAmLX.add(CodAmL.AM2X);
        codAmLX.add(CodAmL.AM3X);
        codAmLX.add(CodAmL.AM4X);
        codAmLX.add(CodAmL.AM5X);
        codAmLX.add(CodAmL.AM6X);
        codAmLX.add(CodAmL.AM7X);
        codAmLX.add(CodAmL.AM8X);
        if(codAmLX.containsAll(enquete.getCodAmL())) {
            enquete.getCodAmL().clear();
            enquete.getCodAmL().add(CodAmL.AM00);
        }
    }

    public void detachAllByDossiers(long id) {
        List<Dossier> dossiers = dossierService.getAllByIndividu(id);
        for (Dossier dossier : dossiers) {
            Enquete enquete = enqueteRepository.findByDossierId(dossier.getId()).orElse(null);
            if (enquete != null) {
                enquete.setDossier(null);
            }
        }
    }

    public Map<Classification, CodHd> getClassificationEnqueteMap() {
        Map<Classification, CodHd> classificationMap = new HashMap<>();
        classificationMap.put(Classification.TROUBLES_DES_FONCTIONS_AUDITIVES, CodHd.AUD);
        classificationMap.put(Classification.MOTEUR, CodHd.MOT);
        classificationMap.put(Classification.TROUBLES_DES_FONCTIONS_VISUELLES, CodHd.VUE);
        classificationMap.put(Classification.TROUBLES_VISCERAUX, CodHd.VIS);
        classificationMap.put(Classification.TROUBLES_VISCERAUX_CANCER, CodHd.VIS0);
        classificationMap.put(Classification.TROUBLE_DU_LANGAGE_OU_DE_LA_PAROLE, CodHd.LNG);
        classificationMap.put(Classification.AUTISME, CodHd.TSA);
        classificationMap.put(Classification.NON_COMMUNIQUE, CodHd.TND);
        classificationMap.put(Classification.REFUS, CodHd.TND);
        classificationMap.put(Classification.AUTRES_TROUBLES, CodHd.AUT);
        classificationMap.put(Classification.TROUBLES_INTELLECTUELS_ET_COGNITIFS, CodHd.COG);
        classificationMap.put(Classification.TROUBLES_PSYCHIQUES, CodHd.PSY);
        return classificationMap;
    }

    public List<String> getCodFils() {
        return enqueteEnumFilFmtScoRepository.findDistinctByCodScoIsNull();
    }

    public List<String> getCodFmtByCodFil(String codFil) {
        return enqueteEnumFilFmtScoRepository.findDistinctByCodFil(codFil);
    }

    public List<String> getCodScoByCodFmt(String codFmt) {
        return enqueteEnumFilFmtScoRepository.findDistinctByCodFmt(codFmt);
    }

    public Map<String, String> getAllCodFmt() {
        Map<String, String> codFmts = new HashMap<>();
        List<EnqueteEnumFilFmtScoLibelle> enqueteEnumFilFmtScoLibelles = enqueteEnumFilFmtScoLibelleRepository.findAll();
        for (EnqueteEnumFilFmtScoLibelle enqueteEnumFilFmtScoLibelle : enqueteEnumFilFmtScoLibelles) {
            codFmts.put(enqueteEnumFilFmtScoLibelle.getCod().toLowerCase(), enqueteEnumFilFmtScoLibelle.getLibelle());
        }
        return codFmts;
    }

    public List<SlimSelectData> getSlimSelectDtosOfCodFmts(String codFil) {
        List<String> codFmts = getCodFmtByCodFil(codFil);
        List<SlimSelectData> slimSelectDtos = new ArrayList<>();
        if (codFmts.size() > 0) {
            slimSelectDtos.add(new SlimSelectData("", ""));
            for (String codFmt : codFmts) {
                slimSelectDtos.add(new SlimSelectData(enqueteEnumFilFmtScoLibelleRepository.findByCod("FMT" + codFmt), codFmt));
            }
        }
        return slimSelectDtos;
    }

    public List<SlimSelectData> getSlimSelectDtosOfCodScos(String codFmt) {
        List<String> codScos = getCodScoByCodFmt(codFmt);
        List<SlimSelectData> slimSelectDtos = new ArrayList<>();
        if (codScos.size() > 0) {
            slimSelectDtos.add(new SlimSelectData("", ""));
            for (String codSco : codScos) {
                SlimSelectData slimSelectDto = new SlimSelectData(enqueteEnumFilFmtScoLibelleRepository.findByCod("SCO" + codSco), codSco);
                if (slimSelectDto.getValue() != null) {
                    slimSelectDtos.add(slimSelectDto);
                }
            }
        }
        return slimSelectDtos;
    }

    public Enquete findByDossierId(Long dossierId) {
        return enqueteRepository.findByDossierId(dossierId).get();
    }

    @Transactional
    public void finished(Long enqueteId) {
        Enquete enquete = getById(enqueteId);
        if (enquete.getFinished() != null) {
            enquete.setFinished(!enquete.getFinished());
        } else {
            enquete.setFinished(true);
        }
    }

    public List<Enquete> findAllByDossierYear(int year) {
        return enqueteRepository.findAllByDossierYear(year);

    }
}
