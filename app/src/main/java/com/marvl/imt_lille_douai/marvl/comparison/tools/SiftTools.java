package com.marvl.imt_lille_douai.marvl.comparison.tools;

public class SiftTools {

    // Le nombre de meilleurs caractéristiques à retenir. Les caractéristiques sont classées par leurs scores (mesuré dans l'algorithme SIFT comme le contraste local).
    public static int nFeatures = 0;

    // Nombre de couche dans chaque octave (3 est la valeur utilisée avec D.Lowe). Le nombre d'octave est calculé automatiquement à partir de la résolution de l'image.
    public static int nOctaveLayers = 3;

    // Seuil de contraste utilisé pour filtrer les caractéristiques des régions à faible contraste. Plus le seuil est important, moins les caractéristiques sont produites par le détecteur.
    public static double contrastThreshold = 0.03;

    // Seuil utilisé pour filtrer les caractéristiques de pointe. Plus la valeur est importante moins les caractéristiques sont filtrées
    public static int edgeThresold = 10;

    // sigma gaussien appliqué à l'image d'entrée à l'octave \ # 0. Réduire le nombre si image capturée est de faible qualité
    public static double sigma = 1.6;
}