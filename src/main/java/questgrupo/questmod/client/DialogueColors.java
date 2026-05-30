package questgrupo.questmod.client;

public class DialogueColors {
    public static int outerBg = 0xFF655746;
    public static int mainBg = 0xFF4C4238;
    public static int trazoExterior = 0xFF7D7060;
    public static int trazoRecuadro = 0xFF403A30;
    public static int trazoInterior = 0xFF736351;
    public static int separator = 0xFF312D26;
    public static int separatorSombra = 0xFF5A4D42;
    public static int textNPC = 0xFFCECECF;
    public static int btnBgNorm = 0xFF665A48;
    public static int btnLight = 0xFF837563;
    public static int btnDark = 0xFF383028;
    public static int btnText = 0xFFF2F2F2;
    public static int btnOutline = 0xFF201C17;
    public static int hoverGreen = 0xFF4A792A;
    public static int hoverLight = 0xFF73BD42;
    public static int hoverDark = 0xFF36591F;

    public static void restaurarDefaults() {
        outerBg = 0xFF655746;
        mainBg = 0xFF4C4238;
        trazoExterior = 0xFF7D7060;
        trazoRecuadro = 0xFF403A30;
        trazoInterior = 0xFF736351;
        separator = 0xFF312D26;
        separatorSombra = 0xFF5A4D42;
        textNPC = 0xFFCECECF;
        btnBgNorm = 0xFF665A48;
        btnLight = 0xFF837563;
        btnDark = 0xFF383028;
        btnText = 0xFFF2F2F2;
        btnOutline = 0xFF201C17;
        hoverGreen = 0xFF4A792A;
        hoverLight = 0xFF73BD42;
        hoverDark = 0xFF36591F;
    }

    public static void apply(String target, int color) {
        switch (target) {
            case "outerBg": outerBg = color; break;
            case "mainBg": mainBg = color; break;
            case "trazoExterior": trazoExterior = color; break;
            case "trazoRecuadro": trazoRecuadro = color; break;
            case "trazoInterior": trazoInterior = color; break;
            case "separator": separator = color; break;
            case "separatorSombra": separatorSombra = color; break;
            case "textNPC": textNPC = color; break;
            case "btnBgNorm": btnBgNorm = color; break;
            case "btnLight": btnLight = color; break;
            case "btnDark": btnDark = color; break;
            case "btnText": btnText = color; break;
            case "btnOutline": btnOutline = color; break;
            case "hoverGreen": hoverGreen = color; break;
            case "hoverLight": hoverLight = color; break;
            case "hoverDark": hoverDark = color; break;
        }
    }

    public static int get(String target) {
        switch (target) {
            case "outerBg": return outerBg;
            case "mainBg": return mainBg;
            case "trazoExterior": return trazoExterior;
            case "trazoRecuadro": return trazoRecuadro;
            case "trazoInterior": return trazoInterior;
            case "separator": return separator;
            case "separatorSombra": return separatorSombra;
            case "textNPC": return textNPC;
            case "btnBgNorm": return btnBgNorm;
            case "btnLight": return btnLight;
            case "btnDark": return btnDark;
            case "btnText": return btnText;
            case "btnOutline": return btnOutline;
            case "hoverGreen": return hoverGreen;
            case "hoverLight": return hoverLight;
            case "hoverDark": return hoverDark;
        }
        return 0xFFFFFFFF;
    }
}
