package org.esupportail.esupagape.repository;

import org.esupportail.esupagape.entity.Amenagement;
import org.esupportail.esupagape.entity.enums.StatusAmenagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScolariteRepository extends JpaRepository <Amenagement, Long>{
    @Query("select distinct a from Amenagement a join Dossier d on a.dossier = d join Individu i on d.individu = i  " +
            "where  " +
            "(:statusAmenagement is null or a.statusAmenagement = :statusAmenagement) " +
            "and (:codComposante is null or a.dossier.codComposante  = :codComposante) " +
            "and a.statusAmenagement = 'VISE_ADMINISTRATION'" +
            "and (:yearFilter is null or d.year = :yearFilter)")

    Page<Amenagement> findByFullTextSearchScol(StatusAmenagement statusAmenagement, String codComposante, Integer yearFilter, Pageable pageable);

    @Query("select a from Amenagement a join Dossier d on a.dossier = d join Individu i on d.individu = i " +
            "where (upper(i.firstName) like upper(concat('%', :fullTextSearch))) " +
            "or (upper(concat(i.name, ' ', i.firstName)) like upper(concat('%', :fullTextSearch, '%'))) " +
            "or (upper(concat(i.firstName, ' ', i.name)) like upper(concat('%', :fullTextSearch, '%'))) " +
            "and (:yearFilter is null or d.year = :yearFilter) " +
            "and (:codComposante is null or a.dossier.codComposante  = :codComposante) " +
            "and a.statusAmenagement = 'VISE_ADMINISTRATION' ")
    Page<Amenagement> findByIndividuNameScol(@Param("fullTextSearch") String fullTextSearch,
                                                 @Param("yearFilter") Integer yearFilter,
                                                 Pageable pageable);

}
