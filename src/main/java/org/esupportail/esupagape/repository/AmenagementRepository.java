package org.esupportail.esupagape.repository;

import org.esupportail.esupagape.entity.Amenagement;
import org.esupportail.esupagape.entity.Individu;
import org.esupportail.esupagape.entity.enums.StatusAmenagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenagementRepository extends JpaRepository<Amenagement, Long> {

    Page<Amenagement> findByDossierId(Long dossierId, Pageable pageable);

    @Query("select a from Amenagement a where a.dossier.id = :dossierId and a.statusAmenagement = :statusAmenagement order by a.administrationDate desc")
    List<Amenagement> findByDossierIdAndStatusAmenagement(Long dossierId, StatusAmenagement statusAmenagement);

    List<Amenagement> findByStatusAmenagement(StatusAmenagement statusAmenagement);

    List<Amenagement> findByStatusAmenagementAndDossierYear(StatusAmenagement statusAmenagement, int year);

    @Query("select distinct a from Amenagement a join Dossier d on a.dossier = d " +
            "where " +
            "(:statusAmenagement is null or a.statusAmenagement = :statusAmenagement) " +
            "and (:codComposante is null or a.dossier.codComposante  = :codComposante) " +
            "and a.statusAmenagement != 'BROUILLON' and a.statusAmenagement != 'SUPPRIME'" +
            "and (:yearFilter is null or d.year = :yearFilter)")
    Page<Amenagement> findByFullTextSearch(StatusAmenagement statusAmenagement, String codComposante, Integer yearFilter, Pageable pageable);


    @Query("select a from Amenagement a join Dossier d on a.dossier = d join Individu i on d.individu = i " +
            "where (upper(i.firstName) like upper(concat('%', :fullTextSearch))) " +
            "or (upper(concat(i.name, ' ', i.firstName)) like upper(concat('%', :fullTextSearch, '%'))) " +
            "or (upper(concat(i.firstName, ' ', i.name)) like upper(concat('%', :fullTextSearch, '%'))) " +
            "or (upper(d.individu.numEtu) = upper(:fullTextSearch)) " +
            "or (upper(d.individu.codeIne) = upper(:fullTextSearch)) " +
            "and (d.year < :yearFilter) " +
            "and d.individu.desinscrit != true " +
            "and a.statusAmenagement = 'VISE_ADMINISTRATION' " +
            "and a.typeAmenagement = 'CURSUS'")
    Page<Amenagement> findByIndividuNamePortable(@Param("fullTextSearch") String fullTextSearch,
                                                 @Param("yearFilter") Integer yearFilter,
                                                 Pageable pageable);


    @Query("select count(a) from Amenagement a join Dossier d on a.dossier = d " +
            "where " +
            "a.statusAmenagement = 'VALIDE_MEDECIN' " +
            "and (:yearFilter is null or d.year = :yearFilter)")
    Long countToValidate(Integer yearFilter);

    @Query("""
            select distinct a from Amenagement a
            join Dossier d on a.dossier = d
            where (:codComposante is null or a.dossier.codComposante  = :codComposante)
            and a.statusAmenagement = 'VISE_ADMINISTRATION'
            and (d.year < :yearFilter)
            and (select count(*) from Dossier d1 where d1.individu = d.individu and d1.amenagementPorte = a) = 0
            and a.typeAmenagement = 'CURSUS'
            and (d.individu.desinscrit is null or d.individu.desinscrit = false)
            """)
    Page<Amenagement> findByFullTextSearchPortable(String codComposante, Integer yearFilter, Pageable pageable);

    @Query("""
            select count(distinct a) from Amenagement a
            left join Dossier d on a.dossier = d
            where a.statusAmenagement = 'VISE_ADMINISTRATION'
            and (d.year < :currentYear)
            and (select count(*) from Dossier d1 where d1.individu = d.individu and d1.amenagementPorte = a) = 0
            and a.typeAmenagement = 'CURSUS'
            and (d.individu.desinscrit is null or d.individu.desinscrit = false)
            """)
    Long countToPorte(Integer currentYear);

    @Query(value = "select a from Amenagement a " +
            "join Dossier d on a.dossier = d " +
            "join Individu i on d.individu = i " +
            "where " +
            "i = :individu " +
            "and a.statusAmenagement = 'VISE_ADMINISTRATION' " +
            "and (d.year < :yearFilter)")
    List<Amenagement> findAmenagementPrec(Individu individu, Integer yearFilter);

    @Query(value = "select a from Amenagement a " +
            "where " +
            "(:statusAmenagement is null or a.statusAmenagement = :statusAmenagement) " +
            "and (:codComposante is null or a.dossier.codComposante  = :codComposante) " +
            "order by a.administrationDate desc")
    Page<Amenagement> findByFullTextSearchAdmin(StatusAmenagement statusAmenagement, String codComposante, Pageable pageable);

}
