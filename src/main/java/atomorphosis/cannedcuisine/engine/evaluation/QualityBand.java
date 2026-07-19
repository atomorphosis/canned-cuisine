package atomorphosis.cannedcuisine.engine.evaluation;

public enum QualityBand {
    FAILED,
    QUESTIONABLE,
    STANDARD,
    GOOD,
    EXCELLENT,
    EXCEPTIONAL;

    public static QualityBand fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Quality score must be in the range [0, 100]");
        }
        if (score < 20) {
            return FAILED;
        }
        if (score < 40) {
            return QUESTIONABLE;
        }
        if (score < 60) {
            return STANDARD;
        }
        if (score < 80) {
            return GOOD;
        }
        if (score < 95) {
            return EXCELLENT;
        }
        return EXCEPTIONAL;
    }
}
