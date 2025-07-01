package com.pitanguinha.streaming.enums.media.music;

/**
 * Enum representing various moods that can be associated with music.
 * 
 * @since 1.0
 */
public enum Mood {
    HAPPY("Happy"),
    SAD("Sad"),
    CALM("Calm"),
    EXCITED("Excited"),
    RELAXED("Relaxed"),
    MOTIVATED("Motivated"),
    NOSTALGIC("Nostalgic"),
    ROMANTIC("Romantic"),
    ENERGETIC("Energetic"),
    MELANCHOLIC("Melancholic"),
    PEACEFUL("Peaceful"),
    INTENSE("Intense"),
    CHILLED("Chilled"),
    FUNKY("Funky"),
    SOULFUL("Soulful"),
    DREAMY("Dreamy"),
    SPIRITUAL("Spiritual"),
    MYSTERIOUS("Mysterious"),
    DARK("Dark"),
    PLAYFUL("Playful"),
    SERENE("Serene"),
    VIBRANT("Vibrant");

    public final String moodName;

    Mood(String moodName) {
        this.moodName = moodName;
    }
}
