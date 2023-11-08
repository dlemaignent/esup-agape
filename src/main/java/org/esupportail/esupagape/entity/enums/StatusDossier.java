package org.esupportail.esupagape.entity.enums;

public enum StatusDossier {

    IMPORTE("rgb(208,201,226)", "rgba(208,201,226, 0.3)", "#5b5b5b"),
    AJOUT_MANUEL("rgb(61,133,198)", "rgba(61,133,198, 0.2)", "#5b5b5b"),
    RECU_PAR_LA_MEDECINE_PREVENTIVE("rgb(205,247,249) ", "rgba(205,247,249, 0.3)", "#5b5b5b"),
    RECONDUIT("rgb(255,191,151)", "rgba(255,191,151, 0.3)", "#5b5b5b"),
    NON_RECONDUIT("rgb(248,215,253)", "rgba(248,215,253, 0.3)", "#5b5b5b"),
    ACCUEILLI("rgb(255,253,204)", "rgba(255,253,204, 0.3)", "#5b5b5b"),
    SUIVI("rgb(182,215,168)", "rgba(182,215,168, 0.3)", "#5b5b5b"),
    IMPOSSIBLE_A_CONTACTER("rgb(234,153,153)", "rgba(234,153,153, 0.3)", "#5b5b5b"),
    ANONYMOUS("rgb(225,221,210)", "rgba(225,221,210, 0.3)", "#5b5b5b");
    //DESINSCRIT

    final String badgeColor;

    final String backgroundColor;

    final String textColor;

    StatusDossier(String badgeColor, String backgroundColor, String textColor) {
        this.badgeColor = badgeColor;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }
}
