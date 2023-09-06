package org.esupportail.esupagape.service.externalws.apogee;

import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.IdentifiantsEtudiantDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@ConditionalOnBean(ApogeeEtuFactory.class)
public class WsApogeeServiceEtudiant {

	private static final Logger logger = LoggerFactory.getLogger(WsApogeeServiceEtudiant.class);

	private final ApogeeEtuFactory apogeeEtuFactory;

	public WsApogeeServiceEtudiant(ApogeeEtuFactory apogeeEtuFactory) {
		this.apogeeEtuFactory = apogeeEtuFactory;
	}

	public String recupererIdentifiantsEtudiant(String nom, String prenom, String dateNaiss) {
		logger.debug("recup infos etudiant dans apogee.");
		String idEtu = "";
		try {
			if(!dateNaiss.equals("%")) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date date = formatter.parse(dateNaiss);
				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				dateNaiss = dateFormat.format(date);
			}
			System.err.println(dateNaiss);
			IdentifiantsEtudiantDTO2 identifiantsEtudiantDTO = apogeeEtuFactory.getInstanceEtudiant()
					.recupererIdentifiantsEtudiantV2(null, null, null, null,
							null, nom, prenom, dateNaiss, null);
			idEtu = identifiantsEtudiantDTO.getCodEtu().toString();
		} catch (Exception e) {
			logger.error("Erreur lors de la recup des infos", e);
		}
		return idEtu;

	}
	
	public InfoAdmEtuDTO4 recupererInfosAdmEtu(String numEtu) {
		logger.debug("recup infos administratives dans apogee : " + numEtu);
		InfoAdmEtuDTO4 infoEtudiant = null;
		try {
			infoEtudiant = apogeeEtuFactory.getInstanceEtudiant().recupererInfosAdmEtuV4(numEtu);
		} catch (Exception e) {
			logger.debug("Erreur lors de la recup des infos : "  + e.getMessage());
		}
		return infoEtudiant;
	}

	public CoordonneesDTO2 recupererAdressesEtudiant(String numEtu, String annee) {
		logger.debug("Recup infos adresse dans apogee : " + numEtu);
		CoordonneesDTO2 adresseEtudiant = null;
		try {
			adresseEtudiant = apogeeEtuFactory.getInstanceEtudiant().recupererAdressesEtudiantV2(numEtu, annee, "O");
		} catch (Exception e) {
			logger.debug("Erreur lors de la recup des infos : " + e.getMessage());
		}
		return adresseEtudiant;
	}
	
	public String recupererLogin(String nom, String prenom, String dateNaiss) {
		logger.debug("Recup login dans apogee.");
		String login = "";
		try {
		if(!dateNaiss.equals("%")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = formatter.parse(dateNaiss);
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			dateNaiss = dateFormat.format(date);
		}
		IdentifiantsEtudiantDTO2 identifiantsEtudiantDTO = apogeeEtuFactory.getInstanceEtudiant().recupererIdentifiantsEtudiantV2(
				null, null, null, null, null, nom, prenom,
				dateNaiss, null);
		login = identifiantsEtudiantDTO.getLoginAnnuaire();
		} catch (Exception e) {
			logger.error("Erreur lors de la recup du login", e);
		}
		return login;
	}

}
